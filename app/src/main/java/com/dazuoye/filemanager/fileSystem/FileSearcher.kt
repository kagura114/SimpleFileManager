package com.dazuoye.filemanager.fileSystem

import android.os.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File

fun searchFile(name: String,typeRegex: Regex? = null, onFinished: (List<String>) -> Unit){
  CoroutineScope(Dispatchers.IO).launch {
    val resultList = searchDir(name,Environment.getExternalStorageDirectory().path)
    if (typeRegex == null){
      onFinished.invoke(resultList)
    }else{
      val finalResult = resultList.filter { it.contains(typeRegex) }
      onFinished.invoke(finalResult)
    }
  }
}

suspend fun searchDir(name: String, directory: String): List<String> {
  val list = mutableListOf<String>()
  val dir = File(directory)
  if (!dir.isDirectory || dir.name.startsWith('.')){
    return emptyList()
  }

  val inside = dir.listFiles()
  if (inside == null || inside.isEmpty()){
    return emptyList()
  }

  inside.forEach {
    if (it.isDirectory){
        list.addAll(coroutineScope{ searchDir(name, it.path) })
    }else if (it.isFile){
      if (it.name.contains(name) && !it.name.startsWith('.') ){
        list.add(it.path)
      }
    }
  }
  return list
}