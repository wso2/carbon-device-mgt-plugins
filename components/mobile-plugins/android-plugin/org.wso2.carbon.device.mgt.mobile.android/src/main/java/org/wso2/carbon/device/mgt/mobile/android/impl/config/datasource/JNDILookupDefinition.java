/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.mobile.android.impl.config.datasource;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Class for hold JndiLookupDefinition of mobile-config.xml at parsing with JAXB.
 */
@XmlRootElement(name = "JndiLookupDefinition")
public class JNDILookupDefinition {

	private String jndiName;
	private List<JNDIProperty> jndiProperties;

	@XmlElement(name = "Name", nillable = false)
	public String getJndiName() {
		return jndiName;
	}

	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	@XmlElementWrapper(name = "Environment", nillable = false)
	@XmlElement(name = "Property", nillable = false)
	public List<JNDIProperty> getJndiProperties() {
		return jndiProperties;
	}

	public void setJndiProperties(List<JNDIProperty> jndiProperties) {
		this.jndiProperties = jndiProperties;
	}

	@XmlRootElement(name = "Property")
	public static class JNDIProperty {

		private String name;

		private String value;

		@XmlAttribute(name = "Name")
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@XmlValue
		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

}

