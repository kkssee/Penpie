package com.test.character;

import com.test.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CharacterService {
    @Autowired
    private CharacterMapper characterMapper;

    protected static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        } return null;
    }

    public Long uploadVoice(VoiceVO voice, MultipartFile file, HttpServletRequest request) throws IOException {
        String relativePath = "src/main/resources/static/audio/voice/";
        String absolutePath = new File("").getAbsolutePath() + "/" + relativePath;
        File directory = new File(absolutePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        UUID uuid = UUID.randomUUID();
        String voicePath = uuid + "_" + file.getOriginalFilename();
        file.transferTo(new File(absolutePath + voicePath));

        voice.setVoicePath(voicePath);
        voice.setActorNo(getCurrentUserDetails().getUserno());

        characterMapper.uploadVoice(voice);
        Long vno = voice.getVno();
        System.out.println("목소리 no:  " + vno);

        return vno;
    }

    @Transactional
    public Long createCharacter(CharacterVO character, MultipartFile file, HttpServletRequest request) throws IOException {
        String relativePath = "src/main/resources/static/img/characterPic/";
        String absolutePath = new File("").getAbsolutePath() + "/" + relativePath;
        File directory = new File(absolutePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        UUID uuid = UUID.randomUUID();
        String coverPath = uuid + "_" + file.getOriginalFilename();
        file.transferTo(new File(absolutePath + coverPath));

        character.setCharacterPic(coverPath);
        character.setMakerNo(getCurrentUserDetails().getUserno());

        characterMapper.createCharacter(character);
        Long cno = character.getCno();

        return cno;
    }

    public List<CharacterVO> getList(String sortOrder) {
        return characterMapper.getList();
    }

    public CharacterVO getCharacter(Long cno) {
        return characterMapper.getCharacter(cno);
    }

    public Long getRoomId(Long cno) {
        CharacterChatRoomVO rv = new CharacterChatRoomVO();
        rv.setCno(cno);
        rv.setUserno(getCurrentUserDetails().getUserno());

        Long roomId = characterMapper.getRoomId(rv);
        if(roomId == null) {
            roomId = characterMapper.createRoomId(rv);
        } return roomId;
    }

    public List<CharacterChatVO> getChatHistory(Long roomId) {
        List<CharacterChatVO> chatHistory = characterMapper.getChatHistory(roomId);
        return chatHistory;
    }

    public String getMemory(Long room_id) {
        return characterMapper.getMemory(room_id);
    }

    public int saveChat(CharacterChatVO cv) {
        return characterMapper.saveChat(cv);
    }

    public VoiceVO getVoice(Long room_id) {
        VoiceVO voice = characterMapper.getVoice(room_id);
        if (voice == null) {
            voice = new VoiceVO();
            voice.setVoicePath("TomHiddleston.mp3");
            voice.setRefText("Einstein's theory of relativity is E equals mc squared. This means that energy equals mass times the speed of light squared");
        } else {
            // voice가 null이 아니면 기존 처리
            if (voice.getVoicePath() == null) {
                voice.setVoicePath("TomHiddleston.mp3");
                voice.setRefText("Einstein's theory of relativity is E equals mc squared. This means that energy equals mass times the speed of light squared");
            }
        }
        return voice;
    }

    public List<VoiceVO> getVoiceList() {
        return characterMapper.getVoiceList();
    }
}
