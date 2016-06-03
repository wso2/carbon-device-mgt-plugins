package org.wso2.carbon.mdm.services.android.omadm.operations.beans;

/**
 * This class acts as an entity which represents a Profile Operation.
 */
public class ProfileOperation {

    String featureCode;
    String data;
    boolean isEnabled;
    boolean isCompliant;

    public boolean isCompliant() {
        return isCompliant;
    }

    public void setIsCompliant(boolean compliance) {
        this.isCompliant = compliance;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getFeatureCode() {
        return featureCode;
    }

    public void setFeatureCode(String featureCode) {
        this.featureCode = featureCode;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
