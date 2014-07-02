package com.solace.cf4j.config;

import java.util.ArrayList;
import java.util.List;

import com.solace.cf4j.Cache;

public class Caches {

    protected List<Caches.CacheConfig> cache;

    /**
     * Gets the value of the cache property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cache property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCache().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Caches.CacheConfig }
     * 
     * 
     */
    public List<Caches.CacheConfig> getCache() {
        if (cache == null) {
            cache = new ArrayList<Caches.CacheConfig>();
        }
        return this.cache;
    }
    
    public void setCaches(List<Caches.CacheConfig> caches)
    {
    	this.cache = caches;
    }


    public static class CacheConfig {

        protected Caches.CacheConfig.Type type;
        protected String name;

        /**
         * Gets the value of the type property.
         * 
         * @return
         *     possible object is
         *     {@link Caches.CacheConfig.Type }
         *     
         */
        public Caches.CacheConfig.Type getType() {
            return type;
        }

        /**
         * Sets the value of the type property.
         * 
         * @param value
         *     allowed object is
         *     {@link Caches.CacheConfig.Type }
         *     
         */
        public void setType(Caches.CacheConfig.Type value) {
            this.type = value;
        }

        /**
         * Gets the value of the name property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setName(String value) {
            this.name = value;
        }


        public static class Type {

            protected List<Caches.CacheConfig.Type.Property> property;
            protected Class<? extends Cache> value;

            /**
             * Gets the value of the property property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the property property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getProperty().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Caches.CacheConfig.Type.Property }
             * 
             * 
             */
            public List<Caches.CacheConfig.Type.Property> getProperty() {
                if (property == null) {
                    property = new ArrayList<Caches.CacheConfig.Type.Property>();
                }
                return this.property;
            }
            
            public void setProperties(List<Caches.CacheConfig.Type.Property> value)
            {
            	this.property = value;
            }

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public Class<? extends Cache> getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValue(Class<? extends Cache> value) {
                this.value = value;
            }


            public static class Property {

                protected String name;
                protected String value;

                /**
                 * Gets the value of the name property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getName() {
                    return name;
                }

                /**
                 * Sets the value of the name property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setName(String value) {
                    this.name = value;
                }

                /**
                 * Gets the value of the value property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getValue() {
                    return value;
                }

                /**
                 * Sets the value of the value property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setValue(String value) {
                    this.value = value;
                }

            }

        }

    }

}
