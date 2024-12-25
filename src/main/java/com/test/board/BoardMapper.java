package com.test.board;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface BoardMapper {
    void addPost(Post p);

    int getGeneratedPostId(Post p);

    Post getPost(int postId);

    List<Post> getList(Board board);

    int updatePost(Post post);

    int deleteBoardImages(@Param("imgIdList") List<Integer> imgIdList);

    int deletePost(int postid);

    int deleteBComment(int postid);

    int inputLike(int postid);

    int inputViews(int postid);


    List<Post> freelistByTitle(@Param("keyword") String keyword);
    List<Post> freelistByTitleANDCont(@Param("keyword") String keyword);

    List<Post> freelistByCont(@Param("keyword") String keyword);

    List<Post> replylistByTitle(@Param("keyword") String keyword);

    List<Post> replylistByTitleANDCont(@Param("keyword") String keyword);

    List<Post> replylistByCont(@Param("keyword") String keyword);

    void saveComment(@Param("postId") int postId,
                     @Param("userNo") int userNo,
                     @Param("comment") String comment,
                     @Param("createDt") Date createDt);

    Map<String, Object> selectComment(@Param("bCommentId") int bCommentId);

    int selectMaxOrderNo(@Param("postid") int postid, @Param("groupno") int groupno);

    int countChild(@Param("parentId") int parentId);

    void updateCommentByNo(@Param("postId") int postId,@Param("groupNo") int groupNo, @Param("orderNo") int orderNo);

    void insetReply(@Param("postId") int postId,@Param("userNo") int userNo,@Param("parentNo") int parentNo, @Param("comment") String comment, @Param("createDt") Date createDt, @Param("groupno") int groupno,
                    @Param("depthno") int depthno, @Param("orderno")int orderno);

    List<BComment> getCommentList(int postId);

    int deleteCommentByCid(int commentId);

    List<Post> getBestList();

    List<Post> getBestFreeByT(String keyword);

    List<Post> getBestByTitle(String keyword);

    List<Post> getBestByTitileANDCont(String keyword);

    List<Post> getBestByCont(String keyword);
}
