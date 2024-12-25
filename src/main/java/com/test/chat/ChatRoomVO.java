package com.test.chat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chat_roomvo")
public class ChatRoomVO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 채팅방 ID

    @Column(nullable = false)
    private Long sender;  // 첫 번째 사용자 ID (username)

    @Column(nullable = false)
    private Long receiver;  // 두 번째 사용자 ID (username)

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ChatVO> messages;

    @Transient
    private String senderNickname; // 발신자 닉네임

    @Transient
    private String receiverNickname; // 수신자 닉네임

    @Transient
    private String lastMessage; // 가장 최근 메시지

    @Transient
    private LocalDateTime lastMessageTime; // 가장 최근 메시지 시간

    @Transient
    private String timestampStr;

    @Transient
    private String profileImg;
}