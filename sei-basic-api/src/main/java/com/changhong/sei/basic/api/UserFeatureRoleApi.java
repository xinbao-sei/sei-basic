package com.changhong.sei.basic.api;

import com.changhong.sei.basic.dto.FeatureRoleDto;
import com.changhong.sei.basic.dto.UserDto;
import com.changhong.sei.basic.dto.UserFeatureRoleDto;
import com.changhong.sei.core.api.BaseRelationApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 实现功能: 用户分配的功能角色API接口
 *
 * @author 王锦光 wangjg
 * @version 2020-01-30 9:24
 */
@FeignClient(name = "sei-basic", path = "userFeatureRole")
@RequestMapping(path = "userFeatureRole", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public interface UserFeatureRoleApi extends BaseRelationApi<UserFeatureRoleDto, UserDto, FeatureRoleDto> {
}