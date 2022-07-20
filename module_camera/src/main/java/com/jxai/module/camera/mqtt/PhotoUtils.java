package com.jxai.module.camera.mqtt;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.jxai.lib.network.mqtt.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class PhotoUtils {

    /**
     * 存放拍照和相册中被选中的照片的路径
     */
    public final static String FILE = "/sdcard/photo/";

    private static String fileName;
    //存放裁剪后的照片的目录
    private static File file;
    public final static String FILE_VIDEO_PATH = "/mtkbs/video/";
    /**
     * 裁剪后照片的名称
     */
    public final static String FILE_IMAGE_PATH = "/mtkbs/image/";
    public final static String FILE_VIDEO_NAME = "alert_video.mp4";
    public final static String FILE_HISTORY_PATH = "/mtkbs/history/";
    private final static int LAST_CACHE_DAYS = -30;

    /**
     * 循环存储30天
     */

    public static String saveToSDCardByQueue(Bitmap bitmap, int imageID) {
        String[] lastDateString = Utils.getLastDateString(LAST_CACHE_DAYS);
        if (lastDateString.length < 2) {
            Log.d("mtk", "get last 30days date failed");
            return "";
        }
        String last30Dir = lastDateString[0];
        String todayDir = lastDateString[1];
        String last30Path = Environment.getExternalStorageDirectory().getAbsolutePath() + PhotoUtils.FILE_HISTORY_PATH + last30Dir;
        String todayPath = Environment.getExternalStorageDirectory().getAbsolutePath() + PhotoUtils.FILE_HISTORY_PATH + todayDir;
        File f30 = new File(last30Path);
        if (f30.exists()) {
            // 删除该目录
            deleteTempDirs(last30Path);
        }
        File fToday = new File(todayPath);
        if (!fToday.exists()) {
            fToday.mkdirs();
        }
        return PhotoUtils.saveToSDCard(bitmap, PhotoUtils.FILE_HISTORY_PATH + todayDir + "/", imageID + "");
    }

    /**
     * 保存bitmap 到sdCard
     *
     * @param bitmap
     */
    public static String saveToSDCard(Bitmap bitmap, String path, String name) {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            return null;
        }
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + path;
        FileOutputStream b = null;
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();// 创建文件夹
        }
        try {
            String fileName1;
            if (TextUtils.isEmpty(name)) {
                fileName1 = filePath + "avator_ablm.jpg";
            } else {
                if (!name.endsWith(".jpg")) {
                    name = name + ".jpg";
                }
                fileName1 = filePath + name;
            }
            b = new FileOutputStream(fileName1);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
            fileName = fileName1;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "-1";
        } finally {
            try {
                if (b != null) {
                    b.flush();
                    b.close();
//                    if (bitmap != null) {
//                        bitmap.recycle();
//                    }
                    return fileName;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "-1";
            }
            return fileName;
        }
    }

    /**
     * 文件转base64字符串
     *
     * @param file
     * @return
     */
    public static String fileToBase64(File file) {
        String base64 = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] bytes = new byte[in.available()];
            int length = in.read(bytes);
            base64 = Base64.encodeToString(bytes, 0, length, Base64.NO_PADDING | Base64.NO_WRAP);
        } catch (FileNotFoundException e) {
            Log.e("file not found", "");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("file not found", "");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return base64;
    }


    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.NO_PADDING | Base64.NO_WRAP);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    public static Bitmap getLocationBitmap(String path) {
        if (!TextUtils.isEmpty(path)) {
            FileInputStream inputStream = null;
            Bitmap bitmap;
            try {
                inputStream = new FileInputStream(path);
                bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (Exception e) {
                Log.e("", e.getMessage());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }


    /**
     * 通过降低图片的质量来压缩图片
     *
     * @param bitmap    要压缩的图片位图对象
     * @param maxSize   压缩后图片大小的最大值,单位KB
     * @param saveThumb 是否保存压缩图片
     * @return 压缩后的图片位图对象
     */
    public static Bitmap compressByQuality(Bitmap bitmap, int maxSize, String savePath, boolean saveThumb) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        Log.d("before compress：", baos.toByteArray().length + "");
        boolean isCompressed = false;
        while (baos.toByteArray().length / 1024 > maxSize) {
            quality -= 10;
            if (quality < 10) {
                break;
            }
            baos.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
//            Log.e("质量压缩到原来的" + quality + "%时大小为：" + baos.toByteArray().length + "byte");
            isCompressed = true;
        }
        if (saveThumb) {
            try {
                File targetFile2 = new File(savePath);
                if (targetFile2.exists()) {
                    targetFile2.delete();
                }
                targetFile2.createNewFile();
                FileOutputStream fos = new FileOutputStream(targetFile2);//将压缩后的图片保存的本地指定路径中
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (isCompressed) {
            Bitmap compressedBitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length);
//            recycleBitmap(bitmap);
            return compressedBitmap;
        } else {
            return bitmap;
        }
    }

    /**
     * 回收位图对象
     *
     * @param bitmap
     */
    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            System.gc();
        }
    }

    public static void deleteTempFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        try {
            File file = new File(filePath);
            if (!file.exists())
                return;
            if (file.isFile()) {
                file.delete();// 删除所有文件
            }
        } catch (Exception e) {
            Log.e("", e.getMessage());
        }
    }

    public static void deleteTempDirs(String path) {
        try {
            File dir = new File(path);
            if (!dir.exists() || !dir.isDirectory())
                return;
            File[] files = dir.listFiles();

            for (File file : files) {
                if (file.isFile())
                    file.delete(); // 删除所有文件
            }
            // 删除空目录
            dir.delete();
        } catch (Exception e) {
            Log.e("", e.getMessage());
        }
    }


    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 图片转小视频
     */
    public static void performJcodec(final String videoId, final IImageToVideoListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e("performJcodec: ", "执行开始");
                    SequenceEncoderMp4 se;
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + FILE_IMAGE_PATH);
                    String mediaPath = Environment.getExternalStorageDirectory().getAbsolutePath() + FILE_VIDEO_PATH + videoId + "/";
                    File mp = new File(mediaPath);
                    if (!mp.exists()) {
                        mp.mkdirs();
                    }
                    File out = new File(mediaPath, FILE_VIDEO_NAME);
                    se = new SequenceEncoderMp4(out);
                    File[] files = file.listFiles();
                    if (files == null || files.length == 0) {
                        listener.onFormatFailed();
                        return;
                    }
                    for (File value : files) {
                        if (!value.exists()) {
                            continue;
                        }
                        // 修改width & height, 可以决定视频的大小
                        Bitmap frame = decodeSampledBitmapFromFile(value.getAbsolutePath(), 640, 360);
                        se.encodeImage(frame);
                    }
                    se.finish();
                    String videoPath = mediaPath + FILE_VIDEO_NAME;
                    listener.onFormatSuccess(videoPath);

                    // 删除临时图片
                    deleteTempDirs(file.getAbsolutePath());
                    Log.e("performJcodec: ", "执行完成");
                } catch (IOException e) {
                    Log.e("performJcodec: ", "执行异常 " + e.toString());
                }

            }
        }).start();
    }


    public static Bitmap rotateMyBitmap(Bitmap bmp, boolean isRotate) {
        Matrix matrix = new Matrix();
        if (isRotate) {
            matrix.postRotate(180);
        }
        final Bitmap bitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        final Bitmap nbmp2 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        return nbmp2;
    }


    public static Bitmap decodeFile(File file) throws IOException {
        Bitmap b;
        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = new FileInputStream(file);
        BitmapFactory.decodeStream(fis, null, o);
        fis.close();

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        fis = new FileInputStream(file);
        b = BitmapFactory.decodeStream(fis, null, o2);
        fis.close();
        return b;
    }


    public static interface IImageToVideoListener {

        void onFormatSuccess(String path);

        void onFormatFailed();

    }

}