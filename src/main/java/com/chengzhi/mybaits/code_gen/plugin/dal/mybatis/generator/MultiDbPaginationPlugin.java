package com.chengzhi.mybaits.code_gen.plugin.dal.mybatis.generator;

import com.chengzhi.mybaits.code_gen.plugin.LavaDo;
import com.chengzhi.mybaits.code_gen.plugin.LavaDto;
import com.chengzhi.mybaits.code_gen.plugin.LavaExample;
import com.chengzhi.mybaits.code_gen.plugin.LavaMapper;
import com.chengzhi.mybaits.code_gen.plugin.annotation.Table;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.codegen.XmlConstants;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.io.File;
import java.util.*;

/**
 * 支持一份mapper支持多数据源的插件
 */
public class MultiDbPaginationPlugin extends PluginAdapter {

    public final static String PROPERTY_EXTEND = "extend";

    protected static FullyQualifiedJavaType longType = new FullyQualifiedJavaType("java.lang.Long");
    protected static FullyQualifiedJavaType baseDoType = new FullyQualifiedJavaType(LavaDo.class.getName());
    protected static FullyQualifiedJavaType tableType = new FullyQualifiedJavaType(Table.class.getName());

    protected static String MAPPER_XML_FILE_POSTFIX = "Ext";
    protected static String MAPPER_JAVA_FILE_POTFIX = "Ext";
    protected static String SQLMAP_COMMON_POSTFIX = "and is_deleted = 'n'";
    protected static String ANNOTATION_RESOURCE = "javax.annotation.Resource";

    /**
     * 生成Do
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Properties tableProperties = introspectedTable.getTableConfiguration().getProperties();

        FullyQualifiedJavaType baseDoType;
        // LavaDo模式
        baseDoType = new FullyQualifiedJavaType(LavaDo.class.getName());
        // 基本属性
        topLevelClass.addImportedType(baseDoType);
        topLevelClass.setSuperClass(baseDoType);
        // 添加mapping默认列
        addBaseColumns(introspectedTable);
        // 判断扩展字段
        boolean isExtend = true;
        String extend = tableProperties.getProperty(PROPERTY_EXTEND);
        if (StringUtils.equalsIgnoreCase(extend, "false")) {
            isExtend = false;
        }
        if (isExtend) {
            // 为扩展字段添加 @JSONField(serialize = false)
            List<IntrospectedColumn> baseColumns = introspectedTable.getBaseColumns();
            for (IntrospectedColumn baseColumn : baseColumns) {
                String columnName = baseColumn.getActualColumnName();
                if (StringUtils.startsWith(columnName, "ext_")) {
                    String javaProperty = baseColumn.getJavaProperty();
                    String getterMethodName = JavaBeansUtil.getGetterMethodName(javaProperty, baseColumn.getFullyQualifiedJavaType());
                    Method method = getMethodInClass(topLevelClass, getterMethodName);
                    if (method != null) {
                        method.addAnnotation("@JSONField(serialize = false)");
                    }
                }
            }
        }

        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    protected Method getMethodInClass(TopLevelClass topLevelClass, String getterMethodName) {
        List<Method> methods = topLevelClass.getMethods();
        for (Method method : methods) {
            if (StringUtils.equals(method.getName(), getterMethodName)) {
                return method;
            }
        }
        return null;
    }

    /**
     * 创建get方法
     *
     * @param columnName
     * @param columnType
     * @param topLevelClass
     */
    protected Method createGetMethod(String columnName, FullyQualifiedJavaType columnType, TopLevelClass topLevelClass) {
        String propertyName = JavaBeansUtil.getCamelCaseString(columnName, false);
        String methodName = JavaBeansUtil.getGetterMethodName(propertyName, columnType);
        return createMethod(JavaVisibility.PUBLIC, methodName, null, columnType, null, "return this." + propertyName + ";", topLevelClass);
    }

    /**
     * 创建set方法
     *
     * @param columnName
     * @param columnType
     * @param topLevelClass
     */
    protected Method createSetMethod(String columnName, FullyQualifiedJavaType columnType, TopLevelClass topLevelClass) {
        String propertyName = JavaBeansUtil.getCamelCaseString(columnName, false);
        String methodName = JavaBeansUtil.getSetterMethodName(propertyName);
        List<Parameter> parms = new ArrayList<Parameter>();
        Parameter parm = new Parameter(columnType, propertyName);
        parms.add(parm);
        return createMethod(JavaVisibility.PUBLIC, methodName, null, null, parms, "this." + propertyName + " = " + propertyName + ";", topLevelClass);
    }

    /**
     * java类中添加方法
     *
     * @param name          方法名
     * @param annotation    注解
     * @param returnType    返回类型，传null为void
     * @param parms         参数列表，为空则没参数
     * @param body          方法体
     * @param topLevelClass
     */
    protected Method createMethod(JavaVisibility visi, String name, String annotation, FullyQualifiedJavaType returnType, List<Parameter> parms, String body, TopLevelClass topLevelClass) {
        Method method = new Method(name);
        method.setVisibility(visi);
        if (StringUtils.isNotBlank(annotation)) {
            method.addAnnotation(annotation);
        }
        if (returnType != null) {
            method.setReturnType(returnType);
            topLevelClass.addImportedType(returnType);
        }
        if (parms != null && !parms.isEmpty()) {
            for (Parameter parm : parms) {
                method.addParameter(parm);
                topLevelClass.addImportedType(parm.getType());
            }
        }
        method.addBodyLine(body);
        topLevelClass.addMethod(method);
        return method;
    }

    /**
     * java内部类中添加方法
     *
     * @param name       方法名
     * @param annotation 注解
     * @param returnType 返回类型，传null为void
     * @param parms      参数列表，为空则没参数
     * @param body       方法体
     * @param innerClass 内部类
     * @param outClass   外部类
     */
    protected void createInnerMethod(JavaVisibility visi, String name, String annotation, FullyQualifiedJavaType returnType, List<Parameter> parms, String body, InnerClass innerClass, TopLevelClass outClass) {
        Method method = new Method(name);
        method.setVisibility(visi);
        if (StringUtils.isNotBlank(annotation)) {
            method.addAnnotation(annotation);
        }
        if (returnType != null) {
            method.setReturnType(returnType);
            outClass.addImportedType(returnType);
        }
        if (parms != null && !parms.isEmpty()) {
            for (Parameter parm : parms) {
                method.addParameter(parm);
                outClass.addImportedType(parm.getType());
            }
        }
        method.addBodyLine(body);
        innerClass.addMethod(method);
    }

    /**
     * 给mapping添加默认列
     *
     * @param introspectedTable
     * @return
     */
    protected void addBaseColumns(IntrospectedTable introspectedTable) {
        // ID
        addColumn("id", "INTEGER", "id", longType, true, true, introspectedTable);
        // gmt_create
        addColumn("gmt_create", "TIMESTAMP", "gmtCreate", FullyQualifiedJavaType.getDateInstance(), false, false, introspectedTable);
        // creator
        addColumn("creator", "VARCHAR", "creator", FullyQualifiedJavaType.getStringInstance(), false, false, introspectedTable);
        // gmt_modified
        addColumn("gmt_modified", "TIMESTAMP", "gmtModified", FullyQualifiedJavaType.getDateInstance(), false, false, introspectedTable);
        // modifier
        addColumn("modifier", "VARCHAR", "modifier", FullyQualifiedJavaType.getStringInstance(), false, false, introspectedTable);
        // is_deleted
        addColumn("is_deleted", "CHAR", "isDeleted", FullyQualifiedJavaType.getStringInstance(), false, false, introspectedTable);

        // status
        String stateAction = introspectedTable.getTableConfiguration().getProperties().getProperty("stateAction");
        if (StringUtils.equalsIgnoreCase("true", stateAction)) {
            addColumn("status", "CHAR", "status", FullyQualifiedJavaType.getStringInstance(), false, false, introspectedTable);
        }
    }

    /**
     * 在mapper xml中添加列
     *
     * @param name              数据库列名
     * @param javaName          对应java字段名
     * @param jdbcType          数据库列类型
     * @param javaType          java字段类型
     * @param isSequence        是否自增列
     * @param isKey             是否主键
     * @param introspectedTable
     */
    protected void addColumn(String name, String jdbcType, String javaName, FullyQualifiedJavaType javaType, boolean isSequence, boolean isKey, IntrospectedTable introspectedTable) {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setActualColumnName(name);
        column.setJdbcTypeName(jdbcType);
        column.setJavaProperty(javaName);
        column.setSequenceColumn(isSequence);
        column.setFullyQualifiedJavaType(javaType);
        introspectedTable.addColumn(column);
        if (isKey) {
            introspectedTable.addPrimaryKeyColumn(name);
        }
    }

    /**
     * 为1个字段添加Criteria内部类方法<br>
     * 包括IsNull、EqualTo、GreaterThan、In、Between、Like等等
     *
     * @param fieldName
     * @param propertyName
     * @param type
     * @param returnClazz
     * @param innerClazz
     * @param outClazz
     */
    protected void createCriteriaMethod(String fieldName, String propertyName, FullyQualifiedJavaType type, InnerClass returnClazz, InnerClass innerClazz, TopLevelClass outClazz) {
        // IsNull
        createInnerMethod(JavaVisibility.PUBLIC, "and" + propertyName + "IsNull", null, returnClazz.getType(), null, "addCriterion(\"" + fieldName + " is null\");\n            return (Criteria) this;", innerClazz, outClazz);
        // IsNotNull
        createInnerMethod(JavaVisibility.PUBLIC, "and" + propertyName + "IsNotNull", null, returnClazz.getType(), null, "addCriterion(\"" + fieldName + " is not null\");\n            return (Criteria) this;", innerClazz, outClazz);
        // EqualTo
        List<Parameter> typeParms = new ArrayList<Parameter>();
        Parameter typeParm = new Parameter(type, "value");
        typeParms.add(typeParm);
        createInnerMethod(JavaVisibility.PUBLIC, "and" + propertyName + "EqualTo", null, returnClazz.getType(), typeParms, "addCriterion(\"" + fieldName + " =\", value, \"" + propertyName + "\");\n            return (Criteria) this;", innerClazz, outClazz);
        // NotEqualTo
        createInnerMethod(JavaVisibility.PUBLIC, "and" + propertyName + "NotEqualTo", null, returnClazz.getType(), typeParms, "addCriterion(\"" + fieldName + " <>\", value, \"" + propertyName + "\");\n            return (Criteria) this;", innerClazz, outClazz);
        // GreaterThan
        createInnerMethod(JavaVisibility.PUBLIC, "and" + propertyName + "GreaterThan", null, returnClazz.getType(), typeParms, "addCriterion(\"" + fieldName + " >\", value, \"" + propertyName + "\");\n            return (Criteria) this;", innerClazz, outClazz);
        // GreaterThanOrEqualTo
        createInnerMethod(JavaVisibility.PUBLIC, "and" + propertyName + "GreaterThanOrEqualTo", null, returnClazz.getType(), typeParms, "addCriterion(\"" + fieldName + " >=\", value, \"" + propertyName + "\");\n            return (Criteria) this;", innerClazz, outClazz);
        // LessThan
        createInnerMethod(JavaVisibility.PUBLIC, "and" + propertyName + "LessThan", null, returnClazz.getType(), typeParms, "addCriterion(\"" + fieldName + " <\", value, \"" + propertyName + "\");\n            return (Criteria) this;", innerClazz, outClazz);
        // LessThanOrEqualTo
        createInnerMethod(JavaVisibility.PUBLIC, "and" + propertyName + "LessThanOrEqualTo", null, returnClazz.getType(), typeParms, "addCriterion(\"" + fieldName + " <=\", value, \"" + propertyName + "\");\n            return (Criteria) this;", innerClazz, outClazz);
        // In
        List<Parameter> listParms = new ArrayList<Parameter>();
        Parameter listParm = new Parameter(new FullyQualifiedJavaType(List.class.getName() + "<" + type.getFullyQualifiedName() + ">"), "values");
        listParms.add(listParm);
        createInnerMethod(JavaVisibility.PUBLIC, "and" + propertyName + "In", null, returnClazz.getType(), listParms, "addCriterion(\"" + fieldName + " in\", values, \"" + propertyName + "\");\n            return (Criteria) this;", innerClazz, outClazz);
        // NotIn
        createInnerMethod(JavaVisibility.PUBLIC, "and" + propertyName + "NotIn", null, returnClazz.getType(), listParms, "addCriterion(\"" + fieldName + " not in\", values, \"" + propertyName + "\");\n            return (Criteria) this;", innerClazz, outClazz);
        // Between
        List<Parameter> parameterList = new ArrayList<Parameter>();
        Parameter value1 = new Parameter(type, "value1");
        Parameter value2 = new Parameter(type, "value2");
        parameterList.add(value1);
        parameterList.add(value2);
        createInnerMethod(JavaVisibility.PUBLIC, "and" + propertyName + "Between", null, returnClazz.getType(), parameterList, "addCriterion(\"" + fieldName + " between\", value1, value2, \"" + propertyName + "\");\n            return (Criteria) this;", innerClazz, outClazz);
        // NotBetween
        createInnerMethod(JavaVisibility.PUBLIC, "and" + propertyName + "NotBetween", null, returnClazz.getType(), parameterList, "addCriterion(\"" + fieldName + " not between\", value1, value2, \"" + propertyName + "\");\n            return (Criteria) this;", innerClazz, outClazz);
        // Like
        createInnerMethod(JavaVisibility.PUBLIC, "and" + propertyName + "Like", null, returnClazz.getType(), typeParms, "addCriterion(\"" + fieldName + " like\", value, \"" + propertyName + "\");\n            return (Criteria) this;", innerClazz, outClazz);
        // NotLike
        createInnerMethod(JavaVisibility.PUBLIC, "and" + propertyName + "NotLike", null, returnClazz.getType(), typeParms, "addCriterion(\"" + fieldName + " not like\", value, \"" + propertyName + "\");\n            return (Criteria) this;", innerClazz, outClazz);
    }

    protected String getTableName(IntrospectedTable introspectedTable) {
        return introspectedTable.getTableConfiguration().getTableName();
    }

    /**
     * 创建Example类
     */
    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        // 删除父类重复字段
        List<Field> fields = topLevelClass.getFields();
        if (fields != null) {
            for (int i = fields.size() - 1; i >= 0; i--) {
                Field field = fields.get(i);
                String name = field.getName();
                if ("orderByClause".equals(name) || "distinct".equals(name)) {
                    fields.remove(i);
                }
            }
        }
        // 删除父类重复方法
        List<Method> methods = topLevelClass.getMethods();
        if (methods != null) {
            for (int i = methods.size() - 1; i >= 0; i--) {
                Method method = methods.get(i);
                String name = method.getName();
                if (name.contains("OrderByClause") || name.contains("Distinct")) {
                    methods.remove(i);
                }
            }
        }

        String lavaExample = LavaExample.class.getName();
        List<InnerClass> innerClasses = topLevelClass.getInnerClasses();
        InnerClass innerClass = null;
        InnerClass returnClass = null;
        for (InnerClass ic : innerClasses) {
            FullyQualifiedJavaType type = ic.getType();
            if ("GeneratedCriteria".equals(type.getShortName())) {
                innerClass = ic;
            } else if ("Criteria".equals(type.getShortName())) {
                returnClass = ic;
            }
        }
        createCriteriaMethod("id", "Id", longType, returnClass, innerClass, topLevelClass);
        createCriteriaMethod("gmt_create", "GmtCreate", FullyQualifiedJavaType.getDateInstance(), returnClass, innerClass, topLevelClass);
        createCriteriaMethod("creator", "Creator", FullyQualifiedJavaType.getStringInstance(), returnClass, innerClass, topLevelClass);
        createCriteriaMethod("gmt_modified", "GmtModified", FullyQualifiedJavaType.getDateInstance(), returnClass, innerClass, topLevelClass);
        createCriteriaMethod("modifier", "Modifier", FullyQualifiedJavaType.getStringInstance(), returnClass, innerClass, topLevelClass);
        createCriteriaMethod("is_deleted", "IsDeleted", FullyQualifiedJavaType.getStringInstance(), returnClass, innerClass, topLevelClass);

        // 添加获取Do方法
        topLevelClass.addImportedType(baseDoType);
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("Class<? extends LavaDo>");
        String doName = GeneratorUtil.getDoName(introspectedTable);
        String shortDoName = GeneratorUtil.getShortClassName(doName);
        createMethod(JavaVisibility.PUBLIC, "getDoClass", "@Override", returnType, null, "return " + shortDoName + ".class;", topLevelClass);
        topLevelClass.addImportedType(doName);
        topLevelClass.setSuperClass(lavaExample);
        topLevelClass.addImportedType(lavaExample);

        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    // 添删改Document的sql语句及属性
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {

        XmlElement parentElement = document.getRootElement();

        updateDocumentNameSpace(introspectedTable, parentElement);

        moveDocumentInsertSql(parentElement);

        super.sqlMapInsertSelectiveElementGenerated(parentElement, introspectedTable);
        sqlMapInsertSelectiveForMutilDatabaseGenerated(parentElement, introspectedTable);

        moveDocumentUpdateByPrimaryKeySql(parentElement);

        sqlDeleteByPrimaryKeyForMutilDatabaseGenerated(parentElement, introspectedTable);

        generateMultiDbPageSql(parentElement, introspectedTable);

        // generateDataAccessSql(parentElement);

        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    protected void updateDocumentNameSpace(IntrospectedTable introspectedTable, XmlElement parentElement) {
        Attribute namespaceAttribute = null;
        for (Attribute attribute : parentElement.getAttributes()) {
            if (attribute.getName().equals("namespace")) {
                namespaceAttribute = attribute;
            }
        }
        parentElement.getAttributes().remove(namespaceAttribute);
        parentElement.getAttributes().add(new Attribute("namespace", introspectedTable.getMyBatis3JavaMapperType() + MAPPER_JAVA_FILE_POTFIX));
    }

    protected void moveDocumentInsertSql(XmlElement parentElement) {
        XmlElement insertElement = null;
        for (Element element : parentElement.getElements()) {
            XmlElement xmlElement = (XmlElement) element;
            if (xmlElement.getName().equals("insert")) {
                for (Attribute attribute : xmlElement.getAttributes()) {
                    if (attribute.getValue().equals("insert")) {
                        insertElement = xmlElement;
                        break;
                    }
                }

            }

        }
        parentElement.getElements().remove(insertElement);
    }

    protected void sqlMapInsertSelectiveForMutilDatabaseGenerated(XmlElement parentElement, IntrospectedTable introspectedTable) {
        XmlElement oldElement = null;
        XmlElement mysqlElement = null;
        for (Element element : parentElement.getElements()) {
            XmlElement xmlElement = (XmlElement) element;
            if (xmlElement.getName().equals("insert")) {
                for (Attribute attribute : xmlElement.getAttributes()) {
                    if (attribute.getValue().equals("insertSelective")) {
                        oldElement = xmlElement;
                        mysqlElement = (XmlElement) this.copyElement(xmlElement);
                        break;
                    }
                }
            }
        }
        parentElement.getElements().remove(oldElement);

        parentElement.addElement(mysqlElement);

        mysqlElement.addAttribute(new Attribute("useGeneratedKeys", "true"));
        mysqlElement.addAttribute(new Attribute("keyProperty", "id"));
    }

    protected void moveDocumentUpdateByPrimaryKeySql(XmlElement parentElement) {
        XmlElement updateElement = null;
        for (Element element : parentElement.getElements()) {
            XmlElement xmlElement = (XmlElement) element;
            if (xmlElement.getName().equals("update")) {
                for (Attribute attribute : xmlElement.getAttributes()) {
                    if (attribute.getValue().equals("updateByPrimaryKey")) {
                        updateElement = xmlElement;
                        break;
                    }
                }

            }

        }
        parentElement.getElements().remove(updateElement);
    }

    /**
     * 对于不同的数据库生成不同的DeleteByPrimaryKey语句 主要是修改时间上有差异，如oracle是sysdate，其他的未必是
     *
     * @param parentElement
     * @param introspectedTable
     */
    protected void sqlDeleteByPrimaryKeyForMutilDatabaseGenerated(XmlElement parentElement, IntrospectedTable introspectedTable) {
        // 添加oracle的删除语句
        // XmlElement oracleDeleteElement = new XmlElement("update");
        // context.getCommentGenerator().addComment(oracleDeleteElement);
        // Attribute oracleIdAttr = new Attribute("id", "deleteByPrimaryKey");
        // Attribute oracleParameterTypeAttr = new Attribute("parameterType", GeneratorUtil.getDoName(introspectedTable));
        // Attribute oracleDatabaseIdAttr = new Attribute("databaseId", "oracle");
        //
        // oracleDeleteElement.getAttributes().add(oracleIdAttr);
        // oracleDeleteElement.getAttributes().add(oracleParameterTypeAttr);
        // oracleDeleteElement.getAttributes().add(oracleDatabaseIdAttr);
        //
        // TextElement oracleSqlElement = new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime() + " set is_deleted = 'y',modifier=#{modifier,jdbcType=VARCHAR},gmt_modified=sysdate where id = #{id,jdbcType=NUMERIC}");
        // oracleDeleteElement.getElements().add(oracleSqlElement);
        // parentElement.addElement(oracleDeleteElement);

        // 添加mysql的删除语句
        XmlElement mysqlDeleteElement = new XmlElement("update");
        context.getCommentGenerator().addComment(mysqlDeleteElement);
        Attribute mysqlIdAttr = new Attribute("id", "deleteByPrimaryKey");
        Attribute mysqlParameterTypeAttr = new Attribute("parameterType", GeneratorUtil.getDoName(introspectedTable));
        // Attribute mysqlDatabaseIdAttr = new Attribute("databaseId", "mysql");

        mysqlDeleteElement.getAttributes().add(mysqlIdAttr);
        mysqlDeleteElement.getAttributes().add(mysqlParameterTypeAttr);
        // mysqlDeleteElement.getAttributes().add(mysqlDatabaseIdAttr);

        TextElement mysqlSqlElement = new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime() + " set is_deleted = 'y',modifier=#{modifier,jdbcType=VARCHAR},gmt_modified=current_timestamp where id = #{id,jdbcType=BIGINT}");
        mysqlDeleteElement.getElements().add(mysqlSqlElement);
        parentElement.addElement(mysqlDeleteElement);
    }

    /**
     * 不同数据库的分页不一样，需要不同的生成语句
     *
     * @param parentElement
     * @param introspectedTable
     */
    protected void generateMultiDbPageSql(XmlElement parentElement, IntrospectedTable introspectedTable) {
        // // oracle分页语句前半部分
        // XmlElement oraclePrefixElement = new XmlElement("sql");
        // context.getCommentGenerator().addComment(oraclePrefixElement);
        // oraclePrefixElement.addAttribute(new Attribute("id", "OracleDialectPrefix"));
        // XmlElement oraclePageStart = new XmlElement("if");
        // oraclePageStart.addAttribute(new Attribute("test", "page != null"));
        // oraclePageStart.addElement(new TextElement("select * from ( select row_.*, rownum rownum_ from ( "));
        // oraclePrefixElement.addElement(oraclePageStart);
        // parentElement.addElement(oraclePrefixElement);
        //
        // // oracle分页语句后半部分
        // XmlElement oracleSuffixElement = new XmlElement("sql");
        // context.getCommentGenerator().addComment(oracleSuffixElement);
        // oracleSuffixElement.addAttribute(new Attribute("id", "OracleDialectSuffix"));
        // XmlElement oraclePageEnd = new XmlElement("if");
        // oraclePageEnd.addAttribute(new Attribute("test", "page != null"));
        // oraclePageEnd.addElement(new TextElement("<![CDATA[ ) row_ ) where rownum_ >= #{page.begin} and rownum_ < #{page.end} ]]>"));
        // oracleSuffixElement.addElement(oraclePageEnd);
        // parentElement.addElement(oracleSuffixElement);
        //
        // // mysql分页语句前半部分
        // String tableName = getTableName(introspectedTable);
        // XmlElement mysqlPrefixElement = new XmlElement("sql");
        // context.getCommentGenerator().addComment(mysqlPrefixElement);
        // mysqlPrefixElement.addAttribute(new Attribute("id", "MysqlDialectPrefix"));
        // XmlElement mysqlPageStart = new XmlElement("if");
        // mysqlPageStart.addAttribute(new Attribute("test", "page != null"));
        // mysqlPageStart.addElement(new TextElement("from " + tableName + " where id in ( select id from ( select id "));
        // mysqlPrefixElement.addElement(mysqlPageStart);
        // parentElement.addElement(mysqlPrefixElement);
        //
        // // mysql分页语句后半部分
        // XmlElement mysqlSuffixElement = new XmlElement("sql");
        // context.getCommentGenerator().addComment(mysqlSuffixElement);
        // mysqlSuffixElement.addAttribute(new Attribute("id", "MysqlDialectSuffix"));
        // XmlElement mysqlPageEnd = new XmlElement("if");
        // mysqlPageEnd.addAttribute(new Attribute("test", "page != null"));
        // mysqlPageEnd.addElement(new TextElement("<![CDATA[ limit #{page.begin}, #{page.length} ) as temp_page_table) ]]>"));
        // mysqlSuffixElement.addElement(mysqlPageEnd);
        // // 再加排序
        // XmlElement ifEl = new XmlElement("if");
        // ifEl.addAttribute(new Attribute("test", "page != null and orderByClause != null"));
        // ifEl.addElement(new TextElement("order by ${orderByClause}"));
        // mysqlSuffixElement.addElement(ifEl);
        //
        // parentElement.addElement(mysqlSuffixElement);
    }

    // protected void generateDataAccessSql(XmlElement parentElement) {
    //
    // XmlElement fullOrgPathElement = new XmlElement("sql");
    // context.getCommentGenerator().addComment(fullOrgPathElement);
    // fullOrgPathElement.addAttribute(new Attribute("id", "fullOrgPath"));
    // XmlElement pageStart = new XmlElement("if");
    // pageStart.addAttribute(new Attribute("test", "fullOrgPath != null"));
    // pageStart.addElement(new TextElement(SQLMAP_COMMON_POTFIX_PVG));
    // fullOrgPathElement.addElement(pageStart);
    // parentElement.addElement(fullOrgPathElement);
    //
    // XmlElement ownerElement = new XmlElement("sql");
    // context.getCommentGenerator().addComment(ownerElement);
    // ownerElement.addAttribute(new Attribute("id", "owner"));
    // XmlElement pageEnd = new XmlElement("if");
    // pageEnd.addAttribute(new Attribute("test", "owner != null"));
    // pageEnd.addElement(new TextElement(SQLMAP_COMMON_POTFIX_OWNER));
    // ownerElement.addElement(pageEnd);
    // parentElement.addElement(ownerElement);
    // }

    protected Element copyElement(Element element) {
        if (element instanceof XmlElement) {
            XmlElement xmlElement = (XmlElement) element;
            String name = xmlElement.getName();
            List<Attribute> aList = xmlElement.getAttributes();
            List<Element> eList = xmlElement.getElements();
            XmlElement newElement = new XmlElement(name);
            if (aList != null) {
                for (int i = 0; i < aList.size(); i++) {
                    newElement.addAttribute(new Attribute(aList.get(i).getName(), aList.get(i).getValue()));
                }
            }
            if (eList != null) {
                for (int i = 0; i < eList.size(); i++) {
                    Element e = this.copyElement(eList.get(i));
                    newElement.addElement(e);
                }
            }
            return newElement;
        } else if (element instanceof TextElement) {
            TextElement textElement = (TextElement) element;
            TextElement newElement = new TextElement(textElement.getContent());
            return newElement;
        }

        return null;
    }

    // selectByPrimaryKey
    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        TextElement text = new TextElement(SQLMAP_COMMON_POSTFIX);
        element.addElement(text);
        return super.sqlMapSelectByPrimaryKeyElementGenerated(element, introspectedTable);
    }

    // updateByPrimaryKeySelective
    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        // List<Element> elements = element.getElements();
        // XmlElement setItem = null;
        // int modifierItemIndex = -1;
        // int gmtModifiedItemIndex = -1;
        // for (Element e : elements) {
        // if (e instanceof XmlElement) {
        // setItem = (XmlElement) e;
        // for (int i = 0; i < setItem.getElements().size(); i++) {
        // XmlElement xmlElement = (XmlElement) setItem.getElements().get(i);
        // for (Attribute att : xmlElement.getAttributes()) {
        // if (att.getValue().equals("modifier != null")) {
        // modifierItemIndex = i;
        // break;
        // }
        //
        // if (att.getValue().equals("gmtModified != null")) {
        // gmtModifiedItemIndex = i;
        // break;
        // }
        //
        // }
        // }
        // }
        //
        // }
        //
        // if (modifierItemIndex != -1 && setItem != null) {
        // addXmlElementmodifier(setItem, modifierItemIndex);
        //
        // }
        //
        // if (gmtModifiedItemIndex != -1 && setItem != null) {
        // addGmtModifiedXmlElement(setItem, gmtModifiedItemIndex);
        // }

        // TextElement text = new TextElement(SQLMAP_COMMON_POTFIX);
        // element.addElement(text);
        return super.sqlMapUpdateByPrimaryKeySelectiveElementGenerated(element, introspectedTable);
    }

    // protected void addGmtModifiedXmlElement(XmlElement setItem, int gmtModifiedItemIndex) {
    // XmlElement defaultGmtModified = new XmlElement("if");
    // defaultGmtModified.addAttribute(new Attribute("test", "gmtModified == null"));
    //
    // XmlElement oracleModifiedTest = new XmlElement("if");
    // oracleModifiedTest.addAttribute(new Attribute("test", "_databaseId == 'oracle'"));
    // oracleModifiedTest.addElement(new TextElement("gmt_modified = sysdate,"));
    // defaultGmtModified.addElement(oracleModifiedTest);
    //
    // XmlElement mysqlModifiedTest = new XmlElement("if");
    // mysqlModifiedTest.addAttribute(new Attribute("test", "_databaseId == null||_databaseId == 'mysql'"));
    // mysqlModifiedTest.addElement(new TextElement("gmt_modified = current_timestamp,"));
    // defaultGmtModified.addElement(mysqlModifiedTest);
    //
    // setItem.getElements().add(gmtModifiedItemIndex + 1, defaultGmtModified);
    // }

    // updateByPrimaryKey
    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return super.sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    // deleteByPrimaryKey
    public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        // 原有的deleteByPrimaryKey不需要，进行自定义
        return false;
    }

    // insert
    public void sqlInsertSelectiveGenerated(XmlElement parentElement, String dbType, XmlElement element, IntrospectedTable introspectedTable) {
        List<Element> elements = element.getElements();
        parentElement.addElement(element);
        // Element fixE = elements.get(5);
        // mysql添加属性
        if (dbType != null && dbType.equals("mysql")) {
            element.addAttribute(new Attribute("useGeneratedKeys", "true"));
            element.addAttribute(new Attribute("keyProperty", "id"));
        }

        XmlElement fieldItem = null;
        XmlElement valueItem = null;
        for (Element e : elements) {
            if (e instanceof XmlElement) {
                XmlElement xmlElement = (XmlElement) e;
                if (xmlElement.getName().equals("trim")) {
                    for (Attribute arr : xmlElement.getAttributes()) {
                        if (arr.getValue().equals("(")) {
                            fieldItem = xmlElement;
                            break;
                        }

                        if (arr.getValue().equals("values (")) {
                            valueItem = xmlElement;
                            break;
                        }
                    }
                }
            }
        }

        if (fieldItem != null) {
            XmlElement defaultGmtCreate = new XmlElement("if");
            defaultGmtCreate.addAttribute(new Attribute("test", "gmtCreate == null"));
            defaultGmtCreate.addElement(new TextElement("gmt_create,"));
            fieldItem.addElement(1, defaultGmtCreate);

            XmlElement defaultGmtModified = new XmlElement("if");
            defaultGmtModified.addAttribute(new Attribute("test", "gmtModified == null"));
            defaultGmtModified.addElement(new TextElement("gmt_modified,"));
            fieldItem.addElement(1, defaultGmtModified);

            XmlElement defaultmodifier = new XmlElement("if");
            defaultmodifier.addAttribute(new Attribute("test", "modifier == null"));
            defaultmodifier.addElement(new TextElement("modifier,"));
            fieldItem.addElement(1, defaultmodifier);

            XmlElement defaultcreator = new XmlElement("if");
            defaultcreator.addAttribute(new Attribute("test", "creator == null"));
            defaultcreator.addElement(new TextElement("creator,"));
            fieldItem.addElement(1, defaultcreator);

            XmlElement defaultIsDeleted = new XmlElement("if");
            defaultIsDeleted.addAttribute(new Attribute("test", "isDeleted == null"));
            defaultIsDeleted.addElement(new TextElement("is_deleted,"));
            fieldItem.addElement(1, defaultIsDeleted);

        }

        if (valueItem != null) {
            XmlElement defaultGmtCreate = new XmlElement("if");
            defaultGmtCreate.addAttribute(new Attribute("test", "gmtCreate == null"));

            if (dbType != null && dbType.equals("mysql")) {
                defaultGmtCreate.addElement(new TextElement("current_timestamp,"));
            } else if (dbType != null && dbType.equals("oracle")) {
                defaultGmtCreate.addElement(new TextElement("sysdate,"));
            }

            valueItem.addElement(defaultGmtCreate);

            XmlElement defaultGmtModified = new XmlElement("if");
            defaultGmtModified.addAttribute(new Attribute("test", "gmtModified == null"));

            if (dbType != null && dbType.equals("mysql")) {
                defaultGmtModified.addElement(new TextElement("current_timestamp,"));
            } else if (dbType != null && dbType.equals("oracle")) {
                defaultGmtModified.addElement(new TextElement("sysdate,"));
            }

            valueItem.addElement(defaultGmtModified);

            XmlElement defaultmodifier = new XmlElement("if");
            defaultmodifier.addAttribute(new Attribute("test", "modifier == null"));
            defaultmodifier.addElement(new TextElement("'system',"));
            valueItem.addElement(defaultmodifier);

            XmlElement defaultcreator = new XmlElement("if");
            defaultcreator.addAttribute(new Attribute("test", "creator == null"));
            defaultcreator.addElement(new TextElement("'system',"));
            valueItem.addElement(defaultcreator);

            XmlElement defaultIsDeleted = new XmlElement("if");
            defaultIsDeleted.addAttribute(new Attribute("test", "isDeleted == null"));
            defaultIsDeleted.addElement(new TextElement("'n',"));
            valueItem.addElement(defaultIsDeleted);

        }
    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        Parameter parameter = new Parameter(new FullyQualifiedJavaType(GeneratorUtil.getDoName(introspectedTable)), "record");
        method.getParameters().clear();
        method.addParameter(parameter);
        return super.clientDeleteByPrimaryKeyMethodGenerated(method, interfaze, introspectedTable);
    }

    /**
     * 不再需要Mapper.java类，直接使用mapperExt.java
     */
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    // selectByExample
    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        XmlElement lastXmlE = (XmlElement) element.getElements().remove(element.getElements().size() - 1);

        // XmlElement oraclePrefixTest = new XmlElement("if");
        // oraclePrefixTest.addAttribute(new Attribute("test", "_databaseId == 'oracle'"));
        // XmlElement oraclePrefix = new XmlElement("include");
        // oraclePrefix.addAttribute(new Attribute("refid", "OracleDialectPrefix"));
        // oraclePrefixTest.addElement(oraclePrefix);
        // element.getElements().add(5, oraclePrefixTest);
        //
        // XmlElement mysqlPrefixTest = new XmlElement("if");
        // mysqlPrefixTest.addAttribute(new Attribute("test", "_databaseId == null||_databaseId == 'mysql'"));
        // XmlElement mysqlPrefix = new XmlElement("include");
        // mysqlPrefix.addAttribute(new Attribute("refid", "MysqlDialectPrefix"));
        // mysqlPrefixTest.addElement(mysqlPrefix);
        // element.getElements().add(9, mysqlPrefixTest);

        XmlElement isdeletedElement = new XmlElement("if");
        isdeletedElement.addAttribute(new Attribute("test", "oredCriteria.size !=0 and oredCriteria.get(0).criteria.size != 0"));
        isdeletedElement.addElement(new TextElement(SQLMAP_COMMON_POSTFIX));
        element.addElement(isdeletedElement);
        isdeletedElement = new XmlElement("if");
        isdeletedElement.addAttribute(new Attribute("test", "oredCriteria.size ==0 or oredCriteria.get(0).criteria.size == 0"));
        isdeletedElement.addElement(new TextElement("where is_deleted = 'n'"));
        element.addElement(isdeletedElement);

        element.addElement(lastXmlE);

        // XmlElement oracleSuffixTest = new XmlElement("if");
        // oracleSuffixTest.addAttribute(new Attribute("test", "_databaseId == 'oracle'"));
        // XmlElement oracleSuffix = new XmlElement("include");
        // oracleSuffix.addAttribute(new Attribute("refid", "OracleDialectSuffix"));
        // oracleSuffixTest.addElement(oracleSuffix);
        // element.getElements().add(oracleSuffixTest);
        //
        // XmlElement mysqlSuffixTest = new XmlElement("if");
        // mysqlSuffixTest.addAttribute(new Attribute("test", "_databaseId == null||_databaseId == 'mysql'"));
        // XmlElement mysqlSuffix = new XmlElement("include");
        // mysqlSuffix.addAttribute(new Attribute("refid", "MysqlDialectSuffix"));
        // mysqlSuffixTest.addElement(mysqlSuffix);
        // element.getElements().add(mysqlSuffixTest);

        return super.sqlMapSelectByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    // countByExample
    @Override
    public boolean sqlMapCountByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        List<Attribute> attrs = element.getAttributes();
        for (int i = attrs.size() - 1; i >= 0; i--) {
            Attribute attr = attrs.get(i);
            if ("resultType".equals(attr.getName())) {
                Attribute newattr = new Attribute("resultType", Integer.class.getName());
                attrs.remove(i);
                attrs.add(i, newattr);
            }
        }

        XmlElement isNotNullElement = new XmlElement("if");
        isNotNullElement.addAttribute(new Attribute("test", "oredCriteria.size != 0 and oredCriteria.get(0).criteria.size != 0"));
        isNotNullElement.addElement(new TextElement(SQLMAP_COMMON_POSTFIX));
        element.addElement(isNotNullElement);
        isNotNullElement = new XmlElement("if");
        isNotNullElement.addAttribute(new Attribute("test", "oredCriteria.size == 0 or oredCriteria.get(0).criteria.size == 0"));
        isNotNullElement.addElement(new TextElement("where is_deleted = 'n'"));
        element.addElement(isNotNullElement);
        // XmlElement fullOrgPath = new XmlElement("include");
        // fullOrgPath.addAttribute(new Attribute("refid", "fullOrgPath"));
        // element.addElement(fullOrgPath);
        //
        // XmlElement owner = new XmlElement("include");
        // owner.addAttribute(new Attribute("refid", "owner"));
        // element.addElement(owner);
        return super.sqlMapCountByExampleElementGenerated(element, introspectedTable);
    }

    // 生成XXExt.xml
    @Override
    public List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles(IntrospectedTable introspectedTable) {

        String[] splitFile = introspectedTable.getMyBatis3XmlMapperFileName().split("\\.");
        String fileNameExt = null;
        if (splitFile[0] != null) {
            fileNameExt = splitFile[0] + MAPPER_XML_FILE_POSTFIX + ".xml";
        }

        if (isExistExtFile(context.getSqlMapGeneratorConfiguration().getTargetProject(), introspectedTable.getMyBatis3XmlMapperPackage(), fileNameExt)) {
            return super.contextGenerateAdditionalXmlFiles(introspectedTable);
        }

        Document document = new Document(XmlConstants.MYBATIS3_MAPPER_PUBLIC_ID, XmlConstants.MYBATIS3_MAPPER_SYSTEM_ID);

        XmlElement root = new XmlElement("mapper");
        document.setRootElement(root);
        String namespace = introspectedTable.getMyBatis3SqlMapNamespace() + MAPPER_XML_FILE_POSTFIX;
        root.addAttribute(new Attribute("namespace", namespace));

        GeneratedXmlFile gxf = new GeneratedXmlFile(document, fileNameExt, introspectedTable.getMyBatis3XmlMapperPackage(), context.getSqlMapGeneratorConfiguration().getTargetProject(), false, context.getXmlFormatter());

        List<GeneratedXmlFile> answer = new ArrayList<GeneratedXmlFile>(1);
        answer.add(gxf);

        return answer;
    }

    /**
     * 生成其他Java文件<br>
     * 目前生成有MapperExt、Dto
     */
    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> generatedJavaFiles = new ArrayList<GeneratedJavaFile>(1);

        GeneratedJavaFile mapperExt = generateMapperExtJava(introspectedTable);
        if (mapperExt != null) {
            generatedJavaFiles.add(mapperExt);
        }

        GeneratedJavaFile dto = generateDto(introspectedTable);
        if (dto != null) {
            generatedJavaFiles.add(dto);
        }

        // GeneratedJavaFile vo = generateVo(introspectedTable);
        // if (vo != null) {
        // generatedJavaFiles.add(vo);
        // }
        return generatedJavaFiles;
    }

    protected GeneratedJavaFile generateMapperExtJava(IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType() + MAPPER_JAVA_FILE_POTFIX);
        Interface interfaze = new Interface(type);
        interfaze.setVisibility(JavaVisibility.PUBLIC);
        context.getCommentGenerator().addJavaFileComment(interfaze);

        FullyQualifiedJavaType annotation = new FullyQualifiedJavaType(ANNOTATION_RESOURCE);
        interfaze.addAnnotation("@Resource");
        interfaze.addImportedType(annotation);

        String tableName = introspectedTable.getTableConfiguration().getTableName();
        interfaze.addAnnotation("@" + Table.class.getSimpleName() + "(\"" + tableName + "\")");
        interfaze.addImportedType(tableType);

        // 继承LavaMapper<Do,Exam>
        String doName = GeneratorUtil.getDoName(introspectedTable);
        String exmpName = introspectedTable.getExampleType();
        FullyQualifiedJavaType lavaMapperType = new FullyQualifiedJavaType(LavaMapper.class.getName() + "<" + doName + "," + exmpName + ">");
        interfaze.addImportedType(lavaMapperType);
        lavaMapperType = new FullyQualifiedJavaType(LavaMapper.class.getSimpleName() + "<" + getShortClassName(doName) + "," + getShortClassName(exmpName) + ">");
        interfaze.addSuperInterface(lavaMapperType);

        CompilationUnit compilationUnits = interfaze;
        GeneratedJavaFile generatedJavaFile = new GeneratedJavaFile(compilationUnits, context.getJavaModelGeneratorConfiguration().getTargetProject(), context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());

        if (isExistExtFile(generatedJavaFile.getTargetProject(), generatedJavaFile.getTargetPackage(), generatedJavaFile.getFileName())) {
            return null;
        }
        return generatedJavaFile;
    }

    protected GeneratedJavaFile generateDto(IntrospectedTable introspectedTable) {
        String dtoPackage = this.properties.getProperty(GeneratorUtil.PROPERTY_DTO_PACKAGE);
        String boPackage = this.properties.getProperty(GeneratorUtil.PROPERTY_BO_PACKAGE);

        String dtoName = GeneratorUtil.getDtoName(dtoPackage, introspectedTable);

        FullyQualifiedJavaType type = new FullyQualifiedJavaType(dtoName);
        TopLevelClass clazz = new TopLevelClass(type);
        clazz.setVisibility(JavaVisibility.PUBLIC);
        FullyQualifiedJavaType baseDtoType;
        baseDtoType = new FullyQualifiedJavaType(LavaDto.class.getName());
        clazz.setSuperClass(baseDtoType);
        clazz.addImportedType(baseDtoType);
        context.getCommentGenerator().addJavaFileComment(clazz);

        GeneratedJavaFile generatedJavaFile = new GeneratedJavaFile(clazz, context.getJavaModelGeneratorConfiguration().getTargetProject(), context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());

        if (isExistExtFile(generatedJavaFile.getTargetProject(), generatedJavaFile.getTargetPackage(), generatedJavaFile.getFileName())) {
            return null;
        }
        addBaseFields(introspectedTable, clazz);
        return generatedJavaFile;
    }

    protected void addBaseFields(IntrospectedTable introspectedTable, TopLevelClass clazz) {
        List<IntrospectedColumn> baseColumns = introspectedTable.getBaseColumns();
        Method method;
        for (IntrospectedColumn baseColumn : baseColumns) {
            String columnName = baseColumn.getActualColumnName();
            if (isBizColumn(columnName)) {

                String javaProperty = baseColumn.getJavaProperty();
                Field field = new Field(javaProperty, baseColumn.getFullyQualifiedJavaType());
                context.getCommentGenerator().addFieldComment(field, introspectedTable, baseColumn);
                field.setVisibility(JavaVisibility.PRIVATE);
                clazz.addField(field);

                FullyQualifiedJavaType filedType = baseColumn.getFullyQualifiedJavaType();
                method = createGetMethod(columnName, filedType, clazz);
                // context.getCommentGenerator().addGetterComment(method, introspectedTable, baseColumn);
                method = createSetMethod(columnName, filedType, clazz);
                // context.getCommentGenerator().addGetterComment(method, introspectedTable, baseColumn);

            }
        }
    }

    /**
     * 是否业务字段
     *
     * @param columnName
     * @return
     */
    protected boolean isBizColumn(String columnName) {
        // 系统默认字段不算
        for (String defaultColumn : defaultColumns) {
            if (StringUtils.equalsIgnoreCase(defaultColumn, columnName)) {
                return false;
            }
        }
        // 扩展字段不算
        if (StringUtils.startsWithIgnoreCase(columnName, "ext_")) {
            return false;
        }
        return true;
    }

    protected static List<String> defaultColumns = Arrays.asList("id", "creator", "gmt_create", "modifier", "gmt_modified", "is_deleted");

    protected boolean isExistExtFile(String targetProject, String targetPackage, String fileName) {

        File project = new File(targetProject);
        if (!project.isDirectory()) {
            return true;
        }

        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(targetPackage, ".");
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
            sb.append(File.separatorChar);
        }

        File directory = new File(project, sb.toString());
        if (!directory.isDirectory()) {
            boolean rc = directory.mkdirs();
            if (!rc) {
                return true;
            }
        }

        File testFile = new File(directory, fileName);
        if (testFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * 获得类名短名称，不带路径
     *
     * @param className 类全名
     * @return
     */
    protected String getShortClassName(String className) {
        return className.substring(className.lastIndexOf(".") + 1);
    }

    public static void main(String[] args) {
        String config = MultiDbPaginationPlugin.class.getClassLoader().getResource("generatorConfig_old.xml").getFile();
        String[] arg = {"-configfile", config, "-overwrite"};
        ShellRunner.main(arg);
    }
}
