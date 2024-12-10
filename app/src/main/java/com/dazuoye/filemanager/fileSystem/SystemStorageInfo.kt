package com.dazuoye.filemanager.fileSystem

import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Environment
import android.os.storage.StorageManager
import java.io.IOException

class SystemStorageInfo(context: Context) {
  private val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
  private val storageStatsManager =
    context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager

  private var totalStorage = 1uL
  private var freeStorage = 0uL

  fun getTotalStorageSize(): ULong {
    try {
      val uuid = storageManager.getUuidForPath(Environment.getDataDirectory())
      totalStorage = storageStatsManager.getTotalBytes(uuid).toULong()
      return totalStorage
    } catch (e: IOException) {
      return 1uL
    }
  }

  fun getFreeStorageSize(): ULong {
    try {
      val uuid = storageManager.getUuidForPath(Environment.getDataDirectory())
      freeStorage = storageStatsManager.getFreeBytes(uuid).toULong()
      return freeStorage
    } catch (e: IOException) {
      return 0uL
    }
  }

  fun getUsedPercentage(): Int =
    100 - Math.round(freeStorage.toDouble() * 100 / totalStorage.toDouble()).toInt()
}

