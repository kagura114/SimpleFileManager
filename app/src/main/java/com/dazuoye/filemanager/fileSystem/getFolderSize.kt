package com.dazuoye.filemanager.fileSystem

import java.io.File

fun getFolderSize(folder: File, level: Int = 0): Long {
  if (!folder.exists() || !folder.isDirectory) {
    return 0
  }

  if (level >= 5) {    // 限制递归层数防止过度递归
    return 1.shl(63)
  }

  var size = 0L

  folder.listFiles()
  folder.listFiles()?.forEach { content ->
    size += if (content.isFile) {
      content.length()
    } else if (content.isDirectory) {
      getFolderSize(content, level + 1).let {
        if (size > 0 || it > 0) {
          it
        } else {
          -it
        }
      }
    } else {
      0
    }
  }
  return size
}
