package com.andrulban.sandbox.stripe.exception;

import java.util.Date;

public class ErrorInfo {

    private Date timestamp;
    private int statusCode;
    private String message;

    public ErrorInfo() {
    }

    public ErrorInfo(Date timestamp, int statusCode, String message) {
        this.timestamp = timestamp;
        this.statusCode = statusCode;
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
