package com.changhong.sei.basic.entity;

import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

/**
 * 用户邮件提醒
 * Created by WangShuFa on 2018/7/11.
 */
@Entity
@Table(name = "user_email_alert")
@Access(AccessType.FIELD)
@DynamicInsert
@DynamicUpdate
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserEmailAlert extends BaseAuditableEntity implements ITenant {
    private static final long serialVersionUID = 7385304267196101647L;
    /**
     * 用户Id
     */
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    /**
     * 待办工作数量
     */
    @Column(name = "to_do_amount", length = 10)
    private Integer toDoAmount=0;

    /**
     * 间隔时间（小时）
     */
    @Column(name = "hours", length = 10)
    private Integer hours=0;

    /**
     * 最后提醒时间
     */
    @Column(name = "last_time")
    private Date lastTime;

    /**
     * 租户代码
     */
    @Column(name = "tenant_code", length = 10,nullable = false)
    private String tenantCode;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getToDoAmount() {
        return toDoAmount;
    }

    public void setToDoAmount(Integer toDoAmount) {
        this.toDoAmount = toDoAmount;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
       this.tenantCode=tenantCode;
    }
}
