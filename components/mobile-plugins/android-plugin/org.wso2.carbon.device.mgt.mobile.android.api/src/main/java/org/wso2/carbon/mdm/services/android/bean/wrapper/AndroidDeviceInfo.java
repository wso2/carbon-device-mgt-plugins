package org.wso2.carbon.mdm.services.android.bean.wrapper;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ApiModel(
        value = "DeviceInfo",
        description = "This class carries all information related to the device information provided by a device."
)
public class AndroidDeviceInfo extends DeviceInfo implements Serializable {
    private static final long serialVersionUID = 1998101733L;
    @ApiModelProperty(
            name = "IMEI",
            value = "IMEI number of the device.",
            required = true
    )
    private String IMEI;
    @ApiModelProperty(
            name = "IMSI",
            value = "IMSI number of the device.",
            required = true
    )
    private String IMSI;
    @ApiModelProperty(
            name = "deviceModel",
            value = "Model of the device.",
            required = true
    )
    private String deviceModel;
    @ApiModelProperty(
            name = "vendor",
            value = "Vendor of the device.",
            required = true
    )
    private String vendor;
    @ApiModelProperty(
            name = "osVersion",
            value = "Operating system version.",
            required = true
    )
    private String osVersion;
    @ApiModelProperty(
            name = "batteryLevel",
            value = "Battery level of the device.",
            required = true
    )
    private Double batteryLevel;
    @ApiModelProperty(
            name = "internalTotalMemory",
            value = "Total internal memory of the device.",
            required = true
    )
    private Double internalTotalMemory;
    @ApiModelProperty(
            name = "internalAvailableMemory",
            value = "Total available memory of the device.",
            required = true
    )
    private Double internalAvailableMemory;
    @ApiModelProperty(
            name = "externalTotalMemory",
            value = "Total external memory of the device.",
            required = true
    )
    private Double externalTotalMemory;
    @ApiModelProperty(
            name = "externalAvailableMemory",
            value = "Total external memory avilable of the device.",
            required = true
    )
    private Double externalAvailableMemory;
    @ApiModelProperty(
            name = "operator",
            value = "Mobile operator of the device.",
            required = true
    )
    private String operator;
    @ApiModelProperty(
            name = "connectionType",
            value = "How the device is connected to the network.",
            required = true
    )
    private String connectionType;
    @ApiModelProperty(
            name = "mobileSignalStrength",
            value = "Current mobile signal strength.",
            required = true
    )
    private Double mobileSignalStrength;
    @ApiModelProperty(
            name = "ssid",
            value = "ssid of the connected WiFi.",
            required = true
    )
    private String ssid;
    @ApiModelProperty(
            name = "cpuUsage",
            value = "Current total cpu usage.",
            required = true
    )
    private Double cpuUsage;
    @ApiModelProperty(
            name = "totalRAMMemory",
            value = "Total Ram memory size.",
            required = true
    )
    private Double totalRAMMemory;
    @ApiModelProperty(
            name = "availableRAMMemory",
            value = "Available total memory of RAM.",
            required = true
    )
    private Double availableRAMMemory;
    @ApiModelProperty(
            name = "pluggedIn",
            value = "Whether the device is plugged into power or not.",
            required = true
    )
    private boolean pluggedIn;
    @ApiModelProperty(
            name = "updatedTime",
            value = "Device updated time.",
            required = true
    )
    private Date updatedTime;
    @ApiModelProperty(
            name = "location",
            value = "Last updated location of the device",
            required = false
    )
    private DeviceLocation location;
    @ApiModelProperty(
            name = "deviceDetailsMap",
            value = ".",
            required = true
    )
    private Map<String, String> deviceDetailsMap = new HashMap();

    public AndroidDeviceInfo() {
    }

    public DeviceLocation getLocation() {
        return this.location;
    }

    public void setLocation(DeviceLocation location) {
        this.location = location;
    }

    public String getIMEI() {
        return this.IMEI != null?this.IMEI:"";
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public String getIMSI() {
        return this.IMSI != null?this.IMSI:"";
    }

    public void setIMSI(String IMSI) {
        this.IMSI = IMSI;
    }

    public String getDeviceModel() {
        return this.deviceModel != null?this.deviceModel:"";
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getVendor() {
        return this.vendor != null?this.vendor:"";
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getOsVersion() {
        return this.osVersion != null?this.osVersion:"";
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public Double getBatteryLevel() {
        return this.batteryLevel != null?this.batteryLevel:Double.valueOf(0.0D);
    }

    public void setBatteryLevel(Double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Double getInternalTotalMemory() {
        return this.internalTotalMemory != null?this.internalTotalMemory:Double.valueOf(0.0D);
    }

    public void setInternalTotalMemory(Double internalTotalMemory) {
        this.internalTotalMemory = internalTotalMemory;
    }

    public Double getInternalAvailableMemory() {
        return this.internalAvailableMemory != null?this.internalAvailableMemory:Double.valueOf(0.0D);
    }

    public void setInternalAvailableMemory(Double internalAvailableMemory) {
        this.internalAvailableMemory = internalAvailableMemory;
    }

    public Double getExternalTotalMemory() {
        return this.externalTotalMemory != null?this.externalTotalMemory:Double.valueOf(0.0D);
    }

    public void setExternalTotalMemory(Double externalTotalMemory) {
        this.externalTotalMemory = externalTotalMemory;
    }

    public Double getExternalAvailableMemory() {
        return this.externalAvailableMemory != null?this.externalAvailableMemory:Double.valueOf(0.0D);
    }

    public void setExternalAvailableMemory(Double externalAvailableMemory) {
        this.externalAvailableMemory = externalAvailableMemory;
    }

    public String getOperator() {
        return this.operator != null?this.operator:"";
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getConnectionType() {
        return this.connectionType != null?this.connectionType:"";
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public Double getMobileSignalStrength() {
        return this.mobileSignalStrength != null?this.mobileSignalStrength:Double.valueOf(0.0D);
    }

    public void setMobileSignalStrength(Double mobileSignalStrength) {
        this.mobileSignalStrength = mobileSignalStrength;
    }

    public String getSsid() {
        return this.ssid != null?this.ssid:"";
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public Double getCpuUsage() {
        return this.cpuUsage != null?this.cpuUsage:Double.valueOf(0.0D);
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public Double getTotalRAMMemory() {
        return this.totalRAMMemory != null?this.totalRAMMemory:Double.valueOf(0.0D);
    }

    public void setTotalRAMMemory(Double totalRAMMemory) {
        this.totalRAMMemory = totalRAMMemory;
    }

    public Double getAvailableRAMMemory() {
        return this.availableRAMMemory != null?this.availableRAMMemory:Double.valueOf(0.0D);
    }

    public void setAvailableRAMMemory(Double availableRAMMemory) {
        this.availableRAMMemory = availableRAMMemory;
    }

    public boolean isPluggedIn() {
        return this.pluggedIn;
    }

    public void setPluggedIn(boolean pluggedIn) {
        this.pluggedIn = pluggedIn;
    }

    public Date getUpdatedTime() {
        if(this.updatedTime.equals((Object)null)) {
            this.updatedTime = new Date();
        }

        return this.updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public void setDeviceDetailsMap(Map<String, String> deviceDetailsMap) {
        this.deviceDetailsMap = deviceDetailsMap;
    }

    public Map<String, String> getDeviceDetailsMap() {
        return this.deviceDetailsMap;
    }
}
