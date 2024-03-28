/*******************************************************************************
 * Copyright (c) 2015,2024 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ivan Iliev - initial API and implementation
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

import com.eclipsesource.jaxrs.publisher.ServletConfiguration;


@RunWith( MockitoJUnitRunner.class )
public class ServletConfigurationTrackerTest {
  
  private ServletConfigurationTracker servletConfigurationTracker;
  
  @Mock
  private JAXRSConnector connector;
  @Mock
  private ServiceReference<Object> reference;

  @Before
  public void setUp() {
    BundleContext context = mock( BundleContext.class );
    servletConfigurationTracker = new ServletConfigurationTracker( context, connector );
  }
  
  @Test
  public void delegatesAddServletConfigurationService() {
    servletConfigurationTracker.addingService( reference );
    
    verify( connector ).setServletConfiguration( reference );
  }
  
  @Test
  public void delegatesRemoveServletConfigurationService() {
    ServletConfiguration service = mock( ServletConfiguration.class );
    
    servletConfigurationTracker.removedService( reference, service );
    
    verify( connector ).unsetServletConfiguration( reference, service );
  }
}
