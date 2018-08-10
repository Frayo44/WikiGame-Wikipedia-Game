package com.yoavfranco.wikigame.helpers;

import com.yoavfranco.wikigame.fragments.BaseScreen;
import com.yoavfranco.wikigame.fragments.ChallengeQuickPlayScreen;
import com.yoavfranco.wikigame.fragments.ChallengesScreen;
import com.yoavfranco.wikigame.fragments.ChooseModeChallengeScreen;
import com.yoavfranco.wikigame.fragments.ChooseModeScreen;
import com.yoavfranco.wikigame.fragments.FriendsScreen;
import com.yoavfranco.wikigame.fragments.HelpScreen;
import com.yoavfranco.wikigame.fragments.LeaderboardScreen;
import com.yoavfranco.wikigame.fragments.LevelsScreen;
import com.yoavfranco.wikigame.fragments.MainMenuScreen;
import com.yoavfranco.wikigame.fragments.QuickPlayScreen;
import com.yoavfranco.wikigame.fragments.RegisterationScreen;
import com.yoavfranco.wikigame.fragments.SettingsScreen;
import com.yoavfranco.wikigame.fragments.ShopScreen;

public class ScreensHolder {
    private BaseScreen[] screens = {
            new MainMenuScreen(),
            new LevelsScreen(),
            new ChallengesScreen(),
            new QuickPlayScreen(),
            new ShopScreen(),
            new HelpScreen(),
            new SettingsScreen(),
            new LeaderboardScreen(),
            new RegisterationScreen(),
            new ChallengeQuickPlayScreen(),
            new ChooseModeScreen(),
            new ChooseModeChallengeScreen(),
            new FriendsScreen()
    };

    public BaseScreen getMainMenu() {
        return screens[0];
    }

    public BaseScreen getLevels() {
        return screens[1];
    }

    public BaseScreen getChallenges() {
        return screens[2];
    }

    public BaseScreen getQuickPlay() {
        return screens[3];
    }

    public BaseScreen getShop() {
        return screens[4];
    }

    public BaseScreen getHelp() {
        return screens[5];
    }

    public BaseScreen getSettings() {
        return screens[6];
    }

    public BaseScreen getLeaderboards() {
        return screens[7];
    }

    public BaseScreen getRegisteration() {
        return screens[8];
    }

    public BaseScreen getChallengeQuickPlay() {
        return screens[9];
    }

    public BaseScreen getChooseMode() {
        return screens[10];
    }

    public BaseScreen getChooseModeChallenge() {
        return screens[11];
    }

    public BaseScreen getFriendsScreen() {
        return screens[12];
    }

    public void reInitialize(int index, BaseScreen value) {
        screens[index] = value;
    }
}
