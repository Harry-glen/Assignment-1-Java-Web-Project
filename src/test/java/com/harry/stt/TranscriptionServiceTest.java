package com.harry.stt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

// exercises the real transcribe() method end-to-end against a fake HTTP server standing in
// for OpenAI, so we verify request construction (auth header, url) and response parsing
// (text + token usage) without ever making a real network call or needing a Spring context
class TranscriptionServiceTest {

    @Test
    void transcribeParsesTextAndRecordsTokenUsage() throws Exception {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        server.expect(requestTo("https://api.openai.com/v1/audio/transcriptions"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", startsWith("Bearer ")))
            .andRespond(withSuccess("""
                {"text": "hello world", "usage": {"input_tokens": 10, "output_tokens": 4}}
                """, MediaType.APPLICATION_JSON));

        TokenStats tokenStats = new TokenStats();
        TranscriptionService transcriptionService = new TranscriptionService(
                "https://api.openai.com/v1/audio/transcriptions", "test-key", tokenStats, builder);

        MultipartFile audio = new MockMultipartFile("file", "audio.webm", "audio/webm", new byte[] {1, 2, 3});

        String text = transcriptionService.transcribe(audio);

        assertThat(text).isEqualTo("hello world");
        assertThat(tokenStats.getInputTokens()).isEqualTo(10);
        assertThat(tokenStats.getOutputTokens()).isEqualTo(4);
    }
}
