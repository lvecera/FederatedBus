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
package org.jboss.bus.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.bus.api.CompoundContext;
import org.jboss.bus.api.FederatedBus;
import org.jboss.bus.api.FederatedBusException;
import org.jboss.bus.api.Message;
import org.jboss.bus.internal.AbstractMessageTranslator;
import org.jboss.bus.internal.MessageImpl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class VertxMessageTranslator extends AbstractMessageTranslator {

   private static final Logger log = LogManager.getLogger(VertxMessageTranslator.class);

   private Vertx vertx;
   private EventBus eventBus;

   public VertxMessageTranslator() {
      name = "vertx";
   }

   @Override
   public void initialize(final CompoundContext compoundContext) {
      super.initialize(compoundContext);
      initVertx(compoundContext.getContext(Vertx.class));
   }

   private void initVertx(final Vertx vertx) {
      this.vertx = vertx;
   }

   @Override
   public void start(FederatedBus federatedBus) {
      super.start(federatedBus);

      eventBus = vertx.eventBus();

      if (inputEndpoints.size() > 0) {
         inputEndpoints.forEach(endpoint -> {
            eventBus.consumer(endpoint, vertxMessage -> {
               final Message message = new MessageImpl();
               final Map<String, Object> headers = new HashMap<>();

               for (final String key : vertxMessage.headers().names()) {
                  final List<String> list = vertxMessage.headers().getAll(key);
                  if (list.size() == 1) {
                     headers.put(key, vertxMessage.headers().get(key));
                  } else {
                     headers.put(key, list);
                  }
               }
               if (!headers.containsKey(TRANSLATOR_SIGNATURE)) {
                  message.setHeaders(headers);

                  final Object body = vertxMessage.body();

                  if (body instanceof Serializable) {
                     message.setPayload((Serializable) body);
                  } else {
                     message.setPayload(body.toString());
                  }

                  message.setHeader(Message.FROM_HEADER, getName() + ":" + endpoint);
                  message.setHeader(Message.SOURCE_HEADER, getName());
                  federatedBus.processMessage(message);
               }
            });
         });
      }
   }

   @Override
   public void sendMessage(final Message message) throws FederatedBusException {
      final DeliveryOptions options = new DeliveryOptions();

      for (final String key : message.getHeaders().keySet()) {
         options.addHeader(key, message.getHeader(key).toString());
      }

      options.addHeader(TRANSLATOR_SIGNATURE, "true");

      for (String endpoint : outputEndpoints) {
         eventBus.send(endpoint, message.getPayload(), options);
      }
   }
}
