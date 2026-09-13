package com.harry.stt;

import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TranscriptionController {
	    
    private final TranscriptionService transcriptionService;
    public TranscriptionController(TranscriptionService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }
    
    @PostMapping("/api/v1/transcribe")
    public String transcribe(@RequestParam("file") MultipartFile audio) throws IOException {
        return transcriptionService.transcribe(audio);

    }
}
