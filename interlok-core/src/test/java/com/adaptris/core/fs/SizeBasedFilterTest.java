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

package com.adaptris.core.fs;

import java.io.File;

import com.adaptris.core.BaseCase;

public class SizeBasedFilterTest extends BaseCase {

  private static final int DIFF = 2;
  private File file;

  public SizeBasedFilterTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
    file = new File(PROPERTIES.getProperty("fs.SizeBasedFilter"));
  }

  public void testSizeLessThanFileIsSmaller() throws Exception {
    SizeBasedFileFilter filter = new SizeLessThan(String.valueOf(file.length() + DIFF));
    assertTrue("filter.accept() should be true", filter.accept(file));
    filter = new SizeLessThan(file.length() + DIFF);
    assertTrue("filter.accept() should be true", filter.accept(file));
  }

  public void testSizeLessThanFileIsLarger() throws Exception {
    SizeBasedFileFilter filter = new SizeLessThan(String.valueOf(file.length() - DIFF));
    assertTrue("filter.accept() should be false", !filter.accept(file));
    filter = new SizeLessThan(file.length() - DIFF);
    assertTrue("filter.accept() should be false", !filter.accept(file));
  }

  public void testSizeLessThanFileIsExact() throws Exception {
    SizeBasedFileFilter filter = new SizeLessThan(String.valueOf(file.length()));
    assertTrue("filter.accept() should be false", !filter.accept(file));
    filter = new SizeLessThan(file.length());
    assertTrue("filter.accept() should be false", !filter.accept(file));
  }

  public void testSizeLessThanOrEqualFileIsSmaller() throws Exception {
    SizeBasedFileFilter filter = new SizeLessThanOrEqual(String.valueOf(file.length() + DIFF));
    assertTrue("filter.accept() should be true", filter.accept(file));
    filter = new SizeLessThanOrEqual(file.length() + DIFF);
    assertTrue("filter.accept() should be true", filter.accept(file));
  }

  public void testSizeLessThanOrEqualFileIsLarger() throws Exception {
    SizeBasedFileFilter filter = new SizeLessThanOrEqual(String.valueOf(file.length() - DIFF));
    assertTrue("filter.accept() should be false", !filter.accept(file));
    filter = new SizeLessThanOrEqual(file.length() - DIFF);
    assertTrue("filter.accept() should be false", !filter.accept(file));
  }

  public void testSizeLessThanOrEqualFileIsExact() throws Exception {
    SizeBasedFileFilter filter = new SizeLessThanOrEqual(String.valueOf(file.length()));
    assertTrue("filter.accept() should be true", filter.accept(file));
    filter = new SizeLessThanOrEqual(file.length());
    assertTrue("filter.accept() should be true", filter.accept(file));
  }

  public void testSizeGreaterThanFileIsSmaller() throws Exception {
    SizeBasedFileFilter filter = new SizeGreaterThan(String.valueOf(file.length() + DIFF));
    assertTrue("filter.accept() should be false", !filter.accept(file));
    filter = new SizeGreaterThan(file.length() + DIFF);
    assertTrue("filter.accept() should be false", !filter.accept(file));
  }

  public void testSizeGreaterThanFileIsLarger() throws Exception {
    SizeBasedFileFilter filter = new SizeGreaterThan(String.valueOf(file.length() - DIFF));
    assertTrue("filter.accept() should be true", filter.accept(file));
    filter = new SizeGreaterThan(file.length() - DIFF);
    assertTrue("filter.accept() should be true", filter.accept(file));
  }

  public void testSizeGreaterThanFileIsExact() throws Exception {
    SizeBasedFileFilter filter = new SizeGreaterThan(String.valueOf(file.length()));
    assertTrue("filter.accept() should be false", !filter.accept(file));
    filter = new SizeGreaterThan(file.length());
    assertTrue("filter.accept() should be false", !filter.accept(file));
  }

  public void testSizeGreaterThanOrEqualFileIsSmaller() throws Exception {
    SizeBasedFileFilter filter = new SizeGreaterThanOrEqual(String.valueOf(file.length() + DIFF));
    assertTrue("filter.accept() should be false", !filter.accept(file));
    filter = new SizeGreaterThanOrEqual(file.length() + DIFF);
    assertTrue("filter.accept() should be false", !filter.accept(file));
  }

  public void testSizeGreaterThanOrEqualFileIsLarger() throws Exception {
    SizeBasedFileFilter filter = new SizeGreaterThanOrEqual(String.valueOf(file.length() - DIFF));
    assertTrue("filter.accept() should be true", filter.accept(file));
    filter = new SizeGreaterThanOrEqual(file.length() - DIFF);
    assertTrue("filter.accept() should be true", filter.accept(file));
  }

  public void testSizeGreaterThanOrEqualFileIsExact() throws Exception {
    SizeBasedFileFilter filter = new SizeGreaterThanOrEqual(String.valueOf(file.length()));
    assertTrue("filter.accept() should be true", filter.accept(file));
    filter = new SizeGreaterThanOrEqual(file.length());
    assertTrue("filter.accept() should be true", filter.accept(file));
  }
}