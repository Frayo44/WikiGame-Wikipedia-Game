package com.yoavfranco.wikigame.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.utils.Consts;
import com.yoavfranco.wikigame.utils.SearchItem;

import java.util.ArrayList;
import java.util.List;

public class SearchItemsAdapter extends RecyclerView.Adapter<SearchItemsAdapter.MyViewHolder> {

    private List<SearchItem> searchItems;
    Activity mActivity;
    String type;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle;
        public TextView tvDescription;
        public ButtonBarLayout itemView;

        public MyViewHolder(View view) {
            super(view);
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            tvDescription = (TextView) view.findViewById(R.id.tvDescription);
            itemView = (ButtonBarLayout) view.findViewById(R.id.itemView);
        }
    }


    public SearchItemsAdapter(Activity activity, ArrayList<SearchItem> items, String type) {
        // WARNING: the following line creates a shallow copy of the ArrayList, meaning that if the list is updated, one must call swap().
        // not only notifyDataSetChanged().
        this.searchItems = new ArrayList<>(items);
        this.mActivity = activity;
        this.type = type;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_item, parent, false);


        return new MyViewHolder(itemView);
    }

    public void updateList(List<SearchItem> list){
        searchItems = list;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final SearchItem searchItem = this.searchItems.get(position);

        holder.tvTitle.setText(searchItem.getTitle());
        holder.tvDescription.setText(searchItem.getSubject());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("shouldCloseFragments", false);
                returnIntent.putExtra("type", type);
                returnIntent.putExtra("search_item", searchItem);
                returnIntent.putExtra("updateMode", Consts.UPDATE_MODE_QUICK_PLAY_VALUES_CHANGED);
                mActivity.setResult(1, returnIntent);
                mActivity.finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchItems.size();
    }
}