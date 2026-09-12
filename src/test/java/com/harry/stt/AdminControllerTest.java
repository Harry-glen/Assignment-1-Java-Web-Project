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
}