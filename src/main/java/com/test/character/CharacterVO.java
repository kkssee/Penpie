package com.test.character;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class CharacterVO {
    private Long cno;
    private Long makerNo;
    private String characterName;
    private String characterDesc;
    private String characterPrompt;
    private String relationships;
    private int characterVisibility;
    private String characterPic;



    private MultipartFile file;

}
