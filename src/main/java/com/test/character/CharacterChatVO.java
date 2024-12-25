package com.test.character;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CharacterChatVO {
    private Long message_id;
    private Long room_id;
    private Long sender;
    private String message;
    private LocalDateTime timestamp;
    private int is_read;
}
