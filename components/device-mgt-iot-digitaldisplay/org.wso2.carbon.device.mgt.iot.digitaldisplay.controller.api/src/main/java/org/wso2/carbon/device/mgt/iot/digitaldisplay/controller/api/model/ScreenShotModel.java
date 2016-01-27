package org.wso2.carbon.device.mgt.iot.digitaldisplay.controller.api.model;

public class ScreenShotModel {

    private String[] scrrenShotData;
    private int length;

    public ScreenShotModel(){

    }

    public ScreenShotModel(String[] scrrenShotData , int length){
        this.scrrenShotData = scrrenShotData;
        this.length = length;
    }

    public void setScrrenShotData(String[] scrrenShotData){
        this.scrrenShotData = scrrenShotData;
    }

    public void setLength(int length){
        this.length = length;
    }

    public String[] getScrrenShotData(){
        return this.scrrenShotData;
    }

    public int getLength(){
        return this.length;
    }

}
