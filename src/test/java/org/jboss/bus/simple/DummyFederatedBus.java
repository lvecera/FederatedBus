/*
 * -----------------------------------------------------------------------\
 * FederatedBus
 *  
 * Copyright (C) 2014 - 2016 the original author or authors.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.bus.api.FederatedBusException;
import org.jboss.bus.api.Message;
import org.jboss.bus.api.MessageTranslator;
import org.jboss.bus.internal.AbstractFederatedBus;

import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class DummyFederatedBus extends AbstractFederatedBus {

   private static final Logger log = LogManager.getLogger(SimpleFederatedBus.class);

   private boolean a;

   public void processMessage(final Message message) {
      messageTranslators.forEach(messageTranslator ->
      {
         try {
            messageTranslator.sendMessage(message);
         } catch (FederatedBusException ex) {
            log.error("Unable to send message: ", ex);
         }

      });
   }

   public Set<MessageTranslator> getTranslators() {
      return messageTranslators;
   }

   public boolean isA() {
      return a;
   }

   public void setA(boolean a) {
      this.a = a;
   }
}
