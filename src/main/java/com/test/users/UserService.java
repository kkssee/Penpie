package com.test.users;

import com.test.CustomUserDetails;
import com.test.novel.CommentDTO;
import com.test.novel.NovelVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    @Lazy
    private BCryptPasswordEncoder passwordEncoder;

    protected CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }
        return null;
    }

    public void registerUser(UserVO userVO) {
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userVO.getPassword());
        userVO.setPassword(encodedPassword);
        // 사용자 정보 저장
        userDAO.insertUser(userVO);

        // 기본 권한 설정
        userDAO.insertAuthority(userVO.getUserid(), "ROLE_USER");
    }

    public Long getUserno(String nickname) {
        return userDAO.getUserno(nickname);
    }

    public List<NovelVO> getSubscribedNovels(Long profileUserno) {
        Long userno = (getCurrentUserDetails() != null) ? getCurrentUserDetails().getUserno() : 0;
        return userDAO.getSubscribedNovels(userno, profileUserno);
    }

    public List<CommentDTO> getCommentsByUser(Long profileUserno) {
        return userDAO.getCommentsByUser(getCurrentUserDetails().getUserno(), profileUserno);
    }

    public List<NovelVO> getNovelsByAuthor(Long profileUserno) {
        return userDAO.getNovelsByAuthor(getCurrentUserDetails().getUserno(), profileUserno);
    }


    public boolean insertUserVerification(boolean isAdult, Date sDate, String gender, String username) {
        return userDAO.insertUserVerification(isAdult, sDate, gender, username, getCurrentUserDetails().getUserno());
    }

    public boolean updatesubscribeAuthorStatus(Long authorno, boolean subscribed) {
        Long userno = getCurrentUserDetails().getUserno();
        if(subscribed) {
            return userDAO.removeAuthorSubscribe(authorno, userno);
        } else {
            return userDAO.addAuthorSubscribe(authorno, userno);
        }
    }

    public UserVO getSubscribeStatus(Long userno, Long viewerUserNo) {
        return userDAO.getSubscribeStatus(userno, viewerUserNo);
    }

    public String getUserProfileImage(Long userno) {
        return userDAO.getUserProfileImage(userno);
    }

    public int getFollowingCount(Long userno) {
        return Objects.requireNonNullElse(userMapper.getFollowingCount(userno), 0);
    }

    public List<UserVO> getFollowerList(Long userno, Long viewerUserNo) {
        return userMapper.getFollowerList(userno, viewerUserNo);
    }

    public List<UserVO> getFollowingList(Long userno, Long viewerUserNo) {
        return userMapper.getFollowingList(userno, viewerUserNo);
    }

    @Transactional
    public void savePoint(int amount) {
        UserVO user = new UserVO();
        user.setPie_point(amount/100);
        user.setUserno(getCurrentUserDetails().getUserno());
        Integer point = userMapper.getPoint(user.getUserno());
        if(point == null) {
            userMapper.createPoint(user);
        } else {
            userMapper.savePoint(user);
        }
        userMapper.pointRecord(user);
    }

    public int getPoint() {
        Long userno = getCurrentUserDetails().getUserno();
        if(userno == null) {
            return 0;
        }
        Integer point = userMapper.getPoint(userno);
        if(point == null) {
            return 0;
        } else return point;
    }
}
