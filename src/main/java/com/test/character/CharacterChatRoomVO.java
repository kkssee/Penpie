package com.test.character;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CharacterChatRoomVO {
    private Long room_id;
    private Long userno;
    private Long cno;
    private String memory;
}
