package com.iyundao.repository;

import com.iyundao.base.BaseRepository;
import com.iyundao.entity.Sign;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName: SignRepository
 * @project: IYunDao
 * @author: 念
 * @Date: 2019/6/6 10:10
 * @Description: 仓库 - 签到
 * @Version: V2.0
 */
@Repository
public interface SignRepository extends BaseRepository<Sign, String> {

    /**
     * 根据活动ID获取实体集合
     * @param id
     * @return
     */
    @Query("select s from Sign s where s.activity.id = ?1")
    List<Sign> findByActivityId(String id);

    @Query(value = "select * from t_sign  where type = ?1 and userid = ?2 order by signTime desc", nativeQuery = true)
    List<Sign> findActivityByUserId(int aaa, String id);

}
