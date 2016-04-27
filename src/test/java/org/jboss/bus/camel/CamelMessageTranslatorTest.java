package org.jboss.bus.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.jboss.bus.internal.AbstractMessageTranslator;
import org.jboss.bus.internal.CompoundContextImpl;
import org.jboss.bus.simple.SimpleFederatedBus;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class CamelMessageTranslatorTest {

   @Test
   public void testCamelMessageTranslator() throws Exception {

      CamelContext camelContext = new DefaultCamelContext();
      camelContext.addRoutes(new TestCamelRoutes());

      camelContext.start();

      SimpleFederatedBus federatedBus = new SimpleFederatedBus();
      CompoundContextImpl compoundContext = new CompoundContextImpl();
      compoundContext.putContext(CamelContext.class, camelContext);
      CamelMessageTranslator messageTranslator = new CamelMessageTranslator();
      messageTranslator.setInputEndpoints("direct:test1, direct:test2");
      messageTranslator.setOutputEndpoints("direct:test3, direct:test4");
      messageTranslator.initialize(compoundContext);
      federatedBus.registerTranslator(messageTranslator);

      federatedBus.start();

      ProducerTemplate producerTemplate = camelContext.createProducerTemplate();

      Thread.sleep(100); // make sure the message translator is already started

      final List<Message> results3 = Collections.synchronizedList(new LinkedList<>());
      final List<Message> results4 = Collections.synchronizedList(new LinkedList<>());

      camelContext.getEndpoint("direct:test3").createConsumer(exchange -> results3.add(exchange.getIn())).start();
      camelContext.getEndpoint("direct:test4").createConsumer(exchange -> results4.add(exchange.getIn())).start();

      producerTemplate.sendBody("direct:test1", "hello");
      Thread.sleep(100);

      producerTemplate.sendBody("direct:test2", "hi!");
      Thread.sleep(100);

      Assert.assertTrue(AbstractMessageTranslator.isSigned(results3.get(0).getHeaders()));
      Assert.assertEquals(results3.get(0).getBody().toString(), "hello");

      Assert.assertTrue(AbstractMessageTranslator.isSigned(results4.get(0).getHeaders()));
      Assert.assertEquals(results4.get(0).getBody().toString(), "hello");

      Assert.assertTrue(AbstractMessageTranslator.isSigned(results3.get(1).getHeaders()));
      Assert.assertEquals(results3.get(1).getBody().toString(), "hi!");

      Assert.assertTrue(AbstractMessageTranslator.isSigned(results4.get(1).getHeaders()));
      Assert.assertEquals(results4.get(1).getBody().toString(), "hi!");

      federatedBus.stop();
      camelContext.stop();
   }

   private static class TestCamelRoutes extends RouteBuilder {
      @Override
      public void configure() throws Exception {

      }
   }

}