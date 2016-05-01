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
package org.jboss.bus.api;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public interface Message extends Serializable {

   String FROM_HEADER = "federated.bus.from";
   String SOURCE_HEADER = "federated.bus.source";

   /**
    * Gets message properties.
    *
    * @return Message properties.
    */
   Properties getProperties();

   /**
    * Sets message properties.
    *
    * @param properties
    *       Message properties.
    */
   void setProperties(final Properties properties);

   /**
    * Gets a message property.
    *
    * @param name
    *       Name of the property.
    * @return The value of the property.
    */
   String getProperty(final String name);

   /**
    * Gets a message property, returning a default value when the property is not set.
    *
    * @param name
    *       Name of the property.
    * @param defaultValue
    *       The value to be returned when the property is not set.
    * @return The value of the property.
    */
   String getProperty(final String name, final String defaultValue);

   /**
    * Sets a message property.
    *
    * @param name
    *       Name of the property.
    * @param value
    *       A new value of the property.
    */
   void setProperty(final String name, final String value);

   /**
    * Gets the message payload.
    *
    * @return The message payload.
    */
   Serializable getPayload();

   /**
    * Sets the message payload.
    *
    * @param payload
    *       The message payload.
    */
   void setPayload(final Serializable payload);

   /**
    * Sets the message headers.
    *
    * @param headers
    *       The message headers.
    */
   void setHeaders(final Map<String, Object> headers);

   /**
    * Gets the message headers.
    *
    * @return The message headers.
    */
   Map<String, Object> getHeaders();

   /**
    * Sets a message header.
    *
    * @param name
    *       The header name.
    * @param value
    *       A new header value.
    */
   void setHeader(final String name, final Object value);

   /**
    * Gets a message header.
    *
    * @param name
    *       The header name.
    * @return The header value.
    */
   Object getHeader(final String name);

   /**
    * Gets a message header, returning a default value when the property is not set.
    *
    * @param name
    *       Name of the header.
    * @param defaultValue
    *       The value to be returned when the header is not set.
    * @return The value of the property.
    */
   Object getHeader(final String name, final Object defaultValue);
}
