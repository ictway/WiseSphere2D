//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.16 at 01:29:12 PM KST 
//


package com.ictway.wisesphere.services.jaxb.wms;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for DirectStyleType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DirectStyleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="File" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="LegendConfigurationFile" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="LegendGraphicFile">
 *             &lt;complexType>
 *               &lt;simpleContent>
 *                 &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                   &lt;attribute name="outputGetLegendGraphicUrl" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *                 &lt;/extension>
 *               &lt;/simpleContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DirectStyleType", propOrder = {
    "file",
    "name",
    "legendConfigurationFile",
    "legendGraphicFile"
})
public class DirectStyleType {

    @XmlElement(name = "File", required = true)
    protected String file;
    @XmlElement(name = "Name")
    protected String name;
    @XmlElement(name = "LegendConfigurationFile")
    protected String legendConfigurationFile;
    @XmlElement(name = "LegendGraphicFile")
    protected DirectStyleType.LegendGraphicFile legendGraphicFile;

    /**
     * Gets the value of the file property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFile(String value) {
        this.file = value;
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

    /**
     * Gets the value of the legendConfigurationFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLegendConfigurationFile() {
        return legendConfigurationFile;
    }

    /**
     * Sets the value of the legendConfigurationFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLegendConfigurationFile(String value) {
        this.legendConfigurationFile = value;
    }

    /**
     * Gets the value of the legendGraphicFile property.
     * 
     * @return
     *     possible object is
     *     {@link DirectStyleType.LegendGraphicFile }
     *     
     */
    public DirectStyleType.LegendGraphicFile getLegendGraphicFile() {
        return legendGraphicFile;
    }

    /**
     * Sets the value of the legendGraphicFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link DirectStyleType.LegendGraphicFile }
     *     
     */
    public void setLegendGraphicFile(DirectStyleType.LegendGraphicFile value) {
        this.legendGraphicFile = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
     *       &lt;attribute name="outputGetLegendGraphicUrl" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class LegendGraphicFile {

        @XmlValue
        protected String value;
        @XmlAttribute(name = "outputGetLegendGraphicUrl")
        protected Boolean outputGetLegendGraphicUrl;

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

        /**
         * Gets the value of the outputGetLegendGraphicUrl property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isOutputGetLegendGraphicUrl() {
            if (outputGetLegendGraphicUrl == null) {
                return true;
            } else {
                return outputGetLegendGraphicUrl;
            }
        }

        /**
         * Sets the value of the outputGetLegendGraphicUrl property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setOutputGetLegendGraphicUrl(Boolean value) {
            this.outputGetLegendGraphicUrl = value;
        }

    }

}