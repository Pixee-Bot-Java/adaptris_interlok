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

package com.adaptris.core.common;

import java.io.IOException;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.Removal;
import com.adaptris.core.CoreException;
import com.adaptris.core.MessageDrivenDestination;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.util.URLString;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@code DataInputParameter} implementation that reads a file specified by a metadata key.
 * @config metadata-file-input-parameter
 *
 * @deprecated since 3.5.0 use {@link FileDataInputParameter#setDestination(MessageDrivenDestination)} using {@link com.adaptris.core.MetadataDestination} instead for consistency.
 *
 */
@XStreamAlias("metadata-file-input-parameter")
@DisplayOrder(order = {"metadataKey"})
@Deprecated
@Removal(version = "3.8.3", message = "use FileDataInputParameter with a MetadataDestination ")
public class MetadataFileInputParameter extends FileInputParameterImpl {
  private static transient boolean warningLogged;

  @NotBlank
  @AutoPopulated
  private String metadataKey;

  public MetadataFileInputParameter() {
    LoggingHelper.logDeprecation(warningLogged, ()-> { warningLogged=true;}, this.getClass().getSimpleName(), FileDataInputParameter.class.getName());
  }

  @Override
  public String extract(InterlokMessage message) throws CoreException {
    try {
      String fileUrl = message.getMessageHeaders().get(getMetadataKey());
      return this.load(new URLString(fileUrl), message.getContentEncoding());
    } catch (IOException ex) {
      throw new CoreException(ex);
    }
  }


  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String key) {
    this.metadataKey = Args.notNull(key, "metadata key");
  }
}
