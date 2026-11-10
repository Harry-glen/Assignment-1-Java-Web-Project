package com.harry.stt;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

// Test stub standing in for the OpenAI transcription API during local development
@RestController
public class StubSttController {

    @PostMapping("/stub/transcriptions")
    public Map<String, String> fakeTranscription() {
        return Map.of("text", "this is a fake transcription");
    }

        // Stands in for OpenAI's whisper-1 verbose_json reply during local development.
    // One good segment and one junk segment (silence/noise), so the filter can be seen working.
    @PostMapping("/stub/audio/transcriptions")
    public WhisperResponse fakeWhisper() {
        return new WhisperResponse(
                30.0,
                "hello there this is a test",
                List.of(
                        new WhisperResponse.Segment(0.5, 3.0, "Hello there, this is a test.", -0.2, 0.01),   // kept
                        new WhisperResponse.Segment(4.0, 6.0, "Thanks for watching!", -2.0, 0.9)),            // junk: dropped
                List.of(
                        new WhisperResponse.Word("Hello", 0.5, 0.9),
                        new WhisperResponse.Word("there,", 0.9, 1.3),
                        new WhisperResponse.Word("this", 1.5, 1.7),
                        new WhisperResponse.Word("is", 1.7, 1.8),
                        new WhisperResponse.Word("a", 1.8, 1.9),
                        new WhisperResponse.Word("test.", 1.9, 3.0)));
    }
}