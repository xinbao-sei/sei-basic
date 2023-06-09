package com.changhong.sei.basic.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import com.changhong.sei.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 实现功能: 专家用户DTO
 *
 * @author 王锦光 wangjg
 * @version 2020-01-28 9:36
 */
@ApiModel(description = "专家用户DTO")
public class ExpertUserDto extends BaseEntityDto {
    private static final long serialVersionUID = -1686281691296654038L;
    /**
     * 代码
     */
    @NotBlank
    @Size(max = 30)
    @ApiModelProperty(value = "代码(max = 30)", required = true)
    private String code;
    /**
     * 名称
     */
    @NotBlank
    @Size(max = 100)
    @ApiModelProperty(value = "名称(max = 100)", required = true)
    private String name;

    /**
     * 有效期
     */
    @ApiModelProperty(value = "有效期(Date)", required = true)
    @NotNull
    @JsonFormat(timezone = DateUtils.DEFAULT_TIMEZONE, pattern = DateUtils.DEFAULT_DATE_FORMAT)
    private Date expireDate;

    /**
     * 租户代码
     */
    @ApiModelProperty(value = "租户代码")
    private String tenantCode;
    /**
     * 专家ID（同步过来的源表中的ID）
     */
    @NotBlank
    @Size(max = 36)
    @ApiModelProperty(value = "专家ID(max = 36)", required = true)
    private String expertId;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getExpertId() {
        return expertId;
    }

    public void setExpertId(String expertId) {
        this.expertId = expertId;
    }
}
