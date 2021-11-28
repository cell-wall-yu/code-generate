package com.chengzhi.minimvc.converters;

import org.springframework.core.convert.converter.Converter;

import java.math.BigDecimal;

public class String2BigDecimalConverter implements Converter<String, BigDecimal> {

	public BigDecimal convert(String arg0) {
		try {
			return BigDecimal.valueOf(Double.valueOf(arg0));
		} catch (Exception e) {
		}
		return null;
	}

}
