package com.test.chat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chat_vo")
public class ChatVO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 메시지 ID

    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoomVO chatRoom;  // 채팅방

    @Column(nullable = false)
    private Long sender;  // 발신자 ID (username)

    @Column(nullable = false)
    private Long receiver;  // 수신자 ID (username)

    @Column(nullable = false)
    private String message;  // 메시지 내용

    private LocalDateTime timestamp;  // 메시지 전송 시간

    @Column(nullable = false)
    private boolean isRead = false; // 메시지 읽음 상태 (기본값은 false)

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    @Transient
    private String profileImg;
}
