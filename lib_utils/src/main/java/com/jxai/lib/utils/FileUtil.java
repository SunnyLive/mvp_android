package com.jxai.lib.utils;


import android.util.Log;

import java.io.File;

/**
 *
 * 文件的统一管理工具类
 *
 *
 */
public class FileUtil {

    private static final String TAG = FileUtil.class.getName();

    /**
     *
     * 创建文件路径
     *
     * @param dir
     * @return
     */
    public static boolean createFileDir(String dir) {

        File dirFile = new File(dir);
        if (dirFile.exists()) {
            return true;
        }
        File parentFile = dirFile.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            //父文件夹不存在，则先创建父文件夹，再创建自身文件夹
            return createFileDir(parentFile.getPath())
                    && createFileDir(dirFile.getPath());
        } else {
            boolean mkdirs = dirFile.mkdirs();
            boolean isSuccess = mkdirs || dirFile.exists();
            if (!isSuccess) {
                Log.e(TAG, "createFileDir fail " + dirFile);
            }
            return isSuccess;
        }
    }

    /**
     *
     * 创建文件夹跟文件
     *
     * @param dirPath 文件路径
     * @param fileName 文件
     * @return isSuccess
     */
    public static File createFile(String dirPath, String fileName) {
        try {
            File dirFile = new File(dirPath);
            if (!dirFile.exists()) {
                if (!createFileDir(dirFile.getPath())) {
                    Log.e(TAG , "createFile dirFile.mkdirs fail");
                    return null;
                }
            } else if (!dirFile.isDirectory()) {
                boolean delete = dirFile.delete();
                if (delete) {
                    return createFile(dirPath, fileName);
                } else {
                    Log.e(TAG , "createFile dirFile !isDirectory and delete fail");
                    return null;
                }
            }
            File file = new File(dirPath, fileName);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e(TAG , "createFile createNewFile fail");
                    return null;
                }
            }
            return file;
        } catch (Exception e) {
            Log.e(TAG , "createFile fail :" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }



}
