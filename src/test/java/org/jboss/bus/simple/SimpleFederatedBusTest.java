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
package org.jboss.bus.simple;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class SimpleFederatedBusTest {

   @Test
   public void testSimpleBus() {
      DummyMessageTranslator t1 = new DummyMessageTranslator();
      DummyMessageTranslator t2 = new DummyMessageTranslator();
      DummyMessageTranslator t3 = new DummyMessageTranslator();

      SimpleFederatedBus federatedBus = new SimpleFederatedBus();

      federatedBus.registerTranslator(t1);
      federatedBus.registerTranslator(t2);
      federatedBus.registerTranslator(t3);

      federatedBus.start();

      t1.generateMessage("hello1");
      t2.generateMessage("hello2");
      t1.generateMessage("hello3");

      federatedBus.stop();

      verifyMessages(t1);
      verifyMessages(t2);
      verifyMessages(t3);
   }

   private void verifyMessages(final DummyMessageTranslator translator) {
      List<String> messages = translator.getMessages().stream().map(m -> m.getPayload().toString()).collect(Collectors.toList());
      Assert.assertEquals(messages.size(), 3);
      Assert.assertTrue(messages.contains("hello1"));
      Assert.assertTrue(messages.contains("hello2"));
      Assert.assertTrue(messages.contains("hello3"));
   }
}