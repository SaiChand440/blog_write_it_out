package com.chandhu.firstapp;

import java.util.Date;

public class NotificationPost extends NotificationPostId {
    public Date timeStamp;

    public NotificationPost(){

    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
