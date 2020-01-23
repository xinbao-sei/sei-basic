package com.changhong.sei.basic.manager;

import com.changhong.sei.apitemplate.ApiTemplate;
import com.changhong.sei.basic.dao.DataRoleAuthTypeValueDao;
import com.changhong.sei.basic.dto.DataRoleRelation;
import com.changhong.sei.basic.entity.DataAuthorizeType;
import com.changhong.sei.basic.entity.DataRoleAuthTypeValue;
import com.changhong.sei.basic.manager.util.AuthorityUtil;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.context.SessionUser;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.auth.AuthEntityData;
import com.changhong.sei.core.dto.auth.AuthTreeEntityData;
import com.changhong.sei.core.manager.BaseEntityManager;
import com.changhong.sei.core.manager.bo.OperateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 实现功能：数据角色分配权限类型的值业务逻辑实现
 *
 * @author 王锦光(wangj)
 * @version 1.0.00      2017-05-04 14:04
 */
@Component
public class DataRoleAuthTypeValueManager extends BaseEntityManager<DataRoleAuthTypeValue> {
    static final String GET_AUTH_ENTITY_DATA_METHOD = "getAuthEntityDataByIds";
    static final String GET_AUTH_TREE_ENTITY_DATA_METHOD = "getAuthTreeEntityDataByIds";
    static final String FIND_ALL_AUTH_ENTITY_DATA_METHOD = "findAllAuthEntityData";
    static final String FIND_ALL_AUTH_TREE_ENTITY_DATA_METHOD = "findAllAuthTreeEntityData";
    @Autowired
    private DataRoleAuthTypeValueDao dao;
    @Autowired
    private DataAuthorizeTypeManager dataAuthorizeTypeManager;
    @Autowired
    private UserManager userManager;
    @Autowired
    private ApiTemplate apiTemplate;

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
    @CacheEvict(value = "getNormalUserAuthorizedEntities",allEntries = true)
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
    @Transactional
    @CacheEvict(value = "getNormalUserAuthorizedEntities",allEntries = true)
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
        DataAuthorizeType authorizeType = dataAuthorizeTypeManager.findOne(authTypeId);
        if (Objects.isNull(authorizeType)) {
            return Collections.emptyList();
        }
        List<String> entityIds = dao.getAssignedEntityIds(roleId, authTypeId);
        if (Objects.isNull(entityIds) || entityIds.isEmpty()) {
            return Collections.emptyList();
        }
        //调用API服务，获取业务实体
        String appModuleCode = authorizeType.getAuthorizeEntityType().getAppModule().getApiBaseAddress();
        String path = String.format("%s/%s", authorizeType.getAuthorizeEntityType().getApiPath(), GET_AUTH_ENTITY_DATA_METHOD);
        ResultData resultData = apiTemplate.postByAppModuleCode(appModuleCode, path, ResultData.class, entityIds);
        if (resultData.isFailed()){
            return new ArrayList<>();
        }
        return (List<AuthEntityData>)resultData.getData();
    }

    /**
     * 通过数据角色和权限类型获取未分配的业务实体数据
     *
     * @param roleId     数据角色Id
     * @param authTypeId 权限类型Id
     * @return 业务实体数据
     */
    public List<AuthEntityData> getUnassignedAuthDataList(String roleId, String authTypeId) {
        Set<AuthEntityData> dataSet = new HashSet<>();
        //获取当前用户
        SessionUser sessionUser = ContextUtil.getSessionUser();
        //获取当前用户可分配的数据
        List<AuthEntityData> canAssigned = userManager.getUserCanAssignAuthDataList(authTypeId, sessionUser.getUserId());
        //获取已经分配的数据
        List<AuthEntityData> assigned = getAssignedAuthDataList(roleId, authTypeId);
        dataSet.addAll(canAssigned);
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
        DataAuthorizeType authorizeType = dataAuthorizeTypeManager.findOne(authTypeId);
        if (Objects.isNull(authorizeType)) {
            return Collections.emptyList();
        }
        List<String> entityIds = dao.getAssignedEntityIds(roleId, authTypeId);
        if (Objects.isNull(entityIds) || entityIds.isEmpty()) {
            return Collections.emptyList();
        }
        //调用API服务，获取业务实体
        String appModuleCode = authorizeType.getAuthorizeEntityType().getAppModule().getApiBaseAddress();
        String path = String.format("%s/%s", authorizeType.getAuthorizeEntityType().getApiPath(), GET_AUTH_TREE_ENTITY_DATA_METHOD);
        ResultData resultData = apiTemplate.postByAppModuleCode(appModuleCode, path, ResultData.class, entityIds);
        if (resultData.isFailed()){
            return new ArrayList<>();
        }
        return (List<AuthTreeEntityData>)resultData.getData();
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
        List<AuthTreeEntityData> dataSet = userManager.getUserCanAssignAuthTreeDataList(authTypeId, sessionUser.getUserId());
        if (Objects.isNull(dataSet) || dataSet.isEmpty()) {
            return dataSet;
        }
        //获取已经分配的数据
        List<String> assignedIds = getAssignedEntityIds(roleId, authTypeId);
        if (Objects.isNull(assignedIds) || assignedIds.isEmpty()) {
            return dataSet;
        }
        //删除已分配的数据
        removeAssigned(dataSet, assignedIds);
        return new ArrayList<>(dataSet);
    }

    /**
     * 去除已分配的权限数据
     *
     * @param allAuthData 所有权限数据
     * @param assignedIds 已分配id
     * @return
     */
    private void removeAssigned(List<AuthTreeEntityData> allAuthData, List<String> assignedIds) {
        allAuthData.removeIf(authTreeEntityData -> Objects.isNull(authTreeEntityData.getChildren()) && assignedIds.contains(authTreeEntityData.getId()));
        //获取子级的列表
        for (AuthTreeEntityData tree : allAuthData) {
            List<AuthTreeEntityData> child = tree.getChildren();
            if (Objects.nonNull(child) && !child.isEmpty()) {
                removeAssigned(child, assignedIds);
            }
        }
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
}