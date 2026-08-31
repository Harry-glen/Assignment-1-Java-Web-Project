//For testing only

package com.harry.stt;

import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

//Test stub standing in for the OpenAI transcription API during local development
@RestController
public class StubSttController {

    @PostMapping("/stub/transcriptions")
    public Map<String, String> fakeTranscription() {
        return Map.of("text", "this is a fake transcription");
    }
}