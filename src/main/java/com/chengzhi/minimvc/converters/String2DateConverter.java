package com.chengzhi.minimvc.converters;

import com.chengzhi.utils.CommonUtil;
import org.springframework.core.convert.converter.Converter;

import java.util.Date;
import java.util.List;

public class String2DateConverter implements Converter<String, Date> {

    private List<String> formats;

    public void init() {
        if (formats == null || formats.isEmpty()) {
            throw new RuntimeException("springmvc String2DateConverter 参数配置有误");
        }
    }

    public Date convert(String arg0) {
        for (String format : formats) {
            try {
                return CommonUtil.getSdf(format).parse(arg0);
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }

    public void setFormats(List<String> formats) {
        this.formats = formats;
    }

}
