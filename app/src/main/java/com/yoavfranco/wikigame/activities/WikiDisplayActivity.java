package com.yoavfranco.wikigame.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.yoavfranco.wikigame.HTTP.WikiGameAPI;
import com.yoavfranco.wikigame.HTTP.WikiGameInterface;
import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.utils.Challenge;
import com.yoavfranco.wikigame.utils.Consts;
import com.yoavfranco.wikigame.utils.CountDownTimerWithPause;
import com.yoavfranco.wikigame.utils.ErrorDialogs;
import com.yoavfranco.wikigame.utils.Friend;
import com.yoavfranco.wikigame.utils.Level;
import com.yoavfranco.wikigame.utils.Utils;
import com.yoavfranco.wikigame.utils.WikipediaUtils;
import com.yoavfranco.wikigame.views.RoundButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WikiDisplayActivity extends AppCompatActivity {
    @BindView(R.id.article_name_tv)
    AppCompatTextView articleNameTV;
    @BindView(R.id.numclicks_tv)
    AppCompatTextView numClicksTV;
    @BindView(R.id.numclicks_tv2)
    AppCompatTextView numClicksTV2;
    @BindView(R.id.target_value_tv)
    AppCompatTextView targetArticleTV;
    @BindView(R.id.webview_back)
    AppCompatImageView backButton;
    @BindView(R.id.webview_next)
    AppCompatImageView webview_next;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.pause_button)
    RoundButton pauseButton;
    @BindView(R.id.game_results_wrapper_aim)
    LinearLayout gameReusltsWrapperAim;
    @BindView(R.id.game_results_wrapper_practice)
    LinearLayout gameReusltsWrapperPractice;
    @BindView(R.id.tvToBeat2)
    AppCompatTextView tvToBeat;

    private final String WIKI_PREFIX = "https://en.m.wikipedia.org/wiki/";
    private WikiWebView webView;
    private boolean isStyleSheetReplaced;
    private boolean isPageChanged;
    private boolean isLoadingPage;
    private boolean isGoingBack;
    private boolean isGameFinished;

    String currentArticle;
    int numClicks = -1;
    private String firstArticle;
    private String targetArticle;

    private InterstitialAd mInterstitialAd;

    // TODO: a challenge game or a practice game is not in any "level", we shouldn't trick this screen into using this variable in these cases.
    // this is bad practice.
    private Level currentLevel;
    private Friend friend;

    private WikiGameAPI wikiGameAPI;

    long secondsPassed;
    CountDownTimerWithPause countDownTimer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_wiki_display);
        ButterKnife.bind(this);
        progressBar.setIndeterminate(true);
        Intent intent = getIntent();
        this.firstArticle = intent.getStringExtra(Consts.BUNDLE_LEVEL0_FIRST_ARTICLE_KEY);
        this.targetArticle = intent.getStringExtra(Consts.BUNDLE_LEVEL0_TARGET_ARTICLE_KEY);
        setTargetArticleTitle("➜ " + targetArticle);
        this.wikiGameAPI = new WikiGameAPI();
        this.isStyleSheetReplaced = false;
        this.isPageChanged = false;
        this.isLoadingPage = false;
        this.isGameFinished = false;
        this.currentLevel = (Level) intent.getSerializableExtra("current_level");
        setCurrentArticleTitle(firstArticle);
        this.friend = (Friend) intent.getSerializableExtra("friend");

        webView = new WikiWebView(WikiDisplayActivity.this);
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.addRule(RelativeLayout.BELOW, R.id.bottomBar);
        webView.setLayoutParams(p);
        webView.loadUrl(WikipediaUtils.wikiURLForArticleName(this.firstArticle));
        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.new_wiki_wrapper);
        wrapper.addView(webView);
        updateBackButton();
        updateNextButton();

        if (currentLevel.getMode().equals(Consts.TIMER_MODE)) {
            AppCompatImageView modeIcon = (AppCompatImageView) findViewById(R.id.modeIcon);
            modeIcon.setImageResource(R.drawable.ic_alarm);
            AppCompatImageView modeIcon2 = (AppCompatImageView) findViewById(R.id.modeIcon2);
            modeIcon2.setImageResource(R.drawable.ic_alarm);
            startTimer();
        }
        if (friend != null)
            if (friend.getChallenge() != null)
                if (friend.getChallenge().getNumClicks() > 0 || friend.getChallenge().getTime() > 0) {
                    initiateToBeatTopBarMode();
                    if (friend.getChallenge().getMode().equals(Consts.CLICKS_MODE)) {
                        tvToBeat.setText(friend.getChallenge().getNumClicks() + "");
                    } else {
                        long min = TimeUnit.SECONDS.toMinutes(friend.getChallenge().getTime());
                        long sec = (TimeUnit.SECONDS.toSeconds(friend.getChallenge().getTime()) - 60 * min);
                        tvToBeat.setText((min < 10 ? ("0" + min) : min + "") + ":" + (sec < 10 ? "0" + sec : sec));
                    }
                }

        if (currentLevel != null)
            if (currentLevel.getBest() > 0) {
                initiateToBeatTopBarMode();
                if (currentLevel.getMode().equals(Consts.CLICKS_MODE)) {
                    tvToBeat.setText(currentLevel.getBest() + "");
                } else {
                    long min = TimeUnit.SECONDS.toMinutes(currentLevel.getBest());
                    long sec = (TimeUnit.SECONDS.toSeconds(currentLevel.getBest()) - 60 * min);
                    tvToBeat.setText((min < 10 ? ("0" + min) : min + "") + ":" + (sec < 10 ? "0" + sec : sec));
                }
            }

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-9470442411108307/1499231472");
        requestNewInterstitial();

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGiveUpDialog();
            }
        });
    }


    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();

        mInterstitialAd.loadAd(adRequest);
    }

    private void initiateToBeatTopBarMode() {
        RelativeLayout topBar = (RelativeLayout) findViewById(R.id.topBar);
        topBar.setPadding(topBar.getPaddingLeft(), 0, topBar.getPaddingRight(), 0);
        gameReusltsWrapperAim.setVisibility(View.VISIBLE);
        gameReusltsWrapperPractice.setVisibility(View.GONE);
    }

    private void startTimer() {
        countDownTimer = new CountDownTimerWithPause(10000000, 1000, true) {
            public void onTick(long m) {
                secondsPassed++;
                long min = TimeUnit.SECONDS.toMinutes(secondsPassed);
                long sec = (TimeUnit.SECONDS.toSeconds(secondsPassed) - 60 * min);
                numClicksTV.setText((min < 10 ? ("0" + min) : min + "") + ":" + (sec < 10 ? "0" + sec : sec) + " ");
                numClicksTV2.setText((min < 10 ? ("0" + min) : min + "") + ":" + (sec < 10 ? "0" + sec : sec) + " ");
            }

            public void onFinish() {
            }
        };
        countDownTimer.create();
    }

    private void updateBackButton() {
        if (!webView.canGoBack()) {
            backButton.setAlpha(0.4f);
            backButton.setOnClickListener(null);
        } else {
            backButton.setAlpha(1f);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    private void updateNextButton() {
        if (!webView.canGoForward()) {
            webview_next.setAlpha(0.3f);
            webview_next.setOnClickListener(null);
        } else {
            webview_next.setAlpha(1f);
            webview_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webView.goForward();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            isGoingBack = true;
            webView.goBack();
        } else {
            showGiveUpDialog();
        }

    }

    private void onPageBack() {
        updateBackButton();
    }

    private void setCurrentArticleTitle(String value) {
        if (value.length() >= 20) {
            value = value.substring(0, 20);
            value += "...";
        }
        articleNameTV.setText(value);
    }

    private void setTargetArticleTitle(String value) {
        if (value.length() >= 15) {
            value = value.substring(0, 15);
            value += "...";
        }
        targetArticleTV.setText(value);
    }

    private void onPageChange() {
        if (!webView.getTitle().contains("-")) return;
        setCurrentArticleTitle(webView.getTitle().substring(0, webView.getTitle().indexOf('-')));
        updateBackButton();
        updateNextButton();
    }

    private void showWebView(final boolean show) {
        showProgressBar(!show);
        webView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showProgressBar(final boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void verifyServerResponse(JSONObject response) {
        try {
            if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                if (response.getBoolean("is_game_finished")) {

                    int rand = new Random().nextInt((101));
                    if (rand < 80 && mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    }


                    this.isGameFinished = true;
                    if (countDownTimer != null)
                        countDownTimer.pause();

                    int numClicks = response.getInt("num_clicks");
                    int timeTaken = response.getInt("time_taken");
                    int record = currentLevel.isClicksMode() ? numClicks : timeTaken;
                    JSONObject achievements = response.getJSONObject(Consts.KEY_ACHIEVEMENTS);

                    if (achievements.has("challenge_status")) {
                        switch (achievements.getString("challenge_status")) {
                            case "sent":
                                Challenge newChallenge = Challenge.fromJSON(achievements.getJSONObject("sent_challenge"));
                                this.friend.setChallenge(newChallenge);
                                showChallengeSentDialog();
                                break;
                            case "accepted":
                                showChallengeAcceptedDialog();
                                break;
                            case "not_accepted":
                                showChallengeFailedDialog();
                                break;
                            case "tie":
                                showChallengeTieDialog();
                                break;
                        }
                    }
                    else if (response.getBoolean("is_success")) {
                        // success
                        int newPoints = achievements.getInt("new_points");
                        int totalPoints = achievements.getInt("total_points");
                        int pointsForLevel = achievements.getInt("points_for_level");
                        if (achievements.has(Consts.KEY_NEW_LEVEL)) {
                            JSONObject newLevel = achievements.getJSONObject(Consts.KEY_NEW_LEVEL);
                            Level newLevelUnlocked = Level.fromJSON(newLevel);
                            showNewLevelUnlockDialog(newLevelUnlocked, totalPoints, pointsForLevel, newPoints, numClicks, timeTaken);
                        } else if (currentLevel.getLevelName().equals("practice level") && pointsForLevel > 0) {
                            showSuccessPracticeDialog(pointsForLevel, numClicks, timeTaken);
                        }
                        else if (pointsForLevel > 0) {
                            showSuccessLevelDialog(pointsForLevel, newPoints, totalPoints, numClicks, timeTaken);
                        }
                    } else {
                        // failure
                        showFailedDialog(record, currentLevel.getMode(), currentLevel.getMaximumAllowedRecord());
                    }
                }
            } else {
                ErrorDialogs.showSomethingWentWrongDialog(this, true);
            }
        } catch (JSONException e) {
            ErrorDialogs.showBadResponseDialog(this, true);
            e.printStackTrace();
        }
    }

    private void showSuccessPracticeDialog(int pointsForLevel, int numClicks, int time) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        new MaterialStyledDialog.Builder(this)
                // TODO: add strings to xml/strings file
                .setTitle("You made it!")
                .setDescription("You reached " + targetArticle + " in " + numClicks + " steps and " + time + " seconds.\n\n" +
                        "Good job!")
                .setStyle(Style.HEADER_WITH_ICON)
                .setHeaderDrawable(getResources().getDrawable(getStarsDrawableByPoints(pointsForLevel)))
                .setCancelable(false)
                .withIconAnimation(false)
                .withDialogAnimation(true)
                .withDivider(true)
                .setPositiveText(R.string.restart)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("updateMode", Consts.UPDATE_MODE_SUCCESS_PRACTICE_MODE);
                        setResult(1, returnIntent);
                        dialog.dismiss();
                        WikiDisplayActivity.this.finish();
                    }
                }).setNegativeText(R.string.menu)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("shouldCloseFragments", true);
                        returnIntent.putExtra("updateMode", Consts.UPDATE_MODE_SUCCESS_PRACTICE_MODE);
                        setResult(1, returnIntent);
                        dialog.dismiss();
                        WikiDisplayActivity.this.finish();
                    }
                })
                .show();
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.tada);
        mp.start();
    }

    private void showFailedDialog(int record, String mode, int requiredRecord) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        String clicksText = "You reached " + targetArticle + " in " + record + " clicks.\n"
                + "You need less than " + requiredRecord + " clicks in order to success.";
        String timeText = "You reached " + targetArticle + " in " + record + " seconds.\n"
                + "You need less than " + requiredRecord + " seconds in order to success.";
        String text = mode.equals(Consts.CLICKS_MODE) ? clicksText : timeText;

        new MaterialStyledDialog.Builder(this)
                // TODO: add strings to xml/strings file
                .setTitle("You didn't make it...")
                .setDescription(text)
                .setStyle(Style.HEADER_WITH_TITLE)
                .setCancelable(false)
                .withIconAnimation(false)
                .withDialogAnimation(true)
                .withDivider(true)
                .setPositiveText(R.string.restart)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("updateMode", Consts.UPDATE_MODE_SUCCESS_PRACTICE_MODE);
                        setResult(1, returnIntent);
                        dialog.dismiss();
                        WikiDisplayActivity.this.finish();
                    }
                }).setNegativeText(R.string.menu)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("shouldCloseFragments", true);
                        returnIntent.putExtra("updateMode", Consts.UPDATE_MODE_SUCCESS_PRACTICE_MODE);
                        setResult(1, returnIntent);
                        dialog.dismiss();
                        WikiDisplayActivity.this.finish();
                    }
                })
                .show();
    }

    private void showGiveUpDialog() {
        pauseButton.animateOut();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        new MaterialStyledDialog.Builder(this)
                // TODO: add strings to xml/strings file
                .setTitle("Give up?")
                .setDescription("Are you sure you want to leave the game?\nYou will loose all of your progress!")
                .setStyle(Style.HEADER_WITH_TITLE)
                .setCancelable(false)
                .withIconAnimation(false)
                .withDialogAnimation(true)
                .withDivider(true)
                .setPositiveText("CANCEL")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        pauseButton.animateIn();
                    }
                }).setNegativeText("LEAVE")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        giveUpChallenge();
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("shouldCloseFragments", true);
                        setResult(1, returnIntent);
                        dialog.dismiss();
                        WikiDisplayActivity.this.finish();
                    }
                })
                .show();
    }

    private void showChallengeAcceptedDialog() {
        pauseButton.animateOut();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        new MaterialStyledDialog.Builder(this)
                // TODO: add strings to xml/strings file
                .setTitle("Challenge accepted!")
                .setDescription("You made it better than your friend.")
                .setStyle(Style.HEADER_WITH_TITLE)
                .setCancelable(false)
                .withIconAnimation(false)
                .withDialogAnimation(true)
                .withDivider(true)
                .setPositiveText("FINE")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("updateMode", Consts.UPDATE_MODE_CHALLENGE_REMOVED);
                        returnIntent.putExtra("challengeStatus", "accepted");
                        returnIntent.putExtra("shouldCloseFragments", true);
                        returnIntent.putExtra("friend", friend );
                        setResult(1, returnIntent);
                        dialog.dismiss();
                        WikiDisplayActivity.this.finish();
                    }
                })
                .setNegativeText("CHALLENGE BACK")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("updateMode", Consts.UPDATE_MODE_CHALLENGE_REMOVED_AND_CREATING_NEW);
                        returnIntent.putExtra("challengeStatus", "accepted");
                        returnIntent.putExtra("shouldCloseFragments", true);
                        returnIntent.putExtra("friend", friend );
                        setResult(1, returnIntent);
                        dialog.dismiss();
                        WikiDisplayActivity.this.finish();
                    }
                }).show();
    }

    private void showChallengeSentDialog() {
        pauseButton.animateOut();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        new MaterialStyledDialog.Builder(this)
                // TODO: add strings to xml/strings file
                .setTitle("Success!")
                .setDescription("Your challenge has been sent to your friend.")
                .setStyle(Style.HEADER_WITH_TITLE)
                .setCancelable(false)
                .withIconAnimation(false)
                .withDialogAnimation(true)
                .withDivider(true)
                .setPositiveText("FINE")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("updateMode", Consts.UPDATE_MODE_CHALLENGE_SENT);
                        returnIntent.putExtra("challengeStatus", "sent");
                        returnIntent.putExtra("shouldCloseFragments", true);
                        returnIntent.putExtra("friend", friend );
                        setResult(1, returnIntent);
                        WikiDisplayActivity.this.finish();
                    }
                }).show();
    }

    private void showChallengeFailedDialog() {
        pauseButton.animateOut();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        new MaterialStyledDialog.Builder(this)
                // TODO: add strings to xml/strings file
                .setTitle("That's bad.")
                .setDescription("You didn't make it better than your friend.")
                .setStyle(Style.HEADER_WITH_TITLE)
                .setCancelable(false)
                .withIconAnimation(false)
                .withDialogAnimation(true)
                .withDivider(true)
                .setPositiveText("OH WELL")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("updateMode", Consts.UPDATE_MODE_CHALLENGE_REMOVED);
                        returnIntent.putExtra("challengeStatus", "not_accepted");
                        returnIntent.putExtra("shouldCloseFragments", true);
                        returnIntent.putExtra("friend", friend);
                        setResult(1, returnIntent);
                        dialog.dismiss();
                        WikiDisplayActivity.this.finish();
                    }
                }).setNegativeText("CHALLENGE BACK")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("updateMode", Consts.UPDATE_MODE_CHALLENGE_REMOVED_AND_CREATING_NEW);
                        returnIntent.putExtra("challengeStatus", "not_accepted");
                        returnIntent.putExtra("shouldCloseFragments", true);
                        returnIntent.putExtra("friend", friend );
                        setResult(1, returnIntent);
                        dialog.dismiss();
                        WikiDisplayActivity.this.finish();
                    }
                }).show();
    }

    private void showChallengeTieDialog() {
        pauseButton.animateOut();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        new MaterialStyledDialog.Builder(this)
                // TODO: add strings to xml/strings file
                .setTitle("It's a tie!")
                .setDescription("You both did well. nobody wins")
                .setStyle(Style.HEADER_WITH_TITLE)
                .setCancelable(false)
                .withIconAnimation(false)
                .withDialogAnimation(true)
                .withDivider(true)
                .setPositiveText("OH WELL")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("updateMode", Consts.UPDATE_MODE_CHALLENGE_REMOVED);
                        returnIntent.putExtra("shouldCloseFragments", true);
                        returnIntent.putExtra("challengeStatus", "tie");
                        returnIntent.putExtra("friend", friend);
                        setResult(1, returnIntent);
                        dialog.dismiss();
                        WikiDisplayActivity.this.finish();
                    }
                }).setNegativeText("CHALLENGE BACK")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("updateMode", Consts.UPDATE_MODE_CHALLENGE_REMOVED_AND_CREATING_NEW);
                        returnIntent.putExtra("challengeStatus", "tie");
                        returnIntent.putExtra("shouldCloseFragments", true);
                        returnIntent.putExtra("friend", friend );
                        setResult(1, returnIntent);
                        dialog.dismiss();
                        WikiDisplayActivity.this.finish();
                    }
                }).show();
    }

    private void showSuccessLevelDialog(int pointsForLevel, final int pointsAdded, int totalPoints,final int mumClicks, final int time) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        new MaterialStyledDialog.Builder(this)
                // TODO: add strings to xml/strings file
                .setTitle("You made it!")
                .setDescription("You reached " + targetArticle + " in " + mumClicks + " steps and "+ time + " seconds.\n\n" +
                        (pointsAdded > 0 ? "New record! " : "") +
                        "You earned " + pointsAdded + " new points and now you have " + totalPoints + " points.")
                .setStyle(Style.HEADER_WITH_ICON)
                .setHeaderDrawable(getResources().getDrawable(getStarsDrawableByPoints(pointsForLevel)))
                .setCancelable(false)
                .withIconAnimation(false)
                .withDialogAnimation(true)
                .withDivider(true)
                .setPositiveText(R.string.next_level)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("updateMode", Consts.UPDATE_MODE_SUCCESS_LEVEL);
                        if (pointsAdded > 0) {
                            returnIntent.putExtra("pointsAdded", pointsAdded);
                            returnIntent.putExtra("newBest", currentLevel.getMode().equals(Consts.CLICKS_MODE) ? numClicks : time);
                        }
                        setResult(1, returnIntent);
                        dialog.dismiss();
                        WikiDisplayActivity.this.finish();
                    }
                }).setNegativeText(R.string.menu)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("shouldCloseFragments", true);
                        returnIntent.putExtra("updateMode", Consts.UPDATE_MODE_SUCCESS_LEVEL);
                        if (pointsAdded > 0) {
                            returnIntent.putExtra("pointsAdded", pointsAdded);
                            returnIntent.putExtra("newBest", currentLevel.getMode().equals(Consts.CLICKS_MODE) ? numClicks : time);
                        }
                        setResult(1, returnIntent);
                        dialog.dismiss();
                        WikiDisplayActivity.this.finish();
                    }
                })
                .show();
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.tada);
        mp.start();
    }

    private void showNewLevelUnlockDialog(final Level newLevelUnlocked, int points,
                                          int pointsForLevel, final int pointsAdded, final int numClicks, final int time) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        new MaterialStyledDialog.Builder(this)
                // TODO: add strings to xml/strings file
                .setTitle("Level " + newLevelUnlocked.getLevelName() + " unlocked!")
                .setDescription("You reached " + targetArticle + " in " + numClicks + " steps and " + time + " seconds.\n\n" +
                        "New record! You earned " + pointsAdded + " new points and now you have " + points + " points!")
                .setStyle(Style.HEADER_WITH_ICON)
                .setHeaderDrawable(getResources().getDrawable(getStarsDrawableByPoints(pointsForLevel)))
                .setCancelable(false)
                .withIconAnimation(false)
                .withDialogAnimation(true)
                .withDivider(true)
                .setPositiveText(R.string.next_level)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("updateMode", Consts.UPDATE_MODE_NEW_LEVEL_UNLOCKED);
                        returnIntent.putExtra("pointsAdded", pointsAdded);
                        returnIntent.putExtra("newBest", currentLevel.getMode().equals(Consts.CLICKS_MODE) ? numClicks : time);
                        returnIntent.putExtra(Consts.BUNDLE_LEVEL_KEY, newLevelUnlocked);
                        setResult(1, returnIntent);
                        dialog.dismiss();
                        WikiDisplayActivity.this.finish();
                    }
                }).setNegativeText(R.string.menu)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("shouldCloseFragments", true);
                        returnIntent.putExtra("updateMode", Consts.UPDATE_MODE_NEW_LEVEL_UNLOCKED);
                        returnIntent.putExtra("pointsAdded", pointsAdded);
                        returnIntent.putExtra("newBest", currentLevel.getMode().equals(Consts.CLICKS_MODE) ? numClicks : time);
                        returnIntent.putExtra(Consts.BUNDLE_LEVEL_KEY, newLevelUnlocked);
                        setResult(1, returnIntent);
                        dialog.dismiss();
                        WikiDisplayActivity.this.finish();
                    }
                })
                .show();
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.tada);
        mp.start();
    }

    private int getStarsDrawableByPoints(int points) {
        if (points >= 60)
            return R.drawable.threestars;
        else if (points >= 30)
            return R.drawable.twostar;

        return R.drawable.onestar;
    }

    private void giveUpChallenge() {
        if (this.friend == null) return;
        if (this.friend.getChallenge() == null) return;

        wikiGameAPI.giveUpChallengeAsync(new WikiGameInterface(this) {
            @Override
            public void onFinishedProcessingWikiRequest(JSONObject response) {
                try {
                    if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                        // good for you.
                    } else {
                        ErrorDialogs.showSomethingWentWrongDialog(WikiDisplayActivity.this, false);
                    }
                } catch (JSONException e) {
                    ErrorDialogs.showBadResponseDialog(WikiDisplayActivity.this, false);
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean isChallengeGame() {
        return this.friend != null;
    }

    private class WikiWebView extends WebView {
        @SuppressLint("SetJavaScriptEnabled")
        public WikiWebView(Context context) {
            super(context);
            getSettings().setJavaScriptEnabled(true);
            setWebViewClient(new WikiWebClient());
        }

        @Override
        public void goBack() {
            super.goBack();
            onPageBack();
        }
    }

    private class WikiWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Utils.logDebug(this, "Page started loading");

            isPageChanged = false;
            isLoadingPage = true;

            if (!url.startsWith(WIKI_PREFIX)) {
                ErrorDialogs.showOnlyWikipediaDialog(WikiDisplayActivity.this);
                return;
            }

            numClicks++;
            currentArticle = url.substring(WIKI_PREFIX.length());

            progressBar.bringToFront();
            if (!currentLevel.getMode().equals(Consts.TIMER_MODE)) {
                numClicksTV2.setText(String.valueOf(numClicks));
                numClicksTV.setText(String.valueOf(numClicks));
            }

            if (isGoingBack) {
                isGoingBack = false;
                wikiGameAPI.goBackAsync(new WikiGameInterface(WikiDisplayActivity.this) {
                    @Override
                    public void onFinishedProcessingWikiRequest(JSONObject response) {
                        verifyServerResponse(response);
                    }
                });
            }
            else if (numClicks > 0) {
                wikiGameAPI.followLinkAsync(currentArticle, new WikiGameInterface(WikiDisplayActivity.this) {
                    @Override
                    public void onFinishedProcessingWikiRequest(JSONObject response) {
                        verifyServerResponse(response);
                    }
                });
            } else {
                wikiGameAPI.startNewGameAsync(new WikiGameInterface(WikiDisplayActivity.this) {
                    @Override
                    public void onFinishedProcessingWikiRequest(JSONObject response) {
                        verifyServerResponse(response);
                    }
                });
            }
            showProgressBar(true);

            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
          //  ErrorDialogs.showNetworkErrorDialog(WikiDisplayActivity.this, true);
            // TODO: the activity is not closed even though it should
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Utils.logDebug(this, "Page finished loading");
            isLoadingPage = false;

            if (!isPageChanged) {
                onPageChange();
                showProgressBar(false);
                isPageChanged = true;

                String finalArticleName = webView.getUrl().substring(WIKI_PREFIX.length());
                if (!finalArticleName.equals(currentArticle) && !isGameFinished) {
                    // looks like a wikipedia redirect. we're gonna send a fix request
                    wikiGameAPI.fixLinkAsync(finalArticleName, new WikiGameInterface(WikiDisplayActivity.this) {
                        @Override
                        public void onFinishedProcessingWikiRequest(JSONObject response) {
                            verifyServerResponse(response);
                        }
                    });
                }
            }
            if (!isStyleSheetReplaced) {
                injectCSS(view);
            }
            super.onPageFinished(view, url);
        }

        private void injectCSS(WebView webView) {
            try {
                webView.loadUrl("javascript:(function() {" +
                        "var css = document.createElement(\"style\");\n" +
                        "css.type = \"text/css\";\n" +
                        "css.innerHTML = \"" + readFileAsString() + "\";\n" +
                        "document.body.appendChild(css);" +
                        "})()");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String readFileAsString() throws java.io.IOException {
            BufferedReader reader = null;
            StringBuilder sb = null;
            try {
                reader = new BufferedReader(new InputStreamReader(getAssets().open("style.css"), "UTF-8"));
                sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Utils.logDebug("Css string is ", sb != null ? sb.toString() : null);
            return sb != null ? sb.toString() : null;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(final WebView view, String url) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                if (url.contains("only=styles")) {
                    return replaceWebViewCss();
                }
            }
            return super.shouldInterceptRequest(view, url);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (request.getUrl().toString().contains("only=styles")) {
                    return replaceWebViewCss();
                }
            }
            return super.shouldInterceptRequest(view, request);
        }

        private WebResourceResponse replaceWebViewCss() {
            isStyleSheetReplaced = true;
            return getCssWebResourceResponseFromAsset();
        }

        private WebResourceResponse getCssWebResourceResponseFromAsset() {
            try {
                return getUtf8EncodedCssWebResourceResponse(getAssets().open("style.css"));
            } catch (IOException e) {
                return null;
            }
        }

        private WebResourceResponse getUtf8EncodedCssWebResourceResponse(InputStream data) {
            return new WebResourceResponse("text/css", "UTF-8", data);
        }

        @Override
        public void onPageCommitVisible(WebView view, String url) {
            if (!isPageChanged) {
                onPageChange();
                showProgressBar(false);
                isPageChanged = true;
            }
            super.onPageCommitVisible(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

    }
}
