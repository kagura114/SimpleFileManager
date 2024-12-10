package com.dazuoye.filemanager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// 扩展 Context，定义一个 DataStore，用于存储应用设置
val Context.settingStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

/**
 * SettingStorage 类用于管理和访问应用的设置存储。
 * 它封装了 DataStore 访问逻辑，支持异步获取和设置偏好数据。
 */
class SettingStorage(private val context: Context) {
  val hideExtension = booleanPreferencesKey("hide_extension")
  val hideHiddenFile = booleanPreferencesKey("hide_hidden_file")

  /**
   * 获取给定偏好设置键的值。
   * @param key 偏好设置键
   * @return 偏好设置的值，如果未设置则返回 null
   */
  fun <T> get(key: Preferences.Key<T>): T? = runBlocking {
    // 使用 DataStore 获取偏好数据，并获取与给定键关联的值
    context.settingStore.data
      .map { value -> value[key] }
      .first() // 获取流的第一个值
  }

  /**
   * 设置给定偏好设置键的值。
   * @param key 偏好设置键
   * @param value 要存储的值
   */
  fun <T> set(key: Preferences.Key<T>, value: T) = CoroutineScope(Dispatchers.Default).launch {
    // 使用 DataStore 更新偏好数据
    context.settingStore.edit { it[key] = value }
  }
}
