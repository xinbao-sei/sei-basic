package com.changhong.sei.basic.controller;

import com.changhong.sei.basic.api.DataRoleApi;
import com.changhong.sei.basic.dto.DataRoleDto;
import com.changhong.sei.basic.entity.DataRole;
import com.changhong.sei.basic.service.DataRoleService;
import com.changhong.sei.core.controller.DefaultBaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.service.BaseEntityService;
import io.swagger.annotations.Api;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 实现功能: 数据角色API服务实现
 *
 * @author 王锦光 wangjg
 * @version 2020-01-21 8:42
 */
@RestController
@Api(value = "DataRoleService", tags = "数据角色API服务")
public class DataRoleController implements DefaultBaseEntityController<DataRole, DataRoleDto>,
        DataRoleApi {
    @Autowired
    private DataRoleService service;
    @Autowired
    private ModelMapper modelMapper;
    @Override
    public BaseEntityService<DataRole> getService() {
        return service;
    }

    @Override
    public ModelMapper getModelMapper() {
        return modelMapper;
    }

    /**
     * 获取数据实体的类型
     *
     * @return 类型Class
     */
    @Override
    public Class<DataRole> getEntityClass() {
        return DataRole.class;
    }

    /**
     * 获取传输实体的类型
     *
     * @return 类型Class
     */
    @Override
    public Class<DataRoleDto> getDtoClass() {
        return DataRoleDto.class;
    }

    /**
     * 通过角色组Id获取角色清单
     *
     * @param roleGroupId 角色组Id
     * @return 角色清单
     */
    @Override
    public ResultData<List<DataRoleDto>> findByDataRoleGroup(String roleGroupId) {
        List<DataRole> roles = service.findByDataRoleGroup(roleGroupId);
        List<DataRoleDto> dtos = roles.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResultData.success(dtos);
    }

    /**
     * 获取用户本人可以分配的角色
     *
     * @param roleGroupId 角色组Id
     * @return 可以分配的角色
     */
    @Override
    public ResultData<List<DataRoleDto>> getCanAssignedRoles(String roleGroupId) {
        List<DataRole> roles = service.getCanAssignedRoles(roleGroupId);
        List<DataRoleDto> dtos = roles.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResultData.success(dtos);
    }

    /**
     * 将数据实体转换成DTO
     *
     * @param entity 业务实体
     * @return DTO
     */
    @Override
    public DataRoleDto convertToDto(DataRole entity) {
        return custConvertToDto(entity);
    }

    /**
     * 转换数据角色数据实体为DTO
     * @param entity 数据角色数据实体
     * @return 数据角色DTO
     */
    static DataRoleDto custConvertToDto(DataRole entity){
        if (Objects.isNull(entity)){
            return null;
        }
        ModelMapper custMapper = new ModelMapper();
        // 创建自定义映射规则
        PropertyMap<DataRole, DataRoleDto> propertyMap = new PropertyMap<DataRole, DataRoleDto>() {
            @Override
            protected void configure() {
                // 使用自定义转换规则
                map().setDataRoleGroupId(source.getDataRoleGroupId());
                map().setPublicOrgId(source.getPublicOrgId());
            }
        };
        // 添加映射器
        custMapper.addMappings(propertyMap);
        // 转换
        return custMapper.map(entity, DataRoleDto.class);
    }
}