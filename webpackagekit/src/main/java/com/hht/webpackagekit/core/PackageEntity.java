package com.hht.webpackagekit.core;

import java.util.List;

/**
 * 离线包Index信息
 */
public class PackageEntity {
  private int errorCode;
  private List<PackageInfo> data;

  public void setItems(List<PackageInfo> data) {
    this.data = data;
  }

  public List<PackageInfo> getItems() {
    return data;
  }
}
