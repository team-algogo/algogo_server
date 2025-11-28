package com.ssafy.algogo.common.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("[UNEXPECTED ERROR]", e);

        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatusCode())
            .body(new ErrorResponse(errorCode.getErrorCode(), e.getMessage(), e.getData()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException e) {

        boolean isMissingParams = false;

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            String code = fieldError.getCode();
            if (code != null && code.equals("NotNull") || Objects.requireNonNull(code)
                .equals("NotBlank") || code.equals("NotEmpty")) {
                isMissingParams = true;
                break;
            }
        }

        String errorMessage = Objects.requireNonNull(
            e.getBindingResult().getFieldError().getDefaultMessage());

        ErrorCode code = null;
        if (isMissingParams) {
            code = ErrorCode.MISSING_PARAMETER;
        } else {
            code = ErrorCode.INVALID_PARAMETER;
        }

        return ResponseEntity
            .status(code.getHttpStatusCode())
            .body(new ErrorResponse(code.getErrorCode(), errorMessage));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
        NoResourceFoundException e) {
        ErrorCode code = ErrorCode.NOT_FOUND;
        return ResponseEntity.status(code.getHttpStatusCode())
            .body(new ErrorResponse(code.getErrorCode(), "요청 URL을 찾을 수 없습니다."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException e) {
        ErrorCode code = ErrorCode.BAD_REQUEST;
        return ResponseEntity.status(code.getHttpStatusCode())
            .body(new ErrorResponse(code.getErrorCode(), "잘못된 파라미터 타입입니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllException(Exception e) {
        log.error("[UNEXPECTED ERROR]", e);

        ErrorCode code = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(code.getHttpStatusCode())
            .body(new ErrorResponse(code.getErrorCode(), e.getLocalizedMessage()));
    }
}
