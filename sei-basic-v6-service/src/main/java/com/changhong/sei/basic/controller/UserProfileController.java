package com.changhong.sei.basic.controller;

import com.changhong.sei.basic.api.UserProfileApi;
import com.changhong.sei.basic.dto.LanguageValue;
import com.changhong.sei.basic.dto.UserInfoDto;
import com.changhong.sei.basic.dto.UserPreferenceEnum;
import com.changhong.sei.basic.dto.UserProfileDto;
import com.changhong.sei.basic.entity.Employee;
import com.changhong.sei.basic.entity.UserProfile;
import com.changhong.sei.basic.service.EmployeeService;
import com.changhong.sei.basic.service.UserProfileService;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.utils.ResultDataUtil;
import com.changhong.sei.enums.UserType;
import com.changhong.sei.notify.dto.UserNotifyInfo;
import com.changhong.sei.util.EnumUtils;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 实现功能: 用户配置API服务实现
 *
 * @author 王锦光 wangjg
 * @version 2020-01-30 10:00
 */
@RestController
@Api(value = "UserProfileService", tags = "用户配置API服务实现")
@RequestMapping(path = "userProfile", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserProfileController extends BaseEntityController<UserProfile, UserProfileDto> implements UserProfileApi {
    @Autowired
    private UserProfileService service;
    @Autowired
    private EmployeeService employeeService;

    @Override
    public BaseEntityService<UserProfile> getService() {
        return service;
    }

    /**
     * 获取支持的语言
     */
    @Override
    public ResultData<List<LanguageValue>> getLanguages() {
        return service.getLanguages();
    }

    /**
     * 查询一个用户配置
     *
     * @param userId 用户id
     * @return 用户配置
     */
    @Override
    public ResultData<UserProfileDto> findByUserId(String userId) {
        // 获取用户配置信息
        UserProfileDto profileDto = convertToDto(service.findByUserId(userId));
        if (Objects.isNull(profileDto)) {
            // 用户【{0}】没有创建配置信息！
            return ResultDataUtil.fail("00095", userId);
        }
        // 获取企业员工信息
        if (profileDto.getUserType() == UserType.Employee) {
            Employee employee = employeeService.findOne(userId);
            if (Objects.nonNull(employee)) {
                profileDto.setEmployeeCode(employee.getCode());
                if (Objects.nonNull(employee.getOrganization())) {
                    profileDto.setOrganizationName(employee.getOrganization().getName());
                }
            }
        }
        return ResultData.success(profileDto);
    }

    /**
     * 查询一个用户配置
     *
     * @return 用户配置
     */
    @Override
    public ResultData<UserInfoDto> getUserInfo() {
        String userId = ContextUtil.getUserId();
        // 获取用户配置信息
        UserProfile userProfile = service.findByUserId(userId);
        if (Objects.isNull(userProfile)) {
            // 用户【{0}】没有创建配置信息！
            return ResultDataUtil.fail("00095", userId);
        }
        UserInfoDto profileDto = dtoModelMapper.map(userProfile, UserInfoDto.class);
        // 获取企业员工信息
        if (profileDto.getUserType() == UserType.Employee) {
            Employee employee = employeeService.findOne(userId);
            if (Objects.nonNull(employee)) {
                profileDto.setEmployeeCode(employee.getCode());
                if (Objects.nonNull(employee.getOrganization())) {
                    profileDto.setOrganizationName(employee.getOrganization().getName());
                }
            }
        }
        return ResultData.success(profileDto);
    }

    /**
     * 更新用户信息
     *
     * @param dto 业务实体DTO
     * @return 操作结果
     */
    @Override
    public ResultData<UserInfoDto> updateInfo(UserProfileDto dto) {
        if (Objects.isNull(dto)) {
            // 输入的数据传输对象为空！
            return ResultData.fail(ContextUtil.getMessage("core_service_00002"));
        }
        String id = dto.getId();
        if (StringUtils.isBlank(id)) {
            // 用户【{0}】配置不存在.
            return ResultData.fail(ContextUtil.getMessage("00092", id));
        }
        UserProfile profile = service.findOne(id);
        if (Objects.isNull(profile)) {
            // 用户【{0}】配置不存在.
            return ResultData.fail(ContextUtil.getMessage("00092", id));
        }
        UserProfile userProfile = convertToEntity(dto);
        String email = userProfile.getEmail();
        if (StringUtils.isNotBlank(email) && StringUtils.contains(email, "*")) {
            userProfile.setEmail(profile.getEmail());
        }
        String idCard = userProfile.getIdCard();
        if (StringUtils.isNotBlank(idCard) && StringUtils.contains(idCard, "*")) {
            userProfile.setIdCard(profile.getIdCard());
        }
        String mobile = userProfile.getMobile();
        if (StringUtils.isNotBlank(mobile) && StringUtils.contains(mobile, "*")) {
            userProfile.setMobile(profile.getMobile());
        }
        service.save(userProfile);

        UserInfoDto userInfoDto = dtoModelMapper.map(userProfile, UserInfoDto.class);
        // 获取企业员工信息
        if (userInfoDto.getUserType() == UserType.Employee) {
            Employee employee = employeeService.findOne(userProfile.getUserId());
            if (Objects.nonNull(employee)) {
                userInfoDto.setEmployeeCode(employee.getCode());
                if (Objects.nonNull(employee.getOrganization())) {
                    userInfoDto.setOrganizationName(employee.getOrganization().getName());
                }
            }
        }
        return ResultData.success(userInfoDto);
    }

    /**
     * 根据用户id列表获取通知信息
     *
     * @param userIds 用户id集合
     */
    @Override
    public ResultData<List<UserNotifyInfo>> findNotifyInfoByUserIds(List<String> userIds) {
        return ResultData.success(service.findNotifyInfoByUserIds(userIds));
    }

    /**
     * 获取当前用户的记账用户
     *
     * @return 记账用户
     */
    @Override
    public ResultData<String> findAccountor() {
        return ResultData.success(service.findAccountor());
    }

    /**
     * 获取当前用户的偏好配置
     *
     * @return 偏好配置. 如:{portrait:'data:image/png;base64,XXX', guide:'true'}
     */
    @Override
    public ResultData<String> getPreferences() {
        return ResultData.success(service.getPreferences(ContextUtil.getUserId()));
    }

    /**
     * 自定义设置Entity转换为DTO的转换器
     */
    @Override
    protected void customConvertToDtoMapper() {
        // 创建自定义映射规则
        PropertyMap<UserProfile, UserProfileDto> propertyMap = new PropertyMap<UserProfile, UserProfileDto>() {
            @Override
            protected void configure() {
                // 使用自定义转换规则
                map().setUserId(source.getUserId());
            }
        };
        // 添加映射器
        dtoModelMapper.addMappings(propertyMap);
        // 创建自定义映射规则
        PropertyMap<UserProfile, UserInfoDto> propertyMap1 = new PropertyMap<UserProfile, UserInfoDto>() {
            @Override
            protected void configure() {
                // 使用自定义转换规则
                map().setUserId(source.getUserId());
            }
        };
        // 添加映射器
        dtoModelMapper.addMappings(propertyMap1);
    }

    /**
     * 保存业务实体
     *
     * @param dto 业务实体DTO
     * @return 操作结果
     */
    @Override
    public ResultData<UserProfileDto> save(UserProfileDto dto) {
        ResultData<UserProfileDto> resultData = super.save(dto);
        if (resultData.failed()) {
            return resultData;
        }
        // 重新获取数据
        ResultData<UserProfileDto> profileDto = findByUserId(resultData.getData().getUserId());
        return ResultData.success(resultData.getMessage(), profileDto.getData());
    }

    /**
     * 设置用户偏好配置
     *
     * @param preference 偏好配置类型
     * @param value      偏好配置
     * @return 返回操作结果
     */
    @Override
    public ResultData<Void> setUserPreference(String preference, Object value) {
        UserPreferenceEnum preferenceEnum = EnumUtils.getEnum(UserPreferenceEnum.class, preference);
        if (Objects.isNull(preferenceEnum)) {
            return ResultData.fail(ContextUtil.getMessage("00119"));
        }
        return service.putUserPreference(ContextUtil.getUserId(), preferenceEnum, value);
    }
}
