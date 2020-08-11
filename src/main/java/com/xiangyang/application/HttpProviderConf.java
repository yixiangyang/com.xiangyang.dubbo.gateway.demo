package com.xiangyang.application;

import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
@Data
@Component
public class HttpProviderConf {
    private List<String> usePackage;
}
