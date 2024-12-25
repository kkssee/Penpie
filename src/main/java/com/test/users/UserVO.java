package com.test.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Repository
public class UserVO {
    private Long userno;
    private String userid;
    private String password;
    private String nickname;
    private int isAdult;
    private int isAuthor;

    private String profileImg;
    private String username;
    private int gender;
    private Date birth;
    private String contact;
    private String email;
    private int enabled;

    private Date signup_date;
    private String authority;
    private int is_Admin;
    private int isSubscribed;
    private int subscribeCount;

    private int pie_point;

    public UserVO(Long userno, String userid, String password, String username, String nickname, int gender, Date birth, String contact, String email, Date signupDate) {
        this.userno = userno;
        this.userid = userid;
        this.password = password;
        this.username = username;
        this.nickname = nickname;
        this.gender = gender;
        this.birth = birth;
        this.contact = contact;
        this.email = email;
        this.signup_date = signupDate;

    }

    public UserVO(String userid, String email)
    {
        this.userid = userid;
        this.email = email;
    }
    //세션 실험용으로 만듬
    public UserVO(Long userno, String userid, String password, String nickname) {
        this.userno = userno;
        this.userid = userid;
        this.password = password;
        this.nickname = nickname;
    }

    public UserVO(int subscribeCount, int isSubscribed) {
        this.subscribeCount = subscribeCount;
        this.isSubscribed = isSubscribed;
    }

    public void setBirthday(String sBirth) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            java.util.Date   uBirth = sdf.parse(sBirth);
            this.birth = new Date(uBirth.getTime());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSignupDate(String sDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            java.util.Date  uSignupDate= sdf.parse(sDate);
            this.signup_date = new Date(uSignupDate.getTime());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
