package com.example.freshmeatidentification;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.view.View;
import android.content.Intent;

public class Result extends AppCompatActivity {
    private TextView result1TextView;
    private TextView result2TextView;
    private TextView result3TextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        ImageView imageView = findViewById(R.id.imageView);
        result1TextView = findViewById(R.id.result1_text_view);
        result2TextView = findViewById(R.id.result2_text_view);
        result3TextView = findViewById(R.id.result3_text_view);
        TextView percentageTextView = findViewById(R.id.percentage_text_view); // パーセンテージ表示用

        // Intentからデータを取得
        String imagePath = getIntent().getStringExtra("IMAGE_PATH");

        // パーセンテージデータを受け取る
        float fleshPercentage = getIntent().getFloatExtra("FLESH_PERCENTAGE", 0);
        float halfFleshPercentage = getIntent().getFloatExtra("HALF_FLESH_PERCENTAGE", 0);
        float spoiledPercentage = getIntent().getFloatExtra("SPOILED_PERCENTAGE", 0);

        //肉の状態を決定
        String meatCondition;

        if (fleshPercentage >= halfFleshPercentage && fleshPercentage >= spoiledPercentage) {
            meatCondition = "flesh";
        } else if (halfFleshPercentage >= fleshPercentage && halfFleshPercentage >= spoiledPercentage) {
            meatCondition = "half flesh";
        } else {
            meatCondition = "spoiled";
        }


        // 画像を表示
        if (imagePath != null) {
            Uri imageUri = Uri.parse(imagePath);
            imageView.setImageURI(imageUri);
        }

        // パーセンテージを表示
        String percentageText;

        if (fleshPercentage < 0 || halfFleshPercentage < 0 || spoiledPercentage < 0) {
            percentageText = "データがありません"; // エラーハンドリング
        } else {
            percentageText = String.format("新鮮: %.1f%%\n半分新鮮: %.1f%%\n腐敗: %.1f%%", fleshPercentage, halfFleshPercentage, spoiledPercentage);
        }
        percentageTextView.setText(percentageText);


        // 結果に応じたコメントを設定
        if ("flesh".equals(meatCondition)) {
            result1TextView.setText("肉の状態: 新鮮");
            result2TextView.setText("美味しく食べられるよ！");
            result3TextView.setText("保存方法に気を付けてね！");
            setAllTextColors(0xFF0000FF); // 青色
        } else if ("half flesh".equals(meatCondition)) {
            result1TextView.setText("肉の状態: 半分新鮮");
            result2TextView.setText("すぐに食べてね！");
            result3TextView.setText("状態を確認してね！");
            setAllTextColors(0xFF008000); // 緑色
        } else {
            result1TextView.setText("肉の状態: 腐敗");
            result2TextView.setText("食べちゃだめだよ！");
            result3TextView.setText("捨ててねー！！");
            setAllTextColors(0xFFFF0000); // 赤色
        }
        Button firstReturnButton = findViewById(R.id.first_return_button);
        //ボタンの処理最初の画面に戻る
        firstReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intentを使って新しいアクティビティに遷移
                Intent intent = new Intent(Result.this, CameraActivity.class); // MainActivityに遷移
                startActivity(intent); // アクティビティを開始
            }
        });
        // ボタンの処理終了時
        Button endButton = findViewById(R.id.end_button);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity(); // アプリ全体を終了
            }
        });
    }

    // すべてのTextViewの色を変更するメソッド
    private void setAllTextColors(int color){
        result1TextView.setTextColor(color);
        result2TextView.setTextColor(color);
        result3TextView.setTextColor(color);
    }


}
