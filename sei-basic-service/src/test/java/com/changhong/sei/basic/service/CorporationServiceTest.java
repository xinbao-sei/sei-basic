package com.changhong.sei.basic.service;

import com.changhong.com.sei.core.test.BaseUnitTest;
import com.changhong.sei.basic.entity.Corporation;
import com.changhong.sei.core.util.JsonUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 实现功能:
 *
 * @author 王锦光 wangjg
 * @version 2020-01-26 14:42
 */
public class CorporationServiceTest extends BaseUnitTest {
    @Autowired
    private CorporationService service;

    @Test
    public void findByCode() {
        String code = "10044-Q000";
        Corporation corporation = service.findByCode(code);
        Assert.assertNotNull(corporation);
        System.out.println(JsonUtils.toJson(corporation));
    }
}