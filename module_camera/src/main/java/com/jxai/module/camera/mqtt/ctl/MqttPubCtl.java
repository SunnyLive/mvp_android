package com.jxai.module.camera.mqtt.ctl;


import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.jxai.lib.network.mqtt.Config;
import com.jxai.lib.network.mqtt.Utils;
import com.jxai.lib.network.mqtt.mqttModel.AlertEntity;
import com.jxai.lib.network.mqtt.mqttModel.BatteryEntity;
import com.jxai.lib.network.mqtt.mqttModel.DataUploadEntity;
import com.jxai.lib.network.mqtt.mqttModel.FetchResultEntity;
import com.jxai.lib.network.mqtt.mqttModel.HeartbeatEntity;
import com.jxai.lib.network.mqtt.mqttModel.ImageUploadEntity;
import com.jxai.lib.network.mqtt.mqttModel.RegistryEntity;
import com.jxai.lib.network.mqtt.mqttModel.SensorDataEntity;
import com.jxai.lib.network.mqtt.mqttModel.SettingEntity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MqttPubCtl {

    public String genHealthData(Context context, String rfId, String coordinate, Integer cameraNum) {
        HeartbeatEntity entity = new HeartbeatEntity();
        entity.setCpu(Utils.getTotalCpuRate());
        entity.setDisk(Utils.getDiskRate());
        entity.setEth0(Utils.getIPAddress(context));
        entity.setMac(Utils.getDeviceId());
        entity.setMem(Utils.getUsedPercentValue(context));
        entity.setDevice_status("normal");
        entity.setRfid(TextUtils.isEmpty(rfId) ? "-" : rfId);
        entity.setApi("v2");
        entity.setCoordinate(coordinate);
        entity.setCamera_number(cameraNum);

        Gson gson = new Gson();
        return gson.toJson(entity);
    }

    public SettingEntity genSettingObject() {
        //todo 临时赋值，为了阉割 SharePreference
        String value = "";//SharePreference.INSTANCE.getValue(SETTING_KEY);
        Gson gson = new Gson();
        return gson.fromJson(value, SettingEntity.class);
    }

    /**
     * 上传设备设置信息
     */
    public String genSettingData() {
        //todo 临时赋值，为了阉割 SharePreference
        return "";//SharePreference.INSTANCE.getValue(SETTING_KEY);
    }

    /**
     * 上传历史图片
     */
    public String genImageUploadData(String imageStr) {
        ImageUploadEntity entity = new ImageUploadEntity();
        entity.setCreate_time(System.currentTimeMillis());
        //todo 临时赋值，为了阉割 SharePreference
        String cameraId = "0";//SharePreference.INSTANCE.getValue("cameraId");
        if (TextUtils.isEmpty(cameraId)) {
            cameraId = Utils.getDeviceId();
        }
        entity.setDevice_id(cameraId);
        entity.setImg_str(imageStr);
        Gson gson = new Gson();
        return gson.toJson(entity);
    }

    /**
     * 注册 ai labels
     */
    public String genRegistryData() {
        RegistryEntity entity = new RegistryEntity();
        String labels = Config.loadAiLabels();
        entity.setKey("");
        entity.setSupport(labels);
        entity.setUuid(Utils.getDeviceId());
        entity.setVersion("jiegouhua_v1.2");
        Gson gson = new Gson();
        return gson.toJson(entity);
    }

    /**
     * 返回告警图片
     */
    public String genFetchImageData(String as, String path) {
        FetchResultEntity entity = new FetchResultEntity();
        entity.setAs(as);
        entity.setU(Utils.getDeviceId());

        // todo
        Gson gson = new Gson();
        return gson.toJson(entity);
    }

    /**
     * 手动拍照
     */
    public String genSnapImageData() {
        FetchResultEntity entity = new FetchResultEntity();
        Gson gson = new Gson();
        return gson.toJson(entity);
    }

    /**
     * 告警信息 TODO
     * <p>
     * method : a / m
     */
    public String genAlertData(String method, String aiResult, String imageStr, String videoUrl) {
        AlertEntity entity = new AlertEntity();
        entity.setT(System.currentTimeMillis());
        //todo 临时使用，为了阉割 SharePreference
        String cameraId = Utils.getDeviceId();//SharePreference.INSTANCE.getValue("cameraId");
        if (TextUtils.isEmpty(cameraId)) {
            cameraId = Utils.getDeviceId();
        }
        entity.setU(cameraId);
        if ("m".equals(method)) {
            method = "m:" + cameraId;
            entity.setId(imageStr);
        } else {
            entity.setImg_str(imageStr);
            entity.setId(UUID.randomUUID().toString());
        }
        entity.setM(method);
        entity.setH(Utils.getDeviceId());
        Gson gsonAiResult = new Gson();

        Type type = new TypeToken<ArrayList<ArrayList<String>>>() {
        }.getType();
        //entity.setR(gsonAiResult.fromJson(aiResult,type));
        entity.setR(aiResult);
        entity.setVideo_url(videoUrl);
        Gson gson = new Gson();
        return gson.toJson(entity);
    }

    public String genPanguData(JsonArray aiResult, String device_id) {
        SensorDataEntity entity = new SensorDataEntity();
        entity.setT(System.currentTimeMillis());
        entity.setU(device_id);
        entity.setImg_str("");
        entity.setM("a");
        entity.setH(Utils.getDeviceId());
        entity.setImg_str("");
        entity.setR(aiResult);
        Gson gson = new Gson();
        return gson.toJson(entity);
    }

    public String genEdgeData(String device_id, float value) {
        DataUploadEntity entity = new DataUploadEntity();
        entity.setCreate_time(System.currentTimeMillis());
        entity.setDevice_id(device_id);
        Map<String, Object> v = new HashMap<>();
        v.put("value", value);
        DataUploadEntity.Data data = new DataUploadEntity.Data();
        data.setFields(v);
        DataUploadEntity.Data[] datas = new DataUploadEntity.Data[]{data};
        entity.setData(datas);
        Gson gson = new Gson();
        return gson.toJson(entity);
    }

    public String genEdgeBatteryData(BatteryEntity battery, String imageStr) {
        DataUploadEntity entity = new DataUploadEntity();
        entity.setCreate_time(System.currentTimeMillis());
        //todo 临时赋值，为了阉割 SharePreference
        String cameraId = "1";//SharePreference.INSTANCE.getValue("cameraId");
        if (TextUtils.isEmpty(cameraId)) {
            cameraId = Utils.getDeviceId();
        }
        entity.setDevice_id(cameraId);
        Map<String, Object> v = new HashMap<>();
        v.put("health", battery.getHealth());
        v.put("temp", battery.getTemp());
        v.put("remaincapacity", battery.getRemaincapacity());
        v.put("chargestatus", battery.isChargestatus());
        v.put("voltage", battery.getVoltage());
        v.put("chargevoltage", battery.getChargevoltage());
        v.put("current", battery.getCurrent());
        v.put("chargecurrent", battery.getChargecurrent());

        DataUploadEntity.Data data = new DataUploadEntity.Data();
        data.setFields(v);
        DataUploadEntity.Data[] datas = new DataUploadEntity.Data[]{data};
        entity.setData(datas);
        entity.setImg_str(imageStr);
        Gson gson = new Gson();
        return gson.toJson(entity);
    }


    /**
     * 设备事件 todo
     */
    public String genEventData() {
        return "";
    }

}
