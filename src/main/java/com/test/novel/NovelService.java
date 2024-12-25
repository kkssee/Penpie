package com.test.novel;

import com.test.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.*;

@Slf4j
@Service
public class NovelService {
    @Autowired
    private NovelDAO novelDAO;
    @Autowired
    private NovelMapper novelMapper;

    protected CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }
        return null;
    }

    public int addNovel(NovelVO nv, MultipartFile file, HttpServletRequest request) throws IOException {
        String relativePath = "src/main/resources/static/img/cover/";
        String absolutePath = new File("").getAbsolutePath() + "/" + relativePath;
        File directory = new File(absolutePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        UUID uuid = UUID.randomUUID();
        String coverPath = uuid + "_" + file.getOriginalFilename();
        file.transferTo(new File(absolutePath + coverPath));


        nv.setCover_path(coverPath);
        nv.setUserno(getCurrentUserDetails().getUserno());

        if(nv.getIs_secret() == 1) {
            nv.setIs_free(0);
        } else nv.setIs_free(nv.getIs_secret());

        // 소설 정보 추가
        novelMapper.addNovel(nv);
        int nv_id = nv.getNv_id();
        System.out.println("소설 아이디:  " + nv.getNv_id());

        Map<String, Object> params = new HashMap<>();
        params.put("nv_id", nv_id);
        params.put("nvctg_name", nv.getNvctg_name());

        novelMapper.addNovelCategory(params);

        Map<String, Integer> tags = new HashMap<>();
        tags.put("nv_id", nv.getNv_id());
        // 태그 추가
        for (String tagName : nv.getNvtag_name()) {
            int nvtag_id = getOrCreateTagId(tagName);
            tags.put("nvtag_id", nvtag_id);
            novelMapper.insertTag(tags);
        } return nv_id;
    }

    public Integer getOrCreateTagId(String tagName) {
        Integer tagId = novelMapper.getTagId(tagName);

        if (tagId == null) {
            novelMapper.createTag(tagName);
            tagId = novelMapper.getLastInsertTagId();
        }
        return tagId;
    }



    public List<NovelVO> getList() { return novelDAO.getList(getCurrentUserDetails().getUserno()); }

    public NovelVO getNovel(int nv_id) {
        Long userno = (getCurrentUserDetails() != null) ? getCurrentUserDetails().getUserno() : 0;
        return novelDAO.getNovel(nv_id, userno);
    }

    public List<EpisodeVO> getEpList(int nv_id, String sortOrder) {
        List<EpisodeVO> list = novelDAO.getEpList(nv_id, sortOrder);
        for(EpisodeVO ep : list) {
            if(ep.getCommentCount() == null) {
                ep.setCommentCount(0);
            }
        } return list;
    }

    public EpisodeVO getEp(int ep_id) {
        Long userno = (getCurrentUserDetails() != null) ? getCurrentUserDetails().getUserno() : 0;
        return novelDAO.getEp(ep_id, userno);
    }

    @Transactional
    public Map<String, Object> handleEpisodeUpload(int nv_id, String ep_title, String editorContent) {
        EpisodeVO ev = createEpisode(nv_id, ep_title, editorContent);
        int ep_id = novelDAO.addEp(ev);  // 데이터베이스에 에피소드 추가

        // 응답 데이터 구성
        Map<String, Object> result = new HashMap<>();
        result.put("ep_id", ep_id);
        result.put("uploaded", ep_id > 0);

        return result;
    }

    // 에피소드 생성 로직을 별도로 분리
    private EpisodeVO createEpisode(int nv_id, String ep_title, String editorContent) {
        EpisodeVO ev = new EpisodeVO();
        ev.setNv_id(nv_id);
        ev.setEp_title(ep_title);
        ev.setEp_contents(editorContent);
        return ev;
    }

    @Transactional
    public boolean editNovel(NovelVO nv, MultipartFile file, HttpServletRequest request) throws IOException {
        String originalCover = novelDAO.getCover(nv.getNv_id());
        handleFileUpload(nv, file, originalCover);  // 파일 업로드 로직을 별도로 처리

        nv.setUpdated_at(new java.sql.Date(new Date().getTime()));  // 수정된 날짜 설정

        boolean editNovel = novelDAO.editNovel(nv);
        boolean editCtg = novelDAO.editCtg(nv);

        handleTags(nv);  // 태그 추가/삭제 로직을 별도로 처리

        return editNovel && editCtg;
    }

    // 파일 업로드 처리 로직
    private void handleFileUpload(NovelVO nv, MultipartFile file, String originalCover) throws IOException {
        String relativePath = "src/main/resources/static/img/cover/";
        String absolutePath = new File("").getAbsolutePath() + "/" + relativePath;
        File directory = new File(absolutePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        if (file != null && !file.isEmpty()) {
            String newCoverPath = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            file.transferTo(new File(absolutePath + newCoverPath));
            nv.setCover_path(newCoverPath);
            deleteOldFile(absolutePath, originalCover);  // 기존 파일 삭제
        } else {
            nv.setCover_path(originalCover);  // 파일이 없으면 기존 경로 유지
        }
    }

    // 기존 파일 삭제 로직
    private void deleteOldFile(String fpath, String originalCover) {
        File oldFile = new File(fpath + originalCover);
        if (oldFile.exists() && oldFile.delete()) {
            log.info("기존 커버 파일 삭제 성공: {}", originalCover);
        }
    }

    // 태그 처리 로직
    private void handleTags(NovelVO nv) {
        if (nv.getTagsAdded() != null) {
            for (String tag : nv.getTagsAdded()) {
                novelDAO.addNovelTag(nv.getNv_id(), tag);
            }
        }
        if (nv.getTagsRemoved() != null) {
            for (String tag : nv.getTagsRemoved()) {
                novelDAO.removeNovelTag(nv.getNv_id(), tag);
            } novelDAO.removeOrphanTags();  // 고아 태그 삭제
        }
    }

    public boolean editEp(EpisodeVO ev) { return novelDAO.editEp(ev); }

    public List<CommentVO> getComments(int ep_id) { return novelDAO.getCommentList(ep_id); }

    public List<CommentDTO> getCommentsWithLikes(int epId) { return novelDAO.getCommentsWithLikes(epId, getCurrentUserDetails().getUserno()); }

    public int addComment(CommentDTO cv) {
        cv.setUserno(getCurrentUserDetails().getUserno());
        cv.setIsUpdated(0);
        return novelDAO.addComment(cv);
    }

    public List<CommentVO> getReplyList(int commentId) { return novelDAO.getReplyList(commentId); }

    public List<NovelVO> getMyNovelList() { return novelDAO.getMyNovelList(getCurrentUserDetails().getUserno()); }

    public CommentDTO getComment(int commentId) { return novelDAO.getComment(commentId, getCurrentUserDetails().getUserno()); }


    public boolean updateLikeStatus(int commentId, boolean liked) {
        Long userno = getCurrentUserDetails().getUserno();
        if(liked) {
            return novelDAO.removeLike(commentId, userno);
        } else {
            return novelDAO.addLike(commentId, userno);
        }
    }

    public List<CommentDTO> getReplyWithLikes(int comment_id) { return novelDAO.getReplyWithLikes(comment_id, getCurrentUserDetails().getUserno()); }

    public boolean delComment(int commentId) {
        int commentDeleted = novelDAO.delComment(commentId);
        int repliesDeleted = novelDAO.delReplies(commentId);
        // 댓글이 삭제되었고, 답글이 삭제되었거나 답글이 없는 경우도 성공으로 간주
        return commentDeleted > 0;
    }

    public List<String> uploadEpisodeImages(MultipartFile[] uploadFiles) {
        String epPath = "src/main/resources/static/img/epfiles/";
        String savePath = new File("").getAbsolutePath() + "/" + epPath;
        File uploadPath = new File(savePath);
        File directory = new File(savePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        List<String> fileNames = new ArrayList<>();
        for (MultipartFile multipartFile : uploadFiles) {
            String uploadFileName = multipartFile.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            uploadFileName = uuid + "_" + uploadFileName;

            File saveFile = new File(uploadPath, uploadFileName);
            try {
                multipartFile.transferTo(saveFile);
                fileNames.add(uploadFileName);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        return fileNames;
    }

    public ResponseEntity<byte[]> showImage(String fileName) {
        String epPath = "src/main/resources/static/img/epfiles/";
        String savePath = new File("").getAbsolutePath() + "/" + epPath;
        log.info("에피이미지 경로: {}", savePath);
        File file = new File(savePath, fileName);
        ResponseEntity<byte[]> result = null;
        File directory = new File(savePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try {
            HttpHeaders header = new HttpHeaders();
            header.add("Content-type", Files.probeContentType(file.toPath()));

            // 이미지 파일을 byte 배열로 변환하여 ResponseEntity에 담아 반환
            result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file), header, HttpStatus.OK);
        } catch (NoSuchFileException e) {
            log.error("No Such FileException: {}", e.getFile());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            log.error("IOException: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } return result;
    }

    public ResponseEntity<String> deleteImage(String fileName) {
        String epPath = "src/main/resources/static/img/epfiles/";
        String savePath = new File("").getAbsolutePath() + "/" + epPath;
        File file = new File(savePath, fileName);
        if (file.exists() && file.delete()) {
            return ResponseEntity.ok("파일 삭제 성공");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("파일 삭제 실패");
        }
    }

    public boolean updateEpLikeStatus(int epId, boolean liked) {
        Long userno = getCurrentUserDetails().getUserno();
        if(liked) {
            return novelDAO.removeEpLike(epId, userno);
        } else {
            return novelDAO.addEpLike(epId, userno);
        }
    }

    public boolean updateNovelSubscribeStatus(int nvId, boolean subscribed) {
        Long userno = (getCurrentUserDetails() != null) ? getCurrentUserDetails().getUserno() : 0;

        if(subscribed) {
            return novelDAO.removeNvSubscribe(nvId, userno);
        } else {
            return novelDAO.addNvSubscribe(nvId, userno);
        }
    }

    public int getFirstEpisode(int nvId) {
        return novelDAO.getFirstEpisodeId(nvId);
    }

    public List<EpisodeVO> getViewHistory(int nvId) {
        long userno = (getCurrentUserDetails() != null) ? getCurrentUserDetails().getUserno() : 0;
        if(userno > 0) {
            return novelDAO.getViewHistory(userno, nvId);
        } else return null;
    }

    public boolean recordViewHistory(int epId) {
        Long userno = (getCurrentUserDetails() != null) ? getCurrentUserDetails().getUserno() : 0;
        if(userno > 0) {
            Optional<Integer> viewed = novelDAO.checkHistory(userno, epId);
            if(viewed.isPresent() && viewed.get() > 0) {
                return novelDAO.updateViewHistory(userno, epId);
            } else {
                return novelDAO.recordViewHistory(userno, epId);
            }
        } else return false;
    }

    public List<String> getCategories() {
        return novelMapper.getCategories();
    }

    public EpisodeVO getPreviousEpisode(int ep_id, int nv_id) {
        return novelMapper.getPreviousEpisode(ep_id, nv_id);
    }

    public EpisodeVO getNextEpisode(int ep_id, int nv_id) {
        return novelMapper.getNextEpisode(ep_id, nv_id);
    }
}
