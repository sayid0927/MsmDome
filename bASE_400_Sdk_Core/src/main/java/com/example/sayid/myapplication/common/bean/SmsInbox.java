package com.example.sayid.myapplication.common.bean;

public class SmsInbox {
    public final static int OP_READ_SMS = 14;
    public final static int OP_WRITE_SMS = 15;
    public final static int OP_RECEIVE_SMS = 16;
    public final static int OP_SEND_SMS = 20;

    public final static String CONTENT_SMS = "content://sms";
    public final static String CONTENT_SMS_SENT = "content://sms/sent";
    public final static String CONTENT_SMS_RECEIVE = "content://sms/inbox";

    private int id;
    private long threadId;
    private int type;
    private int read;
    private String date;
    private String body;
    private String center;
    private String address;

    public int getId() {
        return this.id;
    }

    public void setId(int paramInt) {
        this.id = paramInt;
    }

    public long getThreadId() {
        return this.threadId;
    }

    public void setThreadId(long paramLong) {
        this.threadId = paramLong;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int paramInt) {
        this.type = paramInt;
    }

    public int getRead() {
        return this.read;
    }

    public void setRead(int paramInt) {
        this.read = paramInt;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String paramString) {
        this.date = paramString;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String paramString) {
        this.body = paramString;
    }

    public String getCenter() {
        return this.center;
    }

    public void setCenter(String paramString) {
        this.center = paramString;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String paramString) {
        this.address = paramString;
    }
}