package com.jxai.lib.network.mqtt;


import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.orhanobut.logger.Logger;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.UUID;

public class MqttClient {

    private static final String TAG = MqttClient.class.getName();

    private MqttAndroidClient mMqttAndroidClient;
    private MqttConnectOptions mMqttConnectOptions;
    private String mServerURI;
    private Context mContext;
    private String mUserName;
    private String mPassWord;
    private int reConnectNumber;
    private boolean isMqttConnected;

    public MqttClient(Builder builder) {
        this.mServerURI = builder.mServerURI;
        this.mContext = builder.mContext;
        this.mUserName = builder.mUserName;
        this.mPassWord = builder.mPassWord;
        init();
    }


    private void init() {
        Utils.initDeviceInfo(mContext);
        mMqttAndroidClient = new MqttAndroidClient(mContext, mServerURI, Utils.getDeviceId());
        mMqttConnectOptions = new MqttConnectOptions();
        mMqttConnectOptions.setCleanSession(true); //设置是否清除缓存
        mMqttConnectOptions.setConnectionTimeout(10); //设置超时时间，单位：秒
        mMqttConnectOptions.setKeepAliveInterval(20); //设置心跳包发送间隔，单位：秒
        mMqttConnectOptions.setUserName(mUserName); //设置用户名
        mMqttConnectOptions.setPassword(mPassWord.toCharArray()); //设置密码
        Logger.i("初始化成功  当前设备id = " + Utils.getDeviceId());
    }

    public MqttClient setCallback(Callback mMqttCallback) {
        mMqttAndroidClient.setCallback(mMqttCallback); //设置监听订阅消息的回调
        return this;
    }


    public void destroy() {
        try {
            Logger.i(TAG, "destroy");
            isMqttConnected = false;
            mMqttAndroidClient.unregisterResources();
            mMqttAndroidClient.disconnect(); //断开连接
            mMqttAndroidClient = null;
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    public MqttClient connection() {
        if (mMqttAndroidClient != null && !mMqttAndroidClient.isConnected() && isConnectIsNomarl()) {
            try {
                reConnectNumber++;
                mMqttAndroidClient.connect(mMqttConnectOptions, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        return this;
    }


    public boolean isMqttConnected() {
        return isMqttConnected;
    }

    /**
     * 发布 （模拟其他客户端发布消息）
     *
     * @param message 消息
     */
    public void sendMessage(String topic, String message) {
        int qos = 2;
        try {
            if (TextUtils.isEmpty(message)) {
                return;
            }
            Logger.d("sendMessage----------topic--------->>> "
                    + topic + " \nmessage = " + message);
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            if (mMqttAndroidClient != null) {
                mMqttAndroidClient.publish(topic, message.getBytes(), qos, false);
            }

        } catch (MqttException e) {
            e.printStackTrace();
            Logger.e(e.getMessage());
        }
    }


    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNomarl() {
        if (Utils.isNetWorkAvailable(mContext)) {
            return true;
        } else {
            Log.i(TAG, "没有可用网络");
            /*没有可用网络的时候，延迟5秒再尝试重连，（无限连接）*/
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    reClientConnection();
                }
            }, 5000);
            return false;
        }
    }


    //MQTT是否连接成功的监听
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 ");
            try {
                isMqttConnected = true;
                mMqttAndroidClient.subscribe(Config.genSnapTopic(), 2);//手动拍照
                mMqttAndroidClient.subscribe(Config.genSettingTopic(), 2);//更新配置信息
                mMqttAndroidClient.subscribe(Config.TOPIC_KICK, 2);//上传注册信息
                mMqttAndroidClient.subscribe(Config.genFetchTopic(), 2);//获取告警图片
                mMqttAndroidClient.subscribe(Config.genAddSensorTopic(), 2);//添加传感器
                reConnectNumber = 0;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            // 有网状态下的重连 （主要是broker的连接问题）
            Log.i(TAG, "连接失败 ");
            isMqttConnected = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (reConnectNumber <= 20) {
                        reClientConnection();
                    }
                }
            }, 5000);
        }
    };


    /**
     * 连接MQTT服务器
     */
    private void reClientConnection() {
        if (mMqttAndroidClient != null && !mMqttAndroidClient.isConnected() && isConnectIsNomarl()) {
            try {
                reConnectNumber++;
                mMqttAndroidClient.connect(mMqttConnectOptions, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {

        public String mServerURI = "tcp://119.29.184.128:1883";
        //public String mServerURI = "tcp://172.29.100.41:1883";
        public Context mContext;
        public String mMacAddress = UUID.randomUUID().toString(); //客户端ID，一般以客户端唯一标识符表示，这里用设备序列号表示
        //public String mUserName = "admin";
        public String mUserName = "admin";
        //public String mPassWord = "public";
        public String mPassWord = "test";
        public Callback mMqttCallback;

        public Builder setMacAddress(String mMacAddress){
            this.mMacAddress = mMacAddress;
            return this;
        }

        public Builder setServerURI(String mServerURI) {
            this.mServerURI = mServerURI;
            return this;
        }

        public Builder setContext(Context mContext) {
            this.mContext = mContext;
            return this;
        }

        public Builder setUserName(String mUserName) {
            this.mUserName = mUserName;
            return this;
        }

        public Builder setPassWord(String mPassWord) {
            this.mPassWord = mPassWord;
            return this;
        }

        public Builder setMqttCallback(Callback mMqttCallback) {
            this.mMqttCallback = mMqttCallback;
            return this;
        }

        public MqttClient build() {
            return new MqttClient(this);
        }


    }


    /**
     *
     * 这里是mqtt的回调方法
     *
     * 这里面做了重连机制，
     *
     * 这里只给外部暴露一个成功回调的接口
     *
     */
    public abstract static class Callback implements MqttCallback {

        private MqttClient mMqttClient;

        public Callback(MqttClient mMqttClient) {
            this.mMqttClient = mMqttClient;
        }

        @Override
        public void connectionLost(Throwable cause) {
            Log.i(TAG, "连接断开 ");
            mMqttClient.isMqttConnected = false;
            try {
                mMqttClient.reClientConnection();//连接断开，重连
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {

            try {
                onMessageArrived(topic, message);
            } catch (Exception e) {
                Logger.e(e.getMessage());
            }


        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }

        public abstract void onMessageArrived(String topic, MqttMessage message);

    }
}
