package com.pk.mappergenerator.util;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {

    public static boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }

    public static String getFildName(String columnName) {
        // 只有在有下划线的时候才处理为驼峰式
        if (columnName.contains(Const.UNDER_LINE)) {
            String[] items = columnName.split(Const.UNDER_LINE);
            StringBuilder builder = new StringBuilder();
            builder.append(items[0]);
            for (int i = 1; i < items.length; i++) {
                // 先全部转换为小写，再将下划线后一位字符转换为大写
                items[i] = items[i].toLowerCase();
                builder.append(StringUtils.capitalize(items[i]));
            }
            return builder.toString();
        }
        return columnName;
    }

    public static void deleteLastStr(StringBuilder sb, int count) {
        int length = sb.length();
        if (length >= count) {
            sb.delete(length - count, length);
        } else {
            sb.delete(0, length);
        }
    }

    public static String replace(String str, String newKey, String value) {
        return StringUtils.replace(str, newKey, value);
    }
}
