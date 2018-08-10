package com.yoavfranco.wikigame.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.yoavfranco.wikigame.HTTP.WikiGameAPI;
import com.yoavfranco.wikigame.HTTP.WikiGameInterface;
import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.utils.Challenge;
import com.yoavfranco.wikigame.utils.Consts;
import com.yoavfranco.wikigame.utils.ErrorDialogs;
import com.yoavfranco.wikigame.utils.Friend;
import com.yoavfranco.wikigame.utils.FriendRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestItemAdapter extends RecyclerView.Adapter<FriendRequestItemAdapter.MyViewHolder> {

    private List<FriendRequest> friendRequests;
    Activity mContext;
    FriendRequestType friendRequestType;
    FriendRequestAdapterInterface parent;
    private int listSize;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView usernameTextView;
        public TextView tvHasntResponded, stateTextView;
        public Button confirmButton, ignoreButton;
        public CircularImageView userCountryImageView;
        public TextView timeAgoTextView;

        public MyViewHolder(View view) {
            super(view);
            usernameTextView = (TextView) view.findViewById(R.id.tvChallengerName);
            userCountryImageView = (CircularImageView) view.findViewById(R.id.ciCountry);
            confirmButton = (Button) view.findViewById(R.id.bConfirm);
            ignoreButton = (Button) view.findViewById(R.id.bIgnore);
            timeAgoTextView = (TextView) view.findViewById(R.id.tvTimeAgo);
            tvHasntResponded = (TextView) view.findViewById(R.id.tvHasntResponded);
            stateTextView = (TextView) view.findViewById(R.id.tvFriendState);
        }
    }



    public void swap(ArrayList<FriendRequest> newData) {
        swap(newData, this.friendRequests.size() != 0);
    }

    public void swap(ArrayList<FriendRequest> newData, boolean animated) {
        if (animated) {
            removeAll();
            addAll(newData);
        } else {
            this.friendRequests.clear();
            this.friendRequests.addAll(newData);
            this.listSize = this.friendRequests.size();
            notifyDataSetChanged();
        }
    }

    private void addAll(List<FriendRequest> suggestionsFriendsList)
    {
        for (int i = 0; i < suggestionsFriendsList.size(); i++) {
            addItem(i, suggestionsFriendsList.get(i));
        }
        this.listSize = friendRequests.size();
    }

    private void removeAll()
    {
        friendRequests.clear();
        notifyItemRangeRemoved(0, listSize);
    }

    public void addItem(int position, FriendRequest leaderboardItem) {
        friendRequests.add(position, leaderboardItem);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        friendRequests.remove(position);
        notifyItemRemoved(position);
    }


    public FriendRequestItemAdapter(Activity activity, List<FriendRequest> suggestionsFriendsList, FriendRequestType friendRequestType, FriendRequestAdapterInterface parent) {
        // WARNING: the following line creates a shallow copy of the ArrayList, meaning that if the list is updated, one must call swap().
        // not only notifyDataSetChanged().
        this.friendRequests = new ArrayList<>(suggestionsFriendsList);
        this.mContext = activity;
        this.friendRequestType = friendRequestType;
        this.parent = parent;
        this.listSize = suggestionsFriendsList.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_request_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final FriendRequest friendRequest = friendRequests.get(position);

        holder.timeAgoTextView.setText(DateUtils.getRelativeTimeSpanString(friendRequest.getSentTime().getTime()));
        Picasso.with(holder.userCountryImageView.getContext()).load(friendRequest.getFlagURL()).fit().placeholder(R.drawable.progress_animation).into(holder.userCountryImageView);
        if (friendRequestType == FriendRequestType.Sent) {
            holder.confirmButton.setVisibility(View.GONE);
            holder.ignoreButton.setVisibility(View.GONE);
            holder.usernameTextView.setText(friendRequest.getReceiverUsername());
            holder.tvHasntResponded.setVisibility(View.VISIBLE);
        } else if (friendRequestType == FriendRequestType.Pending){
            holder.tvHasntResponded.setVisibility(View.GONE);
            holder.usernameTextView.setText(friendRequest.getSenderUsername());
        }

        final ScaleAnimation scale = (ScaleAnimation) AnimationUtils.loadAnimation(mContext, R.anim.shrink);
        scale.setInterpolator(new AnticipateInterpolator());
        scale.setFillAfter(true);
        scale.setDuration(300);

        scale.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
            //    holder.stateTextView.setText("");
                holder.stateTextView.setVisibility(View.VISIBLE);
                holder.stateTextView.animate().alpha(0.7f).setDuration(200);
                holder.confirmButton.setVisibility(View.GONE);
                holder.ignoreButton.setVisibility(View.GONE);
            }
        });


        holder.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.confirmButton.setAnimation(scale);
                holder.confirmButton.startAnimation(scale);
                holder.ignoreButton.setAnimation(scale);
                holder.ignoreButton.startAnimation(scale);
                // TODO: Remove item with animation
                // TODO: Loading animation
                WikiGameAPI wikiGameAPI = new WikiGameAPI();
                wikiGameAPI.acceptFriendRequestAsync(friendRequest.getSenderUsername(), new WikiGameInterface(mContext) {
                    @Override
                    public void onFinishedProcessingWikiRequest(JSONObject response) {
                        try {
                            if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                                Challenge newChallenge = response.has("new_challenge") ? Challenge.fromJSON(response.getJSONObject("new_challenge")) : null;
                                Friend newFriend = new Friend(friendRequest.getSenderUsername(), friendRequest.getCountryCode(), friendRequest.getFlagURL());
                                newFriend.setChallenge(newChallenge);

                                holder.stateTextView.setText("Friend successfully added");
                                // friendRequests.remove(position);
                                // notifyDataSetChanged();
                                //parent.onFriendRequestRemoved(friendRequest, true, newFriend);
                            } else {
                                holder.stateTextView.setText("Failed to confirm friend");
                            }
                        } catch (JSONException e) {
                            ErrorDialogs.showBadResponseDialog(getActivityContext(), false);
                            e.printStackTrace();
                        }

                    }
                });

            }
        });

        holder.ignoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.confirmButton.setAnimation(scale);
                holder.confirmButton.startAnimation(scale);
                holder.ignoreButton.setAnimation(scale);
                holder.ignoreButton.startAnimation(scale);
                // TODO: Remove item with animation
                // TODO: Loading animation
                final FriendRequest friendRequest = friendRequests.get(position);
                WikiGameAPI wikiGameAPI = new WikiGameAPI();
                wikiGameAPI.ignoreFriendRequestAsync(friendRequests.get(position).getSenderUsername(), new WikiGameInterface(mContext) {
                    @Override
                    public void onFinishedProcessingWikiRequest(JSONObject response) {
                        try {
                            if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                                holder.stateTextView.setText("Friend successfully ignored");
                                parent.onFriendRequestRemoved(friendRequest, false, null);
                            } else {
                                holder.stateTextView.setText("Failed to ignore friend");
                            }
                        } catch (JSONException e) {
                            holder.stateTextView.setText("Failed to ignore friend");
                            ErrorDialogs.showBadResponseDialog(getActivityContext(), false);
                            e.printStackTrace();
                        }

                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendRequests.size();
    }
}