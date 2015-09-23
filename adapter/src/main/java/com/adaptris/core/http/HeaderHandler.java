package com.adaptris.core.http;

import javax.servlet.http.HttpServletRequest;

import com.adaptris.core.AdaptrisMessage;

/**
 * Interface for handling behaviour for HTTP headers.
 * 
 * 
 */
public interface HeaderHandler {
  
  /**
   * Handle the headers from the {@link HttpServletRequest}.
   * 
   * @param message the target {@link AdaptrisMessage}
   * @param request the request
   * @param itemPrefix Any prefix that needs to be applied
   * @deprecated since 3.0.6, concrete implementations of this interface can decide what to do with prefixes.
   */
  @Deprecated
  public void handleHeaders(AdaptrisMessage message, HttpServletRequest request, String itemPrefix);

  /**
   * Handle the headers from the {@link HttpServletRequest}.
   * 
   * @param message the target {@link AdaptrisMessage}
   * @param request the request
   */
  void handleHeaders(AdaptrisMessage message, HttpServletRequest request);

}
