package com.test.chat;

import com.test.users.UserDAO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/ws")
public class ChatController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private UserDAO userDAO;

    @GetMapping("")
    @ResponseBody
    public String index()
    {
        return "WebSocket Test";
    }

    @GetMapping("/in/{receiver}")
    public String joinChatRoom(@PathVariable("receiver") Long receiver,
                               HttpServletRequest request, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String senderId = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long sender = userDAO.getUsernoByUserid(senderId);

        ChatRoomVO chatRoom = chatRoomRepository.findChatRoomByParticipants(sender, receiver);
        if(chatRoom != null) {
            Long chatRoomId = chatRoom.getId();
            // 이전 메시지 불러오기
            List<ChatVO> messages = chatService.getMessagesByChatRoomId(chatRoomId);
            model.addAttribute("chatRoomId", chatRoomId);
            model.addAttribute("messages", messages); // 이전 메시지 목록 추가
        } else model.addAttribute("chatRoomId", 0);
        log.info("리시버 정보: {}", receiver);
        String nickname = userDAO.getNicknameByUserno(receiver);
        String receiverImg = userDAO.getUserProfileImage(receiver);
        String myNickname = userDAO.getNicknameByUserno(sender);
        // 모델에 필요한 데이터 추가
        model.addAttribute("sender", sender);
        model.addAttribute("receiver", receiver);
        model.addAttribute("receiverNickname", nickname);
        model.addAttribute("myNickname", myNickname);
        model.addAttribute("receiverImg", receiverImg);

        return "chat/chatForm"; // 채팅 페이지로 리다이렉트
    }

    @PostMapping("/markAsRead/{chatRoomId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long chatRoomId, @RequestBody Map<String, Object> request) {
        Integer usernoInt = (Integer) request.get("userno");
        Long userno = usernoInt != null ? usernoInt.longValue() : null;
        // 모든 메시지를 읽음 상태로 업데이트하는 로직
        chatService.markAllMessagesAsRead(chatRoomId, userno);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rooms")
    public String chatRoomList(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            Long userno = userDAO.getUsernoByUserid(username);
            List<ChatRoomVO> chatRooms = chatService.findChatRoomsByUser(userno);
            for (ChatRoomVO chatRoom : chatRooms) {
                chatRoom.setProfileImg(chatService.getChatRoomImg(chatRoom.getId(), userno));
            }
            model.addAttribute("chatRooms", chatRooms);
            model.addAttribute("currentUser", userno); // 현재 사용자 이름 추가
        }
        return "chat/chatRoomList"; // 채팅방 목록을 보여주는 페이지
    }

}

