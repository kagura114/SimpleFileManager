package com.dazuoye.filemanager.compose

import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.dazuoye.filemanager.BuildConfig
import com.dazuoye.filemanager.R
import com.dazuoye.filemanager.fileSystem.byTypeFileLister.DocumentLister
import com.dazuoye.filemanager.fileSystem.byTypeFileLister.ImageLister
import com.dazuoye.filemanager.fileSystem.byTypeFileLister.MusicLister
import com.dazuoye.filemanager.fileSystem.byTypeFileLister.VideoLister
import com.dazuoye.filemanager.main_page

/**
 * RequirePermissionActivity 继承自 ComponentActivity，用于请求用户的存储权限。
 * 如果权限已经被授予，应用将导航到主页面并初始化系统。
 */
class RequirePermissionActivity : ComponentActivity() {

  /**
   * 在 Activity 创建时调用的方法。
   * 该方法启用全屏模式，设置状态栏颜色，并构建一个界面来提示用户授予存储权限。
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge() // 启用全屏边到边显示
    val activity = this
    window.statusBarColor = getColor(R.color.WhiteSmoke) // 设置状态栏颜色

    // 使用 Jetpack Compose 构建 UI
    setContent {
      Column(
        modifier = Modifier
          .statusBarsPadding() // 为状态栏留出空间
          .fillMaxHeight(0.9f) // 设置列高度为 90%
          .fillMaxWidth(), // 填满宽度
        horizontalAlignment = Alignment.CenterHorizontally, // 水平居中
        verticalArrangement = Arrangement.Center // 垂直居中
      ) {
        // 显示提示文本，根据系统版本显示不同的提示信息
        Text(
          text = getString(
            if (VERSION.SDK_INT >= VERSION_CODES.R) {
              R.string.require_manage_storage // Android R 及以上需要的权限
            } else {
              R.string.require_permission_readwrite // 较低版本需要的权限
            }
          ),
          modifier = Modifier.padding(vertical = 10.dp), // 设置垂直内边距
          fontSize = 30.sp // 设置字体大小
        )

        // 按钮，用于请求用户授予存储权限
        Button(
          onClick = { // 按钮点击事件
            if (VERSION.SDK_INT >= VERSION_CODES.R) {
              // Android R 及以上版本
              if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                intent.setData(uri)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // 清空栈顶
                startActivity(intent)
              }
            } else {
              // 针对较旧版本的系统请求权限
              val permissions = arrayOf(
                permission.READ_EXTERNAL_STORAGE,
                permission.WRITE_EXTERNAL_STORAGE
              )
              ActivityCompat.requestPermissions(
                activity, permissions, 100
              )
            }
          },
          colors = ButtonColors(
            containerColor = Color(0xFF039BE5), // 按钮背景颜色
            contentColor = Color.White, // 按钮文本颜色
            disabledContainerColor = Color.Gray, // 按钮禁用状态背景颜色
            disabledContentColor = Color.White // 按钮禁用状态文本颜色
          )
        ) {
          // 按钮文本
          Text(text = getString(R.string.give_permission))
        }
      }
    }
  }

  /**
   * 在 Activity 恢复时调用的方法。
   * 检查是否已经授予权限，如果是，则导航到主页面并初始化系统。
   */
  override fun onResume() {
    super.onResume()
    if (checkPermissions(this)) {
      val intent = Intent(this, main_page::class.java)
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // 清空栈顶
      startActivity(intent)
      initSystem() // 初始化系统
      finish()
    }
  }
}

/**
 * 检查应用是否已获得所需的存储权限。
 * @param context 上下文，用于访问权限检查方法
 * @return Boolean 如果权限已被授予，返回 true；否则返回 false
 */
fun checkPermissions(context: Context): Boolean {
  // 对于 Android R 及以上版本，检查是否具有管理所有文件的权限
  if (VERSION.SDK_INT >= VERSION_CODES.R) {
    if (!Environment.isExternalStorageManager()) {
      return false
    }
  } else {
    // 对于较低版本，逐个检查读写权限
    val permissions = arrayOf(
      permission.READ_EXTERNAL_STORAGE,
      permission.WRITE_EXTERNAL_STORAGE
    )
    permissions.forEach {
      if (context.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED) {
        return false
      }
    }
  }
  return true
}

/**
 * 初始化系统的资源列表，如图像、视频、音乐和文档。
 */
fun initSystem() {
  ImageLister.instance.initialize() // 初始化图像列表
  VideoLister.instance.initialize() // 初始化视频列表
  MusicLister.instance.initialize() // 初始化音乐列表
  DocumentLister.instance.initialize() // 初始化文档列表
}
