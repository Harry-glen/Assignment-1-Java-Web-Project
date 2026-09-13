package com.harry.stt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TranscriptionService {

    private final String sttApiUrl;
    private final String sttApiKey;
    private final TokenStats tokenStats;
    private final RestClient restClient;

    public TranscriptionService(
            @Value("${stt.api.url}") String sttApiUrl,
            @Value("${stt.api.key}") String sttApiKey,
            TokenStats tokenStats,
            RestClient.Builder restClientBuilder) {
        this.sttApiUrl = sttApiUrl;
        this.sttApiKey = sttApiKey;
        this.tokenStats = tokenStats;
        this.restClient = restClientBuilder.build();
    }
	
	// Send audio to configured STT service and return response
	// URL comes from config (stt.api.url), this points at local stub
	// during development and the real OpenAI endpoint on TITAN same code, both
	
	public String transcribe(MultipartFile audio) throws IOException {
	    // build the multipart body: the audio file + the model field
	    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
	    
	    body.add("file", new ByteArrayResource(audio.getBytes()) {
	        @Override
	        public String getFilename() {
	            return "audio.webm";   // gives OpenAI a filename+extension it recognises
	        }
	    });
	    
	    body.add("model", "gpt-4o-mini-transcribe");
	    
		TranscriptionResponse response = restClient.post()
				.uri(sttApiUrl)
				.header("Authorization", "Bearer " + sttApiKey)
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(body)
				.retrieve()
				.body(TranscriptionResponse.class);

		if (response.usage() != null) {
			tokenStats.addUsage(response.usage().inputTokens(), response.usage().outputTokens());
		}
		
		return response.text();
	}
}

