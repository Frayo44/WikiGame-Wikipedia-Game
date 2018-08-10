package com.yoavfranco.wikigame.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yoavfranco.wikigame.HTTP.WikiGameAPI;
import com.yoavfranco.wikigame.HTTP.WikiGameInterface;
import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.activities.MainActivity;
import com.yoavfranco.wikigame.activities.WikiDisplayActivity;
import com.yoavfranco.wikigame.activities.SearchActivity;
import com.yoavfranco.wikigame.utils.Challenge;
import com.yoavfranco.wikigame.utils.Consts;
import com.yoavfranco.wikigame.utils.ErrorDialogs;
import com.yoavfranco.wikigame.utils.Friend;
import com.yoavfranco.wikigame.utils.Level;
import com.yoavfranco.wikigame.utils.SearchItem;
import com.yoavfranco.wikigame.views.RoundButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class ChallengeQuickPlayScreen extends BaseScreen {

    @BindViews({R.id.play_now, R.id.reset})
    RoundButton[] buttons;
    @BindView(R.id.tvStartWithInp)
    TextView startArticleTextView;
    @BindView(R.id.tvEndWithInp)
    TextView targetArticleTextView;
    @BindView(R.id.iv_arrow)
    AppCompatImageView arrow;
    @BindView(R.id.tvBestScoreClicks)
    TextView bestScoreClicksTextView;
    @BindView(R.id.tvBestTime)
    TextView bestScoreTimeTextView;
    @BindView(R.id.best_score_wrapper_clicks)
    LinearLayout clicksLinearLayout;
    @BindView(R.id.best_score_wrapper_time)
    LinearLayout timeLinearLayout;
    WikiGameAPI wikiGameAPI;
    @BindView(R.id.icArrowTop)
    AppCompatImageView icArrowTop;
    @BindView(R.id.icArrowBottom)
    AppCompatImageView icArrowBottom;

    private Friend friend;

    private String startArticleSubject;
    private String targetArticleSubject;
    private String serverStartArticle;
    private String serverTargetArticle;
    private String startArticle;
    private String targetArticle;

    private boolean isShuffleAnimationPaused;
    private int shuffleAnimationInterval = 70;
    private int shuffleAnimationTime;
    private int shuffleAnimationTickCounter;
    private String selectedMode;

    @BindView(R.id.tvStartSubject)
    TextView tvStartSubject;
    @BindView(R.id.tvTargetSubject)
    TextView tvTargetSubject;


    boolean shuffleAnimationTimerStarted = false;
    Handler handler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int maximumAnimationTime = 2000;
            onShuffleAnimationTimerTick(shuffleAnimationInterval, maximumAnimationTime);

            if (shuffleAnimationTimerStarted) {
                startShuffleAnimationTimer();
            }
        }
    };

    public void stopShuffleAnimationTimer() {
        shuffleAnimationTimerStarted = false;
        handler.removeCallbacks(runnable);
    }

    public void startShuffleAnimationTimer() {
        shuffleAnimationTimerStarted = true;

        handler.postDelayed(runnable, shuffleAnimationInterval);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.challenge_quickplay_screen, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        this.isShuffleAnimationPaused = false;
        wikiGameAPI = new WikiGameAPI();

        animateArrow();
        updateUI();

        if (isCreatingNewChallenge())
            shuffleArticles();

        // play button
        buttons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlayButtonClicked();
            }
        });

        // reshuffle button
        buttons[1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onShuffleButtonClicked(this);
                }
            });
    }

    private void openSearchArticlesIntent(String type) {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra("type", type);
        startActivityForResult(intent, 1);
    }


    private void switchVisiblityState(boolean in) {
        if(!in) {
            icArrowTop.setVisibility(View.INVISIBLE);
            icArrowTop.animate().alpha(0.0f).setDuration(200);
            icArrowBottom.setVisibility(View.INVISIBLE);
            icArrowBottom.animate().alpha(0.0f).setDuration(200);
            tvTargetSubject.setVisibility(View.INVISIBLE);
            tvTargetSubject.animate().alpha(0.0f).setDuration(200);
            tvStartSubject.setVisibility(View.INVISIBLE);
            tvStartSubject.animate().alpha(0.0f).setDuration(200);
        } else {
            icArrowTop.setVisibility(View.VISIBLE);
            icArrowTop.animate().alpha(1.0f).setDuration(200);
            icArrowBottom.setVisibility(View.VISIBLE);
            icArrowBottom.animate().alpha(1.0f).setDuration(200);
            tvTargetSubject.setVisibility(View.VISIBLE);
            tvTargetSubject.animate().alpha(1.0f).setDuration(200);
            tvStartSubject.setVisibility(View.VISIBLE);
            tvStartSubject.animate().alpha(1.0f).setDuration(200);
        }
    }

    private void onShuffleButtonClicked(View.OnClickListener onClickListener) {
        switchVisiblityState(false);
        buttons[1].animateClick(onClickListener);
        shuffleArticles();
    }

    private void onPlayButtonClicked() {
        buttons[0].setClickable(false);
        if (startArticle == null) {
            ErrorDialogs.showNetworkErrorDialog(getActivity(), false);
        }
        if (!isCreatingNewChallenge()) {
            // we're answering a challenge
            wikiGameAPI.tryChallengeAsync(friend.getUsername(), new WikiGameInterface(getActivity()) {
                @Override
                public void onFinishedProcessingWikiRequest(JSONObject response) {
                    try {
                        if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                            startChallengeGame();
                        }
                        else {
                            ErrorDialogs.showBadResponseDialog(getActivityContext(), false);
                        }
                    }
                    catch (JSONException e) {
                        ErrorDialogs.showBadResponseDialog(getActivityContext(), false);
                        e.printStackTrace();
                    }
                }
            });

        } else if (isCreatingNewChallenge()) {
            if (!serverStartArticle.equals(startArticle) || !serverTargetArticle.equals(targetArticle)) {
                // the start/target articles are not the same as returned from server, meaning the user has chosen new ones
                // let's let the server know
                wikiGameAPI.chooseChallengeAsync(this.friend.getUsername(), this.selectedMode, this.startArticle, this.targetArticle, new WikiGameInterface(getActivity()) {
                    @Override
                    public void onFinishedProcessingWikiRequest(JSONObject response) {
                        handleServerResponse(response, true);
                    }
                });
            } else {
                startChallengeGame();
            }
        }
    }

    private void handleServerResponse(JSONObject response, boolean shouldStartGame) {
        try {
            if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                serverStartArticle = startArticle = (response.getString(Consts.KEY_START_ARTICLE));
                serverTargetArticle = targetArticle = (response.getString(Consts.KEY_TARGET_ARTICLE));
                if (response.has("start_article_subject")) {
                    startArticleSubject = response.getString("start_article_subject");
                    targetArticleSubject = response.getString("target_article_subject");
                }
                if (shouldStartGame) startChallengeGame();

            } else {
                ErrorDialogs.showSomethingWentWrongDialog(getActivity(), false);
                stopShuffleAnimationTimer();
            }
        } catch (JSONException e) {
            ErrorDialogs.showBadResponseDialog(getActivity(), false);
            stopShuffleAnimationTimer();
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1 || data == null) return;

        final String type = data.getStringExtra("type");
        final SearchItem searchItem = (SearchItem) data.getSerializableExtra("search_item");
        if(type.equals("start")) {
            this.startArticle = searchItem.getTitle();
            this.startArticleSubject = searchItem.getSubject();
        }
        else {
            this.targetArticle = searchItem.getTitle();
            this.targetArticleSubject = searchItem.getSubject();
        }

        updateUI();
    }

    private void startChallengeGame() {
        setOnClearListener(new OnClearListener() {
            @Override
            public void clearDone() {
                if (startArticle != null) {
                    Intent intent = new Intent(getActivity(), WikiDisplayActivity.class);
                    intent.putExtra(Consts.BUNDLE_LEVEL0_FIRST_ARTICLE_KEY, startArticle);
                    intent.putExtra(Consts.BUNDLE_LEVEL0_TARGET_ARTICLE_KEY, targetArticle);
                    // TODO: delete the following line and support challenge at NewWikiDisplay
                    String mode = isCreatingNewChallenge() ? selectedMode : friend.getChallenge().getMode();

                    // getting default maximum allowed record
                    MainActivity mainActivity = (MainActivity) getActivity();
                    int requiredRecord = mode.equals(Consts.CLICKS_MODE) ? mainActivity.getMaximumAllowedClicks() : mainActivity.getMaximumAllowedTime();

                    Level dummyLevel = new Level("challenge level", false, mode, 0, requiredRecord);
                    intent.putExtra("current_level", dummyLevel);
                    intent.putExtra("friend", friend);
                    getActivity().startActivityForResult(intent, 1);
                }
            }
        });
        clear();
    }

    private void updateUI() {

        if (getView() == null) return;

        if (buttons != null)
            buttons[0].setClickable(true);

        icArrowTop.setVisibility(isCreatingNewChallenge() ? View.VISIBLE : View.GONE);
        icArrowBottom.setVisibility(isCreatingNewChallenge() ? View.VISIBLE : View.GONE);

        Challenge challenge = friend.getChallenge();
        String mode = challenge != null ? challenge.getMode() : this.selectedMode;
        if (mode == null) return;

        if (startArticleSubject != null && targetArticleSubject != null) {
            switchVisiblityState(true);
            tvStartSubject.setText(startArticleSubject);
            tvTargetSubject.setText(targetArticleSubject);
        } else {
            switchVisiblityState(false);
        }

        AppCompatImageView leftIcon = ((MainActivity) getActivity()).settings;
        if (mode.equals(Consts.CLICKS_MODE)) {
            leftIcon.setImageResource(R.drawable.ic_clicks);
            leftIcon.setClickable(false);
            timeLinearLayout.setVisibility(View.GONE);
            clicksLinearLayout.setVisibility(View.VISIBLE);
        } else {
            leftIcon.setImageResource(R.drawable.ic_alarm);
            leftIcon.setClickable(false);
            timeLinearLayout.setVisibility(View.VISIBLE);
            clicksLinearLayout.setVisibility(View.GONE);
        }

        if (isCreatingNewChallenge()) {
            clicksLinearLayout.setVisibility(View.GONE);
            timeLinearLayout.setVisibility(View.GONE);
            buttons[1].setVisibility(View.VISIBLE);

            startArticleTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openSearchArticlesIntent("start");
                }
            });

            targetArticleTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openSearchArticlesIntent("target");
                }
            });
            icArrowBottom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openSearchArticlesIntent("target");
                }
            });
            icArrowTop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openSearchArticlesIntent("start");
                }
            });

        } else {
            buttons[1].setVisibility(View.GONE);
            startArticle = challenge.getStartArticle();
            targetArticle = challenge.getTargetArticle();
            bestScoreClicksTextView.setText(challenge.getNumClicks() + " clicks to beat  ");
            long min = TimeUnit.SECONDS.toMinutes(challenge.getTime());
            long sec = (TimeUnit.SECONDS.toSeconds(challenge.getTime()) - 60 * min);
            bestScoreTimeTextView.setText(min + "m" + " " + (sec < 10 ? "0" + sec : sec)  + "s" + " to beat  ");
        }

        TextView tvLevelName = ((MainActivity) getActivity()).topBarTextView;
        tvLevelName.setVisibility(View.VISIBLE);
        tvLevelName.setText("Me VS " + friend.getUsername());

        if (startArticle != null && targetArticle != null) {
            this.startArticleTextView.setText(startArticle);
            this.targetArticleTextView.setText(targetArticle);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopShuffleAnimationTimer();
        this.isShuffleAnimationPaused = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopShuffleAnimationTimer();
    }

    public String getStartArticle() {
        return startArticle;
    }

    public void setStartArticle(String startArticle) {
        this.startArticle = startArticle;
    }

    public String getTargetArticle() {
        return targetArticle;
    }

    public void setTargetArticle(String targetArticle) {
        this.targetArticle = targetArticle;
    }

    public void shuffleArticles() {

        startArticle = null;

        if (wikiGameAPI == null)
            wikiGameAPI = new WikiGameAPI();

        // requesting random articles from server
        wikiGameAPI.shuffleChallengeAsync(friend.getUsername(), selectedMode, new WikiGameInterface(this.getActivity()) {
            @Override
            public void onFinishedProcessingWikiRequest(JSONObject response) {
                handleServerResponse(response, false);
            }

            @Override
            public void onFailedMakingWikiRequest(WikiError errorCause) {
                ErrorDialogs.showNetworkErrorDialog(getActivity(), false);
                stopShuffleAnimationTimer();
            }
        });

        shuffleAnimationTime = 840;
        shuffleAnimationTickCounter = 0;
        startShuffleAnimationTimer();
    }

    public String getSelectedMode() {
        return selectedMode;
    }

    public void setSelectedMode(String selectedMode) {
        this.selectedMode = selectedMode;
    }

    private void onShuffleAnimationTimerTick(int animationInterval, int maximumAnimationTime) {
        int requiredTicks = Math.round((float) shuffleAnimationTime / animationInterval);
        if (shuffleAnimationTickCounter >= requiredTicks) {
            // animation should end now
            if (startArticle != null && targetArticle != null) {
                updateUI();
                stopShuffleAnimationTimer();
            } else {
                // animation should end but we don't yet have the result from server!
                if (shuffleAnimationTime != maximumAnimationTime) {
                    // let's give it a chance
                    shuffleAnimationTime = maximumAnimationTime;
                } else {
                    stopShuffleAnimationTimer();
                    Toast.makeText(getActivity(), "Failed to shuffle articles!", Toast.LENGTH_SHORT);
                    //ErrorDialogs.showNetworkErrorDialog(getActivity(), false);
                }
            }

        } else {
            shuffleAnimationTickCounter++;
            int randomIndex1 = new Random().nextInt(Consts.RANDOM_ARTICLES_ARRAY.length);
            int randomIndex2 = new Random().nextInt(Consts.RANDOM_ARTICLES_ARRAY.length);
            startArticleTextView.setText(Consts.RANDOM_ARTICLES_ARRAY[randomIndex1]);
            targetArticleTextView.setText(Consts.RANDOM_ARTICLES_ARRAY[randomIndex2]);
        }
    }

   /* @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Animation a = new Animation() {};
        a.setDuration(0);
        return a;
    }*/

    private void animateArrow() {
        ScaleAnimation scale = (ScaleAnimation) AnimationUtils.loadAnimation(getActivity(), R.anim.grow_arrow);
        scale.setInterpolator(new FastOutSlowInInterpolator());
        arrow.startAnimation(scale);
    }

    @Override
    public void clear() {
        for (RoundButton button : buttons) {
            if(button.getVisibility() == View.VISIBLE)
                button.animateOut();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (onClearListener != null)
                    onClearListener.clearDone();
            }
        }, 300);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isShuffleAnimationPaused) {
            startShuffleAnimationTimer();
            this.isShuffleAnimationPaused = false;
        }
        for (RoundButton button : buttons) {
            if (button.getVisibility() == View.VISIBLE)
                button.animateIn(0);
        }
    }

    private boolean isCreatingNewChallenge() {
        return this.friend.getChallenge() == null;
    }

    public Friend getFriend() {
        return friend;
    }

    public void setFriend(Friend friend) {
        this.friend = friend;
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(0, "alpha",
                1f);
        fadeOut.setDuration(0);
        return fadeOut;

    }
}

