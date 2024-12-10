package com.dazuoye.filemanager.fileSystem.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView.LayoutParams
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import com.dazuoye.filemanager.R
import com.dazuoye.filemanager.SettingStorage
import java.io.File

class ImageModel(image: File) {
  val name: String = image.name
  var thumbnail: Bitmap
  val nameWithoutExt: String = image.nameWithoutExtension

  init {
    if (!image.isFile) {
      throw RuntimeException("No such Image")
    }
    thumbnail = ThumbnailUtils.createImageThumbnail(image, Size(80, 120), null)
  }
}

class ImageAdapter(context: Context, list: ArrayList<ImageModel>) :
  ArrayAdapter<ImageModel>(context, 0, list) {
  private val settingStorage = SettingStorage(context)
  private val hideExtension = settingStorage.get(settingStorage.hideExtension)
  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val listView = convertView ?: LayoutInflater.from(context).inflate(
      R.layout.picture_card_item, parent, false
    )
    val model = getItem(position) ?: throw RuntimeException()
    listView.findViewById<ImageView>(R.id.pictureCardImage).setImageBitmap(model.thumbnail)
    val card = listView.findViewById<TextView>(R.id.pictureCardText)
    card.text = if (hideExtension == true) {
      model.nameWithoutExt
    } else {
      model.name
    }
    listView.setLayoutParams(LayoutParams(GridView.AUTO_FIT, 530))

    return listView
  }
}