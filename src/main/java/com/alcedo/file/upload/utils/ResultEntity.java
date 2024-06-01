package com.alcedo.file.upload.utils;

import org.springframework.http.HttpStatus;

/**
 * @ClassName: ResultEntity
 * @Author:  Alcedo
 * @CreateTime: 2023-05-16
 * @Description:
 */
public class ResultEntity<T> {



    private T data;

    /**
     * 状态码：200表示成功，500表示失败
     */
    private Integer code;

    private String msg;

    public ResultEntity() {
        this.code = HttpStatus.OK.value();
        this.msg = "请求成功";
    }

    public ResultEntity(T data, Integer code, String msg) {
        this.data = data;
        this.code = code;
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static <T> ResultEntity<T> success() {
        return new ResultEntity<>(null, HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
    }

    public static <T> ResultEntity<T> success(T data) {
        return new ResultEntity<T>(data, HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
    }

    public static <T> ResultEntity<T> success(String msg) {
        return new ResultEntity<T>(null, HttpStatus.OK.value(), msg);
    }

    public static <T> ResultEntity<T> success(T data, String msg) {
        return new ResultEntity<T>(data, HttpStatus.OK.value(), msg);
    }

    public static <T> ResultEntity<T> success(Integer code, String msg) {
        return new ResultEntity<T>(null, code, msg);
    }

    public static <T> ResultEntity<T> success(T data, Integer code, String msg) {
        return new ResultEntity<T>(data, code, msg);
    }

    public static <T> ResultEntity<T> error() {
        return new ResultEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    public static <T> ResultEntity<T> error(T data) {
        return new ResultEntity<T>(data, HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    public static <T> ResultEntity<T> error(String msg) {
        return new ResultEntity<T>(null, HttpStatus.INTERNAL_SERVER_ERROR.value(), msg);
    }

    public static <T> ResultEntity<T> error(T data, String msg) {
        return new ResultEntity<T>(data, HttpStatus.INTERNAL_SERVER_ERROR.value(), msg);
    }

    public static <T> ResultEntity<T> error(Integer code, String msg) {
        return new ResultEntity<T>(null, code, msg);
    }

    public static <T> ResultEntity<T> error(T data, Integer code, String msg) {
        return new ResultEntity<T>(data, code, msg);
    }



}
