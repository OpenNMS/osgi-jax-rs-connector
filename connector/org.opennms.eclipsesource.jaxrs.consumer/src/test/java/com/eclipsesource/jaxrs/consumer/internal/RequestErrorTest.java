package com.eclipsesource.jaxrs.consumer.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Test;


public class RequestErrorTest {
  
  @Test
  public void testMessageContainsUrl() {
    RequestConfigurer configurer = mock( RequestConfigurer.class );
    when( configurer.getRequestUrl() ).thenReturn( "http://foo.bar" );
    RequestError requestError = new RequestError( configurer, Response.serverError().build(), "GET" );
    
    String message = requestError.getMessage();
    
    assertTrue( message.contains( "http://foo.bar" ) );
  }
  
  @Test
  public void testMessageContainsStatus() {
    RequestConfigurer configurer = mock( RequestConfigurer.class );
    RequestError requestError = new RequestError( configurer, Response.status(666).build(), "GET" );
    
    String message = requestError.getMessage();
    
    assertTrue( message.contains( "666" ) );
  }
  
  @Test
  public void testMessageContainsMethod() {
    RequestConfigurer configurer = mock( RequestConfigurer.class );
    RequestError requestError = new RequestError( configurer, Response.status(666).build(), "GET" );
    
    String message = requestError.getMessage();
    
    assertTrue( message.contains( "GET" ) );
  }
  
  @Test
  public void testMessageContainsBody() {
    RequestConfigurer configurer = mock( RequestConfigurer.class );
    RequestError requestError = new RequestError( configurer, Response.status(500, "failure").entity("body").build(), "GET" );
    
    String message = requestError.getMessage();
    
    assertTrue( message.contains( "body" ) );
  }

  @Test
  public void testSavesEntityInternal() {
    RequestConfigurer configurer = mock( RequestConfigurer.class );
    RequestError requestError = new RequestError( configurer, Response.ok("body").build(), "GET" );

    assertEquals( "body", requestError.getEntity() );
  }

  @Test
  public void testHasBody() {
    RequestConfigurer configurer = mock( RequestConfigurer.class );
    Response response = Response.ok("body").build();
    RequestError requestError = new RequestError( configurer, response, "GET" );

    assertTrue( requestError.hasEntity() );
    assertEquals( "body", requestError.getEntity() );
  }
  
  @Test
  public void testHasStatus() {
    RequestConfigurer configurer = mock( RequestConfigurer.class );
    Response response = Response.status(233).build();
    RequestError requestError = new RequestError( configurer, response, "GET" );
    
    int status = requestError.getStatus();
    
    assertEquals( 233, status );
  }
  
  @Test
  public void testHasMethod() {
    RequestConfigurer configurer = mock( RequestConfigurer.class );
    Response response = Response.ok().build();
    RequestError requestError = new RequestError( configurer, response, "GET" );
    
    String method = requestError.getMethod();
    
    assertEquals( "GET", method );
  }
  
  @Test
  public void testHasUrl() {
    RequestConfigurer configurer = mock( RequestConfigurer.class );
    Response response = Response.ok().header("foo", "bar").build();
    RequestError requestError = new RequestError( configurer, response, "GET" );
    
    MultivaluedMap<String, Object> actualHeaders = requestError.getHeaders();

    assertEquals( "bar", actualHeaders.getFirst("foo" ));
  }
}
