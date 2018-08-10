package com.yoavfranco.wikigame.fragments;

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
import com.yoavfranco.wikigame.activities.SearchActivity;
import com.yoavfranco.wikigame.activities.WikiDisplayActivity;
import com.yoavfranco.wikigame.utils.Consts;
import com.yoavfranco.wikigame.utils.ErrorDialogs;
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

public class QuickPlayScreen extends BaseScreen {

    @BindViews({R.id.play_now, R.id.reset})
    RoundButton[] buttons;
    @BindView(R.id.tvStartWithInp)
    TextView startArticleTextView;
    @BindView(R.id.tvEndWithInp)
    TextView targetArticleTextView;
    @BindView(R.id.iv_arrow)
    AppCompatImageView arrow;
    @BindView(R.id.best_score_wrapper_clicks)
    LinearLayout clicksLinearLayout;
    @BindView(R.id.best_score_wrapper_time)
    LinearLayout timeLinearLayout;
    @BindView(R.id.tvBestScoreClicks)
    TextView bestScoreClicksTextView;
    @BindView(R.id.tvBestTime)
    TextView bestScoreTimeTextView;
    @BindView(R.id.icArrowTop)
    AppCompatImageView icArrowTop;
    @BindView(R.id.icArrowBottom)
    AppCompatImageView icArrowBottom;

    @BindView(R.id.tvStartSubject)
    TextView tvStartSubject;
    @BindView(R.id.tvTargetSubject)
    TextView tvTargetSubject;

    WikiGameAPI wikiGameAPI;

    private String serverStartArticle;
    private String serverTargetArticle;
    private String startArticle;
    private String targetArticle;
    private String startArticleSubject;
    private String targetArticleSubject;
    private Level level;

    private boolean isShuffleAnimationPaused;
    private int shuffleAnimationInterval = 70;
    private int shuffleAnimationTime;
    private int shuffleAnimationTickCounter;

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
        return inflater.inflate(R.layout.quickplay_screen, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        wikiGameAPI = new WikiGameAPI();

        animateArrow();

        buttons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlayButtonClicked();
            }
        });

        buttons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onShuffleButtonClicked(this);
            }
        });

        updateUI();
    }

    private void openSearchArticlesIntent(String type) {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra("type", type);
        startActivityForResult(intent, 1);
    }


    public void onPlayButtonClicked() {
        buttons[0].setClickable(false);
        if (startArticle == null) {
            ErrorDialogs.showNetworkErrorDialog(getActivity(), false);
            buttons[0].setClickable(true);
            return;
        }
        if (!serverStartArticle.equals(startArticle) || !serverTargetArticle.equals(targetArticle) && isPractice()) {
            // the start/target articles are not the same as returned from server, meaning the user has chosen new ones
            // let's let the server know
            wikiGameAPI.chooseAsync(level.getMode(), startArticle, targetArticle, new WikiGameInterface(getActivity()) {
                @Override
                public void onFinishedProcessingWikiRequest(JSONObject response) {
                    handleServerResponse(response);
                }
            });
        }

        // getting default maximum allowed record
        MainActivity mainActivity = (MainActivity) getActivity();
        int requiredRecord = this.level.getMode().equals(Consts.CLICKS_MODE) ? mainActivity.getMaximumAllowedClicks() : mainActivity.getMaximumAllowedTime();
        this.level.setMaximumAllowedRecord(requiredRecord);

        setOnClearListener(new OnClearListener() {
            @Override
            public void clearDone() {
                if (startArticle != null) {
                    Intent intent = new Intent(getActivity(), WikiDisplayActivity.class);
                    intent.putExtra(Consts.BUNDLE_LEVEL0_FIRST_ARTICLE_KEY, startArticle);
                    intent.putExtra(Consts.BUNDLE_LEVEL0_TARGET_ARTICLE_KEY, targetArticle);
                    intent.putExtra("current_level", level);
                    getActivity().startActivityForResult(intent, 1);
                }
            }
        });

        clear();
    }

    public void handleServerResponse(JSONObject response) {
        try {
            if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                serverStartArticle = startArticle = response.getString(Consts.KEY_START_ARTICLE);
                serverTargetArticle = targetArticle = response.getString(Consts.KEY_TARGET_ARTICLE);
                if (response.has("start_article_subject") || response.has("target_article_subject")) {
                    startArticleSubject = response.has("start_article_subject") ? response.getString("start_article_subject") : "";
                    targetArticleSubject = response.has("target_article_subject") ? response.getString("target_article_subject") : "";
                }
            } else {
                stopShuffleAnimationTimer();
                ErrorDialogs.showSomethingWentWrongDialog(getActivity(), false);
            }
        } catch (JSONException e) {
            stopShuffleAnimationTimer();
            ErrorDialogs.showBadResponseDialog(getActivity(), false);
            e.printStackTrace();
        }
    }

    public void onShuffleButtonClicked(View.OnClickListener clickListener)
    {
        switchChooseControlsVisiblityState(false);
        buttons[1].animateClick(clickListener);
        shuffleArticles();
    }

    private void switchChooseControlsVisiblityState(boolean visible) {
        if (visible) {
            // fade in
            if (isPractice()) {
                icArrowTop.setVisibility(View.VISIBLE);
                icArrowTop.animate().alpha(1.0f).setDuration(200);
                icArrowBottom.setVisibility(View.VISIBLE);
                icArrowBottom.animate().alpha(1.0f).setDuration(200);
                tvStartSubject.setVisibility(View.VISIBLE);
                tvStartSubject.animate().alpha(1.0f).setDuration(200);
            }
            tvTargetSubject.setVisibility(View.VISIBLE);
            tvTargetSubject.animate().alpha(1.0f).setDuration(200);
        }
        else {
            // "fade" out
            if (isPractice()) {
                icArrowTop.setVisibility(View.INVISIBLE);
                icArrowTop.setAlpha(0.0f);
                icArrowBottom.setVisibility(View.INVISIBLE);
                icArrowBottom.setAlpha(0.0f);
                tvStartSubject.setVisibility(View.INVISIBLE);
                tvStartSubject.setAlpha(0.0f);
            }
            tvTargetSubject.setVisibility(View.INVISIBLE);
            tvTargetSubject.animate().alpha(0.0f).setDuration(200);
        }
    }

    public void updateUI() {

        if (getView() == null) return;

        if (startArticle != null && targetArticle != null) {
            this.startArticleTextView.setText(startArticle);
            this.targetArticleTextView.setText(targetArticle);
        }

        if (buttons != null)
            buttons[0].setClickable(true);

        icArrowTop.setVisibility(isPractice() ? View.VISIBLE : View.GONE);
        icArrowBottom.setVisibility(isPractice() ? View.VISIBLE : View.GONE);

        if (startArticleSubject != null && targetArticleSubject != null) {
            switchChooseControlsVisiblityState(true);
            tvStartSubject.setText(startArticleSubject);
            tvTargetSubject.setText(targetArticleSubject);
        } else {
            switchChooseControlsVisiblityState(false);
            tvStartSubject.setText("");
            tvTargetSubject.setText("");
        }

        String mode = level.getMode();

        // TODO: the following code takes the settings icon from the MainActivity and changes its image to another icon.
        // this is a bad and we want to change it in the future.
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

        TextView tvLevelName = ((MainActivity) getActivity()).topBarTextView;
        tvLevelName.setVisibility(View.VISIBLE);

        if (level.getLevelName().equals("practice level")) {
            tvLevelName.setText("Practice Mode");
            clicksLinearLayout.setVisibility(View.GONE);
            timeLinearLayout.setVisibility(View.GONE);

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
            tvLevelName.setText("Level " + level.getLevelName());
        }

        bestScoreClicksTextView.setText("Best:  " + (level.getBest() == 0 ? "--" : level.getBest()) + " ");

        long min = TimeUnit.SECONDS.toMinutes(level.getBest());
        long sec = (TimeUnit.SECONDS.toSeconds(level.getBest()) - 60 * min);
        bestScoreTimeTextView.setText("Best: " + (level.getBest() == 0 ? "--" : min + "m" + " " + (sec < 10 ? "0" + sec : sec)  + "s ") + " ");
    }

    @Override
    public void onPause() {
        super.onPause();
        stopShuffleAnimationTimer();
        this.isShuffleAnimationPaused = true;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopShuffleAnimationTimer();
    }

    public void shuffleArticles() {

        startArticle = null;
        targetArticle = null;

        if (wikiGameAPI == null)
            wikiGameAPI = new WikiGameAPI();

        // requesting random articles from server
        WikiGameInterface wikiGameInterface = new WikiGameInterface(this.getActivity()) {
            @Override
            public void onFinishedProcessingWikiRequest(JSONObject response) {
               handleServerResponse(response);
            }

            @Override
            public void onFailedMakingWikiRequest(WikiError errorCause) {
                ErrorDialogs.showNetworkErrorDialog(getActivity(), false);
                stopShuffleAnimationTimer();
            }
        };

        boolean isPractice = this.level.getLevelName().equals("practice level");
        if (isPractice)
            wikiGameAPI.shufflePracticeAsync(this.level.getMode(), wikiGameInterface);
        else
            wikiGameAPI.shuffleLevelAsync(this.level.getLevelName(), wikiGameInterface);

        shuffleAnimationTime = 840;
        shuffleAnimationTickCounter = 0;
        startShuffleAnimationTimer();
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
                    Toast.makeText(getActivity(), "Failed to shuffle articles!", Toast.LENGTH_SHORT).show();
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

    private boolean isPractice() {
        // bad idea, change this thing
        return this.level.getLevelName().equals("practice level");
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    private void animateArrow() {
        ScaleAnimation scale = (ScaleAnimation) AnimationUtils.loadAnimation(getActivity(), R.anim.grow_arrow);
        scale.setInterpolator(new FastOutSlowInInterpolator());
        arrow.startAnimation(scale);
    }


    public Level getLevel() {
        return level;
    }

    @Override
    public void clear() {
        for (RoundButton button : buttons) {
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
            button.animateIn(0);
        }
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
}