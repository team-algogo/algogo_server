package com.ssafy.algogo.common.advice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SuccessResponse {

    private String message;
    private Object data;

    public static SuccessResponse success(String message, Object data) {
        return new SuccessResponse(message, data);
    }


}
