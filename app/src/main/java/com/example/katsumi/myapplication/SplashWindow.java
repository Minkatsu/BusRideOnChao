package com.example.katsumi.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

/**
 * Created by Katsumi on 2015/02/01.
 */
public class SplashWindow extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // タイトルを非表示にします。
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // splash.xmlをViewに指定します。
        setContentView(R.layout.splash_window);
        Handler hdl = new Handler();

        // 500ms遅延させてsplashHandlerを実行します。
        hdl.postDelayed(new splashHandler(), 500);
    }

    class splashHandler implements Runnable {
        public void run() {
            // スプラッシュ完了後に実行するActivityを指定します。
            Intent intent = new Intent(getApplication(), MainActivity.class);
            startActivity(intent);

            // SplashActivityを終了させます。
            SplashWindow.this.finish();
        }
    }

}
