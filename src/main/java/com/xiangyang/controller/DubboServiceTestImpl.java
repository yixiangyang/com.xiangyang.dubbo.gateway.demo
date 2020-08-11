package com.xiangyang.controller;

import org.apache.dubbo.config.annotation.DubboService;

@DubboService()
public class DubboServiceTestImpl implements DubboServiceTest{
    @Override
    public String testDubbo(String name) {
        return "这个是测试ddd";
    }
}
