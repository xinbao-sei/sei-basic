package com.changhong.sei.basic.dto;

import com.changhong.sei.core.dto.serach.QuickSearchParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：岗位查询参数
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/6/26 9:27      秦有宝                     新建
 * <p/>
 * *************************************************************************************************
 */
@ApiModel(description = "岗位查询参数")
public class PositionQueryParam extends QuickSearchParam {

    /**
     * 是否包含组织机构子节点
     */
    @ApiModelProperty(value = "是否包含组织机构子节点", required = true)
    private Boolean includeSubNode = Boolean.FALSE;

    /**
     * 组织机构Id
     */
    @ApiModelProperty(value = "组织机构Id")
    private String organizationId;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    private String userId;

    public Boolean getIncludeSubNode() {
        return includeSubNode;
    }

    public void setIncludeSubNode(Boolean includeSubNode) {
        this.includeSubNode = includeSubNode;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
