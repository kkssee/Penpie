package com.test.index;

import com.test.CustomUserDetails;
import com.test.comment.CommentDAO;
import com.test.novel.CommentDTO;
import com.test.novel.NovelDAO;
import com.test.novel.NovelVO;
import com.test.users.UserDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
@Slf4j
@Controller
@RequestMapping("/index")
public class IndexController {
    @Autowired
    IndexService indexService;
    @Autowired
    UserDAO userDAO;
    @Autowired
    NovelDAO novelDAO;
    @Autowired
    private CommentDAO commentDAO;

    protected CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        } return null;
    }

    @GetMapping("")
    public String home(Model model) {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails != null) {
            model.addAttribute("nickname", userDetails.getNickname());
            model.addAttribute("profileImg", userDetails.getProfileImg());
            Long userno = userDetails.getUserno();
            model.addAttribute("userno", userno);
            List<NovelVO> lastReadNovels = indexService.getLastReadNovels(userno);
            model.addAttribute("lastReadNovels", lastReadNovels);
        } else {
            model.addAttribute("nickname", "Guest");
        }
        List<BannerVO> list = indexService.getBanners();  // 배너
        model.addAttribute("list", list);
        return "index/index";
    }

    @GetMapping("/denied")
    public String denied(Model model) {
        return "index/denied";
    }

    @GetMapping("/comments")
    @ResponseBody
    public List<CommentDTO> getRecentComments() {
        // get recent top 5 comments
        return commentDAO.getTopComments();
    }

}
