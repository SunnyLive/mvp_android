package com.jxai.module.camera.ui

import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.gson.Gson
import com.jxai.lib.common.constant.RouterURL
import com.jxai.lib.common.ui.CommonActivity
import com.jxai.lib.network.mqtt.Config
import com.jxai.lib.network.mqtt.MqttClient
import com.jxai.lib.picture.zip.PictureUtil
import com.jxai.lib.picture.zip.luban.Luban
import com.jxai.lib.picture.zip.luban.OnCompressListener
import com.jxai.lib.utils.encry.Base64Util
import com.jxai.module.camera.R
import com.jxai.module.camera.mqtt.ctl.MqttPubCtl
import com.jxai.module.camera.mvp.CameraPresenter
import com.jxai.module.camera.mvp.ICameraView
import com.jxai.module.camera.mvp.dto.PictureVerify
import com.orhanobut.logger.Logger
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject
import java.io.File

@Route(path = RouterURL.CAMERA_ACTIVITY, name = "CameraActivity")
class CameraActivity : CommonActivity<ICameraView, CameraPresenter>(), ICameraView {


    private val mFragment by lazy { CameraFragment.newInstance() }
    lateinit var mMqttClient: MqttClient
    lateinit var mImageToBase64: String
    override fun getLayout(): Int {
        return R.layout.activity_camera
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val actionBar = supportActionBar
        actionBar!!.hide() // 去掉标题栏

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        ) // 设置全屏

        mFragment.setPictureEnCodeListener { sourceFile ->

            updateMqttMsg()

            Luban.with(this@CameraActivity).load(sourceFile)
                .ignoreBy(50).setCompressListener(object :OnCompressListener{
                    override fun onStart() {

                    }
                    override fun onSuccess(file: File?) {
                        file?.apply {
                            mImageToBase64 = Base64Util.imageToBase64(file.path)
                            p?.requestServerVerify(mImageToBase64)
                            //这里删除图片
                            if (exists()) {
                                sourceFile.delete()
                                delete()
                            }
                        }

                    }

                    override fun onError(e: Throwable?) {

                    }

                })
                .launch()
        }


        savedInstanceState ?: supportFragmentManager.beginTransaction()
            .replace(R.id.container, mFragment)
            .commit()

        /**
         *
         * 这里是MQTT通讯回调相关的
         *
         * Mqtt已经初始化好了 这里是回调监听
         *
         */

        mMqttClient = MqttClient.builder()
            .setContext(this).build()
        mMqttClient.setCallback(object : MqttClient.Callback(mMqttClient) {
            override fun onMessageArrived(topic: String, message: MqttMessage) {

                Logger.d("onMessageArrived  >>>>>> topic = $topic  message = $message")


            }
        }).connection()
        Config.initAiLabels(applicationContext)
    }

    private val pubCtl: MqttPubCtl = MqttPubCtl()

    fun updateMqttMsg() {

        if (mMqttClient.isMqttConnected) {

            //todo 这里应该是要上传坐标信息的
            val lon = 0
            val lat = 0
            mMqttClient.sendMessage(
                Config.genHealthTopic(),
                pubCtl.genHealthData(this, "", "$lon,$lat", 1)
            )
            mMqttClient.sendMessage(Config.TOPIC_REGISTRY, pubCtl.genRegistryData())
        }

    }


    override fun initPresenter(): CameraPresenter {
        return CameraPresenter()
    }

    override fun initView() {

    }

    override fun initEvent() {

    }

    /**
     *
     * AI 处理完成后回调用到这个
     *
     */
    override fun upLoadImageInfo(imageInfo: PictureVerify) {
        val img = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            .toString() + "/jx_camera/result.jpeg"

        if (!Base64Util.base64ToFile(mImageToBase64,img)) return

        mImageToBase64 = Base64Util.imageToBase64(img, Base64.NO_PADDING or Base64.NO_WRAP)
        imageInfo.result?.apply {
            if (isNotEmpty()) {
                val next = iterator().next()
                if ("[]" != next.value) {
                    val aiResult = next.value.toString()
                    val alertData = pubCtl.genAlertData("a", aiResult, mImageToBase64, "")
                    mMqttClient.sendMessage(Config.genAlertTopic(), alertData)
                }
            }

        }

    }

    override fun onTakePicture() {
        mFragment.takePicture()
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun netError() {

    }


}