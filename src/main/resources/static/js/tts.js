import { Client } from "https://cdn.jsdelivr.net/npm/@gradio/client@1.8.0/dist/index.min.js";


export async function basic_tts(audioBlob, inputText) {
    console.log("왓다");
    try {
        const client = await Client.connect("https://100.26.206.194.nip.io/");
        const result = await client.predict("/basic_tts", {
            ref_audio_input: audioBlob,
            ref_text_input: "",
            gen_text_input: inputText,
            remove_silence: true,
            cross_fade_duration_slider: 0.15,
            speed_slider: 1.2,
        });

        const audioBlobResult = await fetch(result.data[0].url).then(res => res.blob());
        const audioURL = URL.createObjectURL(audioBlobResult);

        // HTML의 audio 태그를 업데이트
        const audioElement = document.getElementById("audioPreview");
        if (audioElement) {
            audioElement.src = audioURL;
            audioElement.style.display = "block";
            audioElement.play();
        } else {
            console.error("Audio tag with id 'audioPreview' not found.");
        }
        document.getElementById("refText").value = result.data[2].value;
        console.log("Audio playback started");
        console.log("ref_text_input: ", result.data[2].value);
    } catch (error) {
        console.error("Error in basic_tts:", error);
    }
}

export async function voice_call(voiceSelect, inputText) {
    console.log("키타키타");
    //const response_0 = await fetch("https://penpie.site/audio/voice/" + voiceSelect.value);
    const response_0 = await fetch("https://penpie.site/audio/voice/HermioneGranger.mp3");
    const exampleAudio = await response_0.blob();
    console.log(voiceSelect.value);
    try {
        const client = await Client.connect("https://100.26.206.194.nip.io/");
        const result = await client.predict("/basic_tts", {
            ref_audio_input: exampleAudio,
            ref_text_input: "",
            gen_text_input: inputText,
            remove_silence: true,
            cross_fade_duration_slider: 0.15,
            speed_slider: 1.2,
        });

        const audioBlobResult = await fetch(result.data[0].url).then(res => res.blob());
        const audioURL = URL.createObjectURL(audioBlobResult);

        // HTML의 audio 태그를 업데이트
        const audioElement = document.getElementById("audioPreview");
        if (audioElement) {
            document.getElementById("loading-spinner").style.display = "none";
            audioElement.src = audioURL;
            audioElement.play();
            await new Promise((resolve) => {
                audioElement.onended = () => resolve();
            });
        } else {
            console.error("Audio tag with id 'audioPreview' not found.");
        }
        console.log("Audio playback started");
    } catch (error) {
        console.error("Error in voice_call:", error);
    }
}


