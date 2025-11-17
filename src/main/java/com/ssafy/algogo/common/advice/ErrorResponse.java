package com.ssafy.algogo.common.advice;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private final String errorCode;
    private String message;
    private Object data;

    public ErrorResponse(String errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public ErrorResponse(String errorCode, String message, Object data) {
        this.errorCode = errorCode;
        this.message = message;
        this.data = data;
    }


}
