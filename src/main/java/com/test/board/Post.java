package com.test.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Post {
    private int postid;
    private String title;
    private String content;
    private String nickname;
    private int userno;
    private Date date;
    private Date update;
    private int views;
    private int plike;
    // private List<Bimg> bimgs;
    private int b_category_id;
    private String cate_name;
    private int tcoment;



    public Post(int postId, String postTitle, String nickname, String postCont, Date postDate, int views, int plike, int bCategoryId) {
        this.postid = postId;
        this.title = postTitle;
        this.nickname = nickname;
        this.content = postCont;
        this.date = postDate;
        this.update = postDate;
        this.views = views;
        this.plike = plike;
        this.b_category_id = bCategoryId;
    }
    public Post(String title, int userno, Date date, String content, int bCategoryId) {
        this.title = title;
        this.userno = userno;
        this.date = date;
        this.content = content;
        this.b_category_id = bCategoryId;

    }


    public Post(int postid, String title, String nickname, String content, Date date, int views, int plike, int b_category_id,  String cate_name) {
        this.title = title;
        this.nickname = nickname;
        this.date = date;
        this.content = content;
        this.b_category_id = b_category_id;
        this.postid = postid;
        this.plike = plike;
        this.cate_name = cate_name;
        this.views = views;
    }

    public Post(int postid,String title,String postcont, Date upDate) {
        this.postid = postid;
        this.title = title;

        this.update = upDate;
        this.content = postcont;


    }
}
