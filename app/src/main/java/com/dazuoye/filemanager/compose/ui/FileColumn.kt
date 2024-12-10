package com.dazuoye.filemanager.compose.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.dazuoye.filemanager.R
import com.dazuoye.filemanager.SettingStorage
import com.dazuoye.filemanager.compose.PasteHelper
import com.dazuoye.filemanager.fileSystem.CutHelper
import com.dazuoye.filemanager.fileSystem.WrappedFile
import com.dazuoye.filemanager.fileSystem.WrappedFile.Type
import com.dazuoye.filemanager.main_page
import com.dazuoye.filemanager.utils.AlertHelper
import com.dazuoye.filemanager.utils.ClipHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileNotFoundException

class FileColumn(val context: Context) {
  private val fileList = mutableStateListOf<WrappedFile>()
  private val settingStorage = SettingStorage(context)

  @Composable
  fun Draw(startFolder: String) {
    var path by remember { mutableStateOf(startFolder) }
    var shouldUpdate by remember { mutableStateOf(false) }

    // 检查当前在的目录，有问题就不渲染
    val cwd = File(path)
    if (!cwd.isDirectory) {
      return
    }

    var isOkay by remember { mutableStateOf(false) }
    var sortByTime by remember { mutableStateOf(true) }

    LaunchedEffect(path, shouldUpdate, sortByTime) {
      isOkay = false
      fileList.clear()
      val wfList = cwd.listFiles()?.map { WrappedFile(it) }
      if (wfList != null) {
        if (settingStorage.get(settingStorage.hideHiddenFile) == true) { // 隐藏点文件，默认 false
          if (sortByTime) {
            fileList.addAll(wfList.sortedBy { it.lastModifiedTime }.filter { !it.name.startsWith('.') })
          } else {
            fileList.addAll(wfList.sortedBy { it.size }.filter { !it.name.startsWith('.') })
          }
        }else{
          if (sortByTime) {
            fileList.addAll(wfList.sortedBy { it.lastModifiedTime })
          } else {
            fileList.addAll(wfList.sortedBy { it.size })
          }
        }
      }
      isOkay = true
    }

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
          text = path,
          fontSize = 24.sp,
          modifier = Modifier
            .padding(start = 10.dp)
            .fillMaxWidth(0.75f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.weight(1f))

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
      if (!isOkay) {
        Column(
          modifier = Modifier.fillMaxSize(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = context.getString(R.string.loading),
            fontSize = 34.sp
          )
        }
      } else {
        DrawColumns(fileList, path, update = { shouldUpdate = !shouldUpdate }) {
          if (it == "/storage/emulated") {
            return@DrawColumns
          }
          val file = File(it)
          if (file.isDirectory) {
            path = it
          } else if (file.isFile) {
            val uri = FileProvider.getUriForFile(
              context,
              context.packageName + ".provider",
              file
            )
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, WrappedFile(file).mime)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
          }
        }
      }
    }
  }

  private companion object {
    var dropTarget: String? = null
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun DrawColumns(
    fileList: List<WrappedFile>,
    cwd: String,
    parent: String? = null,
    update: (() -> Unit)? = null,
    onItemClick: ((String) -> Unit)? = null
  ) {
    val dragAndDropCallBack = remember {
      object : DragAndDropTarget {
        override fun onDrop(event: DragAndDropEvent): Boolean {
          val target = File(dropTarget ?: return false)
          if (!target.exists()) {
            return false.also {
              dropTarget = null
            }
          }
          val data = event.toAndroidDragEvent()
            .clipData.getItemAt(0).text
          if (!data.startsWith("Drag:")) {
            return false.also {
              dropTarget = null
            }
          }

          val source = File(data.split(':').last())
          if (!source.exists()) {
            return false.also {
              dropTarget = null
            }
          }

          if (source.path == target.path) {
            dropTarget = null
            return false
          }

          if (target.isFile) {
            if (source.isFile) {
              val dir = source.parent ?: return false
              val f = File("$dir/合并文件夹")
              if (!f.exists()) {
                f.mkdir()
              }
              ClipHelper.getInstance(context).copy(source, context)
              val sourceUri = ClipHelper.getInstance(context).paste(context) ?: return false
              val inputStream = try {
                context.contentResolver.openInputStream(sourceUri)
              } catch (e: FileNotFoundException) {
                return false.also {
                  dropTarget = null
                }
              }
              if (inputStream != null) {
                val actualFile = File(f, source.name)
                actualFile.writeBytes(IOUtils.toByteArray(inputStream))
                inputStream.close()
              }
              ClipHelper.getInstance(context).copy(target, context)
              val targetUri = ClipHelper.getInstance(context).paste(context) ?: return false
              val inputStream2 = context.contentResolver.openInputStream(targetUri)
              if (inputStream2 != null) {
                val actualFile = File(f, target.name)
                actualFile.writeBytes(IOUtils.toByteArray(inputStream2))
                inputStream2.close()
              }
              Toast.makeText(
                context,
                "已放入 $dir/合并文件夹",
                Toast.LENGTH_SHORT
              ).show()
            } else if (source.isDirectory) {
              ClipHelper.getInstance(context).copy(target, context)
              val sourceUri = ClipHelper.getInstance(context).paste(context) ?: return false.also {
                dropTarget = null
              }
              val inputStream = try {
                context.contentResolver.openInputStream(sourceUri)
              } catch (e: FileNotFoundException) {
                return false.also {
                  dropTarget = null
                }
              }
              if (inputStream != null) {
                val actualFile = File(source, target.name)
                actualFile.writeBytes(IOUtils.toByteArray(inputStream))
                inputStream.close()
              }
              Toast.makeText(
                context,
                "已放入 ${source.path}",
                Toast.LENGTH_SHORT
              ).show()
            }
          } else if (target.isDirectory) {
            if (source.isFile) {
              ClipHelper.getInstance(context).copy(source, context)
              val sourceUri = ClipHelper.getInstance(context).paste(context) ?: return false.also {
                dropTarget = null
              }
              val inputStream = try {
                context.contentResolver.openInputStream(sourceUri)
              } catch (e: FileNotFoundException) {
                return false.also {
                  dropTarget = null
                }
              }
              if (inputStream != null) {
                val actualFile = File(target, source.name)
                actualFile.writeBytes(IOUtils.toByteArray(inputStream))
                inputStream.close()
              }
              Toast.makeText(
                context,
                "已放入 ${target.path}",
                Toast.LENGTH_SHORT
              ).show()
            }
          }
          update?.invoke()
          dropTarget = null
          return true
        }
      }
    }

    LazyColumn(
      modifier = Modifier.padding(vertical = 5.dp)
    ) {
      // 最顶上那个
      if (parent == null) {
        val parts = cwd.split('/')
        if (parts.lastIndex != 0) {
          val prevDir = StringBuilder()
          for (i in 0..<parts.lastIndex) {
            prevDir.append("/${parts[i]}")
          }
          val prev = File(prevDir.toString())
          if (prev.isDirectory) {
            item {
              FileSingleView(
                WrappedFile(prev),
                cwd,
                ImageVector.vectorResource(R.drawable.outline_arrow_upward_32),
                context.getString(R.string.prev_folder),
                prev.path,
                update
              ) {
                onItemClick?.invoke(it)
              }
            }
          }
        }
      } else {
        item {
          FileSingleView(
            WrappedFile(File(parent)),
            cwd,
            ImageVector.vectorResource(R.drawable.outline_arrow_upward_32),
            context.getString(R.string.prev_folder),
            parent,
            update
          ) {
            onItemClick?.invoke(it)
          }
        }
      }

      // 下面的内容
      items(fileList) { file ->
        FileSingleView(
          file, cwd,
          update = update,
          dragAndDrop = Modifier
            .dragAndDropSource {
              detectTapGestures(onLongPress = {
                startTransfer(
                  DragAndDropTransferData(
                    ClipData.newPlainText(
                      "Drag", "Drag:${file.path}"
                    )
                  )
                )
              })
            },
          dragAndDropCallBack = dragAndDropCallBack,
          onItemClick = onItemClick
        )
      }
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun FileSingleView(
    file: WrappedFile,
    cwd: String,
    forceIcon: ImageVector? = null,
    forceName: String? = null,
    forceParent: String? = null,
    update: (() -> Unit)? = null,
    @SuppressLint("ModifierParameter") dragAndDrop: Modifier = Modifier,
    dragAndDropCallBack: DragAndDropTarget? = null,
    onItemClick: ((String) -> Unit)? = null
  ) {
    val openFileDialog = remember { mutableIntStateOf(3) }
    when (openFileDialog.intValue) {
      0 -> AskForName(
        onDismissRequest = { openFileDialog.intValue = 3 },
        onConfirmation = {
          if (it.isEmpty()) {
            Toast.makeText(
              context,
              context.getString(R.string.error_need_input_name),
              Toast.LENGTH_SHORT
            ).show()
          } else {
            val f = File("$cwd/$it")
            if (f.exists()) {
              Toast.makeText(
                context,
                context.getString(R.string.error_already_exist),
                Toast.LENGTH_SHORT
              ).show()
            } else {
              f.mkdir()
            }
          }
          update?.invoke()
          openFileDialog.intValue = 3
        },
        dir = cwd,
        isDirectory = true
      )

      1 -> AskForName(
        onDismissRequest = { openFileDialog.intValue = 3 },
        onConfirmation = {
          if (it.isEmpty()) {
            Toast.makeText(
              context,
              context.getString(R.string.error_need_input_name),
              Toast.LENGTH_SHORT
            ).show()
          } else {
            val f = File("$cwd/$it")
            if (f.exists()) {
              Toast.makeText(
                context,
                context.getString(R.string.error_already_exist),
                Toast.LENGTH_SHORT
              ).show()
            } else {
              f.createNewFile()
            }
          }
          update?.invoke()
          openFileDialog.intValue = 3
        },
        dir = cwd,
        isDirectory = false
      )

      else -> {}
    }

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
        }
        .then(
          if (dragAndDropCallBack != null) {
            Modifier.dragAndDropTarget(
              shouldStartDragAndDrop = { event ->
                val result = event
                  .mimeTypes()
                  .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                if (result) {
                  dropTarget = file.path
                }
                result
              }, target = dragAndDropCallBack
            )
          } else {
            Modifier
          }
        ),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Image(
        forceIcon ?: ImageVector.vectorResource(
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
          .then(dragAndDrop)
      )
      Column(
        modifier = Modifier
          .padding(horizontal = 15.dp)
          .fillMaxWidth(0.8f)
      ) {
        Text(
          text = forceName
            ?: if (settingStorage.get(settingStorage.hideExtension) == true && !file.name.startsWith('.')) {
              file.nameWithoutExt
            } else {
              file.name
            },
          fontSize = 24.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = forceParent ?: if (file.type == Type.FILE) {
            "${file.getModifiedTimeString(context)}   ${file.getSizeString()}"
          } else {
            file.getModifiedTimeString(context)
          },
          fontSize = 15.sp,
          color = Color.Gray,
          maxLines = 1
        )
      }

      Spacer(Modifier.weight(1f))

      if (forceName == null) {
        IconButton(
          onClick = {
            if (file.type == Type.FILE) { // 普通文件
              AlertHelper.showNoPasteAlert(context,
                onCopy = {
                  val f = File(file.path)
                  if (f.isFile) {
                    ClipHelper.getInstance(context).copy(f, context)
                  }
                },
                onDelete = {
                  AlertHelper.showDeleteAlert(context, file.path) {
                    update?.invoke()
                  }
                },
                onCut = {
                  CutHelper.cut(context, File(file.path))
                  update?.invoke()
                },
                onInfo = {
                  AlertHelper.showFileInfoAlert(context, file.path)
                }
              )
            } else { // 普通文件夹
              AlertHelper.showNoPasteAlert(context,
                onCopy = {
                  val f = File(file.path)
                  if (f.isDirectory) {
                    ClipHelper.getInstance(context).copyFolder(f.path)
                  }
                },
                onDelete = {
                  AlertHelper.showDeleteAlert(context, file.path) {
                    update?.invoke()
                  }
                },
                onCut = {
                  val f = File(file.path)
                  if (f.isDirectory) {
                    CutHelper.cutFolder(context, f)
                  }
                  update?.invoke()
                },
                onInfo = {
                  AlertHelper.showFileInfoAlert(context, file.path)
                }
              )
            }
          },
          modifier = Modifier.padding(horizontal = 10.dp)
        ) {
          Image(
            ImageVector.vectorResource(R.drawable.outline_info_24), "info"
          )
        }
      } else { // 最上面那个按钮
        IconButton(
          onClick = {
            AlertHelper.showOnlyPasteInfoNewAlert(context,
              onPaste = {
                val uri = ClipHelper.getInstance(context).paste(context)
                if (uri != null) {
                  val name = uri.path?.split('/')?.last() ?: "somePastedItem"
                  val ext = name.split('.').last()
                  var actualFile = File(cwd, name)
                  while (actualFile.exists()) {
                    actualFile = File("${actualFile.path}_paste.$ext")
                  }
                  val inputStream = context.contentResolver.openInputStream(uri)
                  if (inputStream != null) {
                    actualFile.writeBytes(IOUtils.toByteArray(inputStream))
                    inputStream.close()
                    // 刷新
                    update?.invoke()
                  }
                } else {
                  val pasteDir = ClipHelper.getInstance(context).pasteFolder()
                  if (pasteDir != null) {
                    val sourceDir = File(pasteDir)
                    if (sourceDir.isDirectory) {
                      var destDir = File("$cwd/${sourceDir.name}")
                      while (destDir.exists()) {
                        destDir = File("${destDir.path}_paste")
                      }
                      destDir.mkdir()
                      PasteHelper.copyDirectory(sourceDir, destDir)
                    }
                    update?.invoke()
                  } else {
                    Toast.makeText(
                      context,
                      context.getString(R.string.error_nothing_to_paste),
                      Toast.LENGTH_SHORT
                    )
                      .show()
                  }
                }
              },
              onInfo = {
                AlertHelper.showFileInfoAlert(context, cwd)
              },
              onNewFile = {
                openFileDialog.intValue = 1
              },
              onNewFolder = {
                openFileDialog.intValue = 0
              }
            )
          },
          modifier = Modifier.padding(horizontal = 10.dp)
        ) {
          Image(
            ImageVector.vectorResource(R.drawable.outline_info_i_24), "info"
          )
        }
      }
    }
  }

  @Composable
  fun AskForName(
    onDismissRequest: () -> Unit,
    onConfirmation: (String) -> Unit,
    dir: String,
    isDirectory: Boolean
  ) {
    var input by remember { mutableStateOf("") }
    AlertDialog(
      icon = {
        Icon(
          ImageVector.vectorResource(R.drawable.baseline_question_mark_24),
          contentDescription = "Ask"
        )
      },
      title = {
        Text(text = context.getString(R.string.input_name))
      },
      text = {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = context.getString(
              if (isDirectory) {
                R.string.create_directory
              } else {
                R.string.create_file
              }, dir
            ),
            modifier = Modifier.padding(vertical = 5.dp)
          )

          TextField(
            value = input,
            onValueChange = { input = it },
            maxLines = 1
          )
        }

      },
      onDismissRequest = {
        onDismissRequest()
      },
      confirmButton = {
        TextButton(
          onClick = {
            onConfirmation(input)
          }
        ) {
          Text(context.getString(R.string.confirm))
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            onDismissRequest()
          }
        ) {
          Text(context.getString(R.string.cancel))
        }
      }
    )
  }
}