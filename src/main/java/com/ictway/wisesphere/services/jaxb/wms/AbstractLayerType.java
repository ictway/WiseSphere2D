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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AbstractLayerType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractLayerType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.deegree.org/services/wms}BaseAbstractLayerType">
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Abstract" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MetadataSetId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element ref="{http://www.deegree.org/services/wms}Keywords" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.deegree.org/services/wms}BoundingBox" minOccurs="0"/>
 *         &lt;element name="CRS" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Dimension" type="{http://www.deegree.org/services/wms}DimensionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element ref="{http://www.deegree.org/services/wms}ScaleDenominators" minOccurs="0"/>
 *           &lt;element name="ScaleUntil" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *           &lt;element name="ScaleAbove" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.deegree.org/services/wms}AbstractLayer" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="FeatureStoreId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="CoverageStoreId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="RemoteWMSStoreId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/choice>
 *         &lt;element name="DirectStyle" type="{http://www.deegree.org/services/wms}DirectStyleType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SLDStyle" type="{http://www.deegree.org/services/wms}SLDStyleType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="LayerOptions" type="{http://www.deegree.org/services/wms}LayerOptionsType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="queryable" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractLayerType", propOrder = {
    "name",
    "title",
    "_abstract",
    "metadataSetId",
    "keywords",
    "boundingBox",
    "crs",
    "dimension",
    "scaleDenominators",
    "scaleUntil",
    "scaleAbove",
    "abstractLayer",
    "featureStoreId",
    "coverageStoreId",
    "remoteWMSStoreId",
    "directStyle",
    "sldStyle",
    "layerOptions"
})
@XmlSeeAlso({
    LogicalLayer.class,
    UnrequestableLayer.class,
    RequestableLayer.class
})
public class AbstractLayerType
    extends BaseAbstractLayerType
{

    @XmlElement(name = "Name")
    protected String name;
    @XmlElement(name = "Title")
    protected String title;
    @XmlElement(name = "Abstract")
    protected String _abstract;
    @XmlElement(name = "MetadataSetId")
    protected String metadataSetId;
    @XmlElement(name = "Keywords")
    protected List<KeywordsType> keywords;
    @XmlElement(name = "BoundingBox")
    protected BoundingBoxType boundingBox;
    @XmlElement(name = "CRS")
    protected String crs;
    @XmlElement(name = "Dimension")
    protected List<DimensionType> dimension;
    @XmlElement(name = "ScaleDenominators")
    protected ScaleDenominatorsType scaleDenominators;
    @XmlElement(name = "ScaleUntil")
    protected Double scaleUntil;
    @XmlElement(name = "ScaleAbove")
    protected Double scaleAbove;
    @XmlElementRef(name = "AbstractLayer", namespace = "http://www.deegree.org/services/wms", type = JAXBElement.class)
    protected List<JAXBElement<? extends BaseAbstractLayerType>> abstractLayer;
    @XmlElement(name = "FeatureStoreId")
    protected String featureStoreId;
    @XmlElement(name = "CoverageStoreId")
    protected String coverageStoreId;
    @XmlElement(name = "RemoteWMSStoreId")
    protected String remoteWMSStoreId;
    @XmlElement(name = "DirectStyle")
    protected List<DirectStyleType> directStyle;
    @XmlElement(name = "SLDStyle")
    protected List<SLDStyleType> sldStyle;
    @XmlElement(name = "LayerOptions")
    protected LayerOptionsType layerOptions;
    @XmlAttribute(name = "queryable")
    protected Boolean queryable;

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
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the abstract property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAbstract() {
        return _abstract;
    }

    /**
     * Sets the value of the abstract property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAbstract(String value) {
        this._abstract = value;
    }

    /**
     * Gets the value of the metadataSetId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMetadataSetId() {
        return metadataSetId;
    }

    /**
     * Sets the value of the metadataSetId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMetadataSetId(String value) {
        this.metadataSetId = value;
    }

    /**
     * Gets the value of the keywords property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the keywords property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getKeywords().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link KeywordsType }
     * 
     * 
     */
    public List<KeywordsType> getKeywords() {
        if (keywords == null) {
            keywords = new ArrayList<KeywordsType>();
        }
        return this.keywords;
    }

    /**
     * Gets the value of the boundingBox property.
     * 
     * @return
     *     possible object is
     *     {@link BoundingBoxType }
     *     
     */
    public BoundingBoxType getBoundingBox() {
        return boundingBox;
    }

    /**
     * Sets the value of the boundingBox property.
     * 
     * @param value
     *     allowed object is
     *     {@link BoundingBoxType }
     *     
     */
    public void setBoundingBox(BoundingBoxType value) {
        this.boundingBox = value;
    }

    /**
     * Gets the value of the crs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCRS() {
        return crs;
    }

    /**
     * Sets the value of the crs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCRS(String value) {
        this.crs = value;
    }

    /**
     * Gets the value of the dimension property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dimension property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDimension().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DimensionType }
     * 
     * 
     */
    public List<DimensionType> getDimension() {
        if (dimension == null) {
            dimension = new ArrayList<DimensionType>();
        }
        return this.dimension;
    }

    /**
     * Gets the value of the scaleDenominators property.
     * 
     * @return
     *     possible object is
     *     {@link ScaleDenominatorsType }
     *     
     */
    public ScaleDenominatorsType getScaleDenominators() {
        return scaleDenominators;
    }

    /**
     * Sets the value of the scaleDenominators property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScaleDenominatorsType }
     *     
     */
    public void setScaleDenominators(ScaleDenominatorsType value) {
        this.scaleDenominators = value;
    }

    /**
     * Gets the value of the scaleUntil property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getScaleUntil() {
        return scaleUntil;
    }

    /**
     * Sets the value of the scaleUntil property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setScaleUntil(Double value) {
        this.scaleUntil = value;
    }

    /**
     * Gets the value of the scaleAbove property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getScaleAbove() {
        return scaleAbove;
    }

    /**
     * Sets the value of the scaleAbove property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setScaleAbove(Double value) {
        this.scaleAbove = value;
    }

    /**
     * Gets the value of the abstractLayer property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractLayer property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractLayer().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link RequestableLayer }{@code >}
     * {@link JAXBElement }{@code <}{@link UnrequestableLayer }{@code >}
     * {@link JAXBElement }{@code <}{@link DynamicLayer }{@code >}
     * {@link JAXBElement }{@code <}{@link BaseAbstractLayerType }{@code >}
     * {@link JAXBElement }{@code <}{@link LogicalLayer }{@code >}
     * {@link JAXBElement }{@code <}{@link StatisticsLayer }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends BaseAbstractLayerType>> getAbstractLayer() {
        if (abstractLayer == null) {
            abstractLayer = new ArrayList<JAXBElement<? extends BaseAbstractLayerType>>();
        }
        return this.abstractLayer;
    }

    /**
     * Gets the value of the featureStoreId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFeatureStoreId() {
        return featureStoreId;
    }

    /**
     * Sets the value of the featureStoreId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFeatureStoreId(String value) {
        this.featureStoreId = value;
    }

    /**
     * Gets the value of the coverageStoreId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCoverageStoreId() {
        return coverageStoreId;
    }

    /**
     * Sets the value of the coverageStoreId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCoverageStoreId(String value) {
        this.coverageStoreId = value;
    }

    /**
     * Gets the value of the remoteWMSStoreId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemoteWMSStoreId() {
        return remoteWMSStoreId;
    }

    /**
     * Sets the value of the remoteWMSStoreId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemoteWMSStoreId(String value) {
        this.remoteWMSStoreId = value;
    }

    /**
     * Gets the value of the directStyle property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the directStyle property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDirectStyle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DirectStyleType }
     * 
     * 
     */
    public List<DirectStyleType> getDirectStyle() {
        if (directStyle == null) {
            directStyle = new ArrayList<DirectStyleType>();
        }
        return this.directStyle;
    }

    /**
     * Gets the value of the sldStyle property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sldStyle property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSLDStyle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SLDStyleType }
     * 
     * 
     */
    public List<SLDStyleType> getSLDStyle() {
        if (sldStyle == null) {
            sldStyle = new ArrayList<SLDStyleType>();
        }
        return this.sldStyle;
    }

    /**
     * Gets the value of the layerOptions property.
     * 
     * @return
     *     possible object is
     *     {@link LayerOptionsType }
     *     
     */
    public LayerOptionsType getLayerOptions() {
        return layerOptions;
    }

    /**
     * Sets the value of the layerOptions property.
     * 
     * @param value
     *     allowed object is
     *     {@link LayerOptionsType }
     *     
     */
    public void setLayerOptions(LayerOptionsType value) {
        this.layerOptions = value;
    }

    /**
     * Gets the value of the queryable property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isQueryable() {
        return queryable;
    }

    /**
     * Sets the value of the queryable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setQueryable(Boolean value) {
        this.queryable = value;
    }

}