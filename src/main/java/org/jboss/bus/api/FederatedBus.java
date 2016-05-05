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
package org.jboss.bus.api;

/**
 * Contract definition of the main bus connecting all the systems together.
 *
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public interface FederatedBus {

   /**
    * Called by translators when there is a new message to process.
    *
    * @param message The message to be processed.
    */
   void processMessage(final Message message);

   /**
    * Registers a new message translator with this federated bus.
    * The translator is automatically initialized using the current compound context.
    *
    * @param messageTranslator The message translator to be registered.
    */
   void registerTranslator(final MessageTranslator messageTranslator);

   /**
    * Starts the federated bus. Also starts all the registered translators.
    */
   void start();

   /**
    * Stops the federated bus. Also starts all the registered translators.
    */
   void stop();

   /**
    * Gets the current compound context for this bus instance.
    *
    * @return The compound context.
    */
   CompoundContext getCompoundContext();

   /**
    * Sets the current compound context for this bus instance.
    *
    * @param compoundContext A new compound context to be set.
    */
   void setCompoundContext(CompoundContext compoundContext);

}
