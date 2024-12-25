package com.test.index;

import com.test.novel.NovelVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Mapper
@Repository
public interface IndexMapper {
    List<NovelVO> top5Views();

    List<NovelVO> getLastReadNovels(@Param("userno") Long userno);

    List<BannerVO> getBanners();
}
