package com.test.search;

import com.test.CustomUserDetails;
import com.test.novel.NovelVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/search")
public class SearchController {
    @Autowired
    private SearchService searchService;

    protected CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }
        return null;
    }

    @GetMapping("")
    public String searchForm(Model model) {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails != null) {
            model.addAttribute("nickname", userDetails.getNickname());
            model.addAttribute("profileImg", userDetails.getProfileImg());
            Long userno = userDetails.getUserno();
            model.addAttribute("userno", userno);
        } else {
            model.addAttribute("nickname", "Guest");
        }

        //List<NovelVO> recommendations = searchService.getNovelRecommendations(getCurrentUserDetails().getUserno());
        return "search/searchForm";
    }

    @GetMapping("novel")
    public String searchNovel(@RequestParam(value="keyword", required=false) String keyword,
                              @RequestParam(value = "isFree", required = false) String isFree,
                              @RequestParam(value = "isAdult", required = false) String isAdult,
                              @RequestParam(value = "isCompleted", required = false) String isCompleted,
                              @RequestParam(value = "minEp", required = false) String minEp,
                              @RequestParam(value = "maxEp", required = false) String maxEp,
                              @RequestParam(value = "orderBy", required = false) String orderBy,
                              @RequestParam(value = "favoriteGenre", required = false) List<String> favoriteGenres,
                              @RequestParam(value = "excludeWordText", required = false) String excludeWordText,
                              @RequestParam(value = "excludeWord", required = false) List<String> excludeWordOptions,
                              Model model) {
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("keyword", keyword);
        searchParams.put("isFree", Integer.parseInt(isFree));
        searchParams.put("isAdult", Integer.parseInt(isAdult));
        searchParams.put("isCompleted", Integer.parseInt(isCompleted));
        searchParams.put("minEp", Integer.parseInt(minEp));
        searchParams.put("maxEp", Integer.parseInt(maxEp));
        searchParams.put("orderBy", orderBy);
        searchParams.put("favoriteGenres", favoriteGenres);
        searchParams.put("excludeWordText", excludeWordText);
        searchParams.put("excludeWordOptions", excludeWordOptions);
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails != null) {
            model.addAttribute("nickname", userDetails.getNickname());
            model.addAttribute("profileImg", userDetails.getProfileImg());
            Long userno = userDetails.getUserno();
            model.addAttribute("userno", userno);
        } else {
            model.addAttribute("nickname", "Guest");
        }
        log.info("와이라노");
        List<NovelVO> searchResult = searchService.searchNovels(searchParams);
        model.addAttribute("novelList", searchResult);
        model.addAttribute("keyword", keyword);
        return "search/searchNovel";
    }

    @PostMapping("suggestions")
    @ResponseBody
    public Map<String, List<NovelVO>> getSuggestions(@RequestParam("keyword") String keyword) {
        Map<String, List<NovelVO>> suggestions = new HashMap<>();
        suggestions.put("titles", searchService.getTitleSuggestions(keyword));
        suggestions.put("authors", searchService.getAuthorSuggestions(keyword));
        return suggestions;
    }

}
