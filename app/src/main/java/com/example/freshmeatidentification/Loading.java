package com.example.freshmeatidentification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Loading extends AppCompatActivity {
    private static final String TAG = "activity_loading";
    private Interpreter tflite;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // Intentから画像パスを取得
        imagePath = getIntent().getStringExtra("IMAGE_PATH");
        if (imagePath == null) {
            Toast.makeText(this, "画像パスが取得できませんでした", Toast.LENGTH_SHORT).show();
            // エラー時にLoadingFailedアクティビティに遷移する
            navigateToFailed();
            return;
        }

        try {
            tflite = new Interpreter(loadModelFile());
            classifyImage();
        } catch (IOException e) {
            Log.e(TAG, "モデルの読み込みに失敗しました", e);
            navigateToFailed();
        }
    }


    private MappedByteBuffer loadModelFile() throws IOException {
        File modelFile = new File(getExternalFilesDir(null), "meat_freshness_model.tflite");
        try (FileInputStream inputStream = new FileInputStream(modelFile)) {
            FileChannel fileChannel = inputStream.getChannel();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        }
    }

    private void classifyImage() {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

        TensorBuffer inputBuffer = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
        convertBitmapToTensorBuffer(resizedBitmap, inputBuffer);

        float[][] result = new float[1][1];
        tflite.run(inputBuffer.getBuffer(), result);

        if (result[0][0] > 0.5) {
            //navigateToResult(); // 結果に応じた処理を追加
            navigateToFailed();
        } else {
            navigateToFailed();
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

    /*private void navigateToResult() {
        Intent intent = new Intent(this, Result.class);
        startActivity(intent);
        finish();
    }*/

    private void navigateToFailed() {
        Intent intent = new Intent(this, LoadingFailed.class);
        startActivity(intent);
        finish();
    }
}
