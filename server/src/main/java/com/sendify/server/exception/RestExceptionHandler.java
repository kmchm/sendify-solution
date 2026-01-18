package com.sendify.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(TrackingReferenceMissingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleTrackingReferenceMissing(TrackingReferenceMissingException e) {
        return e.getMessage();
    }

    @ExceptionHandler(CaptchaRequiredException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public String handleCaptchaRequired(CaptchaRequiredException e) {
        return e.getMessage();
    }

    @ExceptionHandler(ShipmentTrackingException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public String handleShipmentTracking(ShipmentTrackingException e) {
        return e.getMessage();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception e) {
        return "An unexpected error occurred. Please try again later.";
    }
}
