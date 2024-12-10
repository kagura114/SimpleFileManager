package com.dazuoye.filemanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dazuoye.filemanager.compose.SearchActivity;
import com.dazuoye.filemanager.compose.SettingActivity;
import com.dazuoye.filemanager.compose.ViewFileActivity;
import com.dazuoye.filemanager.fileSystem.DeleteHelper;
import java.io.File;

public class main_page extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.main_page);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });

    ButtonClickerHandler buttonClickerHandler = new ButtonClickerHandler(this);

    findViewById(R.id.MainPagePictureButton).setOnClickListener(buttonClickerHandler);
    findViewById(R.id.MainPageVideoButton).setOnClickListener(buttonClickerHandler);
    findViewById(R.id.MainPageMusicButton).setOnClickListener(buttonClickerHandler);
    findViewById(R.id.MainPageDocumentButton).setOnClickListener(buttonClickerHandler);
    findViewById(R.id.MainPageStorageButton).setOnClickListener(buttonClickerHandler);
    findViewById(R.id.MainPageDownloadButton).setOnClickListener(buttonClickerHandler);
    findViewById(R.id.MainPageAllFilesButton).setOnClickListener(buttonClickerHandler);
    findViewById(R.id.MainPageDownloadButton).setOnClickListener(buttonClickerHandler);
    findViewById(R.id.MainPageDocumentsButton).setOnClickListener(buttonClickerHandler);
    findViewById(R.id.MainPageRecordingButton).setOnClickListener(buttonClickerHandler);
    findViewById(R.id.MainPageDCIMButton).setOnClickListener(buttonClickerHandler);
    findViewById(R.id.MainPagePicturesButton).setOnClickListener(buttonClickerHandler);
    findViewById(R.id.MainPageSearchButton).setOnClickListener(buttonClickerHandler);
    findViewById(R.id.MainPageSettingsButton).setOnClickListener(buttonClickerHandler);
  }

  @Override protected void onDestroy() {
    // 把剪切的缓存文件删掉
    File cacheDir = new File(Environment.getExternalStorageDirectory() + "/.copy");
    if (cacheDir.exists()) {
      DeleteHelper.Companion.delete(cacheDir.getPath());
    }
    super.onDestroy();
  }

  public class ButtonClickerHandler implements View.OnClickListener {
    private final Context context;

    ButtonClickerHandler(Context context) {
      this.context = context;
    }

    @Override
    public void onClick(View view) {
      if (view.getId() == R.id.MainPagePictureButton) {
        Intent intent = new Intent(context, picture_page.class);
        startActivity(intent);
      }
      if (view.getId() == R.id.MainPageVideoButton) {
        Intent intent = new Intent(context, video_page.class);
        startActivity(intent);
      }
      if (view.getId() == R.id.MainPageMusicButton) {
        Intent intent = new Intent(context, music_page.class);
        startActivity(intent);
      }
      if (view.getId() == R.id.MainPageDocumentButton) {
        Intent intent = new Intent(context, document_page.class);
        startActivity(intent);
      }
      if (view.getId() == R.id.MainPageStorageButton) {
        Intent intent = new Intent(context, store_page.class);
        startActivity(intent);
      }
      if (view.getId() == R.id.MainPageAllFilesButton) {
        Intent intent = new Intent(context, ViewFileActivity.class);
        startActivity(intent);
      }
      if (view.getId() == R.id.MainPageDCIMButton) {
        Intent intent = new Intent(context, ViewFileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("folder", Environment.getExternalStorageDirectory().getPath() + "/DCIM");
        intent.putExtras(bundle);
        startActivity(intent);
      }
      if (view.getId() == R.id.MainPageDownloadButton) {
        Intent intent = new Intent(context, ViewFileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("folder",
            Environment.getExternalStorageDirectory().getPath() + "/Download");
        intent.putExtras(bundle);
        startActivity(intent);
      }
      if (view.getId() == R.id.MainPageDocumentsButton) {
        Intent intent = new Intent(context, ViewFileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("folder",
            Environment.getExternalStorageDirectory().getPath() + "/Documents");
        intent.putExtras(bundle);
        startActivity(intent);
      }
      if (view.getId() == R.id.MainPageRecordingButton) {
        Intent intent = new Intent(context, ViewFileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("folder",
            Environment.getExternalStorageDirectory().getPath() + "/Recordings");
        intent.putExtras(bundle);
        startActivity(intent);
      }
      if (view.getId() == R.id.MainPagePicturesButton) {
        Intent intent = new Intent(context, ViewFileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("folder",
            Environment.getExternalStorageDirectory().getPath() + "/Pictures");
        intent.putExtras(bundle);
        startActivity(intent);
      }
      if (view.getId() == R.id.MainPageSearchButton) {
        Intent intent = new Intent(context, SearchActivity.class);
        startActivity(intent);
      }
      if (view.getId() == R.id.MainPageSettingsButton) {
        Intent intent = new Intent(context, SettingActivity.class);
        startActivity(intent);
      }
    }
  }
}