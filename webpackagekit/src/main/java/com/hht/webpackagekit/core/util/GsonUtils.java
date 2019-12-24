package com.hht.webpackagekit.core.util;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * gson 工具
 */
public class GsonUtils {

    private static Gson gson = new Gson();

    /**
     * 在格式错误时不抛异常, 返回null, 为了处理服务器500+的情况, 会返回一个普通字符串
     */
    public static <T> T fromJsonIgnoreException(String json, Class<T> classOfT) {
        try {
            return gson.fromJson(json, classOfT);
        } catch (Throwable ignore) {
            return null;
        }
    }

    /**
     * 在格式错误时不抛异常, 返回null, 为了处理服务器500+的情况, 会返回一个普通字符串
     */
    public static <T> T fromJsonIgnoreException(InputStream json, Class<T> classOfT) {
        Reader reader = null;
        BufferedReader bufferedReader = null;
        T entity = null;
        try {
            reader = new InputStreamReader(json);
            bufferedReader = new BufferedReader(reader);
            entity = gson.fromJson(bufferedReader, classOfT);
        } catch (Throwable ignore) {

        } finally {

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                }
            }
        }
        return entity;
    }
}
