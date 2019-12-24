package com.hht.webpackagekit.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * mimeType工具类
 */
public class MimeTypeUtils {
    private static List<String> supportMineTypeList = new ArrayList<>(2);

    static {
        supportMineTypeList.add("application/x-javascript");
        supportMineTypeList.add("image/jpeg");
        supportMineTypeList.add("image/tiff");
        supportMineTypeList.add("text/css");
        supportMineTypeList.add("image/gif");
        supportMineTypeList.add("image/png");
        supportMineTypeList.add("application/javascript");
    }

    public static boolean checkIsSupportMimeType(String mimeType) {
        return supportMineTypeList.contains(mimeType);
    }
}
