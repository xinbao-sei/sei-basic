package com.changhong.sei.basic.manager;

import com.changhong.sei.basic.dao.EmployeeDao;
import com.changhong.sei.basic.dao.OrganizationDao;
import com.changhong.sei.basic.dto.EmployeeCopyParam;
import com.changhong.sei.basic.dto.EmployeeQueryParam;
import com.changhong.sei.basic.dto.Executor;
import com.changhong.sei.basic.dto.UserQueryParam;
import com.changhong.sei.basic.entity.*;
import com.changhong.sei.basic.manager.util.EmailUtil;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.*;
import com.changhong.sei.core.manager.BaseEntityManager;
import com.changhong.sei.core.manager.bo.OperateResult;
import com.changhong.sei.core.manager.bo.OperateResultWithData;
import com.chonghong.sei.enums.UserAuthorityPolicy;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：实现企业员工的业务逻辑服务
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/5 13:41      秦有宝                     新建
 * <p/>
 * *************************************************************************************************
 */
@Component
public class EmployeeManager extends BaseEntityManager<Employee> {

    @Autowired
    private EmployeeDao employeeDao;
    @Autowired
    private UserManager userManager;
    @Autowired
    private OrganizationDao organizationDao;
    @Autowired
    private EmployeePositionManager employeePositionManager;
    @Autowired
    private UserProfileManager userProfileManager;
    @Autowired
    private EmailUtil emailUtil;
    @Autowired
    private PositionManager positionManager;
    @Autowired
    private UserFeatureRoleManager userFeatureRoleManager;
    @Autowired
    private UserDataRoleManager userDataRoleManager;

    @Override
    protected BaseEntityDao<Employee> getDao() {
        return employeeDao;
    }

    /**
     * 保存
     *
     * @param entity 实体
     * @return 返回操作对象
     */
    @Override
    @Transactional
    public OperateResultWithData<Employee> save(Employee entity) {
        entity.setPassword(DigestUtils.md5Hex("123456"));   //默认为：123456
        return saveEmployee(entity);
    }

    /**
     * 保存(使用外部加密密码)
     *
     * @param entity 实体
     * @return 返回操作对象
     */
    @Transactional
    public OperateResultWithData<Employee> saveWithPassword(Employee entity) {
        return saveEmployee(entity);
    }

    /**
     * 保存(外部调用使用EmployeeService.save)
     *
     * @param entity 实体
     * @return 返回操作对象
     */
    @Transactional
    protected OperateResultWithData<Employee> saveEmployee(Employee entity) {
        boolean isNew = entity.isNew();
        //检查该租户下员工编号不能重复
        if (employeeDao.isCodeExist(entity.getCode(), entity.getId())) {
            //00040 = 该员工编号【{0}】已存在，请重新输入！
            return OperateResultWithData.operationFailure("00040", entity.getCode());
        }
        if (isNew) {
            employeeDao.save(entity, true);
        } else {
            //修改用户
            User user = userManager.findById(entity.getId());
            user.setUserName(entity.getUserName());
            user.setFrozen(entity.isFrozen());
            userManager.save(user);
            //如果是修改管理员，修改用户配置邮箱
            if (entity.isCreateAdmin()) {
                UserProfile userProfile = userProfileManager.findByUserId(entity.getId());
                userProfile.setEmail(entity.getEmail());
                userProfileManager.save(userProfile);
            }
            employeeDao.save(entity, false);
        }

        OperateResultWithData<Employee> operateResultWithData;
        if (isNew) {
            operateResultWithData = OperateResultWithData.operationSuccess("core_00001");
        } else {
            operateResultWithData = OperateResultWithData.operationSuccess("core_00002");
        }
        operateResultWithData.setData(entity);
        if (isNew && entity.isCreateAdmin() && operateResultWithData.successful()) {
            emailUtil.sendEmailNotifyUser(emailUtil.constructEmailMessage(entity));
        }
        return operateResultWithData;
    }

    /**
     * 根据查询参数获取企业员工(分页)
     *
     * @param employeeQueryParam 查询参数
     * @return 企业员工
     */
    public PageResult<Employee> findByEmployeeParam(EmployeeQueryParam employeeQueryParam) {
        PageResult<Employee> result = employeeDao.findByEmployeeParam(employeeQueryParam);
        Collection<Employee> employees = result.getRows();
        if (!CollectionUtils.isEmpty(employees)) {
            for (Employee employee : employees) {
                employee.setUserName(employee.getUser().getUserName());
            }
        }
        return result;
    }

    /**
     * 根据组织机构的id获取员工
     *
     * @param organizationId 组织机构的id
     * @return 员工清单
     */
    public List<Employee> findByOrganizationId(String organizationId) {
        return employeeDao.findByOrganizationId(organizationId);
    }

    /**
     * 根据组织机构的id获取员工(不包含冻结)
     *
     * @param organizationId 组织机构的id
     * @return 员工清单
     */
    public List<Employee> findByOrganizationIdWithoutFrozen(String organizationId) {
        List<Employee> list = employeeDao.findByOrganizationIdAndUserFrozenFalse(organizationId);
        list.forEach(r -> {
            //设置组织机构
            Organization organization = r.getOrganization();
            if (Objects.nonNull(organization)) {
                r.setUserRemark(organization.getName());
            }
            //设置岗位
            List<Position> positions = employeePositionManager.getChildrenFromParentId(r.getId());
            if (Objects.nonNull(positions) && !positions.isEmpty()) {
                Position position = positions.get(0);
                r.setUserRemark(position.getOrganization().getName() + "-" + position.getName());
            }
        });
        return list;
    }

    /**
     * 获取企业员工用户
     *
     * @param param 企业员工用户查询参数
     * @return 员工清单
     */
    public PageResult<Employee> findByUserQueryParam(UserQueryParam param) {
        Search search = new Search(param);
        if (param.getIncludeSubNode()) {
            List<Organization> orgs = organizationDao.getChildrenNodes4Unfrozen(param.getOrganizationId());
            List<String> orgIds = new ArrayList<>();
            orgs.forEach((r) -> orgIds.add(r.getId()));
            search.addFilter(new SearchFilter("organization.id", orgIds, SearchFilter.Operator.IN));
        } else {
            search.addFilter(new SearchFilter("organization.id", param.getOrganizationId(), SearchFilter.Operator.EQ));
        }
        PageResult<Employee> employees = findByPage(search);
        Iterator<Employee> iterator = employees.getRows().iterator();
        while (iterator.hasNext()) {
            Employee employee = iterator.next();
            //去除管理员显示
            if (!UserAuthorityPolicy.NormalUser.equals(employee.getUser().getUserAuthorityPolicy())) {
                iterator.remove();
            }
            employee.setUserName(employee.getUser().getUserName());
            employee.setFrozen(employee.getUser().getFrozen());
        }
        return employees;
    }

    /**
     * 基于主键集合查询集合数据对象
     *
     * @param strings 主键集合
     */
    @Override
    public List<Employee> findByIds(Collection<String> strings) {
        List<Employee> employees = super.findByIds(strings);
        if (!CollectionUtils.isEmpty(employees)) {
            for (Employee employee : employees) {
                employee.setUserName(employee.getUser().getUserName());
            }
        }
        return employees;
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
                resultPositions = positions.stream().filter(p -> organization.getId().equals(p.getOrganization().getId())).collect(Collectors.toList());
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

    /**
     * 根据企业员工的id列表获取执行人
     *
     * @param employeeIds 企业员工的id列表
     * @return 执行人清单
     */
    public List<Executor> getExecutorsByEmployeeIds(List<String> employeeIds) {
        if (employeeIds == null || employeeIds.size() == 0) {
            return Collections.emptyList();
        }
        List<Employee> employees = findByIds(employeeIds);
        List<Executor> executors = new ArrayList<>();
        for (Employee r : employees) {
            //排除冻结的用户
            if (r.getUser().getFrozen()) {
                continue;
            }
            executors.add(construstExecutor(r));
        }
        return executors;
    }

    /**
     * 查询可分配的功能角色
     *
     * @param featureRoleGroupId 功能角色组id
     * @param userId             用户id
     * @return 功能角色清单
     */
    public List<FeatureRole> getCanAssignedFeatureRoles(String featureRoleGroupId, String userId) {
        return userManager.getCanAssignedFeatureRoles(featureRoleGroupId, userId);
    }

    /**
     * 查询可分配的数据角色
     *
     * @param dataRoleGroupId 数据角色组id
     * @param userId          用户id
     * @return 数据角色清单
     */
    public List<DataRole> getCanAssignedDataRoles(String dataRoleGroupId, String userId) {
        return userManager.getCanAssignedDataRoles(dataRoleGroupId, userId);
    }

    /**
     * 通过租户代码获取租户管理员
     *
     * @param tenantCode 租户代码
     * @return 员工
     */
    public Employee findAdminByTenantCode(String tenantCode) {
        List<Employee> users = employeeDao.findByTenantCodeAndUserUserAuthorityPolicy(tenantCode, UserAuthorityPolicy.TenantAdmin);
        if (CollectionUtils.isEmpty(users)) {
            users = employeeDao.findByTenantCodeAndUserUserAuthorityPolicy(tenantCode, UserAuthorityPolicy.GlobalAdmin);
        }
        if (users != null && users.size() == 1) {
            Employee employee = users.get(0);
            if (Objects.nonNull(employee)) {
                UserProfile userProfile = userProfileManager.findByUserId(employee.getId());
                if (Objects.nonNull(userProfile)) {
                    employee.setEmail(userProfile.getEmail());
                }
                return employee;
            }
        }
        return null;
    }

    /**
     * 通过员工编号获取员工
     *
     * @param code 员工编号
     * @return 员工
     */
    public Employee findByCode(String code) {
        return employeeDao.findByCodeAndTenantCode(code, ContextUtil.getTenantCode());
    }

    /**
     * 快速查询企业用户(分页)
     *
     * @param param 快速查询参数
     * @return 企业用户查询结果
     */
    public PageResult<Employee> quickSearch(QuickSearchParam param) {
        // 构造查询参数
        Search search = new Search(param);
        Collection<String> quickSearchProperties = param.getQuickSearchProperties();
        if (Objects.isNull(quickSearchProperties) || quickSearchProperties.isEmpty()) {
            //以员工编号或姓名查询
            quickSearchProperties = new ArrayList<>();
            quickSearchProperties.add("code");
            quickSearchProperties.add("user.userName");
            search.setQuickSearchProperties(quickSearchProperties);
        }
        List<SearchOrder> sortOrders = param.getSortOrders();
        if (Objects.isNull(sortOrders) || sortOrders.isEmpty()) {
            //以员工编号排序
            sortOrders = new ArrayList<>();
            sortOrders.add(new SearchOrder("code"));
            search.setSortOrders(sortOrders);
        }
        //限制未冻结的用户
        search.addFilter(new SearchFilter("user.frozen", false, SearchFilter.Operator.EQ));
        return findByPage(search);
    }

    /**
     * 快速查询企业用户作为流程执行人
     *
     * @param param 快速查询参数
     * @return 企业用户查询结果
     */
    public PageResult<Executor> quickSearchExecutors(QuickSearchParam param) {
        PageResult<Employee> employeeResult = quickSearch(param);
        //转换为执行人
        PageResult<Executor> result = new PageResult<>(employeeResult);
        List<Employee> employees = Objects.nonNull(employeeResult) ? employeeResult.getRows() : null;
        if (Objects.isNull(employees) || employees.isEmpty()) {
            result.setRows(Collections.emptyList());
            return result;
        }
        List<Executor> executors = new ArrayList<>();
        employees.forEach((e) -> {
            if (Objects.nonNull(e)) {
                executors.add(construstExecutor(e));
            }
        });
        result.setRows(executors);
        return result;
    }


    /**
     * 根据组织机构IDS与岗位分类IDS获取执行人
     *
     * @param orgIds     组织机构IDS
     * @param postCatIds 岗位分类IDS
     * @return 执行人清单
     */
    public List<Executor> getExecutorsByPostCatAndOrg(List<String> orgIds, List<String> postCatIds) {
        if (Objects.isNull(orgIds) || orgIds.isEmpty() || Objects.isNull(postCatIds) || postCatIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Executor> es = new ArrayList<>();
        //1.根据岗位类别筛选出所有岗位
        List<Position> posts = positionManager.findByFilter(new SearchFilter(Position.POSITION_CATEGORY_ID, postCatIds, SearchFilter.Operator.IN));
        if (Objects.nonNull(posts) && !posts.isEmpty()) {
            //2.筛查指定组织的岗位
            List<Position> resultPositions = posts.stream().filter(e -> orgIds.contains(e.getOrganization().getId())).collect(Collectors.toList());
            if (!resultPositions.isEmpty()) {
                //3.根据指定岗位寻找执行人
                Set<String> resultPositionIds = resultPositions.stream().map(Position::getId).collect(Collectors.toSet());
                List<Employee> employees = employeePositionManager.getParentsFromChildIds(new ArrayList<>(resultPositionIds));
                if (Objects.nonNull(employees) && !employees.isEmpty()) {
                    //ist<Employee> result = employees.stream().filter(e -> orgIds.contains(e.getOrganization().getId())).collect(Collectors.toList());
                    employees.forEach(e -> es.add(construstExecutor(e)));
                }
            }
        }
        return es;
    }

    /**
     * 根据岗位id和员工用户查询参数获取所有可分配企业员工用户
     *
     * @param param 员工用户查询参数
     * @return 员工用户查询结果
     */
    public List<Employee> listAllCanAssignEmployees(UserQueryParam param) {
        //获取已经分配的员工列表
        String positionId = param.getPositionId();
        List<Employee> assignedEmployees = employeePositionManager.getParentsFromChildId(positionId);
        Set<String> assignedEmployeeIds = assignedEmployees.stream().map(Employee::getId).collect(Collectors.toSet());
        //是否包含子节点
        Search search = new Search(param);
        if (param.getIncludeSubNode()) {
            List<Organization> orgs = organizationDao.getChildrenNodes4Unfrozen(param.getOrganizationId());
            List<String> orgIds = new ArrayList<>();
            //添加当前节点组织id，避免没有子节点下查询全部组织的id
            orgIds.add(param.getOrganizationId());
            orgs.forEach((r) -> orgIds.add(r.getId()));
            search.addFilter(new SearchFilter("organization.id", orgIds, SearchFilter.Operator.IN));
        } else {
            search.addFilter(new SearchFilter("organization.id", param.getOrganizationId(), SearchFilter.Operator.EQ));
        }
        List<Employee> employees = findByFilters(search);
        List<Employee> result = employees.stream()
                .filter(e ->
                        //去除掉已经分配的
                        !assignedEmployeeIds.contains(e.getId())
                                //去除掉用户已经冻结的
                                && !e.getUser().getFrozen()
                                //只获取企业员工的
                                && UserAuthorityPolicy.NormalUser.equals(e.getUser().getUserAuthorityPolicy()))
                .collect(Collectors.toList());
        //填充名称和冻结状态
        for (Employee entity : result) {
            entity.setUserName(entity.getUser().getUserName());
            entity.setFrozen(entity.getUser().getFrozen());
        }
        return result;
    }

    /**
     * 通过用户id获取员工
     *
     * @param userId 用户id
     * @return 员工
     */
    public ResultData<Employee> findByUserId(String userId) {
        Employee employee = findByProperty("user.id", userId);
        if (Objects.isNull(employee)) {
            return ResultData.fail(ContextUtil.getMessage("00055"));
        } else {
            UserProfile userProfile = userProfileManager.findByUserId(employee.getUser().getId());
            if (!ObjectUtils.isEmpty(userProfile)) {
                employee.setEmail(userProfile.getEmail());
                employee.setMobile(userProfile.getMobile());
            }
            return ResultData.success(employee);
        }
    }

    /**
     * 把一个企业用户的角色复制到多个企业用户
     *
     * @param copyParam 复制参数
     * @return 操作结果
     */
    public OperateResult copyToEmployees(EmployeeCopyParam copyParam) {
        // 获取源用户
        Employee employee = findOne(copyParam.getEmployeeId());
        if (Objects.isNull(employee) || Objects.isNull(employee.getUser())) {
            // 企业用户【{0}】不存在！
            return OperateResult.operationFailure("00092", copyParam.getEmployeeId());
        }
        User user = employee.getUser();
        AtomicInteger atomicInteger = new AtomicInteger(0);
        List<String> targetEmployeeIds = copyParam.getTargetEmployeeIds();
        if (CollectionUtils.isEmpty(targetEmployeeIds)) {
            // 【{0}-{1}】的角色成功复制到【{2}】个企业用户！
            return OperateResult.operationSuccess("00093", employee.getCode(), user.getUserName(), atomicInteger.intValue());
        }
        // 循环复制角色
        for (String targetEmployeeId : targetEmployeeIds) {
            boolean isCopied = copyEmployeeRoles(targetEmployeeId, copyParam.getFeatureRoleIds(), copyParam.getDataRoleIds());
            if (isCopied) {
                atomicInteger.incrementAndGet();
            }
        }
        // 【{0}-{1}】的角色成功复制到【{2}】个企业用户！
        return OperateResult.operationSuccess("00093", employee.getCode(), user.getUserName(), atomicInteger.intValue());
    }

    /**
     * 单个用户角色的复制
     *
     * @param targetEmployeeId 目标用户
     * @param featureRoleIds   功能角色Id清单
     * @param dataRoleIds      数据角色Id清单
     */
    private boolean copyEmployeeRoles(String
                                              targetEmployeeId, List<String> featureRoleIds, List<String> dataRoleIds) {
        // 检查输入参数
        if (CollectionUtils.isEmpty(featureRoleIds) && CollectionUtils.isEmpty(dataRoleIds)) {
            return false;
        }
        // 获取目标用户
        Employee targetEmployee = findOne(targetEmployeeId);
        if (Objects.isNull(targetEmployee)) {
            return false;
        }
        // 保存用户功能角色
        if (CollectionUtils.isNotEmpty(featureRoleIds)) {
            userFeatureRoleManager.insertRelations(targetEmployeeId, featureRoleIds);
        }
        // 保存用户数据角色
        if (CollectionUtils.isNotEmpty(dataRoleIds)) {
            userDataRoleManager.insertRelations(targetEmployeeId, dataRoleIds);
        }
        return true;
    }
}