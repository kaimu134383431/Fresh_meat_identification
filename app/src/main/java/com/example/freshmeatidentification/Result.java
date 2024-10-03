package com.example.freshmeatidentification;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Result extends AppCompatActivity {
    private ImageView imageView;
    private TextView result1TextView;
    private TextView result2TextView;
    private TextView result3TextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        imageView = findViewById(R.id.imageView);
        result1TextView = findViewById(R.id.result1_text_view);
        result2TextView = findViewById(R.id.result2_text_view);
        result3TextView = findViewById(R.id.result3_text_view);

        // Intentからデータを取得
        String imagePath = getIntent().getStringExtra("IMAGE_PATH");
        String meatCondition = getIntent().getStringExtra("MEAT_CONDITION");

        // 画像を表示
        if (imagePath != null) {
            Uri imageUri = Uri.parse(imagePath);
            imageView.setImageURI(imageUri);
        }

        // 結果に応じたコメントを設定
        if ("flesh".equals(meatCondition)) {
            result1TextView.setText("肉の状態: 新鮮");
            result2TextView.setText("美味しくいただけます。");
            result3TextView.setText("保存方法に注意してください。");
            setAllTextColors(0xFF0000FF); // 青色
        } else if ("half flesh".equals(meatCondition)) {
            result1TextView.setText("肉の状態: 半分新鮮");
            result2TextView.setText("すぐに消費することをお勧めします。");
            result3TextView.setText("状態を確認してください。");
            setAllTextColors(0xFF008000); // 緑色
        } else {
            result1TextView.setText("肉の状態: 腐敗");
            result2TextView.setText("食べないでください。");
            result3TextView.setText("廃棄してください。");
            setAllTextColors(0xFFFF0000); // 赤色
        }
    }

    // すべてのTextViewの色を変更するメソッド
    private void setAllTextColors(int color){
        result1TextView.setTextColor(color);
        result2TextView.setTextColor(color);
        result3TextView.setTextColor(color);
    }

}
