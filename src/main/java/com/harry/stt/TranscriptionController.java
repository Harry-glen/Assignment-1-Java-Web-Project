package com.harry.stt;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TranscriptionController {

    @PostMapping("/api/v1/transcribe")
    public String transcribe(@RequestParam("file") MultipartFile audio) {
    	long audioSize = audio.getSize();
    	return "Received audio: " + audioSize + " bytes";
    }
}