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
public class DroolsAdvancedFederatedBusTest {

   @Test
   public void testDroolsAdvancedFederatedBus() throws Exception {

      final CompoundContext context = FederatedBusFactory.getDefaultCompoundContext();
      final List<FederatedBus> buses = FederatedBusFactory.loadFromXml(FederatedBusFactoryTest.class.getResource("/drools-advanced-bus.xml").getPath(), context);
      final FederatedBus federatedBus = buses.get(0);
      federatedBus.start();

      ProducerTemplate producerTemplate = context.getContext(CamelContext.class).createProducerTemplate();

      final List<Message> results = Collections.synchronizedList(new LinkedList<>());
      final List<Message> results2 = Collections.synchronizedList(new LinkedList<>());

      context.getContext(CamelContext.class).getEndpoint("direct:outTest").createConsumer(exchange -> results.add(exchange.getIn())).start();
      context.getContext(CamelContext.class).getEndpoint("direct:outTest2").createConsumer(exchange -> results2.add(exchange.getIn())).start();

      producerTemplate.sendBody("direct:inTest", "potato");
      Thread.sleep(100);

      producerTemplate.sendBody("direct:inTest2", "potato2");
      Thread.sleep(100);

      Assert.assertTrue(AbstractMessageTranslator.isSigned(results.get(0).getHeaders()));
      Assert.assertEquals(results.get(0).getBody().toString(), "Hello potato2");

      Assert.assertEquals(results.get(0).getHeader(org.jboss.bus.api.Message.FROM_HEADER), "camel2:direct://inTest2");
      Assert.assertEquals(results.get(0).getHeader(org.jboss.bus.api.Message.SOURCE_HEADER), "camel2");

      Assert.assertTrue(AbstractMessageTranslator.isSigned(results2.get(0).getHeaders()));
      Assert.assertEquals(results2.get(0).getBody().toString(), "potato");

      Assert.assertEquals(results2.get(0).getHeader(org.jboss.bus.api.Message.FROM_HEADER), "camel:direct://inTest");
      Assert.assertEquals(results2.get(0).getHeader(org.jboss.bus.api.Message.SOURCE_HEADER), "camel");


      FederatedBusFactory.shutdownContext(federatedBus.getCompoundContext());
   }
}