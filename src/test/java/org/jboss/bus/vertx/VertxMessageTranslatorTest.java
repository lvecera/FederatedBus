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
      Assert.assertEquals(message1.headers().get(MessageTranslator.FROM_HEADER), "vertx:inEnd");
   }

}