package com.tutocode.tutotchat.Entities;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.Map;

public class Message {

    private String uid;
    private String username;
    private String userId;
    private String content;
    private String imageUrl;
    private Long date;

    public Message(){}

    public Message(String username, String userId, String content, String imageUrl) {
        this.username = username;
        this.userId = userId;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Map<String, String> getDate(){
        return ServerValue.TIMESTAMP;
    }

    @Exclude
    public Long getLongDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }
}
