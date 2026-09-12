package com.harry.stt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class TokenStatsConcurrencyTest {

    @Test
    void concurrentUpdatesDoNotLoseCounts() throws InterruptedException {
        TokenStats stats = new TokenStats();

        int threads = 500;              // many concurrent writers
        int addsPerThread = 100;        // each adds this many times
        long inputPerAdd = 3;
        long outputPerAdd = 1;

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch startSignal = new CountDownLatch(1);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startSignal.await();   // all threads wait, then hammer together
                    for (int j = 0; j < addsPerThread; j++) {
                        stats.addUsage(inputPerAdd, outputPerAdd);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        startSignal.countDown();          // release all threads at once
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        // if AtomicLong is truly thread-safe, no updates are lost, so totals are exact:
        long expectedInput = (long) threads * addsPerThread * inputPerAdd;
        long expectedOutput = (long) threads * addsPerThread * outputPerAdd;

        assertThat(stats.getInputTokens()).isEqualTo(expectedInput);
        assertThat(stats.getOutputTokens()).isEqualTo(expectedOutput);
    }
}