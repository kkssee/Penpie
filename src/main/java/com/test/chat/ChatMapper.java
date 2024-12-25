package com.test.chat;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMapper {
    String findOpponentProfileImg(Long id, Long userno);
}
