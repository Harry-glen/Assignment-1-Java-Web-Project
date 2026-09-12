package com.harry.stt;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AdminController {

    private final ServerStartupTime serverStartupTime;
    private final TokenStats tokenStats; 
    
    private final ConfigurableApplicationContext applicationContext;

    public AdminController(ServerStartupTime serverStartupTime,
                           TokenStats tokenStats,
                           ConfigurableApplicationContext applicationContext) {
        this.serverStartupTime = serverStartupTime;
        this.tokenStats = tokenStats;
        this.applicationContext = applicationContext;
    }
    
    @GetMapping("/api/v1/admin/uptime")
    public UptimeResponse getUptime() {
        Instant start = serverStartupTime.getStartTime();
        Instant now = Instant.now();
        double seconds = Duration.between(start, now).toNanos() / 1_000_000_000.0;

        return new UptimeResponse(start.toString(), now.toString(), seconds);
    }
    
    @GetMapping("/api/v1/global/stats")
    public GlobalStatsResponse getStats() {
    	return new GlobalStatsResponse(tokenStats.getInputTokens(), tokenStats.getOutputTokens());
    }
    
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
    
    @PostMapping("/api/v1/admin/shutdown")
    public ResponseEntity<?> shutdown() { 
        // compareAndSet(false, true): atomically flips the flag ONLY if it was still false.
        // Returns true for the first caller (who now owns the shutdown), false for any later caller.
        if (!shuttingDown.compareAndSet(false, true)) {
            // someone already triggered shutdown — reject this one with 409
            ErrorResponse error = new ErrorResponse(
                Instant.now().toString(), 409, "Conflict",
                "Graceful shutdown is already in progress.", "/api/v1/admin/shutdown");
            return ResponseEntity.status(409).body(error);
        }

    	
        // shutdown runs on a separate thread so this 202 can be sent before the server stops,
        // closing the context immediately would kill the server before the response is flushed
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            applicationContext.close();
        }).start();

        return ResponseEntity.accepted().body(new ShutdownResponse("Graceful shutdown requested."));
    }
}