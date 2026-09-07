package com.harry.stt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TranscriptionService {
	@Value("${stt.api.url}")
	private String sttApiUrl;
	
	@Value("${stt.api.key}")
	private String sttApiKey;
	
	// Send audio to configured STT service and return response
	// URL comes from config (stt.api.url), this points at local stub
	// during development and the real OpenAI endpoint on TITAN same code, both
	
	public String transcribe(MultipartFile audio) {
	    // build the multipart body: the audio file + the model field
	    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
	    body.add("file", audio.getResource());
	    body.add("model", "gpt-4o-mini-transcribe");
	    
		RestClient restClient = RestClient.create();
		
		TranscriptionResponse response = restClient.post()
				.uri(sttApiUrl)
				.header("Authorization", "Bearer " + sttApiKey)
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(body)
				.retrieve()
				.body(TranscriptionResponse.class);
		return response.text();

	}
}

