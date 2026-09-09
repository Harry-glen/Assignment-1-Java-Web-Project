package com.harry.stt;

import java.time.Duration;
import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AdminController {

    private final ServerStartupTime serverStartupTime;

    public AdminController(ServerStartupTime serverStartupTime) {
        this.serverStartupTime = serverStartupTime;
    }

    @GetMapping("/api/v1/admin/uptime")
    public UptimeResponse getUptime() {
        Instant start = serverStartupTime.getStartTime();
        Instant now = Instant.now();
        double seconds = Duration.between(start, now).toNanos() / 1_000_000_000.0;

        return new UptimeResponse(start.toString(), now.toString(), seconds);
    }
}