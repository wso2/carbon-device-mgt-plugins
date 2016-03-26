package org.wso2.carbon.device.mgt.iot.digitaldisplay.controller.api.model;

public class ScreenShotModel {

    private String[] screenShotData;
    private int length;

    public ScreenShotModel(){

    }

    public ScreenShotModel(String[] screenShotData , int length){
        this.screenShotData = screenShotData;
        this.length = length;
    }

    public void setScreenShotData(String[] screrenShotData){
        this.screenShotData = screenShotData;
    }

    public void setLength(int length){
        this.length = length;
    }

    public String[] getScreenShotData(){
        return this.screenShotData;
    }

    public int getLength(){
        return this.length;
    }

}
