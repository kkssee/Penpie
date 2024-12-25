package com.test.novel;

import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
public class EpisodeVO {
    @Id
    private int ep_id;
    private int nv_id;
    private String ep_title;
    private String ep_contents;
    private Date ep_date;
    private int ep_views;
    private int price;

    private int episode_number;

    private int isLiked;  // 1 if liked, 0 if not liked
    private int likeCount;

    private Integer commentCount;

    private int isRent;
    private int isBought;

    public EpisodeVO(int epId, String epTitle, String epContents, Date epDate, int epViews, Integer commentCount, int episode_number, int price) {
        this.ep_id = epId;
        this.ep_title = epTitle;
        this.ep_contents = epContents;
        this.ep_date = epDate;
        this.ep_views = epViews;
        this.commentCount = commentCount;
        this.episode_number = episode_number;
        this.price = price;
    }

    public EpisodeVO(int epId, int nvId, String epTitle, String epContents, Date epDate, int epViews){
        this.ep_id = epId;
        this.nv_id = nvId;
        this.ep_title = epTitle;
        this.ep_contents = epContents;
        this.ep_date = epDate;
        this.ep_views = epViews;
    }

    public EpisodeVO(int epId, int nvId, String epTitle, String epContents, Date epDate, int epViews, int isLiked, int likeCount, int episode_number) {
        this.ep_id = epId;
        this.nv_id = nvId;
        this.ep_title = epTitle;
        this.ep_contents = epContents;
        this.ep_date = epDate;
        this.ep_views = epViews;
        this.isLiked = isLiked;
        this.likeCount = likeCount;
        this.episode_number = episode_number;
    }
}
