package com.jxai.lib.network.okhttp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;

public class StringFormatUtils {
    public static String formatDouble(double d) {
        BigDecimal bg = new BigDecimal(d).setScale(2, RoundingMode.DOWN);
        double num = bg.doubleValue();
        if (Math.round(num) - num == 0) {
            return String.valueOf((long) num);
        }
        return String.valueOf(num);
    }

    /**
     * String转换Md5
     */
    public static String string2Md5(String value) {
        String MD5 = "";

        if (null == value) return MD5;

        try {
            MessageDigest mD = MessageDigest.getInstance("MD5");
            MD5 = byteArrayToHexString(mD.digest(value.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (MD5 == null) MD5 = "";

        return MD5;
    }

    private static String byteArrayToHexString(byte[] data) {
        char[] out = new char[data.length << 1];

        for (int i = 0, j = 0; i < data.length; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }
        return new String(out);
    }

    private static final char[] DIGITS_LOWER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
}
