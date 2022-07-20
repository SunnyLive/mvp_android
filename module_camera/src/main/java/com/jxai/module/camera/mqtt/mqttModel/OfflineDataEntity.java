package com.jxai.module.camera.mqtt.mqttModel;

import java.util.ArrayList;
import java.util.List;

public class OfflineDataEntity {

    private List<CacheDataEntity> dataList = new ArrayList<>();

    public List<CacheDataEntity> getDataList() {
        return dataList;
    }

    public void setDataList(List<CacheDataEntity> dataList) {
        this.dataList = dataList;
    }

    public static class CacheDataEntity {

        private long timestamp;
        private String dataPath;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getDataPath() {
            return dataPath;
        }

        public void setDataPath(String dataPath) {
            this.dataPath = dataPath;
        }
    }
}
