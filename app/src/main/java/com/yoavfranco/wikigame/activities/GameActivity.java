package com.yoavfranco.wikigame.activities;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.yoavfranco.wikigame.utils.Utils;


public abstract class GameActivity extends AppCompatActivity {
    @Override
    protected void onResume() {
        super.onResume();
        hideUI();
    }

    private void hideUI() {
        View decorView = getWindow().getDecorView();
        if (decorView != null)
            decorView.setSystemUiVisibility((Utils.isAndroidNewerThanKK() ?
                    (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN) : 0) | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}
