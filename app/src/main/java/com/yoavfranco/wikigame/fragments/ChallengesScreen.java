package com.yoavfranco.wikigame.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yoavfranco.wikigame.HTTP.WikiGameAPI;
import com.yoavfranco.wikigame.HTTP.WikiGameInterface;
import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.activities.MainActivity;
import com.yoavfranco.wikigame.activities.SearchActivity;
import com.yoavfranco.wikigame.adapters.ChallengeItemAdapter;
import com.yoavfranco.wikigame.adapters.ChallengeTurn;
import com.yoavfranco.wikigame.adapters.SuggestionsAdapter;
import com.yoavfranco.wikigame.utils.Consts;
import com.yoavfranco.wikigame.utils.ErrorDialogs;
import com.yoavfranco.wikigame.utils.Friend;
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

public class ChallengesScreen extends BaseScreen {

    @BindView(R.id.playFriendsListView)
    RecyclerView playableFriendsListView;
    @BindView(R.id.playLayout)
    LinearLayout playLayout;
    ChallengeItemAdapter yourTurnAdapter;
    private ArrayList<Friend> yourTurnFriends;

    @BindView(R.id.suggestionFriendsListView)
    RecyclerView suggestionsFriendsListView;
    @BindView(R.id.suggestionsLayout)
    CardView suggestionsLayout;
    private SuggestionsAdapter suggestionsAdapter;
    private ArrayList<SuggestedFriend> suggestedFriends;

    @BindView(R.id.pendingFriendsListView)
    RecyclerView theirTurnListView;
    @BindView(R.id.pendingLayout)
    LinearLayout theirTurnLayout;
    ChallengeItemAdapter theirTurnAdapter;
    private ArrayList<Friend> theirTurnFriends;

    @BindView(R.id.your_turn_friends_empty_text_view)
    TextView yourTurnEmptyTextView;
    @BindView(R.id.suggestions_empty_text_view)
    TextView suggestionsEmptyTextView;
    @BindView(R.id.their_turn_friends_empty_text_view)
    TextView theirTurnEmptyTextView;

    @BindView(R.id.newChallengeFab)
    FloatingActionButton newChallengeFab;

    TextView yourTurnNumGamesTextView;
    TextView theirTurnNumGamesTextView;

    WikiGameAPI wikiGameAPI;

    SwipeRefreshLayout mSwipeRefreshLayout;

    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.challenges_screen, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (yourTurnFriends == null) {
            yourTurnFriends = new ArrayList<>();
            theirTurnFriends = new ArrayList<>();
            suggestedFriends = new ArrayList<>();
        }

        mainActivity = (MainActivity)getActivity();

        wikiGameAPI = new WikiGameAPI();

        SlideInUpAnimator playableFriendsItemAnimator = new SlideInUpAnimator(new OvershootInterpolator(1f));
        yourTurnAdapter = new ChallengeItemAdapter(this, mainActivity, yourTurnFriends, ChallengeTurn.YourTurn);
        playableFriendsListView.setItemAnimator(playableFriendsItemAnimator);
        playableFriendsListView.setAdapter(yourTurnAdapter);
        playableFriendsListView.setNestedScrollingEnabled(false);

        SlideInUpAnimator theirFriendItemAnimator = new SlideInUpAnimator(new OvershootInterpolator(1f));
        theirTurnAdapter = new ChallengeItemAdapter(this, mainActivity, theirTurnFriends, ChallengeTurn.TheirTurn);
        theirTurnListView.setItemAnimator(theirFriendItemAnimator);
        theirTurnListView.setAdapter(theirTurnAdapter);
        theirTurnListView.setNestedScrollingEnabled(false);

        suggestionsAdapter = new SuggestionsAdapter(suggestedFriends, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false);
        SlideInUpAnimator suggestedFriendItemAnimator = new SlideInUpAnimator(new OvershootInterpolator(1f));
        suggestionsFriendsListView.setItemAnimator(suggestedFriendItemAnimator);
        suggestionsFriendsListView.setLayoutManager(mLayoutManager);
        suggestionsFriendsListView.setAdapter(suggestionsAdapter);

        this.yourTurnNumGamesTextView = (TextView) view.findViewById(R.id.your_turn_num_games_text_view);
        this.theirTurnNumGamesTextView = (TextView) view.findViewById(R.id.their_turn_num_games_text_view);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        refreshItems(false);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems(true);
            }
        });

        updateUI(false);
        initiateFabAnimation(view);
    }

    private void refreshItems(final boolean animated) {

        wikiGameAPI.socialAsync(new WikiGameInterface(mainActivity) {
            @Override
            public void onFinishedProcessingWikiRequest(JSONObject response) {
                try {
                    if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                        SocialInfo socialInfo = SocialInfo.fromJSON(response);


                        suggestedFriends = new ArrayList<>();
                        JSONArray suggestedFriendsObject = response.getJSONArray("suggested_friends");
                        for (int i = 0; i < suggestedFriendsObject.length(); i++) {
                            suggestedFriends.add(SuggestedFriend.fromJSON(suggestedFriendsObject.getJSONObject(i)));
                        }

                        mainActivity.updateSocialInfo(socialInfo, suggestedFriends);
                        mainActivity.populateScreensWithLocalData(); // tell MainActivity to update our own data after sorting

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
        if (playableFriendsListView == null) return; // view is not loaded yet

        yourTurnAdapter.swap(yourTurnFriends, animated);
        theirTurnAdapter.swap(theirTurnFriends, animated);
        suggestionsAdapter.swap(suggestedFriends, animated);

        // TODO: if there are no friends at all, show "no friend to show" text
        suggestionsLayout.setVisibility(View.VISIBLE);
        playLayout.setVisibility(View.VISIBLE);
        theirTurnLayout.setVisibility(View.VISIBLE);

        suggestionsEmptyTextView.setVisibility(suggestedFriends.size() == 0 ? View.VISIBLE : View.GONE);
        yourTurnEmptyTextView.setVisibility(yourTurnFriends.size() == 0 ? View.VISIBLE : View.GONE);
        theirTurnEmptyTextView.setVisibility(theirTurnFriends.size() == 0 ? View.VISIBLE : View.GONE);

        this.yourTurnNumGamesTextView.setText(yourTurnFriends.size() + " games");
        this.theirTurnNumGamesTextView.setText(theirTurnFriends.size() + " games");

        Utils.logInfo(this, "UI updated");
    }

    public void setTheirTurnFriends(ArrayList<Friend> theirTurnFriends) {
        this.theirTurnFriends = theirTurnFriends;
    }

    public void setSuggestedFriends(ArrayList<SuggestedFriend> suggestedFriends) {
        this.suggestedFriends = suggestedFriends;
    }

    public void setYourTurnFriends(ArrayList<Friend>  yourTurnFriends) {
        this.yourTurnFriends = yourTurnFriends;
    }

    private void initiateFabAnimation(View view) {
        newChallengeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction mFragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragment =  new AddFriendsScreen();
                mFragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                mFragmentTransaction.replace(R.id.fragment_container,fragment);
                mFragmentTransaction.addToBackStack(fragment.getTag());
                mFragmentTransaction.commit();
            }
        });

    }

    @Override
    public void clear() {
        if (onClearListener != null)
            onClearListener.clearDone();
    }
}
