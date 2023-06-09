package com.changhong.sei.basic.api;

import com.changhong.sei.basic.dto.*;
import com.changhong.sei.core.api.BaseRelationApi;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 实现功能: 功能角色分配的功能项API接口
 *
 * @author 王锦光 wangjg
 * @version 2020-01-28 10:26
 */
@FeignClient(name = "sei-basic", path = "featureRoleFeature")
public interface FeatureRoleFeatureApi extends BaseRelationApi<FeatureRoleFeatureDto, FeatureRoleDto, FeatureDto> {
    /**
     * 根据模块，获取指定角色授权树
     *
     * @param appModuleId   应用模块id
     * @param featureRoleId 角色id
     * @return 指定模块授权树形对象集合
     */
    @GetMapping(path = "getAuthTree")
    @ApiOperation(notes = "根据模块，获取指定角色授权树", value = "根据模块，获取指定角色授权树")
    ResultData<List<AuthTreeVo>> getAuthTree(@RequestParam("appModuleId") String appModuleId, @RequestParam("featureRoleId") String featureRoleId);

    /**
     * 获取角色的功能项树
     *
     * @param featureRoleId 角色id
     * @return 功能项树清单
     */
    @GetMapping(path = "getFeatureTree")
    @ApiOperation(notes = "获取角色的功能项树", value = "获取角色的功能项树(应用模块-页面-功能项)")
    ResultData<List<FeatureNode>> getFeatureTree(@RequestParam("featureRoleId") String featureRoleId);

    /**
     * 获取未分配的功能项树
     *
     * @param appModuleId 应用模块id
     * @param featureRoleId 角色id
     * @return 功能项树清单
     */
    @GetMapping(path = "getUnassignedFeatureTree")
    @ApiOperation(notes = "获取未分配的功能项树", value = "获取未分配的功能项树(页面-功能项)")
    ResultData<List<FeatureNode>> getUnassignedFeatureTree(@RequestParam("appModuleId") String appModuleId,
                                                           @RequestParam("featureRoleId") String featureRoleId);
}
