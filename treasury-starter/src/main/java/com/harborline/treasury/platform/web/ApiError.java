package com.harborline.treasury.platform.web;

import java.time.OffsetDateTime;

public class ApiError {
    private final int status;
    private final String error;
    private final String message;
    private final String correlationId;
    private final OffsetDateTime timestamp;

    public ApiError(int status, String error, String message, String correlationId) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.correlationId = correlationId;
        this.timestamp = OffsetDateTime.now();
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getCorrelationId() { return correlationId; }
    public OffsetDateTime getTimestamp() { return timestamp; }
}
