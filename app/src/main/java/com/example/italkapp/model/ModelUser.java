package com.example.italkapp.model;

import com.google.firebase.database.PropertyName;

public class ModelUser {
    String name,email,image,cover,uid,onlineStatus,typingTo; //adding two more fildes
     boolean isBlock=false;
    public ModelUser() {
    }

    public ModelUser(String name, String email,  String phone, String image, String cover, String uid, String onlineStatus, String typingTo, boolean isBlock) {
        this.name = name;
        this.email = email;
        this.image = image;
        this.cover = cover;
        this.uid = uid;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
        this.isBlock = isBlock;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }
    @PropertyName("isBlock")
    public boolean isBlock() {
        return isBlock;
    }
    @PropertyName("isBlock")
    public void setBlock(boolean block) {
        isBlock = block;
    }
}
