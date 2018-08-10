package com.yoavfranco.wikigame.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yoavfranco.wikigame.HTTP.WikiGameAPI;
import com.yoavfranco.wikigame.HTTP.WikiGameInterface;
import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.activities.MainActivity;
import com.yoavfranco.wikigame.adapters.FriendRequestAdapterInterface;
import com.yoavfranco.wikigame.adapters.FriendRequestType;
import com.yoavfranco.wikigame.adapters.FriendRequestItemAdapter;
import com.yoavfranco.wikigame.adapters.SuggestionsAdapter;
import com.yoavfranco.wikigame.utils.Consts;
import com.yoavfranco.wikigame.utils.ErrorDialogs;
import com.yoavfranco.wikigame.utils.Friend;
import com.yoavfranco.wikigame.utils.FriendRequest;
import com.yoavfranco.wikigame.utils.SocialInfo;
import com.yoavfranco.wikigame.utils.SuggestedFriend;
import com.yoavfranco.wikigame.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class FriendsScreen extends BaseScreen implements FriendRequestAdapterInterface {

    @BindView(R.id.pending_friend_requests_list_view)
    RecyclerView pendingFriendRequestsListView;
    private FriendRequestItemAdapter pendingFriendsAdapter;
    private ArrayList<FriendRequest> pendingFriendRequests;

    @BindView(R.id.suggestionFriendsListView)
    RecyclerView suggestionsFriendsListView;
    private SuggestionsAdapter suggestedFriendsAdapter;
    private ArrayList<SuggestedFriend> suggestedFriends;

    @BindView(R.id.sent_friend_requests_list_view)
    RecyclerView sentFriendRequestsListView;
    private FriendRequestItemAdapter sentRequestsAdapter;
    private ArrayList<FriendRequest> sentFriendRequests;

    @BindView(R.id.add_friend_button)
    FloatingActionButton addFriendButton;

    TextView numPendingRequestsTextView;
    TextView numSentRequestsTextView;
    TextView numSuggestedFriendsTextView;

    @BindView(R.id.pending_friend_requests_empty_view)
    TextView pendingFriendRequestsEmtpyTextView;
    @BindView(R.id.sent_friend_requests_empty_text_view)
    TextView sentFriendRequestsEmptyTextView;
    @BindView(R.id.suggestions_empty_text_view)
    TextView friendSuggestionsEmptyTextView;

    SwipeRefreshLayout mSwipeRefreshLayout;

    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.friends_screen, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (pendingFriendRequests == null) {
            pendingFriendRequests = new ArrayList<>();
            sentFriendRequests = new ArrayList<>();
            suggestedFriends = new ArrayList<>();
        }

        mainActivity = (MainActivity)getActivity();

        SlideInUpAnimator pendingFriendsRequestsItemAnimator = new SlideInUpAnimator(new OvershootInterpolator(1f));
        pendingFriendRequestsListView.setItemAnimator(pendingFriendsRequestsItemAnimator);
        pendingFriendsAdapter = new FriendRequestItemAdapter(mainActivity, pendingFriendRequests, FriendRequestType.Pending, this);
        pendingFriendRequestsListView.setAdapter(pendingFriendsAdapter);
        pendingFriendRequestsListView.setNestedScrollingEnabled(false);

        SlideInUpAnimator sentFriendsRequestsItemAnimator = new SlideInUpAnimator(new OvershootInterpolator(1f));
        sentFriendRequestsListView.setItemAnimator(sentFriendsRequestsItemAnimator);
        sentRequestsAdapter = new FriendRequestItemAdapter(mainActivity, pendingFriendRequests, FriendRequestType.Sent, this);
        sentFriendRequestsListView.setAdapter(sentRequestsAdapter);
        sentFriendRequestsListView.setNestedScrollingEnabled(false);

        suggestedFriendsAdapter = new SuggestionsAdapter(suggestedFriends, this);
        SlideInUpAnimator suggestedFriendItemAnimator = new SlideInUpAnimator(new OvershootInterpolator(1f));
        suggestionsFriendsListView.setItemAnimator(suggestedFriendItemAnimator);
        suggestionsFriendsListView.setAdapter(suggestedFriendsAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false);
        suggestionsFriendsListView.setLayoutManager(mLayoutManager);

        this.numPendingRequestsTextView = (TextView) view.findViewById(R.id.num_pending_friend_requests_text_view);
        this.numSentRequestsTextView = (TextView) view.findViewById(R.id.num_sent_friend_requests_text_view);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        updateUI(false);
        initiateFabAnimation(view);
        refreshItems(false);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems(true);
            }
        });

    }

    private void refreshItems(final boolean animated) {

        WikiGameAPI wikiGameAPI = new WikiGameAPI();
        wikiGameAPI.socialAsync(new WikiGameInterface(mainActivity) {
            @Override
            public void onFinishedProcessingWikiRequest(JSONObject response) {
                try {
                    if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                        SocialInfo socialInfo = SocialInfo.fromJSON(response);

                        pendingFriendRequests = socialInfo.getPendingFriendRequests();
                        sentFriendRequests = socialInfo.getSentFriendRequests();

                        suggestedFriends = new ArrayList<>();
                        JSONArray suggestedFriendsObject = response.getJSONArray("suggested_friends");
                        for (int i = 0; i < suggestedFriendsObject.length(); i++) {
                            suggestedFriends.add(SuggestedFriend.fromJSON(suggestedFriendsObject.getJSONObject(i)));
                        }

                        mainActivity.updateSocialInfo(socialInfo, suggestedFriends);

                        updateUI(animated);
                        onItemsLoadComplete();
                    } else {
                        ErrorDialogs.showSomethingWentWrongDialog(mainActivity, true);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
                catch (JSONException e) {
                    // JSON parsing failed.
                    ErrorDialogs.showBadResponseDialog(mainActivity, true);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailedMakingWikiRequest(WikiError errorCause) {
                ErrorDialogs.showNetworkErrorDialog(getActivityContext(), false);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void updateUI(boolean animated)
    {
        if (getView() == null) return; // view is not loaded yet

        pendingFriendsAdapter.swap(pendingFriendRequests, animated);
        sentRequestsAdapter.swap(sentFriendRequests, animated);
        suggestedFriendsAdapter.swap(suggestedFriends, animated);

        CardView suggestionsLayout = (CardView) getView().findViewById(R.id.suggestionsLayout);
        suggestionsLayout.setVisibility(View.VISIBLE);
        LinearLayout playLayout = (LinearLayout) getView().findViewById(R.id.pending_friend_requests_layout);
        playLayout.setVisibility(View.VISIBLE);
        LinearLayout pendingLayout = (LinearLayout) getView().findViewById(R.id.sent_friend_requests_layout);
        pendingLayout.setVisibility(View.VISIBLE);

        friendSuggestionsEmptyTextView.setVisibility(suggestedFriends.size() == 0 ? View.VISIBLE : View.GONE);
        pendingFriendRequestsEmtpyTextView.setVisibility(pendingFriendRequests.size() == 0 ? View.VISIBLE : View.GONE);
        sentFriendRequestsEmptyTextView.setVisibility(sentFriendRequests.size() == 0 ? View.VISIBLE : View.GONE);

        this.numPendingRequestsTextView.setText(pendingFriendRequests.size() + " requests");
        this.numSentRequestsTextView.setText(sentFriendRequests.size() + " requests");

        Utils.logInfo(this, "UI updated");
    }

    @Override
    public void onFriendRequestRemoved(FriendRequest friendRequest, boolean isConfirmed, @Nullable Friend newFriend) {
        this.pendingFriendRequests.remove(friendRequest);
        mainActivity.updateFriendsData(pendingFriendRequests, sentFriendRequests, suggestedFriends);
        if (isConfirmed) {
            mainActivity.addFriend(newFriend);
        }

        //this.updateUI(false);
    }

    public void setSentFriendRequests(ArrayList<FriendRequest> sentFriendRequests) {
        this.sentFriendRequests = sentFriendRequests;
    }

    public void setSuggestedFriends(ArrayList<SuggestedFriend> suggestedFriends) {
        this.suggestedFriends = suggestedFriends;
    }

    public void setPendingFriendRequests(ArrayList<FriendRequest> pendingFriendRequests) {
        this.pendingFriendRequests = pendingFriendRequests;
    }

    private void initiateFabAnimation(View view) {
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction mFragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragment =  new AddFriendsScreen();
                mFragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                mFragmentTransaction.replace(R.id.fragment_container,fragment);
                mFragmentTransaction.addToBackStack(fragment.getTag()
                );
                mFragmentTransaction.commit();
            }
        });

    }

    @Override
    public void clear() {

    }
}
