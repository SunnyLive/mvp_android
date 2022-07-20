package com.jxai.lib.network.mqtt.mqttModel;

import java.util.ArrayList;

public class AlertEntity {
    private String u; // uuid
    private String m; // method
    private String h; // host_ip
    private long t; // timestamp
    private String id; // imageId
    private String r;  // ai result [][]Object
    //private String r;  // ai result [][]Object
    private String img_str; //
    private String video_url;

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getU() {
        return u;
    }

    public void setU(String u) {
        this.u = u;
    }

    public String getM() {
        return m;
    }

    public void setM(String m) {
        this.m = m;
    }

    public String getH() {
        return h;
    }

    public void setH(String h) {
        this.h = h;
    }

    public long getT() {
        return t;
    }

    public void setT(long t) {
        this.t = t;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setR(String r) {
        this.r = r;
    }

    public String getImg_str() {
        return img_str;
    }

    public void setImg_str(String img_str) {
        this.img_str = img_str;
    }
}
