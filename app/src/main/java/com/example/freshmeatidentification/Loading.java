/*package com.example.freshmeatidentification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class Loading extends AppCompatActivity {
    private static final String TAG = "LoadingActivity";
    private Interpreter tflite;
    private String imagePath; // 画像パスを保存

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // Intent から画像パスを取得
        imagePath = getIntent().getStringExtra("IMAGE_PATH");

        // TensorFlow Lite モデルを読み込む
        try {
            tflite = new Interpreter(loadModelFile());
            classifyImage();
        } catch (IOException e) {
            Log.e(TAG, "モデルの読み込みに失敗しました", e);
            navigateToFailed();
        }
    }

    // モデルファイルを読み込むメソッド
    private MappedByteBuffer loadModelFile() throws IOException {
        File modelFile = new File(getExternalFilesDir(null), "meat_freshness_model.tflite");
        FileChannel fileChannel = FileChannel.open(modelFile.toPath(), StandardOpenOption.READ);
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
    }

    // 画像を推論するメソッド
    private void classifyImage() {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        // 画像を前処理（リサイズ、正規化など）する必要があります
        // ここで推論を行います
        float[][] result = new float[1][1]; // 結果を保存する配列
        tflite.run(bitmap, result); // 実際の推論

        if (result[0][0] > 0.5) { // 例えば、0.5を閾値とする場合
            navigateToResult();
        } else {
            navigateToFailed();
        }
    }

    private void navigateToResult() {
        Intent intent = new Intent(this, Result.class);
        startActivity(intent);
        finish();
    }

    private void navigateToFailed() {
        Intent intent = new Intent(this, LoadingFailed.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tflite != null) {
            tflite.close();
        }
    }
}
*/