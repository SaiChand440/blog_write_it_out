package com.chandhu.firstapp;

import java.util.Date;

public class BookMarkPost extends BookMarkPostId{
    public String user_id;
    public String image_url;
    public String desc;
    public String thumb_url;
    public Date timeStamp;

    public BookMarkPost () {
        //needed because firebase always requires an empty constructor
    }

    public BookMarkPost (String user_id, String image_url, String desc, String thumb_url,Date timeStamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.desc = desc;
        this.thumb_url = thumb_url;
        this.timeStamp = timeStamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getDesc() {
        return desc;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

}
