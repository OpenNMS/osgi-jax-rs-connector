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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;


@RunWith( MockitoJUnitRunner.class )
public class HttpServiceTrackerTest {
  
  private HttpServiceTracker httpTracker;
  @Mock
  private JAXRSConnector connector;
  @Mock
  private ServiceReference<Object> reference;

  @Before
  public void setUp() {
    BundleContext context = mock( BundleContext.class );
    httpTracker = new HttpServiceTracker( context, connector );
  }
  
  @Test
  public void delegatesAddHttpService() {
    httpTracker.addingService( reference );
    
    verify( connector ).addHttpService( reference );
  }
  
  @Test
  public void delegatesRemoveHttpService() {
    HttpService service = mock( HttpService.class );
    
    httpTracker.removedService( reference, service );
    
    verify( connector ).removeHttpService( service );
  }
}
