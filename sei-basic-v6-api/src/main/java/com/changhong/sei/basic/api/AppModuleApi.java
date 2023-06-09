package com.changhong.sei.basic.api;

import com.changhong.sei.basic.dto.AppModuleDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.FindAllApi;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <strong>实现功能:</strong>
 * <p>应用模块API接口</p>
 */
@FeignClient(name = "sei-basic", path = "appModule")
public interface AppModuleApi extends BaseEntityApi<AppModuleDto>,
        FindAllApi<AppModuleDto> {
    /**
     * 通过代码查询应用模块
     * @param code 代码
     * @return 操作结果
     */
    @GetMapping(path = "findByCode")
    @ApiOperation(value = "获取应用模块", notes = "通过代码获取应用模块")
    ResultData<AppModuleDto> findByCode(@RequestParam("code") String code);
}
