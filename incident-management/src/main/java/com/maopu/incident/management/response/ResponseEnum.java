package com.maopu.incident.management.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author maopu
 * @version 1.0
 * @className ResponseEnum
 * @description TODO
 * @date 2024/11/28 23:35
 */

@Getter
@AllArgsConstructor
public enum ResponseEnum {
    // Operation successful
    success(200, "Operation successful"),
    // Closure
    closure(301, "Banned"),
    // Error
    error(400, "Bad request"),
    // Please login
    please_login(401, "Unauthorized"),
    // Forbidden
    forbidden(403, "Forbidden"),
    // Object not exist
    obj_not_exist(404, "Object not found"),
    // Failure
    failure(500, "Internal server error");

    private int code;
    private String message;

}
