package com.dazuoye.filemanager.fileSystem

import android.content.Context
import android.os.Environment
import com.dazuoye.filemanager.compose.PasteHelper
import com.dazuoye.filemanager.utils.ClipHelper
import java.io.File

class DeleteHelper {
  companion object {
    fun delete(path: String) {
      val file = File(path)
      if (file.isFile) {
        file.delete()
      } else if (file.isDirectory) {
        file.deleteRecursively()
      }
    }
  }
}

class CutHelper {
  companion object {
    fun cut(context: Context, file: File) {
      val cacheDir = File("${Environment.getExternalStorageDirectory()}/.copy")
      if (!cacheDir.exists()) {
        cacheDir.mkdir()
      }

      if (file.exists()) {
        val tempFile = File("${Environment.getExternalStorageDirectory()}/.copy", file.name)
        val bytes = file.readBytes()
        tempFile.writeBytes(bytes)
        ClipHelper.getInstance(context).copy(tempFile, context)
        DeleteHelper.delete(file.path)
      }
    }

    fun cutFolder(context: Context, folder: File) {
      val cacheDir = File("${Environment.getExternalStorageDirectory()}/.copy")
      if (!cacheDir.exists()) {
        cacheDir.mkdir()
      }

      if (folder.exists()) {
        val tempFolder = File("${Environment.getExternalStorageDirectory()}/.copy", folder.name)
        PasteHelper.copyDirectory(folder, tempFolder)
        ClipHelper.getInstance(context).copyFolder(tempFolder.path)
        DeleteHelper.delete(folder.path)
      }
    }
  }
}