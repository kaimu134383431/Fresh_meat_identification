package com.example.freshmeatidentification;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSION = 100;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // ローディングボタンと再撮影ボタンの設定
        Button loadingButton = findViewById(R.id.b_loading);
        Button reshootingButton = findViewById(R.id.b_reshooting);

        // ローディングボタンが押された時の処理
        loadingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Loading画面に移動
                Intent intent = new Intent(CameraActivity.this, Loading.class);
                startActivity(intent);
                finish(); // 現在のアクティビティを終了
            }
        });

        // 再撮影ボタンが押された時の処理
        reshootingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // カメラで写真を撮るところまで戻る
                openCameraApp();
            }
        });

        // パーミッションの確認
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                !isStoragePermissionGranted()) {
            // 必要なパーミッションをリクエスト
            ActivityCompat.requestPermissions(this, getRequiredPermissions(), REQUEST_PERMISSION);
        } else {
            openCameraApp(); // パーミッションが許可された場合のみカメラを起動
        }
    }

    // APIレベルに応じたストレージのパーミッションを確認するメソッド
    private boolean isStoragePermissionGranted() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // 必要なパーミッションを取得するメソッド
    private String[] getRequiredPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            return new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        }
    }

    // カメラアプリを起動するメソッド
    private void openCameraApp() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "画像ファイルの作成に失敗しました", Toast.LENGTH_SHORT).show();
                return; // 処理を中止
            }

            if (photoFile != null) {
                try {
                    imageUri = FileProvider.getUriForFile(this,
                            getApplicationContext().getPackageName() + ".fileprovider", // FileProviderの設定に合わせたURI
                            photoFile);
                    if (imageUri == null) {
                        Toast.makeText(this, "URIの取得に失敗しました", Toast.LENGTH_SHORT).show();
                        return; // 処理を中止
                    }
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "URIの取得に失敗しました", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // 撮影した画像ファイルを保存するためのメソッド
    private File createImageFile() throws IOException {
        // 一意のファイル名を作成
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); // getExternalFilesDirはパーミッション不要
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    // カメラアプリから戻ってきた時の処理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // imageUriがnullでないことを確認
            if (imageUri != null) {
                ImageView imageView = findViewById(R.id.imageView); // あなたのImageViewのIDに置き換えてください
                imageView.setImageURI(imageUri);
            } else {
                Toast.makeText(this, "画像の取得に失敗しました", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // パーミッションリクエストの結果を受け取るメソッド
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            boolean cameraGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean storageGranted = grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED;

            if (cameraGranted && storageGranted) {
                openCameraApp(); // パーミッションが許可された場合にカメラを起動
            } else {
                Toast.makeText(this, "カメラとストレージのパーミッションが必要です", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
