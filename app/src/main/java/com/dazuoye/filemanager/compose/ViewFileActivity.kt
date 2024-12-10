package com.dazuoye.filemanager.compose

import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dazuoye.filemanager.R
import com.dazuoye.filemanager.compose.ui.FileColumn
import java.io.File

class ViewFileActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    var path =
      intent.extras?.getString("folder") ?: Environment.getExternalStorageDirectory().path
    val file = File(path)
    if (!file.isDirectory) {
      path = Environment.getExternalStorageDirectory().path
    }
    enableEdgeToEdge()
    window.statusBarColor = getColor(R.color.WhiteSmoke)
    setContent {
      Surface(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(getColor(R.color.WhiteSmoke)))
      ) {
        FileColumn(this).Draw(path)
      }
    }
  }
}

