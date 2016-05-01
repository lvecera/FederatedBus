package org.jboss.bus.drools;

import org.apache.camel.CamelContext;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.jboss.bus.api.CompoundContext;
import org.jboss.bus.api.FederatedBus;
import org.jboss.bus.config.FederatedBusFactory;
import org.jboss.bus.config.FederatedBusFactoryTest;
import org.jboss.bus.internal.AbstractMessageTranslator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class DroolsFederatedBusTest {

   @Test
   public void testDroolsFederatedBus() throws Exception {

      final CompoundContext context = FederatedBusFactory.getDefaultCompoundContext();
      final List<FederatedBus> buses = FederatedBusFactory.loadFromXml(FederatedBusFactoryTest.class.getResource("/drools-bus.xml").getPath(), context);
      final FederatedBus federatedBus = buses.get(0);
      federatedBus.start();

      ProducerTemplate producerTemplate = context.getContext(CamelContext.class).createProducerTemplate();

      final List<Message> results = Collections.synchronizedList(new LinkedList<>());

      context.getContext(CamelContext.class).getEndpoint("direct:outTest").createConsumer(exchange -> results.add(exchange.getIn())).start();

      producerTemplate.sendBody("direct:inTest", "myTestMessage");
      Thread.sleep(100);

      Assert.assertTrue(AbstractMessageTranslator.isSigned(results.get(0).getHeaders()));
      Assert.assertEquals(results.get(0).getBody().toString(), "myTestMessage");

      Assert.assertEquals(results.get(0).getHeader(org.jboss.bus.api.Message.FROM_HEADER), "camel:direct://inTest");
      Assert.assertEquals(results.get(0).getHeader(org.jboss.bus.api.Message.SOURCE_HEADER), "camel");


      FederatedBusFactory.shutdownContext(federatedBus.getCompoundContext());
   }
}