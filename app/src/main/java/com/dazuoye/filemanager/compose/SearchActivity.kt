package com.dazuoye.filemanager.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dazuoye.filemanager.R
import com.dazuoye.filemanager.compose.ui.SearchFileColumn
import com.dazuoye.filemanager.fileSystem.byTypeFileLister.DocumentLister
import com.dazuoye.filemanager.fileSystem.byTypeFileLister.ImageLister
import com.dazuoye.filemanager.fileSystem.byTypeFileLister.MusicLister
import com.dazuoye.filemanager.fileSystem.byTypeFileLister.VideoLister

class SearchActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val type = intent.extras?.getString("type")
    val searchTypeRegex: Regex? = when (type) {
      "music" -> MusicLister.regex
      "image" -> ImageLister.regex
      "video" -> VideoLister.regex
      "document" -> DocumentLister.regex
      else -> null
    }
    val searchFileColumn = SearchFileColumn(this,when (type) {
      "music" -> getString(R.string.music)
      "image" -> getString(R.string.picture)
      "video" -> getString(R.string.video)
      "document" -> getString(R.string.document)
      else -> ""
    },searchTypeRegex)

    enableEdgeToEdge()
    setContent {
      Surface(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(getColor(R.color.WhiteSmoke)))
      )
      { searchFileColumn.Draw() }
    }
  }
}