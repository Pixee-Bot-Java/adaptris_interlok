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

package com.adaptris.fs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FsCase {

  protected static Properties PROPERTIES;
  private static final String PROPERTIES_RESOURCE = "unit-tests.properties";

  static {
    PROPERTIES = new Properties();
    InputStream in = FsCase.class.getClassLoader().getResourceAsStream(PROPERTIES_RESOURCE);
    try {
      if (in == null) {
        throw new IOException("cannot locate resource [" + PROPERTIES_RESOURCE + "] on classpath");
      }
      PROPERTIES.load(in);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

}
