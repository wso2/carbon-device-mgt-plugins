package org.wso2.carbon.device.mgt.iot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;

/**
 * This class provides util methods for manager services
 */
public abstract class AbstractManagerService {
	private static Log log = LogFactory.getLog(AbstractManagerService.class);

	protected PrivilegedCarbonContext ctx;

	// Returns current username
	// this method assumes WebappAuthenticationValve is setting username, tenant
	// upon successful authentication. Add context param doAuthentication to "true" on web.xml

	/**
	 * Returns current username. this method assumes WebappAuthenticationValve is setting username,
	 * tenant_domain, tenant_id upon successful authentication.
	 * Add context param doAuthentication to "true" on web.xml.
	 *
	 * @return current username
	 */
	protected String getCurrentUserName(){
		return CarbonContext.getThreadLocalCarbonContext().getUsername();
	}

	/**
	 * Returns OSGi service. Should invoke endTenantFlow() to end the tenant flow once osgi service
	 * is consumed.
	 * @param osgiServiceClass
	 * @param <T> OSGi service class
	 * @return OSGi service
	 */
	protected <T> T getServiceProvider(Class<T> osgiServiceClass) {
		String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
		PrivilegedCarbonContext.startTenantFlow();
		ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		ctx.setTenantDomain(tenantDomain, true);
		if (log.isDebugEnabled()) {
			log.debug("Getting thread local carbon context for tenant domain: " + tenantDomain);
		}
		return (T) ctx.getOSGiService(osgiServiceClass.getClass(), null);
	}

	/**
	 * Ends tenant flow.
	 */
	protected void endTenantFlow() {
		PrivilegedCarbonContext.endTenantFlow();
		ctx = null;
		if (log.isDebugEnabled()) {
			log.debug("Tenant flow ended");
		}
	}
}
