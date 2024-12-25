package com.test.chat;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class ChatDAO {

    private final JdbcTemplate jdbcTemplate;

    public ChatDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ChatRoomVO findChatRoomByParticipants(String sender, String receiver) {
        String sql = "SELECT id, sender, receiver FROM chat_roomvo WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?)";

        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{sender, receiver, receiver, sender}, new RowMapper<ChatRoomVO>() {
                @Override
                public ChatRoomVO mapRow(ResultSet rs, int rowNum) throws SQLException {
                    ChatRoomVO chatRoom = new ChatRoomVO();
                    chatRoom.setId(rs.getLong("id"));
                    chatRoom.setSender(rs.getLong("sender"));
                    chatRoom.setReceiver(rs.getLong("receiver"));
                    // `messages` 필드는 지연 로딩 때문에 여기서 설정할 수 없음
                    return chatRoom;
                }
            });
        } catch (EmptyResultDataAccessException e) {
            // 채팅방이 존재하지 않는 경우
            return null;
        }
    }
}
