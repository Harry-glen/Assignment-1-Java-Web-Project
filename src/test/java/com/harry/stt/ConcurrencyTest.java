package com.harry.stt;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

// starts the real app on a random port so that it fires real concurrent HTTP requests at it
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConcurrencyTest {

    @LocalServerPort
    private int port;   // the random port Spring chose for the test server

    @Test
    void handlesOver200ConcurrentRequests() throws InterruptedException, ExecutionException{
        int requestCount = 250;
        
        String url = "http://localhost:" + port + "/api/v1/admin/uptime";

        HttpClient client = HttpClient.newHttpClient();
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Future<Integer>> results = new ArrayList<>();

        for (int i = 0; i < requestCount; i++) {
            results.add(executor.submit(() -> {
                startSignal.await();   // each task waits here until released together
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
                HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.statusCode();
            }));
        }

        startSignal.countDown();   // release all 250 at once

        executor.shutdown();
        boolean finishedInTime = executor.awaitTermination(30, TimeUnit.SECONDS);
        assertThat(finishedInTime).isTrue();   // no hanging, all completed in time

        for (Future<Integer> result : results) {
            assertThat(result.get()).isEqualTo(200);   // every request succeeded
        }
    }
}