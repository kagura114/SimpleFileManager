package com.dazuoye.filemanager.compose

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

/**
 * PasteHelper 类提供了静态方法，用于复制文件和目录。
 */
class PasteHelper {
  companion object {

    /**
     * 复制目录及其所有内容到目标目录。
     * @param sourceDir 源目录
     * @param destDir 目标目录
     * 如果目标目录不存在，则创建它。
     * 如果源目录不存在或参数不是目录，则抛出异常。
     */
    fun copyDirectory(sourceDir: File, destDir: File) {
      // 如果目标目录不存在，则创建它
      if (!destDir.exists()) {
        destDir.mkdirs()
      }

      // 如果源目录不存在，则抛出异常
      require(sourceDir.exists()) { "sourceDir does not exist" }

      // 如果参数不是目录，则抛出异常
      require(!(sourceDir.isFile || destDir.isFile)) { "Either sourceDir or destDir is not a directory" }

      // 调用内部方法递归复制目录
      copyDirectoryImpl(sourceDir, destDir)
    }

    /**
     * 实现目录复制的内部方法。
     * @param sourceDir 源目录
     * @param destDir 目标目录
     * 遍历源目录中的所有文件和子目录，并递归复制它们。
     */
    private fun copyDirectoryImpl(sourceDir: File, destDir: File) {
      val items = sourceDir.listFiles()
      if (items != null && items.isNotEmpty()) {
        for (anItem: File in items) {
          if (anItem.isDirectory) {
            val newDir = File(destDir, anItem.name)
            newDir.mkdir() // 创建子目录
            // 递归复制目录
            copyDirectory(anItem, newDir)
          } else {
            // 复制单个文件
            val destFile = File(destDir, anItem.name)
            copySingleFile(anItem, destFile)
          }
        }
      }
    }

    /**
     * 复制单个文件到目标位置。
     * @param sourceFile 源文件
     * @param destFile 目标文件
     * 如果目标文件不存在，则创建它。
     */
    private fun copySingleFile(sourceFile: File, destFile: File) {
      if (!destFile.exists()) {
        destFile.createNewFile() // 创建目标文件
      }

      var sourceChannel: FileChannel? = null
      var destChannel: FileChannel? = null

      try {
        // 使用文件通道进行文件复制
        sourceChannel = FileInputStream(sourceFile).channel
        destChannel = FileOutputStream(destFile).channel
        sourceChannel.transferTo(0, sourceChannel.size(), destChannel)
      } finally {
        // 关闭文件通道，释放资源
        sourceChannel?.close()
        destChannel?.close()
      }
    }
  }
}
