package com.dazuoye.filemanager

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import com.dazuoye.filemanager.fileSystem.adapters.VideoAdapter
import com.dazuoye.filemanager.fileSystem.adapters.VideoModel
import com.dazuoye.filemanager.compose.SearchActivity
import com.dazuoye.filemanager.fileSystem.CutHelper
import com.dazuoye.filemanager.fileSystem.byTypeFileLister.DocumentLister
import com.dazuoye.filemanager.fileSystem.byTypeFileLister.VideoLister
import com.dazuoye.filemanager.fileSystem.byTypeFileLister.VideoLister.Companion.instance
import com.dazuoye.filemanager.utils.AlertHelper
import com.dazuoye.filemanager.utils.ClipHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import java.io.File

class video_page : BaseActivity() {
  private var videoList = listOf<String>()
  private val pasteDir = "${Environment.getExternalStorageDirectory().path}/Movies/pasted"
  private var videoListOrderType = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    this.enableEdgeToEdge()
    setContentView(R.layout.video_page)
    ViewCompat.setOnApplyWindowInsetsListener(
      findViewById(R.id.main)
    ) { v: View, insets: WindowInsetsCompat ->
      val systemBars =
        insets.getInsets(Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    // 设置排序按钮的点击事件
    val sortImageView = findViewById<ImageView>(R.id.sortVideoView)
    sortImageView.setOnClickListener { v: View? -> showSortOptions() }

    // 设置左箭头的点击事件，返回上一级页面
    val leftArrowImageView = findViewById<ImageView>(R.id.leftArrowImageView)
    leftArrowImageView.setOnClickListener { v: View? ->
      val intent =
        Intent(
          this@video_page,
          main_page::class.java
        )
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
      startActivity(intent)
      finish()
    }

    val searchImageView = findViewById<ImageView>(R.id.searchVideoView)
    searchImageView.setOnClickListener { v: View? ->
      val intent =
        Intent(
          this,
          SearchActivity::class.java
        )
      val bundle = Bundle()
      bundle.putString("type", "video")
      intent.putExtras(bundle)
      startActivity(intent) // 跳转到搜索页面
    }

    findViewById<ImageView>(R.id.refreshData).setOnClickListener { _ ->
      update()
    }

    val videoGrid: GridView = findViewById(R.id.VideoGrid)

    videoGrid.onItemClickListener =
      AdapterView.OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
        val file = File(videoList[position])
        if (file.isFile) {
          val uri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".provider",
            file
          )
          val intent = Intent(Intent.ACTION_VIEW)
          intent.setDataAndType(uri, "video/*")
          intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
          startActivity(intent)
        }
      }
    videoGrid.onItemLongClickListener =
      AdapterView.OnItemLongClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
        showVideoOperation(
          position
        )
        true
      }

    // 最后在后台刷新
    CoroutineScope(Dispatchers.Default).launch {
      val loadingTextView = findViewById<TextView>(R.id.LoadingBlankText)
      val defaultText = loadingTextView.text
      launch { loadingText(loadingTextView, defaultText) }
      videoList = instance.dateOrderedList()
      val models = ArrayList<VideoModel>()
      for (path in videoList) {
        models.add(VideoModel(File(path)))
      }
      runOnUiThread {
        val adapter = VideoAdapter(this@video_page, models)
        videoGrid.setAdapter(adapter)
        findViewById<TextView>(R.id.LoadingBlankText).visibility = View.GONE
        findViewById<LinearLayout>(R.id.NothingFoundHint).visibility = if (models.isEmpty){ // 没有东西则显示空
          View.VISIBLE
        }else{
          View.GONE
        }
      }
    }
  }

  private fun showVideoOperation(position: Int) {
    AlertHelper.showItemAlert(this,
      onCopy = {
        val file = File(videoList[position])
        if (file.isFile) {
          ClipHelper.getInstance(this).copy(file, this)
        }
      },
      onPaste = {
        val uri = ClipHelper.getInstance(this).paste(this)
        if (uri != null) {
          val dir = File(pasteDir)
          if (!dir.exists()) {
            dir.mkdir()
          }
          val name = uri.path?.split('/')?.last() ?: "somePastedItem"
          val ext = name.split('.').last()
          if (!".$ext".matches(VideoLister.regex)) {
            Toast.makeText(this, getString(R.string.error_nothing_to_paste), Toast.LENGTH_SHORT)
              .show()
            return@showItemAlert
          }
          val actualFile = File(dir, "${name}_paste.$ext")
          val inputStream = contentResolver.openInputStream(uri)
          if (inputStream != null) {
            actualFile.writeBytes(IOUtils.toByteArray(inputStream))
            inputStream.close()
            // 刷新
            update()
          }
        } else {
          Toast.makeText(this, getString(R.string.error_nothing_to_paste), Toast.LENGTH_SHORT)
            .show()
        }
      },
      onDelete = {
        AlertHelper.showDeleteAlert(this, videoList[position]) {
          update()
        }
      },
      onCut = {
        CutHelper.cut(this, File(videoList[position]))
        update()
      },
      onInfo = {
        AlertHelper.showFileInfoAlert(this, videoList[position])
      }
    )
  }

  private fun showSortOptions() {
    val loadingTextView = findViewById<TextView>(R.id.LoadingBlankText)
    // 创建对话框构建者
    val builder = Builder(this@video_page)
    builder.setTitle("选择排序方式")
      .setItems(
        arrayOf<CharSequence>("按时间排序（默认）", "按大小排序")
      ) { dialog: DialogInterface?, which: Int ->
        when (which) {
          0 -> {
            videoListOrderType = 0
            runOnUiThread {
              loadingTextView.visibility = View.VISIBLE
            }
            CoroutineScope(Dispatchers.IO).launch {
              loadingText(
                loadingTextView,
                loadingTextView.text
              )
            }
            update {
              runOnUiThread {
                loadingTextView.visibility = View.GONE
                Toast.makeText(
                  this@video_page,
                  "已选择按时间排序",
                  Toast.LENGTH_SHORT
                ).show()
              }
            }
          }

          1 -> {
            videoListOrderType = 1
            runOnUiThread {
              loadingTextView.visibility = View.VISIBLE
            }
            CoroutineScope(Dispatchers.IO).launch {
              loadingText(
                loadingTextView,
                loadingTextView.text
              )
            }
            update {
              runOnUiThread {
                loadingTextView.visibility = View.GONE
                Toast.makeText(
                  this@video_page,
                  "已选择按大小排序",
                  Toast.LENGTH_SHORT
                ).show()
              }
            }
          }
        }
      }
      .setNegativeButton(
        "取消"
      ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
      .show()
  }

  private fun update(runSomethingMore: (() -> Unit)? = null) {
    instance.initialize {
      videoList = when (videoListOrderType) {
        0 -> instance.dateOrderedList()
        1 -> instance.sizeOrderedList()
        else -> listOf()
      }
      val models = ArrayList<VideoModel>()
      for (path in videoList) {
        models.add(VideoModel(File(path)))
      }
      val adapter = VideoAdapter(this, models)
      runOnUiThread {
        val grid = findViewById<GridView>(R.id.VideoGrid)
        grid.setAdapter(adapter)
        findViewById<LinearLayout>(R.id.NothingFoundHint).visibility = if (models.isEmpty){ // 没有东西则显示空
          View.VISIBLE
        }else{
          View.GONE
        }
      }
      runSomethingMore?.invoke()
    }
  }

  @SuppressLint("SetTextI18n")
  private fun loadingText(
    loadingTextView: TextView,
    defaultText: CharSequence,
    dots: String = ""
  ) {
    Thread.sleep(500)
    val next = if (dots.length > 3) {
      ""
    } else {
      "$dots."
    }
    runOnUiThread {
      loadingTextView.text = "$defaultText$next"
    }

    if (loadingTextView.visibility != View.GONE) {
      loadingText(loadingTextView, defaultText, next)
    }
  }
}