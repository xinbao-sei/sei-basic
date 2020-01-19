package com.changhong.sei.basic.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 实现功能: 系统菜单DTO
 *
 * @author 王锦光 wangjg
 * @version 2020-01-19 21:57
 */
@ApiModel(description = "系统菜单DTO")
public class MenuDto extends BaseEntityDto{
    /**
     * 菜单代码
     */
    private String code;
    /**
     * 菜单名称
     */
    @NotBlank
    @Size(max = 20)
    private String name;
    /**
     * 菜单代码路径
     */
    private String codePath;

    /**
     * 菜单名称路径
     */
    private String namePath;

    /**
     * 菜单层级
     */
    @NotNull
    @Min(0)
    private Integer nodeLevel = 0;

    /**
     * 排序号
     */
    @NotNull
    @Min(0)
    private Integer rank=0;

    /**
     * 父节点id
     */
    private String parentId;

    /**
     * 功能项组Id
     */
    private String featureId;

    /**
     * 关联功能项代码
     */
    private String featureCode;

    /**
     * 关联功能项名称
     */
    private String featureName;

    /**
     * 图标样式名称
     */
    private String iconCls;

    /**
     * 子节点列表
     */
    private List<MenuDto> children;

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

    public String getCodePath() {
        return codePath;
    }

    public void setCodePath(String codePath) {
        this.codePath = codePath;
    }

    public String getNamePath() {
        return namePath;
    }

    public void setNamePath(String namePath) {
        this.namePath = namePath;
    }

    public Integer getNodeLevel() {
        return nodeLevel;
    }

    public void setNodeLevel(Integer nodeLevel) {
        this.nodeLevel = nodeLevel;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public String getFeatureCode() {
        return featureCode;
    }

    public void setFeatureCode(String featureCode) {
        this.featureCode = featureCode;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getIconCls() {
        return iconCls;
    }

    public void setIconCls(String iconCls) {
        this.iconCls = iconCls;
    }

    public List<MenuDto> getChildren() {
        return children;
    }

    public void setChildren(List<MenuDto> children) {
        this.children = children;
    }
}
