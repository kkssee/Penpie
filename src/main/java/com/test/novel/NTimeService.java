package com.test.novel;

import com.test.comment.CommentDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NTimeService {
    @Autowired
    private NovelDAO novelDAO;

    // IP와 에피소드 ID에 대한 마지막 조회 시간을 저장하는 맵
    private Map<String, LocalDateTime> viewRecords = new HashMap<>();
    @Autowired
    private CommentDAO commentDAO;

    // IP와 에피소드 ID를 조합하여 고유 키를 생성
    private String generateKey(String ip, int postId) {
        return ip + "_" + postId;
    }

    // 마지막 조회 시간을 반환
    public LocalDateTime getLastViewTime(String ip, int postId) {
        return viewRecords.get(generateKey(ip, postId));
    }

    // 조회 기록을 기록
    public void recordView(String ip, int postId) {
        viewRecords.put(generateKey(ip, postId), LocalDateTime.now());
    }

    // 조회 가능 여부 확인
    public boolean canView(String ip, int postId) {
        LocalDateTime lastViewTime = getLastViewTime(ip, postId);
        if (lastViewTime == null) {
            return true; // 기록이 없으면 조회 가능
        }
        // 서버 시간 기준으로 1시간이 지났는지 확인
        return lastViewTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    // 조회 수 증가
    public boolean incrementViewCount(int postId, String ip) {
        if (canView(ip, postId)) {
            boolean success = novelDAO.incrementViewCount(postId);
            if (success) {
                recordView(ip, postId); // 조회 기록 업데이트
            }
            return success;
        }
        return false; // 조회 불가
    }

    // 스케줄링 메서드: 10분마다 호출
    @Scheduled(fixedRate = 6000000) // 10분(600,000ms)마다 호출
    public void fetchTopComments() {
        List<CommentDTO> topComments = commentDAO.getTopComments();
        // 가져온 댓글을 처리할 로직 추가 (예: 캐시 저장, 클라이언트에 전송 등)
        System.out.println("Top comments fetched: " + topComments);
    }
}
