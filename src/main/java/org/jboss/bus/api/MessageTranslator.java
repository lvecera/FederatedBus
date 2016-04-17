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
package org.jboss.bus.api;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public interface MessageTranslator {

   /**
    * Initialize message translator with given compound context
    * @param compoundContext
    */
   void initialize(final CompoundContext compoundContext);

   /**
    * Starts message translator.
    *
    * @param federatedBus that starting the message translator
    */
   void start(final FederatedBus federatedBus);

   /**
    * Stops message translator.
    */
   default void stop() {
   }

   /**
    * Used by federated bus to publish message to channel used by this message translator.
    *
    * @param message
    * @throws FederatedBusException
    */
   void sendMessage(final Message message) throws FederatedBusException;
}
