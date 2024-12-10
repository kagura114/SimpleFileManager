package com.dazuoye.filemanager.utils;

public class FSHelper {
  static {
    System.loadLibrary("fshelper");
  }

  public static String getFolderSizeNativeMethod(String dir) {
    return getFolderSizeNative(dir);
  }
  public static long getFolderSizeBytesNativeMethod(String dir) {
    return getFolderSizeBytesNative(dir);
  }

  private static native String getFolderSizeNative(String dir);
  private static native long getFolderSizeBytesNative(String dir);
}
