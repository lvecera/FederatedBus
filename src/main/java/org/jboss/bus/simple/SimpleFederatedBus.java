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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.bus.api.FederatedBusException;
import org.jboss.bus.api.Message;
import org.jboss.bus.internal.AbstractFederatedBus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Simplest federated bus that just forwards all inbound messages to all outbound translators.
 *
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class SimpleFederatedBus extends AbstractFederatedBus {

   /**
    * Logger for this class.
    */
   private static final Logger log = LogManager.getLogger(SimpleFederatedBus.class);

   /**
    * Allows to execute submitted tasks using a thread pool.
    */
   private ThreadPoolExecutor executor;

   /**
    * Size of the thread pool.
    */
   private int threadPoolSize = 10;

   @Override
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
      executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize);
      super.start();

      log.info("Simple federated bus started!");
   }

   @Override
   public void stop() {
      super.stop();
      executor.shutdown();
   }

   /**
    * Gets the size of the thread pool.
    *
    * @return Size of the thread pool.
    */
   public int getThreadPoolSize() {
      return threadPoolSize;
   }

   /**
    * Sets the size of the thread pool to given value.
    *
    * @param threadPoolSize The new size of the thread pool.
    */
   public void setThreadPoolSize(final int threadPoolSize) {
      this.threadPoolSize = threadPoolSize;
   }
}
