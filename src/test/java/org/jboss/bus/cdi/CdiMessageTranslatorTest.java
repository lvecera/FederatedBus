package org.jboss.bus.cdi;

import org.apache.camel.CamelContext;
import org.apache.camel.Message;
import org.jboss.bus.api.CompoundContext;
import org.jboss.bus.api.FederatedBus;
import org.jboss.bus.api.MessageTranslator;
import org.jboss.bus.camel.CamelMessageTranslator;
import org.jboss.bus.config.FederatedBusFactory;
import org.jboss.bus.config.FederatedBusFactoryTest;
import org.jboss.bus.internal.AbstractMessageTranslator;
import org.jboss.weld.environment.se.WeldContainer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.*;

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

      context.getContext(CamelContext.class).getEndpoint("direct://testCdi").createConsumer(exchange -> results.add(exchange.getIn())).start();

      context.getContext(WeldContainer.class).event().fire("myMessageFromCdi");

      Thread.sleep(500);

      Assert.assertTrue(AbstractMessageTranslator.isSigned(results.get(0).getHeaders()));
      Assert.assertEquals(results.get(0).getBody().toString(), "myMessageFromCdi");
      Assert.assertEquals(results.get(0).getHeader(MessageTranslator.FROM_HEADER), "cdi:java.lang.String");
      Assert.assertEquals(results.get(0).getHeader(MessageTranslator.SOURCE_HEADER), "cdi");

      federatedBus.stop();
      FederatedBusFactory.shutdownBus(federatedBus);
   }

}