package com.chengzhi.mybaits.code_gen.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author ycz
 * @version 1.0.0
 * @date 2021/6/10 0010 下午 5:13
 * json工具类
 */
public class JsonUtil {

    private static Logger log = LoggerFactory.getLogger(JsonUtil.class);


    /**
     * 将json串转换成 对象
     *
     * @param json
     * @param classOfT
     * @return
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        try {
            if (json == null || json.matches("\\s*")) {
                return null;
            }

            return JSONObject.toJavaObject(JSONObject.parseObject(json), classOfT);
        } catch (Exception e) {
            log.error("json解析出错：" + e.getMessage(), e);
            return null;
        }
    }


    /**
     * 将对象转化为字符串
     *
     * @param obj
     * @return
     */
    public static String toString(Object obj) {
        if (ObjectUtils.isEmpty(obj)) {
            return null;
        }
        return JSONObject.toJSONString(obj);
    }


}
