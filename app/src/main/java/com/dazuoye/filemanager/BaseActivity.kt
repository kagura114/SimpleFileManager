package com.dazuoye.filemanager

import androidx.appcompat.app.AppCompatActivity
import com.dazuoye.filemanager.fileSystem.DeleteHelper.Companion.delete
import java.io.File

/**
 * BaseActivity 继承自 AppCompatActivity，提供了一些基础功能。
 * 主要功能是在销毁 Activity 时清理缓存和释放系统资源。
 */
open class BaseActivity : AppCompatActivity() {

  /**
   * 在 Activity 销毁时调用的方法。
   * 此方法用于删除缓存中的 "clipboard" 文件并触发垃圾回收。
   */
  override fun onDestroy() {
    super.onDestroy()

    // 创建一个指向缓存目录中 "clipboard" 文件的引用
    val clipFile = File(this.cacheDir, "clipboard")

    // 如果 "clipboard" 文件存在，调用 delete 方法删除文件
    if (clipFile.exists()) {
      delete(clipFile.path)
    }

    // 显式调用垃圾回收器，提示系统进行内存回收
    System.gc()
  }
}
