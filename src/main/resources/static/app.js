// grab the button from index
const recordButton = document.getElementById('record-btn');

async function startRecording(){
	try {
		const stream = await navigator.mediaDevices.getUserMedia({ audio: true});
		// success
		console.log('Got the mic:', stream);
		
	} catch (err) {
		// failure
		console.error('Microphone access failed:', err);
	}
}

// run startRecording
recordButton.addEventListener('click', startRecording);