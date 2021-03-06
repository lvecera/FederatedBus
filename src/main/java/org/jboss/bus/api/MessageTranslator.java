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
 * An interface between an actual event driven system and the federated bus.
 *
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public interface MessageTranslator {

   String TRANSLATOR_SIGNATURE = "federated.bus.processed";

   /**
    * Initializes the message translator with the given compound context.
    *
    * @param compoundContext The compound context.
    */
   void initialize(final CompoundContext compoundContext);

   /**
    * Starts the message translator.
    *
    * @param federatedBus Federated bus starting the message translator
    */
   void start(final FederatedBus federatedBus);

   /**
    * Stops the message translator.
    */
   default void stop() {
   }

   /**
    * Used by the federated bus to publish message to a channel served by this message translator.
    *
    * @param message The message to be distributed.
    * @throws FederatedBusException When it was not possible to deliver the message.
    */
   void sendMessage(final Message message) throws FederatedBusException;

   /**
    * Gets the name of this translator which is later used in message headers to identify the source of the message.
    *
    * @return The translator's name.
    */
   String getName();

   /**
    * Sets the name of this translator which is later used in message headers to identify the source of the message.
    *
    * @param name The translator's name.
    */
   void setName(final String name);
}
