package com.test.novel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class NovelDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int addNovel(NovelVO nv) {
        String sql = "INSERT INTO novels (nv_id, nv_title, nv_desc, userno, created_at, updated_at, is_adult, is_secret, cover_path, is_completed)"
                + "VALUES(NV_SEQ.NEXTVAL, ?, ?, ?, SYSDATE, SYSDATE, ?, ?, ?, 0)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn-> {
            PreparedStatement pstmt;
            pstmt = conn.prepareStatement(sql, new String[] {"nv_id"});
            pstmt.setString(1, nv.getNv_title());
            pstmt.setString(2, nv.getNv_desc());
            pstmt.setLong(3, nv.getUserno());
            pstmt.setInt(4, nv.getIs_adult());
            pstmt.setInt(5, nv.getIs_secret());
            pstmt.setString(6, nv.getCover_path());
            return pstmt;
        }, keyHolder);

        String sql2 = "INSERT INTO nvctg_list (nv_id, nvctg_id)"
                     + "VALUES (? , (SELECT nvctg_id FROM nvctg WHERE nvctg_name = ? ))";
        jdbcTemplate.update(sql2, keyHolder.getKey().intValue(), nv.getNvctg_name());

        for (String tagName : nv.getNvtag_name()) {
            String sqlInsertNvtagList = "INSERT INTO nvtag_list (nv_id, nvtag_id) VALUES (?, ?)";
            jdbcTemplate.update(sqlInsertNvtagList, keyHolder.getKey().intValue(), getOrCreateTagId(tagName));
        } return keyHolder.getKey().intValue();
    }

    private Integer getOrCreateTagId(String tagName) {
        Integer nvtag_id = null;
        String sqlCheckTagExists = "SELECT nvtag_id FROM nvtag WHERE nvtag_name = ?";

        try {
            nvtag_id = jdbcTemplate.queryForObject(sqlCheckTagExists, Integer.class, tagName);
        } catch (EmptyResultDataAccessException e) {
            String sqlCreateTag = "INSERT INTO nvtag (nvtag_id, nvtag_name) VALUES (nvtag_seq.NEXTVAL, ?)";
            KeyHolder tagKeyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement pstmt = conn.prepareStatement(sqlCreateTag, new String[] {"nvtag_id"});
                pstmt.setString(1, tagName);
                return pstmt;
            }, tagKeyHolder);
            nvtag_id = tagKeyHolder.getKey().intValue();
        } return nvtag_id;
    }

   public List<NovelVO> getList(Long userno) {
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
               "WHERE nv.is_secret != 1 " +
               "GROUP BY nv.nv_id, nv.nv_title, nv.nv_desc, nv.cover_path, nv.updated_at, nv.is_adult, nv.is_secret, u.nickname";

       List<NovelVO> list = jdbcTemplate.query(sql, new Object[]{userno}, (rs, i) ->
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



    public NovelVO getNovel(int nv_id, Long userno) {
        String sql = "SELECT nv.nv_id, nv.nv_title, nv.nv_desc, nv.cover_path, nv.updated_at, nv.is_adult, nv.is_secret, " +
                "u.nickname, " +
                "(SELECT COUNT(*) FROM novel_subscribe WHERE nv_id = nv.nv_id) AS subscribeCount, " +
                "COALESCE((SELECT 1 FROM novel_subscribe WHERE userno = ? AND nv_id = nv.nv_id), 0) AS isSubscribed " +
                "FROM novels nv " +
                "JOIN users u ON nv.userno = u.userno " +
                "WHERE nv.nv_id = ?";

        NovelVO nv = jdbcTemplate.queryForObject(sql, (rs, i) -> {
            return new NovelVO(
                    rs.getInt("nv_id"),
                    rs.getString("nv_title"),
                    rs.getString("nv_desc"),
                    rs.getString("nickname"),
                    rs.getDate("updated_at"),
                    rs.getInt("is_adult"),
                    rs.getInt("is_secret"),
                    rs.getString("cover_path"),
                    rs.getInt("subscribeCount"),
                    rs.getInt("isSubscribed")
            );
        }, userno, nv_id);


        String sqlGetCtg = "SELECT nt.nvctg_name FROM nvctg nt " +
                "JOIN nvctg_list nvl ON  nt.nvctg_id = nvl.nvctg_id " +
                "JOIN novels nv ON nv.nv_id = nvl.nv_id " +
                "WHERE nv.nv_id = ?";
        String ctg = jdbcTemplate.queryForObject(sqlGetCtg, String.class, nv_id);

        String sqlGetTags = "SELECT nt.nvtag_name FROM nvtag nt \n" +
                "JOIN nvtag_list nvl ON  nt.nvtag_id = nvl.nvtag_id\n" +
                "JOIN novels nv ON nv.nv_id = nvl.nv_id \n" +
                "WHERE nv.nv_id = ?";
        List<String> tagsList = jdbcTemplate.query(sqlGetTags, new Object[]{nv_id},
                (rs, rowNum) -> rs.getString("NVTAG_NAME"));
        String[] tags = tagsList.toArray(new String[0]);

        // 쿼리 실행 및 Map<String, Object> 반환
        String sqlEpStats = "SELECT ev.nv_id, " +
                "COALESCE(SUM(lk.likeCount), 0) AS likeCount, " +
                "COALESCE(SUM(co.commentCount), 0) AS commentCount, " +
                "COALESCE(SUM(ev.ep_views), 0) AS totalViews " +
                "FROM episode ev " +
                "LEFT JOIN (SELECT ep_id, COUNT(*) AS likeCount FROM ep_likes GROUP BY ep_id) lk ON ev.ep_id = lk.ep_id " +
                "LEFT JOIN (SELECT ep_id, COUNT(*) AS commentCount FROM comments GROUP BY ep_id) co ON ev.ep_id = co.ep_id " +
                "WHERE ev.nv_id = ? " +
                "GROUP BY ev.nv_id";

        Map<String, Object> epStats;
        try {
            epStats = jdbcTemplate.queryForMap(sqlEpStats, nv_id);
        } catch (EmptyResultDataAccessException e) {
            // 결과가 없을 경우 빈 Map으로 초기화
            epStats = new HashMap<>();
        }

// Map에서 값을 추출하고 변환
        int likeCount = 0;
        int commentCount = 0;
        int totalViews = 0;

        if (epStats.get("likeCount") != null) {
            likeCount = ((Number) epStats.get("likeCount")).intValue();
        }
        if (epStats.get("commentCount") != null) {
            commentCount = ((Number) epStats.get("commentCount")).intValue();
        }
        if (epStats.get("totalViews") != null) {
            totalViews = ((Number) epStats.get("totalViews")).intValue();
        }

// 소설 정보에 에피소드 통계 추가
        nv.setLikeCount(likeCount);
        nv.setCommentCount(commentCount);
        nv.setTotalViews(totalViews);

        nv.setNvctg_name(ctg);
        nv.setNvtag_name(tags);

        return nv;
    }

    public NovelVO novelDetails(int novelId) {
        String sql = "SELECT * FROM novels WHERE nv_id = ?";
        /*Emp e = jdbcTemplate.queryForObject(sql, (rs, i) ->
                new Emp(rs.getInt("EMPNO"), rs.getString("ENAME"), rs.getString("JOB"), rs.getInt("SAL"), rs.getDate("HIREDATE")), empno);
        return e;*/
        return null;
    }

    public boolean novelUpdate(int novelId) {
        return false;
    }

    public boolean novelDelete(int novelId) {
        return false;
    }

    public List<EpisodeVO> getEpList(int nv_id, String sortOrder) {
        String sql = "SELECT ROW_NUMBER() OVER (ORDER BY ev.ep_date ASC) AS episode_number, " +
                "ev.ep_id, ev.ep_title, ev.ep_contents, ev.ep_date, ev.ep_views, ev.price, " +
                "COUNT(co.ep_id) AS commentCount " +
                "FROM episode ev " +
                "LEFT JOIN comments co ON ev.ep_id = co.ep_id " +
                "JOIN Novels nv ON ev.nv_id = nv.nv_id " +
                "WHERE nv.nv_id = ? " +
                "GROUP BY ev.ep_id, ev.ep_title, ev.ep_contents, ev.ep_date, ev.ep_views, ev.price " +
                "ORDER BY ev.ep_date " + sortOrder;

        return jdbcTemplate.query(sql, (rs, i) ->
                new EpisodeVO(
                        rs.getInt("ep_id"),
                        rs.getString("ep_title"),
                        rs.getString("ep_contents"),
                        rs.getDate("ep_date"),
                        rs.getInt("ep_views"),
                        rs.getInt("commentCount"),
                        rs.getInt("episode_number"), // 에피소드 번호 추가
                        rs.getInt("price") // 에피소드 번호 추가
                ), nv_id);
    }


    public EpisodeVO getEp(int ep_id, Long userno) {
        String sql = "SELECT * FROM (\n" +
                "    SELECT ep.ep_id, ep.nv_id, ep.ep_title, ep.ep_contents, ep.ep_date, ep.ep_views, \n" +
                "           COALESCE(l.is_liked, 0) AS is_liked, \n" +
                "           (SELECT COUNT(*) FROM ep_likes el WHERE el.ep_id = ep.ep_id) AS like_count, \n" +
                "           ROW_NUMBER() OVER (PARTITION BY ep.nv_id ORDER BY ep.ep_date ASC) AS episode_number \n" +
                "    FROM episode ep \n" +
                "    LEFT JOIN (SELECT ep_id, 1 AS is_liked FROM ep_likes WHERE userno = ?) l \n" +
                "    ON l.ep_id = ep.ep_id\n" +
                ") subquery \n" +
                "WHERE subquery.ep_id = ?";

        EpisodeVO ev = jdbcTemplate.queryForObject(sql, (rs, i) ->
                new EpisodeVO(
                        rs.getInt("ep_id"),
                        rs.getInt("nv_id"),
                        rs.getString("ep_title"),
                        rs.getString("ep_contents"),
                        rs.getDate("ep_date"),
                        rs.getInt("ep_views"),
                        rs.getInt("is_liked"),
                        rs.getInt("like_count"),
                        rs.getInt("episode_number") // episode_number 추가
                ), userno, ep_id);
        return ev;
    }


    public int addEp(EpisodeVO ev) {
        String sql = "INSERT INTO episode (ep_id, nv_id, ep_title, ep_contents, ep_date, ep_views)"
                + "VALUES(EP_SEQ.NEXTVAL, ?, ?, ?, SYSDATE, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn-> {
            PreparedStatement pstmt;
            pstmt = conn.prepareStatement(sql, new String[] {"ep_id"});
            pstmt.setInt(1, ev.getNv_id());
            pstmt.setString(2, ev.getEp_title());
            pstmt.setString(3, ev.getEp_contents());
            pstmt.setInt(4, ev.getEp_views());
            return pstmt;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public boolean editNovel(NovelVO nv) {
        String sql = "UPDATE novels SET is_secret = ?, is_adult = ?, nv_title = ?, " +
                                        "nv_desc = ?, cover_path = ?, updated_at = ?, " +
                "is_completed = ? " +
                "WHERE nv_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, nv.getIs_secret(), nv.getIs_adult(), nv.getNv_title(),
                                                nv.getNv_desc(), nv.getCover_path(), nv.getUpdated_at(), nv.getIs_completed(), nv.getNv_id());
        return rowsAffected > 0;
    }

    public boolean editCtg(NovelVO nv) {
        String sql = "UPDATE nvctg_list SET nvctg_id = (SELECT nvctg_id FROM nvctg WHERE nvctg_name = ? ) WHERE nv_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, nv.getNvctg_name(), nv.getNv_id());
        return rowsAffected > 0;
    }

    public String getCover(int nv_id) {
        String sql = "SELECT cover_path FROM novels WHERE nv_id = ?";
        String cover_path = jdbcTemplate.queryForObject(sql, String.class, nv_id);
        return cover_path;
    }

    public void addNovelTag(int nvId, String tagName) {
        String sqlInsertNvtagList = "INSERT INTO nvtag_list (nv_id, nvtag_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlInsertNvtagList, nvId, getOrCreateTagId(tagName));
    }
    public void removeNovelTag(int novelId, String tagName) {
        String sqlDeleteTag = "DELETE FROM nvtag_list WHERE nv_id = ? AND nvtag_id = (SELECT nvtag_id FROM nvtag WHERE nvtag_name = ?)";
        jdbcTemplate.update(sqlDeleteTag, novelId, tagName);
    }
    public void removeOrphanTags() {
        // 자식이 없는 태그(즉, nvtag_list 테이블에 존재하지 않는 태그)를 삭제하는 쿼리
        String sqlDeleteOrphanTags = "DELETE FROM nvtag WHERE nvtag_id NOT IN (SELECT nvtag_id FROM nvtag_list)";
        jdbcTemplate.update(sqlDeleteOrphanTags);
    }

    public boolean editEp(EpisodeVO ev) {
        String sql = "UPDATE episode SET ep_title = ?, ep_contents = ? WHERE ep_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, ev.getEp_title(), ev.getEp_contents(), ev.getEp_id());
        return rowsAffected > 0;
    }

    public List<CommentVO> getCommentList(int ep_id) {
        String sql = "SELECT co.comment_id, co.ep_id, co.comment_contents, co.created_at, co.is_updated, " +
                "u.nickname " +
                "FROM comments co " +
                "JOIN users u ON co.userno = u.userno " +
                "WHERE co.ep_id = ? AND co.parent_comment_id = 0";

        return jdbcTemplate.query(sql, (rs, i) ->
                new CommentVO(
                        rs.getInt("comment_id"),
                        rs.getInt("ep_id"),
                        rs.getString("comment_contents"),
                        rs.getDate("created_at"),
                        rs.getInt("is_updated"),
                        rs.getString("nickname")
                ), ep_id);
    }


    public int addComment(CommentDTO cv) {
        String sql = "INSERT INTO comments (comment_id, ep_id, userno, comment_contents, created_at, is_updated, parent_comment_id)"
                + " VALUES (COMMENT_SEQ.NEXTVAL, ?, ?, ?, SYSDATE, 0, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement pstmt = conn.prepareStatement(sql, new String[] {"comment_id"});
            pstmt.setInt(1, cv.getEpId());
            pstmt.setLong(2, cv.getUserno());
            pstmt.setString(3, cv.getCommentContents());
            pstmt.setInt(4, cv.getParentCommentId());
            return pstmt;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    public List<CommentVO> getReplyList(int commentId) {
        String sql = "SELECT co.comment_id, co.ep_id, co.comment_contents, co.created_at, co.is_updated, " +
                "co.parent_comment_id, u.nickname " +
                "FROM comments co " +
                "JOIN users u ON co.userno = u.userno " +
                "WHERE co.parent_comment_id = ?";

        return jdbcTemplate.query(sql, (rs, i) ->
                new CommentVO(
                        rs.getInt("comment_id"),
                        rs.getInt("ep_id"),
                        rs.getString("comment_contents"),
                        rs.getDate("created_at"),
                        rs.getInt("is_updated"),
                        rs.getInt("parent_comment_id"),
                        rs.getString("nickname")
                ), commentId);
    }

    public Long getUserno(String userid) {
        String sql = "SELECT userno FROM users WHERE userid = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, userid);
        } catch (EmptyResultDataAccessException e) {
            // 사용자 없는 경우 처리
            throw new RuntimeException("사용자를 찾을 수 없습니다.", e);
        }
    }

    public List<NovelVO> getMyNovelList(Long userno) {
        String sql = "SELECT * FROM novels nv JOIN users u ON nv.userno = u.userno WHERE nv.userno = ?";
        //String sql = "SELECT * FROM novels WHERE userno = ?";
        List<NovelVO> myList = jdbcTemplate.query(sql, (rs, i) ->
                new NovelVO(rs.getInt("nv_id"), rs.getString("nv_title"), rs.getString("nv_desc"), rs.getString("nickname"), rs.getDate("updated_at"), rs.getInt("is_adult"), rs.getInt("is_secret"), rs.getString("cover_path")), userno);
        return myList;
    }

    public CommentDTO getComment(int comment_id, Long userno) {
        String sql = "SELECT c.comment_id, c.ep_id, c.comment_contents, c.created_at, u.userno, u.nickname, \n" +
                "       COALESCE(l.is_liked, 0) AS is_liked, \n" +
                "       (SELECT COUNT(*) FROM comment_likes cl WHERE cl.comment_id = c.comment_id) AS like_count\n" +
                "FROM comments c \n" +
                "LEFT JOIN users u ON c.userno = u.userno\n" +
                "LEFT JOIN (SELECT comment_id, 1 AS is_liked FROM comment_likes WHERE userno = ?) l\n" +
                "ON c.comment_id = l.comment_id\n" +
                "WHERE c.comment_id = ?";

        return jdbcTemplate.queryForObject(sql, new Object[]{userno, comment_id}, (rs, rowNum) -> {
            CommentDTO comment = new CommentDTO();
            comment.setCommentId(rs.getInt("comment_id"));
            comment.setEpId(rs.getInt("ep_id"));
            comment.setCommentContents(rs.getString("comment_contents"));
            comment.setCreatedAt(rs.getDate("created_at"));
            comment.setUserno(rs.getLong("userno"));
            comment.setNickname(rs.getString("nickname"));
            comment.setIsLiked(rs.getInt("is_liked"));
            comment.setLikeCount(rs.getInt("like_count"));

            return comment;
        });
    }


    public List<CommentDTO> getCommentsWithLikes(int epId, Long userno) {
        String sql = "SELECT c.comment_id, c.ep_id, c.comment_contents, c.created_at, u.userno, u.nickname, \n" +
                "       COALESCE(l.is_liked, 0) AS is_liked, \n" +
                "       (SELECT COUNT(*) FROM comment_likes cl WHERE cl.comment_id = c.comment_id) AS like_count,\n" +
                "(SELECT COUNT(*) FROM comments r WHERE r.parent_comment_id = c.comment_id) AS reply_count \n" +
                "FROM comments c \n" +
                "LEFT JOIN users u ON c.userno = u.userno\n" +
                "LEFT JOIN (SELECT comment_id, 1 AS is_liked FROM comment_likes WHERE userno = ?) l\n" +
                "ON c.comment_id = l.comment_id\n" +
                "WHERE c.ep_id = ? AND c.parent_comment_id = 0";
        return jdbcTemplate.query(sql, new Object[]{userno, epId}, (rs, rowNum) -> {
            CommentDTO commentDTO = new CommentDTO();
            commentDTO.setCommentId(rs.getInt("comment_id"));
            commentDTO.setEpId(rs.getInt("ep_id"));
            commentDTO.setCommentContents(rs.getString("comment_contents"));
            commentDTO.setCreatedAt(rs.getDate("created_at"));
            commentDTO.setUserno(rs.getLong("userno"));
            commentDTO.setNickname(rs.getString("nickname"));
            commentDTO.setIsLiked(rs.getInt("is_liked"));
            commentDTO.setLikeCount(rs.getInt("like_count"));
            commentDTO.setReplyCount(rs.getInt("reply_count"));
            return commentDTO;
        });

    }


    public boolean addLike(int commentId, Long userno) {
        String sql = "INSERT INTO comment_likes (comment_id, userno, created_at) VALUES (?, ?, SYSDATE)";
        return jdbcTemplate.update(sql, commentId, userno) > 0;
    }

    public boolean removeLike(int commentId, Long userno) {
        String sql = "DELETE FROM comment_likes WHERE comment_id = ? AND userno = ?";
        return jdbcTemplate.update(sql, commentId, userno) > 0;
    }

    public List<CommentDTO> getReplyWithLikes(int commentId, Long userno) {
        String sql = "SELECT c.comment_id, c.ep_id, c.comment_contents, c.created_at, u.userno, u.nickname, \n" +
                "       COALESCE(l.is_liked, 0) AS is_liked, \n" +
                "       (SELECT COUNT(*) FROM comment_likes cl WHERE cl.comment_id = c.comment_id) AS like_count\n" +
                "FROM comments c \n" +
                "LEFT JOIN users u ON c.userno = u.userno\n" +
                "LEFT JOIN (SELECT comment_id, 1 AS is_liked FROM comment_likes WHERE userno = ?) l\n" +
                "ON c.comment_id = l.comment_id\n" +
                "WHERE c.parent_comment_id = ?";  // 대댓글을 가져오기 위해 parent_comment_id로 필터링

        return jdbcTemplate.query(sql, new Object[]{userno, commentId}, (rs, rowNum) -> {
            CommentDTO commentDTO = new CommentDTO();
            commentDTO.setCommentId(rs.getInt("comment_id"));
            commentDTO.setEpId(rs.getInt("ep_id"));  // 대댓글이 속한 댓글의 ep_id도 설정
            commentDTO.setCommentContents(rs.getString("comment_contents"));
            commentDTO.setCreatedAt(rs.getDate("created_at"));
            commentDTO.setUserno(rs.getLong("userno"));
            commentDTO.setNickname(rs.getString("nickname"));
            commentDTO.setIsLiked(rs.getInt("is_liked"));
            commentDTO.setLikeCount(rs.getInt("like_count"));
            return commentDTO;
        });
    }

    public int delComment(int commentId) {
        String sql = "DELETE FROM comments WHERE comment_id = ?";
        return jdbcTemplate.update(sql, commentId);
    }

    public int delReplies(int commentId) {
        String sql = "DELETE FROM comments WHERE parent_comment_id = ?";
        return jdbcTemplate.update(sql, commentId);
    }

    public boolean removeEpLike(int epId, Long userno) {
        String sql = "DELETE FROM ep_likes WHERE ep_id = ? AND userno = ?";
        return jdbcTemplate.update(sql, epId, userno) > 0;
    }

    public boolean addEpLike(int epId, Long userno) {
        String sql = "INSERT INTO ep_likes (ep_id, userno) VALUES (?, ?)";
        return jdbcTemplate.update(sql, epId, userno) > 0;
    }

    public boolean removeNvSubscribe(int nvId, Long userno) {
        String sql = "DELETE FROM NOVEL_SUBSCRIBE WHERE nv_id = ? AND userno = ?";
        return jdbcTemplate.update(sql, nvId, userno) > 0;
    }

    public boolean addNvSubscribe(int nvId, Long userno) {
        String sql = "INSERT INTO NOVEL_SUBSCRIBE (nv_id, userno, created_at) VALUES (?, ?, SYSDATE)";
        return jdbcTemplate.update(sql, nvId, userno) > 0;
    }

    public boolean incrementViewCount(int epId) {
        String sql = "UPDATE episode SET ep_views = ep_views + 1 WHERE ep_id = ?";
        return jdbcTemplate.update(sql, epId) > 0;
    }


    // 현재 댓글 중에서 top 5를 가져오는 메서드
    public List<CommentDTO> getBestComments() {
        String sql = "SELECT c.comment_id, c.ep_id, c.comment_contents, u.nickname, " +
                "(SELECT COUNT(*) FROM comment_likes cl WHERE cl.comment_id = c.comment_id) AS like_count " +
                "FROM comments c " +
                "JOIN users u ON c.userno = u.userno " +
                "WHERE EP_ID = ?" + // 최근 24시간 내의 댓글
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

    public int getFirstEpisodeId(int nvId) {
        String sql = "SELECT ep_id FROM (SELECT ep_id FROM episode WHERE nv_id = ? ORDER BY ep_date ASC) WHERE ROWNUM = 1";
        return jdbcTemplate.queryForObject(sql, new Object[]{nvId}, Integer.class);
    }

    public List<EpisodeVO> getViewHistory(Long userno, int nvId) {

        if(userno > 0) {
            // 인증된 사용자에 대한 쿼리
            String sql = "SELECT " +
                    "ev.episode_number, " + // episode_number는 getEpList에서 가져온 결과 사용
                    "ev.ep_id, " +
                    "ev.ep_title " +
                    "FROM ( " +
                    "   SELECT ROW_NUMBER() OVER (ORDER BY ev.ep_date ASC) AS episode_number, " +
                    "   ev.ep_id, ev.ep_title " +
                    "   FROM episode ev " +
                    "   WHERE ev.nv_id = ? " +  // 특정 nv_id에 해당하는 에피소드
                    ") ev " +
                    "JOIN view_history vh ON ev.ep_id = vh.ep_id " +
                    "WHERE vh.userno = ? " + // 사용자의 view_history에 해당하는 에피소드
                    "ORDER BY vh.rdate ASC"; // 사용자 view history의 rdate로 정렬

            return jdbcTemplate.query(sql, new Object[]{nvId, userno}, (rs, rowNum) -> {
                EpisodeVO episode = new EpisodeVO();
                episode.setEp_id(rs.getInt("ep_id"));
                episode.setEpisode_number(rs.getInt("episode_number")); // 에피소드 번호 설정
                episode.setEp_title(rs.getString("ep_title"));
                return episode;
            });
        } else {
            String guestSql = "SELECT " +
                    "ROW_NUMBER() OVER (ORDER BY ev.ep_date ASC) AS episode_number, " +
                    "ev.ep_id, ev.ep_title " +
                    "FROM episode ev " +
                    "WHERE ev.nv_id = ? " +  // 특정 nv_id에 해당하는 에피소드
                    "ORDER BY ev.ep_date ASC"; // 에피소드 생성일로 정렬

            return jdbcTemplate.query(guestSql, new Object[]{nvId}, (rs, rowNum) -> {
                EpisodeVO episode = new EpisodeVO();
                episode.setEp_id(rs.getInt("ep_id"));
                episode.setEpisode_number(rs.getInt("episode_number")); // 에피소드 번호 설정
                episode.setEp_title(rs.getString("ep_title"));
                return episode;
            });
        }

    }




    public Optional<Integer> checkHistory(Long userno, int epId) {
        String checkSql = "SELECT COUNT(*) FROM view_history WHERE userno = ? AND ep_id = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(checkSql, new Object[]{userno, epId}, Integer.class);
            return Optional.ofNullable(count);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty(); // 결과가 없을 경우 Optional.empty() 반환
        }
    }

    public boolean recordViewHistory(Long userno, int epId) {
        String insertSql = "INSERT INTO view_history (userno, nv_id, ep_id, rDate) " +
                "VALUES (?, (SELECT nv_id FROM EPISODE WHERE ep_id = ?), ?, SYSDATE)";
        return jdbcTemplate.update(insertSql, userno, epId, epId) > 0;
    }

    public boolean updateViewHistory(Long userno, int epId) {
        String updateSql = "UPDATE view_history SET rDate = SYSDATE WHERE userno = ? AND ep_id = ?";
        return jdbcTemplate.update(updateSql, userno, epId) > 0;
    }
}
