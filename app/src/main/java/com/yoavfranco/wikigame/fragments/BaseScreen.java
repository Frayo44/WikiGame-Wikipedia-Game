package com.yoavfranco.wikigame.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.View;
import android.view.animation.Animation;

import com.yoavfranco.wikigame.utils.Utils;

import static com.yoavfranco.wikigame.fragments.BaseScreen.Action.CHECK_BACK;

public abstract class BaseScreen extends Fragment {
    public ScreenChanger screenChanger;
    public OnClearListener onClearListener;

    public static boolean disableFragmentsAnimations = false;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity a;
        if (context instanceof Activity){
            a = (Activity) context;
            this.screenChanger = (ScreenChanger) a;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            this.screenChanger = (ScreenChanger) activity;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.logInfo(this, "Screen Resumed");
        if (screenChanger != null)
            screenChanger.onScreenChange(this, CHECK_BACK, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Utils.logDebug(this, "Screen Created");
    }

    public abstract void clear();

    public void setOnClearListener(OnClearListener onClearListener) {
        this.onClearListener = onClearListener;
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        if (BaseScreen.disableFragmentsAnimations) {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(0, "alpha",
                    1f);
            fadeOut.setDuration(0);
            return fadeOut;
        }
        return super.onCreateAnimator(transit, enter, nextAnim);
    }

    public enum Action {
        CHECK_BACK,
        QUICKPLAY,
        LEVELS,
        CHALLENGES,
        SHOP,
        HELP,
        LEADERBOARDS,
        FRIENDS,
        CHOOSE_MODE_PRACTICE,
        CHOOSE_MODE_CHALLENGE,
        CHALLENGE_QUICKPLAY,
        SETTINGS, LEVEL_CLICK
    }

    public interface OnClearListener {
        void clearDone();
    }

    public interface ScreenChanger {
        void onScreenChange(BaseScreen screen, Action action, Intent extra);
    }
}
