package com.xiangyang.application;

import lombok.Data;

@Data
public class HttpRequest {
    private String param ;//入参
    private String service ;//请求service
    private String method ;//请求方法
}
