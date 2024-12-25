package com.test.index;

import com.test.novel.NovelVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
@Slf4j
@Repository
public class IndexDAO {
    @Autowired
    IndexMapper indexMapper;

    public List<NovelVO> top5Views() {
        return indexMapper.top5Views();
    }

    /*public List<NovelVO> get() {

    }*/
}
