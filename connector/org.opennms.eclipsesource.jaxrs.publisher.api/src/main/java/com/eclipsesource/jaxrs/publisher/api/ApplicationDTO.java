/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package com.eclipsesource.jaxrs.publisher.api;

import static com.eclipsesource.jaxrs.publisher.api.AnnotationUtils.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Feature;
import javax.ws.rs.ext.Provider;

public class ApplicationDTO {

    private final String context;
    private Set<Object> resources;
    private Map<String, Object> properties;

    public ApplicationDTO(String context) {
        this.context = context;
    }

    public void setResources(Set<Object> resources) {
        this.resources = resources;
    }

    public Set<Object> getResources() {
        return resources;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Set<Object> getProviders() {
        return resources.stream().filter(r -> hasAnnotation(r, Provider.class)).collect(Collectors.toSet());
    }

    public Set<Object> getFeatures() {
        return resources.stream().filter(r -> r instanceof Feature).collect(Collectors.toSet());
    }

    public String getContext() {
        return context;
    }

}
