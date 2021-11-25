package com.chengzhi.mybaits.code_gen.plugin.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 用于指定Mapper对象对应的主表
 *
 * @author jiyu.gjy
 *
 */
@Retention(RUNTIME)
@Inherited
@Target(TYPE)
public @interface Table {
    String value();
}
