package com.maopu.incident.management.response;


import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;


@NoArgsConstructor
public class ResponseFactory {

    public static <T> Response<T> getResponse(int status, String message, T data) {
        Response response = new Response();
        response.setStatus(status);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static <T> Response<T> getResponse(ResponseEnum responseEnum, T data) {
        return getResponse(responseEnum.getCode(), responseEnum.getMessage(), data);
    }

    public static Response getResponse(ResponseEnum responseEnum) {
        return getResponse(responseEnum.getCode(), responseEnum.getMessage(), (Object) null);
    }

    public static <T> Response<T> getSuccess(String message, T data) {
        return getResponse(ResponseEnum.success.getCode(), message, data);
    }

    public static <T> Response<T> getSuccessData(T data) {
        return getResponse(ResponseEnum.success.getCode(), ResponseEnum.success.getMessage(), data);
    }


    public static Response getSuccessMessage(String message) {
        return getResponse(ResponseEnum.success.getCode(), message, (Object) null);
    }

    public static Response getSuccess() {
        return getResponse(ResponseEnum.success.getCode(), ResponseEnum.success.getMessage(), (Object) null);
    }

    public static Response getError(String message) {
        return getResponse(ResponseEnum.error.getCode(), message, (Object) null);
    }

    public static Response getError() {
        return getResponse(ResponseEnum.error.getCode(), ResponseEnum.error.getMessage(), (Object) null);
    }

    public static Response getError(ResponseEnum responseEnum) {
        return getResponse(responseEnum.getCode(), responseEnum.getMessage(), (Object) null);
    }

    public static <V> Response<Map<String, V>> getSuccessData(String k1, V v1) {
        HashMap data = new HashMap(1);
        data.put(k1, v1);
        return getResponse(ResponseEnum.success.getCode(), ResponseEnum.success.getMessage(), data);
    }

}
