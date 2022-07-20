package com.jxai.lib.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 *
 * 这个是日期跟时间处理的工具类
 *
 *
 */
public class DateUtil {



    /**
     * 获取N天前的日期
     */
    public static String[] getLastDateString(int dayBefore) {
        SimpleDateFormat formatHM3 = new SimpleDateFormat("yyyyMMdd");
        Date now = new Date();
        Date lastMonth;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DATE, dayBefore);
        lastMonth = calendar.getTime();   //得到前3月的时间

        String dateBeforeStr = formatHM3.format(lastMonth);
        String dateNowStr = formatHM3.format(now); //格式化当前时间
        return new String[]{dateBeforeStr, dateNowStr};
    }







}
