package com.jxai.lib.network.mqtt;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import com.jxai.lib.utils.DateUtil;
import com.jxai.lib.utils.DeviceUtil;
import com.jxai.lib.utils.FileUtil;
import com.jxai.lib.utils.NetworkUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static String deviceId;
    private static String hostIp;

    public static void initDeviceInfo(Context context) {
        deviceId = DeviceUtil.getSubscriberId(context);
        hostIp = getIPAddress(context);
    }

    public static String getDeviceId() {
        return deviceId;
    }

    public static String getIPAddress() {
        return hostIp;
    }

    public static boolean setDefaultSimCard(Context context) {
        SubscriptionManager sm = SubscriptionManager.from(context);
        SubscriptionInfo subInfo = sm.getActiveSubscriptionInfoForSimSlotIndex(0);
        if (subInfo == null) {
            Log.e("mtk_app", "sim  slot 0 is empty");
            subInfo = sm.getActiveSubscriptionInfoForSimSlotIndex(1);
            if (subInfo == null) {
                Log.e("mtk_app", "sim  slot 1 is empty");
                return false;
            }
        }
        int subId = subInfo.getSubscriptionId();
        try {
            Method method = sm.getClass().getMethod("setDefaultDataSubId",int.class);
            method.invoke(sm,subId);
            return true;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static long getSysFreeMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

    public static long getSysFreeSpace() {
        File file = Environment.getExternalStorageDirectory();//获取SD卡的目录
        return file.getFreeSpace();
    }

    public static int getProcessCpuRate() {
        StringBuilder tv = new StringBuilder();
        int rate = 0;

        try {
            String Result;
            Process p;
            p = Runtime.getRuntime().exec("top -n 1");

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((Result = br.readLine()) != null) {
                if (Result.trim().length() < 1) {
                    continue;
                } else {
                    String[] CPUusr = Result.split("%");
                    Log.d("--------------", Result);

                    tv.append("USER:" + CPUusr[0] + "\n");
                    String[] CPUusage = CPUusr[0].split("User");
                    String[] SYSusage = CPUusr[1].split("System");
                    tv.append("CPU:" + CPUusage[1].trim() + " length:" + CPUusage[1].trim().length() + "\n");
                    tv.append("SYS:" + SYSusage[1].trim() + " length:" + SYSusage[1].trim().length() + "\n");

                    rate = Integer.parseInt(CPUusage[1].trim()) + Integer.parseInt(SYSusage[1].trim());
                    break;
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(rate + "");
        return rate;
    }

    public static String getIPAddress(Context context) {
        return NetworkUtil.getIPAddress(context);
    }

    /**
     * 判断当前线程是否为主线程
     *
     * @return
     */
    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }


    public static  String replaceCNSpace(String mString) {
        Pattern p = Pattern.compile("  ");//中文空格
        Matcher m = p.matcher(mString);
        String after = m.replaceAll("");//替换为英文空格
        return after;
    }

    public static float getDiskRate() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        if (stat.getBlockCountLong() == 0) {
            return 0f;
        }
        float availableBlocks = stat.getAvailableBlocksLong();
        float totalBlocks = stat.getBlockCountLong();
        int rate = (int) ( (totalBlocks - availableBlocks) / totalBlocks * 1000);
        return rate / 10f;
    }

    /**
     * 计算已使用内存的百分比，并返回。
     *
     * @param context 可传入应用程序上下文。
     * @return 已使用内存的百分比，以字符串形式返回。
     */
    public static float getUsedPercentValue(Context context) {
        long totalMemorySize = getTotalMemory();
        long availableSize = getAvailableMemory(context) / 1024;
        int percent = (int) ((totalMemorySize - availableSize) / (float) totalMemorySize * 1000);

        return percent / 10f;
    }

    /**
     * 获取当前可用内存，返回数据以字节为单位。
     *
     * @param context 可传入应用程序上下文。
     * @return 当前可用内存。
     */
    public static long getAvailableMemory(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        mActivityManager.getMemoryInfo(mi);
        return mi.availMem;
    }

    /**
     * 获取系统总内存,返回字节单位为KB
     *
     * @return 系统总内存
     */
    public static long getTotalMemory() {
        long totalMemorySize = 0;
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            //将非数字的字符替换为空
            totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return totalMemorySize;
    }

    /**
     * 获取当前进程的CPU使用率
     *
     * @return CPU的使用率
     */
    public static float getCurProcessCpuRate() {
        float totalCpuTime1 = getTotalCpuTime();
        float processCpuTime1 = getAppCpuTime();
        try {
            Thread.sleep(360);
        } catch (Exception e) {
            e.printStackTrace();
        }
        float totalCpuTime2 = getTotalCpuTime();
        float processCpuTime2 = getAppCpuTime();
        float cpuRate = 100 * (processCpuTime2 - processCpuTime1)
                / (totalCpuTime2 - totalCpuTime1);
        return cpuRate;
    }

    /**
     * 获取总的CPU使用率
     *
     * @return CPU使用率
     */
    public static float getTotalCpuRate() {
        try {
            String[] cpuInfo = getCpuInfo();
            if (cpuInfo == null || cpuInfo.length < 9) {
                return 2.0f;
            }

            long totalCpuTime1 = Long.parseLong(cpuInfo[2])
                    + Long.parseLong(cpuInfo[3]) + Long.parseLong(cpuInfo[4])
                    + Long.parseLong(cpuInfo[6]) + Long.parseLong(cpuInfo[5])
                    + Long.parseLong(cpuInfo[7]) + Long.parseLong(cpuInfo[8]);
            long totalUsedCpuTime1 = totalCpuTime1 - Long.parseLong(cpuInfo[5]);

            try {
                Thread.sleep(360);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            cpuInfo = getCpuInfo();
            if (cpuInfo == null || cpuInfo.length < 9) {
                return 2.0f;
            }
            long totalCpuTime2 = Long.parseLong(cpuInfo[2])
                    + Long.parseLong(cpuInfo[3]) + Long.parseLong(cpuInfo[4])
                    + Long.parseLong(cpuInfo[6]) + Long.parseLong(cpuInfo[5])
                    + Long.parseLong(cpuInfo[7]) + Long.parseLong(cpuInfo[8]);
            long totalUsedCpuTime2 = totalCpuTime2 - Long.parseLong(cpuInfo[5]);
            ;
            if ((totalCpuTime2 - totalCpuTime1) == 0) {
                return 2.0f;
            }

            int cpuRate = (int) ((totalUsedCpuTime2 - totalUsedCpuTime1)
                    / (float) (totalCpuTime2 - totalCpuTime1) * 1000);

            return cpuRate / 10f;

        } catch (Exception e) {
            e.printStackTrace();
            return 2.0f;
        }
    }

    public static String[] getCpuInfo() {

        //todo 暂时屏蔽
        return new String[]{};
        /*String[] cpuInfos = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return cpuInfos;*/
    }

    /**
     * 获取系统总CPU使用时间
     *
     * @return 系统CPU总的使用时间
     */
    public static long getTotalCpuTime() {
        String[] cpuInfos = getCpuInfo();
        if (cpuInfos == null || cpuInfos.length < 9) {
            return 1;
        }
        return Long.parseLong(cpuInfos[2])
                + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
                + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5])
                + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
    }

    /**
     * 获取当前进程的CPU使用时间
     *
     * @return 当前进程的CPU使用时间
     */
    public static long getAppCpuTime() {
        // 获取应用占用的CPU时间

        //todo 这个地方暂时这样搞，拿不到权限，先给他默认值
        return 1024 * 4;
        /*String[] cpuInfos = null;
        try {
            int pid = android.os.Process.myPid();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/" + pid + "/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        long appCpuTime = Long.parseLong(cpuInfos[13])
                + Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15])
                + Long.parseLong(cpuInfos[16]);
        return appCpuTime;*/
    }


    public static boolean isNetWorkAvailable(Context context){
        return NetworkUtil.isNetWorkAvailable(context);
    }

    public static int getCurrentHour(){
        Calendar mCalendar=Calendar.getInstance();
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        return mCalendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取N天前的日期
     */
    public static String[] getLastDateString(int dayBefore) {
        return DateUtil.getLastDateString(dayBefore);
    }
	
    /**
     * 创建文件夹---之所以要一层层创建，是因为一次性创建多层文件夹可能会失败！
     */
    public static boolean createFileDir(File dirFile) {
        if (dirFile == null) return true;
        return FileUtil.createFileDir(dirFile.getPath());
    }

    public static File createFile(String dirPath, String fileName) {
        return FileUtil.createFile(dirPath,fileName);
    }

    public static void writeAlarmData(int imageID, byte[] data) {
        try {
            createFile("/sdcard/i1alarm",imageID+"alarm.txt");
            FileOutputStream fout = new FileOutputStream("/sdcard/i1alarm/"+imageID+"alarm.txt");
            fout.write(data);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeBatteryData(byte[] data) {
        try {
            createFile("/sdcard/i1alarm","bat.txt");
            FileOutputStream fout = new FileOutputStream("/sdcard/i1alarm/bat.txt");
            fout.write(data);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeDeviceErrorData(byte[] data) {
        try {
            createFile("/sdcard/i1alarm","error.txt");
            FileOutputStream fout = new FileOutputStream("/sdcard/i1alarm/error.txt");
            fout.write(data);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeTime2File() throws IOException {
		createFile("/sdcard","jxcheck.txt");
        try{
			long nowTime = System.currentTimeMillis();
            FileOutputStream fout = new FileOutputStream("/sdcard/jxcheck.txt");
            byte [] bytes = String.valueOf(nowTime).getBytes();

            fout.write(bytes);
            fout.close();
        }

        catch(Exception e){
            e.printStackTrace();
        }
    }

}
