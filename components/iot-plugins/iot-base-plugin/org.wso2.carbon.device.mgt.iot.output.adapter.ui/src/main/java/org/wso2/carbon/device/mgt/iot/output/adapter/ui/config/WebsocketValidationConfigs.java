
package org.wso2.carbon.device.mgt.iot.output.adapter.ui.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for WebsocketValidationConfigs complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WebsocketValidationConfigs">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Authenticator" type="{}Authenticator"/>
 *         &lt;element name="Authorizer" type="{}Authorizer"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */

@XmlRootElement(name = "WebsocketValidationConfigs")
@XmlAccessorType(XmlAccessType.FIELD)
public class WebsocketValidationConfigs {

    @XmlElement(name = "Authenticator", required = true)
    protected Authenticator authenticator;
    @XmlElement(name = "Authorizer", required = true)
    protected Authorizer authorizer;

    /**
     * Gets the value of the authenticator property.
     * 
     * @return
     *     possible object is
     *     {@link Authenticator }
     *     
     */
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    /**
     * Sets the value of the authenticator property.
     * 
     * @param value
     *     allowed object is
     *     {@link Authenticator }
     *     
     */
    public void setAuthenticator(Authenticator value) {
        this.authenticator = value;
    }

    /**
     * Gets the value of the authorizer property.
     * 
     * @return
     *     possible object is
     *     {@link Authorizer }
     *     
     */
    public Authorizer getAuthorizer() {
        return authorizer;
    }

    /**
     * Sets the value of the authorizer property.
     * 
     * @param value
     *     allowed object is
     *     {@link Authorizer }
     *     
     */
    public void setAuthorizer(Authorizer value) {
        this.authorizer = value;
    }

}
