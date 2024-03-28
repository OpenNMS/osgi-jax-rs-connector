package com.eclipsesource.jaxrs.karaf.itest;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.vmOption;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;

import java.io.File;

import javax.inject.Inject;

import org.apache.karaf.features.FeaturesService;
import org.hamcrest.core.StringContains;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;

import com.eclipsesource.jaxrs.sample.model.SimpleMessage;
import com.eclipsesource.jaxrs.sample.service.GreetingResource;


@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class FeatureIntegrationTest {

    @Inject
    FeaturesService featuresService;

    @Inject
    @Filter(value = "(com.eclipsesource.jaxrs.publish=false)")
    GreetingResource jaxrsGreetingResource;

    @Inject
    @Filter(value = "!(com.eclipsesource.jaxrs.publish=false)")
    GreetingResource greetingResource;

    @Configuration
    public Option[] config() {
        // URL definitions
        final MavenArtifactUrlReference karafUrl = maven()
                .groupId("org.apache.karaf")
                .artifactId("apache-karaf")
                .type("tar.gz")
                .versionAsInProject();
        final MavenUrlReference karafStandardRepo = maven().groupId("org.apache.karaf.features")
                .artifactId("standard")
                .classifier("features")
                .type("xml")
                .versionAsInProject();
        final MavenUrlReference projectFeatures = maven().groupId("org.opennms.eclipsesource.jaxrs")
                .artifactId("features")
                .classifier("features")
                .type("xml")
                .versionAsInProject();

        // Configure container
        Option[] options = new Option[0];

        // Enable debugging of container
        if (Boolean.getBoolean("debug")) {
            options = OptionUtils.combine(
                    options,
                    vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"));
        }

        // By default a custom localRepository property will not be used, so manually force the container to use it
        if (System.getProperty("localRepository") != null) {
            final String localRepository = System.getProperty("localRepository");
            options = OptionUtils.combine(
                options,
                editConfigurationFilePut(
                    "etc/org.ops4j.pax.url.mvn.cfg",
                    "org.ops4j.pax.url.mvn.defaultRepositories",
                    String.join(",\n", new String[] {
                            "file:" + localRepository + "@snapshots@id=default-repo",
                            "file:${karaf.home}/${karaf.default.repository}@id=system.repository@snapshots",
                            "file:${karaf.data}/kar@id=kar.repository@multi@snapshots",
                            "file:${karaf.base}/${karaf.default.repository}@id=child.system.repository@snapshots"
                    })));
        }

        // More options
        options = OptionUtils.combine(
                options,
                karafDistributionConfiguration().frameworkUrl(karafUrl)
                        .unpackDirectory(new File("target/exam"))
                        .useDeployFolder(false),

                keepRuntimeFolder(),

                KarafDistributionOption.features(karafStandardRepo, "http", "scr"),
                KarafDistributionOption.features(projectFeatures, "jax-rs-connector", "jax-rs-provider-jackson", "jax-rs-shell-commands"),

                mavenBundle().groupId("org.opennms.eclipsesource.jaxrs").artifactId("jax-rs-sample").versionAsInProject()
        );
        return options;
    }

    @Test
    public void testJaxRsConnectorFeatureIsInstalled() throws Exception {
        assertThat(featuresService.isInstalled(featuresService.getFeature("jax-rs-connector")), is(true));
        assertThat(featuresService.isInstalled(featuresService.getFeature("jax-rs-provider-jackson")), is(true));
    }

    @Test
    public void testJaxRsServiceIsPublished() throws Exception {
        assertThat(greetingResource, notNullValue());
        assertThat(jaxrsGreetingResource, notNullValue());
    }

    @Test
    @Ignore("Does not work as the jax-rs-sample is not registering a Json Message Body Reader/Writer. Disabling for now")
    public void testJaxRsClientCallReturnsAGreeting() throws Exception {
        // you can verify that it calls the HTTP service if you change the base url used to publish this consumer
        // see class  com.eclipsesource.jaxrs.sample.consumer.PublishConsumerService
        SimpleMessage message = jaxrsGreetingResource.greeting();
        assertThat(message.getMessage(), StringContains.containsString("Hello this is current time"));
    }
}
