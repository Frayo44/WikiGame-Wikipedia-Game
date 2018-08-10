package com.yoavfranco.wikigame.views;

import android.content.Context;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.ScaleAnimation;

import com.yoavfranco.wikigame.R;

public class RoundButton extends FloatingActionButton {

    public RoundButton(Context context) {
        super(context);
        init();
    }

    public RoundButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        animateIn();
    }

    public void animateClick(final OnClickListener onClickListener) {
        setOnClickListener(null);
        animateOut(200, false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animateIn(200);
                setOnClickListener(onClickListener);
            }
        }, 210);
    }

    public void animateOut(int duration, boolean fillAfter) {
        ScaleAnimation scale = (ScaleAnimation) AnimationUtils.loadAnimation(getContext(), R.anim.shrink);
        scale.setInterpolator(new AnticipateInterpolator());
        scale.setFillAfter(fillAfter);
        scale.setDuration(duration);
        startAnimation(scale);
    }

    public void animateIn(int duration) {
        if ((this.getAnimation() == null || this.getAnimation().hasEnded()) && this.getVisibility() == View.VISIBLE) {
            ScaleAnimation scale = (ScaleAnimation) AnimationUtils.loadAnimation(getContext(), R.anim.grow);
            scale.setInterpolator(new FastOutSlowInInterpolator());
            scale.setDuration(duration);
            startAnimation(scale);
        }
    }

    public void animateOut() {
        animateOut(300, true);
    }

    public void animateOut(int duration) {
        animateOut(duration, true);
    }

    public void animateOut(boolean fillAfter) {
        animateOut(300, fillAfter);
    }

    public void animateIn() {
        animateIn(300);
    }
}
