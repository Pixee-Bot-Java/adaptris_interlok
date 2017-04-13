/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.services;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.MDC;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Add a mapped diagnostic context via {@link MDC#put(String, String)}.
 * <p>
 * If you have a large number of workflows then it may be useful to use a mapped diagnostic context to provide additional
 * information into your logfile.
 * </p>
 * 
 * @config add-logging-context-service
 * 
 */
@XStreamAlias("add-logging-context-service")
@AdapterComponent
@ComponentProfile(summary = "Add a mapped diagnostic context for logging; useful for filtering", tag = "service,logging,debug")
@DisplayOrder(order = { "key", "value"})
public class AddLoggingContext extends ServiceImp {

  @NotBlank
  private String key;
  @NotBlank
  private String value;

  public AddLoggingContext() {
    super();
  }

  public AddLoggingContext(String key, String value) {
    this();
    setKey(key);
    setValue(value);
  }

  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      MDC.put(msg.resolve(getKey()), msg.resolve(getValue()));
    }
    catch (IllegalArgumentException | IllegalStateException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  @Override
  public void prepare() throws CoreException {
  }

  public String getKey() {
    return key;
  }

  /**
   * Set the key for the mapped diagnostic context.
   * 
   * @param key the key to set
   */
  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  /**
   * Set the value for the mapped diagnostic context.
   * 
   * @param value the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

}
