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

package com.eclipsesource.jaxrs.publisher.api;

import java.util.List;

public interface ApplicationRegistry {

    List<String> getEndpoints();

    List<String> getRootPaths();

    List<ApplicationDTO> getAllApplications();

    boolean isApplicationReady(String context);
}
