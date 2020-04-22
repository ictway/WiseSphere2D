//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.16 at 01:29:12 PM KST 
//


package com.ictway.wisesphere.services.jaxb.wms;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ServiceConfigurationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ServiceConfigurationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DefaultLayerOptions" type="{http://www.deegree.org/services/wms}LayerOptionsType" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="ThemeId" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element ref="{http://www.deegree.org/services/wms}AbstractLayer" minOccurs="0"/>
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
@XmlType(name = "ServiceConfigurationType", propOrder = {
    "defaultLayerOptions",
    "themeId",
    "abstractLayer"
})
public class ServiceConfigurationType {

    @XmlElement(name = "DefaultLayerOptions")
    protected LayerOptionsType defaultLayerOptions;
    @XmlElement(name = "ThemeId")
    protected List<String> themeId;
    @XmlElementRef(name = "AbstractLayer", namespace = "http://www.deegree.org/services/wms", type = JAXBElement.class)
    protected JAXBElement<? extends BaseAbstractLayerType> abstractLayer;

    /**
     * Gets the value of the defaultLayerOptions property.
     * 
     * @return
     *     possible object is
     *     {@link LayerOptionsType }
     *     
     */
    public LayerOptionsType getDefaultLayerOptions() {
        return defaultLayerOptions;
    }

    /**
     * Sets the value of the defaultLayerOptions property.
     * 
     * @param value
     *     allowed object is
     *     {@link LayerOptionsType }
     *     
     */
    public void setDefaultLayerOptions(LayerOptionsType value) {
        this.defaultLayerOptions = value;
    }

    /**
     * Gets the value of the themeId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the themeId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getThemeId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getThemeId() {
        if (themeId == null) {
            themeId = new ArrayList<String>();
        }
        return this.themeId;
    }

    /**
     * Gets the value of the abstractLayer property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link RequestableLayer }{@code >}
     *     {@link JAXBElement }{@code <}{@link UnrequestableLayer }{@code >}
     *     {@link JAXBElement }{@code <}{@link DynamicLayer }{@code >}
     *     {@link JAXBElement }{@code <}{@link BaseAbstractLayerType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LogicalLayer }{@code >}
     *     {@link JAXBElement }{@code <}{@link StatisticsLayer }{@code >}
     *     
     */
    public JAXBElement<? extends BaseAbstractLayerType> getAbstractLayer() {
        return abstractLayer;
    }

    /**
     * Sets the value of the abstractLayer property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link RequestableLayer }{@code >}
     *     {@link JAXBElement }{@code <}{@link UnrequestableLayer }{@code >}
     *     {@link JAXBElement }{@code <}{@link DynamicLayer }{@code >}
     *     {@link JAXBElement }{@code <}{@link BaseAbstractLayerType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LogicalLayer }{@code >}
     *     {@link JAXBElement }{@code <}{@link StatisticsLayer }{@code >}
     *     
     */
    public void setAbstractLayer(JAXBElement<? extends BaseAbstractLayerType> value) {
        this.abstractLayer = value;
    }

}