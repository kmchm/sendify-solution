package com.sendify.server.exception;

public class CaptchaRequiredException extends RuntimeException {
    public CaptchaRequiredException(String message) { super(message); }
    public CaptchaRequiredException(String message, Throwable cause) { super(message, cause); }
}