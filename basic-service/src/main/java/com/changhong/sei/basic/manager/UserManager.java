package com.changhong.sei.basic.manager;

import com.changhong.sei.apitemplate.ApiTemplate;
import com.changhong.sei.basic.dao.EmployeeDao;
import com.changhong.sei.basic.dao.MenuDao;
import com.changhong.sei.basic.dao.TenantDao;
import com.changhong.sei.basic.dao.UserDao;
import com.changhong.sei.basic.dto.Executor;
import com.changhong.sei.basic.dto.FeatureType;
import com.changhong.sei.basic.dto.MenuDto;
import com.changhong.sei.basic.dto.RoleType;
import com.changhong.sei.basic.entity.*;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.auth.AuthEntityData;
import com.changhong.sei.core.dto.auth.AuthTreeEntityData;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.local.LocalUtil;
import com.changhong.sei.core.manager.BaseEntityManager;
import com.changhong.sei.core.manager.bo.OperateResult;
import com.chonghong.sei.enums.UserAuthorityPolicy;
import com.chonghong.sei.enums.UserType;
import com.chonghong.sei.exception.ServiceException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.changhong.sei.basic.manager.DataRoleAuthTypeValueManager.FIND_ALL_AUTH_TREE_ENTITY_DATA_METHOD;
import static com.changhong.sei.basic.manager.DataRoleAuthTypeValueManager.GET_AUTH_TREE_ENTITY_DATA_METHOD;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：实现用户的业务逻辑服务
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/14 9:46        秦有宝                      新建
 * <p/>
 * *************************************************************************************************
 */
@Service
public class UserManager extends BaseEntityManager<User> {
    @Autowired
    private UserDao userDao;
    @Autowired
    private FeatureRoleManager featureRoleManager;
    @Autowired
    private UserFeatureRoleManager userFeatureRoleManager;
    @Autowired
    private EmployeePositionManager employeePositionManager;
    @Autowired
    private PositionFeatureRoleManager positionFeatureRoleManager;
    @Autowired
    private FeatureRoleFeatureManager featureRoleFeatureManager;
    @Autowired
    private FeatureManager featureManager;
    @Autowired
    private TenantAppModuleManager tenantAppModuleManager;
    @Autowired
    private TenantDao tenantDao;
    @Autowired
    private MenuManager menuManager;
    @Autowired
    private DataAuthorizeTypeManager dataAuthorizeTypeManager;
    @Autowired
    private UserDataRoleManager userDataRoleManager;
    @Autowired
    private PositionDataRoleManager positionDataRoleManager;
    @Autowired
    private DataRoleManager dataRoleManager;
    @Autowired
    private DataRoleAuthTypeValueManager dataRoleAuthTypeValueManager;
    @Autowired
    private PositionManager positionManager;
    @Autowired
    private EmployeeDao employeeDao;
    @Autowired
    private AppModuleManager appModuleManager;
    @Autowired
    private FeatureGroupManager featureGroupManager;
    //注入，以便服务之间调用能够缓存
    @Autowired
    private UserManager userManager;
    @Autowired
    private ApiTemplate apiTemplate;

    @Override
    protected BaseEntityDao<User> getDao() {
        return userDao;
    }

    /**
     * 根据用户id查询用户
     *
     * @param id 用户id
     * @return 用户
     */
    public User findById(String id) {
        return userDao.getById(id);
    }

    /**
     * 获取租户管理员可以使用的功能项清单
     *
     * @param tenantCode 租户代码
     * @return 功能项清单
     */
    private List<Feature> getTenantAdminCanUseFeatures(String tenantCode) {
        //获取租户
        Tenant tenant = tenantDao.findByFrozenFalseAndCode(tenantCode);
        if (tenant == null) {
            return Collections.emptyList();
        }
        //获取租户已经分配的应用模块对应的功能项清单
        return featureManager.getTenantCanUseFeatures(tenant);
    }

    /**
     * 获取用户有权限的功能项清单
     *
     * @param userId 平台用户Id
     * @return 功能项清单
     */
    @Cacheable(value = "UserAuthorizedFeaturesCache", key = "'UserAuthorizedFeatures:'+#userId")
    public List<Feature> getUserAuthorizedFeatures(String userId) {
        List<Feature> result = new ArrayList<>();
        //获取用户
        User user = findOne(userId);
        if (user == null) {
            return result;
        }
        UserAuthorityPolicy authorityPolicy = user.getUserAuthorityPolicy();
        //判断全局管理员
        if (authorityPolicy == UserAuthorityPolicy.GlobalAdmin) {
            return result;
        }
        //判断租户管理员
        if (authorityPolicy == UserAuthorityPolicy.TenantAdmin) {
            //获取租户管理员可以使用的功能项清单
            return getTenantAdminCanUseFeatures(user.getTenantCode());
        }
        //一般用户的功能角色
        Set<FeatureRole> userRoles = new HashSet<>();
        //获取用户的公共角色
        List<FeatureRole> publicRoles = featureRoleManager.getPublicFeatureRoles(user);
        userRoles.addAll(publicRoles);
        //获取用户授权的角色
        List<FeatureRole> authRoles = userFeatureRoleManager.getChildrenFromParentId(user.getId());
        //添加可以使用的角色
        authRoles.forEach((r) -> {
            if (r.getRoleType() == RoleType.CanUse) {
                userRoles.add(r);
            }
        });
        //获取用户的岗位
        if (user.getUserType() == UserType.Employee) {
            List<Position> positions = employeePositionManager.getChildrenFromParentId(user.getId());
            List<String> positionIds = new ArrayList<>();
            positions.forEach((p) -> positionIds.add(p.getId()));
            //获取岗位对应的角色
            List<FeatureRole> positionRoles = positionFeatureRoleManager.getChildrenFromParentIds(positionIds);
            //添加可以使用的角色
            positionRoles.forEach((r) -> {
                if (r.getRoleType() == RoleType.CanUse) {
                    userRoles.add(r);
                }
            });
        }
        //获取角色分配的功能项清单
        if (userRoles.isEmpty()) {
            return result;
        }
        List<String> userRoleIds = new ArrayList<>();
        userRoles.forEach((r) -> userRoleIds.add(r.getId()));
        List<Feature> features = featureRoleFeatureManager.getChildrenFromParentIds(userRoleIds);
        result.addAll(features);
        return result;
    }

    /**
     * 获取用户有权限的操作菜单树(VO)
     *
     * @param userId 用户Id
     * @return 操作菜单树
     */
    @Cacheable(value = "UserAuthorizedMenusCache", key = "'UserAuthorizedMenus:'+#userId")
    public List<Menu> getUserAuthorizedMenus(String userId) {
        //获取用户
        User user = findOne(userId);
        if (user == null) {
            throw new ServiceException("租户【" + ContextUtil.getTenantCode() + "】,用户【" + userId + "】不存在！");
        }

        Set<Menu> userMenus = new HashSet<>();
        UserAuthorityPolicy authorityPolicy = user.getUserAuthorityPolicy();
        //判断全局管理员
        if (authorityPolicy == UserAuthorityPolicy.GlobalAdmin) {
            userMenus.addAll(menuManager.findAll());
        } else {
            //判断租户管理员和一般用户
            //--获取用户有权限的功能项清单
            List<Feature> userFeatures = userManager.getUserAuthorizedFeatures(userId);
            //通过功能项清单获取菜单节点
            List<Menu> memuNodes = menuManager.findByFeatures(userFeatures);
            if (CollectionUtils.isNotEmpty(memuNodes)) {
                //拼接菜单关联的父节点
                memuNodes.forEach((m) -> {
                    List<Menu> parents = menuManager.getParentNodes(m, true);
                    userMenus.addAll(parents);
                });
            }
        }
        // 菜单多语言
        LocalUtil.localSet(ContextUtil.getAppCode(), Menu.class, userMenus);
        //通过菜单生成展示对象
        userMenus.forEach((m) -> {
            //环境格式化
            //vo.setFeatureUrl(GlobalParam.environmentFormat(vo.getFeatureUrl()));
            Feature feature = m.getFeature();
            if (Objects.nonNull(feature)) {
                String baseAddress = feature.getFeatureGroup().getAppModule().getWebBaseAddress();
                StringBuilder url = new StringBuilder(64);
                url.append(ContextUtil.getProperty(baseAddress)).append(feature.getUrl());
                m.setMenuUrl(url.toString());
            }
        });
        //构造菜单树
        return MenuManager.buildTree(new ArrayList<>(userMenus));
    }

    /**
     * 获取用户有权限分配的功能项清单
     *
     * @param userId 用户Id
     * @return 可分配的功能项清单
     */
    @Cacheable(value = "UserCanAssignFeaturesCache", key = "'UserCanAssignFeatures:'+#userId")
    public List<Feature> getUserCanAssignFeatures(String userId) {
        List<Feature> result = new ArrayList<>();
        //获取用户
        User user = findOne(userId);
        if (user == null) {
            return result;
        }
        UserAuthorityPolicy authorityPolicy = user.getUserAuthorityPolicy();
        //判断全局管理员
        if (authorityPolicy == UserAuthorityPolicy.GlobalAdmin) {
            return featureManager.findAll();
        }
        //判断租户管理员
        if (authorityPolicy == UserAuthorityPolicy.TenantAdmin) {
            //获取租户管理员可以使用的功能项清单
            return getTenantAdminCanUseFeatures(user.getTenantCode());
        }
        //一般用户的可分配功能角色
        Set<FeatureRole> userRoles = new HashSet<>();
        List<FeatureRole> authRoles = userFeatureRoleManager.getChildrenFromParentId(user.getId());
        //添加可以分配的角色
        authRoles.forEach((r) -> {
            if (r.getRoleType() == RoleType.CanAssign) {
                userRoles.add(r);
            }
        });
        //获取用户的岗位
        if (user.getUserType() == UserType.Employee) {
            List<Position> positions = employeePositionManager.getChildrenFromParentId(user.getId());
            List<String> positionIds = new ArrayList<>();
            positions.forEach((p) -> positionIds.add(p.getId()));
            //获取岗位对应的角色
            List<FeatureRole> positionRoles = positionFeatureRoleManager.getChildrenFromParentIds(positionIds);
            //添加可以分配的角色
            positionRoles.forEach((r) -> {
                if (r.getRoleType() == RoleType.CanAssign) {
                    userRoles.add(r);
                }
            });
        }
        //获取角色分配的功能项清单
        if (userRoles.isEmpty()) {
            return result;
        }
        List<String> userRoleIds = new ArrayList<>();
        userRoles.forEach((r) -> userRoleIds.add(r.getId()));
        List<Feature> features = featureRoleFeatureManager.getChildrenFromParentIds(userRoleIds);
        result.addAll(features);
        return result;
    }

    /**
     * 获取用户前端权限检查的功能项键值
     *
     * @param userId 用户Id
     * @return 功能项键值
     */
    @Cacheable(value = "UserAuthorizedFeatureMapsCache", key = "'UserAuthorizedFeatureMaps:'+#userId")
    public Map<String, Map<String, String>> getUserAuthorizedFeatureMaps(String userId) {
        Map<String, Map<String, String>> result = new HashMap<>();
        //获取用户有权限的功能项清单
        List<Feature> authFeatures = userManager.getUserAuthorizedFeatures(userId);
        //是全局管理员
        if (CollectionUtils.isNotEmpty(authFeatures)) {
            //循环构造键值对
            for (Feature feature : authFeatures) {
                FeatureGroup featureGroup = feature.getFeatureGroup();
                if (featureGroup == null) {
                    continue;
                }
                AppModule appModule = featureGroup.getAppModule();
                if (appModule == null) {
                    continue;
                }
                //只添加操作功能项
                if (feature.getFeatureType() != FeatureType.Operate) {
                    continue;
                }
                //判断应用模块键值是否存在
                if (!result.containsKey(appModule.getWebBaseAddress())) {
                    result.put(appModule.getWebBaseAddress(), new HashMap<>());
                }
                //添加功能项
                result.get(appModule.getWebBaseAddress()).put(feature.getCode(), feature.getUrl());
            }
        }
        return result;
    }

    /***
     * 判断用户是否有指定功能项的权限
     * @param userId 用户Id
     * @param featureCode 功能项代码
     * @return 有无权限
     */
    public boolean hasFeatureAuthority(String userId, String featureCode) {
        //获取用户
        User user = findOne(userId);
        if (user == null) {
            return false;
        }
        UserAuthorityPolicy authorityPolicy = user.getUserAuthorityPolicy();
        //判断全局管理员
        if (authorityPolicy == UserAuthorityPolicy.GlobalAdmin) {
            return true;
        }
        //判断租户管理员
        if (authorityPolicy == UserAuthorityPolicy.TenantAdmin) {
            //获取功能项
            Feature feature = featureManager.findByCode(featureCode);
            if (feature == null || feature.getFeatureGroup() == null || feature.getFeatureGroup().getAppModule() == null) {
                return false;
            }
            if (!feature.getTenantCanUse()) {
                return false;
            }
            AppModule appModule = feature.getFeatureGroup().getAppModule();
            //获取租户
            Tenant tenant = tenantDao.findByFrozenFalseAndCode(user.getTenantCode());
            if (tenant == null) {
                return false;
            }
            TenantAppModule tenantAppModule = tenantAppModuleManager.getRelation(tenant.getId(), appModule.getId());
            return tenantAppModule != null;
        }
        //先从缓存获取 2019年3月13日:采用spring cache自动代理注入缓存
        //Object cacheFeatures = redisTemplate.opsForValue().get("UserAuthorizedFeatures:"+userId);
        List<Feature> features;
        /*if (cacheFeatures!=null&& cacheFeatures instanceof List){
            List cacheFeaturesList = (List)cacheFeatures;
            features = new ArrayList<>();
            for (Object cache:cacheFeaturesList){
                if (cache instanceof Feature){
                    features.add((Feature)cache);
                }
            }
        }
        else {*/
        features = userManager.getUserAuthorizedFeatures(userId);
        //}
        //判断是否已经授权
        Feature authorized = features.stream().filter((r) -> Objects.equals(r.getCode(), featureCode)).findAny().orElse(null);
        return authorized != null;
    }

    /**
     * 清除用户权限相关的所有缓存
     */
    public void clearUserAuthorizedCaches(String userId) {
        if (StringUtils.isBlank(userId)) {
            return;
        }
        String pattern = "*" + userId;
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 获取用户可以分配的数据权限业务实体清单
     *
     * @param dataAuthTypeId 数据权限类型Id
     * @param userId         用户Id
     * @return 数据权限业务实体清单
     */
    @Cacheable(value = "UserCanAssignAuthDataList", key = "'UserCanAssignAuthDataList:'+#dataAuthTypeId+':'+#userId")
    public List<AuthEntityData> getUserCanAssignAuthDataList(String dataAuthTypeId, String userId) {
        //获取数据权限类型
        DataAuthorizeType authorizeType = dataAuthorizeTypeManager.findOne(dataAuthTypeId);
        if (authorizeType == null) {
            return Collections.emptyList();
        }
        //获取用户
        User user = findOne(userId);
        if (user == null) {
            return Collections.emptyList();
        }
        UserAuthorityPolicy authorityPolicy = user.getUserAuthorityPolicy();
        //判断是全局管理员，不能分配数据权限
        if (authorityPolicy == UserAuthorityPolicy.GlobalAdmin) {
            return Collections.emptyList();
        }
        //判断是租户管理员，可以分配租户的所有数据
        if (authorityPolicy == UserAuthorityPolicy.TenantAdmin) {
            //调用API服务，获取业务实体
            String appModuleCode = authorizeType.getAuthorizeEntityType().getAppModule().getApiBaseAddress();
            String path = String.format("%s/%s", authorizeType.getAuthorizeEntityType().getApiPath(), DataRoleAuthTypeValueManager.FIND_ALL_AUTH_ENTITY_DATA_METHOD);
            ResultData resultData = apiTemplate.getByAppModuleCode(appModuleCode, path, ResultData.class);
            if (resultData.isFailed()){
                return new ArrayList<>();
            }
            return (List<AuthEntityData>)resultData.getData();
        }
        //一般用户，通过数据角色获取业务实体清单
        Set<AuthEntityData> entities = new HashSet<>();
        //一般用户的数据角色
        Set<DataRole> userRoles = getNormalUserDataRoles(user);
        //获取角色分配的数据权限业务实体清单
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        userRoles.forEach((r) -> {
            List<AuthEntityData> dataList = dataRoleAuthTypeValueManager.getAssignedAuthDataList(r.getId(), dataAuthTypeId);
            entities.addAll(dataList);
        });
        return new ArrayList<>(entities);
    }

    /**
     * 获取一般用户的数据角色清单
     *
     * @param user 用户
     * @return 数据角色清单
     */
    private Set<DataRole> getNormalUserDataRoles(User user) {
        //一般用户的数据角色
        Set<DataRole> userRoles = new HashSet<>();
        //获取用户的公共角色
        List<DataRole> publicRoles = dataRoleManager.getPublicDataRoles(user);
        userRoles.addAll(publicRoles);
        //一般用户的角色
        List<DataRole> authRoles = userDataRoleManager.getChildrenFromParentId(user.getId());
        userRoles.addAll(authRoles);
        //获取用户的岗位
        if (user.getUserType() == UserType.Employee) {
            List<Position> positions = employeePositionManager.getChildrenFromParentId(user.getId());
            List<String> positionIds = new ArrayList<>();
            positions.forEach((p) -> positionIds.add(p.getId()));
            //获取岗位对应的角色
            List<DataRole> positionRoles = positionDataRoleManager.getChildrenFromParentIds(positionIds);
            userRoles.addAll(positionRoles);
        }
        return userRoles;
    }

    /**
     * 获取一般用户有权限的业务实体Id清单
     *
     * @param entityClassName 权限对象类名
     * @param featureCode     功能项代码
     * @param userId          用户Id
     * @return 业务实体Id清单
     */
    @Cacheable(value = "getNormalUserAuthorizedEntities", key = "'NormalUserAuthorizedEntities:'+#entityClassName+':'+#featureCode+':'+#userId")
    public List<String> getNormalUserAuthorizedEntities(String entityClassName, String featureCode, String userId) {
        //获取用户
        User user = findOne(userId);
        if (user == null) {
            return Collections.emptyList();
        }
        if (user.getUserAuthorityPolicy() != UserAuthorityPolicy.NormalUser) {
            return Collections.emptyList();
        }
        //获取数据权限类型
        DataAuthorizeType dataAuthType = dataAuthorizeTypeManager.getByEntityClassNameAndFeature(entityClassName, featureCode);
        if (dataAuthType == null) {
            return Collections.emptyList();
        }
        //一般用户，通过数据角色获取业务实体清单
        Set<String> entityIds = new HashSet<>();
        //一般用户的数据角色
        Set<DataRole> userRoles = getNormalUserDataRoles(user);
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        userRoles.forEach((r) -> {
            List<String> ids = dataRoleAuthTypeValueManager.getAssignedEntityIds(r.getId(), dataAuthType.getId());
            entityIds.addAll(ids);
        });
        return new ArrayList<>(entityIds);
    }

    /**
     * 获取用户可以分配的数据权限树形业务实体清单
     *
     * @param dataAuthTypeId 数据权限类型Id
     * @param userId         用户Id
     * @return 数据权限树形业务实体清单
     */
    @Cacheable(value = "UserCanAssignAuthTreeDataList", key = "'UserCanAssignAuthTreeDataList:'+#dataAuthTypeId+':'+#userId")
    public List<AuthTreeEntityData> getUserCanAssignAuthTreeDataList(String dataAuthTypeId, String userId) {
        //获取数据权限类型
        DataAuthorizeType authorizeType = dataAuthorizeTypeManager.findOne(dataAuthTypeId);
        if (authorizeType == null) {
            return Collections.emptyList();
        }
        //获取用户
        User user = findOne(userId);
        if (user == null) {
            return Collections.emptyList();
        }
        UserAuthorityPolicy authorityPolicy = user.getUserAuthorityPolicy();
        //判断是全局管理员，不能分配数据权限
        if (authorityPolicy == UserAuthorityPolicy.GlobalAdmin) {
            return Collections.emptyList();
        }
        //判断是租户管理员，可以分配租户的所有数据
        if (authorityPolicy == UserAuthorityPolicy.TenantAdmin) {
            //调用API服务，获取业务实体
            String appModuleCode = authorizeType.getAuthorizeEntityType().getAppModule().getApiBaseAddress();
            String path = String.format("%s/%s", authorizeType.getAuthorizeEntityType().getApiPath(), FIND_ALL_AUTH_TREE_ENTITY_DATA_METHOD);
            ResultData resultData = apiTemplate.getByAppModuleCode(appModuleCode, path, ResultData.class);
            if (resultData.isFailed()){
                return new ArrayList<>();
            }
            return (List<AuthTreeEntityData>)resultData.getData();
        }
        //一般用户，通过数据角色获取业务实体清单
        Set<String> entityIds = new HashSet<>();
        //一般用户的数据角色
        Set<DataRole> userRoles = getNormalUserDataRoles(user);
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        userRoles.forEach((r) -> {
            List<String> ids = dataRoleAuthTypeValueManager.getAssignedEntityIds(r.getId(), dataAuthTypeId);
            entityIds.addAll(ids);
        });
        //通过业务实体Id清单获取树形业务实体
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
     * 查询可分配的功能角色
     *
     * @param featureRoleGroupId 功能角色组id
     * @param userId             用户id
     * @return 功能角色清单
     */
    public List<FeatureRole> getCanAssignedFeatureRoles(String featureRoleGroupId, String userId) {
        Set<FeatureRole> result = new HashSet<>();
        //获取可分配的功能角色
        List<FeatureRole> canAssigned = featureRoleManager.getCanAssignedRoles(featureRoleGroupId);
        //获取已经分配的功能角色
        List<FeatureRole> assigned = userFeatureRoleManager.getChildrenFromParentId(userId);
        result.addAll(canAssigned);
        result.removeAll(assigned);
        return new ArrayList<>(result);
    }

    /**
     * 查询可分配的数据角色
     *
     * @param dataRoleGroupId 数据角色组id
     * @param userId          用户id
     * @return 数据角色清单
     */
    public List<DataRole> getCanAssignedDataRoles(String dataRoleGroupId, String userId) {
        Set<DataRole> result = new HashSet<>();
        //获取可分配的数据角色
        List<DataRole> canAssigned = dataRoleManager.getCanAssignedRoles(dataRoleGroupId);
        //获取已经分配的功能角色
        List<DataRole> assigned = userDataRoleManager.getChildrenFromParentId(userId);
        result.addAll(canAssigned);
        result.removeAll(assigned);
        return new ArrayList<>(result);
    }

    /**
     * 测试后台作业服务方法
     *
     * @return 操作结果
     */
    public OperateResult taskOne() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return OperateResult.operationSuccess("执行成功");
    }

    /**
     * 测试后台作业服务方法
     *
     * @return 操作结果
     */
    public OperateResult taskTwo() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return OperateResult.operationSuccess("执行成功");
    }

    /**
     * 根据用户的id列表获取执行人（如果有员工信息，另赋值组织机构和岗位信息）
     *
     * @param userIds 用户的id列表
     * @return
     */
    public List<Executor> getExecutorsByUserIds(List<String> userIds) {
        if (userIds == null || userIds.size() == 0) {
            return Collections.emptyList();
        }
        List<User> users = findByIds(userIds);
        List<Executor> executors = new ArrayList<>();
        for (User r : users) {
            //排除冻结的用户
            if (r.getFrozen()) {
                continue;
            }
            executors.add(construstExecutor(r));
        }
        return executors;
    }

    /**
     * 根据公司IDS与岗位分类IDS获取执行人
     *
     * @param corpIds    公司IDS
     * @param postCatIds 岗位分类IDS
     * @return
     */
    public List<Executor> getExecutorsByPostCatAndCorp(List<String> corpIds, List<String> postCatIds) {
        if (Objects.isNull(corpIds) || corpIds.isEmpty() || Objects.isNull(postCatIds) || postCatIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Executor> es = new ArrayList<>();
        //1.根据岗位类别筛选出所有岗位
        List<Position> posts = positionManager.findByFilter(new SearchFilter(Position.POSITION_CATEGORY_ID, postCatIds, SearchFilter.Operator.IN));
        if (Objects.nonNull(posts) && !posts.isEmpty()) {
            //2.根据给定公司ids找到有该公司的角色ids
            //2.1根据类名获取数据权限类型
            DataAuthorizeType corpAuthorizeType = dataAuthorizeTypeManager.getByEntityClassNameAndFeature(Corporation.class.getName(), null);
            if (Objects.nonNull(corpAuthorizeType)) {
                String dataAuthTypeId = corpAuthorizeType.getId();
                //2.2根据数据权限类型id和公司实体id查找所有的角色id列表
                List<String> dataRoleIds = dataRoleAuthTypeValueManager.getRoleIds(dataAuthTypeId, corpIds);
                if (Objects.nonNull(dataRoleIds) && !dataRoleIds.isEmpty()) {
                    //3.根据岗位查找所有的员工
                    Set<String> positionIds = posts.stream().map(Position::getId).collect(Collectors.toSet());
                    List<Employee> employees = employeePositionManager.getParentsFromChildIds(new ArrayList<>(positionIds));
                    //4.筛选出具有上述角色id的员工
                    Iterator<Employee> iterator = employees.iterator();
                    while (iterator.hasNext()) {
                        Employee employee = iterator.next();
                        Set<DataRole> dataRoles = getNormalUserDataRoles(employee.getUser());
                        List<String> userDataRoleIds = dataRoles.stream().map(DataRole::getId).collect(Collectors.toList());
                        //该员工没有对应角色，移除
                        Boolean contained = false;
                        for (String dataRoleId : dataRoleIds) {
                            if (userDataRoleIds.contains(dataRoleId)) {
                                contained = Boolean.TRUE;
                                break;
                            }
                        }
                        if (!contained) {
                            iterator.remove(); //注意这个地方,直接删除list会出现ConcurrentModificationException
                        }
                    }
                    //转化为执行人
                    employees.forEach(e -> es.add(construstExecutor(e)));
                }
            }
        }
        return es;
    }

    /**
     * 获取用户是否有该页面的权限
     *
     * @param userId        用户Id
     * @param pageGroupCode 功能项页面分组代码(react页面路由)
     * @return 有权限则data返回有权限的功能项集合
     */
    @Cacheable(value = "UserAuthorizedFeature", key = "'UserAuthorizedFeature:'+#pageGroupCode+':'+#userId")
    public ResultData<Map<String, String>> getUserAuthorizedFeature(String userId, String pageGroupCode) {
        //判断参数不能为空
        if (StringUtils.isBlank(userId)) {
            return ResultData.fail(ContextUtil.getMessage("00067", "userId"));
        }
        if (StringUtils.isBlank(pageGroupCode)) {
            return ResultData.fail(ContextUtil.getMessage("00067", "pageGroupCode"));
        }
        //获取用户有权限的功能项清单
        Map<String, String> data = new HashMap<>();
        List<Feature> authFeatures = getUserAuthorizedFeatures(userId);
        //是全局管理员
        if (CollectionUtils.isNotEmpty(authFeatures)) {
            //循环构造键值对
            for (Feature feature : authFeatures) {
                //只添加操作功能项
                if (feature.getFeatureType() != FeatureType.Operate) {
                    continue;
                }
                if (StringUtils.equals(pageGroupCode, feature.getGroupCode())) {
                    data.put(feature.getCode(), feature.getUrl());
                }
            }
        }
        return ResultData.success(data);
    }

    /**
     * 通过用户构造流程任务执行人
     *
     * @param user 用户
     */
    private Executor construstExecutor(User user) {
        Executor executor = new Executor();
        executor.setId(user.getId());
        executor.setName(user.getUserName());

        Employee employee = employeeDao.findOne(user.getId());
        if (employee != null) {
            executor.setCode(employee.getCode());
            //设置组织机构
            Organization organization = employee.getOrganization();
            if (Objects.nonNull(organization)) {
                executor.setOrganizationId(organization.getId());
                executor.setOrganizationCode(organization.getCode());
                executor.setOrganizationName(organization.getName());
                executor.setRemark(organization.getName());
            }
            //设置岗位
            List<Position> positions = employeePositionManager.getChildrenFromParentId(employee.getId());
            if (Objects.nonNull(positions) && !positions.isEmpty()) {
                //筛选出指定组织的岗位
                List<Position> resultPositions;
                if (Objects.nonNull(organization)) {
                    resultPositions = positions.stream().filter(p -> StringUtils.equals(organization.getId(), p.getOrganization().getId())).collect(Collectors.toList());
                    //避免该人员可能在所处组织下没有岗位 下一部get(0)数组越界
                    if (resultPositions.isEmpty()) {
                        resultPositions = positions;
                    }
                } else {
                    resultPositions = positions;
                }
                Position position = resultPositions.get(0);
                executor.setPositionId(position.getId());
                executor.setPositionName(position.getName());
                executor.setPositionCode(position.getCode());
                executor.setRemark(position.getOrganization().getName() + "-" + position.getName());
            }
        }
        return executor;
    }

    /**
     * 通过企业用户构造流程任务执行人
     *
     * @param employee 企业用户
     */
    private Executor construstExecutor(Employee employee) {
        Executor executor = new Executor();
        executor.setId(employee.getId());
        executor.setCode(employee.getCode());
        executor.setName(employee.getUserName());
        //设置组织机构
        Organization organization = employee.getOrganization();
        if (Objects.nonNull(organization)) {
            executor.setOrganizationId(organization.getId());
            executor.setOrganizationCode(organization.getCode());
            executor.setOrganizationName(organization.getName());
            executor.setRemark(organization.getName());
        }
        //设置岗位
        List<Position> positions = employeePositionManager.getChildrenFromParentId(employee.getId());
        if (Objects.nonNull(positions) && !positions.isEmpty()) {
            //筛选出指定组织的岗位
            List<Position> resultPositions;
            if (Objects.nonNull(organization)) {
                resultPositions = positions.stream().filter(p -> StringUtils.equals(organization.getId(), p.getOrganization().getId())).collect(Collectors.toList());
                //避免该人员可能在所处组织下没有岗位 下一部get(0)数组越界
                if (resultPositions.isEmpty()) {
                    resultPositions = positions;
                }
            } else {
                resultPositions = positions;
            }
            Position position = resultPositions.get(0);
            executor.setPositionId(position.getId());
            executor.setPositionName(position.getName());
            executor.setPositionCode(position.getCode());
            executor.setRemark(position.getOrganization().getName() + "-" + position.getName());
        }
        return executor;
    }
}