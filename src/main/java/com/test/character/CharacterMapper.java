package com.test.character;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CharacterMapper {
    Long createCharacter(CharacterVO character);

    List<CharacterVO> getList();

    CharacterVO getCharacter(Long cno);

    Long getRoomId(CharacterChatRoomVO rv);
    Long createRoomId(CharacterChatRoomVO rv);
    
    String getMemory(Long room_id);

    List<CharacterChatVO> getChatHistory(Long roomId);
    int saveChat(CharacterChatVO cv);

    void uploadVoice(VoiceVO voice);

    VoiceVO getVoice(Long room_id);

    List<VoiceVO> getVoiceList();
}
