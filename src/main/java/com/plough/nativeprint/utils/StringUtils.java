package com.plough.nativeprint.utils;

/**
 * Created by plough on 2019/2/25.
 */
public class StringUtils {
    public static final String EMPTY = "";

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    public static boolean isBlank(String s) {
        if (s == null) {
            return true;
        }
        int length = s.length();
        return length == 0 || isBlank(s, length);
    }

    private static boolean isBlank(String s, int length) {
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}
