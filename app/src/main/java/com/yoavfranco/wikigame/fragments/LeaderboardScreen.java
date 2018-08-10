package com.yoavfranco.wikigame.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yoavfranco.wikigame.HTTP.WikiGameAPI;
import com.yoavfranco.wikigame.HTTP.WikiGameInterface;
import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.activities.MainActivity;
import com.yoavfranco.wikigame.adapters.LeaderboardItemAdapter;
import com.yoavfranco.wikigame.utils.Consts;
import com.yoavfranco.wikigame.utils.ErrorDialogs;
import com.yoavfranco.wikigame.utils.LeaderboardItem;
import com.yoavfranco.wikigame.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * Created by yoav on 02/03/17.
 */

public class LeaderboardScreen extends BaseScreen {

    @BindView(R.id.pendingFriendsListView)
    RecyclerView itemsListView;
    TextView myRankTextView;
    WikiGameAPI wikiGameAPI;
    @BindView(R.id.progressBarLayout)
    RelativeLayout progressBarLayout;

    LeaderboardItemAdapter leaderboardItemAdapter;
    ArrayList<LeaderboardItem> leaderboardItems;
    int myRank;
    private SwipeRefreshLayout swipeToRefreshLayout;

    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.leaderboards_screen, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.myRankTextView = (TextView)view.findViewById(R.id.your_rank_text_view);
        ButterKnife.bind(this, view);

        if (leaderboardItems == null) {
            leaderboardItems = new ArrayList<>();
            myRank = -1;
        }

        mainActivity = (MainActivity)getActivity();

        wikiGameAPI = new WikiGameAPI();

        if(progressBarLayout.getVisibility() == View.GONE)
            progressBarLayout.setVisibility(View.VISIBLE);

        leaderboardItemAdapter = new LeaderboardItemAdapter(mainActivity, leaderboardItems, mainActivity.getUserInfo().getUsername());
        itemsListView.setAdapter(leaderboardItemAdapter);
        itemsListView.setNestedScrollingEnabled(false);
        SlideInUpAnimator animator = new SlideInUpAnimator(new OvershootInterpolator(1f));
        itemsListView.setItemAnimator(animator);
        swipeToRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        refreshItems(false);

        swipeToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeToRefreshLayout.setRefreshing(true);
                refreshItems(true);
            }
        });

        updateUI(false);
    }

    private void refreshItems(final boolean animated) {

        wikiGameAPI.leaderboardsAsync(new WikiGameInterface(mainActivity) {
            @Override
            public void onFinishedProcessingWikiRequest(JSONObject response) {
                try {
                    if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                        leaderboardItems = new ArrayList<>();
                        JSONArray leaderboards = response.getJSONArray("leaderboards");
                        for (int i = 0; i < leaderboards.length(); i++) {
                            JSONObject record = leaderboards.getJSONObject(i);
                            String username = record.getString("username");
                            String pointsText = record.getString("points");
                            String countryFlagPath = record.has("country_flag") ? record.getString("country_flag") : "/flags/default_flag.png";
                            String countryCode = record.has("country_code") ? record.getString("country_code") : "il";
                            countryFlagPath = Utils.toServerURL(countryFlagPath);
                            LeaderboardItem member = new LeaderboardItem((i + 1) + "", username, pointsText, countryFlagPath, countryCode);
                            myRank = response.getInt("my_position");
                            leaderboardItems.add(member);
                        }
                        updateUI(animated);
                        onItemsLoadComplete();
                    } else {
                        ErrorDialogs.showSomethingWentWrongDialog(mainActivity, true);
                        swipeToRefreshLayout.setRefreshing(false);
                    }
                } catch (JSONException e) {
                    ErrorDialogs.showBadResponseDialog(mainActivity, false);
                    swipeToRefreshLayout.setRefreshing(false);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailedMakingWikiRequest(WikiError errorCause) {
                ErrorDialogs.showNetworkErrorDialog(getActivityContext(), false);
                swipeToRefreshLayout.setRefreshing(false);
            }
        });
    }


    void onItemsLoadComplete() {
        swipeToRefreshLayout.setRefreshing(false);
        if (progressBarLayout.getVisibility() == View.VISIBLE)
            progressBarLayout.setVisibility(View.GONE);
    }

    public void updateUI(boolean animated) {
        leaderboardItemAdapter.swap(leaderboardItems, animated);
        myRankTextView.setText("You're #" + myRank);
    }

    @Override
    public void clear() {
    }
}
