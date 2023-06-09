package com.changhong.sei.basic.controller;

import com.changhong.sei.basic.api.TenantAppModuleApi;
import com.changhong.sei.basic.dto.AppModuleDto;
import com.changhong.sei.basic.dto.TenantAppModuleDto;
import com.changhong.sei.basic.dto.TenantDto;
import com.changhong.sei.basic.entity.AppModule;
import com.changhong.sei.basic.entity.Tenant;
import com.changhong.sei.basic.entity.TenantAppModule;
import com.changhong.sei.basic.service.TenantAppModuleService;
import com.changhong.sei.core.controller.BaseRelationController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.service.BaseRelationService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 实现功能: 租户分配应用模块API服务
 *
 * @author 王锦光 wangjg
 * @version 2020-01-19 15:16
 */
@RestController
@Api(value = "TenantAppModuleApi", tags = "租户分配应用模块API服务")
@RequestMapping(path = "tenantAppModule", produces = MediaType.APPLICATION_JSON_VALUE)
public class TenantAppModuleController extends BaseRelationController<TenantAppModule, Tenant, AppModule, TenantAppModuleDto, TenantDto, AppModuleDto>
        implements TenantAppModuleApi {
    @Autowired
    private TenantAppModuleService service;
    /**
     * 获取当前用户可用的应用模块代码清单
     *
     * @return 应用模块代码清单
     */
    @Override
    public ResultData<List<String>> getAppModuleCodes() {
        List<String> codes = service.getAppModuleCodes();
        return ResultData.success(codes);
    }

    /**
     * 获取当前用户可用的应用模块清单
     *
     * @return 应用模块清单
     */
    @Override
    public ResultData<List<AppModuleDto>> getTenantAppModules() {
        List<AppModule> appModules = service.getTenantAppModules();
        List<AppModuleDto> dtos = appModules.stream().map(this::convertChildToDto).collect(Collectors.toList());
        return ResultData.success(dtos);
    }

    @Override
    public BaseRelationService<TenantAppModule, Tenant, AppModule> getService() {
        return service;
    }
}
