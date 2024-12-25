package com.test.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BComment {
    private int b_comment_id;
    private String b_comment;
    private int post_id;
    private int user_no;
    private int parentno;
    private java.sql.Date create_dt;
    private int groupno;
    private int depthno;
    private int orderno;
    private String nickname;
    private int totalchild;
}
