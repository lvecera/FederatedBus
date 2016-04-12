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
import org.jboss.bus.internal.AbstractFederatedBus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class SimpleFederatedBus extends AbstractFederatedBus {

   private static final Logger log = LogManager.getLogger(SimpleFederatedBus.class);

   private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

   public void processMessage(final Message message) {
      messageTranslators.forEach(messageTranslator ->
            CompletableFuture.runAsync(() -> {
               try {
                  messageTranslator.sendMessage(message);
               } catch (FederatedBusException ex) {
                  log.error("Unable to send message: ", ex);
               }

            }, executor)
      );
   }

   @Override
   public void start() {
      super.start();
      log.info("Simple federated bus started!");
   }

   @Override
   public void stop() {
      try {
         executor.awaitTermination(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
         log.warn("Terminated while there were some deliveries in progress: ", e);
      }
   }

}
