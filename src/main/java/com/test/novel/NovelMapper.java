package com.test.novel;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface NovelMapper {
    // 소설 추가 메서드
    int addNovel(NovelVO nv);

    // 카테고리 추가 메서드
    int addNovelCategory(Map<String, Object> params);

    // 태그 추가 메서드
    int addNovelTag(@Param("nv_id") int nv_id, @Param("nvtag_id") int nvTagId);

    // 태그 ID 가져오기 또는 생성
    Integer getOrCreateTagId(@Param("tagName") String tagName);

    Integer getTagId(String tagName);

    Integer createTag(String tagName);

    Integer getLastInsertTagId();

    void insertTag(Map<String, Integer> tags);

    List<String> getCategories();

    EpisodeVO getPreviousEpisode(int ep_id, int nv_id);
    EpisodeVO getNextEpisode(int ep_id, int nv_id);

}
