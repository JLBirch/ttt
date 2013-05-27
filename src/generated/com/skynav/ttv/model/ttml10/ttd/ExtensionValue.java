//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.05.26 at 07:01:59 PM MDT 
//


package com.skynav.ttv.model.ttml10.ttd;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for extensionValue.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="extensionValue">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="optional"/>
 *     &lt;enumeration value="required"/>
 *     &lt;enumeration value="use"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "extensionValue", namespace = "http://www.w3.org/ns/ttml#datatype")
@XmlEnum
public enum ExtensionValue {

    @XmlEnumValue("optional")
    OPTIONAL("optional"),
    @XmlEnumValue("required")
    REQUIRED("required"),
    @XmlEnumValue("use")
    USE("use");
    private final String value;

    ExtensionValue(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ExtensionValue fromValue(String v) {
        for (ExtensionValue c: ExtensionValue.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
