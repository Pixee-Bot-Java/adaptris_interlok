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

package com.adaptris.core.runtime;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AllowsRetriesConnection;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.DefaultSerializableMessageTranslator;
import com.adaptris.core.Service;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.types.SerializableMessage;

/**
 * Implementation of {@link AdapterComponentCheckerMBean} for use by the GUI to check components.
 * 
 * @author lchan
 * 
 */
public class AdapterComponentChecker extends ChildRuntimeInfoComponentImpl
    implements AdapterComponentCheckerMBean {
  private transient AdapterManager parent;
  private transient DefaultSerializableMessageTranslator messageTranslator;

  private AdapterComponentChecker() {
    super();
    messageTranslator = new DefaultSerializableMessageTranslator();
  }

  public AdapterComponentChecker(AdapterManager owner) {
    this();
    parent = owner;
  }
  
  @Override
  protected String getType() {
    return COMPONENT_CHECKER_TYPE;
  }

  @Override
  protected String uniqueId() {
    return this.getClass().getSimpleName();
  }

  @Override
  public RuntimeInfoComponent getParentRuntimeInfoComponent() {
    return parent;
  }

  @Override
  public void checkInitialise(String xml) throws CoreException {
    AdaptrisMarshaller marshaller = DefaultMarshaller.getDefaultMarshaller();
    AdaptrisComponent component = (AdaptrisComponent) marshaller.unmarshal(xml);
    component.prepare();
    if (component instanceof AllowsRetriesConnection) {
      AllowsRetriesConnection retry = (AllowsRetriesConnection) component;
      if (retry.connectionAttempts() == -1) {
        retry.setConnectionAttempts(0);
      }
    }
    try {
      LifecycleHelper.init(component);
    } finally {
      LifecycleHelper.close(component);
    }
  }

  @Override
  public SerializableMessage applyService(String xml, SerializableMessage serializedMsg) throws CoreException {
    AdaptrisMarshaller marshaller = DefaultMarshaller.getDefaultMarshaller();
    Service service = (Service) marshaller.unmarshal(xml);
    service.prepare();
    AdaptrisMessage msg = messageTranslator.translate(serializedMsg);
    try {
      LifecycleHelper.init(service);
      LifecycleHelper.start(service);
      service.doService(msg);
    }
    finally {
      LifecycleHelper.stop(service);
      LifecycleHelper.close(service);
    }
    return messageTranslator.translate(msg);
  }

}
