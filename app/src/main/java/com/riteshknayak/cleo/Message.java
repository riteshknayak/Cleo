package com.riteshknayak.cleo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName ="messages")
public class Message {


    public static String SENT_BY_ME = "me";
    public static String SENT_BY_BOT="bot";

    @PrimaryKey(autoGenerate = true)
    private int messageNum;

    public void setMessageNum(int messageNum) {
        this.messageNum = messageNum;
    }

    @ColumnInfo(name = "message")
    private String message;

    @ColumnInfo(name = "sentBy")
    private String sentBy;


    //Getters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public int getMessageNum() {
        return messageNum;
    }

    public Message(String message, String sentBy) {
        this.message = message;
        this.sentBy = sentBy;
    }
}