package org.wso2.carbon.iot.android.sense.util.dto;


/**
 * This class represents the data that are required to register
 * the oauth application.
 */
public class ApiRegistrationProfile {

    public String applicationName;
    public String tags[];
    public boolean isAllowedToAllDomains;
    public String consumerKey;
    public String consumerSecret;
    public boolean isMappingAnExistingOAuthApp;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public boolean isAllowedToAllDomains() {
        return isAllowedToAllDomains;
    }

    public void setIsAllowedToAllDomains(boolean isAllowedToAllDomains) {
        this.isAllowedToAllDomains = isAllowedToAllDomains;
    }

    public boolean isMappingAnExistingOAuthApp() {
        return isMappingAnExistingOAuthApp;
    }

    public void setIsMappingAnExistingOAuthApp(boolean isMappingAnExistingOAuthApp) {
        this.isMappingAnExistingOAuthApp = isMappingAnExistingOAuthApp;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }
}