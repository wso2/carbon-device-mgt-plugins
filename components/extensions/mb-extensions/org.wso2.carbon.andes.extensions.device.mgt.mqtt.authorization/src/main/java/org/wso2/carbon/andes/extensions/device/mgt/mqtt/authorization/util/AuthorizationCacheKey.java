package org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.util;

public class AuthorizationCacheKey {
    String tenantDomain;
    String deviceId;
    String deviceType;
    String username;

    public AuthorizationCacheKey(String tenantDomain, String username, String deviceId, String deviceType) {
        this.username = username;
        this.tenantDomain = tenantDomain;
        this.deviceId = deviceId;
        this.deviceType = deviceType;
    }

    @Override
    public int hashCode() {
        int result = this.deviceType.hashCode();
        result = 31 * result + ("@" + this.deviceId + "@" + this.tenantDomain + "@" + this.username).hashCode();

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof AuthorizationCacheKey) && deviceType.equals(
                ((AuthorizationCacheKey) obj).deviceType) && tenantDomain.equals(
                ((AuthorizationCacheKey) obj).tenantDomain ) && deviceId.equals(
                ((AuthorizationCacheKey) obj).deviceId) && username.equals(
                ((AuthorizationCacheKey) obj).username);
    }

}
