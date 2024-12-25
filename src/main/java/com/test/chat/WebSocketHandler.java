package com.test.chat;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static Map<Integer, WebSocketSession> userMap = new HashMap<>();

    @Autowired
    private ChatService chatService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String usernoStr = (String) session.getAttributes().get("userno");
        int userno = usernoStr == null ? 0 : Integer.parseInt(usernoStr);

        // 기존 세션 종료
        if (userMap.containsKey(userno)) {
            WebSocketSession existingSession = userMap.get(userno);
            if (existingSession.isOpen()) {
                existingSession.close(); // 기존 세션 종료
            }
        }

        // 새로운 세션 추가
        userMap.put(userno, session);
        String chatRoomIdstr = (String) session.getAttributes().get("chatRoomId");
        if(chatRoomIdstr != null) {
            session.getAttributes().put("isChatRoom", "true");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Message from server: {}", message.getPayload());

        JSONParser parser = new JSONParser();
        JSONObject jsObj = (JSONObject) parser.parse(message.getPayload());
        // 메시지 타입 확인
        String type = (String) jsObj.get("type");

        if ("status".equals(type)) {
            String statusMessage = (String) jsObj.get("message");
            log.info("Status message: {}", jsObj.get("message"));


            // 입장 또는 퇴장 여부를 구분하는 로직 추가
            if (statusMessage.contains("join")) {
                log.info("User joined: {}", statusMessage);
            } else if (statusMessage.contains("left")) {
                log.info("User left: {}", statusMessage);
            }

            // 메시지를 모든 사용자에게 전송하되, 현재 세션을 가진 사용자(session)에게는 보내지 않음
            for (Entry<Integer, WebSocketSession> entry : userMap.entrySet()) {
                WebSocketSession userSession = entry.getValue();

                // 현재 세션(session)과 다른 세션에만 메시지를 보냄
                if (userSession != null && userSession.isOpen() && !userSession.equals(session)) {
                    JSONObject jsonStatusMessage = new JSONObject();
                    jsonStatusMessage.put("type", "status");
                    jsonStatusMessage.put("message", statusMessage);

                    TextMessage textMessage = new TextMessage(jsonStatusMessage.toString());
                    userSession.sendMessage(textMessage);
                }
            }
        } else {
            // receiver와 sender를 Long으로 가져온 후 null 체크
            Long receiverLong = (Long) jsObj.get("receiver");
            Long senderLong = (Long) jsObj.get("sender");
            String mes = (String) jsObj.get("message");
            String senderNickname = (String) jsObj.get("senderNickname");

            // receiverLong 및 senderLong이 null인 경우 예외 처리
            if (receiverLong == null || senderLong == null) {
                log.error("Receiver or sender ID is null: receiverLong={}, senderLong={}", receiverLong, senderLong);
                return; // 메시지를 처리하지 않고 종료
            }

            // Long을 int로 변환
            Long receiver = (Long) jsObj.get("receiver");
            Long sender = (Long) jsObj.get("sender");

            Long chatRoomIdLong = (Long) jsObj.get("chatRoomId");
            int chatRoomId = chatRoomIdLong.intValue();
            // 메시지를 JSON 형식으로 재구성하여 보냄
            JSONObject jsonMessage = new JSONObject();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            chatRoomId = chatService.sendMessage(sender, receiver, mes);
            jsonMessage.put("chatRoomId", chatRoomId);
            jsonMessage.put("receiver", receiver);
            jsonMessage.put("sender", sender);
            jsonMessage.put("message", mes);
            jsonMessage.put("type", "chatRoom");
            jsonMessage.put("timestamp", timestamp);
            jsonMessage.put("senderNickname", senderNickname);

            TextMessage textMessage = new TextMessage(jsonMessage.toString());

            // 수신자에게 메시지 전송
           /* WebSocketSession receiverSession = userMap.get(receiver);
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.sendMessage(textMessage);
            }*/

            // 발신자에게도 메시지 전송 (클라이언트에 즉시 표시되도록)
            WebSocketSession senderSession = userMap.get(sender);
            if (senderSession != null && senderSession.isOpen()) {
                senderSession.sendMessage(textMessage);
            }

            for (Entry<Integer, WebSocketSession> entry : userMap.entrySet()) {
                WebSocketSession userSession = entry.getValue();

                // 현재 세션(session)과 다른 세션에만 메시지를 보냄
                if (userSession != null && userSession.isOpen() && !userSession.equals(session)) {
                    userSession.sendMessage(textMessage);
                }
            }
        }
    }

    // 특정 채팅방의 모든 사용자에게 메시지를 전송하는 메서드
    private void broadcastToChatRoom(int chatRoomId, WebSocketSession receiverSession, int receiver, TextMessage textMessage) throws Exception {
            // 해당 사용자가 특정 채팅방에 속하는지 확인
            if (receiverSession != null && receiverSession.isOpen() && isUserInChatRoom(receiver, chatRoomId)) {
                receiverSession.sendMessage(textMessage);
            }
    }

    // 사용자가 특정 채팅방에 속하는지 확인하는 메서드
    private boolean isUserInChatRoom(int receiver, int chatRoomId) {
        // 사용자가 어떤 채팅방에 속하는지 확인하는 로직을 구현
        // 예: 세션에 저장된 속성이나 데이터베이스를 확인
        // return true 또는 false;
        //chatService.isUserInChatRoom()
        // 사용자 ID 가져오기
        return true;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Connection Closed");
        // 현재 세션이 채팅방에 있는 경우에만 퇴장 메시지를 전송
        String isChatRoom = (String) session.getAttributes().get("isChatRoom");
        if("true".equals(isChatRoom)) {
            for (Entry<Integer, WebSocketSession> entry : userMap.entrySet()) {
                if (entry.getValue() == session) {
                    int userno = entry.getKey();
                    userMap.remove(userno);
                    log.info("Leave: {}", userno);
                    break;
                }
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Error: " + exception);
        super.handleTransportError(session, exception);
    }
}
