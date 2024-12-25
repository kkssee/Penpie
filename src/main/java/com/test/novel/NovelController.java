package com.test.novel;


import com.test.CustomUserDetails;
import com.test.users.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/novel")
public class NovelController {
    @Autowired
    private NovelService novelService;
    @Autowired
    private UserService userService;
    @Autowired
    private NovelMapper novelMapper;
    @Autowired
    private NTimeService timeService;

    @GetMapping("nvup")
    public String uploadForm(NovelVO novelVO, Model model) {
        model.addAttribute("novel", novelVO);
        List<String> categories = novelService.getCategories();
        model.addAttribute("categories", categories);
        return "novel/novelAddForm";
    }

    @PostMapping("nvup")
    @ResponseBody
    @Transactional
    public Map<String, Object> uploadNovel(@ModelAttribute("nv") NovelVO nv,
                                            @RequestParam("file") MultipartFile file,
                                            HttpServletRequest request,
                                            Model model) throws IOException {
        int nv_id = novelService.addNovel(nv, file, request);
        Map<String, Object> map = new HashMap<>();
        map.put("uploaded", nv_id > 0);
        map.put("nv_id", nv_id);
        return map;
    }

    @GetMapping("list")
    public String novelList(Model model){
        List<NovelVO> list = novelService.getList();
        model.addAttribute("list", list);
        return "novel/novelList";
    }

    @GetMapping("myNovelList")
    public String myList(Model model){
        List<NovelVO> myList = novelService.getMyNovelList();
        model.addAttribute("list", myList);
        return "novel/novelList";
    }
    protected CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        } return null;
    }

    @GetMapping("detail/{nv_id}/episodes")
    public String detail(@PathVariable("nv_id") int nv_id,
                         @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder,
                         Model model) {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails != null) {
            model.addAttribute("nickname", userDetails.getNickname());
            model.addAttribute("profileImg", userDetails.getProfileImg());
            Long userno = userDetails.getUserno();
            model.addAttribute("userno", userno);
        } else {
            model.addAttribute("nickname", "Guest");
        }

        NovelVO nv = novelService.getNovel(nv_id);
        List<EpisodeVO> eplist = novelService.getEpList(nv_id, sortOrder);
        int firstEpisode = novelService.getFirstEpisode(nv_id); // 첫화 가져오기
        model.addAttribute("firstEpisode", firstEpisode); // 첫화 추가

        List<EpisodeVO> viewHistory = novelService.getViewHistory(nv_id);
        model.addAttribute("viewHistory", viewHistory);
        // 마지막 요소 꺼내기
        if (viewHistory != null && !viewHistory.isEmpty()) { // 리스트가 비어있지 않을 경우
            EpisodeVO lastEpisode = viewHistory.getLast(); // 마지막 요소
            model.addAttribute("lastEpisode", lastEpisode); // 마지막 에피소드 추가
            // Extract ep_ids from viewHistory for easier lookup
            List<Integer> viewHistoryIds = viewHistory.stream()
                    .map(EpisodeVO::getEp_id)
                    .collect(Collectors.toList());

            model.addAttribute("viewHistoryIds", viewHistoryIds);
        }

        model.addAttribute("nv", nv);
        model.addAttribute("eplist", eplist);
        model.addAttribute("sortOrder", sortOrder); // 추가
        return "novel/novelDetail";
    }


    @GetMapping("nvedit/{nv_id}")
    public String nvedit(@PathVariable("nv_id") int nv_id, Model model) {
        NovelVO nv = novelService.getNovel(nv_id);
        List<String> categories = novelService.getCategories();  // Fetch categories from the service
        model.addAttribute("nv", nv);
        model.addAttribute("categories", categories);  // Add categories to the model
        return "novel/novelEditForm";
    }


    @PostMapping("nvedit")
    @ResponseBody
    @Transactional
    public Map<String, Boolean> nvedit(@ModelAttribute("nv") NovelVO nv,
                                       @RequestParam("file") MultipartFile file,
                                       HttpServletRequest request,
                                       Model model) throws IOException {
        boolean edited = novelService.editNovel(nv, file, request);
        Map<String, Boolean> map = new HashMap<>();
        map.put("edited", edited);
        return map;
    }

    @GetMapping("epup/{nv_id}")
    public String uploadForm(@PathVariable("nv_id") int nv_id,
                             EpisodeVO episodeVO, Model model) {
        episodeVO.setNv_id(nv_id);
        model.addAttribute("episode", episodeVO);
        return "ep/epAddForm";
    }

    @PostMapping("/epup/{nv_id}")
    @ResponseBody
    @Transactional
    public Map<String, Object> uploadEp(@ModelAttribute("episode") EpisodeVO episodeVO,
                                        @PathVariable("nv_id") int nv_id,
                                        @RequestParam("ep_title") String ep_title,
                                        @RequestParam("editorContent") String editorContent) {
        // 비즈니스 로직을 서비스로 위임
        return novelService.handleEpisodeUpload(nv_id, ep_title, editorContent);
    }

    @GetMapping("episode/{ep_id}")
    public String getEp(@PathVariable("ep_id") int ep_id, HttpServletRequest request, Model model) {
        String ip = request.getRemoteAddr(); // 클라이언트 IP 주소 얻기
        timeService.incrementViewCount(ep_id, ip);

        boolean recorded = novelService.recordViewHistory(ep_id);

        EpisodeVO ev = novelService.getEp(ep_id);
        model.addAttribute("ev", ev);
        return "ep/epDetail";
    }


    @GetMapping("epedit/{ep_id}")
    public String epedit(@PathVariable("ep_id") int ep_id, Model model){
        EpisodeVO ev = novelService.getEp(ep_id);
        model.addAttribute("episode", ev);
        return "ep/epEditForm";
    }

    @PostMapping("epedit")
    @ResponseBody
    @Transactional
    public Map<String, Boolean> editEp(@ModelAttribute("ev") EpisodeVO ev,
                                       @RequestParam(value = "file", required = false) MultipartFile file,
                                       @RequestParam("editorContent") String editorContent)
                                        throws IOException {
        Map<String, Boolean> map = new HashMap<>();
        ev.setEp_contents(editorContent);
        boolean edited = novelService.editEp(ev);
        map.put("edited", edited);
        return map;
    }

    @PostMapping("imgs")
    @ResponseBody
    public List<String> uploadEpisodeImgs(MultipartFile[] uploadFile) {
        return novelService.uploadEpisodeImages(uploadFile);
    }

    @GetMapping("/display")
    @ResponseBody
    public ResponseEntity<byte[]> showImage(@RequestParam("fileName") String fileName) {
        return novelService.showImage(fileName);
    }

    @DeleteMapping("/deleteImg")
    @ResponseBody
    public ResponseEntity<String> deleteImage(@RequestParam("fileName") String fileName) {
        return novelService.deleteImage(fileName);
    }

    @GetMapping("comments/{ep_id}")
    public String comments(@PathVariable("ep_id") int ep_id, Model model){
        CommentDTO cv = new CommentDTO();
        cv.setEpId(ep_id);

        List<CommentDTO> comments = novelService.getCommentsWithLikes(ep_id);

        int commentCount;
        if(comments == null) {
            commentCount = 0;
        } else commentCount = comments.size();
        model.addAttribute("commentCount", commentCount);
        model.addAttribute("comments", comments);
        model.addAttribute("comment", cv);

        return "comments/comments";
    }

    @PostMapping("comment")
    @ResponseBody
    public Map<String, Object> addComment(@ModelAttribute("comment") CommentDTO cv, Model model){
        int co_id = novelService.addComment(cv);
        Map<String, Object> map = new HashMap<>();
        map.put("co_id", co_id);
        map.put("added", true);
        return map;
    }

    @GetMapping("reply/{comment_id}")
    public String commentReply(@PathVariable("comment_id") int comment_id, Model model){
        CommentDTO parent_comment = novelService.getComment(comment_id);

        List<CommentDTO> replyList = novelService.getReplyWithLikes(comment_id);
        int replyCount;
        if(replyList == null) {
            replyCount = 0;
        } else replyCount = replyList.size();
        model.addAttribute("replyCount", replyCount);
        model.addAttribute("replyList", replyList);
        model.addAttribute("comment", parent_comment);

        return "comments/commentReply";
    }

    @PostMapping("addReply")
    @ResponseBody
    public Map<String, Object> addReply(@ModelAttribute("comment") CommentDTO cv, Model model){
        cv.setParentCommentId(cv.getCommentId());
        int re_id = novelService.addComment(cv);
        Map<String, Object> map = new HashMap<>();
        map.put("re_id", re_id);
        map.put("added", true);
        return map;
    }

    @PostMapping("like")
    @ResponseBody
    public Map<String, Object> likeComment(@RequestParam int comment_id,
                                           @RequestParam boolean liked) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = novelService.updateLikeStatus(comment_id, liked);
            response.put("success", success);
            response.put("liked", !liked);
        } catch (Exception e) {
            log.error("Exception occurred: {}", e.getMessage(), e);
            response.put("success", false);
        }
        return response;
    }

    @PostMapping("/likeEp")
    @ResponseBody
    public Map<String, Object> likeEp(@RequestParam int ep_id,
                                      @RequestParam boolean liked) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = novelService.updateEpLikeStatus(ep_id, liked);
            response.put("success", success);
            response.put("liked", !liked);
        } catch (Exception e) {
            log.error("Exception occurred: {}", e.getMessage(), e);
            response.put("success", false);
        }
        return response;
    }

    @DeleteMapping("/delComment")
    public ResponseEntity<?> deleteComment(@RequestParam int commentId) {
        try {
            boolean deleted = novelService.delComment(commentId);
            if (deleted) {
                return ResponseEntity.ok(Map.of("deleted", true));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("deleted", false));
            }
        } catch (Exception e) {
            log.error("Exception occurred: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("subscribeNovel")
    @ResponseBody
    public Map<String, Object> subscribeNovel(@RequestParam int nv_id,
                                      @RequestParam boolean subscribed) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = novelService.updateNovelSubscribeStatus(nv_id, subscribed);
            response.put("success", success);
            response.put("subscribed", !subscribed);
        } catch (Exception e) {
            log.error("Exception occurred: {}", e.getMessage(), e);
            response.put("success", false);
        }
        return response;
    }

    @GetMapping("prevEp/{ep_id}/{nv_id}")
    public String getPreviousEpisode(@PathVariable("ep_id") int ep_id,
                                     @PathVariable("nv_id") int nv_id,
                                     HttpSession session) {
        EpisodeVO ev = novelService.getPreviousEpisode(ep_id, nv_id);
        if(ev == null) {
            session.setAttribute("popupMessage", "This is the first episode");
            return "redirect:/novel/episode/" + ep_id;
        }
        session.removeAttribute("popupMessage");
        return "redirect:/novel/episode/" + ev.getEp_id();
    }
    @GetMapping("nextEp/{ep_id}/{nv_id}")
    public String getNextEpisode(@PathVariable("ep_id") int ep_id,
                                 @PathVariable("nv_id") int nv_id,
                                 HttpSession session) {
        EpisodeVO ev = novelService.getNextEpisode(ep_id, nv_id);
        if(ev == null) {
            session.setAttribute("popupMessage", "This is the last episode");
            return "redirect:/novel/episode/" + ep_id;
        }
        session.removeAttribute("popupMessage");
        return "redirect:/novel/episode/" + ev.getEp_id();
    }
}
