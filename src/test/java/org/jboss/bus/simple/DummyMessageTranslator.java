/*
 * -----------------------------------------------------------------------\
 * FederatedBus
 *  
 * Copyright (C) 2015 - 2016 the original author or authors.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------------/
 */
package org.jboss.bus.simple;

import org.jboss.bus.api.CompoundContext;
import org.jboss.bus.api.FederatedBusException;
import org.jboss.bus.api.Message;
import org.jboss.bus.internal.AbstractMessageTranslator;
import org.jboss.bus.internal.MessageImpl;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class DummyMessageTranslator extends AbstractMessageTranslator {

   private Integer prop1;

   private String prop2;

   private List<Message> messageStore = new LinkedList<>();

   private static final String PROCESS_SET_FLAG = "federated.bus.processed_flag";

   @Override
   public void initialize(final CompoundContext compoundContext) {
      // nop
   }

   @Override
   public void sendMessage(Message message) throws FederatedBusException {
      message.getProperties().setProperty(PROCESS_SET_FLAG, "true");
      messageStore.add(message);
   }

   public void generateMessage(final String payload) {
      final Message message = new MessageImpl();
      message.setPayload(payload);
      federatedBus.processMessage(message);
   }

   public List<Message> getMessages() {
      return Collections.unmodifiableList(messageStore);
   }

   public void setProp2AsEmelent(final Element element) {
      this.prop2 = element.getNodeValue();
   }

   public Integer getProp1() {
      return prop1;
   }

   public void setProp1(Integer prop1) {
      this.prop1 = prop1;
   }

   public String getProp2() {
      return prop2;
   }

   public void setProp2(String prop2) {
      this.prop2 = prop2;
   }

   @Override
   public String getName() {
      return "dummy";
   }
}
