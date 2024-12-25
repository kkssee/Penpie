package com.test.chat;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatVO, Long> {
    @Query("SELECT c FROM ChatVO c WHERE c.chatRoom.id = :chatRoomId ORDER BY c.timestamp ASC")
    List<ChatVO> findByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    // 읽음 상태를 업데이트하는 메서드
    @Modifying
    @Transactional
    @Query("UPDATE ChatVO c SET c.isRead = true WHERE c.chatRoom.id = :chatRoomId AND c.sender != :userno")
    void markAllMessagesAsRead(@Param("chatRoomId") Long chatRoomId, @Param("userno") Long userno);
}
