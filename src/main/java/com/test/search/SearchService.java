package com.test.search;

import com.test.novel.NovelVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SearchService {
    @Autowired
    private SearchMapper searchMapper;

    public List<NovelVO> searchNovels(Map<String, Object> searchParams) {
        int userno = 1;
        searchParams.put("userno", userno);

        String[] splitKeyword = ((String) searchParams.get("keyword")).split("\\s+");
        searchParams.put("keywords", String.join(".*", splitKeyword));

        String excludeWordTextStr = (String) searchParams.get("excludeWordText");
        List<String> excludeWords = null;
        if (excludeWordTextStr != null && !excludeWordTextStr.isEmpty()) {
            excludeWords = Arrays.stream(excludeWordTextStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        searchParams.put("excludeWords", excludeWords);
        return searchMapper.searchNovels(searchParams);
    }

    public List<NovelVO> getTitleSuggestions(String keyword) {
        return searchMapper.getTitleSuggestions(keyword);
    }
    public List<NovelVO> getAuthorSuggestions(String keyword) {
        return searchMapper.getAuthorSuggestions(keyword);
    }

    public List<NovelVO> getNovelRecommendations(Long userno) {
        return searchMapper.getNovelRecommendations(userno);
    }
}
