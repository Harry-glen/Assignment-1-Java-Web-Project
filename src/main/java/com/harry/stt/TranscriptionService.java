package com.harry.stt;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TranscriptionService {
    public String transcribe(MultipartFile audio) {
        return "service was called";
    }
}

