package com.test.users;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    UserMapper userMapper;
    @Autowired
    HttpSession httpSession;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        //서비스 구부을 위한 작업 (구글: google, 카카오: kakao 네이버: naver)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("CustomOAuth2UserService triggered for provider: {}", registrationId);

        //마지막 리턴할때 oauth2user 정보가 들어가는 부분
        String userNAmeAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        String email= null;
        String nickname = null;
        String username = null;
        String gender = null;
        String birthday = null;
        String birthyear = null;
        String mobile = null;
        String id = null;
        String userid = null;
        Map<String,Object> response = oAuth2User.getAttributes();

        //Kakao 로그인일 경우 사용하는 메서드, 필요한 사용자 정보가 kakaoAccount -> kakaoProfile 두번 감싸져 있어서,
        //두번 get() 메서드를 이용해 사용자 정보를 담고있는 Map을 꺼내야한다.
        try {
            if (registrationId.equals("google")) {
                email = (String) response.get("email");
                username = (String) response.get("name");
                id = "go";
            } else {
                throw new OAuth2AuthenticationException("허용되지 않는 인증입니다");
            }
        } catch (ClassCastException e) {
            log.error("OAuth2 attribute type casting error: {}", e.getMessage());
            throw new OAuth2AuthenticationException("잘못된 사용자 정보 형식입니다.");
        }

        UserVO user;
        //이미 가입된 사람인지 정보를 가져옴
        Optional<UserVO> optionalUser = userMapper.findByEmail(email);

        if(optionalUser.isPresent()){
            user = optionalUser.get();
            String authority = userMapper.setRole(user);
            user.setAuthority(authority);
        }else{
            //가입된 사람이 아닐 경우
            user = new UserVO();
            user.setEmail(email);
            user.setAuthority("ROLE_USER");
            userMapper.save(user);
            userid = id+user.getUserno();
            user.setUserid(userid);
            userMapper.updateByNoID(user);
            try {
                userMapper.saveRole(user);

                if(username != null && !username.equals("")){
                    user.setUsername(username);
                    userMapper.updateByNoName(user);
                }

                if(nickname != null && !nickname.equals("")){   //nickname != null ||
                    user.setNickname(nickname);
                    userMapper.updateByNoNick(user);
                }else if(nickname == null){
                    user.setNickname(user.getUsername());
                    userMapper.updateByNoNick(user);
                }



                if(gender != null && !gender.equals("")){
                    int gen = 0;
                    if(gender.equals("F")){
                        gen =1;
                    }else if(gender.equals("M")){
                        gen =0;
                    }
                    user.setGender(gen);
                    userMapper.updateByNoGen(user);
                }

                if(birthday != null){
                    String bir =  birthyear+"-"+birthday;
                    user.setBirth(Date.valueOf(bir));
                    userMapper.updateByNoBirth(user);
                }

                if(mobile != null && !mobile.equals("")){
                    log.info("Connected with mobile");
                    user.setContact(mobile);
                    userMapper.updateByNoCON(user);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        httpSession.setAttribute("user", user);
        log.info(String.valueOf(user));
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getAuthority()))
                , oAuth2User.getAttributes()
                , userNAmeAttributeName);
    }
}
