package com.changhong.sei.basic.entity;

import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.ICodeUnique;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：功能角色组
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017-05-04 10:51      王锦光(wangj)                新建
 * <p/>
 * *************************************************************************************************
 */
@Entity
@Access(AccessType.FIELD)
@Table(name = "feature_role_group")
@DynamicInsert
@DynamicUpdate
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FeatureRoleGroup extends BaseAuditableEntity implements ITenant, ICodeUnique {
    private static final long serialVersionUID = 3882109264158336055L;
    /**
     * 租户代码
     */
    @Column(name = "tenant_code",length = 10,nullable = false)
    private String tenantCode;
    /**
     * 代码
     */
    @Column(name = "code",unique = true,length = 20,nullable = false)
    private String code;
    /**
     * 名称
     */
    @Column(name = "name",length = 50,nullable = false)
    private String name;

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

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
}
