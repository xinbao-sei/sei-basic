package com.changhong.sei.basic.service;

import com.changhong.sei.core.test.BaseUnitTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * 实现功能:
 *
 * @author 王锦光 wangjg
 * @version 2020-02-06 23:10
 */
public class UserServiceTest extends BaseUnitTest {
    @Autowired
    private UserService service;

    @Test
    public void clearUserAuthorizedCaches() {
        String userId = "989C3B6A-6758-11EA-B205-0242C0A8460C";
        service.clearUserAuthorizedCaches(userId);
    }
}