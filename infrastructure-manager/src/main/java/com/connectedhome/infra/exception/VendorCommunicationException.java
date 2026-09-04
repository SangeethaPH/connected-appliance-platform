package com.connectedhome.infra.exception;

public class VendorCommunicationException extends RuntimeException {
    public VendorCommunicationException(String message) { super(message); }
    public VendorCommunicationException(String message, Throwable cause) { super(message, cause); }
}
