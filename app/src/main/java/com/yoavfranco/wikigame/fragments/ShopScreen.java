package com.yoavfranco.wikigame.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yoavfranco.wikigame.R;

public class ShopScreen extends BaseScreen {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shop_screen, container, false);
    }

    @Override
    public void clear() {

    }
}
