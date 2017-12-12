package org.wso2.carbon.mdm.services.android.bean.wrapper;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Properties;

@ApiModel(
        value = "Application",
        description = "This class carries all information related application"
)
public class AndroidApplication implements Serializable{
    private static final long serialVersionUID = 1998101711L;
    @ApiModelProperty(
            name = "id",
            value = "The ID given to the application when it is stored in the EMM database",
            required = true
    )
    private int id;
    @ApiModelProperty(
            name = "platform",
            value = "The mobile device platform. It can be android, ios or windows",
            required = true
    )
    private String platform;
    @ApiModelProperty(
            name = "category",
            value = "The application category",
            required = true
    )
    private String category;
    @ApiModelProperty(
            name = "name",
            value = "The application\'s name",
            required = true
    )
    private String name;
    private String locationUrl;
    @ApiModelProperty(
            name = "imageUrl",
            value = "The icon url of the application",
            required = true
    )
    private String imageUrl;
    @ApiModelProperty(
            name = "version",
            value = "The application\'s version",
            required = true
    )
    private String version;
    @ApiModelProperty(
            name = "type",
            value = "The application type",
            required = true
    )
    private String type;
    @ApiModelProperty(
            name = "appProperties",
            value = "The properties of the application",
            required = true
    )
    private Properties appProperties;
    @ApiModelProperty(
            name = "applicationIdentifier",
            value = "The application identifier",
            required = true
    )
    private String applicationIdentifier;
    @ApiModelProperty(
            name = "memoryUsage",
            value = "Amount of memory used by the application",
            required = true
    )
    private int memoryUsage;
    @ApiModelProperty(
            name = "isActive",
            value = "Is the application actively running",
            required = true
    )
    private boolean isActive;

    public AndroidApplication() {
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocationUrl() {
        return this.locationUrl;
    }

    public void setLocationUrl(String locationUrl) {
        this.locationUrl = locationUrl;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPlatform() {
        return this.platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getApplicationIdentifier() {
        return this.applicationIdentifier;
    }

    public void setApplicationIdentifier(String applicationIdentifier) {
        this.applicationIdentifier = applicationIdentifier;
    }

    public int getMemoryUsage() {
        return this.memoryUsage;
    }

    public void setMemoryUsage(int memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(o != null && this.getClass() == o.getClass()) {
            AndroidApplication that = (AndroidApplication)o;
            if(this.applicationIdentifier != null) {
                if(!this.applicationIdentifier.equals(that.applicationIdentifier)) {
                    return false;
                }
            } else if(that.applicationIdentifier != null) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.id;
        result = 31 * result + (this.applicationIdentifier != null?this.applicationIdentifier.hashCode():0);
        return result;
    }

    public Properties getAppProperties() {
        return this.appProperties;
    }

    public void setAppProperties(Properties appProperties) {
        this.appProperties = appProperties;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }
}
