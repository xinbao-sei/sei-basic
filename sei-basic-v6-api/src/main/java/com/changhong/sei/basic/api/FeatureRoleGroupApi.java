package com.changhong.sei.basic.api;

import com.changhong.sei.basic.dto.FeatureRoleGroupDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.FindAllApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 实现功能: 功能角色组API接口
 *
 * @author 王锦光 wangjg
 * @version 2020-01-28 10:50
 */
@FeignClient(name = "sei-basic", path = "featureRoleGroup")
public interface FeatureRoleGroupApi extends BaseEntityApi<FeatureRoleGroupDto>,
        FindAllApi<FeatureRoleGroupDto> {
}
