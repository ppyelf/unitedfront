package com.iyundao.repository;

import com.iyundao.base.BaseRepository;
import com.iyundao.entity.UserInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @ClassName: UserInfoRepository
 * @project: unitedfront
 * @author: 念
 * @Date: 2019/8/1 9:26
 * @Description: 用户详情仓库
 * @Version: V1.0
 */
@Repository
public interface UserInfoRepository extends BaseRepository<UserInfo, String> {


    /**
     * 根据用户ID查询实体信息
     * @param id
     * @return
     */
    @Query("select ui from UserInfo ui where ui.user.id = ?1")
    UserInfo findUserInfoByUserId(String id);
}
