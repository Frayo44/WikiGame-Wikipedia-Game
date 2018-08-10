package com.yoavfranco.wikigame.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.utils.Utils;
import com.yoavfranco.wikigame.views.RoundButton;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class MainMenuScreen extends BaseScreen {

    @BindViews({R.id.play, R.id.arcade, R.id.social})
    RoundButton[] buttons;
    @BindViews({R.id.bottom_help, R.id.bottom_rate, R.id.bottom_shop, R.id.bottom_friends})
    AppCompatImageView[] bottomIcons;
    @BindView(R.id.main_bottom_icons_wrapper)
    CardView icons_wrapper;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_menu_screen, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    private void setButtonClick(final View button, final Action action, final boolean screenChange) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //Utils.logDebug("User clicked:", action.name());
                screenChanger.onScreenChange(MainMenuScreen.this, action, null);
                if (screenChange)
                    v.setOnClickListener(null);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Animation slideUpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
        slideUpAnimation.setInterpolator(new FastOutSlowInInterpolator());
        icons_wrapper.startAnimation(slideUpAnimation);
        setButtonClick(buttons[0], Action.LEVELS, true);
        setButtonClick(buttons[1], Action.CHOOSE_MODE_PRACTICE, true);
        setButtonClick(buttons[2], Action.CHALLENGES, true);
        setButtonClick(bottomIcons[0], Action.HELP, true);
        setButtonClick(bottomIcons[1], Action.LEADERBOARDS, true);
        setButtonClick(bottomIcons[2], Action.SHOP, true);
        setButtonClick(bottomIcons[3], Action.FRIENDS, true);

    }

    @Override
    public void clear() {
        for (RoundButton button : buttons) {
            button.animateOut();
        }
        Animation slideDownAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
        slideDownAnimation.setInterpolator(new FastOutSlowInInterpolator());
        slideDownAnimation.setFillAfter(true);
        icons_wrapper.startAnimation(slideDownAnimation);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (onClearListener != null)
                    onClearListener.clearDone();
            }
        }, 300);
    }
}
