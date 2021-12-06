package com.chengzhi.mybaits.code_gen.plugin.dal.mybatis.generator;

import com.chengzhi.mybaits.code_gen.plugin.AbstractLavaBoImpl;
import com.chengzhi.mybaits.code_gen.plugin.LavaBo;
import com.chengzhi.mybaits.code_gen.plugin.LavaMapStruct;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.CommentGeneratorConfiguration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.PropertyRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 生成业务对象bo和boImpl和mapstruct对象映射
 */
public class BoPlugin extends PluginAdapter {

    private static final Logger log = LoggerFactory.getLogger(BoPlugin.class);

    protected static FullyQualifiedJavaType componentType = new FullyQualifiedJavaType("org.springframework.stereotype.Component");

    private static String annComponent = "@Component";

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        CommentGeneratorConfiguration commentCfg = new CommentGeneratorConfiguration();
        commentCfg.addProperty("beginningDelimiter", "");
        commentCfg.addProperty("endingDelimiter", "");
        commentCfg.setConfigurationType(MapperCommentGenerator.class.getCanonicalName());
        context.setCommentGeneratorConfiguration(commentCfg);

        // 支持oracle获取注释#114
        context.getJdbcConnectionConfiguration().addProperty("remarksReporting", "true");
    }

    /**
     * 生成**Bo.java和**BoImpl.java和**MapStruct.java
     * <p>
     */
    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        // 生成Bo路径
        String basePackage = this.properties.getProperty(GeneratorUtil.PROPERTY_BASE_PACKAGE);
        String basePath = this.properties.getProperty(GeneratorUtil.PROPERTY_BASE_PATH);
        String boPackage = this.properties.getProperty(GeneratorUtil.PROPERTY_BO_PACKAGE);
        String mapstructPackage = this.properties.getProperty(GeneratorUtil.PROPERTY_MAPSTRUCT_PACKAGE);
        log.info("mapstruct生成路径:{}", mapstructPackage);
        if (StringUtils.isBlank(boPackage)) {
            boPackage = basePackage + ".bo";
        }
        log.info("bo生成路径:{}", boPackage);
        String doName = GeneratorUtil.getDoName(introspectedTable);
        String dtoPackage = this.properties.getProperty(GeneratorUtil.PROPERTY_DTO_PACKAGE);
        if (StringUtils.isBlank(dtoPackage)) {
            dtoPackage = basePackage + ".model.dto";
        }
        log.info("dto生成路径:{}", dtoPackage);
        String dtoName = GeneratorUtil.getDtoName(dtoPackage, introspectedTable);
        String exmpName = GeneratorUtil.getExampleName(introspectedTable);
        String mapperName = GeneratorUtil.getMapperExtName(introspectedTable);
        String mapStructName = GeneratorUtil.getMapStructName(mapstructPackage, introspectedTable);
        String boName = GeneratorUtil.getBoName(boPackage, introspectedTable);

        // 建立Bo接口
        FullyQualifiedJavaType boType = new FullyQualifiedJavaType(boName);
        Interface boInterface = new Interface(boType);
        boInterface.setVisibility(JavaVisibility.PUBLIC);
        context.getCommentGenerator().addJavaFileComment(boInterface);

        // 建立mapstruct接口
        Interface mapInterface = new Interface(mapStructName);
        mapInterface.setVisibility(JavaVisibility.PUBLIC);
        mapInterface.addAnnotation("@Mapper(componentModel = \"spring\",unmappedTargetPolicy = ReportingPolicy.IGNORE)");
        mapInterface.addImportedType(new FullyQualifiedJavaType(Mapper.class.getName()));
        mapInterface.addImportedType(new FullyQualifiedJavaType(ReportingPolicy.class.getName()));

        // 继承StateBo或LavaBo
        String doShortName = GeneratorUtil.getShortClassName(doName);
        String dtoShortName = GeneratorUtil.getShortClassName(dtoName);
        String exmpShortName = GeneratorUtil.getShortClassName(exmpName);
        FullyQualifiedJavaType supperInterface = null;
        FullyQualifiedJavaType importInterface = null;

        FullyQualifiedJavaType mapstructInterface = new FullyQualifiedJavaType(GeneratorUtil.getShortClassName(LavaMapStruct.class.getName()) + "<" + doShortName + "," + dtoShortName + ">");
        FullyQualifiedJavaType importedTypeMap = new FullyQualifiedJavaType(LavaMapStruct.class.getName() + "<" + doName + "," + dtoName + ">");

        mapInterface.addImportedType(importedTypeMap);
        mapInterface.addSuperInterface(mapstructInterface);

        supperInterface = new FullyQualifiedJavaType(GeneratorUtil.getShortClassName(LavaBo.class.getName()) + "<" + doShortName + "," + dtoShortName + "," + exmpShortName + ">");
        importInterface = new FullyQualifiedJavaType(LavaBo.class.getName() + "<" + doName + "," + dtoName + "," + exmpName + ">");

        boInterface.addImportedType(importInterface);
        boInterface.addSuperInterface(supperInterface);

        GeneratedJavaFile generatedJavaFile = new GeneratedJavaFile(boInterface, basePath, context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
        GeneratedJavaFile generatedMapJavaFile = new GeneratedJavaFile(mapInterface, basePath, context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());

        if (isExistExtFile(basePath, generatedJavaFile.getTargetPackage(), generatedJavaFile.getFileName())) {
            return super.contextGenerateAdditionalJavaFiles(introspectedTable);
        }
        List<GeneratedJavaFile> generatedJavaFiles = new ArrayList<GeneratedJavaFile>(3);
        generatedJavaFiles.add(generatedJavaFile);
        generatedJavaFiles.add(generatedMapJavaFile);
        // 建立BoImpl类
        FullyQualifiedJavaType implType = new FullyQualifiedJavaType(GeneratorUtil.getBoImplName(boPackage, introspectedTable));
        TopLevelClass clazz = new TopLevelClass(implType);

        FullyQualifiedJavaType supperType = null;
        FullyQualifiedJavaType importType = null;

        supperType = new FullyQualifiedJavaType(GeneratorUtil.getShortClassName(AbstractLavaBoImpl.class.getName()) + "<" + doShortName + "," + dtoShortName + "," + GeneratorUtil.getShortClassName(mapperName) + "," + exmpShortName + "," + mapStructName + ">");
        importType = new FullyQualifiedJavaType(AbstractLavaBoImpl.class.getName() + "<" + doName + "," + dtoName + "," + mapperName + "," + exmpName + "," + mapStructName + ">");

        clazz.addAnnotation(annComponent);
        clazz.addImportedType(importType);
        clazz.addImportedType(componentType);
        clazz.setSuperClass(supperType);
        clazz.addImportedType(boType);
        clazz.addSuperInterface(boType);
        clazz.setVisibility(JavaVisibility.PUBLIC);

        createMethod("setBaseMapper", null, new FullyQualifiedJavaType(mapperName), "mapper", "@Autowired", "setMapper(mapper);", clazz);

        createMethod("setBaseMapStruct", null, new FullyQualifiedJavaType(mapStructName), "mapStruct", "@Autowired", "setMapperStruct(mapStruct);", clazz);

        FullyQualifiedJavaType doType = new FullyQualifiedJavaType("Class<" + doShortName + ">");
        createMethod("getDoClass", doType, null, null, "@Override", "return " + doShortName + ".class;", clazz);

        FullyQualifiedJavaType dtoType = new FullyQualifiedJavaType("Class<" + dtoShortName + ">");
        createMethod("getDtoClass", dtoType, null, null, "@Override", "return " + dtoShortName + ".class;", clazz);

        GeneratedJavaFile implFile = new GeneratedJavaFile(clazz, basePath, context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
        generatedJavaFiles.add(implFile);
        return generatedJavaFiles;
    }

    /**
     * 在接口中创建一个方法
     * <p>
     * 只支持1个参数
     *
     * @param name     方法名
     * @param rsType   返回类型
     * @param parmType 参数类型
     * @param parmName 参数名
     */
    protected void createMethod(String name, FullyQualifiedJavaType rsType, FullyQualifiedJavaType parmType, String parmName, Interface interfaze) {
        Method method = new Method(name);
        method.setVisibility(JavaVisibility.PUBLIC);
        if (rsType != null) {
            method.setReturnType(rsType);
            interfaze.addImportedType(rsType);
        }
        if (parmType != null) {
            Parameter param = new Parameter(parmType, parmName);
            method.addParameter(param);
            interfaze.addImportedType(parmType);
        }
        interfaze.addMethod(method);
    }

    /**
     * 在接口中创建一个方法
     * <p>
     * 只支持1个参数
     *
     * @param name       方法名
     * @param rsType     返回类型
     * @param parmType   参数类型
     * @param parmName   参数名
     * @param annotation 注解
     * @param body       方法体
     */
    protected void createMethod(String name, FullyQualifiedJavaType rsType, FullyQualifiedJavaType parmType, String parmName, String annotation, String body, TopLevelClass clazz) {
        Method method = new Method(name);
        method.setVisibility(JavaVisibility.PUBLIC);
        if (rsType != null) {
            method.setReturnType(rsType);
            clazz.addImportedType(rsType);
        }
        if (parmType != null) {
            Parameter param = new Parameter(parmType, parmName);
            method.addParameter(param);
            clazz.addImportedType(parmType);
        }
        method.addAnnotation(annotation);
        clazz.addImportedType("org.springframework.beans.factory.annotation.Autowired");
        method.addBodyLine(body);
        clazz.addMethod(method);
    }

    protected boolean isExistExtFile(String targetProject, String targetPackage, String fileName) {

        File project = new File(targetProject);
        if (project.exists() && !project.isDirectory()) {
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

    /**
     * This plugin is always valid - no properties are required
     */
    public boolean validate(List<String> warnings) {
        return true;
    }

}
