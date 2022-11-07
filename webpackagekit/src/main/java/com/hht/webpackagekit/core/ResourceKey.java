package com.hht.webpackagekit.core;

import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

/**
 * 资源请求键
 */
public class ResourceKey {
    private final String host;
    private final String schema;
    private final List<String> pathList;

    public ResourceKey(String url) {
        Uri uri = Uri.parse(url);
        host = uri.getHost();
        schema = uri.getScheme();
        pathList = uri.getPathSegments();
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 37 + hashNotNull(host);
        result = result * 37 + hashNotNull(schema);
        if (pathList != null) {
            for (String pathSeg : pathList) {
                result = result * 37 + hashNotNull(pathSeg);
            }
        }
        return result;
    }

    private int hashNotNull(Object o) {
        return o == null ? 0 : o.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ResourceKey)) {
            return false;
        }
        ResourceKey that = (ResourceKey) obj;
        if (!TextUtils.equals(host, that.host)) {
            return false;
        }
        if (!TextUtils.equals(schema, that.schema)) {
            return false;
        }
        if (this.pathList == that.pathList) {
            return true;
        }
        if (pathList == null && that.pathList != null) {
            return false;
        }
        if (pathList != null && that.pathList == null) {
            return false;
        }
        boolean isEqual = true;
        for (String pa : pathList) {
            if (!that.pathList.contains(pa)) {
                isEqual = false;
                break;
            }
        }

        return isEqual;
    }
}
