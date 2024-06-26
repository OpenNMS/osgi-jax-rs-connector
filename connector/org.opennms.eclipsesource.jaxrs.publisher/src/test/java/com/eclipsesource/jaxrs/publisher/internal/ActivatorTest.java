/*******************************************************************************
 * Copyright (c) 2012,2024 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Holger Staudacher - initial API and implementation
 *    Benjamin Reed - test updates to newer Mockito, generics cleanup
 ******************************************************************************/

package com.eclipsesource.jaxrs.publisher.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Dictionary;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;

import com.eclipsesource.jaxrs.publisher.ResourceFilter;


@RunWith( MockitoJUnitRunner.class )
public class ActivatorTest {
  
  private Activator activator;
  @Mock
  private BundleContext context;
  @Mock
  private ServiceRegistration<Object> connectorRegistration;
  @Mock
  private ServiceRegistration<Object> configRegistration;
  @Mock
  private Bundle jerseyServer;

  @Before
  public void setUp() throws InvalidSyntaxException {
    Activator original = new Activator();
    activator = spy( original );
    doReturn( jerseyServer ).when( activator ).getJerseyAPIBundle();
    Filter filter = mock( Filter.class );
    when( context.createFilter( anyString() ) ).thenReturn( filter );
    when( context.registerService( eq( JAXRSConnector.class.getName() ), 
                                   any(), 
                                   nullable( Dictionary.class ) ) ).thenReturn( connectorRegistration );
    when( context.registerService( eq( ManagedService.class.getName() ), 
                                   any(), 
                                   nullable( Dictionary.class ) ) ).thenReturn( configRegistration );
  }
  
  @Test
  public void testRegisterService() throws Exception {
    activator.start( context );
    
    verify( context ).registerService( eq( JAXRSConnector.class.getName() ), 
                                       any( JAXRSConnector.class ), 
                                       nullable( Dictionary.class ) );
    verify( context ).registerService( eq( ManagedService.class.getName() ), 
                                       any( Configuration.class ), 
                                       nullable( Dictionary.class ) );
  }
  
  @Test
  public void testLooksUpResourceFilter() throws Exception {
    activator.start( context );
    
    verify( context ).getServiceReference( ResourceFilter.class.getName() );
  }
  
  @Test
  public void testDeregisterService() throws Exception {
    activator.start( context );
    
    activator.stop( context );
    
    verify( connectorRegistration ).unregister();
    verify( configRegistration ).unregister();
  }
  
  @Test
  public void testStartsJerseyServer() throws Exception {
    when( jerseyServer.getState() ).thenReturn( Bundle.INSTALLED );
    activator.start( context );
    
    verify( jerseyServer ).start();
  }
  
  @Test
  public void testStartsNotJerseyServer() throws Exception {
    when( jerseyServer.getState() ).thenReturn( Bundle.ACTIVE );
    activator.start( context );
    
    verify( jerseyServer, never() ).start();
  }
}
