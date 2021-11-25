package com.chengzhi.mybaits.code_gen.plugin.dal.mybatis.generator;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedTable;

public class GeneratorUtil {

    public static String SPOT = ".";

    public final static String PROPERTY_BASE_PATH = "basePath";
    public final static String PROPERTY_BASE_PACKAGE = "basePackage";
    public final static String PROPERTY_BO_PACKAGE = "boPackage";
    public final static String PROPERTY_DTO_PACKAGE = "dtoPackage";

    public static String EXT_POSTFIX = "Ext";

    public static String SCENARIO = "Scenario";
    public static String DTO_POSTFIX = "DTO";
    public static String VO_POSTFIX = "VO";
    public static String BO_POSTFIX = "BO";
    public static String BO_IMPL_POSTFIX = "BOImpl";
    public static String BO_IMPL_PACKAGE = "impl";

    /**
     * 获取Do名称
     */
    public static String getDoName(IntrospectedTable introspectedTable) {
        return introspectedTable.getBaseRecordType();
    }

    /**
     * 获取Dto名称
     */
    public static String getDtoName(String dtoPackage, IntrospectedTable introspectedTable) {
        return getXoName(dtoPackage, introspectedTable, DTO_POSTFIX);
    }

    /**
     * 获取Vo名称
     */
    public static String getVoName(String voPackage, IntrospectedTable introspectedTable) {
        return getXoName(voPackage, introspectedTable, VO_POSTFIX);
    }

    private static String getXoName(String xoPackage, IntrospectedTable introspectedTable, String postfix) {
        String doName = getDoName(introspectedTable);
        String dtoPrefix = xoPackage + SPOT + getShortClassName(doName);
        if (dtoPrefix.endsWith("DO") || dtoPrefix.endsWith("Do")) {
            return dtoPrefix.substring(0, dtoPrefix.length() - 2) + postfix;
        }
        return dtoPrefix + postfix;
    }

    /**
     * 获取MapperExt名称
     */
    public static String getMapperExtName(IntrospectedTable introspectedTable) {
        return introspectedTable.getMyBatis3JavaMapperType() + EXT_POSTFIX;
    }

    /**
     * 获取Example.java名称
     */
    public static String getExampleName(IntrospectedTable introspectedTable) {
        return introspectedTable.getExampleType();
    }

    /**
     * 获取Bo名称
     */
    public static String getBoName(String boPackage, IntrospectedTable introspectedTable) {
        if (isMasterEntity(introspectedTable)) {
            return boPackage + SPOT + getShortClassName(getDoName(introspectedTable)).replace("DO", "") + SCENARIO
                    + BO_POSTFIX;
        } else {
            return boPackage + SPOT + getShortClassName(getDoName(introspectedTable)).replace("DO", "") + BO_POSTFIX;
        }
    }

    /**
     * 获取Bo实现类名称
     */
    public static String getBoImplName(String boPackage, IntrospectedTable introspectedTable) {
        if (isMasterEntity(introspectedTable)) {
            return boPackage + SPOT + BO_IMPL_PACKAGE + SPOT + getShortClassName(getDoName(introspectedTable)).replace(
                    "DO", "") + SCENARIO + BO_IMPL_POSTFIX;
        } else {

            return boPackage + SPOT + BO_IMPL_PACKAGE + SPOT + getShortClassName(getDoName(introspectedTable)).replace(
                    "DO", "") + BO_IMPL_POSTFIX;
        }
    }

    /**
     * 获得类的包名
     */
    public static String getPackage(String className) {
        return className.substring(0, className.lastIndexOf(SPOT));
    }

    /**
     * 获得类名短名称，不带路径
     *
     * @param className 类全名
     */
    public static String getShortClassName(String className) {
        return className.substring(className.lastIndexOf(SPOT) + 1);
    }

    /**
     * @param introspectedTable
     * @return
     */
    public static boolean isMasterEntity(IntrospectedTable introspectedTable) {
        String masterEntity = introspectedTable.getTableConfiguration().getProperties().getProperty("masterEntity");
        boolean isMasterEntity = StringUtils.equalsIgnoreCase("true", masterEntity);
        return isMasterEntity;
    }
}
