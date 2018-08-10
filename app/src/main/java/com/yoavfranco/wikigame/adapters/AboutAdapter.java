package com.yoavfranco.wikigame.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;
import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.utils.Item;

/**
 * Created by tomer aka rosenpin on 2/9/16.
 */
public class AboutAdapter extends ArrayAdapter<Item> {
    public AboutAdapter(Context context, ArrayList<Item> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = getItem(position);

        convertView = LayoutInflater.from(getContext()).inflate(R.layout.about_item, parent, false);

        TextView tvName = (TextView) convertView.findViewById(R.id.tvTitle);
        TextView tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);
        ImageView ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);

        tvName.setText(item.getTitle());
        tvDescription.setText(item.getDescription());
        ivIcon.setImageDrawable(item.getImg());

        return convertView;
    }
}