/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.output.adapter.websocket.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


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
