package com.connectedhome.infra.exception;

public class UnsupportedVendorMetricException extends RuntimeException {
    public UnsupportedVendorMetricException(String message) {
        super(message);
    }
}
