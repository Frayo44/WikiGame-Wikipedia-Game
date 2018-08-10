package com.yoavfranco.wikigame.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.widget.TextView;

import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.fragments.BaseScreen;
import com.yoavfranco.wikigame.fragments.ChallengeQuickPlayScreen;
import com.yoavfranco.wikigame.fragments.ChallengesScreen;
import com.yoavfranco.wikigame.fragments.ChooseModeChallengeScreen;
import com.yoavfranco.wikigame.fragments.FriendsScreen;
import com.yoavfranco.wikigame.fragments.LevelsScreen;
import com.yoavfranco.wikigame.fragments.MainMenuScreen;
import com.yoavfranco.wikigame.fragments.QuickPlayScreen;
import com.yoavfranco.wikigame.fragments.RegisterationScreen;
import com.yoavfranco.wikigame.helpers.ScreensHolder;
import com.yoavfranco.wikigame.utils.Challenge;
import com.yoavfranco.wikigame.utils.Consts;
import com.yoavfranco.wikigame.utils.Friend;
import com.yoavfranco.wikigame.utils.FriendRequest;
import com.yoavfranco.wikigame.utils.Level;
import com.yoavfranco.wikigame.utils.SuggestedFriend;
import com.yoavfranco.wikigame.utils.SocialInfo;
import com.yoavfranco.wikigame.utils.UserInfo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.nicolaspomepuy.discreetapprate.AppRate;


public class MainActivity extends GameActivity implements BaseScreen.ScreenChanger {

    @BindView(R.id.back)
    AppCompatImageButton back;
    @BindView(R.id.settings)
    public AppCompatImageView settings;
    private ScreensHolder screensHolder;
    @BindView(R.id.tvLevelName)
    public TextView topBarTextView;

    private int maximumAllowedClicks;
    private int maximumAllowedTime;

    private UserInfo userInfo;
    private SocialInfo socialInfo;
    private ArrayList<SuggestedFriend> suggestedFriends;
    private ArrayList<Level> levels;
    private Level practiceLevel;

    private ArrayList<Friend> yourTurnFriends;
    private ArrayList<Friend> theirTurnFriends;

    private final String CLOSABLE_TRANSITION = "closable_transition";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        theirTurnFriends = new ArrayList<>();
        yourTurnFriends = new ArrayList<>();

        this.maximumAllowedClicks = getIntent().getIntExtra("maximum_allowed_clicks", 100);
        this.maximumAllowedTime = getIntent().getIntExtra("maximum_allowed_time", 3600);
        this.userInfo = ((UserInfo) getIntent().getSerializableExtra("user_info"));
        this.socialInfo = ((SocialInfo) getIntent().getSerializableExtra("social_info"));
        levels = (ArrayList<Level>) getIntent().getSerializableExtra(Consts.BUNDLE_LEVELS_KEY);
        suggestedFriends = (ArrayList<SuggestedFriend>) getIntent().getSerializableExtra("suggested_friends");
        practiceLevel = new Level("practice level", false, "both", 0, 0);

        sortOpponentsToTurns();

        screensHolder = new ScreensHolder();
        setScreen(screensHolder.getMainMenu(), false, "main_menu");
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onScreenChange((BaseScreen) getFragmentManager().findFragmentById(R.id.fragment_container), BaseScreen.Action.SETTINGS, null);
            }
        });

        populateScreensWithLocalData();

        AppRate.with(this).checkAndShow();

    }

    private void backCheck(BaseScreen screen) {
        if (screen instanceof MainMenuScreen) {
            back.setVisibility(View.INVISIBLE);
            settings.setImageResource(R.drawable.ic_settings);
            settings.setClickable(true);
        }
        else {
            settings.setClickable(false);
            back.setVisibility(View.VISIBLE);
        }

        if(screen instanceof MainMenuScreen || screen instanceof ChallengesScreen || screen instanceof LevelsScreen) {
            topBarTextView.setVisibility(View.GONE);
        }

        if (screen instanceof MainMenuScreen || screen instanceof QuickPlayScreen || screen instanceof ChallengeQuickPlayScreen) {
            settings.animate().alpha(1.0f).setDuration(200);
            settings.setVisibility(View.VISIBLE);
        } else {
            settings.animate().alpha(0.0f).setDuration(200);
        }
    }

    public void setScreen(BaseScreen fragment) {
        setScreen(fragment, true, fragment.getClass().getName());
    }

    public void setScreen(BaseScreen fragment, boolean addToStack, String transitionName) {

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        if (addToStack) fragmentTransaction.addToBackStack(transitionName);
        fragmentTransaction.commit();
    }

    public void cleanBackStack(String transitionName) {
        BaseScreen.disableFragmentsAnimations = true;
        FragmentManager fm = getFragmentManager();
        /*
        int backStackCount = fm.getBackStackEntryCount();
        for (int i = 0; i < backStackCount; i++) {
            fm.popBackStackImmediate();
        }
        */
        fm.popBackStack(transitionName, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        BaseScreen.disableFragmentsAnimations = false;
    }

    @Override
    public void onScreenChange(final BaseScreen screen, final BaseScreen.Action action, final Intent extras) {
        switch (action) {
            case CHECK_BACK:
                backCheck(screen);
                break;
            case QUICKPLAY:
                screensHolder.reInitialize(3, new QuickPlayScreen());
                practiceLevel.setMode(extras.getStringExtra("mode"));
                screen.setOnClearListener(new BaseScreen.OnClearListener() {
                    @Override
                    public void clearDone() {
                        QuickPlayScreen quickPlayScreen = (QuickPlayScreen) screensHolder.getQuickPlay();
                        quickPlayScreen.setLevel(practiceLevel);
                        quickPlayScreen.shuffleArticles();
                        setScreen(quickPlayScreen, true, CLOSABLE_TRANSITION);
                    }
                });
                screen.clear();
                break;
            case LEVELS:
                populateScreensWithLocalData();
                screen.setOnClearListener(new BaseScreen.OnClearListener() {
                    @Override
                    public void clearDone() {
                        setScreen(screensHolder.getLevels());
                    }
                });
                screen.clear();
                break;
            case CHOOSE_MODE_CHALLENGE:
                screen.setOnClearListener(new BaseScreen.OnClearListener() {
                    @Override
                    public void clearDone() {
                        ((ChooseModeChallengeScreen) screensHolder.getChooseModeChallenge()).setFriend((Friend) extras.getSerializableExtra("friend"));
                        setScreen(screensHolder.getChooseModeChallenge(), true, CLOSABLE_TRANSITION);
                    }
                });
                screen.clear();
                break;
            case CHALLENGE_QUICKPLAY:
                screen.setOnClearListener(new BaseScreen.OnClearListener() {
                    @Override
                    public void clearDone() {
                        screensHolder.reInitialize(9, new ChallengeQuickPlayScreen());
                        ChallengeQuickPlayScreen challengeQuickPlayScreen = (ChallengeQuickPlayScreen) screensHolder.getChallengeQuickPlay();

                        String selectedMode = extras.getStringExtra("mode");
                        Friend friend = (Friend) extras.getSerializableExtra("friend");
                        challengeQuickPlayScreen.setFriend(friend);
                        if (friend.getChallenge() == null) {
                            challengeQuickPlayScreen.setSelectedMode(selectedMode);
                        }
                        setScreen(screensHolder.getChallengeQuickPlay(), true, CLOSABLE_TRANSITION);
                    }
                });
                screen.clear();
                break;
            case CHALLENGES:
                screen.setOnClearListener(new BaseScreen.OnClearListener() {
                    @Override
                    public void clearDone() {
                        if (!isUserRegistered()) {
                            ((RegisterationScreen) screensHolder.getRegisteration()).setNextScreenAction(BaseScreen.Action.CHALLENGES);
                            setScreen(screensHolder.getRegisteration());
                        } else {
                            removeScreen(screensHolder.getRegisteration());
                            populateScreensWithLocalData();
                            setScreen(screensHolder.getChallenges());
                        }
                    }
                });
                screen.clear();
                break;
            case CHOOSE_MODE_PRACTICE:
                screen.setOnClearListener(new BaseScreen.OnClearListener() {
                    @Override
                    public void clearDone() {
                        setScreen(screensHolder.getChooseMode(), true, CLOSABLE_TRANSITION);
                    }
                });
                screen.clear();
                break;
            case LEVEL_CLICK:
                screensHolder.reInitialize(3, new QuickPlayScreen());
                ((QuickPlayScreen) screensHolder.getQuickPlay()).setLevel((Level) extras.getSerializableExtra(Consts.BUNDLE_LEVEL_KEY));
                ((QuickPlayScreen) screensHolder.getQuickPlay()).shuffleArticles();
                setScreen(screensHolder.getQuickPlay(), true, CLOSABLE_TRANSITION);
                screen.clear();
                break;
            case FRIENDS:
                screen.setOnClearListener(new BaseScreen.OnClearListener() {
                    @Override
                    public void clearDone() {
                        if (!isUserRegistered()) {
                            ((RegisterationScreen) screensHolder.getRegisteration()).setNextScreenAction(BaseScreen.Action.FRIENDS);
                            setScreen(screensHolder.getRegisteration());
                        } else {
                            removeScreen(screensHolder.getRegisteration());
                            setScreen(screensHolder.getFriendsScreen());
                        }
                    }
                });
                screen.clear();
                break;
            case HELP:
                screen.setOnClearListener(new BaseScreen.OnClearListener() {
                    @Override
                    public void clearDone() {
                        setScreen(screensHolder.getHelp());
                    }
                });
                screen.clear();
                break;
            case LEADERBOARDS:
                screen.setOnClearListener(new BaseScreen.OnClearListener() {
                    @Override
                    public void clearDone() {
                        if (!isUserRegistered()) {
                            ((RegisterationScreen) screensHolder.getRegisteration()).setNextScreenAction(BaseScreen.Action.LEADERBOARDS);
                            setScreen(screensHolder.getRegisteration());
                        } else {
                            removeScreen(screensHolder.getRegisteration());
                            setScreen(screensHolder.getLeaderboards());
                        }
                    }
                });
                screen.clear();
                break;
            case SHOP:
                screen.setOnClearListener(new BaseScreen.OnClearListener() {
                    @Override
                    public void clearDone() {
                        setScreen(screensHolder.getShop());
                    }
                });
                screen.clear();
                break;
            case SETTINGS:
                screen.setOnClearListener(new BaseScreen.OnClearListener() {
                    @Override
                    public void clearDone() {
                        setScreen(screensHolder.getSettings());
                    }
                });
                screen.clear();
                break;
        }
    }

    private void removeScreen(BaseScreen screen) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove(screen);
        trans.commit();
        manager.popBackStack();
    }

    private boolean isUserRegistered() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String prefsUsername = prefs.getString(Consts.KEY_USER_NAME, null);
        return prefsUsername != null && !prefsUsername.startsWith("_");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode != 1 || data == null) return;

        final int updateMode = data.getIntExtra("updateMode", -1);
        final boolean shouldCloseFragments = data.getBooleanExtra("shouldCloseFragments", false);
        new Handler().post(new Runnable() {
            public void run() {
                if(shouldCloseFragments) {
                    // closing all transitions that have the CLOSABLE_TRANSITION flag set
                    cleanBackStack(CLOSABLE_TRANSITION);
                }
                final QuickPlayScreen quickPlayScreen = (QuickPlayScreen) screensHolder.getQuickPlay();
                switch (updateMode) {
                    case Consts.UPDATE_MODE_SUCCESS_PRACTICE_MODE:
                        if (!shouldCloseFragments) {
                            quickPlayScreen.shuffleArticles();
                        }
                        break;
                    case Consts.UPDATE_MODE_NEW_LEVEL_UNLOCKED: {
                            final Level newLevel = (Level) data.getSerializableExtra(Consts.BUNDLE_LEVEL_KEY);
                            final int pointsAdded = data.getIntExtra("pointsAdded", 0);
                            final int newBest = data.getIntExtra("newBest", 0);
                            int level = Integer.parseInt(newLevel.getLevelName()) - 1;
                            onNewBestForLevel(level + "", newBest, pointsAdded);

                            levels.set(Integer.parseInt(newLevel.getLevelName()) - 1, newLevel);
                            Level[] levelsArr = new Level[levels.size()];
                            levels.toArray(levelsArr);
                            ((LevelsScreen) screensHolder.getLevels()).setLevels(levelsArr);
                            ((LevelsScreen) screensHolder.getLevels()).updateUI();
                            if (!shouldCloseFragments) {
                                quickPlayScreen.setLevel(newLevel);
                                quickPlayScreen.updateUI();
                                quickPlayScreen.shuffleArticles();
                            }
                        }
                        break;
                    case Consts.UPDATE_MODE_SUCCESS_LEVEL: {
                            if (!shouldCloseFragments) {
                                if (data.hasExtra("pointsAdded")) {
                                    final int pointsAdded = data.getIntExtra("pointsAdded", 0);
                                    final int newBest = data.getIntExtra("newBest", 0);
                                    onNewBestForLevel(((QuickPlayScreen) screensHolder.getQuickPlay()).getLevel().getLevelName(), newBest, pointsAdded);
                                }
                                Level nextLevel = levels.get(Integer.parseInt(((QuickPlayScreen) screensHolder.getQuickPlay()).getLevel().getLevelName()));
                                quickPlayScreen.setLevel(nextLevel);
                                quickPlayScreen.updateUI();
                                quickPlayScreen.shuffleArticles();
                            }
                        }
                        break;
                    case Consts.UPDATE_MODE_CHALLENGE_REMOVED: {
                        Friend friend = (Friend) data.getSerializableExtra("friend");
                        String challengeStatus = data.getStringExtra("challengeStatus");
                        removeChallengeFromFriend(friend, challengeStatus);
                    }
                    break;
                    case Consts.UPDATE_MODE_CHALLENGE_REMOVED_AND_CREATING_NEW: {
                        Friend friend = (Friend) data.getSerializableExtra("friend");
                        friend.setChallenge(null);
                        Intent intent = new Intent();
                        intent.putExtra("friend", friend);
                        onScreenChange(screensHolder.getChallenges(), BaseScreen.Action.CHOOSE_MODE_CHALLENGE, intent);
                        String challengeStatus = data.getStringExtra("challengeStatus");
                        removeChallengeFromFriend(friend, challengeStatus);
                    }
                    break;
                    case Consts.UPDATE_MODE_CHALLENGE_SENT: {
                        Friend friend = (Friend) data.getSerializableExtra("friend");
                        moveFriendToTheirTurn(friend);
                    }
                    break;
                }
            }
        });
    }

    public void sortOpponentsToTurns() {
        this.theirTurnFriends = new ArrayList<>();
        this.yourTurnFriends = new ArrayList<>();

        // sort friends
        ArrayList<Friend> userFriends = this.socialInfo.getFriends();
        for (int i = 0; i < userFriends.size(); i++) {
            if (userFriends.get(i).isHisTurn())
                theirTurnFriends.add(userFriends.get(i));
            else
                yourTurnFriends.add(userFriends.get(i));
        }

        // sort strangers (that is, people that you sent them friend requests)
        ArrayList<FriendRequest> sentFriendRequests = this.socialInfo.getSentFriendRequests();
        ArrayList<FriendRequest> pendingFriendRequests = this.socialInfo.getPendingFriendRequests();
        for (int i = 0; i < sentFriendRequests.size(); i++) {
            FriendRequest friendRequest = sentFriendRequests.get(i);
            Friend stranger = new Friend(friendRequest.getReceiverUsername(), friendRequest.getCountryCode(), friendRequest.getFlagURL());
            Challenge challenge = this.socialInfo.getChallengeToStranger(friendRequest.getReceiverUsername());
            stranger.setChallenge(challenge);
            if (challenge != null)
                theirTurnFriends.add(stranger);
            else
                yourTurnFriends.add(stranger);
        }
        /*
        for (int i = 0; i < pendingFriendRequests.size(); i++) {
            FriendRequest friendRequest = pendingFriendRequests.get(i);
            Friend stranger = new Friend(friendRequest.getSenderUsername(), friendRequest.getCountryCode(), friendRequest.getFlagURL());
            Challenge challenge = this.socialInfo.getChallengeFromStranger(friendRequest.getSenderUsername());
            stranger.setChallenge(challenge);
            if (challenge != null)
                yourTurnFriends.add(stranger);
            else
                theirTurnFriends.add(stranger);
        }
        */
    }

    public void moveFriendToTheirTurn(Friend friend) {
        this.theirTurnFriends.add(friend);
        this.yourTurnFriends.remove(friend);
        populateScreensWithLocalData();
        ((ChallengesScreen) screensHolder.getChallenges()).updateUI(false);
    }

    public void removeChallengeFromFriend(Friend friend, String challengeStatus) {
        int victoriesBonus = !challengeStatus.equals("not_accepted") ? 1 : 0;
        int loosesBonus = !challengeStatus.equals("accepted") ? 1 : 0;
        for (int i=0;i<this.yourTurnFriends.size();i++) {
            if (this.yourTurnFriends.get(i).getUsername().equals(friend.getUsername())) {
                this.yourTurnFriends.get(i).setChallenge(null);
                this.yourTurnFriends.get(i).setVictories(this.yourTurnFriends.get(i).getVictories() + victoriesBonus);
                this.yourTurnFriends.get(i).setLooses(this.yourTurnFriends.get(i).getLooses() + loosesBonus);
            }
        }
        for (int i=0;i<this.theirTurnFriends.size();i++) {
            if (this.theirTurnFriends.get(i).getUsername().equals(friend.getUsername())) {
                this.theirTurnFriends.get(i).setChallenge(null);
                this.theirTurnFriends.get(i).setVictories(this.theirTurnFriends.get(i).getVictories() + victoriesBonus);
                this.theirTurnFriends.get(i).setLooses(this.theirTurnFriends.get(i).getLooses() + loosesBonus);
            }
        }
        populateScreensWithLocalData();
        ((ChallengesScreen) screensHolder.getChallenges()).updateUI(false);
    }

    public void addFriendRequest(FriendRequest friendRequest) {
        this.socialInfo.getSentFriendRequests().add(friendRequest);
        yourTurnFriends.add(new Friend(friendRequest.getReceiverUsername(), friendRequest.getCountryCode(), friendRequest.getFlagURL()));
        populateScreensWithLocalData();
        ((ChallengesScreen)screensHolder.getChallenges()).updateUI(false);
        ((FriendsScreen)screensHolder.getFriendsScreen()).updateUI(false);
    }

    public void populateScreensWithLocalData() {
        ArrayList<FriendRequest> sentFriendRequests = this.socialInfo.getSentFriendRequests();
        ArrayList<FriendRequest> pendingFriendRequests = this.socialInfo.getPendingFriendRequests();

        FriendsScreen friendsScreen = (FriendsScreen) screensHolder.getFriendsScreen();
        friendsScreen.setSentFriendRequests(sentFriendRequests);
        friendsScreen.setPendingFriendRequests(pendingFriendRequests);
        friendsScreen.setSuggestedFriends(suggestedFriends);

        ChallengesScreen challengesScreen = ((ChallengesScreen)screensHolder.getChallenges());
        challengesScreen.setTheirTurnFriends(theirTurnFriends);
        challengesScreen.setYourTurnFriends(yourTurnFriends);
        challengesScreen.setSuggestedFriends(suggestedFriends);

        Level[] levelsArr = new Level[levels.size()];
        levels.toArray(levelsArr);
        ((LevelsScreen) screensHolder.getLevels()).setLevels(levelsArr);
    }

    public void addFriend(Friend friend) {
        this.socialInfo.getFriends().add(friend);
        sortOpponentsToTurns();
    }

    public void onNewBestForLevel(String levelName, int newBest, int pointsAdded) {
        for (int i=0;i<this.levels.size();i++) {
            if (this.levels.get(i).getLevelName().equals(levelName)) {
                this.levels.get(i).setBest(newBest);
            }
        }
        this.getUserInfo().setTotalPoints(this.getUserInfo().getTotalPoints() + pointsAdded);
    }

    public void updateFriendsData(ArrayList<FriendRequest> pendingFriendRequests, ArrayList<FriendRequest> sentFriendRequests, ArrayList<SuggestedFriend> suggestedFriends) {
        this.socialInfo.setPendingFriendRequests(pendingFriendRequests);
        this.socialInfo.setSentFriendRequests(sentFriendRequests);
        this.suggestedFriends = suggestedFriends;
    }

    public void updateSocialInfo(SocialInfo socialInfo, ArrayList<SuggestedFriend> suggestedFriends) {
        this.socialInfo = socialInfo;
        this.suggestedFriends = suggestedFriends;
        sortOpponentsToTurns();
    }

    public ScreensHolder getScreensHolder() {
        return screensHolder;
    }

    public void setScreensHolder(ScreensHolder screensHolder) {
        this.screensHolder = screensHolder;
    }

    public void onRegistrationCompleted(String newUsername) {
        this.userInfo.setUsername(newUsername);
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public ArrayList<Level> getLevels() {
        return levels;
    }

    public void setLevels(ArrayList<Level> levels) {
        this.levels = levels;
    }

    public int getMaximumAllowedClicks() {
        return maximumAllowedClicks;
    }

    public void setMaximumAllowedClicks(int maximumAllowedClicks) {
        this.maximumAllowedClicks = maximumAllowedClicks;
    }

    public int getMaximumAllowedTime() {
        return maximumAllowedTime;
    }

    public void setMaximumAllowedTime(int maximumAllowedTime) {
        this.maximumAllowedTime = maximumAllowedTime;
    }
}
