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

import org.apache.commons.lang3.StringUtils;
import org.jboss.bus.api.CompoundContext;
import org.jboss.bus.api.FederatedBus;
import org.jboss.bus.api.MessageTranslator;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Common functionality of message translators. Handles inbound and outbound endpoints and translator naming.
 *
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
abstract public class AbstractMessageTranslator implements MessageTranslator {

   /**
    * Federated bus to which message translator is registered.
    */
   protected FederatedBus federatedBus;

   /**
    * Set of input endpoints.
    */
   protected Set<String> inputEndpoints;

   /**
    * Set of output endpoints.
    */
   protected Set<String> outputEndpoints;

   /**
    * Name of this message translator.
    */
   protected String name = "abstract";

   /**
    * Tells whether the message was already processed through the federated bus so that it is not processed
    * repeatedly.
    * @param headers The message headers that are checked for a spcific bus signature.
    * @return True if and only if this message was already processed.
    */
   public static boolean isSigned(final Map<String, Object> headers) {
      return headers.containsKey(TRANSLATOR_SIGNATURE);
   }

   @Override
   public void start(FederatedBus federatedBus) {
      this.federatedBus = federatedBus;
   }

   /**
    * Gets input endpoints as a set.
    * @return The set of input endpoints.
    */
   public Set<String> getInputEndpointsAsSet() {
      return inputEndpoints;
   }

   /**
    * Sets input endpoints.
    * @param inputEndpoints Input endpoints separated by ",".
    */
   public void setInputEndpoints(final String inputEndpoints) {
      this.inputEndpoints = Arrays.asList(inputEndpoints.split(",")).stream().map(StringUtils::strip).collect(Collectors.toSet());
   }

   /**
    * Gets output endpoints as a set.
    * @return The set of output endpoints.
    */
   public Set<String> getOutputEndpointsAsSet() {
      return outputEndpoints;
   }

   /**
    * Sets output endpoints.
    * @param outputEndpoints Output endpoints separated by ",".
    */
   public void setOutputEndpoints(final String outputEndpoints) {
      this.outputEndpoints = Arrays.asList(outputEndpoints.split(",")).stream().map(StringUtils::strip).collect(Collectors.toSet());
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public void setName(String name) {
      this.name = name;
   }
}
