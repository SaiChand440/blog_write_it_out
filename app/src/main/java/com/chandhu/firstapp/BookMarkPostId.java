package com.chandhu.firstapp;

import com.google.firebase.firestore.Exclude;

public class BookMarkPostId {

    @Exclude
    public String BookMarkPostId;

    public <T extends BookMarkPostId> T withId(final String id){
        this.BookMarkPostId = id;
        return (T) this;
    }
}
