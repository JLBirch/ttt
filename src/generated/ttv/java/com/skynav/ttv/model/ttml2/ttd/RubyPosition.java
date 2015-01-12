//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.01.11 at 05:03:30 PM MST 
//


package com.skynav.ttv.model.ttml2.ttd;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for rubyPosition.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="rubyPosition">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="before"/>
 *     &lt;enumeration value="after"/>
 *     &lt;enumeration value="interCharacter"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "rubyPosition", namespace = "http://www.w3.org/ns/ttml#datatype")
@XmlEnum
public enum RubyPosition {

    @XmlEnumValue("before")
    BEFORE("before"),
    @XmlEnumValue("after")
    AFTER("after"),
    @XmlEnumValue("interCharacter")
    INTER_CHARACTER("interCharacter");
    private final String value;

    RubyPosition(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RubyPosition fromValue(String v) {
        for (RubyPosition c: RubyPosition.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
