package com.example.freshmeatidentification;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

        // カメラとストレージのパーミッションを確認
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_PERMISSION);
        } else {
            openCameraApp(); // 両方のパーミッションが許可された場合のみ呼び出し
        }
    }


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
                            getApplicationContext().getPackageName() + ".fileprovider", // ここを修正
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
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            boolean cameraGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean storageGranted = grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED;

            if (cameraGranted && storageGranted) {
                openCameraApp();
            } else {
                // パーミッションが拒否された場合の処理
                Toast.makeText(this, "カメラとストレージのパーミッションが必要です", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
