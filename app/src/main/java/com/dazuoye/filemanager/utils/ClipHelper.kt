package com.dazuoye.filemanager.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.dazuoye.filemanager.BuildConfig
import com.dazuoye.filemanager.fileSystem.DeleteHelper.Companion.delete
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.InputStream
import java.nio.charset.CharsetDecoder

class ClipHelper private constructor(context: Context) {
  companion object {
    private const val label = "${BuildConfig.APPLICATION_ID}\$ClipHelper"
    private const val ENCODE_LABEL = "ClipHelper"

    private var instance: ClipHelper? = null

    @OptIn(InternalCoroutinesApi::class)
    fun getInstance(context: Context): ClipHelper =
      instance ?: synchronized(this) {
        instance ?: ClipHelper(context).also { instance = it }
      }
  }

  private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  private val contentResolver: ContentResolver = context.contentResolver

  fun copy(file: File, context: Context) {
    val uri = FileProvider.getUriForFile(
      context,
      context.applicationContext.packageName + ".provider",
      file
    )
    val clip = ClipData.newUri(contentResolver, label, uri)
    clipboard.setPrimaryClip(clip)

    // 备用的复制方法
    val clipFile = File(context.cacheDir,"clipboard")
    if (clipFile.exists()){
      delete(clipFile.path)
    }

    clipFile.createNewFile()
    val fw = FileWriter(clipFile)
    fw.write(uri.toString())
    fw.close()
  }

  fun paste(context: Context): Uri? {
    val clip = clipboard.primaryClip
    clip?.run {
      val item: ClipData.Item = getItemAt(0)
      if (item.uri == null){
        // 备用粘贴方式
        val clipFile = File(context.cacheDir,"clipboard")
        if (clipFile.isFile()){
          val input = FileInputStream(clipFile)
          val content = IOUtils.toString(input,"UTF-8")
          return Uri.parse(content)
        }
      }

      return item.uri
    }
    return null
  }

  fun copyFolder(folder: String) {
    val clip = ClipData.newPlainText("SingleFolderCopy", "$ENCODE_LABEL:${folder}")
    clipboard.setPrimaryClip(clip)
  }

  fun pasteFolder(): String? {
    val clip = clipboard.primaryClip
    val content = clip?.run {
      val item: ClipData.Item = getItemAt(0)
      item.text
    }
    if (content != null) {
      if (content.startsWith(ENCODE_LABEL)) {
        return content.split(':').last()
      }
    }
    return null
  }
}