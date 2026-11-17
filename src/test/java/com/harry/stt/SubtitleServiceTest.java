package com.harry.stt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// Tests the subtitle cache against a fake OpenAI server. MockRestServiceServer fails the test if
// Whisper is called more often than we expect, which is how "cache hit" is proved.
class SubtitleServiceTest {

    private static final String URL = "https://api.openai.com/v1/audio/transcriptions";
    private static final String WHISPER_REPLY = """
            {"duration": 30.0, "text": "hello",
             "segments": [{"start": 0.0, "end": 1.0, "text": "Hello.", "avg_logprob": -0.2, "no_speech_prob": 0.01}],
             "words": [{"word": "Hello.", "start": 0.0, "end": 1.0}]}
            """;

    private MockRestServiceServer server;
    private UsageStats usage;
    private SubtitleService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        usage = new UsageStats();
        service = new SubtitleService(URL, "test-key", usage, builder);
    }

    private static MockMultipartFile audio(int... bytes) {
        byte[] data = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) data[i] = (byte) bytes[i];
        return new MockMultipartFile("file", "audio.wav", "audio/wav", data);
    }

    @Test
    void sameAudioTwiceCallsWhisperOnceAndCountsUsageOnce() throws Exception {
        // Only ONE upstream call is expected; a second call would fail the test.
        server.expect(requestTo(URL)).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(WHISPER_REPLY, MediaType.APPLICATION_JSON));

        SubtitleResponse first = service.subtitles(audio(1, 2, 3));
        SubtitleResponse second = service.subtitles(audio(1, 2, 3));

        assertThat(second).isEqualTo(first);
        assertThat(first.segments()).hasSize(1);
        assertThat(usage.get()).isEqualTo(30.0);   // the cache hit added nothing
        server.verify();
    }

    @Test
    void differentAudioCallsWhisperAgain() throws Exception {
        server.expect(requestTo(URL)).andRespond(withSuccess(WHISPER_REPLY, MediaType.APPLICATION_JSON));
        server.expect(requestTo(URL)).andRespond(withSuccess(WHISPER_REPLY, MediaType.APPLICATION_JSON));

        service.subtitles(audio(1, 2, 3));
        service.subtitles(audio(4, 5, 6));

        assertThat(usage.get()).isEqualTo(60.0);
        server.verify();
    }

    @Test
    void failedCallIsNotCached() throws Exception {
        server.expect(requestTo(URL)).andRespond(withServerError());
        server.expect(requestTo(URL)).andRespond(withSuccess(WHISPER_REPLY, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service.subtitles(audio(1, 2, 3))).isInstanceOf(RestClientException.class);
        SubtitleResponse retry = service.subtitles(audio(1, 2, 3));   // same audio: must try Whisper again

        assertThat(retry.segments()).hasSize(1);
        assertThat(usage.get()).isEqualTo(30.0);   // the failed call counted nothing
        server.verify();
    }
}
