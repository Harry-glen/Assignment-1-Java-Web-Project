package com.harry.stt;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

/**
 * Turns an audio chunk from the video player into subtitle text and timings.
 * It sends the audio to OpenAI's whisper-1 model, asks for both phrase (segment)
 * and word timestamps, drops phrases that are probably not speech, and records
 * how many seconds of audio were sent so /api/v1/global/usage can report it.
 *
 * <p>Finished results are cached by a hash of the audio, so playing or seeking over the same
 * media again (from any client) costs no Whisper time, is fast, and still works if the network
 * to OpenAI is down.
 *
 * <p>AI acknowledgement: the cache design (shared futures so concurrent requests for the same
 * audio make one Whisper call) was written with help from Claude (Anthropic), and reviewed and
 * understood by the author.
 */
@Service
public class SubtitleService {

    private final String subtitlesApiUrl; // real OpenAI URL normally, or the local stub in the "local" profile
    private final String sttApiKey;       // read from the environment, never logged or hard-coded
    private final UsageStats usageStats;
    private final RestClient restClient;

    // Audio hash -> its result. A future is stored (not just the result) so that if two requests for
    // the same audio arrive together, the second waits for the first Whisper call instead of making
    // its own. Results are small text, so there is no size limit or eviction.
    private final ConcurrentMap<String, CompletableFuture<SubtitleResponse>> cache = new ConcurrentHashMap<>();

    public SubtitleService(
            @Value("${subtitles.api.url}") String subtitlesApiUrl,
            @Value("${stt.api.key}") String sttApiKey,
            UsageStats usageStats,
            RestClient.Builder restClientBuilder) {
        this.subtitlesApiUrl = subtitlesApiUrl;
        this.sttApiKey = sttApiKey;
        this.usageStats = usageStats;
        this.restClient = restClientBuilder.build();
    }

    public SubtitleResponse subtitles(MultipartFile audio) throws IOException {
        byte[] bytes = audio.getBytes();
        String key = hash(bytes);

        CompletableFuture<SubtitleResponse> mine = new CompletableFuture<>();
        CompletableFuture<SubtitleResponse> existing = cache.putIfAbsent(key, mine);   // atomic: only one caller wins
        if (existing != null) {
            // Cache hit, or another request is already fetching this audio: use its result.
            try {
                return existing.join();
            } catch (CompletionException e) {
                // The call we were waiting on failed; pass the same error on so the player retries.
                if (e.getCause() instanceof RuntimeException cause) {
                    throw cause;
                }
                throw e;
            }
        }

        // Cache miss: we own this key, so we call Whisper.
        try {
            SubtitleResponse result = transcribe(bytes);
            mine.complete(result);
            return result;
        } catch (RuntimeException e) {
            cache.remove(key, mine);          // never cache a failure, so the next request tries again
            mine.completeExceptionally(e);    // wake anyone who was waiting on us
            throw e;
        }
    }

    // SHA-256 of the audio bytes as hex text: identical audio gives the identical key.
    private static String hash(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is always available", e);
        }
    }

    // The real work: one call to Whisper. Only reached on a cache miss.
    private SubtitleResponse transcribe(byte[] audio) {
        // Build the multipart form that OpenAI expects: the audio file plus options.
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(audio) {
            @Override
            public String getFilename() {
                return "audio.wav"; // OpenAI needs a filename with an extension it recognises
            }
        });
        body.add("model", "whisper-1");
        body.add("response_format", "verbose_json");        // needed to get timestamps and probabilities
        body.add("timestamp_granularities[]", "word");      // the same key twice is allowed:
        body.add("timestamp_granularities[]", "segment");   // we want both words and segments

        WhisperResponse response = restClient.post()
                .uri(subtitlesApiUrl)
                .header("Authorization", "Bearer " + sttApiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(WhisperResponse.class);

        // Count only audio we actually sent to OpenAI (duration is in seconds).
        usageStats.add(response.duration());

        // Whisper sometimes invents text for silence or background noise. The
        // assignment says to drop a segment when it is probably not speech AND the
        // model is not confident in its own text, so both conditions must hold.
        List<SubtitleSegment> segments = new ArrayList<>();
        if (response.segments() != null) {
            for (WhisperResponse.Segment s : response.segments()) {
                boolean junk = s.noSpeechProb() > 0.6 && s.avgLogProb() < -1.0;
                if (!junk) {
                    segments.add(new SubtitleSegment(s.start(), s.end(), s.text()));
                }
            }
        }

        // Words are passed on unchanged so the player can split long phrases at natural pauses.
        // The list can be missing from the reply, so treat that as empty.
        List<SubtitleWord> words = new ArrayList<>();
        if (response.words() != null) {
            for (WhisperResponse.Word w : response.words()) {
                words.add(new SubtitleWord(w.word(), w.start(), w.end()));
            }
        }

        return new SubtitleResponse(segments, words);
    }
}