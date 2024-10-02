package com.example.freshmeatidentification;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import java.io.InputStream;
import java.io.IOException;
import android.content.res.AssetFileDescriptor;
import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

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

        // URIからファイルパスを取得
        imagePath = getRealPathFromURI(Uri.parse(imagePath));
        if (imagePath == null) {
            Toast.makeText(this, "画像ファイルのパスが取得できませんでした", Toast.LENGTH_SHORT).show();
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
        try (AssetFileDescriptor fileDescriptor = getAssets().openFd("meat_freshness_model.tflite");
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
             FileChannel fileChannel = inputStream.getChannel()) {

            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }



    private void classifyImage() {
        Bitmap bitmap = getBitmapFromUri(Uri.parse(imagePath));
        if (bitmap == null) {
            Log.e(TAG, "ビットマップのデコードに失敗しました");
            navigateToFailed();
            return;
        }

        // 入力画像サイズを416x416にリサイズ
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 416, 416, true);

        TensorBuffer inputBuffer = TensorBuffer.createFixedSize(new int[]{1, 416, 416, 3}, DataType.FLOAT32);
        convertBitmapToTensorBuffer(resizedBitmap, inputBuffer);

        // 出力バッファのサイズを [1, 3] に修正
        float[][] result = new float[1][3];
        tflite.run(inputBuffer.getBuffer(), result);

        // 出力結果の解釈
        float[] probabilities = result[0];
        String meatCondition;

        if (probabilities[0] > probabilities[1] && probabilities[0] > probabilities[2]) {
            meatCondition = "flesh";
        } else if (probabilities[1] > probabilities[0] && probabilities[1] > probabilities[2]) {
            meatCondition = "half flesh";
        } else {
            meatCondition = "spoiled";
        }

        // Result画面に遷移
        //navigateToResult(meatCondition);
        navigateToFailed();
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


    /*private void navigateToResult() {
        Intent intent = new Intent(this, Result.class);
        startActivity(intent);
        finish();
    }*/

    private void navigateToFailed() {
        Intent intent = new Intent(this, LoadingFailed.class);
        String imagePath = getIntent().getStringExtra("IMAGE_PATH");
        if (imagePath != null) {
            intent.putExtra("IMAGE_PATH", imagePath); // 画像パスを再渡し
        }
        startActivity(intent);
        finish();
    }
}
