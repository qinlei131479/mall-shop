package com.tigshop.aspect;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tigshop.bean.model.log.RequestLog;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.IpUtils;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.mapper.log.RequestLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Tigshop
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RequestLogAspect {

    private final RequestLogMapper requestLogMapper;
    private final TigshopProperties tigshopProperties;

    /**
     * 定义切点：拦截所有带 @PostMapping 注解的方法
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMappingMethods() {}

    /**
     * 环绕通知：在方法执行前后进行处理
     */
    @Around("postMappingMethods()")
    public Object logPostRequests(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求相关信息
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();

        // 请求的 URL
        String url = request.getRequestURI();
        // HTTP 请求方法 (一般就是 POST)
        String httpMethod = request.getMethod();
        if (!url.startsWith("/adminapi") || !"POST".equalsIgnoreCase(httpMethod)) {
            return joinPoint.proceed();
        }

        // 类名 + 方法名
        String classMethod = joinPoint.getSignature().toShortString();

        // 获取请求参数（方法的所有入参）
        Object[] args = joinPoint.getArgs();
        String requestParams = "";
        try {
            // 将参数序列化成 JSON，方便存储和查看
            requestParams = new ObjectMapper().writeValueAsString(args);
        } catch (Exception e) {
            log.error("参数序列化失败", e);
        }

        // 构造日志对象
        RequestLog logRecord = new RequestLog();
        logRecord.setUrl(url);
        logRecord.setHttpMethod(httpMethod);
        logRecord.setClassMethod(classMethod);
        logRecord.setRequestParams(requestParams);
        logRecord.setAddTime(DateUtil.formatDateTime(DateUtil.date()));
        logRecord.setAdminId(SecurityUtils.getCurrentAdminId());
        logRecord.setAdminIp(IpUtils.getIpAddr());
        logRecord.setAdminUsername(SecurityUtils.getCurrentUsername());
        String platform = StrUtil.format("{}({})", tigshopProperties.getVersion(), tigshopProperties.getVersionNum());
        logRecord.setPlatform(platform);

        // 插入数据库
        requestLogMapper.insert(logRecord);
        // 保存到 request 中，方便后续使用
        request.setAttribute("REQUEST_LOG_ID", logRecord.getRequestLogId());
        // 执行原方法，并返回结果
        return joinPoint.proceed();
    }
}
