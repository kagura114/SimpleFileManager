package com.dazuoye.filemanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.dazuoye.filemanager.fileSystem.SystemStorageInfo
import com.dazuoye.filemanager.fileSystem.WrappedFile
import com.dazuoye.filemanager.fileSystem.byTypeFileLister.DocumentLister
import com.dazuoye.filemanager.fileSystem.byTypeFileLister.ImageLister
import com.dazuoye.filemanager.fileSystem.byTypeFileLister.MusicLister
import com.dazuoye.filemanager.fileSystem.byTypeFileLister.VideoLister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class store_page : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    this.enableEdgeToEdge()
    setContentView(R.layout.store_page)
    val leftArrowImageView = findViewById<ImageView>(R.id.leftArrowImageView)
    leftArrowImageView.setOnClickListener { v: View? ->
      val intent = Intent(
        this@store_page,
        main_page::class.java
      )
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
      startActivity(intent)
      finish()
    }

    val systemStorageInfo = SystemStorageInfo(this)
    val tv = findViewById<TextView>(R.id.storageText)
    tv.text = getString(
      R.string.used_storage,
      WrappedFile.getSizeString(systemStorageInfo.getTotalStorageSize() - systemStorageInfo.getFreeStorageSize()),
      WrappedFile.getSizeString(systemStorageInfo.getTotalStorageSize())
    )
    (findViewById<View>(R.id.progressText) as TextView).text =
      getString(
        R.string.used_storage_percentage,
        systemStorageInfo.getUsedPercentage()
      )
    (findViewById<View>(R.id.progressBar) as ProgressBar).progress =
      systemStorageInfo.getUsedPercentage()

    CoroutineScope(Dispatchers.Main).launch {
      val imageSize = ImageLister.instance.getFullSize()
      val imageSizeString = WrappedFile.getSizeString(imageSize)
      val videoSize = VideoLister.instance.getFullSize()
      val videoSizeString = WrappedFile.getSizeString(videoSize)
      val musicSize = MusicLister.instance.getFullSize()
      val musicSizeString = WrappedFile.getSizeString(musicSize)
      val documentSize = DocumentLister.instance.getFullSize()
      val documentSizeString = WrappedFile.getSizeString(documentSize)
      val otherSize =
        systemStorageInfo.getTotalStorageSize() - systemStorageInfo.getFreeStorageSize() - imageSize - musicSize - documentSize
      val otherSizeString = WrappedFile.getSizeString(otherSize)
      runOnUiThread {
        findViewById<TextView>(R.id.pictureStorage).text = imageSizeString
        findViewById<TextView>(R.id.videoStorage).text = videoSizeString
        findViewById<TextView>(R.id.audioStorage).text = musicSizeString
        findViewById<TextView>(R.id.documentStorage).text = documentSizeString
        findViewById<TextView>(R.id.appStorage).text = otherSizeString
      }
    }
  }
}