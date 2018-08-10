package com.yoavfranco.wikigame.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.utils.LeaderboardItem;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardItemAdapter extends RecyclerView.Adapter<LeaderboardItemAdapter.MyViewHolder> {

    private List<LeaderboardItem> leaderboardItems;
    private Activity context;
    private String playerUsername;
    private int listSize;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView usernameTextView;
        public TextView tvPoints, tvPlace;
        public CircularImageView userCountryImageView;
        public ImageView firstPlacesImageView;
        public RelativeLayout itemView;


        public MyViewHolder(View view) {
            super(view);
            this.usernameTextView = (TextView)view.findViewById(R.id.tvDisplayName);
            this.tvPlace = (TextView)view.findViewById(R.id.tvPlace);
            this.tvPoints = (TextView)view.findViewById(R.id.tvPoints);
            this.userCountryImageView = (CircularImageView)view.findViewById(R.id.ciCountry);
            this.firstPlacesImageView = (ImageView)view.findViewById(R.id.ivFirstPlaces);
            this.itemView = (RelativeLayout)view.findViewById(R.id.itemView);
        }
    }

    public void swap(ArrayList<LeaderboardItem> newData) {
        swap(newData, this.leaderboardItems.size() != 0);
    }

    public void swap(ArrayList<LeaderboardItem> newData, boolean animated) {
        if (animated) {
            removeAll();
            addAll(newData);
        } else {
            this.leaderboardItems.clear();
            this.leaderboardItems.addAll(newData);
            this.listSize = this.leaderboardItems.size();
            notifyDataSetChanged();
        }
    }

    public LeaderboardItemAdapter(Activity activity, List<LeaderboardItem> items, String playerUsername) {
        // WARNING: the following line creates a shallow copy of the ArrayList, meaning that if the list is updated, one must call swap().
        // not only notifyDataSetChanged().
        this.leaderboardItems = new ArrayList<>(items);
        this.playerUsername = playerUsername;
        this.context = activity;
        this.listSize = leaderboardItems.size();
    }

    private void addAll(List<LeaderboardItem> suggestionsFriendsList)
    {
        for (int i = 0; i < suggestionsFriendsList.size(); i++) {
            addItem(i, suggestionsFriendsList.get(i));
        }
        this.listSize = leaderboardItems.size();
    }

    private void removeAll()
    {
        leaderboardItems.clear();
        notifyItemRangeRemoved(0, listSize);
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.leaderboards_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        LeaderboardItem item = leaderboardItems.get(position);

        holder.tvPlace.setText(item.getRank());
        if (position < -1) {
            holder.firstPlacesImageView.setVisibility(View.VISIBLE);
            if (position == 0)
                holder.firstPlacesImageView.setImageResource(R.drawable.goldmedal);
            else if (position == 1)
                holder.firstPlacesImageView.setImageResource(R.drawable.silvermedal);
            else
                holder.firstPlacesImageView.setImageResource(R.drawable.bronzemedal);
        }

        boolean isMe = item.getDisplayName().equals(playerUsername);
        if (isMe)
            holder.itemView.setBackgroundColor(Color.parseColor("#bdc3c7"));
        else
            holder.itemView.setBackgroundColor(Color.WHITE);
        holder.usernameTextView.setText(isMe ? "Me" : item.getDisplayName());

        holder.usernameTextView.setText(item.getDisplayName().equals(playerUsername) ? "Me" : item.getDisplayName());
        holder.tvPoints.setText(item.getPointsText());
        holder.firstPlacesImageView.setVisibility(View.GONE);
        Picasso.with(context).load(item.getCountryFlag()).fit().placeholder(R.drawable.progress_animation).into(holder.userCountryImageView);

    }

    private int lastPosition = -1;

    private void slideLeft(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public void addItem(int position, LeaderboardItem leaderboardItem) {
        leaderboardItems.add(position, leaderboardItem);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        leaderboardItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return leaderboardItems.size();
    }
}