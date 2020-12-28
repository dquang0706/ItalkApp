package com.example.italkapp.model;

public class ModelNotification {
    String pId,timestamp,myId, typeNotification,hisId,nName,nImage;

    public ModelNotification() {
    }

    public ModelNotification(String pId, String timestamp, String myId, String typeNotification, String hisId, String nName, String nImage) {
        this.pId = pId;
        this.timestamp = timestamp;
        this.myId = myId;
        this.typeNotification = typeNotification;
        this.hisId = hisId;
        this.nName = nName;
        this.nImage = nImage;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMyId() {
        return myId;
    }

    public void setMyId(String myId) {
        this.myId = myId;
    }

    public String getTypeNotification() {
        return typeNotification;
    }

    public void setTypeNotification(String typeNotification) {
        this.typeNotification = typeNotification;
    }

    public String getHisId() {
        return hisId;
    }

    public void setHisId(String hisId) {
        this.hisId = hisId;
    }

    public String getnName() {
        return nName;
    }

    public void setnName(String nName) {
        this.nName = nName;
    }

    public String getnImage() {
        return nImage;
    }

    public void setnImage(String nImage) {
        this.nImage = nImage;
    }
}
