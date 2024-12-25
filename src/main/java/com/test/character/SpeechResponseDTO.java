package com.test.character;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.ai.openai.audio.speech.SpeechResponse;

@Getter
@Data
@NoArgsConstructor
public class SpeechResponseDTO {
    private byte[] output;

    @Builder
    public SpeechResponseDTO(SpeechResponse response) {
        this.output = response.getResult().getOutput();
    }

}

