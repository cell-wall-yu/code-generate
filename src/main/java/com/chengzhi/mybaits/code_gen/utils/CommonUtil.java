package com.chengzhi.mybaits.code_gen.utils;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommonUtil {
    public static String convertDBField2Jave(String input) {
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
}
