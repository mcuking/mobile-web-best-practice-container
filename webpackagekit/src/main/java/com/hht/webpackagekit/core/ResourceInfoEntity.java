package com.hht.webpackagekit.core;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * index entity
 */
public class ResourceInfoEntity {
    private String version;
    private String packageId;

    @Expose(deserialize = false, serialize = false) private String md5;
    private List<ResourceInfo> items;

    public String getVersion() {
        return version;
    }

    public String getPackageId() {
        return packageId;
    }

    public List<ResourceInfo> getItems() {
        return items;
    }
}
