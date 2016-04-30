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
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class CdiMessageTranslator extends AbstractMessageTranslator {

   private static final Logger log = LogManager.getLogger(CdiMessageTranslator.class);

   private WeldContainer weld;

   protected static List<CdiMessageTranslator> instances = new ArrayList<>();
   protected List<Object> processedEvents = Collections.synchronizedList(new ArrayList<>());

   @Override
   public void initialize(final CompoundContext compoundContext) {
      super.initialize(compoundContext);
      initCdi(compoundContext.getContext(WeldContainer.class));
   }

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

   public void processEvent(Object event) {
      final Serializable payload = event instanceof Serializable ? (Serializable) event : event.toString();

      if (processedEvents.contains(payload)) {
         log.info("remove **************************** " + event.toString());
         processedEvents.remove(payload);
      } else {
         final Message message = new MessageImpl(payload);;

         if (log.isDebugEnabled()) {
            log.debug("Processing message: {}", payload.toString());
         }

         message.setHeader(FROM_HEADER, "cdi:" + payload.getClass().getCanonicalName());
         message.setHeader(SOURCE_HEADER, "cdi");
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
