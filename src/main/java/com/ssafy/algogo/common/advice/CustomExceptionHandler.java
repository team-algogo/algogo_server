package com.ssafy.algogo.common.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Objects;

@RestControllerAdvice // restControllerAdvice로 하면될까? or ControllerAdvice로 하면될까?
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        e.printStackTrace();
        return ResponseEntity.status(e.errorCode.getHttpStatusCode()).body(new ErrorResponse(e.errorCode.getErrorCode(), e.getMessage(), e.getData()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {

        boolean isMissingParams = false;

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            String code = fieldError.getCode();
            if (code != null && code.equals("NotNull") || Objects.requireNonNull(code).equals("NotBlank") || code.equals("NotEmpty")) {
                isMissingParams = true;
                break;
            }
        }

        String errorMessage = Objects.requireNonNull(e.getBindingResult().getFieldError().getDefaultMessage());

        if (isMissingParams) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("MISSING PARAMETER", errorMessage != null ? errorMessage : "필수 파라미터를 입력해주세요"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("INVALID PARAMETER", errorMessage != null ? errorMessage : "유효하지 않은 파라미터 입니다."));
        }
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ErrorCode.NOT_FOUND_ERROR.getErrorCode(), "요청 URL을 찾을 수 없습니다."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ErrorCode.NOT_FOUND_ERROR.getErrorCode(), "요청 URL을 찾을 수 없습니다."));
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleAllException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR.getErrorCode(), e.getLocalizedMessage()));
    }
}
