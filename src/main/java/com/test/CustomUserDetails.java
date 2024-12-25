package com.test;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@Setter
public class CustomUserDetails implements UserDetails {
    private Long userno;
    private String userid;
    private String username; // userid in DB
    private String password;
    private String nickname;
    private int enabled;
    private String profileImg;
    private int isAdult;
    private int isAuthor;

    @Setter
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public CustomUserDetails(Long userno, String userid, String username, String password,
                             String nickname, int enabled, String profileImg, int isAdult, int isAuthor) {
        this.userno = userno;
        this.userid = userid;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.enabled = enabled;
        this.profileImg = profileImg;
        this.isAdult = isAdult;
        this.isAuthor = isAuthor;
    }

    public CustomUserDetails(Long userno, String userid, String username, String password,
                             String nickname, int enabled, String profileImg, int isAdult, int isAuthor,
                             Collection<? extends GrantedAuthority> authorities) {
        this.userno = userno;
        this.userid = userid;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.enabled = enabled;
        this.profileImg = profileImg;
        this.isAdult = isAdult;
        this.isAuthor = isAuthor;
        this.authorities = authorities;
    }
}
