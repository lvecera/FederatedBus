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
package org.jboss.bus.internal;

import org.jboss.bus.api.CompoundContext;
import org.jboss.bus.api.FederatedBus;
import org.jboss.bus.api.MessageTranslator;

import java.util.HashSet;
import java.util.Set;

/**
 * Common functionality of all federated buses. Handles translator registry and compound context manipulation.
 * Simplifies further federated bus development.
 *
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
abstract public class AbstractFederatedBus implements FederatedBus {

   /**
    * Compound context for this federated bus.
    */
   CompoundContext compoundContext;

   /**
    * Set of registered message translators.
    */
   protected Set<MessageTranslator> messageTranslators = new HashSet<>();

   @Override
   public void registerTranslator(final MessageTranslator messageTranslator) {
      messageTranslators.add(messageTranslator);
      messageTranslator.initialize(compoundContext);
   }

   @Override
   public void start() {
      messageTranslators.forEach(messageTranslator -> messageTranslator.start(this));
   }

   @Override
   public void stop() {
      messageTranslators.forEach(MessageTranslator::stop);
   }

   @Override
   public CompoundContext getCompoundContext() {
      return compoundContext;
   }

   @Override
   public void setCompoundContext(CompoundContext compoundContext) {
      this.compoundContext = compoundContext;
   }

}
