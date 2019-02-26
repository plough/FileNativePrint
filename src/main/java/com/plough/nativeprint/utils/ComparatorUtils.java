package com.plough.nativeprint.utils;

/**
 * Created by plough on 2019/2/25.
 */
public class ComparatorUtils {
    public static boolean equals(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        }
        return s1.equals(s2);
    }
}
