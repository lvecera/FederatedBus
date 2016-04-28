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
package org.jboss.bus.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.bus.api.CompoundContext;
import org.jboss.bus.api.FederatedBus;
import org.jboss.bus.api.FederatedBusException;
import org.jboss.bus.api.Message;
import org.jboss.bus.internal.AbstractMessageTranslator;
import org.jboss.bus.internal.MessageImpl;

import java.io.Serializable;
import java.util.Map;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class CamelMessageTranslator extends AbstractMessageTranslator {

   private static final Logger log = LogManager.getLogger(CamelMessageTranslator.class);

   private CamelContext camelContext;

   private ProducerTemplate producerTemplate;

   @Override
   public void initialize(CompoundContext compoundContext) {
      super.initialize(compoundContext);
      initCamel(compoundContext.getContext(CamelContext.class));
   }

   private void initCamel(CamelContext camelContext) {
      this.camelContext = camelContext;

      producerTemplate = camelContext.createProducerTemplate();
   }

   @Override
   public void start(FederatedBus federatedBus) {
      super.start(federatedBus);

      if (inputEndpoints.size() > 0) {
         inputEndpoints.forEach(endpoint -> {
            try {
               camelContext.getEndpoint(endpoint).createConsumer(new MessageConsumer()).start();
            } catch (Exception e) {
               log.error("Unable to start consumer for endpoint {}", endpoint);
            }
         });
      }
   }

   @Override
   public void stop() {
      super.stop();
   }

   @Override
   public void sendMessage(Message message) throws FederatedBusException {
      outputEndpoints.forEach(endpoint -> {
         producerTemplate.asyncSend(endpoint, new MessageProcessor(message.getPayload(), message.getHeaders()));
         log.warn("sent to {}", endpoint);
      });
   }

   // receive inbound messages
   private class MessageConsumer implements Processor {

      @Override
      public void process(final Exchange exchange) {
         if (exchange != null && exchange.getIn().getHeader(TRANSLATOR_SIGNATURE) == null) {
            Message message;
            Object messageBody = exchange.getIn().getBody();
            if (log.isDebugEnabled()) {
               log.debug("Processing message: {}", messageBody);
            }
            if (messageBody == null) {
               message = new MessageImpl(null);
            } else if (messageBody instanceof Serializable) {
               message = new MessageImpl((Serializable) messageBody);
            } else {
               message = new MessageImpl(messageBody.toString());
            }

            message.setHeaders(exchange.getIn().getHeaders());
            message.setHeader(FROM_HEADER, "camel:" + exchange.getFromEndpoint().getEndpointUri());
            federatedBus.processMessage(message);
         }
      }
   }

   // send outbound message with headers
   private class MessageProcessor implements Processor {

      private final Object body;
      private final Map<String, Object> headers;


      public MessageProcessor(final Object body, final Map<String, Object> headers) {
         this.body = body;
         this.headers = headers;
      }

      @Override
      public void process(Exchange exchange) throws Exception {
         exchange.getIn().setBody(body);
         exchange.getIn().setHeaders(headers);
         exchange.getIn().setHeader(TRANSLATOR_SIGNATURE, true);
      }
   }
}
