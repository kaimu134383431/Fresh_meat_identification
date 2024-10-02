package com.example.freshmeatidentification;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LoadingFailed extends AppCompatActivity {
    private Button bYes;
    private Button bNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_failed);

        bYes = findViewById(R.id.b_yes);
        bNo = findViewById(R.id.b_no);

        // 「はい」ボタンのクリックリスナー
        bYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intentに画像パスを再度渡す
                Intent intent = new Intent(LoadingFailed.this, Loading.class);
                String imagePath = getIntent().getStringExtra("IMAGE_PATH");
                if (imagePath != null) {
                    intent.putExtra("IMAGE_PATH", imagePath); // 画像パスを再渡し
                }
                startActivity(intent);
                finish();
            }
        });


        // 「いいえ」ボタンのクリックリスナー
        bNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MainActivityに遷移
                Intent intent = new Intent(LoadingFailed.this, MainActivity.class);
                startActivity(intent);
                finish(); // 現在のアクティビティを終了
            }
        });
    }
}
