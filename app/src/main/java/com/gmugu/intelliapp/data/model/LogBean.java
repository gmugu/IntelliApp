package com.gmugu.intelliapp.data.model;

import java.util.Date;

/**
 * Created by mugu on 17/5/8.
 */

public class LogBean {
    private int id;
    private String lockMac;
    private String event;
    private long time;
    private String img;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLockMac() {
        return lockMac;
    }

    public void setLockMac(String lockMac) {
        this.lockMac = lockMac;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    @Override
    public String toString() {
        return "LogBean{" +
                "id=" + id +
                ", lockMac='" + lockMac + '\'' +
                ", event='" + event + '\'' +
                ", time=" + new Date(time) +
                ", img='" + img + '\'' +
                '}';
    }
}
