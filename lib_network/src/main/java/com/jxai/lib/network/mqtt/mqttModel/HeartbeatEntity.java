package com.jxai.lib.network.mqtt.mqttModel;

public class HeartbeatEntity {

    private String mac;
    private String tun0;
    private String eth0;
    private Integer port;
    private Float cpu;
    private Float mem;
    private Float disk;
    private String device_status; // normal : abormal
    private String api;
    private String rfid;
    private String coordinate;
    private Integer camera_number;

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getTun0() {
        return tun0;
    }

    public void setTun0(String tun0) {
        this.tun0 = tun0;
    }

    public String getEth0() {
        return eth0;
    }

    public void setEth0(String eth0) {
        this.eth0 = eth0;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Float getCpu() {
        return cpu;
    }

    public void setCpu(Float cpu) {
        this.cpu = cpu;
    }

    public Float getMem() {
        return mem;
    }

    public void setMem(Float mem) {
        this.mem = mem;
    }

    public Float getDisk() {
        return disk;
    }

    public void setDisk(Float disk) {
        this.disk = disk;
    }

    public String getDevice_status() {
        return device_status;
    }

    public void setDevice_status(String device_status) {
        this.device_status = device_status;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }

    public Integer getCamera_number() {
        return camera_number;
    }

    public void setCamera_number(Integer camera_number) {
        this.camera_number = camera_number;
    }
}
