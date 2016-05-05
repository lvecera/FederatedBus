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
 * Message translator which connects to CDI.
 *
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class CdiMessageTranslator extends AbstractMessageTranslator {

   /**
    * Logger for this class.
    */
   private static final Logger log = LogManager.getLogger(CdiMessageTranslator.class);

   /**
    * The CDI container used to fire events.
    */
   private WeldContainer weld;

   /**
    * Static list of all CDI message translator instances. This bridges the message translator and the CDI system.
    */
   private static final List<CdiMessageTranslator> instances = new ArrayList<>();

   /**
    * List of processed events. Used to store already processed events so they are not processed again.
    */
   private List<Object> processedEvents = Collections.synchronizedList(new ArrayList<>());

   /**
    * Sets the default name of the translator.
    */
   public CdiMessageTranslator() {
      name = "cdi";
   }

   @Override
   public void initialize(final CompoundContext compoundContext) {
      initCdi(compoundContext.getContext(WeldContainer.class));
   }

   /**
    * Initializes CDI. Sets the Weld container.
    *
    * @param weld The CDI context used for message.
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
    * Processes all CDI events. If an incoming event has been already processed, it is removed from the cache of processed messages.
    * Otherwise the event is translated into a message and forwarded to the bus.
    *
    * @param event The fired CDI event.
    */
   private void processEvent(Object event) {
      final Serializable payload = event instanceof Serializable ? (Serializable) event : event.toString();

      if (processedEvents.contains(payload)) {
         processedEvents.remove(payload);
      } else {
         final Message message = new MessageImpl(payload);
         ;

         if (log.isDebugEnabled()) {
            log.debug("Processing message: {}", payload.toString());
         }

         message.setHeader(Message.FROM_HEADER, getName() + ":" + payload.getClass().getCanonicalName());
         message.setHeader(Message.SOURCE_HEADER, getName());
         federatedBus.processMessage(message);
      }
   }

   /**
    * CDI bean catching all fired events.
    */
   @ApplicationScoped
   public static class EventProcessor {

      /**
       * Catches all CDI events and sends them to all instances of CDI translators for processing.
       *
       * @param event The fired CDI event.
       */
      public void processEvent(@Observes Object event) {
         for (CdiMessageTranslator translator : instances) {
            translator.processEvent(event);
         }
      }
   }

}
