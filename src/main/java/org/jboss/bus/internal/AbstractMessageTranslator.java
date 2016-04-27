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
package org.jboss.bus.internal;

import org.jboss.bus.api.CompoundContext;
import org.jboss.bus.api.FederatedBus;
import org.jboss.bus.api.MessageTranslator;

import java.util.Map;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
abstract public class AbstractMessageTranslator implements MessageTranslator {

   protected FederatedBus federatedBus;
   protected CompoundContext compoundContext;

   public static boolean isSigned(final Map<String, Object> headers) {
      return headers.containsKey(TRANSLATOR_SIGNATURE);
   }

   @Override
   public void initialize(CompoundContext compoundContext) {
      this.compoundContext = compoundContext;
   }

   @Override
   public void start(FederatedBus federatedBus) {
      this.federatedBus = federatedBus;
   }
}
