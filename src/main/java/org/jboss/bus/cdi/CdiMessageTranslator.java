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
package org.jboss.bus.cdi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.bus.api.CompoundContext;
import org.jboss.bus.api.FederatedBus;
import org.jboss.bus.api.FederatedBusException;
import org.jboss.bus.api.Message;
import org.jboss.bus.internal.AbstractMessageTranslator;
import org.jboss.bus.internal.MessageImpl;
import org.jboss.weld.environment.se.WeldContainer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class CdiMessageTranslator extends AbstractMessageTranslator {

   /**
    * Logger for this class.
    */
   private static final Logger log = LogManager.getLogger(CdiMessageTranslator.class);

   /**
    * New CDI container, used to fire events.
    */
   private WeldContainer weld;

   /**
    * List of CDI message translator instances. Used to store instances.
    */
   private static final List<CdiMessageTranslator> instances = new ArrayList<>();

   /**
    * List of processed events. Used to store already processed events so they are not processed again.
    */
   private List<Object> processedEvents = Collections.synchronizedList(new ArrayList<>());

   public CdiMessageTranslator() {
      name = "cdi";
   }

   @Override
   public void initialize(final CompoundContext compoundContext) {
      initCdi(compoundContext.getContext(WeldContainer.class));
   }

   /**
    * Initialization of CDI. Sets weld container.
    * @param weld This value is used for setting Weld container.
    */
   private void initCdi(final WeldContainer weld) {
      this.weld = weld;
   }

   @Override
   public void start(FederatedBus federatedBus) {
      super.start(federatedBus);
      instances.add(this);
   }

   @Override
   public void stop() {
      instances.remove(this);
      super.stop();
   }

   @Override
   public void sendMessage(final Message message) throws FederatedBusException {
      processedEvents.add(message.getPayload());
      weld.event().fire(message.getPayload());
   }

   /**
    * Processing event. If incoming event has been already processed, it is removed.
    * Otherwise it creates message with headers and forwards it to the bus.
    * @param event
    */
   private void processEvent(Object event) {
      final Serializable payload = event instanceof Serializable ? (Serializable) event : event.toString();

      if (processedEvents.contains(payload)) {
         processedEvents.remove(payload);
      } else {
         final Message message = new MessageImpl(payload);;

         if (log.isDebugEnabled()) {
            log.debug("Processing message: {}", payload.toString());
         }

         message.setHeader(Message.FROM_HEADER, getName() + ":" + payload.getClass().getCanonicalName());
         message.setHeader(Message.SOURCE_HEADER, getName());
         federatedBus.processMessage(message);
      }
   }

   @ApplicationScoped
   public static class EventProcessor {
      public void processEvent(@Observes Object event) {
         for (CdiMessageTranslator translator : instances) {
            translator.processEvent(event);
         }
      }
   }

}
