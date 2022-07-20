package com.jxai.module.camera.mqtt.mqttModel;

public class BatteryEntity {

    private int health;
    // 温度
    private int temp;
    // 充电状态
    private boolean chargestatus;
    // 剩余电量
    private int remaincapacity;
    // 当前电压
    private int voltage;
    // 充电电压
    private int chargevoltage;
    // 当前电流
    private int current;
    // 充电电流
    private int chargecurrent;


    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public boolean isChargestatus() {
        return chargestatus;
    }

    public void setChargestatus(boolean chargestatus) {
        this.chargestatus = chargestatus;
    }

    public int getRemaincapacity() {
        return remaincapacity;
    }

    public void setRemaincapacity(int remaincapacity) {
        this.remaincapacity = remaincapacity;
    }

    public int getVoltage() {
        return voltage;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public int getChargevoltage() {
        return chargevoltage;
    }

    public void setChargevoltage(int chargevoltage) {
        this.chargevoltage = chargevoltage;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getChargecurrent() {
        return chargecurrent;
    }

    public void setChargecurrent(int chargecurrent) {
        this.chargecurrent = chargecurrent;
    }
}
