package com.chengzhi.mybaits.code_gen.plugin.dal.mybatis.generator;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 类SqlSafeCheckUtil.java的实现描述：TODO 类实现描述
 */
public class SqlSafeCheckUtil {

    private static final char UNDERLINE = '_';
    private static final String DESC = "desc";
    private static final String ASC = "asc";

    public static boolean checkOrderByClause(String orderByClause, Class<?> objectClass) {

        // FIXME 用于适配Lassen代码旧版本LavaExample中没有重写objectClass方法，Lassen升级后删除此段
        if (objectClass == null) {
            return true;
        }
        // FIX END

        boolean isSafe = false;

        if (StringUtils.isNotEmpty(orderByClause) && objectClass != null) {

            List<String> keyList = buildKeyWord(objectClass);
            Pattern p = Pattern.compile("\\s+");
            Matcher m = p.matcher(orderByClause);
            orderByClause = m.replaceAll(" ");

            String[] list = orderByClause.split(",");
            for (int i = 0; i < list.length; i++) {

                String item[] = list[i].split("\\s+");
                for (int j = 0; j < item.length; j++) {
                    if (!keyList.contains(item[j].toLowerCase())) {
                        isSafe = false;
                        return isSafe;
                    }

                }

            }

            isSafe = true;
        }
        return isSafe;
    }

    /**
     * 构建关键词
     *
     * @param obejctClass
     * @return
     */
    private static List<String> buildKeyWord(Class<?> obejctClass) {
        List<String> keyList = new ArrayList<String>();
        keyList.add(DESC);
        keyList.add(ASC);
        keyList.add("");

        Field[] fields = obejctClass.getDeclaredFields();
        keyList.addAll(getFields(fields));
        while (true) {
            Class<?> superclass = obejctClass.getSuperclass();
            if (superclass == null) {
                return keyList;
            }
            fields = superclass.getDeclaredFields();
            keyList.addAll(getFields(fields));
            obejctClass = superclass;
        }
    }

    protected static List<String> getFields(Field[] fields) {
        String[] fieldNames = new String[fields.length];

        for (int f = 0; f < fields.length; f++) {
            fieldNames[f] = toUnderline(fields[f].getName());
        }
        return Arrays.asList(fieldNames);
    }

    /**
     * 转换 驼峰命名到下划线的名
     *
     * @param param
     * @return
     */
    private static String toUnderline(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(UNDERLINE);
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
