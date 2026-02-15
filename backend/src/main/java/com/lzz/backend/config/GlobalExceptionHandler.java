package com.lzz.backend.config;

import com.lzz.backend.dto.ApiResponse;
import com.lzz.backend.exception.ServiceException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ServiceException.class)
    public ApiResponse<Void> handleBadRequest(ServiceException ex) {
        return ApiResponse.fail(ex.getMessage());
    }
}
