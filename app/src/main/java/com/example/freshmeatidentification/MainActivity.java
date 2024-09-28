package com.example.freshmeatidentification;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
//gogo

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //カメラボタンの参照を取得
        ImageButton openCameraButton = findViewById(R.id.b_camera);
        //カメラボタンクリック時の処理
        openCameraButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        });

        /*new Thread(()->{
            //後で必ず変更すること～！とりあえず置いてあるだけ
            //ロードの処理以下に記述
            try{
                Thread.sleep(3000);
            }catch(InterruptedException e){
                return;
            }
            runOnUiThread(() -> {
                findViewById(R.id.l_load).setVisibility(View.GONE);
                findViewById(R.id.s_result).setVisibility(View.VISIBLE);
            });
        }).start();
*/
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
