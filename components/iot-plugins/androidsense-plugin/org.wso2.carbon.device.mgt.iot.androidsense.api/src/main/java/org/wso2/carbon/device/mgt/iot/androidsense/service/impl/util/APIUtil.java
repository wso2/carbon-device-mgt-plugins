package org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagementException;
import org.wso2.carbon.device.mgt.core.permission.mgt.PermissionUtils;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides utility functions used by REST-API.
 */
public class APIUtil {

	private static Log log = LogFactory.getLog(APIUtil.class);

	public static String getAuthenticatedUser() {
		PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		String username = threadLocalCarbonContext.getUsername();
		String tenantDomain = threadLocalCarbonContext.getTenantDomain();
		if (username.endsWith(tenantDomain)) {
			return username.substring(0, username.lastIndexOf("@"));
		}
		return username;
	}

	public static DeviceManagementProviderService getDeviceManagementService() {
		PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		DeviceManagementProviderService deviceManagementProviderService =
				(DeviceManagementProviderService) ctx.getOSGiService(DeviceManagementProviderService.class, null);
		if (deviceManagementProviderService == null) {
			String msg = "Device Management service has not initialized.";
			log.error(msg);
			throw new IllegalStateException(msg);
		}
		return deviceManagementProviderService;
	}

	public static AnalyticsDataAPI getAnalyticsDataAPI() {
		PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		AnalyticsDataAPI analyticsDataAPI =
				(AnalyticsDataAPI) ctx.getOSGiService(AnalyticsDataAPI.class, null);
		if (analyticsDataAPI == null) {
			String msg = "Analytics api service has not initialized.";
			log.error(msg);
			throw new IllegalStateException(msg);
		}
		return analyticsDataAPI;
	}

	public static List<SensorRecord> getAllEventsForDevice(String tableName, String query,
														   List<SortByField> sortByFields) throws AnalyticsException {
		int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
		AnalyticsDataAPI analyticsDataAPI = getAnalyticsDataAPI();
		int eventCount = analyticsDataAPI.searchCount(tenantId, tableName, query);
		if (eventCount == 0) {
			return null;
		}
		AnalyticsDrillDownRequest drillDownRequest = new AnalyticsDrillDownRequest();
		drillDownRequest.setQuery(query);
		drillDownRequest.setTableName(tableName);
		drillDownRequest.setRecordCount(eventCount);
		if (sortByFields != null) {
			drillDownRequest.setSortByFields(sortByFields);
		}
		List<SearchResultEntry> resultEntries = analyticsDataAPI.drillDownSearch(tenantId, drillDownRequest);
		List<String> recordIds = getRecordIds(resultEntries);
		AnalyticsDataResponse response = analyticsDataAPI.get(tenantId, tableName, 1, null, recordIds);
		Map<String, SensorRecord> sensorDatas = createSensorData(AnalyticsDataServiceUtils.listRecords(
				analyticsDataAPI, response));
		List<SensorRecord> sortedSensorData = getSortedSensorData(sensorDatas, resultEntries);
		return sortedSensorData;
	}

	private static List<String> getRecordIds(List<SearchResultEntry> searchResults) {
		List<String> ids = new ArrayList<>();
		for (SearchResultEntry searchResult : searchResults) {
			ids.add(searchResult.getId());
		}
		return ids;
	}

	public static List<SensorRecord> getSortedSensorData(Map<String, SensorRecord> sensorDatas,
														List<SearchResultEntry> searchResults) {
		List<SensorRecord> sortedRecords = new ArrayList<>();
		for (SearchResultEntry searchResultEntry : searchResults) {
			sortedRecords.add(sensorDatas.get(searchResultEntry.getId()));
		}
		return sortedRecords;
	}

	/**
	 * Creates the SensorDatas from records.
	 *
	 * @param records the records
	 * @return the Map of SensorRecord <id, SensorRecord>
	 */
	public static Map<String, SensorRecord> createSensorData(List<Record> records) {
		Map<String, SensorRecord> sensorDatas = new HashMap<>();
		for (Record record : records) {
			SensorRecord sensorData = createSensorData(record);
			sensorDatas.put(sensorData.getId(), sensorData);
		}
		return sensorDatas;
	}

	/**
	 * Create a SensorRecord object out of a Record object
	 *
	 * @param record the record object
	 * @return SensorRecord object
	 */
	public static SensorRecord createSensorData(Record record) {
		SensorRecord recordBean = new SensorRecord();
		recordBean.setId(record.getId());
		recordBean.setValues(record.getValues());
		return recordBean;
	}

	public static APIManagementProviderService getAPIManagementProviderService() {
		PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		APIManagementProviderService apiManagementProviderService =
				(APIManagementProviderService) ctx.getOSGiService(APIManagementProviderService.class, null);
		if (apiManagementProviderService == null) {
			String msg = "API management provider service has not initialized.";
			log.error(msg);
			throw new IllegalStateException(msg);
		}
		return apiManagementProviderService;
	}

	public static JWTClientManagerService getJWTClientManagerService() {
		PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		JWTClientManagerService jwtClientManagerService =
				(JWTClientManagerService) ctx.getOSGiService(JWTClientManagerService.class, null);
		if (jwtClientManagerService == null) {
			String msg = "JWT Client manager service has not initialized.";
			log.error(msg);
			throw new IllegalStateException(msg);
		}
		return jwtClientManagerService;
	}

	public static DeviceAccessAuthorizationService getDeviceAccessAuthorizationService() {
		PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		DeviceAccessAuthorizationService deviceAccessAuthorizationService =
				(DeviceAccessAuthorizationService) ctx.getOSGiService(DeviceAccessAuthorizationService.class, null);
		if (deviceAccessAuthorizationService == null) {
			String msg = "Device Authorization service has not initialized.";
			log.error(msg);
			throw new IllegalStateException(msg);
		}
		return deviceAccessAuthorizationService;
	}


	public static void registerApiAccessRoles(String user) {
		UserStoreManager userStoreManager = null;
		try {
			userStoreManager = getUserStoreManager();
			String[] userList = new String[]{user};
			if (userStoreManager != null) {
				if (!userStoreManager.isExistingRole(Constants.DEFAULT_ROLE_NAME)) {
					userStoreManager.addRole(Constants.DEFAULT_ROLE_NAME, userList, Constants.DEFAULT_PERMISSION);
				} else {
					userStoreManager.updateUserListOfRole(Constants.DEFAULT_ROLE_NAME, null, userList);
				}
			}
		} catch (UserStoreException e) {
			log.error("Error while creating a role and adding a user for android_sense.", e);
		}
	}

	public static UserStoreManager getUserStoreManager() {
		RealmService realmService;
		UserStoreManager userStoreManager;
		try {
			PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
			realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
			if (realmService == null) {
				String msg = "Realm service has not initialized.";
				log.error(msg);
				throw new IllegalStateException(msg);
			}
			int tenantId = ctx.getTenantId();
			userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
		} catch (UserStoreException e) {
			String msg = "Error occurred while retrieving current user store manager";
			log.error(msg, e);
			throw new IllegalStateException(msg);
		}
		return userStoreManager;
	}
}
