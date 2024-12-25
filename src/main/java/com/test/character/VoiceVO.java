package com.test.character;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class VoiceVO {
    private Long vno;
    private Long actorNo;
    private String refText;
    private String voiceName;
    private String voiceDesc;
    private String voicePath;
    private MultipartFile voiceFile;
    private int voiceVisibility;
}
