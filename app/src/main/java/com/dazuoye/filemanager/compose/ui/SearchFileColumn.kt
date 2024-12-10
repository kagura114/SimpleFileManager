package com.dazuoye.filemanager.compose.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.dazuoye.filemanager.R
import com.dazuoye.filemanager.fileSystem.WrappedFile
import com.dazuoye.filemanager.fileSystem.searchFile
import com.dazuoye.filemanager.main_page
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * 用于在搜索结果中显示文件列表的类
 * @param context 上下文对象
 * @param searchTypeName 搜索类型名称，用于显示在UI中
 * @param searchRegex 搜索的正则表达式
 */
class SearchFileColumn(
  val context: Context,
  private val searchTypeName: String,
  private val searchRegex: Regex?
) {
  // 保存搜索到的文件列表
  private val fileList = mutableStateListOf<WrappedFile>()
  // 搜索输入的文本状态
  private val searchText = mutableStateOf("")

  /**
   * Composable函数，用于绘制搜索文件的主界面
   */
  @Composable
  fun Draw() {
    var list by remember { mutableStateOf<List<String>>(emptyList()) }
    var isOkay by remember { mutableStateOf(false) }
    var sortByTime by remember { mutableStateOf(true) }

    // 当搜索结果或排序方式改变时，重新加载并排序文件列表
    LaunchedEffect(isOkay, list, sortByTime) {
      isOkay = false
      fileList.clear()
      val wfList = list.map { WrappedFile(File(it)) }
      if (sortByTime) {
        fileList.addAll(wfList.sortedBy { it.lastModifiedTime })
      } else {
        fileList.addAll(wfList.sortedBy { it.size })
      }
      isOkay = true
    }

    // 主界面布局，包含返回按钮、标题和排序按钮
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .background(Color(context.getColor(R.color.WhiteSmoke)))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // 返回按钮，点击时返回主页面
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

        // 显示搜索标题或搜索结果
        Text(
          text = if (list.isEmpty()) {
            context.getString(R.string.search_here, searchTypeName)
          } else {
            context.getString(R.string.search_result, searchTypeName)
          },
          fontSize = 28.sp,
          modifier = Modifier
            .padding(start = 10.dp)
            .padding(vertical = 5.dp),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.weight(1f))

        // 排序按钮，切换按时间或大小排序
        IconButton(
          onClick = {
            sortByTime = !sortByTime
            CoroutineScope(Dispatchers.Main).launch {
              Toast.makeText(
                context, context.getString(
                  if (sortByTime) {
                    R.string.sort_by_time
                  } else {
                    R.string.sort_by_size
                  }
                ), Toast.LENGTH_SHORT
              ).show()
            }
          },
        ) {
          Image(
            imageVector = ImageVector.vectorResource(
              if (sortByTime) {
                R.drawable.baseline_access_time_24
              } else {
                R.drawable.baseline_storage_24
              }
            ), "sortMethod"
          )
        }
      }

      // 加载或显示搜索结果
      if (!isOkay) {
        Column(
          modifier = Modifier.fillMaxSize(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          // 显示“加载中”的文本
          Text(
            text = context.getString(R.string.loading),
            fontSize = 34.sp
          )
        }
      } else {
        // 显示搜索结果的文件列表
        DrawColumns(
          fileList,
          searchText = searchText.value,
          onSearch = { name, searchWhat ->
            searchText.value = searchWhat
            searchFile(name, searchRegex) {
              list = it
              CoroutineScope(Dispatchers.Main).launch {
                if (list.isEmpty()) {
                  Toast.makeText(
                    context,
                    context.getString(R.string.search_no_result), Toast.LENGTH_SHORT
                  ).show()
                } else {
                  Toast.makeText(
                    context,
                    context.getString(R.string.search_some_result, list.size), Toast.LENGTH_SHORT
                  ).show()
                }
                isOkay = !isOkay
              }
            }
          }
        ) {
          // 文件点击事件，打开对应的文件
          val file = File(it)
          if (file.isFile) {
            val uri = FileProvider.getUriForFile(
              context,
              context.packageName + ".provider",
              file
            )
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, WrappedFile(file).mime)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
              context.startActivity(intent)
            }catch (e: ActivityNotFoundException){
              Toast.makeText(context,"无打开此应用的程序", Toast.LENGTH_SHORT).show()
            }
          }
        }
      }
    }
  }

  /**
   * Composable函数，用于绘制文件列表
   * @param fileList 文件列表
   * @param searchText 搜索输入文本
   * @param onSearch 搜索按钮点击事件
   * @param onItemClick 文件点击事件
   */
  @Composable
  private fun DrawColumns(
    fileList: List<WrappedFile>,
    searchText: String = "",
    onSearch: ((String, String) -> Unit)? = null,
    onItemClick: ((String) -> Unit)? = null
  ) {
    LazyColumn(
      modifier = Modifier.padding(vertical = 5.dp)
    ) {
      // 搜索输入框
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          var searchInput by remember { mutableStateOf(searchText) }

          // 搜索输入框
          TextField(
            value = searchInput,
            maxLines = 1,
            onValueChange = { searchInput = it },
            modifier = Modifier
              .padding(horizontal = 15.dp, vertical = 10.dp)
              .fillParentMaxWidth(0.9f),
            colors = TextFieldDefaults.colors(
              focusedContainerColor = Color(0xFFFFFAFA),
              unfocusedContainerColor = Color.White,
              focusedIndicatorColor = Color(0xFF03A9F4),
              unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle(fontSize = 18.sp),
            trailingIcon = {
              // 点击图标进行搜索
              Image(
                ImageVector.vectorResource(R.drawable.ic_search),
                context.getString(R.string.search),
                modifier = Modifier
                  .clickable {
                    if (searchInput.isNotEmpty()) {
                      if (searchInput == "." || searchInput == "..") {
                        // 输入非法时提示错误
                        Toast
                          .makeText(
                            context,
                            context.getString(R.string.error_search_input_illegal),
                            Toast.LENGTH_SHORT
                          )
                          .show()
                      }
                      onSearch?.invoke(searchInput, searchInput)
                    } else {
                      Toast
                        .makeText(
                          context,
                          context.getString(R.string.error_need_search_input), Toast.LENGTH_SHORT
                        )
                        .show()
                    }
                  }
              )
            }
          )
        }
      }

      // 显示每个文件的视图
      items(fileList) { file ->
        FileSingleView(
          file,
          onItemClick = onItemClick
        )
      }
    }
  }

  /**
   * Composable函数，用于绘制单个文件项
   * @param file 文件对象
   * @param onItemClick 文件点击事件
   */
  @Composable
  private fun FileSingleView(
    file: WrappedFile,
    onItemClick: ((String) -> Unit)? = null
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = Dp.Hairline,
          color = Color.Gray,
          shape = RectangleShape
        )
        .padding(vertical = 3.dp)
        .clickable {
          onItemClick?.invoke(file.path)
        },
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // 显示文件图标
      Image(
        ImageVector.vectorResource(
          when (file.mime.split('/').first()) {
            "dir" -> R.drawable.type_directory
            "image" -> R.drawable.type_image
            "video" -> R.drawable.type_video
            "audio" -> R.drawable.type_audio
            else -> R.drawable.type_file
          }
        ), file.mime,
        modifier = Modifier
          .padding(horizontal = 8.dp)
      )
      Column(
        modifier = Modifier
          .padding(horizontal = 15.dp)
          .fillMaxWidth(0.8f)
      ) {
        // 文件名称
        Text(
          text = file.name,
          fontSize = 24.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        // 文件最后修改时间
        Text(
          text = file.getModifiedTimeString(context),
          fontSize = 15.sp,
          color = Color.Gray,
          maxLines = 1
        )
      }

      Spacer(Modifier.weight(1f))

      // 显示文件信息的按钮
      IconButton(
        onClick = {
          showFileInfoAlert(context, file.path)
        },
        modifier = Modifier.padding(horizontal = 10.dp)
      ) {
        Image(
          ImageVector.vectorResource(R.drawable.outline_info_24), "info"
        )
      }
    }
  }

  /**
   * 显示文件详细信息的弹窗
   * @param context 上下文对象
   * @param file 文件路径
   */
  fun showFileInfoAlert(context: Context, file: String) {
    val f = File(file)
    if (!f.exists()) {
      return
    }
    val wrappedFile = WrappedFile(f)

    val builder = AlertDialog.Builder(context)
    builder.setTitle(context.getString(R.string.file_info))
      .setMessage(
        context.getString(
          R.string.file_info_text,
          wrappedFile.name,
          wrappedFile.path,
          wrappedFile.getSizeString(),
          wrappedFile.getModifiedTimeString(context)
        )
      )
      .setNegativeButton(context.getString(R.string.okay)) { dialog, _ ->
        dialog.dismiss()
      }
      .show()
  }
}
