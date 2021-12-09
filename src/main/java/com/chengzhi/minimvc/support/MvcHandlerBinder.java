package com.chengzhi.minimvc.support;

import com.chengzhi.minimvc.converters.*;
import org.springframework.boot.autoconfigure.validation.ValidatorAdapter;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Administrator
 * @title: ycz
 * @projectName mini-mvc
 * @date 2021/11/22 0022下午 5:35
 */
public class MvcHandlerBinder {

    private ServletRequestDataBinder binder;
    private FormattingConversionServiceFactoryBean conversionServiceBean;

    protected WebDataBinder createBinder(Object target, String objectName) throws Exception {
        binder = new ServletRequestDataBinder(target, objectName);
        return binder;
    }

    private Set dealConversion() {
        Set<Converter> converters = new HashSet<>();
        converters.add(new String2BigDecimalConverter());
        converters.add(new String2DoubleConverter());
        converters.add(new String2IntegerConverter());
        converters.add(new String2LongConverter());
        converters.add(new String2NumberConverter());
        String2DateConverter string2DateConverter = new String2DateConverter();
        string2DateConverter.setFormats(Arrays.asList("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "yyyy/MM/dd"));
        string2DateConverter.init();
        converters.add(string2DateConverter);
        return converters;
    }

    protected void initBinder() throws Exception {
        // 绑定类型转换配置类
        if (null == conversionServiceBean) {
            conversionServiceBean = SpringContextUtil.getContext().getAutowireCapableBeanFactory().createBean(FormattingConversionServiceFactoryBean.class);
            conversionServiceBean.setConverters(dealConversion());
            binder.setConversionService(conversionServiceBean.getObject());
        }

        // 绑定ValidatorAdapter用注解校验参数
        ValidatorAdapter validatorAdapter = SpringContextUtil.getContext().getBean(ValidatorAdapter.class);
        if (binder.getTarget() != null && validatorAdapter.supports(binder.getTarget().getClass())) {
            binder.setValidator(validatorAdapter);
        }
        SpringValidatorAdapter springValidatorAdapter = SpringContextUtil.getContext().getBean(SpringValidatorAdapter.class);
        if (binder.getTarget() != null && springValidatorAdapter.supports(binder.getTarget().getClass())) {
            binder.setValidator(springValidatorAdapter);
        }
    }


    /**
     * 校验参数
     *
     * @param binder
     * @param methodParam
     */
    protected void validateIfApplicable(WebDataBinder binder, MethodParameter methodParam) {
        Annotation[] annotations = methodParam.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation ann = annotations[i];
            Validated validatedAnn = (Validated) AnnotationUtils.getAnnotation(ann, Validated.class);
            if (validatedAnn != null || ann.annotationType().getSimpleName().startsWith("Valid")) {
                Object hints = validatedAnn != null ? validatedAnn.value() : AnnotationUtils.getValue(ann);
                Object[] validationHints = hints instanceof Object[] ? (Object[]) ((Object[]) hints) : new Object[]{hints};
                binder.validate(validationHints);
                break;
            }
        }
    }

    /**
     * 是否绑定必要的异常
     *
     * @param binder
     * @param methodParam
     * @return
     */
    protected boolean isBindExceptionRequired(WebDataBinder binder, MethodParameter methodParam) {
        int i = methodParam.getParameterIndex();
        Class<?>[] paramTypes = methodParam.getMethod().getParameterTypes();
        boolean hasBindingResult = paramTypes.length > i + 1 && Errors.class.isAssignableFrom(paramTypes[i + 1]);
        return !hasBindingResult;
    }
}
