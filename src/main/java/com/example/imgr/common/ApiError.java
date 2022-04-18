package com.example.imgr.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class ApiError {
    private String message;
    private HttpStatus status;
    private List<String> errors;

    public ApiError(HttpStatus status, String message, List<String> errors) {
        super();
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public ApiError(HttpStatus status, String message, String errors) {
        super();
        this.status = status;
        this.message = message;
        this.errors = Arrays.asList(errors);
    }

    public ApiError(HttpStatus status, String message) {
        super();
        this.status = status;
        this.message = message;
        this.errors = Arrays.asList(message);
    }


    public ApiError(String message) {
        super();
        this.message = message;
        this.status = HttpStatus.BAD_REQUEST;
        this.errors = Arrays.asList(message);
    }
}
