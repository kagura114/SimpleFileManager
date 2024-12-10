package com.dazuoye.filemanager.fileSystem.byTypeFileLister

import android.os.Environment
import com.dazuoye.filemanager.fileSystem.WrappedFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ImageLister private constructor() : Lister {
  companion object {
    val instance by lazy { ImageLister() }
    val directories = listOf("DCIM", "Pictures", "Download")
    val regex = "\\.(jpg|png|jpeg|webp)$".toRegex()
  }

  private val imageList = mutableListOf<String>()

  fun initialize(onFinished: (() -> Unit)? = null) {
    imageList.clear()
    CoroutineScope(Dispatchers.IO).launch {
      directories.forEach { imageDirectory ->
        walkDir(
          File("${Environment.getExternalStorageDirectory().path}/${imageDirectory}"),
          imageList,
          regex
        )
      }
      onFinished?.invoke()
    }
    return
  }

  override fun dateOrderedList(): List<String> {
    val wrappedFileList = mutableListOf<WrappedFile>()
    imageList.forEach { wrappedFileList.add(WrappedFile(File(it))) }
    wrappedFileList.sortBy { it.lastModifiedTime }
    val result = mutableListOf<String>()
    wrappedFileList.forEach { result.add(it.path) }
    return result
  }

  override fun sizeOrderedList(): List<String> {
    val wrappedFileList = mutableListOf<WrappedFile>()
    imageList.forEach { wrappedFileList.add(WrappedFile(File(it))) }
    wrappedFileList.sortBy { it.size }
    val result = mutableListOf<String>()
    wrappedFileList.forEach { result.add(it.path) }
    return result
  }

  override fun getFullSize(): ULong {
    var size = 0UL
    val wrappedFileList = mutableListOf<WrappedFile>()
    imageList.forEach { wrappedFileList.add(WrappedFile(File(it))) }
    wrappedFileList.forEach { size += it.size.toUInt() }
    return size
  }
}