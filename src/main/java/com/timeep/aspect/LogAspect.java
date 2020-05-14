package com.timeep.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @author Li
 **/
@Aspect //界面操作
@Component  //组件扫描
public class LogAspect {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    //定义一个切面
    @Pointcut("execution(* com.timeep.controller.*.*(..))")    //execution规定哪些类
    public void log(){}

    @Before("log()")
    public void doBefore(JoinPoint joinPoint){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request=attributes.getRequest();
        String url= request.getRequestURI();
        String ip =request.getRemoteAddr();
        String classMethod=joinPoint.getSignature().getDeclaringTypeName()+"."+joinPoint.getSignature().getName();
        Object[] args=joinPoint.getArgs();
        RequestLog requestLog=new RequestLog(url,ip,classMethod,args);
        logger.info("Request : {}",requestLog);
    }

    @After("log()")
    public void doAfter(){
        logger.info("----------After----------");
    }

    //拦截返回的值
    @AfterReturning(returning = "result",pointcut = "log()")
    public void doAfterReturn( Object result){
        logger.info("Result : {}",result);
    }

    //定义一个内部类存储这些值
    private class RequestLog{
        private String url; //请求url
        private String ip;  //访问者ip
        private String classMethod; //调用方法
        private Object[] args; //请求参数

        @Override
        public String toString() {
            return "RequestLog{" +
                    "url='" + url + '\'' +
                    ", ip='" + ip + '\'' +
                    ", classMethod='" + classMethod + '\'' +
                    ", args=" + Arrays.toString(args) +
                    '}';
        }

        public RequestLog(String url, String ip, String classMethod, Object[] args) {
            this.url = url;
            this.ip = ip;
            this.classMethod = classMethod;
            this.args = args;
        }
    }
}
