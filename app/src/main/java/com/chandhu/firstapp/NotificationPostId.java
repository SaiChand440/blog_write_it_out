package com.chandhu.firstapp;

import com.google.firebase.firestore.Exclude;

public class NotificationPostId {

    @Exclude
    public String NotificationPostId;

    public <T extends NotificationPostId> T withId(final String id){
        this.NotificationPostId = id;
        return (T) this;
    }
}
