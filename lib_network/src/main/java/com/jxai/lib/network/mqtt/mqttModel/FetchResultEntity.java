package com.jxai.lib.network.mqtt.mqttModel;

public class FetchResultEntity {
    private String id;
    private String u; // uuid
    private String as; // alert result
    private String d;  // data

    public String getU() {
        return u;
    }

    public void setU(String u) {
        this.u = u;
    }

    public String getAs() {
        return as;
    }

    public void setAs(String as) {
        this.as = as;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
