package com.example.freshmeatidentification;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LoadingFailed extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_failed);

        // 「はい」ボタンのクリックリスナー
        Button bYes = findViewById(R.id.b_yes);
        bYes.setOnClickListener(v -> {
            // Intentに画像パスを再度渡す
            Intent intent = new Intent(LoadingFailed.this, Loading.class);
            String imagePath = getIntent().getStringExtra("IMAGE_PATH");
            if (imagePath != null) {
                intent.putExtra("IMAGE_PATH", imagePath); // 画像パスを再渡し
            }
            startActivity(intent);
            finish();
        });

        // 「いいえ」ボタンのクリックリスナー
        Button bNo = findViewById(R.id.b_no);
        bNo.setOnClickListener(v -> {
            // MainActivityに遷移
            Intent intent = new Intent(LoadingFailed.this, MainActivity.class);
            startActivity(intent);
            finish(); // 現在のアクティビティを終了
        });
    }
}
