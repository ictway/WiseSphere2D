//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.16 at 01:29:12 PM KST 
//


package com.ictway.wisesphere.services.jaxb.wms;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GMLVersionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="GMLVersionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="GML_2"/>
 *     &lt;enumeration value="GML_30"/>
 *     &lt;enumeration value="GML_31"/>
 *     &lt;enumeration value="GML_32"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "GMLVersionType")
@XmlEnum
public enum GMLVersionType {

    GML_2,
    GML_30,
    GML_31,
    GML_32;

    public String value() {
        return name();
    }

    public static GMLVersionType fromValue(String v) {
        return valueOf(v);
    }

}
