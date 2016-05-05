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
package org.jboss.bus.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.impl.MessageImpl;
import org.jboss.bus.api.CompoundContext;
import org.jboss.bus.api.FederatedBus;
import org.jboss.bus.api.MessageTranslator;
import org.jboss.bus.config.FederatedBusFactory;
import org.jboss.bus.config.FederatedBusFactoryTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class VertxMessageTranslatorTest {

   @Test
   public void testVertxMessageTranslator() throws Exception {
      final CompoundContext context = FederatedBusFactory.getDefaultCompoundContext();
      final List<FederatedBus> buses = FederatedBusFactory.loadFromXml(FederatedBusFactoryTest.class.getResource("/vertx-bus.xml").getPath(), context);
      final FederatedBus federatedBus = buses.get(0);
      federatedBus.start();

      List<Message> messages = new ArrayList<>();

      context.getContext(Vertx.class).eventBus().consumer("outEnd", vertexMessage -> {
         messages.add(vertexMessage);
      });

      Message message = new MessageImpl<>();
      DeliveryOptions options = new DeliveryOptions();
      options.addHeader("myHeader", "myValue");


      context.getContext(Vertx.class).eventBus().send("inEnd", "myMessage", options);

      Thread.sleep(1000);

      Assert.assertEquals(messages.size(), 1);
      Message message1 = messages.iterator().next();
      Assert.assertEquals(message1.body(), "myMessage");
      Assert.assertEquals(message1.headers().get("myHeader"), "myValue");
      Assert.assertEquals(message1.headers().get(MessageTranslator.TRANSLATOR_SIGNATURE), "true");
      Assert.assertEquals(message1.headers().get(org.jboss.bus.api.Message.FROM_HEADER), "vertx:inEnd");

      federatedBus.stop();
      FederatedBusFactory.shutdownContext(federatedBus.getCompoundContext());
   }

}