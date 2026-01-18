package com.sendify.server.exception;

public class ShipmentTrackingException extends RuntimeException {
    public ShipmentTrackingException(String message) { super(message); }
    public ShipmentTrackingException(String message, Throwable cause) { super(message, cause); }
}