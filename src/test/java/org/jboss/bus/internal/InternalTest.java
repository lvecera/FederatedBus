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

import org.jboss.bus.api.Message;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class InternalTest {

   @Test
   public void testMessage() throws Exception {
      final Message message = new MessageImpl("hello");

      message.setHeader("a", "1");
      message.setProperty("b", "2");

      Assert.assertEquals(message.getHeader("a"), "1");
      Assert.assertEquals(message.getProperty("b"), "2");
      Assert.assertEquals(message.getHeader("C", "3"), "3");
      Assert.assertEquals(message.getHeader("a", "3"), "1");

      final Message message1 = new MessageImpl("hello");
      message1.setHeader("a", "1");
      message1.setProperty("b", "2");

      Assert.assertTrue(message.equals(message1));
   }

}