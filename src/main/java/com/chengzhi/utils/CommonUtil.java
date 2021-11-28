package com.chengzhi.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class CommonUtil {
    private static Logger log = LoggerFactory.getLogger(CommonUtil.class);
    public static String replaceAll(String input, String oldChars, String newChars) {

        if (input == null) {
            return input;
        }
        if (oldChars == null || oldChars.length() == 0) {
            return input;
        }

        if (newChars == null) {
            return input;
        }

        StringBuilder sb = new StringBuilder();

        int start = 0;
        int tmpIndex = 0;

        int len = oldChars.length();

        for (; ; ) {

            tmpIndex = input.indexOf(oldChars, start);

            if (tmpIndex != -1) {

                if (tmpIndex > 0) {
                    if (tmpIndex > start) {
                        sb.append(input.substring(start, tmpIndex));
                    }
                    sb.append(newChars);
                } else {
                    sb.append(newChars);
                }

                start = tmpIndex + len;
            } else {

                if (start == 0) {
                    return input;
                } else {
                    sb.append(input.substring(start, input.length()));
                }

                break;
            }

        }

        return sb.toString();
    }

    public static String toJson(Object obj) {
        if (null == obj) {
            return null;
        }
        if ("" == obj) {
            return "";
        }
        return JSONObject.toJSONString(obj);
    }

    public static String getIpAddr(HttpServletRequest request) {
        String ip = null;
        try {

            ip = request.getHeader("Proxy-Nodeway-Ip");
            if (ip != null && ip.length() != 0 && !("unknown".equalsIgnoreCase(ip)) && !("127.0.0.1".equalsIgnoreCase(ip))) {
                return ip;
            }

            ip = request.getHeader("X-Real-IP");
            if (ip != null && ip.length() != 0 && !("unknown".equalsIgnoreCase(ip)) && !("127.0.0.1".equalsIgnoreCase(ip))) {
                return ip;
            }

            ip = request.getRemoteHost();

            return ip;
        } catch (Exception e) {
            log.warn("没有获取到ip");
            return null;
        }
    }

    private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<String, ThreadLocal<SimpleDateFormat>>();

    public static String dateFormat(String date, String pattern) {
        SimpleDateFormat sdf = getSdf(pattern);
        return sdf.format(date);
    }

    public static SimpleDateFormat getSdf(final String pattern) {
        ThreadLocal<SimpleDateFormat> tl = sdfMap.get(pattern);

        // 此处的双重判断和同步是为了防止sdfMap这个单例被多次put重复的sdf
        if (tl == null) {
            synchronized (log) {
                tl = sdfMap.get(pattern);
                if (tl == null) {
                    tl = new ThreadLocal<SimpleDateFormat>() {
                        protected SimpleDateFormat initialValue() {
                            return new SimpleDateFormat(pattern);
                        }
                    };
                    sdfMap.put(pattern, tl);
                }
            }
        }

        return tl.get();
    }

    public static String convertDBField2Java(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean needUppper = false;
        for (char c : input.toCharArray()) {
            if (c == '_') {
                needUppper = true;
                continue;
            } else {
                if (needUppper) {
                    int asscii = (int) c;
                    if (asscii >= 97 && asscii <= 122) {
                        sb.append((char) (c - 32));
                    } else {
                        sb.append(c);
                    }
                } else {
                    sb.append(c);
                }
                needUppper = false;
            }

        }
        return sb.toString();
    }

    public static String readFileContent(String path) {
        String content = null;
        try {
            content = IOUtils.toString(new BufferedReader(new InputStreamReader(new FileInputStream(path), "utf-8")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
    /**
     * 将java属性转换成对应数据库字段形式,如 inputName > input_name , lastLoginTime > last_login_time
     *
     * @return
     */
    public static String convertJavaField2DB(String input) {
        if (input == null) {
            return null;
        }
        for (char c : input.toCharArray()) {
            int asscii = (int) c;
            if (asscii >= 65 && asscii <= 90) {
                input = input.replace(String.valueOf(c), "_" + (char) (asscii + 32));
            }
        }
        return input;
    }
}
