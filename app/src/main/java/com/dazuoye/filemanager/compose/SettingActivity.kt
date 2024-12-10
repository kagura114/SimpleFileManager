package com.dazuoye.filemanager.compose

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dazuoye.filemanager.R
import com.dazuoye.filemanager.SettingStorage
import com.dazuoye.filemanager.compose.ui.Setting
import com.dazuoye.filemanager.main_page
import com.dazuoye.filemanager.utils.Sysinfo

class SettingActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val setting = Setting(
      rowModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 10.dp),
      nameTextStyle = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight(400),
        fontFamily = FontFamily.SansSerif
      ),
      descriptionModifier = Modifier.padding(start = 2.dp)
    )

    val settingStorage = SettingStorage(this)
    val context = this

    setContent {
      Surface(
        modifier = Modifier
          .fillMaxSize()
      ) {
        Column(
          modifier = Modifier
            .background(Color(getColor(R.color.WhiteSmoke)))
            .statusBarsPadding()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = {
                val intent = Intent(
                  context,
                  main_page::class.java
                )
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                context.startActivity(intent)
              }
            ) {
              Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_left_arrow), "back"
              )
            }

            Text(
              text = getString(R.string.settings),
              fontSize = 30.sp,
              modifier = Modifier
                .padding(start = 10.dp)
                .fillMaxWidth(0.75f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          setting.BooleanSetting(
            name = ContextCompat.getString(context, R.string.setting_hide_extension),
            description = ContextCompat.getString(
              context,
              R.string.setting_hide_extension_description
            ),
            initialState = settingStorage.get(settingStorage.hideExtension) ?: false
          ) { settingStorage.set(settingStorage.hideExtension, it) }

          setting.BooleanSetting(
            name = ContextCompat.getString(context, R.string.setting_hide_hidden_file),
            description = ContextCompat.getString(
              context,
              R.string.setting_hide_hidden_file_description
            ),
            initialState = settingStorage.get(settingStorage.hideHiddenFile) ?: false
          ) { settingStorage.set(settingStorage.hideHiddenFile, it) }

          Spacer(modifier = Modifier.weight(1f))

          Text(
            text = stringResource(R.string.about),
            fontSize = 18.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 10.dp)
          )
          setting.BooleanSetting(
            name = "作者",
            description = "",
            initialState = null
          ) { }
          setting.BooleanSetting(
            name = "系统信息",
            description = "Android 版本：${Build.VERSION.RELEASE}\n手机型号: ${Build.MANUFACTURER} ${Build.MODEL}",
            initialState = null
          ) { }
          setting.BooleanSetting(
            name = "高级系统信息",
            description = Sysinfo.getSystem(),
            initialState = null
          ) { }

          Spacer(modifier = Modifier.navigationBarsPadding())
        }
      }
    }
  }
}

