package com.changhong.sei.basic.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

/**
 * <strong>实现功能:</strong>
 * <p>企业用户权限复制参数</p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2019-11-08 14:19
 */
@ApiModel(description = "企业用户权限复制参数")
public class EmployeeCopyParam implements Serializable {
    /**
     * 源用户Id
     */
    @ApiModelProperty(value = "源用户Id(max = 36)", required = true)
    private String employeeId;

    /**
     * 目标用户Id清单
     */
    @ApiModelProperty(value = "目标用户Id清单(List)", required = true)
    private List<String> targetEmployeeIds;

    /**
     * 复制功能角色Id清单
     */
    @ApiModelProperty(value = "复制功能角色Id清单(List)", required = true)
    private List<String> featureRoleIds;

    /**
     * 复制数据角色Id清单
     */
    @ApiModelProperty(value = "复制数据角色Id清单(List)", required = true)
    private List<String> dataRoleIds;

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public List<String> getTargetEmployeeIds() {
        return targetEmployeeIds;
    }

    public void setTargetEmployeeIds(List<String> targetEmployeeIds) {
        this.targetEmployeeIds = targetEmployeeIds;
    }

    public List<String> getFeatureRoleIds() {
        return featureRoleIds;
    }

    public void setFeatureRoleIds(List<String> featureRoleIds) {
        this.featureRoleIds = featureRoleIds;
    }

    public List<String> getDataRoleIds() {
        return dataRoleIds;
    }

    public void setDataRoleIds(List<String> dataRoleIds) {
        this.dataRoleIds = dataRoleIds;
    }
}
