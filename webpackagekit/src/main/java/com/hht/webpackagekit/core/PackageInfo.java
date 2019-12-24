package com.hht.webpackagekit.core;

import android.text.TextUtils;

/**
 * 离线包信息
 */
public class PackageInfo {
  //实际的md5
  private String md5;

  private String module_name;

  //离线包版本号
  private String version = "1";

  //离线包的状态 {@link PackageStatus}
  private int status = PackageStatus.onLine;

  //是否是patch包
  private boolean is_patch;


  // 实际上的下载地址
  private String file_path;



  //离线包md值 由后端下发
  private String patch_file_md5;
  private String origin_file_md5;
  private String patch_file_path;
  private String origin_file_path;


  public void setPatch_file_path(String patch_file_path) {
    this.patch_file_path = patch_file_path;
  }

  public void setOrigin_file_path(String origin_file_path) {
    this.origin_file_path = origin_file_path;
  }

  public String getPackageId() {
    return module_name;
  }

  public String getOrigin_file_md5() {
    return origin_file_md5;
  }

  public String getPatch_file_md5() {
    return patch_file_md5;
  }

  public String getOrigin_file_path() {
    return origin_file_path;
  }

  public String getPatch_file_path() {
    return patch_file_path;
  }

  //设置离线包下载地址
  public void setDownloadUrl(String file_path) {
    this.file_path = file_path;
  }

  //获取离线包下载地址
  public String getDownloadUrl() {
    return file_path;
  }

  public String getVersion() {
    return version;
  }

  public int getStatus() {
    return status;
  }

  public void setIsPatch(boolean isPatch){
    this.is_patch = isPatch;
  }

  public boolean isPatch() {
    return is_patch;
  }


  public void setMd5(String md5) {
    this.md5 = md5;
  }


  public String getMd5() {
    return md5;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setPackageId(String module_name) {
    this.module_name = module_name;
  }

  public void setStatus(int status) {
    this.status = status;
  }

//    public void setMd5(String md5) {
//        this.md5 = md5;
//    }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PackageInfo)) {
      return false;
    }
    PackageInfo that = (PackageInfo) obj;
    return TextUtils.equals(module_name, that.module_name);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = result * 37 + module_name == null ? 0 : module_name.hashCode();
    return result;
  }
}
