package com.test.users;

import com.test.CustomUserDetails;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    protected CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }
        return null;
    }

    @GetMapping("/")
    public String redirectToNovelMain() {
        return "redirect:/index";
    }

    @GetMapping("/loginForm")
    public String loginForm(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout, Model model) {
        if (error != null && error.equals("T")) {
            model.addAttribute("error", "Invalid userid or password");
        } else if (logout != null && logout.equals("T")) {
            model.addAttribute("message", "You have logged out successfully");
        }
        return "users/loginForm";
    }

    @GetMapping("/loginSession")
    public String loginSession(HttpSession session, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            UserVO user = userMapper.findByID(username);
            session.setAttribute("user", user);
        }
        return "redirect:/index";
    }
    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("userVO", new UserVO());
        return "users/joinForm";
    }

    @PostMapping("/join")
    public String join(@ModelAttribute("userVO") UserVO userVO) {
        userService.registerUser(userVO);
        return "redirect:/loginForm";  // 가입 후 로그인 페이지로 리다이렉트
    }

    @GetMapping("/profile/{userno}")
    public String profile(@PathVariable("userno") Long userno, Model model) {
        CustomUserDetails userDetails = getCurrentUserDetails();

        // Profile User
        String nickname = userDAO.getNicknameByUserno(userno);
        model.addAttribute("nickname", nickname);
        String profileImg = userDAO.getUserProfileImage(userno);
        model.addAttribute("profileImg", profileImg);
        model.addAttribute("subscribedNovels", userService.getSubscribedNovels(userno));
        model.addAttribute("userComments", userService.getCommentsByUser(userno));
        model.addAttribute("authoredNovels", userService.getNovelsByAuthor(userno));
        model.addAttribute("followingCount", userService.getFollowingCount(userno));

        // Viewer User
        model.addAttribute("viewerUser", userDetails.getUserid());
        Long viewerUserNo = userDetails.getUserno();
        model.addAttribute("viewerUserNo", viewerUserNo);
        model.addAttribute("uv", userService.getSubscribeStatus(userno, viewerUserNo));

        return "users/profile";
    }

    public String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName(); // username 반환
        }
        return null; // 인증되지 않은 경우 null 반환
    }

    @PostMapping("/logout")
    @ResponseBody
    public Map<String, String> logout(HttpSession session) {
        // 세션 무효화
        session.invalidate();

        // 로그아웃 성공 메시지 반환
        Map<String, String> response = new HashMap<>();
        response.put("message", "성공적으로 로그아웃 되었습니다.");
        return response;
    }

    @PostMapping("/subscribeAuthor")
    @ResponseBody
    public Map<String, Object> subscribeAuthor(@RequestParam Long authorno,
                                      @RequestParam boolean subscribed) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = userService.updatesubscribeAuthorStatus(authorno, subscribed);
            response.put("success", success);
            response.put("subscribed", !subscribed);
        } catch (Exception e) {
            log.error("Exception occurred: {}", e.getMessage(), e);
            response.put("success", false);
        }
        return response;
    }

    @GetMapping("/editProfile/{userno}")
    public String editProfile(@PathVariable("userno") Long userno, Model model) {

        return "users/editProfile";
    }

    @GetMapping("followList/{userno}")
    public String followList(@PathVariable("userno") Long userno,
                         @RequestParam(value = "tab") String tab,
                         Model model) {
        model.addAttribute("defaultTab", tab);
        String nickname = userDAO.getNicknameByUserno(userno);
        model.addAttribute("nickname", nickname);

        Long viewerUserNo = userDAO.getUsernoByUserid(getAuthenticatedUsername());
        model.addAttribute("viewerUserNo", viewerUserNo);

        List<UserVO> followerList = userService.getFollowerList(userno, viewerUserNo);
        List<UserVO> followingList = userService.getFollowingList(userno, viewerUserNo);
        model.addAttribute("followerList", followerList);
        model.addAttribute("followingList", followingList);

        return "users/followList";
    }
}
