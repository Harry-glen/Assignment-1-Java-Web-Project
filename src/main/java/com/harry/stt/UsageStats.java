package com.harry.stt;

import java.util.concurrent.atomic.DoubleAdder;

import org.springframework.stereotype.Component;

/**
 * Running total of audio seconds we have sent to the speech-to-text service.
 * Reported by /api/v1/global/usage. Only audio we actually send to OpenAI is
 * counted, so a future cache hit adds nothing.
 */
@Component
public class UsageStats {

    // Many requests can finish at the same time (the server handles each on its
    // own thread). A DoubleAdder adds safely without locks, where a plain double
    // field could lose updates.
    private final DoubleAdder seconds = new DoubleAdder();

    public void add(double value) {
        seconds.add(value);
    }

    public double get() {
        return seconds.sum(); // DoubleAdder reads with sum(), not get()
    }
}