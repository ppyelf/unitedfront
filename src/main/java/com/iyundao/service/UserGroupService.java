package com.iyundao.service;

import com.iyundao.entity.UserGroup;

import java.util.List;

/**
 * @ClassName: UserGroupService
 * @project: IYunDao
 * @author: 念
 * @Date: 2019/5/28 0:16
 * @Description: 服务 - 用户组
 * @Version: V2.0
 */
public interface UserGroupService {

    /**
     * 获取用户组列表
     * @return
     */
    List<UserGroup> getList();

    /**
     * 根据ID获取实体信息
     * @param id
     * @return
     */
    UserGroup findById(String id);

    /**
     * 保存用户组实体
     * @param userGroup
     */
    UserGroup save(UserGroup userGroup);



    List<UserGroup> findByIds(String[] userGroupIds);

    /**
     * 获取没有父级的集合
     * @return
     */
    List<UserGroup> getListByFatherIsNull();

    /**
     * 根据父级ID获取实体集合
     * @param id
     * @return
     */
    List<UserGroup> findByFatherId(String id);

    /**
     *
     * @param userGroupIds
     * @return
     */
    List<UserGroup> findBysomeIds(String[] userGroupIds);
}
