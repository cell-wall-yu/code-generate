package com.chengzhi.minimvc.support;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

import javax.servlet.http.HttpServletRequest;

public class MvcMappingHandleMapping extends AbstractHandlerMapping {
    protected HandlerInterceptor[] interceptors;

    @Override
    protected Object getHandlerInternal(HttpServletRequest httpServletRequest) throws Exception {
        interceptors = this.getAdaptedInterceptors();
        return null;
    }
}
