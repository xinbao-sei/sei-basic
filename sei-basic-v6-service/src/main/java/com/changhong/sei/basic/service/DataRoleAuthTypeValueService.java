package com.changhong.sei.basic.service;

import com.changhong.sei.apitemplate.ApiTemplate;
import com.changhong.sei.basic.dao.DataRoleAuthTypeValueDao;
import com.changhong.sei.basic.dto.DataRoleRelation;
import com.changhong.sei.basic.entity.DataAuthorizeType;
import com.changhong.sei.basic.entity.DataRoleAuthTypeValue;
import com.changhong.sei.basic.service.client.DataAuthManager;
import com.changhong.sei.basic.service.util.AuthorityUtil;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.context.SessionUser;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.auth.AuthEntityData;
import com.changhong.sei.core.dto.auth.AuthTreeEntityData;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.BaseTreeService;
import com.changhong.sei.core.service.bo.OperateResult;
import com.changhong.sei.core.utils.TransactionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 实现功能：数据角色分配权限类型的值业务逻辑实现
 *
 * @author 王锦光(wangj)
 * @version 1.0.00      2017-05-04 14:04
 */
@Service
public class DataRoleAuthTypeValueService extends BaseEntityService<DataRoleAuthTypeValue> {
    @Autowired
    private DataRoleAuthTypeValueDao dao;
    @Autowired
    private DataAuthorizeTypeService dataAuthorizeTypeService;
    @Autowired
    private UserService userService;
    @Autowired
    private DataAuthManager dataAuthManager;

    @Override
    protected BaseEntityDao<DataRoleAuthTypeValue> getDao() {
        return dao;
    }

    /**
     * 创建数据角色分配关系
     *
     * @param relation 数据角色分配参数
     * @return 操作结果
     */
    //@CacheEvict(value = "getNormalUserAuthorizedEntities",allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public OperateResult insertRelations(DataRoleRelation relation) {
        if (Objects.isNull(relation) || relation.getEntityIds().isEmpty()) {
            return OperateResult.operationSuccess("00006", 0);
        }
        //排除已经存在的分配关系
        DataRoleRelation existRelation = dao.getDataRoleRelation(relation.getDataRoleId(), relation.getDataAuthorizeTypeId());
        Set<String> addEntityIds = new HashSet<>();
        addEntityIds.addAll(relation.getEntityIds());
        addEntityIds.removeAll(existRelation.getEntityIds());
        //创建需要创建的分配关系
        List<DataRoleAuthTypeValue> relations = new ArrayList<>();
        addEntityIds.forEach((e) -> relations.add(new DataRoleAuthTypeValue(relation.getDataRoleId(), relation.getDataAuthorizeTypeId(), e)));
        //提交数据库
        if (!relations.isEmpty()) {
            save(relations);
        }
        // 清除用户权限缓存
        AuthorityUtil.cleanAuthorizedCachesByDataRoleId(relation.getDataRoleId());
        //成功创建{0}个分配关系！
        return OperateResult.operationSuccess("00006", relations.size());
    }

    /**
     * 移除数据角色的分配关系
     *
     * @param relation 数据角色分配参数
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    //@CacheEvict(value = "getNormalUserAuthorizedEntities",allEntries = true)
    public OperateResult removeRelations(DataRoleRelation relation) {
        if (Objects.isNull(relation) || relation.getEntityIds().isEmpty()) {
            return OperateResult.operationSuccess("00007", 0);
        }
        List<DataRoleAuthTypeValue> values = dao.getRelationValues(relation);
        //执行删除
        if (values != null && !values.isEmpty()) {
            dao.deleteAll(values);
        }
        // 清除用户权限缓存
        AuthorityUtil.cleanAuthorizedCachesByDataRoleId(relation.getDataRoleId());
        //成功移除{0}个分配关系！
        return OperateResult.operationSuccess("00007", values != null ? values.size() : 0);
    }

    /**
     * 通过数据角色和权限类型获取已分配的业务实体数据
     *
     * @param roleId     数据角色Id
     * @param authTypeId 权限类型Id
     * @return 业务实体数据
     */
    public List<AuthEntityData> getAssignedAuthDataList(String roleId, String authTypeId) {
        //获取数据权限类型
        DataAuthorizeType authorizeType = dataAuthorizeTypeService.findOne(authTypeId);
        if (Objects.isNull(authorizeType)) {
            return Collections.emptyList();
        }
        List<String> entityIds = dao.getAssignedEntityIds(roleId, authTypeId);
        if (Objects.isNull(entityIds) || entityIds.isEmpty()) {
            return Collections.emptyList();
        }
        //调用API服务，获取业务实体
        String appModuleCode = authorizeType.getAuthorizeEntityType().getAppModule().getApiBaseAddress();
        String apiPath = authorizeType.getAuthorizeEntityType().getApiPath();
        List<AuthEntityData> authEntityDatas = dataAuthManager.getAuthEntityDataByIds(appModuleCode, apiPath, entityIds);
        // 清理并删除未定义的数据权限配置值
        removeUndefinedRelations(roleId, authTypeId, entityIds, authEntityDatas.stream().map(AuthEntityData::getId).collect(Collectors.toList()));
        return authEntityDatas;
    }

    /**
     * 通过数据角色和权限类型获取未分配的业务实体数据
     *
     * @param roleId     数据角色Id
     * @param authTypeId 权限类型Id
     * @return 业务实体数据
     */
    public List<AuthEntityData> getUnassignedAuthDataList(String roleId, String authTypeId) {
        //获取当前用户
        SessionUser sessionUser = ContextUtil.getSessionUser();
        //获取当前用户可分配的数据
        List<AuthEntityData> canAssigned = userService.getUserCanAssignAuthDataList(authTypeId, sessionUser.getUserId());
        //获取已经分配的数据
        List<AuthEntityData> assigned = getAssignedAuthDataList(roleId, authTypeId);
        Set<AuthEntityData> dataSet = new HashSet<>(canAssigned);
        dataSet.removeAll(assigned);
        return new ArrayList<>(dataSet);
    }

    /**
     * 通过数据角色和权限类型获取已分配的树形业务实体数据
     *
     * @param roleId     数据角色Id
     * @param authTypeId 权限类型Id
     * @return 树形业务实体数据
     */
    public List<AuthTreeEntityData> getAssignedAuthTreeDataList(String roleId, String authTypeId) {
        //获取数据权限类型
        DataAuthorizeType authorizeType = dataAuthorizeTypeService.findOne(authTypeId);
        if (Objects.isNull(authorizeType)) {
            return Collections.emptyList();
        }
        List<String> entityIds = dao.getAssignedEntityIds(roleId, authTypeId);
        if (Objects.isNull(entityIds) || entityIds.isEmpty()) {
            return Collections.emptyList();
        }
        //调用API服务，获取业务实体
        String appModuleCode = authorizeType.getAuthorizeEntityType().getAppModule().getApiBaseAddress();
        String apiPath =  authorizeType.getAuthorizeEntityType().getApiPath();
        List<AuthTreeEntityData> authTreeEntityDatas = dataAuthManager.getAuthTreeEntityDataByIds(appModuleCode, apiPath, entityIds);
        // 获取树形权限对象的所有节点清单
        List<String> authTreeEntityIds = new ArrayList<>(BaseTreeService.unBuildTreeIds(authTreeEntityDatas));
        // 清理并删除未定义的数据权限配置值
        removeUndefinedRelations(roleId, authTypeId, entityIds, authTreeEntityIds);
        return authTreeEntityDatas;
    }

    /**
     * 移除未定义的数据权限值(开启一个事务)
     * @param roleId     数据角色Id
     * @param authTypeId 权限类型Id
     * @param entityIds 已配置的业务实体Id清单
     * @param authEntityIds 已配置已定义的业务实体权限数据值
     */
    private void removeUndefinedRelations(String roleId, String authTypeId, List<String> entityIds, List<String> authEntityIds) {
        // 清理并删除未定义的数据权限配置值
        Set<String> undefinedIds = new HashSet<>();
        entityIds.forEach(id-> {
            if (!authEntityIds.contains(id)) {
                undefinedIds.add(id);
            }
        });
        if (CollectionUtils.isNotEmpty(undefinedIds)) {
            // 开启一个事务
            TransactionStatus transactionStatus = TransactionUtil.beginTransaction();
            DataRoleRelation removeRelations = new DataRoleRelation();
            removeRelations.setDataAuthorizeTypeId(authTypeId);
            removeRelations.setDataRoleId(roleId);
            removeRelations.setEntityIds(new ArrayList<>(undefinedIds));
            removeRelations(removeRelations);
            // 提交事务
            TransactionUtil.commit(transactionStatus);
        }
    }

    /**
     * 通过数据角色和权限类型获取未分配的树形业务实体数据(不去除已分配的节点)
     *
     * @param roleId     数据角色Id
     * @param authTypeId 权限类型Id
     * @return 树形业务实体数据
     */
    public List<AuthTreeEntityData> getUnassignedAuthTreeDataList(String roleId, String authTypeId) {
        //获取当前用户
        SessionUser sessionUser = ContextUtil.getSessionUser();
        //获取当前用户可分配的数据
        List<AuthTreeEntityData> dataSet = userService.getUserCanAssignAuthTreeDataList(authTypeId, sessionUser.getUserId());
        if (Objects.isNull(dataSet) || dataSet.isEmpty()) {
            return dataSet;
        }
        //获取已经分配的数据
        List<String> assignedIds = getAssignedEntityIds(roleId, authTypeId);
        if (CollectionUtils.isEmpty(assignedIds)) {
            return dataSet;
        }
        //删除已分配的数据
        return removeAssigned(dataSet, assignedIds);
    }

    /**
     * 去除已分配的权限数据
     *
     * @param allAuthData 所有权限数据
     * @param assignedIds 已分配id
     * @return
     */
    private List<AuthTreeEntityData> removeAssigned(List<AuthTreeEntityData> allAuthData, List<String> assignedIds) {
        // 获取所有子节点清单
        List<AuthTreeEntityData> nodes = BaseTreeService.unBuildTree(allAuthData);
        // 排除已经分配的Id
        nodes.removeIf(node-> assignedIds.contains(node.getId()));
        // 重新组装成树
        return BaseTreeService.buildTree(nodes);
    }

    /**
     * 通过数据角色和权限类型获取已分配的业务实体Id清单
     *
     * @param roleId     数据角色Id
     * @param authTypeId 权限类型Id
     * @return 业务实体Id清单
     */
    List<String> getAssignedEntityIds(String roleId, String authTypeId) {
        return dao.getAssignedEntityIds(roleId, authTypeId);
    }

    /**
     * 判断数据角色是否已经存在分配权限对象
     *
     * @param roleId 数据角色Id
     * @return 是否已经存在分配
     */
    boolean isAlreadyAssign(String roleId, String dataAuthTypeId) {
        return dao.isAlreadyAssign(roleId, dataAuthTypeId);
    }

    /**
     * 根据数据权限类型id以及数据id列表获取角色id列表
     *
     * @param dataAuthTypeId 数据权限类型id
     * @param entityIds      数据id列表
     * @return 角色id列表
     */
    public List<String> getRoleIds(String dataAuthTypeId, List<String> entityIds) {
        if (Objects.isNull(dataAuthTypeId) || Objects.isNull(entityIds) || entityIds.isEmpty()) {
            return Collections.emptyList();
        }
        return dao.getRoleIdsByDataAuthTypeAndEntityIds(dataAuthTypeId, entityIds);
    }

    /**
     * 通过数据角色Id获取数据权限类型
     * @param roleId 数据角色Id
     * @return 数据权限类型清单
     */
    public List<DataAuthorizeType> getAuthorizeTypesByRoleId(String roleId) {
        return dao.getDataAuthTypesByRoleId(roleId);
    }
}
