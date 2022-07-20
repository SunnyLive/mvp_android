package com.jxai.module.camera.mqtt.mqttModel;

import com.google.gson.JsonArray;

public class SensorDataEntity {
    private String u; // uuid
    private String m; // method
    private String h; // host_ip
    private long t; // timestamp
    private String id; // imageId
    private JsonArray r;  // ai result [][]Object
    private String img_str; //

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

    public JsonArray getR() {
        return r;
    }

    public void setR(JsonArray r) {
        this.r = r;
    }

    public String getImg_str() {
        return img_str;
    }

    public void setImg_str(String img_str) {
        this.img_str = img_str;
    }
}
