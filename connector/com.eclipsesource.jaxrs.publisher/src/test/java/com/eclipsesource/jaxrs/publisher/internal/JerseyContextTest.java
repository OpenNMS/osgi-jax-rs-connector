/*******************************************************************************
 * Copyright (c) 2012,2015,2024 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Holger Staudacher - initial API and implementation
 *    Ivan Iliev - added tests for ServletConfigurationTracker
 *    Benjamin Reed - test updates to newer Mockito, generics cleanup
 ******************************************************************************/
package com.eclipsesource.jaxrs.publisher.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.eclipsesource.jaxrs.publisher.ApplicationConfiguration;
import com.eclipsesource.jaxrs.publisher.ServletConfiguration;

@RunWith( MockitoJUnitRunner.class )
public class JerseyContextTest {

  private JerseyContext jerseyContext;
  @Mock
  private ServletContainer servletContainer;
  @Mock
  private HttpService httpService;

  @Mock
  private BundleContext bundleContext = mock( BundleContext.class );

  private RootApplication rootApplication;

  @Before
  public void setUp() {
    rootApplication = spy(new RootApplication());

    final Configuration configuration = createConfiguration( "/test", false, 23L );
    final JerseyContextConfiguration jerseyContextConfiguration = new JerseyContextConfiguration().withConfiguration(configuration);
    final JerseyContext original = new JerseyContext( httpService, jerseyContextConfiguration, rootApplication, new ServiceContainer(bundleContext),  mock( ServletConfiguration.class ));
    jerseyContext = spy( original );
  }

  @Test
  public void testAddResource() throws ServletException, NamespaceException {
    Object resource = new Object();

    jerseyContext.addResource( resource );

    verify( rootApplication ).addResource( resource );
    verify( httpService ).registerServlet(
            eq( "/test" ),
            any( HttpServlet.class ),
            nullable( Dictionary.class ),
            nullable( HttpContext.class )
    );
  }

  @Test
  public void testRemoveSingleResource() throws ServletException, NamespaceException {
    Object resource = new Object();

    jerseyContext.addResource( resource );
    jerseyContext.removeResource( resource );


    verify( rootApplication ).addResource( resource );
    verify( rootApplication ).removeResource( resource );
    verify( httpService ).registerServlet(
            eq( "/test" ),
            any( HttpServlet.class ),
            nullable( Dictionary.class ),
            nullable( HttpContext.class )
    );
    verify( httpService ).unregister( "/test" );
  }

  @Test
  public void testRemoveResource() throws ServletException, NamespaceException {
    when( rootApplication.hasResources() ).thenReturn( true );
    Object resource = new Object();
    Object resource2 = new Object();

    jerseyContext.addResource( resource );
    jerseyContext.addResource( resource2 );
    jerseyContext.removeResource( resource );


    verify( rootApplication ).addResource( resource );
    verify( rootApplication ).addResource( resource2 );
    verify( rootApplication ).removeResource( resource );
    verify( httpService ).registerServlet(
            eq( "/test" ),
            any( HttpServlet.class ),
            nullable( Dictionary.class ),
            nullable( HttpContext.class )
    );
    verify( httpService, never() ).unregister( "/test" );
  }

  @Test
  public void testEliminate() throws ServletException, NamespaceException {
    Object resource = new Object();
    Set<Object> list = new HashSet<Object>();
    list.add( resource );
    when( rootApplication.getResources() ).thenReturn( list );
    jerseyContext.addResource( resource );

    List<Object> resources = jerseyContext.eliminate();

    verify( rootApplication ).addResource( resource );
    verify( httpService ).unregister( "/test" );
    assertEquals( 1, resources.size() );
    assertEquals( resource, resources.get( 0 ) );
  }

  @Test
  public void testEliminateDoesNotFailWithException() throws ServletException, NamespaceException {
    doThrow( IllegalArgumentException.class ).when( httpService ).unregister( anyString() );

    Object resource = new Object();
    Set<Object> list = new HashSet<Object>();
    list.add( resource );
    when( rootApplication.getResources() ).thenReturn( list );
    jerseyContext.addResource( resource );

    List<Object> resources = jerseyContext.eliminate();

    verify( rootApplication ).addResource( resource );
    verify( httpService ).unregister( "/test" );
    assertEquals( 1, resources.size() );
    assertEquals( resource, resources.get( 0 ) );
  }

  @Test( expected = IllegalStateException.class )
  public void testConvertsServletException() throws ServletException, NamespaceException {
    doThrow( new ServletException() ).when( httpService ).registerServlet( anyString(),
                                                                           any( HttpServlet.class ),
                                                                           nullable( Dictionary.class ),
                                                                           nullable( HttpContext.class ) );

    jerseyContext.addResource( new Object() );
  }

  @Test( expected = IllegalStateException.class )
  public void testConvertsNamespaceException() throws ServletException, NamespaceException {
    doThrow( new NamespaceException( "test" ) )
      .when( httpService ).registerServlet( anyString(),
                                            any( HttpServlet.class ),
                                            nullable( Dictionary.class ),
                                            nullable( HttpContext.class ) );

    jerseyContext.addResource( new Object() );
  }

  @Test
  public void testDoesNotRegister_METAINF_SERVICES_LOOKUP_DISABLE() {
    Map<String, Object> properties = jerseyContext.getRootApplication().getProperties();

    assertEquals( false, properties.get( ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE ) );
  }

  @Test
  public void testRegisters_FEATURE_AUTO_DISCOVERY_DISABLE() {
    Map<String, Object> properties = jerseyContext.getRootApplication().getProperties();

    assertEquals( true, properties.get( ServerProperties.FEATURE_AUTO_DISCOVERY_DISABLE ) );
  }

  @Test
  public void testAddResourceWithServletConfigurationServicePresent() throws Exception {
    Configuration configuration = createConfiguration( "/test", false, 23L );
    ServletConfiguration servletConfigurationService = mock( ServletConfiguration.class );
    JerseyContext withConfiguration = spy( new JerseyContext( httpService,
                                                              configuration,
                                                              servletConfigurationService,
                                                              new ServiceContainer( mock( BundleContext.class ) ) ) );
    Object resource = new Object();

    withConfiguration.addResource( resource );

    verify( servletConfigurationService, times( 1 ) ).getHttpContext( any( HttpService.class ), anyString() );
    verify( servletConfigurationService, times( 1 ) ).getInitParams( any( HttpService.class ), anyString() );
  }

  @Test
  public void testAddsApplicationConfigurationsOnStart() {
    ServiceContainer container = new ServiceContainer( bundleContext );
    ServiceReference<Object> reference = mock( ServiceReference.class );
    ApplicationConfiguration appConfig = mock( ApplicationConfiguration.class );
    Map<String, Object> map = new HashMap<>();
    map.put( "foo", "bar" );
    when( appConfig.getProperties() ).thenReturn( map );
    when( bundleContext.getService( reference ) ).thenReturn( appConfig );
    container.add( reference );
    Configuration configuration = createConfiguration( "/test", false, 23L );

    JerseyContext jerseyContext = new JerseyContext( httpService, configuration, container);

    Map<String, Object> properties = jerseyContext.getRootApplication().getProperties();
    assertEquals( properties.get( "foo" ), "bar" );
  }

  @Test
  public void testUpdateConfigurationBeforeServletRegistered() {
    Configuration configuration = createConfiguration("/test2", true, 15000L);
    JerseyContextConfiguration jerseyContextConfiguration = new JerseyContextConfiguration().withConfiguration(configuration);

    jerseyContext.updateConfiguration( jerseyContextConfiguration );

    verify(jerseyContext).updateConfiguration( jerseyContextConfiguration );
    verify(jerseyContext, never()).safeUnregister( "/test" );
    verify(jerseyContext, never()).registerServletWhenNotAlreadyRegistered();
  }

  @Test
  public void testUpdateConfigurationAfterServletRegistered() {
    Object resource = new Object();
    jerseyContext.addResource( resource );
    Configuration configuration = createConfiguration( "/test2", true, 15000L );
    JerseyContextConfiguration jerseyContextConfiguration = new JerseyContextConfiguration().withConfiguration(configuration);

    jerseyContext.updateConfiguration( jerseyContextConfiguration );

    verify( jerseyContext ).updateConfiguration( jerseyContextConfiguration );
    verify( jerseyContext ).safeUnregister( "/test" );
    verify( jerseyContext, times( 2 ) ).registerServletWhenNotAlreadyRegistered();
  }

  @Test
  public void testUpdateAppConfiguration() {
    BundleContext context = mock( BundleContext.class );
    ServiceContainer container = new ServiceContainer( context );
    ServiceReference<Object> reference = mock( ServiceReference.class );
    ApplicationConfiguration appConfig = mock( ApplicationConfiguration.class );
    Map<String, Object> map = new HashMap<>();
    map.put( "foo", "bar" );
    when( appConfig.getProperties() ).thenReturn( map );
    when( context.getService( reference ) ).thenReturn( appConfig );
    container.add( reference );

    jerseyContext.updateAppConfiguration( container );

    verify( rootApplication ).addProperties( map );
  }

  @Test
  public void testUpdateServletConfigurationBeforeServletRegistered() {
    ServletConfiguration configuration = mock ( ServletConfiguration.class);

    jerseyContext.updateServletConfiguration( configuration );

    verify( jerseyContext ).updateServletConfiguration( configuration );
    verify( jerseyContext, never() ).safeUnregister( "/test" );
    verify( jerseyContext, never() ).registerServletWhenNotAlreadyRegistered();
  }

  @Test
  public void testUpdateServletConfigurationAfterServletRegistered() {
    Object resource = new Object();
    jerseyContext.addResource( resource );
    ServletConfiguration configuration = mock ( ServletConfiguration.class);

    jerseyContext.updateServletConfiguration( configuration );

    verify( jerseyContext ).updateServletConfiguration( configuration );
    verify( jerseyContext ).safeUnregister( "/test" );
    verify( jerseyContext, times( 2 ) ).registerServletWhenNotAlreadyRegistered();
  }

  private Configuration createConfiguration( String path, boolean wadlDisable, long publishDelay ) {
    Configuration configuration = mock( Configuration.class );
    when( configuration.getDefaultRootPath() ).thenReturn( path );
    when( configuration.getPublishDelay() ).thenReturn( publishDelay );
    return configuration;
  }

}
