package com.iyundao.service.impl;

import com.iyundao.entity.UserGroup;
import com.iyundao.repository.UserGroupRepository;
import com.iyundao.service.UserGroupService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: UserGroupServiceImpl
 * @project: IYunDao
 * @author: 念
 * @Date: 2019/5/28 0:17
 * @Description: 实现 - 用户组
 * @Version: V2.0
 */
@Service
@Transactional
public class UserGroupServiceImpl implements UserGroupService {

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Override
    public List<UserGroup> getList() {
        return userGroupRepository.getList();
    }

    @Override
    public UserGroup findById(String id) {
        return userGroupRepository.findByUserGroupId(id);
    }

    @Override
    public UserGroup save(UserGroup userGroup) {
        return userGroupRepository.save(userGroup);
    }

    @Override
    public List<UserGroup> findByIds(String[] userGroupIds) {
        List<UserGroup> userGroups = userGroupRepository.findByids(userGroupIds);
        return CollectionUtils.isEmpty(userGroups)
                ? new ArrayList<>()
                : userGroups;
    }

    @Override
    public List<UserGroup> getListByFatherIsNull() {
        return userGroupRepository.getListByFatherIsNull();
    }

    @Override
    public List<UserGroup> findByFatherId(String id) {
        return userGroupRepository.findByFatherId(id);
    }

    @Override
    public List<UserGroup> findBysomeIds(String[] userGroupIds) {
        return userGroupRepository.findByIds(userGroupIds);
    }


}
