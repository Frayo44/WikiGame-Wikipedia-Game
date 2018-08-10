package com.yoavfranco.wikigame.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.fragments.BaseScreen;
import com.yoavfranco.wikigame.fragments.LevelsScreen;
import com.yoavfranco.wikigame.utils.Level;
import com.yoavfranco.wikigame.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;

import static com.yoavfranco.wikigame.utils.Consts.BUNDLE_LEVEL_KEY;


public class LevelsGridAdapter extends BaseAdapter {
    private ArrayList<Level> levels;
    private BaseScreen levelsScreen;
    private Context context;

    public LevelsGridAdapter(Context contexts, Level[] levels, BaseScreen levelsScreen) {
        this.levels = new ArrayList<Level>(Arrays.asList(levels));
        this.levelsScreen = levelsScreen;
        this.context = contexts;
    }

    @Override
    public int getCount() {
        return levels.size();
    }

    @Override
    public Object getItem(int position) {
        return levels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return levels.get(position).getLevelID();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.level_item2, null);
            final View v2 = view;
            view.setVisibility(View.INVISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ScaleAnimation scale = (ScaleAnimation) AnimationUtils.loadAnimation(context, R.anim.grow);
                    scale.setInterpolator(new FastOutSlowInInterpolator());
                    scale.setDuration(150);
                    v2.startAnimation(scale);
                    v2.setVisibility(View.VISIBLE);
                }
            }, position * 15);
        } else
            view = convertView;
        final TextView levelTV = (TextView) view.findViewById(R.id.level);
        levelTV.setText(levels.get(position).getLevelName());
        if (!levels.get(position).isLocked())
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.logDebug(this, "clicked");
                    Intent extra = new Intent();
                    extra.putExtra(BUNDLE_LEVEL_KEY, levels.get(position));
                    levelsScreen.screenChanger.onScreenChange(levelsScreen, BaseScreen.Action.LEVEL_CLICK, extra);
                    v.animate().scaleX(3).scaleY(3).setDuration(300).start();
                }
            });
        else {
            view.setAlpha(0.5f);
            ((CardView) levelTV.getParent()).setCardElevation(2);
        }
        return view;
    }
}
