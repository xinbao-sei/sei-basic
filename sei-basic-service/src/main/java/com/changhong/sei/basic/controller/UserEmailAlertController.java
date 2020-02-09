package com.changhong.sei.basic.controller;

import com.changhong.sei.basic.api.UserEmailAlertApi;
import com.changhong.sei.basic.dto.UserEmailAlertDto;
import com.changhong.sei.basic.entity.UserEmailAlert;
import com.changhong.sei.basic.service.UserEmailAlertService;
import com.changhong.sei.core.controller.DefaultBaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import com.changhong.sei.core.utils.ResultDataUtil;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 实现功能: 用户邮件提醒API服务实现
 *
 * @author 王锦光 wangjg
 * @version 2020-01-22 10:28
 */
@RestController
@Api(value = "UserEmailAlertApi", tags = "用户邮件提醒API服务")
@RequestMapping(path = "userEmailAlert", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserEmailAlertController implements DefaultBaseEntityController<UserEmailAlert, UserEmailAlertDto>,
        UserEmailAlertApi {
    @Autowired
    private UserEmailAlertService service;
    @Override
    public BaseEntityService<UserEmailAlert> getService() {
        return service;
    }

    /**
     * 获取数据实体的类型
     *
     * @return 类型Class
     */
    @Override
    public Class<UserEmailAlert> getEntityClass() {
        return UserEmailAlert.class;
    }

    /**
     * 获取传输实体的类型
     *
     * @return 类型Class
     */
    @Override
    public Class<UserEmailAlertDto> getDtoClass() {
        return UserEmailAlertDto.class;
    }

    /**
     * 通过用户ID列表获取用户邮件通知列表
     *
     * @param userIdS 用户ID列表
     * @return 操作结果
     */
    @Override
    public ResultData<List<UserEmailAlertDto>> findByUserIds(List<String> userIdS) {
        List<UserEmailAlert> alerts = service.findByUserIds(userIdS);
        List<UserEmailAlertDto> dtos = alerts.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResultData.success(dtos);
    }

    /**
     * 通过用户ID列表更新最新提醒时间
     *
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    @Override
    public ResultData updateLastTimes(List<String> userIds) {
        OperateResult result = service.updateLastTimes(userIds);
        return ResultDataUtil.convertFromOperateResult(result);
    }

    /**
     * 获取当前用户邮件通知列表
     *
     * @return 操作结果
     */
    @Override
    public ResultData<List<UserEmailAlertDto>> findByUserIds() {
        List<UserEmailAlert> alerts = service.findByUserIds();
        List<UserEmailAlertDto> dtos = alerts.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResultData.success(dtos);
    }
}
