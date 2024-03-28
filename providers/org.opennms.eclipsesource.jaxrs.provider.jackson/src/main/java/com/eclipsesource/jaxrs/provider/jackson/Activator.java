/*******************************************************************************
 * Copyright (c) 2012,2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Holger Staudacher - initial API and implementation
 *    ProSyst Software GmbH. - compatibility with OSGi specification 4.2 APIs
 *    Ivan Iliev - added ServletConfigurationTracker
 *    Markus von Rüden - various changes and enhancements
 ******************************************************************************/
package com.eclipsesource.jaxrs.provider.jackson;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

public class Activator implements BundleActivator {

  private ServiceRegistration registration;

  @Override
  public void start( BundleContext context ) throws Exception {
    JacksonJaxbJsonProvider service = new JacksonJaxbJsonProvider();
    registration = context.registerService( JacksonJaxbJsonProvider.class.getName(), service, null );
  }

  @Override
  public void stop( BundleContext context ) throws Exception {
    if( registration != null ) {
      registration.unregister();
    }
  }
}
