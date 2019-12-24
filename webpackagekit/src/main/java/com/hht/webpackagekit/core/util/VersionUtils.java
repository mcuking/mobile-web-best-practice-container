package com.hht.webpackagekit.core.util;

/**
 * 版本工具
 */
public class VersionUtils {

    public static int compareVersion(String version1, String version2) {

        if (version1 == null || version2 == null) {
            return 0;
        }
        //注意此处为正则匹配，不能用"."；
        String[] versionArray1 = version1.split("\\.");
        //如果位数只有一位则自动补零（防止出现一个是04，一个是5 直接以长度比较）
        for (int i = 0; i < versionArray1.length; i++) {
            if (versionArray1[i].length() == 1) {
                versionArray1[i] = "0" + versionArray1[i];
            }
        }
        String[] versionArray2 = version2.split("\\.");
        //如果位数只有一位则自动补零
        for (int i = 0; i < versionArray2.length; i++) {
            if (versionArray2[i].length() == 1) {
                versionArray2[i] = "0" + versionArray2[i];
            }
        }
        int idx = 0;
        //取最小长度值
        int minLength = Math.min(versionArray1.length, versionArray2.length);
        int diff = 0;
        //先比较长度再比较字符
        while (idx < minLength && (diff = versionArray1[idx].length() - versionArray2[idx].length()) == 0
            && (diff = versionArray1[idx].compareTo(versionArray2[idx])) == 0) {
            ++idx;
        }
        //如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大；
        diff = (diff != 0) ? diff : versionArray1.length - versionArray2.length;
        return diff;
    }
}
