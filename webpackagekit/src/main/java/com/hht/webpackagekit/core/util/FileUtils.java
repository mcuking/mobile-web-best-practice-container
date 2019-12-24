package com.hht.webpackagekit.core.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.hht.webpackagekit.core.Constants;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * file 工具类
 */
public class FileUtils {

    private static final String TAG = "FileUtils";

    /**
     * 根据fileName获取inputStream
     */
    public static InputStream getInputStream(String fileName) {

        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        File file = new File(fileName);
        if (!file.exists()) {
            return null;
        }
        if (file.isDirectory()) {
            return null;
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (Exception e) {

        }
        if (fileInputStream == null) {
            return null;
        }
        return new BufferedInputStream(fileInputStream);
    }

    /**
     * 解压zip到指定的路径
     *
     * @param zipFileString ZIP的名称
     * @param outPathString 要解压缩路径
     */
    public static boolean unZipFolder(String zipFileString, String outPathString) {
        ZipInputStream inZip = deleteOutUnZipFileIfNeed(zipFileString, outPathString);
        if (inZip == null) {
            return false;
        }
        boolean isSuccess = true;
        ZipEntry zipEntry = null;
        zipEntry = readZipNextZipEntry(inZip);
        if (zipEntry == null) {
            return false;
        }
        String szName;
        while (zipEntry != null) {
            szName = zipEntry.getName();
            /**
             * 不是package开头，认为是无效数据
             */
            if (!szName.startsWith(Constants.RESOURCE_MIDDLE_PATH)) {
                zipEntry = readZipNextZipEntry(inZip);
                continue;
            }
            if (zipEntry.isDirectory()) {
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPathString + File.separator + szName);
                isSuccess = folder.mkdirs();
                if (!isSuccess) {
                    break;
                }
            } else {
                File file = new File(outPathString + File.separator + szName);
                if (!file.exists()) {
                    isSuccess = makeUnZipFile(outPathString, szName);
                }
                if (!isSuccess) {
                    break;
                }
                isSuccess = writeUnZipFileToFile(inZip, file);
            }
            if (!isSuccess) {
                break;
            }
            zipEntry = readZipNextZipEntry(inZip);
        }
        isSuccess = safeCloseFile(inZip);
        return isSuccess;
    }

    private static ZipInputStream deleteOutUnZipFileIfNeed(String zipFileString, String outPathString) {
        boolean isSuccess = true;
        ZipInputStream inZip = null;
        try {
            inZip = new ZipInputStream(new FileInputStream(zipFileString));
        } catch (FileNotFoundException e) {
            isSuccess = false;
        }
        if (!isSuccess) {
            return null;
        }
        File outPath = new File(outPathString);
        if (outPath.exists()) {
            isSuccess = deleteDir(outPath);
        }
        if (!isSuccess) {
            return null;
        }
        return inZip;
    }

    private static boolean makeUnZipFile(String outPathString, String szName) {
        boolean isSuccess = true;
        File file = new File(outPathString + File.separator + szName);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            isSuccess = file.getParentFile().mkdirs();
        }
        if (!isSuccess) {
            return false;
        }
        try {
            isSuccess = file.createNewFile();
        } catch (IOException e) {
            isSuccess = false;
        }
        return isSuccess;
    }

    /**
     * 读取zip数据，如果抛出异常返回-2
     */
    private static int readZipFile(ZipInputStream inZip, byte[] buffer) {
        int len = -1;
        try {
            len = inZip.read(buffer);
        } catch (IOException e) {
            len = -2;
        }
        return len;
    }

    private static ZipEntry readZipNextZipEntry(ZipInputStream inZip) {
        ZipEntry zipEntry = null;
        boolean isSuccess = true;
        try {
            zipEntry = inZip.getNextEntry();
        } catch (IOException e) {
            isSuccess = false;
        }
        if (!isSuccess) {
            return null;
        }
        return zipEntry;
    }

    private static boolean writeUnZipFileToFile(ZipInputStream inZip, File file) {
        boolean isSuccess = true;
        // 获取文件的输出流
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            isSuccess = false;
        }
        int len = -1;
        byte[] buffer = new byte[1024];
        len = readZipFile(inZip, buffer);
        if (len == -1 || len == -2) {
            isSuccess = false;
        }
        if (!isSuccess) {
            return false;
        }
        // 读取（字节）字节到缓冲区
        while (len != -1) {
            // 从缓冲区（0）位置写入（字节）字节
            try {
                out.write(buffer, 0, len);
            } catch (IOException e) {
                isSuccess = false;
            }
            if (!isSuccess) {
                break;
            }
            try {
                out.flush();
            } catch (IOException e) {
                isSuccess = false;
            }
            if (!isSuccess) {
                break;
            }
            len = readZipFile(inZip, buffer);
            if (len == -2) {
                isSuccess = false;
                break;
            }
        }
        try {
            out.close();
        } catch (IOException e) {

        }
        return isSuccess;
    }

    /**
     * 获取缓存目录
     */
    public static File getFileDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;
        if (preferExternal && isExternalStorageMounted()) {
            appCacheDir = getExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            appCacheDir = context.getFilesDir();
        }
        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/file/";
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir;
    }

    public static File getResourceIndexFile(Context context, String packageId, String version) {
        String indexPath =
            getPackageWorkName(context, packageId, version) + File.separator + Constants.RESOURCE_MIDDLE_PATH
                + File.separator + Constants.RESOURCE_INDEX_NAME;
        return new File(indexPath);
    }

    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "file");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                return null;
            }
        }
        return appCacheDir;
    }

    public static boolean isExternalStorageMounted() {
        return Environment.MEDIA_MOUNTED.equalsIgnoreCase(getExternalStorageState());
    }

    public static String getExternalStorageState() {
        String externalStorageState;
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (NullPointerException e) { // (sh)it happens
            externalStorageState = "";
        }
        return externalStorageState;
    }

    /**
     * 获取根容器的地址
     */
    public static String getPackageRootPath(Context context) {
        File fileDir = getFileDirectory(context, false);
        if (fileDir == null) {
            return null;
        }

        String path = fileDir + File.separator + Constants.PACKAGE_FILE_ROOT_PATH;
        File file;
        if (!(file = new File(path)).exists()) {
            file.mkdirs();
        }
        return path;
    }

    /**
     * 获取根容器的地址
     */
    public static String getPackageLoadPath(Context context, String packageId) {
        String root = getPackageRootPath(context);
        if (TextUtils.isEmpty(root)) {
            return null;
        }
        return root + File.separator + packageId;
    }

    /***
     * 根据packageId获取work地址
     * */
    public static String getPackageWorkName(Context context, String packageId, String version) {
        String root = getPackageRootPath(context);
        if (TextUtils.isEmpty(root)) {
            return null;
        }
        return root + File.separator + packageId + File.separator + version + File.separator + Constants.PACKAGE_WORK;
    }

    /***
     * 根据packageId获取work地址
     * */
    public static String getPackageRootByPackageId(Context context, String packageId) {
        String root = getPackageRootPath(context);
        if (TextUtils.isEmpty(root)) {
            return null;
        }
        return root + File.separator + packageId;
    }

    //获取packageIndex.json地址
    public static String getPackageIndexFileName(Context context) {
        String root = getPackageRootPath(context);
        if (TextUtils.isEmpty(root)) {
            return null;
        }
        makeDir(root);
        return root + File.separator + Constants.PACKAGE_FILE_INDEX;
    }

    /***
     * 根据packageId获取update地址
     * */
    public static String getPackageUpdateName(Context context, String packageId, String version) {
        String root = getPackageRootPath(context);
        if (TextUtils.isEmpty(root)) {
            return null;
        }
        return root + File.separator + packageId + File.separator + version + File.separator + Constants.PACKAGE_UPDATE;
    }

    /***
     * 根据packageId获取下载目录文件
     * */
    public static String getPackageDownloadName(Context context, String packageId, String version) {
        String root = getPackageRootPath(context);
        if (TextUtils.isEmpty(root)) {
            return null;
        }
        return root + File.separator + packageId + File.separator + version + File.separator
            + Constants.PACKAGE_DOWNLOAD;
    }

    /***
     * 获取预置在assets文件下的离线包当前存储的路径
     * */
    public static String getPackageAssetsName(Context context, String packageId, String version) {
        String root = getPackageRootPath(context);
        if (TextUtils.isEmpty(root)) {
            return null;
        }
        return root + File.separator + "assets" + File.separator + packageId + File.separator + version + File.separator
            + Constants.PACKAGE_ASSETS;
    }

    /***
     * 根据packageId获取下载目录文件
     * */
    public static String getPackageMergePatch(Context context, String packageId, String version) {
        String root = getPackageRootPath(context);
        if (TextUtils.isEmpty(root)) {
            return null;
        }
        return root + File.separator + packageId + File.separator + version + File.separator
                + Constants.PACKAGE_MERGE;
    }

    /**
     * 复制单个文件
     *
     * @param srcFileName 待复制的文件名
     * @param descFileName 目标文件名
     * @return 如果复制成功，则返回true，否则返回false
     */
    public static boolean copyFileCover(String srcFileName, String descFileName) {
        File srcFile = new File(srcFileName);
        if (!srcFile.exists()) {
            return false;
        } else if (!srcFile.isFile()) {
            return false;
        }
        File descFile = new File(descFileName);
        if (descFile.exists()) {
            if (!FileUtils.delFile(descFileName)) {
                return false;
            }
        } else if (descFile.getParentFile() != null) {
            if (!descFile.getParentFile().exists()) {
                if (!descFile.getParentFile().mkdirs()) {
                    return false;
                }
            }
        } else {
            return false;
        }
        boolean isSuccess;
        try {
            isSuccess = copyFileByChannel(srcFile, descFile);
            return isSuccess;
        } catch (Exception e) {
            return false;
        } finally {
        }
    }

    /**
     * 删除文件，可以删除单个文件或文件夹
     *
     * @param fileName 被删除的文件名
     * @return 如果删除成功，则返回true，否是返回false
     */
    public static boolean delFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return true;
        } else {
            if (file.isFile()) {
                return FileUtils.deleteFile(fileName);
            }
        }
        return true;
    }

    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean makeDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return true;
    }

    /**
     * 复制某个目录及目录下的所有子目录和文件到新文件夹
     *
     * @param oldPath 源文件夹路径
     * @param newPath 目标文件夹路径
     */
    public static boolean copyFolder(String oldPath, String newPath) {
        boolean isSuccess = true;
        try {
            File newFile = new File(newPath);
            if (newFile.exists()) {
                isSuccess = deleteDir(newFile);
            }
            if (!isSuccess) {
                return false;
            }
            isSuccess = newFile.mkdirs();
            if (!isSuccess) {
                return false;
            }
            File fileList = new File(oldPath);
            String[] file = fileList.list();
            File tempFile;
            for (String itemFile : file) {
                // 如果oldPath以路径分隔符/或者\结尾，那么则oldPath/文件名就可以了
                // 否则要自己oldPath后面补个路径分隔符再加文件名
                if (oldPath.endsWith(File.separator)) {
                    tempFile = new File(oldPath + itemFile);
                } else {
                    tempFile = new File(oldPath + File.separator + itemFile);
                }

                if (tempFile.isFile()) {
                    isSuccess = copyFileByChannel(tempFile, new File(newPath + File.separator + tempFile.getName()));
                }
                if (!isSuccess) {
                    break;
                }
                if (tempFile.isDirectory()) {
                    isSuccess = copyFolder(oldPath + File.separator + itemFile, newPath + File.separator + itemFile);
                }
                if (!isSuccess) {
                    break;
                }
            }
        } catch (Exception e) {

        }
        return isSuccess;
    }

    public static boolean copyFileByChannel(File src, File dest) {
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;
        boolean isSuccess = true;
        try {
            fi = new FileInputStream(src);
            fo = new FileOutputStream(dest);
            in = fi.getChannel();
            out = fo.getChannel();
            in.transferTo(0, in.size(), out);
        } catch (IOException e) {
            isSuccess = false;
        } finally {
            try {
                fi.close();
                in.close();
                fo.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isSuccess;
    }

    /**
     * 删除某个目录及目录下的所有子目录和文件
     *
     * @param dir File path
     * @return boolean
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String aChildren : children) {
                    boolean isDelete = deleteDir(new File(dir, aChildren));
                    if (!isDelete) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }

    public static String getStringForZip(InputStream zipFileString) {
      boolean isSuccess = true;
      ZipInputStream inZip = null;
      try {
        inZip = new ZipInputStream(zipFileString);
      } catch (Exception e) {
        isSuccess = false;
      }
      if (!isSuccess) {
        return null;
      }
      ZipEntry zipEntry = null;
      zipEntry = readZipNextZipEntry(inZip);
      if (zipEntry == null) {
        safeCloseFile(inZip);
        return "";
      }
      String szName;
      String zipName = zipEntry.getName().split("\\/")[0];
      Log.d(TAG,"zipName>>>"+zipName);
      while (zipEntry != null) {
        szName = zipEntry.getName();
        Log.d(TAG, "RESOURCE_MIDDLE_PATH>>> " +szName);
        //index.json
        if (szName.equals(zipName + File.separator + Constants.RESOURCE_INDEX_NAME)) {
          break;
        }
        if (!isSuccess) {
          break;
        }
        zipEntry = readZipNextZipEntry(inZip);
      }
      if (zipEntry == null) {
        safeCloseFile(inZip);
        return "";
      }
      StringBuilder sb = new StringBuilder();
      int len = -1;
      byte[] buffer = new byte[2048];
      len = readZipFile(inZip, buffer);
      while (len != -1) {
        if (len == -2) {
          isSuccess = false;
          break;
        }
        sb.append(new String(buffer, 0, len));
        len = readZipFile(inZip, buffer);
      }
      isSuccess = safeCloseFile(inZip);
      if (isSuccess) {
        return sb.toString();
      }
      return "";
    }

    public static boolean safeCloseFile(Closeable file) {
        boolean isSuccess = true;
        try {
            file.close();
        } catch (IOException e) {
            isSuccess = false;
        }
        return isSuccess;
    }

    public static boolean copyFile(InputStream inStream, String newPath) {
        boolean isSuccess = true;
        try {

            int byteread = 0;
            File file = new File(newPath);
            if (file.exists()) {
                isSuccess = file.delete();
            }
            if (!isSuccess) {
                return false;
            }
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                isSuccess = file.getParentFile().mkdirs();
            }
            if (!isSuccess) {
                return false;
            }
            isSuccess = file.createNewFile();
            if (!isSuccess) {
                return false;
            }
            FileOutputStream fs = new FileOutputStream(file);
            byte[] buffer = new byte[1024 * 16];
            while ((byteread = inStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteread);
            }
            try {
                fs.flush();
            } catch (Exception e) {

            }
            safeCloseFile(inStream);
            safeCloseFile(fs);
        } catch (Exception e) {
            isSuccess = false;
        }
        return isSuccess;
    }
}
