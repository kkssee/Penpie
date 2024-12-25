package com.test.novel;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
public class CommentDTO {
    private int commentId;
    private int epId;
    private Long userno;
    private String userId;
    private String commentContents;
    private Date createdAt;
    private String nickname;
    private int isLiked;  // 1 if liked, 0 if not liked
    private int likeCount;
    private int replyCount;

    private int isUpdated;
    private int parentCommentId;
}
