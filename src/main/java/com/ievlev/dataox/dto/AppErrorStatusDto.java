package com.ievlev.dataox.dto;

public class AppErrorStatusDto {
    private int status;
    private String message;

    public AppErrorStatusDto(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
