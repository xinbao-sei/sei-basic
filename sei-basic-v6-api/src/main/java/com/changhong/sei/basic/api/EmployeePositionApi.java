package com.changhong.sei.basic.api;

import com.changhong.sei.basic.dto.EmployeeDto;
import com.changhong.sei.basic.dto.EmployeePositionDto;
import com.changhong.sei.basic.dto.PositionDto;
import com.changhong.sei.core.api.BaseRelationApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 实现功能: 企业员工用户分配岗位的API接口
 *
 * @author 王锦光 wangjg
 * @version 2020-01-27 15:03
 */
@FeignClient(name = "sei-basic", path = "employeePosition")
public interface EmployeePositionApi extends BaseRelationApi<EmployeePositionDto, EmployeeDto, PositionDto> {
}
