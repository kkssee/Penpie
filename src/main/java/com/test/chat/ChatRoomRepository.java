package com.test.chat;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomVO, Long> {
    @Query("SELECT c FROM ChatRoomVO c WHERE (c.sender = :sender AND c.receiver = :receiver) OR (c.sender = :receiver AND c.receiver = :sender)")
    ChatRoomVO findChatRoomByParticipants(@Param("sender") Long sender, @Param("receiver") Long receiver);

    List<ChatRoomVO> findBySenderOrReceiver(Long sender, Long sender1);
}
