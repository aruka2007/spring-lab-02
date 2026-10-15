package org.example.springlab02.aspect;

public class CatalogException extends RuntimeException {
    private final String errorCode;

    public CatalogException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CatalogException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
