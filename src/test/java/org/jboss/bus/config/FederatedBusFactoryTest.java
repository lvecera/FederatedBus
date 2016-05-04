package org.jboss.bus.config;/*
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

import org.jboss.bus.api.FederatedBus;
import org.jboss.bus.api.FederatedBusException;
import org.jboss.bus.simple.DummyFederatedBus;
import org.jboss.bus.simple.DummyMessageTranslator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class FederatedBusFactoryTest {

   @Test
   public void testFederatedBusFactory() throws FederatedBusException {

      FederatedBusFactory factory = new FederatedBusFactory();
      List<FederatedBus> buses = factory.loadFromXml(FederatedBusFactoryTest.class.getResource("/dummy-bus.xml").getPath());

      Assert.assertEquals(buses.size(), 1);
      Assert.assertTrue(buses.get(0) instanceof DummyFederatedBus);
      DummyFederatedBus dummyBus = (DummyFederatedBus) buses.get(0);
      Assert.assertTrue(dummyBus.isA());
      Assert.assertEquals(dummyBus.getTranslators().size(), 1);
      Assert.assertTrue(dummyBus.getTranslators().iterator().next() instanceof DummyMessageTranslator);
      DummyMessageTranslator translator = (DummyMessageTranslator) dummyBus.getTranslators().iterator().next();
      Assert.assertEquals(translator.getProp1(), Integer.valueOf(123));
      Assert.assertEquals(translator.getProp2(), "[hello: null]");

      FederatedBusFactory.shutdownContext(dummyBus.getCompoundContext());
   }

}