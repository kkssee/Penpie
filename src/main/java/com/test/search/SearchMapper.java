package com.test.search;

import com.test.novel.NovelVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SearchMapper {

    List<NovelVO> searchNovels(Map<String, Object> searchParams);

    List<NovelVO> getTitleSuggestions(String keyword);
    List<NovelVO> getAuthorSuggestions(String keyword);

    List<NovelVO> getNovelRecommendations(Long userno);
}
