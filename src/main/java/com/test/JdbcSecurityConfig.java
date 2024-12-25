package com.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableWebSecurity
public class JdbcSecurityConfig {
    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @Autowired
    DataSource dataSource;    // JDBC Authentication에 필요함

    @Bean
    public BCryptPasswordEncoder  passwordEncoder() {
        // log.info("리라->" + enc.encode("abc123ABC!@#")); // ROLE_USER
        return new BCryptPasswordEncoder();
    }

    //Enable jdbc authentication
    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        /*auth.jdbcAuthentication().dataSource(dataSource)
                .usersByUsernameQuery(
                        "SELECT userid, password, enabled FROM users WHERE userid=?")
                .authoritiesByUsernameQuery(
                        "SELECT u.userid, a.authority FROM authorities a " +
                                "JOIN users u ON a.userno = u.userno WHERE u.userid=?");*/
        auth.userDetailsService(customUserDetailsService);
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/resources/**", "/ignore2");
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // log.info("접근제한 설정");
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/resources/**", "/img/**", "/static/**","/css/**", "/js/**", "fragments/**", "/image/cover/*", "/character/list", "/oauth2/**", "/character/**", "/audio/voice/**", "/search/suggestions").permitAll()  // 정적 리소스 접근 허용
                        .requestMatchers("/", "/novel/list", "/novel/detail/**", "/novel/episode/*", "/novel/prevEp/**","/novel/nextEp/**", "/followList/*",
                                "/novel/comments/*","/novel/reply/*", "/index", "/join", "/novel/display", "/index/denied", "/logout", "/ws/chat", "/ws/in/*",  "/ws/in", "/board/display", "/board/deleteImg", "/board/freelist/page/*", "/board/detail/*", "/board/replylist/page/*", "/board/list/*", "/board/liked/*","/loginSession","/board/best/page/*","board/search/*", "board/list/*", "/index/comments","/profile/*", "/search/**", "/search").permitAll()
                        .requestMatchers("/novel/nvup", "/novel/epup/*","/ws/**", "/novel/comment", "novel/epup", "/novel/addReply", "/novel/myNovelList", "/novel/like", "/novel/nvedit/*", "/novel/nvedit", "/novel/delComment", "/novel/likeEp", "/novel/subscribeNovel", "/ws/chat", "/ws/**", "/pp/*","/pp/verify/*","/adult/verify/*", "/subscribeAuthor", "/securepage",
                                "/novel/epedit/*", "/novel/epedit", "/novel/deleteImg", "/novel/imgs","/board/postForm","/board/uppost", "/board/updateForm/*", "/board/update/*", "/editProfile/*", "/pp/*", "/adult/**",
                                "/board/deletepost/*", "/board/write", "/board/imgs", "board/deletcoment/*",
                                "/ws/markAsRead/*", "/character/chat/**","/stream", "/voice/**", "/voice").hasAnyRole("USER", "ADMIN", "MASTER")
                        .anyRequest().denyAll()  // 위의 설정 이외의 모든 요청은 거부한다
                )
                .csrf(csrfConf -> csrfConf.disable())
                .formLogin(loginConf -> loginConf
                        .loginPage("/loginForm")
                        .loginProcessingUrl("/doLogin")
                        .failureUrl("/loginForm?error=T")
                        .defaultSuccessUrl("/loginSession", true)
                        .usernameParameter("id")
                        .passwordParameter("pw")
                        .permitAll()
                )
                .logout(logoutConf -> logoutConf
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/loginForm?logout=T")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(exConf -> exConf
                        .accessDeniedPage("/index/denied")
                );
        return http.build();
    }
    // 메모리 기반 인증을 위한 메소드 제거함
}
