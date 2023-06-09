package com.changhong.sei.basic.dao;

import com.changhong.sei.basic.entity.UserProfile;

import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：用户配置数据访问扩展接口
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/6/15 17:00      秦有宝                     新建
 * <p/>
 * *************************************************************************************************
 */
public interface UserProfileExtDao {
    /**
     * 根据用户id列表获取用户配置
     *
     * @param userIds 用户id集合
     */
    List<UserProfile> findNotifyInfoByUserIds(List<String> userIds);
}
