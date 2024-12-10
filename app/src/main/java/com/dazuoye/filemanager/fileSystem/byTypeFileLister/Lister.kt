package com.dazuoye.filemanager.fileSystem.byTypeFileLister

import java.io.File

interface Lister {
  fun dateOrderedList(): List<String>

  fun sizeOrderedList(): List<String>

  fun getFullSize(): ULong

  fun walkDir(
    directory: File,
    list: MutableList<String>,
    pattern: Regex,
    ignoreDotFile: Boolean = true
  ) {
    if (!directory.exists() || !directory.isDirectory) {
      return
    }
    directory.listFiles()?.forEach {
      if (ignoreDotFile && !it.name.startsWith(".")) {
        if (it.isDirectory) {
          walkDir(it, list, pattern)
        } else if (it.isFile) {
          if (it.name.contains(pattern)) {
            list.add(it.path)
          }
        }
      }
    }
  }

}
