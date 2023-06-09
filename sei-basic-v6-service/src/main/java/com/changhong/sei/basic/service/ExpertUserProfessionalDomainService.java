package com.changhong.sei.basic.service;

import com.changhong.sei.basic.dao.ExpertUserProfessionalDomainDao;
import com.changhong.sei.basic.entity.ExpertUser;
import com.changhong.sei.basic.entity.ExpertUserProfessionalDomain;
import com.changhong.sei.basic.entity.ProfessionalDomain;
import com.changhong.sei.core.dao.BaseRelationDao;
import com.changhong.sei.core.service.BaseRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 专家分配领域业务层
 * Author:jamson
 * date:2018/3/13
 */
@Service
public class ExpertUserProfessionalDomainService extends BaseRelationService<ExpertUserProfessionalDomain, ExpertUser, ProfessionalDomain> {
    @Autowired
    private ExpertUserProfessionalDomainDao expertUserProfessionalDomainDao;

    @Override
    protected BaseRelationDao<ExpertUserProfessionalDomain, ExpertUser, ProfessionalDomain> getDao() {
        return expertUserProfessionalDomainDao;
    }

    @Override
    protected List<ProfessionalDomain> getCanAssignedChildren(String parentId) {
        return expertUserProfessionalDomainDao.getChildrenFromParentId(parentId);
    }
}
