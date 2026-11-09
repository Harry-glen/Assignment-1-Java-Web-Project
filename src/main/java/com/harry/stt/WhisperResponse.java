package com.harry.stt;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Models OpenAI's verbose_json reply. Fields we don't list are ignored on purpose. */
public record WhisperResponse(double duration, String text, List<Segment> segments, List<Word> words) {

    public record Segment(
            double start,
            double end,
            String text,
            @JsonProperty("avg_logprob") double avgLogProb,
            @JsonProperty("no_speech_prob") double noSpeechProb
    ) {}

    public record Word(String word, double start, double end) {}
}