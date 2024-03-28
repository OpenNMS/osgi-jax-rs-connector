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

import java.lang.annotation.Annotation;

public class AnnotationUtils {

    public static <T extends Annotation> T getAnnotation(Object resource, Class<T> annotationClass) {
        T annotation = resource.getClass().getAnnotation(annotationClass);
        if( annotation == null ) {
            Class<?>[] interfaces = resource.getClass().getInterfaces();
            for( Class<?> type : interfaces ) {
                annotation = type.getAnnotation(annotationClass);
                if (annotation != null) {
                    return annotation;
                }
            }
        }
        return annotation;
    }

    public static boolean hasAnnotation(Object service, Class<? extends Annotation> annotationClass) {
        boolean result = service.getClass().isAnnotationPresent(annotationClass);
        if( !result ) {
            Class<?>[] interfaces = service.getClass().getInterfaces();
            for( Class<?> type : interfaces ) {
                result = result || type.isAnnotationPresent(annotationClass);
            }
        }
        return result;
    }
}
