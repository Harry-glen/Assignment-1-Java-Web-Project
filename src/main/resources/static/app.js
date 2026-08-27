// MediaRecorder is event-driven: 'dataavailable' fires repeatedly with audio
// chunks as they record, and 'stop' fires once at the end to assemble them.
let mediaRecorder;
let chunks = [];
let isRecording = false;

// grab the button from index
const recordButton = document.getElementById('record-btn');

async function startRecording(){
	try { 
		chunks = [];
		
		const stream = await navigator.mediaDevices.getUserMedia({ audio: true});
		
		mediaRecorder = new MediaRecorder(stream);
		
		mediaRecorder.addEventListener('dataavailable', (event) =>{
			chunks.push(event.data);
		});
		
		mediaRecorder.addEventListener('stop', () => {
			// Chrome's default capture format, the transcription API needs to be told this later
			const audioBlob = new Blob(chunks, { type: 'audio/webm' });
			// for testing
			console.log('audioBlob', audioBlob);
		});
		
		mediaRecorder.start();
		isRecording = true;
		recordButton.textContent = 'Stop';
	} catch (err) {
		console.error('Microphone access failed:', err);
	}
}

function stopRecording(){
	mediaRecorder.stop();
	isRecording = false;
	recordButton.textContent = 'Record';
}

recordButton.addEventListener('click', () => {
	if (isRecording) {
		stopRecording();
  	} else {
    	startRecording();
  	}
});