package com.test.novel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Data
@AllArgsConstructor
@Getter
@Setter
public class NovelVO {
    private int nv_id;
    private String nv_title;
    private Long userno;
    private String nv_desc;
    private Date created_at;
    private Date updated_at;
    private int is_adult;
    private int is_secret;
    private int is_completed;
    private int is_free;

    private String cover_path;

    private String nvctg_name;
    private String[] nvtag_name;
    private String[] tagsAdded;
    private String[] tagsRemoved;

    private String username;
    private String nickname;

    private int isSubscribed;
    private int subscribeCount;

    private int likeCount;
    private int commentCount;
    private int totalViews;

    private int episode_number;
    private int ep_id;
    private String ep_title;

    public NovelVO() { }

    public NovelVO(int nv_id, String nv_title, String nv_desc, String nickname, Date updated_at, int is_adult, int is_secret, String cover_path) {
        this.nv_id = nv_id;
        this.nv_title = nv_title;
        this.nv_desc = nv_desc;
        this.nickname = nickname;
        this.updated_at = updated_at;
        this.is_adult = is_adult;
        this.is_secret = is_secret;
        this.cover_path = cover_path;
    }

    public NovelVO(int nvId, String nvTitle, String nvDesc, String nickname, Date updatedAt, int isAdult, int isSecret, String coverPath, int subscribeCount, int isSubscribed) {
        this.nv_id = nvId;
        this.nv_title = nvTitle;
        this.nv_desc = nvDesc;
        this.nickname = nickname;
        this.updated_at = updatedAt;
        this.is_adult = isAdult;
        this.is_secret = isSecret;
        this.cover_path = coverPath;
        this.subscribeCount = subscribeCount;
        this.isSubscribed = isSubscribed;
    }

    public NovelVO(int nvId, String nvTitle, String nvDesc, String nickname, Date updatedAt, int isAdult, int isSecret, String coverPath, int subscribeCount, int isSubscribed, int likeCount, int commentCount, int totalViews) {
        this.nv_id = nvId;
        this.nv_title = nvTitle;
        this.nv_desc = nvDesc;
        this.nickname = nickname;
        this.updated_at = updatedAt;
        this.is_adult = isAdult;
        this.is_secret = isSecret;
        this.cover_path = coverPath;
        this.subscribeCount = subscribeCount;
        this.isSubscribed = isSubscribed;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.totalViews = totalViews;
    }
}
