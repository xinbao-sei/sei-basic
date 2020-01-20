package com.changhong.sei.basic.manager;

import com.changhong.com.sei.core.test.BaseUnitTest;
import com.changhong.sei.basic.entity.AppModule;
import com.changhong.sei.core.util.JsonUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * <strong>实现功能:</strong>
 * <p></p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2020-01-19 11:20
 */
public class AppModuleManagerTest extends BaseUnitTest {
    @Autowired
    private AppModuleManager manager;

    @Test
    public void findByCode() {
        String code = "sei-notify";
        AppModule appModule = manager.findByCode(code);
        Assert.assertNotNull(appModule);
        System.out.println(JsonUtils.toJson(appModule));
    }
}