package com.hht.webpackagekit.core;

/**
 * 资源验证器
 * 在webview加载资源前，给使用者一次验证资源 有效性的机会
 */
public interface ResoureceValidator {
    boolean validate(ResourceInfo resourceInfo);
}
