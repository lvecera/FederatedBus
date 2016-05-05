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
package org.jboss.bus.internal;

import org.jboss.bus.api.Message;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class MessageImpl implements Message {

   private static final long serialVersionUID = -6537640529774091119L;

   /**
    * Headers of the message.
    */
   private Map<String, Object> headers;

   /**
    * Properties of the message.
    */
   private Properties properties;

   /**
    * Message payload.
    */
   private Serializable payload = null;

   /**
    * Creates an empty message.
    */
   public MessageImpl() {
      this.headers = new HashMap<>();
      this.properties = new Properties();
   }

   /**
    * Creates a message with the given payload.
    *
    * @param payload Message payload.
    */
   public MessageImpl(final Serializable payload) {
      this();
      this.payload = payload;
   }

   @Override
   public Properties getProperties() {
      return properties;
   }

   @Override
   public void setProperties(final Properties properties) {
      this.properties = properties;
   }

   @Override
   public String getProperty(final String name) {
      return properties.getProperty(name);
   }

   @Override
   public String getProperty(final String name, final String defaultValue) {
      return properties.getProperty(name, defaultValue);
   }

   @Override
   public void setProperty(final String name, final String value) {
      properties.setProperty(name, value);
   }

   @Override
   public Serializable getPayload() {
      return payload;
   }

   @Override
   public void setPayload(final Serializable payload) {
      this.payload = payload;
   }

   @Override
   public void setHeaders(final Map<String, Object> headers) {
      this.headers = headers;
   }

   @Override
   public Map<String, Object> getHeaders() {
      return headers;
   }

   @Override
   public void setHeader(final String name, final Object value) {
      headers.put(name, value);
   }

   @Override
   public Object getHeader(final String name) {
      return headers.get(name);
   }

   @Override
   public Object getHeader(final String name, final Object defaultValue) {
      return headers.getOrDefault(name, defaultValue);
   }

   @Override
   public boolean equals(final Object obj) {
      if (obj == null) {
         return false;
      }
      if (!(obj instanceof Message)) {
         return false;
      }
      final MessageImpl m = (MessageImpl) obj;
      if (!payload.equals(m.payload)) {
         return false;
      }
      if (!headers.equals(m.headers)) {
         return false;
      }
      if (!properties.equals(m.properties)) {
         return false;
      }
      return true;
   }

   @Override
   public int hashCode() {
      return payload.hashCode() + headers.hashCode() + properties.hashCode();
   }

   @Override
   public String toString() {
      return "[payload=[" + payload + "]; headers=" + headers.toString() + "; properties=" + properties.toString() + "]";
   }

}
