package com.jxai.lib.network.mqtt.mqttModel;

public class SettingEntity implements Cloneable {

    private String msg_id;
    private String device_id;
    private Integer interval;
    private Period compute_period;
    private Long time;
    private String type;
    private StoreConfigInfo alert;
    private StoreConfigInfo history;
    private UploadInfo upload;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public StoreConfigInfo getAlert() {
        return alert;
    }

    public void setAlert(StoreConfigInfo alert) {
        this.alert = alert;
    }

    public StoreConfigInfo getHistory() {
        return history;
    }

    public void setHistory(StoreConfigInfo history) {
        this.history = history;
    }

    public UploadInfo getUpload() {
        return upload;
    }

    public void setUpload(UploadInfo upload) {
        this.upload = upload;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public Period getCompute_period() {
        return compute_period;
    }

    public void setCompute_period(Period compute_period) {
        this.compute_period = compute_period;
    }

    @Override
    public Object clone() throws CloneNotSupportedException  {
        try {
            SettingEntity res = (SettingEntity) super.clone();
            res.compute_period = (Period) this.compute_period.clone();
            res.alert = (StoreConfigInfo) this.alert.clone();
            res.history = (StoreConfigInfo) this.history.clone();
            res.upload = (UploadInfo) this.upload.clone();
            res.msg_id = this.msg_id;
            res.device_id = this.device_id;
            res.interval = this.interval;
            res.time = this.time;
            res.type = this.type;

            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public class Period implements Cloneable {
        private int starttime;
        private int endtime;

        public int getStarttime() {
            return starttime;
        }

        public void setStarttime(int starttime) {
            this.starttime = starttime;
        }

        public int getEndtime() {
            return endtime;
        }

        public void setEndtime(int endtime) {
            this.endtime = endtime;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException  {
            try {
                Period res = (Period) super.clone();
                res.endtime = this.endtime;
                res.starttime = this.starttime;
                return res;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class StoreConfigInfo implements Cloneable {
        private Boolean enable;
        private Integer sto_cap;
        private Integer sto_days;
        private Integer sto_freq;
        private Integer storage;
        private String upload;

        public Boolean getEnable() {
            return enable;
        }

        public void setEnable(Boolean enable) {
            this.enable = enable;
        }

        public Integer getSto_cap() {
            return sto_cap;
        }

        public void setSto_cap(Integer sto_cap) {
            this.sto_cap = sto_cap;
        }

        public Integer getSto_days() {
            return sto_days;
        }

        public void setSto_days(Integer sto_days) {
            this.sto_days = sto_days;
        }

        public Integer getSto_freq() {
            return sto_freq;
        }

        public void setSto_freq(Integer sto_freq) {
            this.sto_freq = sto_freq;
        }

        public Integer getStorage() {
            return storage;
        }

        public void setStorage(Integer storage) {
            this.storage = storage;
        }

        public String getUpload() {
            return upload;
        }

        public void setUpload(String upload) {
            this.upload = upload;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            try {
                StoreConfigInfo res = (StoreConfigInfo) super.clone();
                res.enable = this.enable;
                res.sto_cap = this.sto_cap;
                res.sto_days = this.sto_days;
                res.sto_freq = this.sto_freq;
                res.storage = this.storage;
                res.upload = this.upload;

                return res;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class UploadInfo implements Cloneable {
        private Boolean history_enable;
        private Boolean alert_enable;
        private Integer img_freq;
        private String img_type;
        private String alert_img_type;

        public Boolean getHistory_enable() {
            return history_enable;
        }

        public void setHistory_enable(Boolean history_enable) {
            this.history_enable = history_enable;
        }

        public Boolean getAlert_enable() {
            return alert_enable;
        }

        public void setAlert_enable(Boolean alert_enable) {
            this.alert_enable = alert_enable;
        }

        public Integer getImg_freq() {
            return img_freq;
        }

        public void setImg_freq(Integer img_freq) {
            this.img_freq = img_freq;
        }

        public String getImg_type() {
            return img_type;
        }

        public void setImg_type(String img_type) {
            this.img_type = img_type;
        }

        public String getAlert_img_type() {
            return alert_img_type;
        }

        public void setAlert_img_type(String alert_img_type) {
            this.alert_img_type = alert_img_type;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException  {
            try {
                UploadInfo res= (UploadInfo) super.clone();
                res.history_enable = this.history_enable;
                res.alert_enable = this.alert_enable;
                res.img_freq = this.img_freq;
                res.img_type = this.img_type;
                res.alert_img_type = this.alert_img_type;

                return res;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
