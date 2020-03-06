package com.changhong.sei.basic.controller;

import com.changhong.sei.basic.api.MenuApi;
import com.changhong.sei.basic.dto.MenuDto;
import com.changhong.sei.basic.entity.Menu;
import com.changhong.sei.basic.service.MenuService;
import com.changhong.sei.basic.service.UserService;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.controller.DefaultTreeController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.TreeNodeMoveParam;
import com.changhong.sei.core.service.BaseTreeService;
import com.changhong.sei.utils.AsyncRunUtil;
import io.swagger.annotations.Api;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 实现功能: 系统菜单API服务
 *
 * @author 王锦光 wangjg
 * @version 2020-01-19 22:09
 */
@RestController
@Api(value = "MenuApi", tags = "系统菜单API服务")
@RequestMapping(path = "menu", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class MenuController implements DefaultTreeController<Menu, MenuDto>,
        MenuApi {
    @Autowired
    private MenuService menuService;
    @Autowired
    private UserService userService;
    @Autowired
    private AsyncRunUtil asyncRunUtil;

    /**
     * 获取整个菜单树
     *
     * @return 菜单树形对象集合
     */
    @Override
    public ResultData<List<MenuDto>> getMenuTree() {
        List<Menu> menus = menuService.getMenuTree();
        List<MenuDto> dtos = menus.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResultData.success(dtos);
    }

    /**
     * 根据名称模糊查询
     *
     * @param name 名称
     * @return 返回的列表
     */
    @Override
    public ResultData<List<MenuDto>> findByNameLike(String name) {
        List<Menu> menus = menuService.findByNameLike(name);
        List<MenuDto> dtos = menus.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResultData.success(dtos);
    }

    @Override
    public BaseTreeService<Menu> getService() {
        return menuService;
    }

    /**
     * 获取数据实体的类型
     *
     * @return 类型Class
     */
    @Override
    public Class<Menu> getEntityClass() {
        return Menu.class;
    }

    /**
     * 获取传输实体的类型
     *
     * @return 类型Class
     */
    @Override
    public Class<MenuDto> getDtoClass() {
        return MenuDto.class;
    }

    /**
     * 将数据实体转换成DTO
     *
     * @param entity 业务实体
     * @return DTO
     */
    @Override
    public MenuDto convertToDto(Menu entity) {
        return MenuController.custConvertToDto(entity);
    }

    /**
     * 自定义将数据实体转换成DTO
     *
     * @param entity 业务实体
     * @return DTO
     */
    static MenuDto custConvertToDto(Menu entity) {
        if (Objects.isNull(entity)){
            return null;
        }
        ModelMapper custMapper = new ModelMapper();
        // 创建自定义映射规则
        PropertyMap<Menu, MenuDto> propertyMap = new PropertyMap<Menu, MenuDto>() {
            @Override
            protected void configure() {
                // 使用自定义转换规则确定FeatureId
                map().setFeatureId(source.getFeatureId());
                map().setMenuUrl(source.getFeature().getGroupCode());
            }
        };
        // 添加映射器
        custMapper.addMappings(propertyMap);
        // 转换
        return custMapper.map(entity, MenuDto.class);
    }

    /**
     * 保存业务实体
     *
     * @param dto 业务实体DTO
     * @return 操作结果
     */
    @Override
    public ResultData<MenuDto> save(@Valid MenuDto dto) {
        ResultData<MenuDto> result = DefaultTreeController.super.save(dto);
        if (result.failed()) {
            return result;
        }
        // 清除当前用户的权限缓存
        String userId = ContextUtil.getUserId();
        asyncRunUtil.runAsync(() -> userService.clearUserAuthorizedCaches(userId));
        return result;
    }

    /**
     * 删除业务实体
     *
     * @param id 业务实体Id
     * @return 操作结果
     */
    @Override
    public ResultData delete(String id) {
        ResultData result = DefaultTreeController.super.delete(id);
        if (result.failed()) {
            return result;
        }
        // 清除当前用户的权限缓存
        String userId = ContextUtil.getUserId();
        asyncRunUtil.runAsync(() -> userService.clearUserAuthorizedCaches(userId));
        return result;
    }

    /**
     * 移动一个节点
     *
     * @param moveParam 节点移动参数
     * @return 操作状态
     */
    @Override
    public ResultData move(TreeNodeMoveParam moveParam) {
        ResultData result = DefaultTreeController.super.move(moveParam);
        if (result.failed()) {
            return result;
        }
        // 清除当前用户的权限缓存
        String userId = ContextUtil.getUserId();
        asyncRunUtil.runAsync(() -> userService.clearUserAuthorizedCaches(userId));
        return result;
    }
}
