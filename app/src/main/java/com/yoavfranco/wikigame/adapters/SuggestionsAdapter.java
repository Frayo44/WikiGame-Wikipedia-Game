package com.yoavfranco.wikigame.adapters;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.fragments.AddFriendsScreen;
import com.yoavfranco.wikigame.fragments.BaseScreen;
import com.yoavfranco.wikigame.utils.Friend;
import com.yoavfranco.wikigame.utils.SuggestedFriend;

import java.util.ArrayList;
import java.util.List;

public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.MyViewHolder> {
 
    private List<SuggestedFriend> suggestedFriends;
    private BaseScreen baseScreen;
    private int listSize;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView usernameTextView;
        public CircularImageView userCountryImageView;
        public ButtonBarLayout itemView;

        public MyViewHolder(View view) {
            super(view);
            usernameTextView = (TextView) view.findViewById(R.id.tvDisplayName);
            userCountryImageView = (CircularImageView) view.findViewById(R.id.ciCountry);
            itemView = (ButtonBarLayout) view.findViewById(R.id.suggestedItemView);
        }
    }

    public void swap(ArrayList<SuggestedFriend> newData) {
        swap(newData, this.suggestedFriends.size() != 0);
    }

    public void swap(ArrayList<SuggestedFriend> newData, boolean animated) {
        if (animated) {
            removeAll();
            addAll(newData);
        } else {
            this.suggestedFriends.clear();
            this.suggestedFriends.addAll(newData);
            this.listSize = this.suggestedFriends.size();
            notifyDataSetChanged();
        }
    }

    private void addAll(List<SuggestedFriend> suggestionsFriendsList)
    {
        for (int i = 0; i < suggestionsFriendsList.size(); i++) {
            addItem(i, suggestionsFriendsList.get(i));
        }
        this.listSize = suggestedFriends.size();
    }

    private void removeAll()
    {
        suggestedFriends.clear();
        notifyItemRangeRemoved(0, listSize);
    }

    public void addItem(int position, SuggestedFriend leaderboardItem) {
        suggestedFriends.add(position, leaderboardItem);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        suggestedFriends.remove(position);
        notifyItemRemoved(position);
    }
 
    public SuggestionsAdapter(List<SuggestedFriend> suggestionsFriendsList, BaseScreen baseScreen) {
        this.suggestedFriends = new ArrayList<>(suggestionsFriendsList); // shallow copy
        this.baseScreen = baseScreen;
        this.listSize = suggestionsFriendsList.size();
    }
 
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.suggestion_item, parent, false);
 
        return new MyViewHolder(itemView);
    }
 
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final SuggestedFriend friend = suggestedFriends.get(position);
        holder.usernameTextView.setText(friend.getUsername());
        Picasso.with(holder.userCountryImageView.getContext()).load(friend.getFlagURL()).fit().placeholder(R.drawable.progress_animation).into(holder.userCountryImageView);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction mFragmentTransaction = baseScreen.getActivity().getFragmentManager().beginTransaction();
                Fragment fragment =  new AddFriendsScreen();
                Bundle bundle = new Bundle();
                bundle.putSerializable("suggested_friend", friend);
                fragment.setArguments(bundle);
                mFragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                mFragmentTransaction.replace(R.id.fragment_container,fragment);
                mFragmentTransaction.addToBackStack(fragment.getTag());
                mFragmentTransaction.commit();
            }
        });
    }
 
    @Override
    public int getItemCount() {
        return suggestedFriends.size();
    }
}