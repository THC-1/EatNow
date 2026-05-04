package com.eatnow.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final HttpStatus httpStatus;

    public BusinessException(String message) {
        this(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, message);
    }

    public BusinessException(HttpStatus httpStatus, String message) {
        this(httpStatus.value(), httpStatus, message);
    }

    public BusinessException(HttpStatus httpStatus, String message, Throwable cause) {
        this(httpStatus.value(), httpStatus, message, cause);
    }

    public BusinessException(int code, HttpStatus httpStatus, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public BusinessException(int code, HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
