package com.changhong.sei.basic.service;

import com.changhong.sei.core.log.LogUtil;
import com.changhong.sei.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * <strong>实现功能:</strong>
 * <p>调试你好的业务逻辑实现</p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2019-12-18 19:42
 */
@Service
public class HelloService {
    /**
     * 你好业务逻辑
     * @param name 姓名
     * @param param 参数
     * @return 返回句子
     */
    public String sayHello(String name, String param){
        LogUtil.bizLog("执行业务逻辑说：你好！");
        // 测试异常
        if (!StringUtils.equals(name, "王锦光")){
            throw new ServiceException("你不是王锦光！");
        }
        return "你好，"+name+"！参数："+param;
    }
}
