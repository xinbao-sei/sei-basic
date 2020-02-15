package com.changhong.sei.basic.controller;

import com.changhong.sei.apitemplate.ApiTemplate;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.auth.AuthTreeEntityData;
import com.changhong.sei.core.test.BaseUnitTest;
import com.changhong.sei.core.util.JsonUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 实现功能:
 *
 * @author 王锦光 wangjg
 * @version 2020-02-11 13:56
 */
public class OrganizationControllerTest extends BaseUnitTest {
    @Autowired
    private OrganizationController controller;
    @Autowired
    private ApiTemplate apiTemplate;

    @Test
    public void findAllAuthTreeEntityData() {
        ResultData resultData = controller.findAllAuthTreeEntityData();
        System.out.println(JsonUtils.toJson(resultData));
        Assert.assertTrue(resultData.successful());
    }

    @Test
    public void findAllAuthTreeEntityDataViaApi() {
        String path = "organization/findAllAuthTreeEntityData";
        ParameterizedTypeReference<ResultData<List<AuthTreeEntityData>>> typeReference = new ParameterizedTypeReference<ResultData<List<AuthTreeEntityData>>>() {};
        ResultData<List<AuthTreeEntityData>> resultData = apiTemplate.getByAppModuleCode("sei-basic", path, typeReference);
        //List<AuthTreeEntityData> data = JsonUtils.fromJson2List(JsonUtils.toJson(resultData.getData()), AuthTreeEntityData.class);
        System.out.println(JsonUtils.toJson(resultData));
        Assert.assertTrue(resultData.successful());
    }
}