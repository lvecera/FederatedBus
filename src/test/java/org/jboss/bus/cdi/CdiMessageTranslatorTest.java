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
package org.jboss.bus.cdi;

import org.apache.camel.CamelContext;
import org.apache.camel.Message;
import org.jboss.bus.api.CompoundContext;
import org.jboss.bus.api.FederatedBus;
import org.jboss.bus.config.FederatedBusFactory;
import org.jboss.bus.config.FederatedBusFactoryTest;
import org.jboss.bus.internal.AbstractMessageTranslator;
import org.jboss.weld.environment.se.WeldContainer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class CdiMessageTranslatorTest {

   @Test
   public void testCdiMessageTranslator() throws Exception {

      final CompoundContext context = FederatedBusFactory.getDefaultCompoundContext();
      final List<FederatedBus> buses = FederatedBusFactory.loadFromXml(FederatedBusFactoryTest.class.getResource("/cdi-bus.xml").getPath(), context);
      final FederatedBus federatedBus = buses.get(0);
      federatedBus.start();

      final List<Message> results = Collections.synchronizedList(new LinkedList<>());

      context.getContext(CamelContext.class).getEndpoint("direct:testCdi").createConsumer(exchange -> results.add(exchange.getIn())).start();

      context.getContext(WeldContainer.class).event().fire("myMessageFromCdi");

      Thread.sleep(500);

      Assert.assertTrue(AbstractMessageTranslator.isSigned(results.get(0).getHeaders()));
      Assert.assertEquals(results.get(0).getBody().toString(), "myMessageFromCdi");
      Assert.assertEquals(results.get(0).getHeader(org.jboss.bus.api.Message.FROM_HEADER), "cdi:java.lang.String");
      Assert.assertEquals(results.get(0).getHeader(org.jboss.bus.api.Message.SOURCE_HEADER), "cdi");

      federatedBus.stop();
      FederatedBusFactory.shutdownContext(federatedBus.getCompoundContext());
   }

}