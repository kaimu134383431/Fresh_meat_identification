package com.example.freshmeatidentification;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.BitmapFactory;
import java.io.InputStream;

import org.tensorflow.lite.DataType;

public class Loading extends AppCompatActivity {
    private static final String TAG = "activity_loading";
    private Interpreter tflite;
    private String imagePath;
    private ExecutorService executorService;
    private ProgressBar progressBar;
    private TextView progressPercentageTextView; // 進捗パーセンテージ表示用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // UIコンポーネントの初期化
        progressBar = findViewById(R.id.progress_bar);
        progressPercentageTextView = findViewById(R.id.progress_percentage);

        // ExecutorServiceの初期化
        executorService = Executors.newSingleThreadExecutor();

        // Intentから画像パスを取得
        imagePath = getIntent().getStringExtra("IMAGE_PATH");
        if (imagePath == null) {
            Toast.makeText(this, "画像パスが取得できませんでした", Toast.LENGTH_SHORT).show();
            navigateToFailed();
            return;
        }

        imagePath = getRealPathFromURI(Uri.parse(imagePath));
        if (imagePath == null) {
            Toast.makeText(this, "画像ファイルのパスが取得できませんでした", Toast.LENGTH_SHORT).show();
            navigateToFailed();
            return;
        }

        // 非同期でモデル読み込みと推論処理を開始
        executorService.execute(() -> {
            try {
                tflite = new Interpreter(loadModelFile());
                classifyImage();
            } catch (IOException e) {
                Log.e(TAG, "モデルの読み込みに失敗しました", e);
                navigateToFailed();
            }
        });
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        try (AssetFileDescriptor fileDescriptor = getAssets().openFd("meat_freshness_model.tflite");
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
             FileChannel fileChannel = inputStream.getChannel()) {

            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown(); // アクティビティ終了時にExecutorServiceをシャットダウン
        }
    }

    private void classifyImage() {
        Bitmap bitmap = getBitmapFromUri(Uri.parse(imagePath));
        if (bitmap == null) {
            Log.e(TAG, "ビットマップのデコードに失敗しました");
            navigateToFailed();
            return;
        }

        // 画像をパッチに分割して推論
        Bitmap[] patches = splitImageIntoPatches(bitmap, 10);
        float[][] patchResults = new float[patches.length][3];

        for (int i = 0; i < patches.length; i++) {
            Bitmap resizedPatch = Bitmap.createScaledBitmap(patches[i], 416, 416, true);
            TensorBuffer inputBuffer = TensorBuffer.createFixedSize(new int[]{1, 416, 416, 3}, DataType.FLOAT32);
            convertBitmapToTensorBuffer(resizedPatch, inputBuffer);

            float[][] result = new float[1][3];
            tflite.run(inputBuffer.getBuffer(), result);

            patchResults[i] = result[0];  // 各パッチの結果を保存

            // 進捗を更新
            final int progress = (i + 1) * 100 / patches.length; // 現在の進捗パーセンテージ
            runOnUiThread(() -> {
                progressBar.setProgress(progress);
                progressPercentageTextView.setText(progress + "%");
            });
        }

        // 結果の集計や次の画面への遷移
        processPatchResults(patchResults);
    }

    private void processPatchResults(float[][] patchResults) {
        int fleshCount = 0;
        int halfFleshCount = 0;
        int spoiledCount = 0;

        for (float[] result : patchResults) {
            if (result[0] > result[1] && result[0] > result[2]) {
                fleshCount++;
            } else if (result[1] > result[0] && result[1] > result[2]) {
                halfFleshCount++;
            } else {
                spoiledCount++;
            }
        }

        int totalPatches = patchResults.length;
        float fleshPercentage = (fleshCount / (float) totalPatches) * 100;
        float halfFleshPercentage = (halfFleshCount / (float) totalPatches) * 100;
        float spoiledPercentage = (spoiledCount / (float) totalPatches) * 100;

        runOnUiThread(() -> navigateToResult(fleshPercentage, halfFleshPercentage, spoiledPercentage));
    }

    private String getRealPathFromURI(Uri contentUri) {
        if ("content".equals(contentUri.getScheme())) {
            return contentUri.toString();  // ファイルパスの代わりにUriを直接返す
        } else if ("file".equals(contentUri.getScheme())) {
            return contentUri.getPath();
        }
        return null;
    }

    private Bitmap getBitmapFromUri(Uri contentUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(contentUri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "Failed to open InputStream from Uri", e);
            return null;
        }
    }

    private void convertBitmapToTensorBuffer(Bitmap bitmap, TensorBuffer buffer) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] intValues = new int[width * height];
        bitmap.getPixels(intValues, 0, width, 0, 0, width, height);

        float[] floatValues = new float[width * height * 3];
        for (int i = 0; i < intValues.length; ++i) {
            int val = intValues[i];
            floatValues[i * 3] = ((val >> 16) & 0xFF) / 255.0f; // R
            floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) / 255.0f; // G
            floatValues[i * 3 + 2] = (val & 0xFF) / 255.0f; // B
        }

        buffer.loadArray(floatValues);
    }


    private Bitmap[] splitImageIntoPatches(Bitmap originalImage, int numPatches) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // パッチの数を考慮して横と縦の分割数を決める
        int patchWidth = width / numPatches;
        int patchHeight = height / numPatches;

        Bitmap[] patches = new Bitmap[numPatches * numPatches];

        int count = 0;
        for (int i = 0; i < numPatches; i++) {
            for (int j = 0; j < numPatches; j++) {
                int x = j * patchWidth;
                int y = i * patchHeight;

                // パッチ画像を抽出
                patches[count] = Bitmap.createBitmap(originalImage, x, y, patchWidth, patchHeight);
                count++;
            }
        }
        return patches;
    }


    private void navigateToResult(float fleshPercentage, float halfFleshPercentage, float spoiledPercentage) {
        Intent intent = new Intent(this, Result.class);
        intent.putExtra("FLESH_PERCENTAGE", fleshPercentage);
        intent.putExtra("HALF_FLESH_PERCENTAGE", halfFleshPercentage);
        intent.putExtra("SPOILED_PERCENTAGE", spoiledPercentage);

        String imagePath = getIntent().getStringExtra("IMAGE_PATH");
        if (imagePath != null) {
            intent.putExtra("IMAGE_PATH", imagePath);
        }
        startActivity(intent);
        finish();
    }

    private void navigateToFailed() {
        Intent intent = new Intent(this, LoadingFailed.class);
        String imagePath = getIntent().getStringExtra("IMAGE_PATH");
        if (imagePath != null) {
            intent.putExtra("IMAGE_PATH", imagePath);
        }
        startActivity(intent);
        finish();
    }

}
