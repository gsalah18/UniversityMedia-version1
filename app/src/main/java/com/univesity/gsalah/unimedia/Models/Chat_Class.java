package com.univesity.gsalah.unimedia.Models;

import java.util.Date;

/**
 * Created by GSALAH on 12/23/2017.
 */

public class Chat_Class {
    private String message;
    private String sender_username;
    private String sender_id;
    private long date;

    public Chat_Class(String message, String sender_username, String sender_id) {
        this.message = message;
        this.sender_username = sender_username;
        this.sender_id = sender_id;
        date=new Date().getTime();
    }

    public Chat_Class(String message, String sender_username, String sender_id, long date) {
        this.message = message;
        this.sender_username = sender_username;
        this.sender_id = sender_id;
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public String getSender_username() {
        return sender_username;
    }

    public boolean isUser(String id){
        return sender_id.equals(id);
    }

    public String getSender_id() {
        return sender_id;
    }

    public long getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Chat_Class{" +
                "message='" + message + '\'' +
                ", sender_username='" + sender_username + '\'' +
                '}';
    }
}
