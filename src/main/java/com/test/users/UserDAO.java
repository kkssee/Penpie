package com.test.users;

import com.test.novel.CommentDTO;
import com.test.novel.NovelVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class UserDAO {
    @Autowired
    UserMapper userMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public UserVO getUserByID(String username) {
        return userMapper.findByID(username);
    }

    // users 테이블에 사용자 정보 저장
    public void insertUser(UserVO userVO) {
        String sql = "INSERT INTO users (userno, USERID, PASSWORD, USERNAME, NICKNAME, GENDER, BIRTH, CONTACT, EMAIL, SIGNUP_DATE, enabled) " +
                "VALUES (USERS_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, 1)";

        jdbcTemplate.update(sql,
                userVO.getUserid(),
                userVO.getPassword(),
                userVO.getUsername(),
                userVO.getNickname(),
                userVO.getGender(),
                userVO.getBirth(),
                userVO.getContact(),
                userVO.getEmail());
    }

    public Long getUsernoByUserid(String userid) {
        String sql = "SELECT userno FROM users WHERE userid = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, userid);
        } catch (EmptyResultDataAccessException e) {
            // 결과가 없을 경우 0 또는 다른 기본값 반환
            log.warn("해당 유저아이디 {}에 대한 userno를 찾을 수 없습니다.", userid);
            return 0L; // 기본값으로 0을 반환하거나 상황에 맞는 처리를 함
        }
    }


    public void insertAuthority(String userid, String authority) {
        // userid를 기반으로 userno 조회
        Long userno = getUsernoByUserid(userid);

        // 조회한 userno로 authorities 테이블에 권한 추가
        String sql = "INSERT INTO authorities (userno, authority) VALUES (?, ?)";
        jdbcTemplate.update(sql, userno, authority);
    }

    public String getNickname(String userid) {
        try {
            return jdbcTemplate.queryForObject("SELECT nickname FROM users WHERE userid = ?", new Object[]{userid}, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null; // 또는 기본값 반환
        }
    }
    public String getNicknameByUserno(Long userno) {
        try {
            return jdbcTemplate.queryForObject("SELECT nickname FROM users WHERE userno = ?", new Object[]{userno}, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null; // 또는 기본값 반환
        }
    }

    public List<NovelVO> getSubscribedNovels(Long viewerUserno, Long profileUserno) {
        String sql = "SELECT nv.nv_id, nv.nv_title, nv.nv_desc, nv.cover_path, nv.updated_at, nv.is_adult, nv.is_secret, " +
                "u.nickname, " +
                "(SELECT COUNT(*) FROM novel_subscribe WHERE nv_id = nv.nv_id) AS subscribeCount, " +
                "COALESCE((SELECT 1 FROM novel_subscribe WHERE userno = ? AND nv_id = nv.nv_id), 0) AS isSubscribed, " +
                "COALESCE(SUM(lk.likeCount), 0) AS likeCount, " +
                "COALESCE(SUM(co.commentCount), 0) AS commentCount, " +
                "COALESCE(SUM(ev.ep_views), 0) AS totalViews " +
                "FROM novels nv " +
                "JOIN users u ON nv.userno = u.userno " +
                "LEFT JOIN episode ev ON nv.nv_id = ev.nv_id " +
                "LEFT JOIN (SELECT ep_id, COUNT(*) AS likeCount FROM ep_likes GROUP BY ep_id) lk ON ev.ep_id = lk.ep_id " +
                "LEFT JOIN (SELECT ep_id, COUNT(*) AS commentCount FROM comments GROUP BY ep_id) co ON ev.ep_id = co.ep_id " +
                "WHERE nv.is_secret = 0 AND nv.nv_id IN (SELECT nv_id FROM novel_subscribe WHERE userno = ?) " + // profileUserno의 구독 소설만 가져옴
                "GROUP BY nv.nv_id, nv.nv_title, nv.nv_desc, nv.cover_path, nv.updated_at, nv.is_adult, nv.is_secret, u.nickname";

        List<NovelVO> list = jdbcTemplate.query(sql, new Object[]{viewerUserno, profileUserno}, (rs, i) ->
                new NovelVO(
                        rs.getInt("nv_id"),
                        rs.getString("nv_title"),
                        rs.getString("nv_desc"),
                        rs.getString("nickname"),
                        rs.getDate("updated_at"),
                        rs.getInt("is_adult"),
                        rs.getInt("is_secret"),
                        rs.getString("cover_path"),
                        rs.getInt("subscribeCount"),
                        rs.getInt("isSubscribed"),
                        rs.getInt("likeCount"),
                        rs.getInt("commentCount"),
                        rs.getInt("totalViews")
                )
        );

        return list;
    }




    public List<CommentDTO> getCommentsByUser(Long viewerUserno, Long profileUserno) {
        String sql = "SELECT c.comment_id AS commentId, c.ep_id, c.comment_contents AS commentContents, " +
                "       c.created_at AS createdAt, u.nickname, u.userid AS userId, c.PARENT_COMMENT_ID," + // userno 추가
                "       COALESCE(l.is_liked, 0) AS isLiked, " +
                "       (SELECT COUNT(*) FROM comment_likes cl WHERE cl.comment_id = c.comment_id) AS likeCount, " +
                "       (SELECT COUNT(*) FROM comments r WHERE r.parent_comment_id = c.comment_id) AS replyCount " +
                "FROM comments c " +
                "LEFT JOIN users u ON c.userno = u.userno " +
                "LEFT JOIN (SELECT comment_id, 1 AS is_liked FROM comment_likes WHERE userno = ?) l " +
                "ON c.comment_id = l.comment_id " +
                "WHERE c.userno = ?";

        List<CommentDTO> comments = jdbcTemplate.query(sql, new Object[]{viewerUserno, profileUserno}, (rs, rowNum) -> {
            CommentDTO comment = new CommentDTO();
            comment.setCommentId(rs.getInt("commentId"));
            comment.setEpId(rs.getInt("ep_id"));
            comment.setCommentContents(rs.getString("commentContents"));
            comment.setCreatedAt(rs.getDate("createdAt"));
            comment.setNickname(rs.getString("nickname"));
            comment.setParentCommentId(rs.getInt("PARENT_COMMENT_ID"));
            comment.setIsLiked(rs.getInt("isLiked"));
            comment.setLikeCount(rs.getInt("likeCount"));
            comment.setReplyCount(rs.getInt("replyCount"));
            comment.setUserId(rs.getString("userId")); // userId 설정
            return comment;
        });


        return comments;
    }




    public List<NovelVO> getNovelsByAuthor(Long viewerUserno, Long profileUserno) {
        String sql = "SELECT nv.nv_id, nv.nv_title, nv.nv_desc, nv.cover_path, nv.updated_at, nv.is_adult, nv.is_secret, " +
                "u.nickname, " +
                "(SELECT COUNT(*) FROM novel_subscribe WHERE nv_id = nv.nv_id) AS subscribeCount, " +
                "COALESCE((SELECT 1 FROM novel_subscribe WHERE userno = ? AND nv_id = nv.nv_id), 0) AS isSubscribed, " +
                "COALESCE(SUM(lk.likeCount), 0) AS likeCount, " +
                "COALESCE(SUM(co.commentCount), 0) AS commentCount, " +
                "COALESCE(SUM(ev.ep_views), 0) AS totalViews " +
                "FROM novels nv " +
                "JOIN users u ON nv.userno = u.userno " +
                "LEFT JOIN episode ev ON nv.nv_id = ev.nv_id " +
                "LEFT JOIN (SELECT ep_id, COUNT(*) AS likeCount FROM ep_likes GROUP BY ep_id) lk ON ev.ep_id = lk.ep_id " +
                "LEFT JOIN (SELECT ep_id, COUNT(*) AS commentCount FROM comments GROUP BY ep_id) co ON ev.ep_id = co.ep_id " +
                "WHERE nv.userno = ? AND nv.is_secret = 0 " +
                "GROUP BY nv.nv_id, nv.nv_title, nv.nv_desc, nv.cover_path, nv.updated_at, nv.is_adult, nv.is_secret, u.nickname";

        List<NovelVO> list = jdbcTemplate.query(sql, new Object[]{viewerUserno, profileUserno}, (rs, i) ->
                new NovelVO(
                        rs.getInt("nv_id"),
                        rs.getString("nv_title"),
                        rs.getString("nv_desc"),
                        rs.getString("nickname"),
                        rs.getDate("updated_at"),
                        rs.getInt("is_adult"),
                        rs.getInt("is_secret"),
                        rs.getString("cover_path"),
                        rs.getInt("subscribeCount"),
                        rs.getInt("isSubscribed"),
                        rs.getInt("likeCount"),
                        rs.getInt("commentCount"),
                        rs.getInt("totalViews")
                )
        );

        return list;
    }


    public Long getUserno(String nickname) {
        String sql = "SELECT userno FROM users WHERE nickname = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, nickname);
    }

    public boolean insertUserVerification(boolean insertAdult, java.sql.Date birth, String gender, String username, Long userno) {
        int verify;
        if(insertAdult) verify = 1;
        else verify = 0;

        int sex;
        if(gender.equals("female")) sex = 0;
        else sex = 0;

        String sql = "UPDATE users " +
                     "SET birth = ?, gender = ?, username = ?, is_adult = ?" +
                     "WHERE userno = ?";

        int rowsAffected = jdbcTemplate.update(sql, birth, sex, username, verify, userno);
        return rowsAffected > 0;
    }

    public boolean removeAuthorSubscribe(Long authorno, Long userno) {
        String sql = "DELETE FROM author_subscribe WHERE authorno = ? AND userno = ?";
        return jdbcTemplate.update(sql, authorno, userno) > 0;
    }

    public boolean addAuthorSubscribe(Long authorno, Long userno) {
        String sql = "INSERT INTO author_subscribe (authorno, userno, created_at) VALUES (?, ?, SYSDATE)";
        return jdbcTemplate.update(sql, authorno, userno) > 0;
    }

    public UserVO getSubscribeStatus(Long authorno, Long userno) {
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM author_subscribe WHERE authorno = ?) AS subscribeCount, " +
                "COALESCE((SELECT 1 FROM author_subscribe WHERE userno = ? AND authorno = ?), 0) AS isSubscribed " +
                "FROM dual"; // 오라클에서는 dual 사용

        UserVO uv = jdbcTemplate.queryForObject(sql, (rs, i) -> {
            return new UserVO(
                    rs.getInt("subscribeCount"),
                    rs.getInt("isSubscribed")
            );
        }, authorno, userno, authorno);

        return uv;
    }

    public String getUserProfileImage(Long userno) {
        String sql = "SELECT profileimg FROM users WHERE userno = ?";
        return jdbcTemplate.queryForObject(sql, String.class, userno);
    }


}
