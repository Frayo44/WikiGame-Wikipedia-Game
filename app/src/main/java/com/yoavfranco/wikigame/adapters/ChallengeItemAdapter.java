package com.yoavfranco.wikigame.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.fragments.BaseScreen;
import com.yoavfranco.wikigame.fragments.ChallengesScreen;
import com.yoavfranco.wikigame.utils.Challenge;
import com.yoavfranco.wikigame.utils.Friend;
import com.yoavfranco.wikigame.utils.LeaderboardItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChallengeItemAdapter extends RecyclerView.Adapter<ChallengeItemAdapter.MyViewHolder> {

    private List<Friend> friendsItems;
    Activity mContext;
    private ChallengeTurn turn;
    BaseScreen baseScreen;
    private int listSize;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView challengerUsernameTextView;
        public TextView timeAgoTextView;
        public TextView stateTextView;
        public TextView balanceTextView;
        public CircularImageView countryImageView;
        public ButtonBarLayout itemLayout;


        public MyViewHolder(View view) {
            super(view);
            challengerUsernameTextView = (TextView) view.findViewById(R.id.tvChallengerName);
            timeAgoTextView = (TextView) view.findViewById(R.id.tvTimeAgo);
            stateTextView = (TextView) view.findViewById(R.id.tvState);
            balanceTextView = (TextView) view.findViewById(R.id.tvEqaultiy);
            countryImageView = (CircularImageView) view.findViewById(R.id.ciCountry);
            itemLayout = (ButtonBarLayout) view.findViewById(R.id.itemLayout);

        }
    }

   /* public void swap(ArrayList<Friend> newData) {
        removeAll();
        addAll(newData);
        notifyDataSetChanged();
    } */

    public void swap(ArrayList<Friend> newData) {
        swap(newData, this.friendsItems.size() != 0);
    }

    public void swap(ArrayList<Friend> newData, boolean animated) {
        if (animated) {
            removeAll();
            addAll(newData);
        } else {
            this.friendsItems.clear();
            this.friendsItems.addAll(newData);
            this.listSize = this.friendsItems.size();
            notifyDataSetChanged();
        }
    }

    private void addAll(List<Friend> suggestionsFriendsList)
    {
        for (int i = 0; i < suggestionsFriendsList.size(); i++) {
            addItem(i, suggestionsFriendsList.get(i));
        }
        this.listSize = friendsItems.size();
    }

    private void removeAll()
    {
        friendsItems.clear();
        notifyItemRangeRemoved(0, listSize);
    }

    public ChallengeItemAdapter(BaseScreen baseScreen, Activity activity, ArrayList<Friend> friends, ChallengeTurn turn) {
        // WARNING: the following line creates a shallow copy of the ArrayList, meaning that if the list is updated, one must call swap().
        // not only notifyDataSetChanged().
        this.friendsItems = new ArrayList<>(friends);
        this.mContext = activity;
        this.turn = turn;
        this.listSize = friendsItems.size();
        this.baseScreen = baseScreen;
    }

    public void addItem(int position, Friend leaderboardItem) {
        friendsItems.add(position, leaderboardItem);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        friendsItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.challenge_item, parent, false);


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Friend friend = friendsItems.get(position);
        Challenge challenge = friend.getChallenge();
        switch (turn) {
            case YourTurn:
                holder.stateTextView.setText(challenge != null ? "   Play   " : "  Challenge!  ");
                holder.stateTextView.setAlpha(1);
                break;
            case TheirTurn:
                holder.stateTextView.setText("Their Turn");
                holder.itemLayout.setClickable(false);
                break;
            default:
                holder.stateTextView.setText("Challenge!");
                break;
        }

        if (challenge != null)
            holder.timeAgoTextView.setText(DateUtils.getRelativeTimeSpanString(challenge.getSentTime().getTime()));
        else {
            holder.timeAgoTextView.setText("Wasn't sent yet");
        }
        holder.challengerUsernameTextView.setText(friend.getUsername());
        Picasso.with(mContext).load(friend.getFlagURL()).fit().placeholder(R.drawable.progress_animation).into(holder.countryImageView);

        int state = 0; // 0 -> equal, 1 - > me wins, 2 -> friend wins
        if (friend.getLooses() > friend.getVictories())
            state = 1;
        else if (friend.getLooses() < friend.getVictories())
            state = 2;


        String balanceString = friend.getVictories() + " : " + friend.getLooses();
        final SpannableStringBuilder str = new SpannableStringBuilder(balanceString);
        switch (state) {
            case 1: {
                str.setSpan(new ForegroundColorSpan(Color.WHITE), balanceString.indexOf(':') + 1, balanceString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            break;
            case 2: {
                str.setSpan(new ForegroundColorSpan(Color.WHITE), 0, balanceString.indexOf(':'), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            break;
        }

        holder.balanceTextView.setText(str, TextView.BufferType.SPANNABLE);

        if (turn == ChallengeTurn.YourTurn)
            holder.itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent extra = new Intent();
                    extra.putExtra("friend", friend);
                    BaseScreen.Action nextAction = friend.getChallenge() != null ? BaseScreen.Action.CHALLENGE_QUICKPLAY : BaseScreen.Action.CHOOSE_MODE_CHALLENGE;
                    ((ChallengesScreen) baseScreen).screenChanger.onScreenChange(((ChallengesScreen) baseScreen), nextAction, extra);
                }
            });
    }

    @Override
    public int getItemCount() {
        return friendsItems.size();
    }
}