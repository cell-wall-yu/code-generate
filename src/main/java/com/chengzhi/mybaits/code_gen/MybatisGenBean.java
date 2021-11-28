package com.chengzhi.mybaits.code_gen;

import com.chengzhi.mybaits.code_gen.plugin.MyBatisGenerator;
import com.chengzhi.utils.CommonUtil;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

public class MybatisGenBean {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(MybatisGenBean.class);

    private static Properties properties = new Properties();

    public static void start() throws Exception {

        // xml路径,mainClass,模块包名,src路径
        String xmlPath, mainClass, doSubPackage, basePath = null;


        // xml 配置文件路径
        mainClass = System.getProperty("sun.java.command");
        log.info("mainClass: {}", mainClass);
        mainClass = mainClass.replaceAll("\\.", "/");
        mainClass = "/" + mainClass + ".class";
        java.net.URL url = MybatisGenBean.class.getResource(mainClass);
        String path = url.getPath();
        xmlPath = path.substring(0, path.lastIndexOf("/") + 1) + "generatorConfig.xml";
        basePath = xmlPath.substring(0, xmlPath.indexOf("target/classes/")) + "src";
        log.info("当前 mainClass: {}", mainClass);


        // 加载配置项
        String propertiesFilePath = xmlPath.substring(0, xmlPath.lastIndexOf(".")) + ".properties";
        properties.load(new InputStreamReader(new FileInputStream(propertiesFilePath), "utf-8"));
        doSubPackage = properties.getProperty("basePackage") + ".module." + properties.getProperty("moduleName");
        properties.put("doSubPackage", doSubPackage);
        properties.put("basePath", basePath);
        doScan(xmlPath);
    }

    private static void doScan(String generatorConfig) throws Exception {
        String xmlConfig = fillGenerate(generatorConfig);

        log.info("------begin--generator----");
        List<String> warnings = new ArrayList<String>();
        boolean overwrite = true;
        ConfigurationParser cp = new ConfigurationParser(warnings);
        ByteArrayInputStream bis = new ByteArrayInputStream(xmlConfig.getBytes("utf-8"));
        Configuration config = cp.parseConfiguration(bis);
        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);

        log.info("------end------");
    }

    private static String fillGenerate(String inputTemplateFilePath) {

        try {
            String fileContent = CommonUtil.readFileContent(inputTemplateFilePath);

            for (Entry<Object, Object> entry : properties.entrySet()) {
                String valur = entry.getValue().toString().trim();
                String key = entry.getKey().toString().trim();

                fileContent = fileContent.replaceAll("\\$\\{" + key + "\\}", valur);
            }

            String tableNames = properties.getProperty("tableNames");
            String[] tmps = tableNames.split(",");

            String tableModule = //
                    "		<table tableName=\"TABLE_NAME\" domainObjectName=\"OBJECT_MODULE_NAME\">\n" ///
                            + "			<property name=\"stateAction\" value=\"false\" />\n" //
                            + "			<ignoreColumn column=\"ID\" />\n" //
                            + "			<ignoreColumn column=\"GMT_CREATE\" />\n"//
                            + "			<ignoreColumn column=\"CREATOR\" />\n" //
                            + "			<ignoreColumn column=\"GMT_MODIFIED\" />\n"//
                            + "			<ignoreColumn column=\"MODIFIER\" />\n"//
                            + "			<ignoreColumn column=\"IS_DELETED\" />\n" //
                            + "		</table>";//

            StringBuilder sb = new StringBuilder();
            for (String string : tmps) {
                String tmpName = string.trim();

                String objectName = CommonUtil.convertDBField2Java(tmpName.substring(tmpName.indexOf("_") + 1));

                objectName = objectName.substring(0, 1).toUpperCase() + objectName.substring(1);

                String tableDesc = tableModule;
                tableDesc = tableDesc.replace("TABLE_NAME", tmpName);
                tableDesc = tableDesc.replace("OBJECT_MODULE_NAME", objectName);

                sb.append(tableDesc);
                sb.append("\n");
                sb.append("\n");
            }
            fileContent = fileContent.replaceAll("\\$\\{tables_inputs\\}", sb.toString());
            return fileContent;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

}
