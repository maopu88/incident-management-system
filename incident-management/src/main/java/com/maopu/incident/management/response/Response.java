package com.maopu.incident.management.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maopu
 * @version 1.0
 * @className Response
 * @description TODO
 * @date 2024/11/28 23:31
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {
    private int status;
    private String message;
    private T data;

}
