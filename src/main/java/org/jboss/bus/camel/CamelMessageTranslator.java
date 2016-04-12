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
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.bus.api.FederatedBus;
import org.jboss.bus.api.FederatedBusException;
import org.jboss.bus.api.Message;
import org.jboss.bus.internal.AbstractMessageTranslator;
import org.jboss.bus.internal.MessageImpl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static java.lang.System.getProperty;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class CamelMessageTranslator extends AbstractMessageTranslator {

   private CamelContext camelContext;
   private Set<String> inputEndpoints;
   private Set<String> outputEndpoints;

   private ProducerTemplate producerTemplate;

   private static final String CAMEL_PROPERTIES = "camel.properties";
   private static final Logger log = LogManager.getLogger(CamelMessageTranslator.class);

   private ThreadPoolExecutor executor;


   public CamelMessageTranslator(CamelContext camelContext) {
      this.camelContext = camelContext;

      Properties props = new Properties();
      try {
         props.load(CamelMessageTranslator.class.getResourceAsStream(CAMEL_PROPERTIES));
      } catch (NullPointerException | IOException ioe) {
         log.warn("Camel message translator is inactive.");
      }

      String input = props.getProperty("input");

      inputEndpoints = Arrays.asList(input.split(",")).stream().map(StringUtils::strip).collect(Collectors.toSet());

      String output = props.getProperty("output");

      outputEndpoints = Arrays.asList(output.split(",")).stream().map(StringUtils::strip).collect(Collectors.toSet());

      if ((inputEndpoints.size() + outputEndpoints.size()) < 1) {
         log.warn("Input or output endpoint has to be defined.");
      }

      producerTemplate = camelContext.createProducerTemplate();

   }

   @Override
   public void start(FederatedBus federatedBus) {
      super.start(federatedBus);

      if (inputEndpoints.size() > 0) {
         executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(inputEndpoints.size());
         inputEndpoints.forEach(endpoint -> executor.submit(new MessageConsumer(camelContext, endpoint)));
      }
   }

   private class MessageConsumer implements Runnable {

      private String endpoint;
      private ConsumerTemplate consumerTemplate;

      public MessageConsumer(CamelContext camelContext, String endpoint) {
         this.endpoint = endpoint;
         consumerTemplate = camelContext.createConsumerTemplate();
      }

      @Override
      public void run() {
         while (!Thread.currentThread().isInterrupted()) {
            Exchange exchange = consumerTemplate.receive(endpoint);

            if (exchange != null) {
               Message message;
               Object messageBody = exchange.getIn().getBody();
               if (messageBody == null) {
                  message = new MessageImpl(null);
               } else if (messageBody instanceof Serializable) {
                  message = new MessageImpl((Serializable) messageBody);
               } else {
                  message = new MessageImpl(messageBody.toString());
               }

               message.setHeaders(exchange.getIn().getHeaders());
               federatedBus.processMessage(message);
            }
         }
      }
   }
}
