package com.harry.stt;

// Mirrors ErrorResponse in the assignment API YAML
public record ErrorResponse(String timestamp, int status, String error, String message, String path) {}
