package com.test;

import com.test.users.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CustomUserDetails user = userMapper.findByUserId(username);
        Collection<GrantedAuthority> authorities = loadUserAuthorities(user.getUserno());
        user.setAuthorities(authorities);

        return new CustomUserDetails((Long) user.getUserno(), user.getUserid(), user.getUsername(),
                user.getPassword(), user.getNickname(), user.getEnabled(),
                user.getProfileImg(), user.getIsAdult(), user.getIsAuthor(),
                authorities);
    }

    public Collection<GrantedAuthority> loadUserAuthorities(Long userno) {
        List<String> authorities = userMapper.loadUserAuthorities(userno);
        ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String auth : authorities) {
            grantedAuthorities.add(new SimpleGrantedAuthority(auth));
        }
        return grantedAuthorities;
    }
}
