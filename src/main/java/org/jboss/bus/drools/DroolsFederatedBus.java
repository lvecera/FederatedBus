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
package org.jboss.bus.drools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.bus.api.FederatedBusException;
import org.jboss.bus.api.Message;
import org.jboss.bus.api.MessageTranslator;
import org.jboss.bus.internal.AbstractFederatedBus;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.io.KieResources;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class DroolsFederatedBus extends AbstractFederatedBus {

   /**
    * Logger for this class.
    */
   private static final Logger log = LogManager.getLogger(DroolsFederatedBus.class);

   /**
    * Name of rules file.
    */
   private String rules;

   /**
    * Allows to establish an iterative conversation with the engine.
    */
   private KieSession kieSession;

   /**
    * Abstract channel through which data are inserted into the engine.
    */
   private EntryPoint inbound;

   /**
    * Allows to execute submitted task using several pooled threads.
    */
   private ThreadPoolExecutor executor;

   /**
    * Initial value of thread pool size set to 10.
    */
   private int threadPoolSize = 10;

   @Override
   public void start() {
      executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize);
      try {
         final KieServices kieServices = KieServices.Factory.get();

         final KieRepository krp = kieServices.getRepository();
         final KieFileSystem kfs = kieServices.newKieFileSystem();
         final KieResources krs = kieServices.getResources();

         final Path rulesPath = getRulesPath();
         if (rulesPath == null) {
            throw new IOException("Unable to locate rules resource: " + rules);
         }

         final String fileContent = new String(Files.readAllBytes(rulesPath), Charset.defaultCharset());
         final Resource rulesResource = krs.newReaderResource(new StringReader(fileContent), Charset.defaultCharset().name());

         kfs.write("src/main/resources/org/jboss/bus/rules.drl", rulesResource);
         final KieBuilder kb = kieServices.newKieBuilder(kfs);
         kb.buildAll();

         // Check the builder for errors
         if (kb.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            if (log.isErrorEnabled()) {
               log.error(kb.getResults().getMessages().toString());
            }
            throw new IOException("Error compiling rules.");
         }

         KieContainer kieContainer = kieServices.newKieContainer(krp.getDefaultReleaseId());
         kieSession = kieContainer.newKieSession();
         inbound = kieSession.getEntryPoint("inbound");

         kieSession.registerChannel("outbound", message -> sendOutbound((Message) message));

         messageTranslators.forEach(translator -> kieSession.registerChannel(translator.getName(), message -> sendOutbound(translator, (Message) message)));

         super.start();
         log.info("Drools federated bus started!");
      } catch (IOException e) {
         log.error("Drools federated bus could not be started: ", e);
      }
   }

   @Override
   public void stop() {
      super.stop();

      kieSession.unregisterChannel("outbound");
      kieSession.dispose();
   }

   /**
    * Gets the name of the rules file.
    * @return String with rules file name.
    */
   public String getRules() {
      return rules;
   }

   /**
    * Sets the name of the rules file.
    * @param rules New name of rules file.
    */
   public void setRules(String rules) {
      this.rules = rules;
   }

   /**
    * Gets path to rules.
    * @return Path to rules.
    */
   private Path getRulesPath() {
      Path rulesPath = null;

      try {
         rulesPath = Paths.get(DroolsFederatedBus.class.getResource(rules).toURI());
      } catch (URISyntaxException use) {
         // we will try another way, just ignore
      }

      if (rulesPath == null || !rulesPath.toFile().exists()) {
         rulesPath = Paths.get(rules);
      }

      if (!rulesPath.toFile().exists()) {
         rulesPath = null;
      }

      return rulesPath;
   }

   /**
    * Calls send message on translator.
    * @param translator Message translator that is used for sending message.
    * @param message Message that will be sent.
    */
   private void sendOutbound(final MessageTranslator translator, final Message message) {
      CompletableFuture.runAsync(() -> {
         try {
            translator.sendMessage(message);
         } catch (FederatedBusException ex) {
            log.error("Unable to send message: ", ex);
         }
      }, executor);
   }

   /**
    * Calls send message on every registered message translator.
    * @param message Message that will be sent.
    */
   private void sendOutbound(final Message message) {
      messageTranslators.forEach(messageTranslator -> sendOutbound(messageTranslator, message));
   }

   @Override
   public void processMessage(Message message) {
      inbound.insert(message);
      kieSession.fireAllRules();
   }

   /**
    * Gets the size of thread pool.
    * @return Size of thread pool.
    */
   public int getThreadPoolSize() {
      return threadPoolSize;
   }

   /**
    * Sets the size of thread pool to given value.
    * @param threadPoolSize New value of thread pool size.
    */
   public void setThreadPoolSize(final int threadPoolSize) {
      this.threadPoolSize = threadPoolSize;
   }
}
