/*******************************************************************************
 * Copyright (c) 2012,2024 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Holger Staudacher - initial API and implementation
 *    Ivan Iliev - added ServletConfiguration tests
 *    Benjamin Reed - test updates to newer Mockito, generics cleanup
 ******************************************************************************/
package com.eclipsesource.jaxrs.publisher.internal;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

import com.eclipsesource.jaxrs.publisher.ApplicationConfiguration;
import com.eclipsesource.jaxrs.publisher.ServletConfiguration;

@RunWith( MockitoJUnitRunner.class )
public class JAXRSConnectorTest {

  @Mock
  private ServiceReference<Object> httpServiceReference;
  @Mock
  private ServiceReference<Object> resourceServiceReference;
  @Mock
  private BundleContext bundleContext;
  @Mock
  private JerseyContext jerseyContext;

  private JAXRSConnector connector;

  @Before
  public void setUp() {
    // when( httpServiceReference.getPropertyKeys() ).thenReturn( new String[0] );
    when( resourceServiceReference.getPropertyKeys() ).thenReturn( new String[0] );

    JAXRSConnector originalConnector = new JAXRSConnector(
            bundleContext,
            /* mock jersey context factory */ (bundleContext, service, configuration) -> jerseyContext);
    connector = spy( originalConnector );
  }

  @Test
  public void testAddHttpService() {
    HttpService httpService = mock( HttpService.class );
    when( bundleContext.getService( httpServiceReference ) ).thenReturn( httpService );

    HttpService httpService2 = connector.addHttpService( httpServiceReference );

    assertSame( httpService, httpService2 );
  }

  @Test
  public void testRegisterHttpServiceBeforeResource() {
    mockHttpService();
    Object resource = new Object();
    when( bundleContext.getService( resourceServiceReference ) ).thenReturn( resource );

    connector.addHttpService( httpServiceReference );
    connector.addResource( resourceServiceReference );

    verify( jerseyContext ).addResource( resource );
  }

  @Test
  public void testUsesResourceCache() {
    HttpService httpService = mockHttpService();
    Object resource = new Object();
    when( bundleContext.getService( resourceServiceReference ) ).thenReturn( resource );
    ArrayList<Object> resourceList = new ArrayList<Object>();
    resourceList.add( resource );
    when( jerseyContext.eliminate() ).thenReturn( resourceList );

    connector.addHttpService( httpServiceReference );
    connector.addResource( resourceServiceReference );
    connector.removeHttpService( httpService );
    connector.addHttpService( httpServiceReference );

    verify( jerseyContext, times( 2 ) ).addResource( resource );
  }

  @Test
  public void testRegisterHttpServiceAfterResource() {
    mockHttpService();
    Object resource = new Object();
    when( bundleContext.getService( resourceServiceReference ) ).thenReturn( resource );

    connector.addResource( resourceServiceReference );
    connector.addHttpService( httpServiceReference );

    verify( jerseyContext ).addResource( resource );
  }

  @Test
  public void testUpdatePath() {
    Configuration configuration = createConfiguration();
    mockHttpService();
    Object resource = new Object();
    when( bundleContext.getService( resourceServiceReference ) ).thenReturn( resource );

    connector.addResource( resourceServiceReference );
    connector.addHttpService( httpServiceReference );
    connector.updateConfiguration( configuration );

    final JerseyContextConfiguration jerseyContextConfiguration = new JerseyContextConfiguration()
            .withPublishDelay(configuration.getPublishDelay())
            .withRootPath(configuration.getDefaultRootPath());

    verify( jerseyContext ).updateConfiguration( jerseyContextConfiguration );
  }

  @Test
  public void testRemoveHttpService() {
    HttpService httpService = mockHttpService();
    connector.addHttpService( httpServiceReference );
    Object resource = new Object();
    when( bundleContext.getService( resourceServiceReference ) ).thenReturn( resource );
    connector.addResource( resourceServiceReference );

    connector.removeHttpService( httpService );

    verify( jerseyContext ).eliminate();
  }

  @Test
  public void testRemoveResource() {
    mockHttpService();
    connector.addHttpService( httpServiceReference );
    Object resource = new Object();
    when( bundleContext.getService( resourceServiceReference ) ).thenReturn( resource );
    connector.addResource( resourceServiceReference );

    connector.removeResource( resource );

    verify( jerseyContext ).removeResource( resource );
  }

  @Test
  public void testRegisterResourceWithoutHttpService() {
    Object resource = new Object();
    when( bundleContext.getService( resourceServiceReference ) ).thenReturn( resource );

    connector.addResource( resourceServiceReference );

    verify( jerseyContext, never() ).addResource( resource );
  }

  @Test
  public void testRegisterOnlyWithRightPort() {
    mockHttpService();
    Object resource = new Object();
    when( bundleContext.getService( resourceServiceReference ) ).thenReturn( resource );
    when( resourceServiceReference.getProperty( anyString() ) ).thenReturn( "9090" );

    connector.addHttpService( httpServiceReference );
    connector.addResource( resourceServiceReference );

    verify( jerseyContext, never() ).addResource( resource );
  }

  @Test
  @Ignore("Currently disabled, as updating servlet configuration is not properly implemented at this point." +
          "In order to implement it properly the servlet configuration must be configured for a service and a path.")
  public void testAddServletConfigurationUpdatesJerseyContext() {
    connector.addHttpService( httpServiceReference );

    ServletConfiguration servletConfiguration = mock( ServletConfiguration.class );
    ServiceReference<Object> servletConfigurationReference = mock( ServiceReference.class );
    when( bundleContext.getService( servletConfigurationReference ) ).thenReturn( servletConfiguration );

    connector.setServletConfiguration( servletConfigurationReference );

    verify( jerseyContext).updateServletConfiguration( servletConfiguration );
  }

  @Test
  @Ignore("Currently disabled, as updating servlet configuration is not properly implemented at this point." +
          "In order to implement it properly the servlet configuration must be configured for a service and a path.")
  public void testServletConfigurationUpdatesJerseyContext() {
    // Mock http service
    mockHttpService();
    connector.addHttpService( httpServiceReference );

    // Mock servlet configuration
    ServletConfiguration servletConfiguration = mock( ServletConfiguration.class );
    ServiceReference<Object> servletConfigurationReference = mock( ServiceReference.class );
    when( bundleContext.getService( servletConfigurationReference ) ).thenReturn( servletConfiguration );

    // Verify that it is not invoked, as long as there is no resource
    connector.setServletConfiguration( servletConfigurationReference );
    verify( jerseyContext, times(0)).updateServletConfiguration( servletConfiguration );

    // Add resource and verify again
    Object resource = new Object();
    when (bundleContext.getService( resourceServiceReference) ).thenReturn( resource );
    verify( jerseyContext, times(1)).updateServletConfiguration( servletConfiguration );

    // Unset servlet configuration and verify
    connector.unsetServletConfiguration( servletConfigurationReference, servletConfiguration );
    verify( jerseyContext).updateServletConfiguration( null );
  }

  @Test
  public void testServletConfigurationWithoutHttpServices() {
    ServletConfiguration servletConfigurationService = mock( ServletConfiguration.class );
    ServiceReference<Object> servletConfigurationServiceReference = mock( ServiceReference.class );

    when( bundleContext.getService( servletConfigurationServiceReference ) ).thenReturn( servletConfigurationService );

    connector.setServletConfiguration( servletConfigurationServiceReference );

    verify( connector, never() ).doRemoveHttpService( any( HttpService.class ) );
    verify( connector, never() ).doAddHttpService( any( ServiceReference.class ) );

    connector.unsetServletConfiguration( servletConfigurationServiceReference, servletConfigurationService );

    verify( connector, never() ).doRemoveHttpService( any( HttpService.class ) );
    verify( connector, never() ).doAddHttpService( any( ServiceReference.class ) );
  }

  @Test
  public void testUpdateWhenAddingApplicationConfiguration() {
    mockHttpService();

    Object resource = new Object();
    when( bundleContext.getService( resourceServiceReference ) ).thenReturn( resource );

    connector.addHttpService( httpServiceReference );
    connector.addResource( resourceServiceReference );

    ApplicationConfiguration appConfiguration = mock( ApplicationConfiguration.class );
    ServiceReference<Object> applicationConfigurationReference = mock( ServiceReference.class );
    when( bundleContext.getService( applicationConfigurationReference ) ).thenReturn( appConfiguration );

    connector.addApplicationConfiguration( applicationConfigurationReference );

    verify( jerseyContext ).updateAppConfiguration( any(ServiceContainer.class) );

  }

  @Test
  public void testUpdateWhenRemovingApplicationConfiguration() {
    mockHttpService();

    Object resource = new Object();
    when( bundleContext.getService( resourceServiceReference ) ).thenReturn( resource );

    connector.addHttpService( httpServiceReference );
    connector.addResource( resourceServiceReference );

    ApplicationConfiguration appConfiguration = mock( ApplicationConfiguration.class );
    ServiceReference<Object> applicationConfigurationReference = mock( ServiceReference.class );
    when( bundleContext.getService( applicationConfigurationReference ) ).thenReturn( appConfiguration );
    connector.addApplicationConfiguration( applicationConfigurationReference );

    connector.removeApplicationConfiguration( applicationConfigurationReference, appConfiguration );

    verify( jerseyContext, times(2) ).updateAppConfiguration( any(ServiceContainer.class) );
  }

  private HttpService mockHttpService() {
    HttpService httpService = mock( HttpService.class );
    when( bundleContext.getService( httpServiceReference ) ).thenReturn( httpService );
    when( httpServiceReference.getProperty( anyString() ) ).thenReturn( "80" );
    return httpService;
  }

  private Configuration createConfiguration() {
    Configuration configuration = mock( Configuration.class );
    when( configuration.getDefaultRootPath() ).thenReturn( "/test" );
    when( configuration.getPublishDelay() ).thenReturn( 23L );
    return configuration;
  }
}
