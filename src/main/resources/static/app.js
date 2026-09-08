// MediaRecorder is event-driven: 'dataavailable' fires repeatedly with audio
// chunks as they record, and 'stop' fires once at the end to assemble them.
let mediaRecorder;
let chunks = [];
let isRecording = false;
let stream;

// grab the button from index
const recordButton = document.getElementById('record-btn');

async function startRecording(){
	try { 
		chunks = [];
		
		stream = await navigator.mediaDevices.getUserMedia({ audio: true});
		
		mediaRecorder = new MediaRecorder(stream);
		
		mediaRecorder.addEventListener('dataavailable', (event) =>{
			chunks.push(event.data);
		});
		
		mediaRecorder.addEventListener('stop', async () => {
			// Chrome's default capture format, the transcription API needs to be told this later
			const audioBlob = new Blob(chunks, { type: 'audio/webm' });
			
			const formData = new FormData();
			formData.append('file', audioBlob);
			
			const transcriptEl = document.getElementById('transcript');
			
			try {
				const response = await fetch('/api/v1/transcribe', {
				  	method: 'POST',
				  	body: formData
				});
				
				// fetch only throws on network failure, not on error status codes
				if (!response.ok) {
					transcriptEl.textContent = 'Something went wrong, please try again.'
					return;
				}
				
				const transcript = await response.text();
				transcriptEl.classList.remove('placeholder');
				transcriptEl.textContent = transcript;
				
			// this catches the request never completing (no network, server unreachable),
			// as opposed to the server replying with an error above	
			} catch (err) {
				console.error('Transcription request failed:', err);
				transcriptEl.textContent = 'Could not reach the server — please try again.';
			}
		});
		
		mediaRecorder.start();
		isRecording = true;
		recordButton.classList.add('recording');
		recordButton.textContent = 'Stop';
	} catch (err) {
		console.error('Microphone access failed:', err);
	}
}

function stopRecording(){
	mediaRecorder.stop();
	stream.getTracks().forEach((track) => {
		track.stop();
	});
	isRecording = false;
	recordButton.classList.remove('recording');
	recordButton.textContent = 'Record';
}

recordButton.addEventListener('click', () => {
	if (isRecording) {
		stopRecording();
  	} else {
    	startRecording();
  	}
});