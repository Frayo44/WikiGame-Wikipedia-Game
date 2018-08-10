package com.yoavfranco.wikigame.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.utils.Consts;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yoav on 18/03/17.
 */

public class ChooseModeScreen extends BaseScreen {

    @BindView(R.id.cvClicksMode)
    CardView cvClicksMode;

    @BindView(R.id.cvTimerMode)
    CardView cvTimerMode;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.choose_mode_screen, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        cvTimerMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cvClicksMode.setClickable(false);
                cvTimerMode.setClickable(false);
                Intent intent = new Intent();
                intent.putExtra("mode", Consts.TIMER_MODE);
                screenChanger.onScreenChange(ChooseModeScreen.this, Action.QUICKPLAY, intent);
            }
        });

        cvClicksMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cvClicksMode.setClickable(false);
                cvTimerMode.setClickable(false);
                Intent intent = new Intent();
                intent.putExtra("mode", Consts.CLICKS_MODE);
                screenChanger.onScreenChange(ChooseModeScreen.this, Action.QUICKPLAY, intent);
            }
        });
    }


    @Override
    public void clear() {

        getView().animate().scaleX(0).scaleY(0).setDuration(300).start();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (onClearListener != null)
                    onClearListener.clearDone();
            }
        }, 300);
    }
}