package com.harry.stt;

public record UptimeResponse(String utcServerStart, String utcNow, double serverUptimeSeconds) {}