package com.iyundao.service;


import com.iyundao.base.Page;
import com.iyundao.base.Pageable;
import com.iyundao.entity.*;

import java.util.List;

/**
 * @ClassName: ActivityService
 * @project: ayundao
 * @author: 念
 * @Date: 2019/6/5 13:42
 * @Description: 服务 - 活动
 * @Version: V2.0
 */
public interface ActivityService {

    /**
     * 保存实体
     * @param activity
     * @param attendance
     * @param activityFiles
     * @param activityImages
     * @param subjectId
     * @param departId
     * @param groupId
     * @return
     */
    Activity save(Activity activity, List<Attendance> attendance, List<ActivityFile> activityFiles, List<ActivityImage> activityImages, String subjectId, String departId, String groupId);

    /**
     * 根据Id查找实体
     * @param id
     * @return
     */
    Activity find(String id);

    /**
     * 删除实体
     * @param activity
     */
    void delete(Activity activity);

    /**
     * 保存活动文件
     * @param file
     * @return
     */
    ActivityFile saveFile(ActivityFile file);

    /**
     * 保存活动图片
     * @param image
     * @return
     */
    ActivityImage saveImage(ActivityImage image);

    /**
     * 根据IDS获取活动文件集合
     * @param activityFileIds
     * @return
     */
    List<ActivityFile> findActivityFilesByIds(String[] activityFileIds);

    /**
     * 根据IDS获取活动图片集合
     * @param activityImageIds
     * @return
     */
    List<ActivityImage> findActivityImageByIds(String[] activityImageIds);

    /**
     * 根据IDS删除活动文件
     * @param ids
     * @return
     */
    void delFileByIds(String[] ids);

    /**
     * yundaoSERVER
     *  YD_2016
     * 根据IDS删除活动图片
     * @param ids
     */
    void delImage(String[] ids);

    /**
     * 查询活动列表
     * @return
     */
    List<Activity> findAll();

    /**
     * 活动列表分页
     * @return
     * @param pageable
     */
    Page<Activity> findAllForPage(Pageable pageable);

    /**
     * 保存用户签到流程
     * @param sign
     * @return
     */
    Sign saveUserSign(Sign sign);

    /**
     * 根据IDS获取实体信息
     * @param id
     * @return
     */
    ActivityFile findByIds(String id);
}
