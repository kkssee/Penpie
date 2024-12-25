package com.test.board;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class TimeService {
    @Autowired
    private BoardDAO bdao;

    private Map<String, LocalDateTime> likeRecords = new HashMap<>();

    private String generateKey(String ip, int postid) {
        return ip + "_" + postid;
    }

    public LocalDateTime getLastLikeTime(String ip, int postid) {
        return likeRecords.get(generateKey(ip, postid));
    }

    public void recordLike(String ip, int postid) {
        likeRecords.put(generateKey(ip, postid), LocalDateTime.now());
    }

    public boolean canLike(String ip, int postid) {
        LocalDateTime lastLikeTime = getLastLikeTime(ip, postid);
        if (lastLikeTime == null) {
            return true;
        }
        // 1분 지났는지 확인
        return lastLikeTime.isBefore(LocalDateTime.now().minusMinutes(1));      //lastLikeTime.isBefore(LocalDateTime.now().minusHours(24));
    }

    public boolean inputLike(int postid) {
        int l = bdao.inputLike(postid);
        return l>0;
    }
}
