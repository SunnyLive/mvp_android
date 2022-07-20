package com.jxai.lib.network.mqtt;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Config {
    private static boolean HasSimCard = true;
    private static final String AI_LABELS_NAME = "aiLabels.json";

    public static String MINIO_HOST_URL= "http://119.29.184.128/file-proxy/api/v1/fs/file";
    public static final String SETTING_KEY = "setting";
    public static String MQTT_HOST = "tcp://119.29.184.128:1883";
    public static String USERNAME = "admin";
    public static String PASSWORD = "test";

    // for pangu
    // 定时通知端侧上传labels
    public static final String TOPIC_KICK = "b/w2s/kick";
    // 上传labels
    public static final String TOPIC_REGISTRY = "pangu/register";
    // 上传告警信息 pangu/alert/{uuid}
    public static final String TOPIC_ALERT = "pangu/alert/";
    // 上传传感器数据 pangu/data/{uuid}
    public static final String TOPIC_DATA = "pangu/data/";

    // 上传告警图片 pangu/image/{uuid
    public static final String TOPIC_IMAGE = "pangu/image/";
    // 手动拍照
    public static final String TOPIC_SNAP = "/snap";
    // 获取告警图片
    public static final String TOPIC_FETCH = "/fetch/";
    // 添加传感器
    public static final String TOPIC_ADD_SENSOR = "/device/add";

    // for asp
    // 设置配置信息 {uuid}/setting

    // 设置配置信息response edge/{uuid}/setting
    public static final String TOPIC_SETTING = "/setting";
    // 上传历史图片 edge/{uuid}/data
    public static final String TOPIC_INTERNAL_DATA = "/data";
    // 上传设备事件  f = 1m  edge/{uuid}/event
    public static final String TOPIC_EVENT = "/event";
    // 心跳 edge/{uuid}/health
    public static final String TOPIC_HEALTH = "/health";

    // resp url
//    public static final String RTSP_URL = "rtsp://admin:ADMIN123456@192.168.208.209:554/ch01.264";
    public static final String RTSP_URL = "rtsp://admin:ADMIN123456@10.50.0.151:554/ch01.264";
    // face image save path
//    public static final String FACE_IMAGE_SAVE_PATH = "/storage/self/primary/recognition/faceDetect/";
    public static final String FACE_IMAGE_SAVE_PATH = Environment.getExternalStorageDirectory() + "/faceDetect/";

    // asp-hrm 接口地址
    public static final String NET_ASP_HRM_HOST = "http://10.51.4.2";

    public static final String NET_UPLOAD_FILE = NET_ASP_HRM_HOST + "/hrm/api/v1/human/sync";

    private static String aiLabels;
    private static Context mContext;


    public static String[] sensorIds = new String[]{"71b892","71ad26","71a826","71ab7f","718bdf"};
    public static String[] metricShortName = new String[] {"X","Y","Z","S","D","P","U","H","T"};


    public static String initAiLabels(Context context) {
        mContext = context;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(assetManager.open(AI_LABELS_NAME)));
            String line;
            while ((line = bf.readLine()) != null) {
                line = Utils.replaceCNSpace(line);
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        aiLabels = stringBuilder.toString();
        return aiLabels;
    }

    public static String loadAiLabels() {
        if (TextUtils.isEmpty(aiLabels)) {
            return initAiLabels(mContext);
        }
        return aiLabels;
    }

    public static String genAlertTopic() {
        return TOPIC_ALERT + Utils.getDeviceId();
    }

    public static String genDataTopic() {
        return TOPIC_DATA + Utils.getDeviceId();
    }

    public static String genAlertImageTopic() {
        return TOPIC_IMAGE + Utils.getDeviceId();
    }

    public static String genSnapTopic() {
        return Utils.getDeviceId() + TOPIC_SNAP;
    }

    public static String genSettingTopic() {
        return Utils.getDeviceId() + TOPIC_SETTING;
    }

    public static String genFetchTopic() {
        return Utils.getDeviceId() + TOPIC_FETCH + Utils.getDeviceId();
    }

    public static String genSettingRespTopic() {
        return "edge/" + Utils.getDeviceId() + TOPIC_SETTING;
    }

    public static String genHistoryDataTopic() {
        return "edge/" + Utils.getDeviceId() + TOPIC_INTERNAL_DATA;
    }

    public static String genEventTopic() {
        return "edge/" + Utils.getDeviceId() + TOPIC_EVENT;
    }

    public static String genHealthTopic() {
        return "edge/" + Utils.getDeviceId() + TOPIC_HEALTH;
    }

    public static String genAddSensorTopic() {
        return Utils.getDeviceId() + TOPIC_ADD_SENSOR;
    }
    public static String genResponseSensorTopic() {
        return "edge/" +Utils.getDeviceId() + TOPIC_ADD_SENSOR;
    }

    public static boolean isHasSimCard() {
        return HasSimCard;
    }

    public static void setHasSimCard(boolean hasSimCard) {
        HasSimCard = hasSimCard;
    }
}
