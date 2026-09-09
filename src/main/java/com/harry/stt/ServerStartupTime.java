package com.harry.stt;

import java.time.Instant;

import org.springframework.stereotype.Component;

@Component
public class ServerStartupTime {

    private final Instant startTime;

    public ServerStartupTime() {
        this.startTime = Instant.now();   // runs once, at app startup
    }

    public Instant getStartTime() {
        return startTime;
    }
}
