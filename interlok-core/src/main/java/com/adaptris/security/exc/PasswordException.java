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

package com.adaptris.security.exc;

/**
 * Wraps any exceptions encountered during Password operations.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public class PasswordException extends AdaptrisSecurityException {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2010020801L;

  /** @see Exception#Exception()
   *
   *
   */
  public PasswordException() {
    super();
  }

  /** @see Exception#Exception(String)
   *
   *
   */
  public PasswordException(String s) {
    super(s);
  }

  /** @see Exception#Exception(String, Throwable)
   *
   *
   */
  public PasswordException(String s, Throwable t) {
    super(s, t);
  }

  /** @see Exception#Exception(Throwable)
   *
   *
   */
  public PasswordException(Throwable t) {
    super(t);
  }

}
