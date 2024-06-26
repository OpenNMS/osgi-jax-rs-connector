/* ProSyst Software GmbH. - compatibility with OSGi specification 4.2 APIs */
package com.eclipsesource.jaxrs.provider.gson;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Dictionary;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.eclipsesource.jaxrs.provider.gson.internal.Activator;


public class ActivatorTest {
  
  @Test
  public void testStartRegistersProvider() throws Exception {
    Activator activator = new Activator();
    BundleContext context = mock( BundleContext.class );
    
    activator.start( context );
    
    verify( context ).registerService(
            eq( GsonProvider.class.getName() ),
            any( GsonProvider.class ),
            nullable( Dictionary.class )
    );
  }
  
  @Test
  public void testStopUnregistersProvider() throws Exception {
    Activator activator = new Activator();
    BundleContext context = mock( BundleContext.class );
    ServiceRegistration<Object> registration = mock( ServiceRegistration.class );
    when( context.registerService( any( String.class ), any(), nullable( Dictionary.class ) ) )
      .thenReturn( registration );
    
    activator.start( context );
    activator.stop( context );
    
    verify( registration ).unregister();
  }
}
