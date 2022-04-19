package com.example.imgr.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    private T data;
    private String message;

    public ApiResponse(T data) {
        super();
        this.data = data;
        this.message = "Ok";
    }

    public ApiResponse(String message) {
        super();
        this.data = null;
        this.message = message;
    }
    public ApiResponse(T data, String message) {
        super();
        this.data = data;
        this.message = message;
    }
}
