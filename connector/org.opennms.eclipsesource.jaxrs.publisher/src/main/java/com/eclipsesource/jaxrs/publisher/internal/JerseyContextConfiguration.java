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

package com.eclipsesource.jaxrs.publisher.internal;

import java.util.Objects;

public class JerseyContextConfiguration {

    private long publishDelay;
    private String rootPath;

    public JerseyContextConfiguration() {

    }

    public JerseyContextConfiguration withConfiguration(Configuration configuration) {
        withRootPath(configuration.getDefaultRootPath());
        withPublishDelay(configuration.getPublishDelay());
        return this;
    }

    public JerseyContextConfiguration withRootPath(String rootPath) {
        this.rootPath = rootPath;
        return this;
    }

    public JerseyContextConfiguration withPublishDelay(long delayInMs) {
        this.publishDelay = delayInMs;
        return this;
    }

    public long getPublishDelay() {
        return publishDelay;
    }

    public void setPublishDelay(long publishDelay) {
        this.publishDelay = publishDelay;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !(obj instanceof JerseyContextConfiguration)) return false;
        final JerseyContextConfiguration other = (JerseyContextConfiguration) obj;
        final boolean equals = Objects.equals(publishDelay, other.publishDelay)
                && Objects.equals(rootPath, other.rootPath);
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(publishDelay, rootPath);
    }
}
