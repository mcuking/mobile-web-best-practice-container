package com.hht.lib.bsdiff;

public class PatchUtils {

    static PatchUtils instance;

    public static PatchUtils getInstance() {
        if (instance == null)
            instance = new PatchUtils();
        return instance;
    }

    static {
        System.loadLibrary("ApkPatchLibrary");
    }

    /**
     * native方法 使用路径为oldPath的文件与路径为patchPath的补丁包，合成新的文件，并存储于newPath
     * 返回：0，说明操作成功
     *
     * @param oldPath   示例:/sdcard/old.apk
     * @param newPath   示例:/sdcard/new.apk
     * @param patchPath 示例:/sdcard/xx.patch
     */
    public native int patch(String oldPath, String newPath, String patchPath);
}