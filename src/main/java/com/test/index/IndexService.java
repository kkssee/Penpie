package com.test.index;

import com.test.novel.NovelService;
import com.test.novel.NovelVO;
import com.test.users.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IndexService {
    @Autowired
    IndexDAO indexDAO;
    @Autowired
    private UserService userService;
    @Autowired
    private NovelService novelService;
    @Autowired
    private IndexMapper indexMapper;

    public List<NovelVO> top5Views() {
        List<NovelVO> list = indexDAO.top5Views();
        return list;
    }

    public List<BannerVO> getBanners() {
        List<BannerVO> list = indexMapper.getBanners();
        for(BannerVO bv : list) {
            List<String> tagsList = Arrays.stream(bv.getBa_tags().split(","))
                    .map(String::trim) // 공백 제거
                    .collect(Collectors.toList()); // 리스트로 변환
            bv.setTags(tagsList); // BannerVO 객체에 설정
        }
        return list;
    }


    public List<NovelVO> getLastReadNovels(Long userno) {
        return indexMapper.getLastReadNovels(userno);
    }
}
