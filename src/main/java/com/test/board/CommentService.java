package com.test.board;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CommentService {
    @Autowired
    private BoardDAO bdao;

    /*
    public int saveComment(BComment bComment) {
        bComment.setCreate_dt(new Date(System.currentTimeMillis()));

        int i = bdao.saveComment(bComment);
        return i;
    }


    public List<BComment> selectReplyList(int postid) {
        return bdao.selectReplyList(postid);
    }
*/
    public void insertComment(HttpServletRequest req, HashMap<String, Object> param) {
        param.put("create_dt", new Date(System.currentTimeMillis()));
        bdao.saveComment(req,param);
    }

    public Map<String, Object> selectComment(HashMap<String, Object> param) {
        return bdao.selectComment(param);
    }

    public int selectMaxOrderNo(HashMap<String, Object> param) {
        return bdao.selectMaxOrderNo(param);
    }

    public int selectCntChild(HashMap<String, Object> param) {
        return bdao.countChild(param);
    }

    public void updateRemainComment(HashMap<String, Object> param) {
        bdao.updateComentByNo(param);
    }
    //대댓글 작성
    public void insertReplyComment(HttpServletRequest req, HashMap<String, Object> param) {
        param.put("create_dt", new Date(System.currentTimeMillis()));
        bdao.insetReply(req,param);
    }

    public List<BComment> getList(int postId) {
        List<BComment> list = bdao.getCommentList(postId);
        return list;
    }
}
