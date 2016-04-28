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
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.bus.api.CompoundContext;
import org.jboss.bus.api.FederatedBus;
import org.jboss.bus.api.FederatedBusException;
import org.jboss.bus.api.MessageTranslator;
import org.jboss.bus.config.model.Federated;
import org.jboss.bus.config.model.PropertyType;
import org.jboss.bus.internal.CompoundContextImpl;
import org.jboss.bus.internal.ObjectFactory;
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

   private static final Logger log = LogManager.getLogger(FederatedBusFactory.class);

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

      return compoundContext;
   }

   private static Federated getBusModel(final String fileName) throws FederatedBusException {
      try {
         final String busConfig = Files.lines(Paths.get(fileName), Charset.forName("UTF-8")).collect(Collectors.joining());
         final Source configXML = new StreamSource(new ByteArrayInputStream(busConfig.getBytes("UTF-8")));
         final String schemaFileName = "federated-bus.xsd";

         URL configXsdUrl = FederatedBusFactory.class.getResource("/" + schemaFileName);

         InputStream test = configXsdUrl.openStream();
         //noinspection ResultOfMethodCallIgnored
         test.read(); // there always is a byte
         test.close(); // we do not need finally for this as we could not have failed

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

   public static List<FederatedBus> loadFromXml(final String fileName, final CompoundContext compoundContext) throws FederatedBusException {
      final List<FederatedBus> buses = new LinkedList<>();
      final Federated federated = getBusModel(fileName);

      try {
         for (Federated.Bus bus : federated.getBus()) {
            final FederatedBus myBus = (FederatedBus) ObjectFactory.createInstance(bus.getClazz(), parseProperties(bus.getProperties().getProperty()));

            for (Federated.Bus.Translators.Translator trans : bus.getTranslators().getTranslator()) {
               final MessageTranslator myTrans = (MessageTranslator) ObjectFactory.createInstance(trans.getClazz(), parseProperties(trans.getProperties().getProperty()));
               myBus.registerTranslator(myTrans);
               myTrans.initialize(compoundContext);
            }

            buses.add(myBus);
         }
      } catch (Exception e) {
         throw new FederatedBusException("", e);
      }

      return buses;
   }

   public static List<FederatedBus> loadFromXml(final String fileName) throws FederatedBusException {
      return loadFromXml(fileName, getDefaultCompoundContext());
   }
}
