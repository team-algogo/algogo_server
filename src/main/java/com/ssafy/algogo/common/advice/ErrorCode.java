package com.ssafy.algogo.common.advice;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    BAD_REQUEST_ERROR(400, "BAD_REQUEST"),
    FORBIDDEN_ERROR(403, "FORBIDDEN"),
    NOT_FOUND_ERROR(404, "NOT_FOUND"),
    INTERNAL_SERVER_ERROR(500,"SERVER_ERROR");

    private final int httpStatusCode;
    private final String errorCode;
}
