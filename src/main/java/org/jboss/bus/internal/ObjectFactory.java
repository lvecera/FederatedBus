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
package org.jboss.bus.internal;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.FluentPropertyBeanIntrospector;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:lenka@vecerovi.com">Lenka Večeřa</a>
 */
public class ObjectFactory {

   /**
    * Logger of this class.
    */
   private static final Logger log = LogManager.getLogger(ObjectFactory.class);

   /**
    * Cached plugin class loader.
    */
   private static ClassLoader pluginClassLoader = null;

   /**
    * There should be no instance of a utility class.
    */
   private ObjectFactory() {
   }

   /**
    * Looks up for a set method on a bean that is able to accept Element
    *
    * @param object
    *       The object on which we search for the setter.
    * @param propertyName
    *       Name of the property of type Element.
    * @param value
    *       Value to be set to the property.
    * @return <code>true</code> if operation has succeeded, <code>false</code> otherwise
    * @throws InvocationTargetException
    *       When it was not possible to call the setter on the object.
    * @throws IllegalAccessException
    *       When we did not have the correct rights to set any of the properties.
    */
   private static boolean setElementProperty(final Object object, final String propertyName, final Element value) throws InvocationTargetException, IllegalAccessException {
      try {
         final Method setter = object.getClass().getDeclaredMethod("set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1) + "AsElement", Element.class);
         setter.invoke(object, value);

         return true;
      } catch (final NoSuchMethodException e) {
         return false;
      }
   }

   /**
    * Sets the attributes of an object according to the properties provided.
    *
    * @param object
    *       Object on which the properties should be set.
    * @param properties
    *       Properties that should be set as properties of the object. Key is a name of an object property and value is its value.
    * @throws InvocationTargetException
    *       When it was not possible to call the setter on the object.
    * @throws IllegalAccessException
    *       When we did not have the correct rights to set any of the properties.
    */
   public static void setPropertiesOnObject(final Object object, final Properties properties) throws IllegalAccessException, InvocationTargetException {
      final PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
      propertyUtilsBean.addBeanIntrospector(new FluentPropertyBeanIntrospector());
      final BeanUtilsBean beanUtilsBean = new BeanUtilsBean(new EnumConvertUtilsBean(), propertyUtilsBean);

      if (log.isTraceEnabled()) {
         log.trace(String.format("Setting properties on an instance of %s.", object.getClass().getName()));
      }

      for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
         if (log.isTraceEnabled()) {
            log.trace("Setting property: '" + entry.getKey().toString() + "'='" + entry.getValue().toString() + "'");
         }

         boolean successSet = false; // did we manage to set the property value?

         if (entry.getValue() instanceof Element) { // first, is it an XML element? try to set it...
            successSet = setElementProperty(object, entry.getKey().toString(), (Element) entry.getValue());
         }

         if (!successSet) { // not yet set - either it was not an XML element or it failed with it
            beanUtilsBean.setProperty(object, entry.getKey().toString(), entry.getValue());
         }
      }
   }

   /**
    * Creates an instance of the given class and configures its properties.
    *
    * @param className
    *       Name of the class to be constructed.
    * @param properties
    *       Properties to be configured on the class instance.
    * @return Configured class instance.
    * @throws InstantiationException
    *       When it was not possible to create the object instance.
    * @throws IllegalAccessException
    *       When we did not have correct rights to create the object or set any of its properties.
    * @throws ClassNotFoundException
    *       When the given class does not exists.
    * @throws InvocationTargetException
    *       When it was not possible to call any of the properties setters.
    */
   public static Object createInstance(final String className, final Properties properties) throws InstantiationException, IllegalAccessException, ClassNotFoundException, InvocationTargetException {
      if (log.isTraceEnabled()) {
         log.trace(String.format("Summoning a new instance of class '%s'.", className));
      }

      final Object object = Class.forName(className, false, ObjectFactory.class.getClassLoader()).newInstance();
      setPropertiesOnObject(object, properties);

      return object;
   }

   /**
    * Converts camelCaseStringsWithACRONYMS to CAMEL_CASE_STRINGS_WITH_ACRONYMS
    *
    * @param camelCase
    *       The camelCase string.
    * @return The same string in equivalent format for Java enum values.
    */
   public static String camelCaseToEnum(final String camelCase) {
      final String regex = "([a-z])([A-Z])";
      final String replacement = "$1_$2";

      return camelCase.replaceAll(regex, replacement).toUpperCase();
   }

   private static class EnumConvertUtilsBean extends ConvertUtilsBean {
      @SuppressWarnings({ "rawtypes", "unchecked" })
      @Override
      public Object convert(final String value, final Class clazz) {
         if (clazz.isEnum()) {
            return Enum.valueOf(clazz, camelCaseToEnum(value));
         } else {
            return super.convert(value, clazz);
         }
      }
   }
}
