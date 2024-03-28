
package com.eclipsesource.jaxrs.publisher.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.core.Feature;
import javax.ws.rs.ext.Provider;

import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;

import com.eclipsesource.jaxrs.publisher.JerseyContextFactory;
import com.eclipsesource.jaxrs.publisher.ServletConfiguration;
import com.eclipsesource.jaxrs.publisher.api.AnnotationUtils;
import com.eclipsesource.jaxrs.publisher.api.ApplicationDTO;
import com.eclipsesource.jaxrs.publisher.api.ApplicationRegistry;

/**
 *
 * @author mvrueden
 */
public class ApplicationRegistryImpl implements ApplicationRegistry {

    private final BundleContext bundleContext;
    private final Map<HttpService, Map<String, JerseyContext>> contextMap = new HashMap<>();
    private final List<Object> featureProviderList = new ArrayList<>();
    private final JerseyContextFactory jerseyContextFactory;
    private Configuration configuration;

    public ApplicationRegistryImpl(Configuration configuration, BundleContext bundleContext, JerseyContextFactory jerseyContextFactory) {
        this.configuration = Objects.requireNonNull(configuration);
        this.bundleContext = Objects.requireNonNull(bundleContext);
        this.jerseyContextFactory = Objects.requireNonNull(jerseyContextFactory);
    }

    /**
     * Add a specific resource to the httpService.
     * The service may has a {@link ApplicationPath} annotation which indicates
     * where the service should be deployed to (e.g. /services).
     * 
     * @param httpService The HTTP Service to attach the resource to
     * @param service The service to attach
     */
    public void addResource(HttpService httpService, Object service, Map<String, String> properties) {
        // a provider or feature must be registered to all already registered applications
        if (isFeatureOrProvider(service)) {
            featureProviderList.add(service);
            contextMap.values().stream()
                    .flatMap(value -> value.values().stream())
                    .forEach(context -> context.addResource(service));
        } else {
            // Otherwise create or update a context

            // Determine location (e.g. /services)
            String location = getApplicationPath(properties);

            // Get JerseyContext and create if not already created
            JerseyContext context = getContext(httpService, location);
            if (context == null) {
                contextMap.putIfAbsent(httpService, new HashMap<>());
                contextMap.get(httpService).put(location,
                        jerseyContextFactory.createJerseyContext(
                                bundleContext,
                                httpService,
                                new JerseyContextConfiguration()
                                        .withPublishDelay(configuration.getPublishDelay())
                                        .withRootPath(location)));
                context = contextMap.get(httpService).get(location);

                addFeatureAndProvider(context);
            }
            context.addResource(service);
        }
    }

    @Override
    public List<String> getEndpoints() {
        final List<String> endpoints = contextMap.values().stream()
                .flatMap(entry -> entry.entrySet().stream())
                .flatMap(e -> {
                    // get servlet path and ensure it ends with /
                    final String servletPath = !e.getKey().endsWith("/") ? e.getKey() + "/" : e.getKey();
                    // get endpoints
                    return e.getValue().getRootApplication().getResources()
                            .stream()
                            .filter(resource -> AnnotationUtils.hasAnnotation(resource, Path.class))
                            .map(resource -> AnnotationUtils.getAnnotation(resource, Path.class).value())
                            .filter(path -> path != null && !path.trim().isEmpty())
                            .map(path -> path.startsWith("/") ? path.substring(1, path.length()) : path)
                            .map(path -> servletPath + path);

                }).collect(Collectors.toList());
        return endpoints;
    }

    @Override
    public List<String> getRootPaths() {
        return contextMap.values().stream().flatMap(v -> v.keySet().stream()).collect(Collectors.toList());
    }

    @Override
    public List<ApplicationDTO> getAllApplications() {
        return contextMap.values().stream().flatMap(v -> v.entrySet().stream())
                .map(e -> {
                    final String context = e.getKey();
                    final RootApplication rootApplication = e.getValue().getRootApplication();
                    final ApplicationDTO dto = new ApplicationDTO(context);
                    dto.setResources(rootApplication.getResources());
                    dto.setProperties(rootApplication.getProperties());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean isApplicationReady(String context) {
        if (contextMap.isEmpty()) {
            return false;
        }
        return contextMap.values().stream()
                .map(v -> v.get(context))
                .filter(c -> c != null)
                .map(c -> c.isApplicationReady())
                .reduce((ready, ready2) -> ready.booleanValue() && ready2.booleanValue())
                .get();

    }

    private JerseyContext getContext(HttpService httpService, String location) {
        if (contextMap.get(httpService) != null &&
            contextMap.get(httpService).get(location) != null) {
            return contextMap.get(httpService).get(location);
        }
        return null;
    }

    private String getApplicationPath(Map<String, String> properties) {
        String applicationPath = properties.getOrDefault(Properties.APPLICATION_PATH, configuration.getDefaultRootPath());
        return prependSlashIfMissing(applicationPath);
    }

    public void removeResource(HttpService httpService, Object resource, Map<String, String> properties) {
        if (isFeatureOrProvider(resource)) {
            // Remove from all contexts
            featureProviderList.remove(resource);
            contextMap.values().stream()
                    .flatMap(value -> value.values().stream())
                    .forEach(c -> c.removeResource(resource));
        } else {
            final String applicationPath = getApplicationPath(properties);
            final JerseyContext jerseyContext = getContext(httpService, applicationPath);
            if (jerseyContext != null) {
                jerseyContext.removeResource(resource);
            }
        }
    }

    public List<JerseyContext> removeHttpService(HttpService service) {
        Map<String, JerseyContext> remove = contextMap.remove(service);
        if (remove != null) {
            return new ArrayList<>(remove.values());
        }
        return new ArrayList<>();
    }

    public void updateServletConfiguration(HttpService httpService, ServletConfiguration servletConfiguration) {
        if (contextMap.get(httpService) != null) {
            contextMap.get(httpService).values().forEach(context -> {
                context.updateServletConfiguration( servletConfiguration );
            });
        }
    }

    public void updateAppConfiguration(HttpService httpService, ServiceContainer applicationConfigurations) {
        if (contextMap.get(httpService) != null) {
            contextMap.get(httpService).values().forEach(context -> context.updateAppConfiguration( applicationConfigurations));
        }
    }

    public void updateConfiguration(HttpService httpService, Configuration configuration) {
        // update the default root path if it changed
        if (!this.configuration.getDefaultRootPath().equals(configuration.getDefaultRootPath())) {
            JerseyContext context = getContext(httpService, this.configuration.getDefaultRootPath());
            if (context != null) {
                JerseyContextConfiguration newConfiguration = new JerseyContextConfiguration()
                        .withPublishDelay(configuration.getPublishDelay())
                        .withRootPath(configuration.getDefaultRootPath());
                context.updateConfiguration(newConfiguration);
            }
        }

        // update all contexts according to configuration
        contextMap.values().stream().flatMap(map -> map.entrySet().stream()).forEach(entry -> {
            JerseyContextConfiguration newConfiguration = new JerseyContextConfiguration()
                    .withPublishDelay(configuration.getPublishDelay())
                    .withRootPath(entry.getKey());
            entry.getValue().updateConfiguration(newConfiguration);
        });

        this.configuration = configuration;
    }

    private void addFeatureAndProvider(JerseyContext context) {
        featureProviderList.forEach(resource -> context.addResource(resource));
    }

    private static String prependSlashIfMissing(String applicationPath) {
        if (!applicationPath.startsWith("/")) {
            return  "/" + applicationPath;
        }
        return applicationPath;
    }

    private static boolean isFeatureOrProvider(Object service) {
        return AnnotationUtils.hasAnnotation(service, Provider.class) || service instanceof Feature;
    }

}
