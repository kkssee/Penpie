package com.test.users;
import com.test.CustomUserDetails;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {
    UserVO findByID(String username);

    Integer getFollowingCount(Long userno);

    List<UserVO> getFollowerList(Long userno, Long viewerUserNo);
    List<UserVO> getFollowingList(Long userno, Long viewerUserNo);

    // CustomUserDetails
    CustomUserDetails findByUserId(String username);
    List<String> loadUserAuthorities(Long userno);


    //UserVO findByID(String username);

    Optional<UserVO> findByEmail(String email);

    String setRole(UserVO user);

    void save(UserVO user);

    void saveRole(UserVO user);

    void updateByNoName(UserVO user);

    void updateByNoNick(UserVO user);

    void updateByNoBirth(UserVO user);

    void updateByNoGen(UserVO user);

    void updateByNoCON(UserVO user);

    void updateByNoID(UserVO user);

    Integer scanNameAndEmail(String username, String email);
    UserVO getUserByNameAndEmail(String username, String email);
    Integer scanID(String userid);
    Integer updatepwdByID(String encodedPwd, String savedUserId);

    Integer getPoint(Long userno);
    void createPoint(UserVO user);
    void savePoint(UserVO user);
    void pointRecord(UserVO user);


}
