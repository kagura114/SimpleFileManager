package com.dazuoye.filemanager.compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

class Setting(
  private val rowModifier: Modifier = Modifier.fillMaxWidth(),
  private val nameModifier: Modifier = Modifier,
  private val nameTextStyle: TextStyle = TextStyle.Default,
  private val descriptionModifier: Modifier = Modifier,
  private val descriptionTextStyle: TextStyle = TextStyle.Default,
  private val switchModifier: Modifier = Modifier
) {

  @Composable
  fun BooleanSetting(
    name: String,
    initialState: Boolean? = true,
    description: String? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null
  ) {
    var checkState by remember { mutableStateOf(initialState) }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = rowModifier
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(
          if (checkState != null) {
            0.8f
          } else {
            1f
          }
        )
      ) {
        Text(
          text = name,
          modifier = nameModifier,
          style = nameTextStyle
        )
        Spacer(modifier = Modifier.size(5.dp))
        Text(
          text = description ?: "",
          modifier = descriptionModifier,
          style = descriptionTextStyle
        )
      }
      if (checkState != null) {
        Spacer(modifier = Modifier.weight(1f))

        Switch(
          checked = checkState!!,
          onCheckedChange = {
            checkState = it
            onCheckedChange?.invoke(it)
          },
          modifier = switchModifier
        )
      }
    }
  }
}