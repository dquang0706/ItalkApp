package com.example.italkapp.model;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;

public class ModelPost implements Serializable {
    String pId,  pDescr,pImage, pTime, uid, uEmail, uAvatar, uName;

    public ModelPost() {
    }

    public ModelPost(String pId, String pDescr,  String pImage, String pTime, String uid, String uEmail, String uAvatar, String uName) {
        this.pId = pId;
        this.pDescr = pDescr;

        this.pImage = pImage;
        this.pTime = pTime;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uAvatar = uAvatar;
        this.uName = uName;
    }

    @PropertyName("pId")
    public String getpId() {
        return pId;
    }
    @PropertyName("pId")
    public void setpId(String pId) {
        this.pId = pId;
    }


    public String getpDescr() {
        return pDescr;
    }

    public void setpDescr(String pDescr) {
        this.pDescr = pDescr;
    }

    public String getpImage() {
        return pImage;
    }

    public void setpImage(String pImage) {
        this.pImage = pImage;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    @PropertyName("uid")
    public String getUid() {
        return uid;
    }
    @PropertyName("uid")
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuAvatar() {
        return uAvatar;
    }

    public void setuAvatar(String uAvatar) {
        this.uAvatar = uAvatar;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }



}
