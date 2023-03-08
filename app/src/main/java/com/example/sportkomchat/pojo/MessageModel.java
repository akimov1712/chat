package com.example.sportkomchat.pojo;

public class MessageModel {

    private String userName;
    private String message;
    private long date;

    public MessageModel(String userName, String message, long date) {
        this.userName = userName;
        this.message = message;
        this.date = date;
    }

    public MessageModel() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

}
