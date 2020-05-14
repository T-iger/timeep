package com.timeep.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Li
 * @date 2020/4/12
 **/
//ControllerAdvice会拦截 标有 Controller注解的
@ControllerAdvice
public class ControllerExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(Exception.class)//做异常处理拦截
    public ModelAndView exceptionHandler(HttpServletRequest request, Exception e) throws Exception {
        //日志记录访问的url和异常
        logger.error("Request URL : {},Exception : {}", request.getRequestURI(), e);

        /*
            如果AnnotationUtils注解实用，监测到异常和ResponseStatus不为空的情况，
            就是我们自定义的NotFoundException异常，让spring boot处理
        * */
        if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null) {
            throw e;
        }

        //跳转到我们自定义的错误页面
        ModelAndView mv = new ModelAndView();
        mv.addObject("url", request.getRequestURL());
        mv.addObject("exception", e);
        mv.setViewName("error/error");

        return mv;
    }

}
