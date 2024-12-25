package com.test.board;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@Service
//@Repository
public class BoardService {

    @Autowired
    private BoardDAO bdao;

    public int addPost(Post p) {
        System.out.println(p);
        int key = bdao.addPost(p);
        return key;
    }

    public Post getPostById(int postId) {
        Post post = bdao.getPost(postId);
        return post;
    }

    public List<Post> getList(Board board) {
        List<Post> list =bdao.getList(board);
        return list;
    }

    public int deleteBoardImg(String deleteList) {
        // deleteList는 삭제할 이미지 ID의 콤마로 구분된 문자열
        // 예를 들어 "1,2,3" 형태로 되어있다고 가정
        String[] imgIds = deleteList.split(",");

        // 배열을 리스트로 변환하고, 정수형 리스트로 변환
        List<Integer> imgIdList = Arrays.stream(imgIds)
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        // 해당 이미지 ID들을 삭제하는 쿼리 호출
        return bdao.deleteBoardImages(imgIdList);


    }
    public int updatePost(Post post) {
        int key = bdao.updatePost(post);
        return key;
    }

    public Map<String, Boolean> deletePost(int postid) {
        Map<String, Boolean> map = new HashMap<>();
        int rows = bdao.deletePost(postid);
        map.put("deleted", rows>0);
        return map;
    }

    public void inputViews(int postId) {
        bdao.inputViews(postId);
    }

    public List<Post> getSearchByF(String opt, String keyword) {
        keyword = "%" + keyword + "%";
        switch(opt) {
            case "title":
                List<Post> list = bdao.freelistByTitle(keyword);
                return list;
            case "ticont":
                List<Post> list2 = bdao.freelistByTitleANDCont(keyword);
                return list2;
            case "content":
                List<Post> list3 = bdao.freelistByCont(keyword);
                return list3;
            default:
                List<Post> list4 = null;
                log.info("내용,제목 아닌 제3");
                return list4;

        }
    }

    public List<Post> getSearchByR(String opt, String keyword) {
        keyword = "%" + keyword + "%";
        switch(opt) {
            case "title":
                log.info("제목옴");
                List<Post> list = bdao.replylistByTitle(keyword);
                return list;
            case "ticont":
                log.info("내용과제목옴");
                List<Post> list2 = bdao.replylistByTitleANDCont(keyword);
                return list2;
            case "content":
                log.info("내용옴");
                List<Post> list3 = bdao.replylistByCont(keyword);
                return list3;
            default:
                List<Post> list4 = null;
                log.info("내용,제목 아닌 제3");
                return list4;

        }

    }

    public int deltecomment(int commentId) {
        return bdao.deletecommentByCommentId(commentId);
    }

    public List<Post> getBestList() {
        return bdao.getBestList();
    }

    public List<Post> getSearchByB(String opt, String keyword) {
        keyword = "%" + keyword + "%";
        switch(opt) {
            case "title":
                List<Post> list = bdao.BestByTitle(keyword);
                return list;
            case "ticont":
                List<Post> list2 = bdao.BestByTitleANDCont(keyword);
                return list2;
            case "content":

                List<Post> list3 = bdao.BestByCont(keyword);
                return list3;
            default:
                List<Post> list4 = null;
                log.info("내용,제목 아닌 제3");
                return list4;

        }

    }




}
