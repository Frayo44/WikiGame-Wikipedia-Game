package com.yoavfranco.wikigame.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.utils.Level;
import com.yoavfranco.wikigame.utils.Utils;
import com.yoavfranco.wikigame.adapters.LevelsGridAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LevelsScreen extends BaseScreen {

    @BindView(R.id.levels_grid)
    GridView levelsGrid;
    private Level[][] levels;
    LevelsGridAdapter levelsGridAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.levels_select_screen, container, false);
    }

    public void setLevels(Level[]... levels) {
        this.levels = levels;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        if (levels == null)
            Utils.logDebug(this, "levels is null!");

        updateUI();

    }

    public void updateUI() {
        ArrayList<Level> levelsToShow = new ArrayList<Level>(Arrays.asList(levels[0]));
        Level[] levelsToShowArray = new Level[levelsToShow.size()];
        levelsToShow.toArray(levelsToShowArray);

        levelsGridAdapter = new LevelsGridAdapter(getActivity(), levelsToShowArray, this);
        levelsGrid.setAdapter(levelsGridAdapter);
    }

    @Override
    public void clear() {
    }
}
