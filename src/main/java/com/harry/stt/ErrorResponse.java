package com.harry.stt;

public record ErrorResponse(String timestamp, int status, String error, String message, String path) {}
