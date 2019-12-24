package com.hht.webpackagekit;

import com.hht.webpackagekit.core.PackageInfo;

/**
 * 校验资源信息的有效性
 */
public interface PackageValidator {
    boolean validate(PackageInfo info);
}
