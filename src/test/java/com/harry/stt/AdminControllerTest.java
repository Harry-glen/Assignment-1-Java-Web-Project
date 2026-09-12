package com.harry.stt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AdminControllerTest {

    @Test
    void secondShutdownRequestReturns409() {
        // build a controller with real simple beans and a mock context
        AdminController controller = new AdminController(
            new ServerStartupTime(),
            new TokenStats(),
            mock(ConfigurableApplicationContext.class)
        );

        // first call: should be accepted (202)
        ResponseEntity<?> first = controller.shutdown();
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        // second call, while shutdown already in progress: should be rejected (409)
        ResponseEntity<?> second = controller.shutdown();
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
    
    @Test
    void uptimeReturnsSensibleValues() {
        AdminController controller = new AdminController(
            new ServerStartupTime(),
            new TokenStats(),
            mock(ConfigurableApplicationContext.class)
        );

        UptimeResponse uptime = controller.getUptime();

        // all three fields should be populated, and uptime can't be negative
        assertThat(uptime.utcServerStart()).isNotBlank();
        assertThat(uptime.utcNow()).isNotBlank();
        assertThat(uptime.serverUptimeSeconds()).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    void statsReflectRecordedUsage() {
        TokenStats stats = new TokenStats();
        AdminController controller = new AdminController(
            new ServerStartupTime(),
            stats,
            mock(ConfigurableApplicationContext.class)
        );

        // starts at zero
        assertThat(controller.getStats().inputTokens()).isEqualTo(0);

        // after recording usage, the endpoint reflects it
        stats.addUsage(10, 4);
        GlobalStatsResponse response = controller.getStats();
        assertThat(response.inputTokens()).isEqualTo(10);
        assertThat(response.outputTokens()).isEqualTo(4);
    }
}