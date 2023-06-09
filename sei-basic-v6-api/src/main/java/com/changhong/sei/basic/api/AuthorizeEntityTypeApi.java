package com.changhong.sei.basic.api;

import com.changhong.sei.basic.dto.AuthorizeEntityTypeDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.FindAllApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 实现功能: 权限对象类型API接口
 *
 * @author 王锦光 wangjg
 * @version 2020-01-20 11:08
 */
@FeignClient(name = "sei-basic", path = "authorizeEntityType")
public interface AuthorizeEntityTypeApi extends BaseEntityApi<AuthorizeEntityTypeDto>,
        FindAllApi<AuthorizeEntityTypeDto> {
}
