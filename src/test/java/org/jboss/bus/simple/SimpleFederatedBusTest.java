package org.jboss.bus.simple;

import org.jboss.bus.api.Message;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

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