/*
 * -----------------------------------------------------------------------\
 * FederatedBus
 *  
 * Copyright (C) 2014 - 2016 the original author or authors.
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
package org.jboss.bus.config;

import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxImpl;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangeTimedOutException;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.bus.api.CompoundContext;
import org.jboss.bus.api.FederatedBus;
import org.jboss.bus.api.FederatedBusException;
import org.jboss.bus.api.MessageTranslator;
import org.jboss.bus.config.model.Federated;
import org.jboss.bus.config.model.PropertyType;
import org.jboss.bus.internal.AbstractFederatedBus;
import org.jboss.bus.internal.CompoundContextImpl;
import org.jboss.bus.internal.ObjectFactory;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class FederatedBusFactory {

   /**
    * Logger for this class.
    */
   private static final Logger log = LogManager.getLogger(FederatedBusFactory.class);

   /**
    * Gets default compound context.
    * @return Compound context that contains Camel context, Vertx or Weld container.
    */
   public static CompoundContext getDefaultCompoundContext() {
      CamelContext camelContext = new DefaultCamelContext();
      try {
         camelContext.start();
      } catch (Exception e) {
         log.error("Unable to start camel context: ", e);;
      }
      CompoundContextImpl compoundContext = new CompoundContextImpl();
      compoundContext.putContext(CamelContext.class, camelContext);

      Vertx vertx = Vertx.vertx();
      compoundContext.putContext(Vertx.class, vertx);

      WeldContainer weld = new Weld().initialize();
      compoundContext.putContext(WeldContainer.class, weld);


      return compoundContext;
   }

   /**
    * Gets model of bus based on XML file.
    * @param fileName Name of the XML file containing the bus description.
    * @return Federated
    * @throws FederatedBusException It throws this exception when XML configuration cannot be validated.
    */
   private static Federated getBusModel(final String fileName) throws FederatedBusException {
      try {
         final String busConfig = Files.lines(Paths.get(fileName), Charset.forName("UTF-8")).collect(Collectors.joining());
         final Source configXML = new StreamSource(new ByteArrayInputStream(busConfig.getBytes("UTF-8")));
         final String schemaFileName = "federated-bus.xsd";

         URL configXsdUrl = FederatedBusFactory.class.getResource("/" + schemaFileName);

         final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
         final Schema schema = schemaFactory.newSchema(configXsdUrl);

         final JAXBContext context = JAXBContext.newInstance(Federated.class);
         final Unmarshaller unmarshaller = context.createUnmarshaller();
         unmarshaller.setSchema(schema);
         return (Federated) unmarshaller.unmarshal(configXML);
      } catch (final Exception e) {
         throw new FederatedBusException("Cannot validate XML configuration. ", e);
      }

   }

   /**
    * Parses properties.
    * @param properties List of properties that are parsed.
    * @return Parsed properties.
    * @throws FederatedBusException This exception is thrown when property tag has no attribute value nor body set.
    */
   private static Properties parseProperties(final List<PropertyType> properties) throws FederatedBusException{
      final Properties myProps = new Properties();

      for(final PropertyType pt: properties) {
         final String value = pt.getValue();
         final Element element = pt.getAny();

         if (value != null && element != null) {
            throw new FederatedBusException(String.format("A property tag can either have an attribute value (%s) or the body (%s) set, not both at the same time.", value, element.toString()));
         } else if (element == null && value == null) {
            throw new FederatedBusException("A property tag must either have an attribute value or the body set.");
         }

         myProps.put(pt.getName(), value == null ? element : value);
      }

      return myProps;
   }

   /**
    * Loading from XML. This method nitializes federated bus with given context.
    * @param fileName The name of the XML file.
    * @param compoundContext Compound context that is used for initialization.
    * @return List of buses that are described in the XML file.
    * @throws FederatedBusException Throws exception when loading from the XML is not successful.
    */
   public static List<FederatedBus> loadFromXml(final String fileName, final CompoundContext compoundContext) throws FederatedBusException {
      final List<FederatedBus> buses = new LinkedList<>();
      final Federated federated = getBusModel(fileName);

      try {
         for (Federated.Bus bus : federated.getBus()) {
            final FederatedBus myBus = (FederatedBus) ObjectFactory.createInstance(bus.getClazz(), bus.getProperties() == null ? new Properties() : parseProperties(bus.getProperties().getProperty()));
            myBus.setCompoundContext(compoundContext);

            for (Federated.Bus.Translators.Translator trans : bus.getTranslators().getTranslator()) {
               final MessageTranslator myTrans = (MessageTranslator) ObjectFactory.createInstance(trans.getClazz(), trans.getProperties() == null ? new Properties() : parseProperties(trans.getProperties().getProperty()));
               myBus.registerTranslator(myTrans);
            }

            buses.add(myBus);
         }
      } catch (Exception e) {
         throw new FederatedBusException("", e);
      }

      return buses;
   }

   /**
    * Loading from XML file and initialization of federated bus with default compound context.
    * @param fileName The name of the XML file.
    * @return List of buses that are described in the XML file.
    * @throws FederatedBusException Throws exception when loading from the XML is not successful.
    */
   public static List<FederatedBus> loadFromXml(final String fileName) throws FederatedBusException {
      return loadFromXml(fileName, getDefaultCompoundContext());
   }

   /**
    * Shuts down the compound context.
    * @param compoundContext Context that is about to be shut down.
    */
   public static void shutdownContext(CompoundContext compoundContext) {
      WeldContainer weldContainer = compoundContext.getContext(WeldContainer.class);
      if (weldContainer != null) {
         weldContainer.shutdown();
      }

      CamelContext camelContext = compoundContext.getContext(CamelContext.class);
      if (camelContext != null) {
         try {
            camelContext.stop();
         } catch (Exception e) {
            log.warn("Unable to shutdown Camel smoothly: ", e);
         }
      }

      Vertx vertx = compoundContext.getContext(Vertx.class);
      if (vertx != null) {
         vertx.close();
      }

   }
}
