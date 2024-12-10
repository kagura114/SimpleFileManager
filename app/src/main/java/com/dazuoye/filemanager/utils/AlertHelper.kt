package com.dazuoye.filemanager.utils

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog.Builder
import com.dazuoye.filemanager.R
import com.dazuoye.filemanager.fileSystem.DeleteHelper
import com.dazuoye.filemanager.fileSystem.WrappedFile
import java.io.File

class AlertHelper {
  companion object {
    fun showItemAlert(
      context: Context,
      onCopy: () -> Unit,
      onPaste: () -> Unit,
      onDelete: () -> Unit,
      onCut: () -> Unit,
      onInfo: () -> Unit
    ) {
      val builder = Builder(context)
      builder.setTitle(context.getString(R.string.select_action))
        .setItems(
          arrayOf<CharSequence>(
            context.getString(R.string.action_copy),
            context.getString(R.string.action_paste),
            context.getString(R.string.action_delete),
            context.getString(R.string.action_cut),
            context.getString(R.string.action_info)
          )
        ) { _: DialogInterface?, which: Int ->
          when (which) {
            0 -> {
              onCopy()
            }

            1 -> {
              onPaste()
            }

            2 -> {
              onDelete()
            }

            3 -> {
              onCut()
            }

            4 -> {
              onInfo()
            }
          }
        }
        .setNegativeButton(context.getString(R.string.action_cancel))
        { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        .show()
    }

    fun showNoPasteAlert(
      context: Context,
      onCopy: () -> Unit,
      onDelete: () -> Unit,
      onCut: () -> Unit,
      onInfo: () -> Unit
    ) {
      val builder = Builder(context)
      builder.setTitle(context.getString(R.string.select_action))
        .setItems(
          arrayOf<CharSequence>(
            context.getString(R.string.action_copy),
            context.getString(R.string.action_delete),
            context.getString(R.string.action_cut),
            context.getString(R.string.action_info)
          )
        ) { _: DialogInterface?, which: Int ->
          when (which) {
            0 -> {
              onCopy()
            }

            1 -> {
              onDelete()
            }

            2 -> {
              onCut()
            }

            3 -> {
              onInfo()
            }
          }
        }
        .setNegativeButton(context.getString(R.string.action_cancel))
        { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        .show()
    }

    fun showOnlyPasteInfoNewAlert(
      context: Context,
      onPaste: () -> Unit,
      onInfo: () -> Unit,
      onNewFile: () -> Unit,
      onNewFolder: () -> Unit
    ) {
      val builder = Builder(context)
      builder.setTitle(context.getString(R.string.select_action))
        .setItems(
          arrayOf<CharSequence>(
            context.getString(R.string.action_paste),
            context.getString(R.string.action_info),
            context.getString(R.string.action_new_file),
            context.getString(R.string.action_new_folder)
          )
        ) { _: DialogInterface?, which: Int ->
          when (which) {
            0 -> onPaste()
            1 -> onInfo()
            2 -> onNewFile()
            3 -> onNewFolder()
          }
        }
        .setNegativeButton(context.getString(R.string.action_cancel))
        { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        .show()
    }

    fun showDeleteAlert(context: Context, file: String, onConfirm: (() -> Unit)?) {
      val builder = Builder(context)
      builder.setTitle(context.getString(R.string.confirm_to_delete))
        .setMessage(context.getString(R.string.confirm_to_delete_file, file))
        .setPositiveButton(context.getString(R.string.confirm)) { _, _ ->
          DeleteHelper.delete(file)
          onConfirm?.invoke()
        }
        .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
          dialog.dismiss()
        }
        .show()
    }

    fun showFileInfoAlert(context: Context, file: String) {
      val f = File(file)
      if (!f.exists()) {
        return
      }
      val wrappedFile = WrappedFile(f)

      val builder = Builder(context)
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
}