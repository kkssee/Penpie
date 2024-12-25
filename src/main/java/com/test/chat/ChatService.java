package com.test.chat;

import com.test.users.UserDAO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ChatService {
    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatDAO chatDAO;

    @Autowired
    private UserDAO userDAO;
    @Autowired
    private ChatMapper chatMapper;

    // 메시지를 보내기 전 채팅방을 확인하고, 없으면 새로 생성
    @Transactional
    public int sendMessage(Long sender, Long receiver, String message) {
        // 채팅방 확인
        ChatRoomVO chatRoom = chatRoomRepository.findChatRoomByParticipants(sender, receiver);
        if (chatRoom == null) {
            // 채팅방이 없으면 새로 생성
            chatRoom = new ChatRoomVO();
            chatRoom.setSender(sender);
            chatRoom.setReceiver(receiver);
            chatRoom = chatRoomRepository.save(chatRoom);
        }

        // 메시지 저장
        ChatVO chatVO = new ChatVO();
        chatVO.setChatRoom(chatRoom);
        chatVO.setSender(sender);
        chatVO.setReceiver(receiver);
        chatVO.setMessage(message);

        // 최근 메시지와 시간을 갱신합니다.
        chatRoom.setLastMessage(message);
        chatRoom.setLastMessageTime(chatVO.getTimestamp());

        chatRepository.save(chatVO);
        // 채팅방을 저장합니다.
        chatRoomRepository.save(chatRoom);

        return chatRoom.getId().intValue();
    }

    public List<ChatRoomVO> findChatRoomsByUser(Long sender) {
        List<ChatRoomVO> chatRooms = chatRoomRepository.findBySenderOrReceiver(sender, sender);
        LocalDateTime today = LocalDateTime.now();

        for (ChatRoomVO chatRoom : chatRooms) {
            // 상대방의 사용자 ID를 확인
            Long otherUserId = (Objects.equals(chatRoom.getSender(), sender)) ? chatRoom.getReceiver() : chatRoom.getSender();
            chatRoom.setReceiverNickname(userDAO.getNicknameByUserno(otherUserId));
            // 메시지가 있는 경우, 가장 최근 메시지를 시간순으로 가져옴
            if (!chatRoom.getMessages().isEmpty()) {
                ChatVO lastMessage = chatRoom.getMessages()
                        .stream()
                        .max(Comparator.comparing(ChatVO::getTimestamp)) // 타임스탬프 기준으로 가장 최근 메시지
                        .orElse(null);

                chatRoom.setLastMessage(lastMessage.getMessage());
                chatRoom.setLastMessageTime(lastMessage.getTimestamp());
            }

                LocalDateTime givenDate = chatRoom.getLastMessageTime();

                // 오늘 날짜인지 확인
                if (givenDate.toLocalDate().isEqual(today.toLocalDate())) {
                    // 오늘 날짜일 경우 시간만 표시 (HH:mm)
                    chatRoom.setTimestampStr(givenDate.format(DateTimeFormatter.ofPattern("hh:mm a")));
                }
                // 같은 연도인지 확인
                else if (givenDate.getYear() == today.getYear()) {
                    // 올해일 경우 월-일만 표시 (MM-dd)
                    chatRoom.setTimestampStr(givenDate.format(DateTimeFormatter.ofPattern("MM-dd")));
                }
                // 이전 연도일 경우 년-월-일 표시 (yyyy-MM-dd)
                else {
                    chatRoom.setTimestampStr(givenDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                }
        } return chatRooms;
    }

    public void saveMessage(ChatVO chatVO) {
        chatRepository.save(chatVO);
    }

    public List<ChatVO> getMessagesByChatRoomId(Long chatRoomId) {
        return chatRepository.findByChatRoomId(chatRoomId);
    }

    @Transactional
    public void markAllMessagesAsRead(Long chatRoomId, Long userno) {
        // chatRoomId와 userno에 대한 모든 메시지를 읽음 상태로 업데이트
        chatRepository.markAllMessagesAsRead(chatRoomId, userno);
    }


    public String getChatRoomImg(Long id, Long userno) {
       return chatMapper.findOpponentProfileImg(id, userno);
    }
}
