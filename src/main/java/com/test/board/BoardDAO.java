package com.test.board;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class BoardDAO {
    @Autowired
    BoardMapper boardMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public int addPost(Post p) {
        boardMapper.addPost(p);
        return boardMapper.getGeneratedPostId(p);
    }

    public Post getPost(int postId) {
        return boardMapper.getPost(postId);
    }

    public List<Post> getList(Board board) {
        log.info(board.toString());
        List<Post> list = boardMapper.getList(board);
        log.info("리스트 확인"+list.toString());
        return list;
    }

    public int updatePost(Post post) {
        return boardMapper.updatePost(post);
    }

    @Transactional
    public int deleteBoardImages(List<Integer> imgIdList) {
        return boardMapper.deleteBoardImages(imgIdList);
    }


    @Transactional
    public int deletePost(int postid) {
        int row1 = boardMapper.deletePost(postid);
        int row3 = boardMapper.deleteBComment(postid);
        int row4 = row1 + row3;
        if(row4 > 0){
            return row4;
        }
        return 0;
    }

    public int inputLike(int postid) {
        return boardMapper.inputLike(postid);
    }

    public int inputViews(int postid) {
        return boardMapper.inputViews(postid);
    }

    //gpt검사
    public List<Post> freelistByTitle(String keyword) {
        return boardMapper.freelistByTitle(keyword);
        /*
        String sql = "SELECT p.post_id, p.post_title, p.post_date, p.views, p.plike, " +
                "COUNT(c.b_comment_id) AS tcoment, u.nickname " +
                "FROM post p " +
                "LEFT JOIN b_comment c ON p.post_id = c.post_id " +
                "INNER JOIN users u ON u.userno = p.user_no " +
                "WHERE p.post_title LIKE ? AND p.b_category_id = 1 " +
                "GROUP BY p.post_id, p.post_title, p.post_date, p.views, p.plike, u.nickname " +
                "ORDER BY p.post_id DESC";

        List<Post> list = jdbcTemplate.query(
                sql,
                new Object[] { "%" + keyword + "%" },
                (rs, i) -> {
                    Post post = new Post();
                    post.setPostid(rs.getInt("POST_ID"));
                    post.setTitle(rs.getString("POST_TITLE"));
                    post.setDate(rs.getDate("POST_DATE"));
                    post.setViews(rs.getInt("VIEWS"));
                    post.setPlike(rs.getInt("PLIKE"));
                    post.setTcoment(rs.getInt("TCOMENT"));
                    post.setNickname(rs.getString("NICKNAME"));
                    return post;
                }
        );
        return list;*/
    }

    public List<Post> freelistByTitleANDCont(String keyword) {
        return boardMapper.freelistByTitleANDCont(keyword);
    }

    public List<Post> freelistByCont(String keyword) {
        return boardMapper.freelistByCont(keyword);
    }

    public List<Post> replylistByTitle(String keyword) {
        return boardMapper.replylistByTitle(keyword);
    }

    public List<Post> replylistByTitleANDCont(String keyword) {
        return boardMapper.replylistByTitleANDCont(keyword);
    }

    public List<Post> replylistByCont(String keyword) {
        return boardMapper.replylistByCont(keyword);
    }

    public void saveComment(HttpServletRequest req, HashMap<String, Object> param) {
        // 파라미터 값 추출
        int userNo = Integer.parseInt((String) param.get("user_no"));
        String comment = (String) param.get("b_comment");
        int postId = Integer.parseInt((String) param.get("post_id"));
        java.sql.Date createDt = (java.sql.Date) param.get("create_dt"); // java.sql.Date로 변환

        boardMapper.saveComment(postId, userNo, comment, createDt);
    }

    public Map<String, Object> selectComment(HashMap<String, Object> param) {
        int bCommentId = Integer.parseInt((String) param.get("parentno"));
        return boardMapper.selectComment(bCommentId);
    }

    public int selectMaxOrderNo(HashMap<String, Object> param) {
        int postid = Integer.parseInt((String) param.get("post_id"));
        int groupno = (Integer)param.get("groupno");
        log.info("selectMaxOrderNo 번호: " + postid +'/'+groupno);

        return boardMapper.selectMaxOrderNo(postid,groupno);
    }

    public int countChild(HashMap<String, Object> param) {
        int parentId = Integer.parseInt((String) param.get("parentno"));

        return boardMapper.countChild(parentId);
    }

    //댓글 순서 업데이트
    public void updateComentByNo(HashMap<String, Object> param) {
        int postId = Integer.parseInt((String) param.get("post_id"));
        // groupno의 타입이 Integer일 경우 바로 변환
        int groupNo = 0; // 기본값 설정
        int orderNo = 0;
        Object groupnoObj = param.get("groupno");
        Object ordernoObj = param.get("orderno");
// groupno가 Integer라면 Integer로 변환, 그렇지 않다면 String으로 처리
        if (groupnoObj instanceof Integer) {
            groupNo = (Integer) groupnoObj;
        } else if (groupnoObj instanceof String) {
            groupNo = Integer.parseInt((String) groupnoObj);
        }

        if (ordernoObj instanceof Integer) {
            orderNo = (Integer) ordernoObj;
        } else if (ordernoObj instanceof String) {
            orderNo = Integer.parseInt((String) ordernoObj);
        }

        boardMapper.updateCommentByNo(postId, groupNo, orderNo);
    }

    public void insetReply(HttpServletRequest req, HashMap<String, Object> param) {
        int postId = Integer.parseInt((String) param.get("post_id"));
        int userNo = Integer.parseInt((String) param.get("user_no"));
        int parentNo = Integer.parseInt((String) param.get("parentno"));
        String comment = (String) param.get("b_comment");
        java.sql.Date createDt = (java.sql.Date)param.get("create_dt");
        int groupno = (Integer)param.get("groupno");
        int depthno = (Integer)param.get("depthNo");
        int orderno = (Integer)param.get("orderNo");
        log.info("대댓글 정보일부"+postId +'/'+userNo+'/'+parentNo+'/'+comment+'/'+groupno +'/'+orderno);

        boardMapper.insetReply(postId,userNo,parentNo,comment,createDt,groupno,depthno,orderno);
    }

    public List<BComment> getCommentList(int postId) {
        return boardMapper.getCommentList(postId);
    }

    public int deletecomment(int commentId) {
        return boardMapper.deleteBComment(commentId);
    }

    public int deletecommentByCommentId(int commentId) {
        return boardMapper.deleteCommentByCid(commentId);
    }

    public List<Post> getBestList() {
        return boardMapper.getBestList();
    }

    public List<Post> BestByTitle(String keyword) {
        return boardMapper.getBestByTitle(keyword);
    }

    public List<Post> BestByTitleANDCont(String keyword) {
        return boardMapper.getBestByTitileANDCont(keyword);
    }

    public List<Post> BestByCont(String keyword) {
        return boardMapper.getBestByCont(keyword);
    }
}
