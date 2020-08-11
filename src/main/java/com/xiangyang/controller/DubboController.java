package com.xiangyang.controller;

import com.alibaba.fastjson.JSON;
import com.xiangyang.application.HttpProviderConf;
import com.xiangyang.application.HttpRequest;
import com.xiangyang.application.HttpResponse;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DubboController implements ApplicationContextAware {
    private final static Logger logger = LoggerFactory.getLogger(DubboController.class);

    @Resource
    private HttpProviderConf httpProviderConf;


    /**
     * 缓存 Map
     */
    private final Map<String, Class<?>> cacheMap = new HashMap<String, Class<?>>();

    protected ApplicationContext applicationContext;


//http://localhost:9999/com.xiangyang.controller.DubboServiceTest/testDubbo
    @RequestMapping(value = "/{service}/{method}", method = RequestMethod.POST)
    public String api(HttpRequest httpRequest, HttpServletRequest request,
                      @PathVariable String service,
                      @PathVariable String method) {
        logger.debug("ip:{}-httpRequest:{}", getIP(request), JSON.toJSONString(httpRequest));

        String invoke = invoke(httpRequest, service, method);
        logger.debug("callback :" + invoke);
        return invoke;

    }


    private String invoke(HttpRequest httpRequest, String service, String method) {
        httpRequest.setService(service);
        httpRequest.setMethod(method);

        HttpResponse response = new HttpResponse();

        logger.debug("input param:" + JSON.toJSONString(httpRequest));

        if (!CollectionUtils.isEmpty(httpProviderConf.getUsePackage())) {
            boolean isPac = false;
            for (String pac : httpProviderConf.getUsePackage()) {
                if (service.startsWith(pac)) {
                    isPac = true;
                    break;
                }
            }
            if (!isPac) {
                //调用的是未经配置的包
                logger.error("service is not correct,service=" + service);
                response.setCode("2");
                response.setSuccess(false);
                response.setDescription("service is not correct,service=" + service);
            }

        }
        try {
            Class<?> serviceCla = cacheMap.get(service);
            if (serviceCla == null) {
                serviceCla = Class.forName(service);
                logger.debug("serviceCla:" + JSON.toJSONString(serviceCla));

                //设置缓存
                cacheMap.put(service, serviceCla);
            }
            Method[] methods = serviceCla.getMethods();
            Method targetMethod = null;
            for (Method m : methods) {
                if (m.getName().equals(method)) {
                    targetMethod = m;
                    break;
                }
            }

            if (method == null) {
                logger.error("method is not correct,method=" + method);
                response.setCode("2");
                response.setSuccess(false);
                response.setDescription("method is not correct,method=" + method);
            }

            Object bean = this.applicationContext.getBean(serviceCla);
            Object result = null;
            Class<?>[] parameterTypes = targetMethod.getParameterTypes();
            if (parameterTypes.length == 0) {
                //没有参数
                result = targetMethod.invoke(bean);
            } else if (parameterTypes.length == 1) {
                Object json = JSON.parseObject(httpRequest.getParam(), parameterTypes[0]);
                result = targetMethod.invoke(bean, json);
            } else {
                logger.error("Can only have one parameter");
                response.setSuccess(false);
                response.setCode("2");
                response.setDescription("Can only have one parameter");
            }
            return JSON.toJSONString(result);

        } catch (ClassNotFoundException e) {
            logger.error("class not found", e);
            response.setSuccess(false);
            response.setCode("2");
            response.setDescription("class not found");
        } catch (InvocationTargetException e) {
            logger.error("InvocationTargetException", e);
            response.setSuccess(false);
            response.setCode("2");
            response.setDescription("InvocationTargetException");
        } catch (IllegalAccessException e) {
            logger.error("IllegalAccessException", e);
            response.setSuccess(false);
            response.setCode("2");
            response.setDescription("IllegalAccessException");
        }
        return JSON.toJSONString(response);
    }

    /**
     * 获取IP
     *
     * @param request
     * @return
     */
    private String getIP(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String s = request.getHeader("X-Forwarded-For");
        if (s == null || s.length() == 0 || "unknown".equalsIgnoreCase(s)) {

            s = request.getHeader("Proxy-Client-IP");
        }
        if (s == null || s.length() == 0 || "unknown".equalsIgnoreCase(s)) {

            s = request.getHeader("WL-Proxy-Client-IP");
        }
        if (s == null || s.length() == 0 || "unknown".equalsIgnoreCase(s)) {
            s = request.getHeader("HTTP_CLIENT_IP");
        }
        if (s == null || s.length() == 0 || "unknown".equalsIgnoreCase(s)) {

            s = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (s == null || s.length() == 0 || "unknown".equalsIgnoreCase(s)) {

            s = request.getRemoteAddr();
        }
        if ("127.0.0.1".equals(s) || "0:0:0:0:0:0:0:1".equals(s)) {
            try {
                s = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException unknownhostexception) {
                return "";
            }
        }
        return s;
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
