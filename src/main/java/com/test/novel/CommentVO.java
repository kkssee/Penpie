package com.test.novel;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
public class CommentVO {
    private int comment_id;
    private int ep_id;
    private int userno;
    private String comment_contents;
    private Date created_at;
    private int is_updated;
    private int parent_comment_id;

    private int liked;
    private int like_count;
    private String nickname;

    public CommentVO(int commentId, int epId, String commentContents, Date createdAt, int isUpdated, String nickname) {
        this.comment_id = commentId;
        this.ep_id = epId;
        this.comment_contents = commentContents;
        this.created_at = createdAt;
        this.is_updated = isUpdated;
        this.nickname = nickname;
    }

    public CommentVO(int commentId, int epId, String nickname, String commentContents, Date createdAt, int isUpdated) {
        this.comment_id = commentId;
        this.ep_id = epId;
        this.nickname = nickname;
        this.comment_contents = commentContents;
        this.created_at = createdAt;
        this.is_updated = isUpdated;
    }

    public CommentVO(int commentId, int epId, String commentContents, Date createdAt, int isUpdated, int parentCommentId, String nickname) {
        this.comment_id = commentId;
        this.ep_id = epId;
        this.comment_contents = commentContents;
        this.created_at = createdAt;
        this.is_updated = isUpdated;
        this.nickname = nickname;
        this.parent_comment_id = parentCommentId;
    }
}
