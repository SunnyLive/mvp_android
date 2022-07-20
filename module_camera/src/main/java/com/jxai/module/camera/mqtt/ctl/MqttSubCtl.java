package com.jxai.module.camera.mqtt.ctl;


import static com.jxai.lib.network.mqtt.Config.SETTING_KEY;

import com.google.gson.Gson;
import com.jxai.lib.network.mqtt.mqttModel.FetchResultEntity;
import com.jxai.lib.network.mqtt.mqttModel.SettingEntity;

public class MqttSubCtl {

    /**
     * pangu -> 获取告警图片
     *
     * @param payload
     * @return
     */
    public FetchResultEntity parseFetchImageObj(String payload) {
        Gson gson = new Gson();
        return gson.fromJson(payload, FetchResultEntity.class);
    }

    /**
     * 保存下发的设备配置信息
     *
     * @param payload
     */
    public void saveSettingData(String payload) {
        Gson gson = new Gson();

        SettingEntity entity = gson.fromJson(payload, SettingEntity.class);
        String setting = gson.toJson(entity);
        //todo 暂时屏蔽掉
        //SharePreference.INSTANCE.save(SETTING_KEY, setting);
    }

    /**
     * 拉取最新的图片列表
     */
    public void notifyToPullImageList() {

    }

}
