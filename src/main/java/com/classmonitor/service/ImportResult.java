package com.classmonitor.service;

public class ImportResult {

    private final int successCount;
    private final int failureCount;
    private final String message;

    public ImportResult(int successCount, int failureCount, String message) {
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.message = message;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message + " (Success: " + successCount +
                ", Failed: " + failureCount + ")";
    }
}
