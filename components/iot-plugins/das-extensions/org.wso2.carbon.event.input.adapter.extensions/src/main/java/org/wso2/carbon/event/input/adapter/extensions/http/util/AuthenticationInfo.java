package org.wso2.carbon.event.input.adapter.extensions.http.util;

public class AuthenticationInfo {

	/**
	 * this variable is used to check whether the client is authenticated.
	 */
	private boolean authenticated;
	private String username;
	private String tenantDomain;
	private int tenantId;
	/**
	 * returns whether the client is authenticated
	 */
	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	/**
	 * returns the authenticated client username
	 */
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * return the authenticated client tenant domain
	 */
	public String getTenantDomain() {
		return tenantDomain;
	}

	public void setTenantDomain(String tenantDomain) {
		this.tenantDomain = tenantDomain;
	}

	public int getTenantId() {
		return tenantId;
	}

	public void setTenantId(int tenantId) {
		this.tenantId = tenantId;
	}
}
