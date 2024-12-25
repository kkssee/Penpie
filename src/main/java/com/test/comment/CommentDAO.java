package com.test.comment;

import com.test.novel.CommentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CommentDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 현재 댓글 중에서 top 5를 가져오는 메서드
    public List<CommentDTO> getTopComments() {
        String sql = "SELECT c.comment_id, c.ep_id, c.comment_contents, c.PARENT_COMMENT_ID,u.nickname, " +
                "(SELECT COUNT(*) FROM comment_likes cl WHERE cl.comment_id = c.comment_id) AS like_count " +
                "FROM comments c " +
                "JOIN users u ON c.userno = u.userno " +
                "WHERE c.created_at >= SYSDATE - 1 " + // 최근 24시간 내의 댓글
                "ORDER BY like_count DESC " + // 좋아요 수 기준으로 정렬
                "FETCH FIRST 3 ROWS ONLY"; // 상위 3개 가져오기

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CommentDTO comment = new CommentDTO();
            comment.setCommentId(rs.getInt("comment_id"));
            comment.setEpId(rs.getInt("ep_id"));
            comment.setCommentContents(rs.getString("comment_contents"));
            comment.setNickname(rs.getString("nickname"));
            comment.setLikeCount(rs.getInt("like_count"));
            comment.setParentCommentId(rs.getInt("parent_comment_id"));
            return comment;
        });
    }

}
