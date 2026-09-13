package com.harry.stt;

// Mirrors UptimeResponse in the assignment API YAML
public record UptimeResponse(String utcServerStart, String utcNow, double serverUptimeSeconds) {}