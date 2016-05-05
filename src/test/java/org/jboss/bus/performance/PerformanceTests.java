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
package org.jboss.bus.performance;

import io.vertx.core.Vertx;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.bus.api.CompoundContext;
import org.jboss.bus.api.FederatedBus;
import org.jboss.bus.config.FederatedBusFactory;
import org.jboss.bus.config.FederatedBusFactoryTest;
import org.jboss.weld.environment.se.WeldContainer;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Scanner;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
@Test(enabled = false)
public class PerformanceTests {

   private static final Logger log = LogManager.getLogger(PerformanceTests.class);

   static WeldContainer cdi;
   static Vertx vertx;

   @Test
   public void testSimple() throws Exception {
      final CompoundContext context = FederatedBusFactory.getDefaultCompoundContext();
      final List<FederatedBus> buses = FederatedBusFactory.loadFromXml(FederatedBusFactoryTest.class.getResource("/performance1-bus.xml").getPath(), context);
      final FederatedBus federatedBus = buses.get(0);

      federatedBus.start();

      CamelContext camelContext = context.getContext(CamelContext.class);
      camelContext.addRoutes(new InputRoute());

      log.info("Running simple performance test. Press Ctrl-C to stop...");
      promptEnterKey();

      FederatedBusFactory.shutdownContext(federatedBus.getCompoundContext());
   }

   @Test
   public void testDroolsCamel() throws Exception {
      final CompoundContext context = FederatedBusFactory.getDefaultCompoundContext();
      final List<FederatedBus> buses = FederatedBusFactory.loadFromXml(FederatedBusFactoryTest.class.getResource("/performance2-bus.xml").getPath(), context);
      final FederatedBus federatedBus = buses.get(0);

      federatedBus.start();

      CamelContext camelContext = context.getContext(CamelContext.class);
      camelContext.addRoutes(new InputRoute());

      log.info("Running simple performance test. Press Ctrl-C to stop...");
      promptEnterKey();

      FederatedBusFactory.shutdownContext(federatedBus.getCompoundContext());
   }

   @Test
   public void testDroolsCdi() throws Exception {
      final CompoundContext context = FederatedBusFactory.getDefaultCompoundContext();
      cdi = context.getContext(WeldContainer.class);
      final List<FederatedBus> buses = FederatedBusFactory.loadFromXml(FederatedBusFactoryTest.class.getResource("/performance3-bus.xml").getPath(), context);
      final FederatedBus federatedBus = buses.get(0);

      federatedBus.start();

      CamelContext camelContext = context.getContext(CamelContext.class);
      camelContext.addRoutes(new CdiInputRoute());

      log.info("Running simple performance test. Press Ctrl-C to stop...");
      promptEnterKey();

      FederatedBusFactory.shutdownContext(federatedBus.getCompoundContext());
   }

   @Test
   public void testSimpleCdi() throws Exception {
      final CompoundContext context = FederatedBusFactory.getDefaultCompoundContext();
      cdi = context.getContext(WeldContainer.class);
      final List<FederatedBus> buses = FederatedBusFactory.loadFromXml(FederatedBusFactoryTest.class.getResource("/performance4-bus.xml").getPath(), context);
      final FederatedBus federatedBus = buses.get(0);

      federatedBus.start();

      CamelContext camelContext = context.getContext(CamelContext.class);
      camelContext.addRoutes(new CdiInputRoute());

      log.info("Running simple performance test. Press Ctrl-C to stop...");
      promptEnterKey();

      FederatedBusFactory.shutdownContext(federatedBus.getCompoundContext());
   }

   @Test
   public void testSimpleVertx() throws Exception {
      final CompoundContext context = FederatedBusFactory.getDefaultCompoundContext();
      vertx = context.getContext(Vertx.class);
      final List<FederatedBus> buses = FederatedBusFactory.loadFromXml(FederatedBusFactoryTest.class.getResource("/performance5-bus.xml").getPath(), context);
      final FederatedBus federatedBus = buses.get(0);

      federatedBus.start();

      CamelContext camelContext = context.getContext(CamelContext.class);
      camelContext.addRoutes(new VertxInputRoute());

      log.info("Running simple performance test. Press Ctrl-C to stop...");
      promptEnterKey();

      FederatedBusFactory.shutdownContext(federatedBus.getCompoundContext());
   }

   private void promptEnterKey() {
      Scanner scanner = new Scanner(System.in);
      scanner.nextLine();
   }

   private static class InputRoute extends RouteBuilder {

      @Override
      public void configure() throws Exception {
         from("jetty:http://0.0.0.0:8282/simple").to("direct:inTest").from("direct:outTest").setBody(simple("processed")).end();
      }
   }

   private static class CdiInputRoute extends RouteBuilder {

      @Override
      public void configure() throws Exception {
         from("jetty:http://0.0.0.0:8282/simple").bean(MyBean.class, "cdiHello").from("direct:testCdi").setBody(simple("processed")).end();
      }
   }

   private static class VertxInputRoute extends RouteBuilder {

      @Override
      public void configure() throws Exception {
         from("jetty:http://0.0.0.0:8282/simple").bean(MyBean.class, "vertxHello").from("direct:testVertx").setBody(simple("processed")).end();
      }
   }

   public static class MyBean {

      public void cdiHello(String c) {
         if (!"abc".equals(c)) {
            PerformanceTests.cdi.event().select(String.class).fire("abc");
         }
      }

      public void vertxHello(String c) {
         vertx.eventBus().send("vertxbus", c);
      }
   }
}
