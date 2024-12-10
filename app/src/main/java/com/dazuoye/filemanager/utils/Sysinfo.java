package com.dazuoye.filemanager.utils;

public class Sysinfo {
  static {
    System.loadLibrary("sysinfo");
  }

  public static String getSystem(){
    try {
      return getSystemNative();
    } catch (Exception e){
      return "Failed to obtain";
    }
  }

  private static native String getSystemNative();
}
