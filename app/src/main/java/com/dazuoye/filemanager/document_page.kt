package com.dazuoye.filemanager

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
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
import com.dazuoye.filemanager.fileSystem.adapters.DocumentAdapter
import com.dazuoye.filemanager.fileSystem.adapters.DocumentModel
import com.dazuoye.filemanager.compose.SearchActivity
import com.dazuoye.filemanager.fileSystem.CutHelper
import com.dazuoye.filemanager.fileSystem.byTypeFileLister.DocumentLister.Companion.instance
import com.dazuoye.filemanager.fileSystem.byTypeFileLister.DocumentLister.Companion.regex
import com.dazuoye.filemanager.utils.AlertHelper
import com.dazuoye.filemanager.utils.ClipHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import java.io.File

class document_page : BaseActivity() {
  private var documentList = listOf<String>()
  private val pasteDir = "${Environment.getExternalStorageDirectory().path}/Documents/pasted"
  private var listOrderType = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    this.enableEdgeToEdge()
    setContentView(R.layout.document_page)
    ViewCompat.setOnApplyWindowInsetsListener(
      findViewById(R.id.main)
    ) { v: View, insets: WindowInsetsCompat ->
      val systemBars =
        insets.getInsets(Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    // 设置排序按钮的点击事件
    val sortImageView = findViewById<ImageView>(R.id.sortDocumentView)
    sortImageView.setOnClickListener { v: View? -> showSortOptions() }

    // 设置左箭头的点击事件，返回上一级页面
    val leftArrowImageView = findViewById<ImageView>(R.id.leftArrowImageView)
    leftArrowImageView.setOnClickListener { v: View? ->
      val intent =
        Intent(
          this@document_page,
          main_page::class.java
        )
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
      startActivity(intent)
      finish()
    }

    val searchImageView = findViewById<ImageView>(R.id.searchDocumentView)
    searchImageView.setOnClickListener { v: View? ->
      val intent =
        Intent(
          this,
          SearchActivity::class.java
        )
      val bundle = Bundle()
      bundle.putString("type", "document")
      intent.putExtras(bundle)
      startActivity(intent) // 跳转到搜索页面
    }

    findViewById<ImageView>(R.id.refreshData).setOnClickListener { _ ->
      update()
    }

    val documentGrid: GridView = findViewById(R.id.DocumentGrid)

    documentGrid.onItemClickListener =
      AdapterView.OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
        val file = File(documentList[position])
        if (file.isFile) {
          val uri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".provider",
            file
          )
          val intent = Intent(Intent.ACTION_VIEW)
          intent.setDataAndType(
            uri, when (file.extension) {
              "xls" -> "application/vnd.ms-excel"
              "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
              "doc" -> "application/msword"
              "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
              "ppt" -> "application/vnd.ms-powerpoint"
              "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
              "txt" -> "text/plain"
              "htm", "html" -> "text/html"
              "pdf" -> "application/pdf"

              else -> "application/*"
            }
          )
          intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
          try {
            startActivity(intent)
          } catch (e: ActivityNotFoundException) {
            Toast.makeText(baseContext, "没有安装可以打开此类型文件的应用", Toast.LENGTH_SHORT)
              .show()
          }
        }
      }
    documentGrid.onItemLongClickListener =
      AdapterView.OnItemLongClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
        showDocumentOperation(
          position
        )
        true
      }

    // 最后在后台刷新
    CoroutineScope(Dispatchers.Default).launch {
      val loadingTextView = findViewById<TextView>(R.id.LoadingBlankText)
      val defaultText = loadingTextView.text
      launch { loadingText(loadingTextView, defaultText) }
      documentList = instance.dateOrderedList()
      val models = ArrayList<DocumentModel>()
      for (path in documentList) {
        models.add(DocumentModel(File(path)))
      }
      runOnUiThread {
        val adapter = DocumentAdapter(this@document_page, models)
        documentGrid.setAdapter(adapter)
        findViewById<TextView>(R.id.LoadingBlankText).visibility = View.GONE
        findViewById<LinearLayout>(R.id.NothingFoundHint).visibility = if (models.isEmpty){ // 没有东西则显示空
          View.VISIBLE
        }else{
          View.GONE
        }
      }
    }
  }

  private fun showDocumentOperation(position: Int) {
    AlertHelper.showItemAlert(this,
      onCopy = {
        val file = File(documentList[position])
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
          if (!".$ext".matches(regex)) {
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
        AlertHelper.showDeleteAlert(this, documentList[position]) {
          update()
        }
      },
      onCut = {
        CutHelper.cut(this, File(documentList[position]))
        update()
      },
      onInfo = {
        AlertHelper.showFileInfoAlert(this, documentList[position])
      }
    )
  }

  private fun showSortOptions() {
    val loadingTextView = findViewById<TextView>(R.id.LoadingBlankText)
    // 创建对话框构建者
    val builder = Builder(this@document_page)
    builder.setTitle("选择排序方式")
      .setItems(
        arrayOf<CharSequence>("按时间排序（默认）", "按大小排序")
      ) { dialog: DialogInterface?, which: Int ->
        when (which) {
          0 -> {
            listOrderType = 0
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
                  this@document_page,
                  getString(R.string.sort_by_time),
                  Toast.LENGTH_SHORT
                ).show()
              }
            }
          }

          1 -> {
            listOrderType = 1
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
                  this@document_page,
                  getString(R.string.sort_by_size),
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
      documentList = when (listOrderType) {
        0 -> instance.dateOrderedList()
        1 -> instance.sizeOrderedList()
        else -> listOf()
      }
      val models = ArrayList<DocumentModel>()
      for (path in documentList) {
        models.add(DocumentModel(File(path)))
      }
      val adapter = DocumentAdapter(this, models)
      runOnUiThread {
        val grid = findViewById<GridView>(R.id.DocumentGrid)
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