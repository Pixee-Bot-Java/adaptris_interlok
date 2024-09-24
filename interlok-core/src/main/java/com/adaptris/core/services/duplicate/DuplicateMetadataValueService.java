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

package com.adaptris.core.services.duplicate;

import io.github.pixee.security.ObjectInputFilters;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;

/**
 * <p>
 * Abstract super-class of the two <code>Service</code>s which handle duplicate message checking.
 * </p>
 */
public abstract class DuplicateMetadataValueService extends ServiceImp {

  @NotBlank
  private String metadataKey;
  @NotBlank
  private String storeFileUrl;

  // not marshalled
  protected transient List<String> previousValuesStore;
  protected transient File store;

  @Override
  protected void initService() throws CoreException {
    try {
      Args.notNull(getMetadataKey(), "metadataKey");
      Args.notNull(getStoreFileUrl(), "storeFileUrl");
      store = FsHelper.toFile(getStoreFileUrl());
      Args.notNull(store, "storeFile");
      loadPreviouslyReceivedValues();
      previousValuesStore = ObjectUtils.defaultIfNull(previousValuesStore, new ArrayList<String>());
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void closeService() {}


  protected void loadPreviouslyReceivedValues() throws Exception {
    if (store.exists()) {
      try (FileInputStream in = new FileInputStream(store); ObjectInputStream o = new ObjectInputStream(in)) {
        ObjectInputFilters.enableObjectFilterIfUnprotected(o);
        previousValuesStore = (ArrayList<String>) o.readObject();
      }
    }
  }


  int storeSize() {
    return previousValuesStore.size();
  }

  // properties...

  /**
   * <p>
   * Returns the metadata key whose value should be checked.
   * </p>
   *
   * @return metadataKey the metadata key whose value should be checked
   */
  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * <p>
   * Sets the metadata key whose value should be checked. May not be null.
   * </p>
   *
   * @param s the metadata key whose value should be checked
   */
  public void setMetadataKey(String s) {
    metadataKey = Args.notBlank(s, "metadataKey");
  }

  /**
   * <p>
   * Returns the persistent store for previously received values in the form of
   * a file URL. E.g. <code>file:////Users/adaptris/store.dat/</code>.
   * </p>
   *
   * @return the persistent store for previously received values in the form of
   *         a file URL
   */
  public String getStoreFileUrl() {
    return storeFileUrl;
  }

  /**
   * <p>
   * Sets the persistent store for previously received values in the form of a
   * file URL. E.g. <code>file:////Users/adaptris/store.dat</code>. May not
   * be null or empty.
   * </p>
   *
   * @param s the persistent store for previously received values in the form of
   *          a file URL
   */
  public void setStoreFileUrl(String s) {
    storeFileUrl = Args.notNull(s, "storeFileUrl");
  }


  @Override
  public void prepare() throws CoreException {
  }


}
