package com.harry.stt;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * HTTP endpoints for subtitles. No logic lives here: SubtitleService does the
 * work, and this class only maps URLs to it.
 */
@RestController
public class SubtitleController {
    private final SubtitleService subtitleService;
    private final UsageStats usageStats;

    public SubtitleController(SubtitleService subtitleService, UsageStats usageStats) {
        this.subtitleService = subtitleService;
        this.usageStats = usageStats;
    }

    // The player posts a WAV chunk; we reply with phrases and word timings
    // (in seconds from the start of that chunk).
    @PostMapping("/api/v1/subtitles")
    public SubtitleResponse subtitles(@RequestParam("file") MultipartFile audio) throws IOException {
        return subtitleService.subtitles(audio);
    }

    // Total seconds of audio sent to the speech-to-text service since the server started.
    @GetMapping("/api/v1/global/usage")
    public UsageResponse usage() {
        return new UsageResponse(usageStats.get());
    }
}