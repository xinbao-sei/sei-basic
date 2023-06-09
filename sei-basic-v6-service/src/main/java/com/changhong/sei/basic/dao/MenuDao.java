package com.changhong.sei.basic.dao;

import com.changhong.sei.basic.entity.Menu;
import com.changhong.sei.core.dao.BaseTreeDao;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：实现系统菜单数据访问接口
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间                  变更人                 变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/19 16:44              李汶强                  新建
 * 1.0.00      2017/5/10 17:58             高银军                   修改
 * <p/>
 * *************************************************************************************************
 */
@Repository
public interface MenuDao extends BaseTreeDao<Menu> {

    /**
     * 用名称模糊查询
     * @param name 查询关键字
     * @return 查询结果集
     */
    List<Menu> findByNameLike(String name);

    /**
     * 通过功能项查询
     * @param featureId 查询关键字
     * @return 查询结果集
     */
    List<Menu> findByFeatureId(String featureId);

    /**
     * 查询其他菜单配置的功能项
     * @param featureId 查询关键字
     * @return 查询结果集
     */
    Menu findFirstByFeatureIdAndIdNot(String featureId, String id);
}
