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

package com.eclipsesource.jaxrs.consumer.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Dictionary;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.eclipsesource.jaxrs.consumer.ConsumerPublisher;


public class ActivatorTest {
  
  @Path( "/test")
  public static interface Resource1 {
    @GET
    void get();
  }
  
  @Test
  public void testRegistersPublisher() throws Exception {
    Activator activator = new Activator();
    BundleContext context = mock( BundleContext.class );
    
    activator.start( context );
    
    verify( context ).registerService( eq( ConsumerPublisher.class.getName() ), 
                                       any( ConsumerPublisherImpl.class ), 
                                       nullable( Dictionary.class ) );
  }
  
  @Test
  public void testUnregistersPublisher() throws Exception {
    Activator activator = new Activator();
    BundleContext context = mock( BundleContext.class );
    ServiceRegistration<Object> registration = mock( ServiceRegistration.class );
    when( context.registerService( anyString(), any(), nullable( Dictionary.class ) ) ).thenReturn( registration );
    activator.start( context );

    activator.stop( context );
    
    verify( registration ).unregister();
  }
  
  @Test
  public void testUnregistersPublishedServices() throws Exception {
    Activator activator = new Activator();
    BundleContext context = mock( BundleContext.class );
    ServiceRegistration<Object> registration = mock( ServiceRegistration.class );
    ArgumentCaptor<ConsumerPublisher> captor = ArgumentCaptor.forClass( ConsumerPublisher.class );
    when( context.registerService( anyString(), captor.capture(), nullable( Dictionary.class ) ) ).thenReturn( registration );
    activator.start( context );
    captor.getValue().publishConsumers( "http://localhost", new Class<?>[] { Resource1.class }, null );
    
    activator.stop( context );
    
    verify( registration, times( 2 ) ).unregister();
  }
}
