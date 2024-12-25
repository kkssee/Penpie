package com.test.board;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.test.users.UserService;
import com.test.users.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.*;
@Slf4j
@Repository
@RequestMapping("/board")
@Controller
public class BoardController {
    @Autowired
    BoardService boardService;
    @Autowired
    TimeService timeService;
    @Autowired
    CommentService commentService;
    @Autowired
    UserService userService;

    @PostMapping("imgs")
    @ResponseBody
    public List<String> uploadEpisodeImgs(MultipartFile[] uploadFile) {
        String relativePath = "src/main/resources/static/img/boardimg/";
        String absolutePath = new File("").getAbsolutePath() + relativePath;
        File uploadPath = new File(absolutePath);
        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
        }

        List<String> fileNames = new ArrayList<>();
        for (MultipartFile multipartFile : uploadFile) {
            String uploadFileName = multipartFile.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            uploadFileName = uuid + "_" + uploadFileName;

            File saveFile = new File(absolutePath, uploadFileName);
            try {
                multipartFile.transferTo(saveFile);
                fileNames.add(uploadFileName);
            } catch (Exception e) {
                System.err.println(e);
            }
        } return fileNames;
    }

    @GetMapping("/display")
    @ResponseBody
    public ResponseEntity<byte[]> showImageGET(@RequestParam("fileName") String fileName) {
        String relativePath = "src/main/resources/static/img/boardimg/";
        String absolutePath = new File("").getAbsolutePath() + relativePath;

        File file = new File(absolutePath, fileName);
        ResponseEntity<byte[]> result = null;

        try {
            HttpHeaders header = new HttpHeaders();
            header.add("Content-type", Files.probeContentType(file.toPath()));

            result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file), header, HttpStatus.OK);
        } catch (NoSuchFileException e){
            log.error("No Such FileException {}", e.getFile());
        } catch (IOException e) {
            log.error(e.getMessage());
        } return result;
    }

    @DeleteMapping("/deleteImg")
    @ResponseBody
    public ResponseEntity<String> deleteImage(
            @RequestParam("fileName") String fileName
    ) {
        String relativePath = "src/main/resources/static/img/boardimg/";
        String absolutePath = new File("").getAbsolutePath() + relativePath;
        File file = new File(absolutePath, fileName);

        if (file.exists() && file.delete()) {
            return ResponseEntity.ok("파일 삭제 성공");

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("파일 삭제 실패");
        }
    }

    @GetMapping("/freelist/page/{page}")
    public String boardmain(@PathVariable int page,Model model, @RequestParam(required = false) String opt, @RequestParam(required = false) String keyword) {
        Board board = new Board();
        board.setB_categoryid(1);

        PageHelper.startPage(page,5);  // 한 화면에 3개 아이템, 적용이 안되는데 mapper의 MyBatis가 필요한듯
        List<Post> list = boardService.getList(board);
        if (opt != null && keyword != null) {
            list = boardService.getSearchByF(opt,keyword);
        }
        PageInfo<Post> pageInfo = new PageInfo<>(list, 2); //하단에 2개씩 나온다. 즉 현재 페이지 기준 다음이 3개면 다음 그룹
        model.addAttribute("pageInfo", pageInfo);

        return "board/boardmain";
    }

    @GetMapping("/postForm")
    public String postForm(HttpSession session,Model model, Post post) {
        UserVO sessionUser = (UserVO) session.getAttribute("user");
        if (sessionUser == null) {
            model.addAttribute("alertMessage", "로그인이 필요한 서비스입니다.");
            return "board/alertAndLogin";
        }
        model.addAttribute("user", sessionUser);
        log.info("세션 확인"+session.getAttribute("user"));
        model.addAttribute("post", post);
        return "board/postForm";
    }

    @PostMapping("/uppost")
    @ResponseBody
    @Transactional
    public Map<String, Object> uppost(@ModelAttribute("post") Post post,
                                      @RequestParam("userno") int userno,
                                      @RequestParam("title") String title,
                                      @RequestParam("postContent") String postContent,
                                      @RequestParam("b_category_id") int b_category_id) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        log.info("제목: {}", title);
        log.info(" 내용: {}", postContent);

        Post po = new Post();

        po.setTitle(title);
        po.setContent(postContent);
        Date date = new Date();
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        po.setDate(sqlDate);
        po.setUserno(userno);
        po.setB_category_id(b_category_id);

        int key = boardService.addPost(po);
        log.info("키는:"+key);
        if(key>0) {
            map.put("uploaded", true);
            map.put("post_id", key);
        }
        return map;
    }

    @GetMapping("/detail/{post_id}")
    public String detail(@PathVariable("post_id") int post_id, Model model, HttpSession session, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String roles = authentication.getAuthorities().toString();  // 전체 권한을 문자열로 변환

        boolean isMaster = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_MASTER"));
        Post post = boardService.getPostById(post_id);
        model.addAttribute("post", post);
        UserVO sessionUser = (UserVO) session.getAttribute("user");

        String clientIp = request.getRemoteAddr();

        if (sessionUser != null && sessionUser.getUserno() == post.getUserno() || isMaster) {
            model.addAttribute("isOwner", true);
        } else {
            model.addAttribute("isOwner", false);
        }

        boolean alreadyLiked = !timeService.canLike(clientIp, post_id);
        model.addAttribute("hlike", alreadyLiked);
        boardService.inputViews(post_id);

        if(sessionUser != null){   //sessionUser.getUserno() != 0
            model.addAttribute("user", sessionUser);
        }
        List<BComment> bComments = commentService.getList(post_id);
        log.info("코멘트 확인"+bComments.toString());
        if(bComments != null){
            model.addAttribute("comments", bComments);
            for(BComment bComment : bComments){
                if(sessionUser != null){
                    if(bComment.getUser_no()== sessionUser.getUserno() || isMaster){
                        model.addAttribute("deleter", true);
                    }
                }
            }
        }
        return "board/postDetail";
    }

    @PostMapping("/deletcoment/{commentId}")
    @ResponseBody
    public Map<String, Object> deletcoment(@PathVariable("commentId") int commentId) {
        Map<String, Object> map = new HashMap<>();
        int m = boardService.deltecomment(commentId);
        if(m>0) {
            map.put("deleted", true);
        }else{
            map.put("deleted", false);
        }
        return map;
    }
    /*
        @GetMapping("/freelist/page/{page}")
        public String freelist(@PathVariable int page,Model model, @RequestParam(required = false) String opt, @RequestParam(required = false) String keyword) {
            Board board = new Board();
            board.setB_categoryid(1);

            PageHelper.startPage(page,3);  // 한 화면에 3개 아이템, 적용이 안되는데 mapper의 MyBatis가 필요한듯
            List<Post> list = boardService.getList(board);
            if (opt != null && keyword != null) {
                list = boardService.getSearchByF(opt,keyword);
            }
            PageInfo<Post> pageInfo = new PageInfo<>(list, 10); //밑에 3페이지와 관련된 10개?
            model.addAttribute("pageInfo", pageInfo);

            return "board/freelist";
        }
    */
    @GetMapping("/replylist/page/{page}")
    public String replylist(@PathVariable int page,Model model, @RequestParam(required = false) String opt, @RequestParam(required = false) String keyword) {

        Board board = new Board();
        board.setB_categoryid(2);

        //model.addAttribute("list", list);
        PageHelper.startPage(page,5);  // 한 화면에 3개 아이템, 적용이 안되는데 mapper의 MyBatis가 필요한듯
        List<Post> list = boardService.getList(board);
        if (opt != null && keyword != null) {
            list = boardService.getSearchByR(opt,keyword);
        }
        PageInfo<Post> pageInfo = new PageInfo<>(list, 2); //밑에 3페이지와 관련된 10개?

        model.addAttribute("pageInfo", pageInfo);
        return "board/replylist";
    }

    @GetMapping("/list/{b_category_id}")
    public String list(@PathVariable("b_category_id") int b_category_id, Model model) {
        if (b_category_id == 1) {
            return "redirect:/board/freelist/page/1";
        } else if (b_category_id == 2) {
            return "redirect:/board/replylist/page/1";
        }
        return "board/boardmain";
    }

    @GetMapping("/updateForm/{post_id}")
    public String update(@PathVariable("post_id") int post_id, Model model) {
        Post post = boardService.getPostById(post_id);
        model.addAttribute("post", post);
        log.info("업데이트 전에 확인"+post);
        return "board/updateForm";
    }

    @PostMapping("/update/{post_id}")
    @Transactional
    @ResponseBody
    public Map<String,Object> updatePost (@RequestParam(value="files",required = false)  MultipartFile file,
                                          @RequestParam("userno") int userno,
                                          @RequestParam("title") String title,
                                          @RequestParam("postContent") String postContent,
                                          @PathVariable int post_id) {

        Map<String, Object> map = new HashMap<>();
        log.info("제목: {}", title);
        log.info(" 내용: {}", postContent);

        Post po = new Post();
        po.setPostid(post_id);
        po.setTitle(title);
        po.setContent(postContent);
        Date date = new Date();
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        po.setUpdate(sqlDate);
        po.setUserno(userno);

        // 6. 게시글 업데이트 처리
        int result = boardService.updatePost(po);
        map.put("updated", result);
        return map;
    }

    @PostMapping("/deletepost/{postid}")
    @ResponseBody
    public Map<String,Boolean> deletePost (@PathVariable("postid") int postid) {
        return boardService.deletePost(postid);
    }

    @PostMapping("/liked/{postid}")
    @ResponseBody
    public Map<String, Object> likedPost(@PathVariable("postid") int postid, HttpServletRequest request, Model model) {
        String clientIp = request.getRemoteAddr();
        log.info("라이크 요청 처리 중 - IP: " + clientIp);

        Map<String, Object> response = new HashMap<>();

        if (!timeService.canLike(clientIp, postid)) {
            log.info("이미 추천한 게시글입니다. (IP: " + clientIp + ")");
            response.put("liked", false);
            response.put("message", "1분에 한 번만 추천할 수 있습니다.");
        } else {
            boolean liked = timeService.inputLike(postid);
            log.info("추천 결과: " + liked);
            if (liked) {
                timeService.recordLike(clientIp, postid);  // 추천 기록
                response.put("liked", true);
            } else {
                response.put("liked", false);
            }
        }
        return response;
    }

    @PostMapping("/write")
    @ResponseBody
    public Map<String,Object> commentWrite(HttpServletRequest req
            , @RequestParam HashMap<String, Object> param
            , ModelMap model) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        // 널이면 댓글, 널이 아니면 대댓글 작성
        if(param.get("parentno").equals("")) {
            commentService.insertComment(req, param);
            response.put("result", true);
        } else {
            // 대댓글에 작성하는 경우 부모 댓글 정보 가져오기
            Map<String,Object> Comment = commentService.selectComment(param);

            log.info("부모댓글 확인하기"+Comment);
            // 부모 댓글의 깊이, 순서 값을 넣는다.
            int depthNo = Integer.parseInt(String.valueOf(Comment.get("DEPTHNO")));
            int orderNo = Integer.parseInt(String.valueOf(Comment.get("ORDERNO")));
            int groupno = Integer.parseInt(String.valueOf(Comment.get("GROUPNO")));
            log.info("댓글 깊이, 오더, 그룹 확인"+depthNo+orderNo+groupno);
            param.put("groupno", groupno);
            if (depthNo >= 3) {
                // 깊이가 4 이상이면 댓글 작성을 막음
                response.put("more", true);
                return response;
            }
            // 이 조건에 맞는 경우는 모두 부모 댓글에 적는 경우, 대댓글이 아니니까 순서를 맨아래로 해두면 된다.
            if(depthNo == 0 && orderNo == 0) {
                param.put("depthNo", depthNo + 1);
                param.put("orderNo", commentService.selectMaxOrderNo(param));
            } else {
                // 부모 댓글의 자손 수 계산
                int childCount = commentService.selectCntChild(param);

                // 부모 댓글의 자손 수가 0이 아니라면
                if(childCount != 0) {
                    // 부모 댓글의 순서 값 + 자손 수 초과하는 순서 값을 + 1
                    param.put("orderNo", orderNo + childCount);
                    commentService.updateRemainComment(param);

                    // 그 사이에 들어가는 대댓글의 순서 및 깊이 값
                    param.put("orderNo", orderNo + childCount + 1);
                    param.put("depthNo", depthNo + 1);

                } else {
                    // 부모댓글 이후의 대댓글의 순서 값 + 1
                    param.put("orderNo", orderNo);
                    log.info(String.valueOf(orderNo));
                    commentService.updateRemainComment(param);

                    // 그 사이에 들어가는 대댓글의 순서 및 깊이 값
                    param.put("depthNo", depthNo + 1);
                    param.put("orderNo", orderNo + 1);

                }

            }
            // 댓글 작성
            commentService.insertReplyComment(req, param);
            response.put("result", true);

        }
        return response;
    }

    @GetMapping("/best/page/{page}")
    public String bestfreelist(@PathVariable int page,Model model, @RequestParam(required = false) String opt, @RequestParam(required = false) String keyword) {

        PageHelper.startPage(page,5);
        List<Post> list = boardService.getBestList();

        if (opt != null && keyword != null) {
            list = boardService.getSearchByB(opt,keyword);
        }
        PageInfo<Post> pageInfo = new PageInfo<>(list, 2); //밑에 3페이지와 관련된 10개?
        model.addAttribute("pageInfo", pageInfo);

        return "board/bestfreelist";
    }

    @GetMapping("/search/")
    public String Search (String cate,String opt, String keyword) throws UnsupportedEncodingException {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString());
        String redirectUrl = "";
        switch (cate){
            case "자유게시판":
                log.info("자유게시판"+encodedKeyword);
                redirectUrl = String.format("redirect:/board/freelist/page/1?opt=%s&keyword=%s", opt, encodedKeyword);
                break;
            case "소설릴레이":
                log.info("소설릴레이");
                redirectUrl = String.format("redirect:/board/replylist/page/1?opt=%s&keyword=%s", opt, encodedKeyword);
                break;
            case "실시간베스트":
                log.info("실시간베스트");
                redirectUrl = String.format("redirect:/board/best/page/1?opt=%s&keyword=%s", opt, encodedKeyword);
                break;
            default:
                log.info("내용,제목 아닌 제3");
                redirectUrl = "redirect:/board/freelist/page/1";

        }
        return redirectUrl;
    }
}
