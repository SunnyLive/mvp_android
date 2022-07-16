package com.jxai.lib.core.mvp;

import java.io.Serializable;

public class HttpResult<T> implements Serializable {
    public int eventId;
    public String status;
    public String description;
    public T data;

    public static String SUCCESS = "OK";
    public static String ERROR = "ERROR";
    public static String FAIL = "FAIL";
    public static String JUMP = "JUMP";
    public static String GG = "GG";
    public static String WARN = "WARN";

    public boolean isSuccess() {
        return SUCCESS.equals(status);
    }

    public boolean isError() {
        return ERROR.equals(status);
    }

    public boolean isGG() {
        return GG.equals(status);
    }

    public boolean isTokenInvalid() {
        return FAIL.equals(status);
    }

    public boolean isServiceFailed() {
        return GG.equals(status)||WARN.equals(status);
    }

    public boolean isJump() {
        return JUMP.equals(status);
    }

    public boolean isServiceErr() {
        return ERROR.equals(status);
    }

}