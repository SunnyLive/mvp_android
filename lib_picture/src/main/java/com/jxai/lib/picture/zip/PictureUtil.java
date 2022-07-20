package com.jxai.lib.picture.zip;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * 这是一个压缩图片的util
 */
public class PictureUtil {


    private static final String TAG = PictureUtil.class.getName();


    /**
     * 使用哈夫曼压缩图片
     * @param context
     * @param sourceFile
     * @param fileDir
     */
    public static void imageCompress(Context context, String sourceFile, String fileDir) {

        File file = new File(sourceFile);
        if (!file.exists()) {
            Log.e(TAG,"文件不存在");
            return;
        }
       /* ImageCompress
                .with(context.getApplicationContext())
                .load(sourceFile)//需要压缩图片的路径
                .ignoreBy(100)//压缩100K以上的图片
                .fileName(file.getName())//压缩文件名
                .setTargetDir(fileDir)//保存压缩图片的位置
                .setOnCompressListener(new ImageCompress.OnCompressListener() {
                    @Override
                    public void onStart() {
                        Log.i(TAG,"ImageCompress 。。。。。。。。。");
                    }

                    @Override
                    public void onSuccess(String filePath) {
                        //ol.onSuccess(filePath);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.e(TAG,"图片压缩失败。。。");
                    }
                }).launch();*/
    }

}
