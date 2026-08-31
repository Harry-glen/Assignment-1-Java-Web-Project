package com.harry.stt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class TranscriptionController {
	
    private static final Logger log = LoggerFactory.getLogger(TranscriptionController.class);   
    
    @Value("${stt.api.url}")
    private String sttApiUrl;

    @Value("${stt.api.key}")
    private String sttApiKey;


    @PostMapping("/api/v1/transcribe")
    public String transcribe(@RequestParam("file") MultipartFile audio) {
    	log.info("STT API URL is {}", sttApiUrl);
    	long audioSize = audio.getSize();
    	return "Received audio: " + audioSize + " bytes";
    }
}