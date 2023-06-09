package com.changhong.sei.basic.entity;

import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.ICodeUnique;
import com.changhong.sei.core.entity.IFrozen;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

/**
 * 实现功能: 租户
 *
 * @author 王锦光 wangjg
 * @version 2020-01-19 14:35
 */
@Access(AccessType.FIELD)
@Entity
@Table(name = "tenant")
@DynamicInsert
@DynamicUpdate
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Tenant extends BaseAuditableEntity
        implements ICodeUnique, IFrozen {
    private static final long serialVersionUID = -3094139002145835657L;
    /**
     * 租户代码
     */
    @Column(name = "code", length = 10, unique = true, nullable = false)
    private String code;
    /**
     * 租户名称
     */
    @Column(name = "name", length = 200, nullable = false)
    private String name;

    /**
     * 是否冻结，0代表未冻结，1代表冻结
     */
    @Column(name = "frozen", nullable = false)
    private Boolean frozen = Boolean.FALSE;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Boolean getFrozen() {
        return frozen;
    }

    @Override
    public void setFrozen(Boolean frozen) {
        this.frozen = frozen;
    }
}
