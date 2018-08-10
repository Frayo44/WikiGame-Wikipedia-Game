package com.yoavfranco.wikigame.utils;

/**
 * Created by yoav on 25/11/16.
 */

public class Consts {
    // For server wikipedia page parsing
    public static final String TYPE_MAIN_TITLE = "MAIN_TITLE";
    public static final String TYPE_MAIN_IMAGE_URL = "MAIN_IMAGE_URL";
    public static final String TYPE_TITLE = "TITLE";
    public static final String TYPE_PARAGRAPH = "PARAGRAPH";
    public static final String TYPE_IMAGE = "IMAGE";
    public static final String TYPE_TABLE = "TABLE";
    public static final String TYPE_QUOTE = "QUOTE";
    public static final String TYPE_SUBTITLE = "SUBTITLE";
    public static final String TYPE_SUB_SUBTITLE = "SUB_SUBTITLE";
    public static final String TYPE_LIST = "LIST";
    public static final String TYPE_LIST_ITEM = "LIST_ITEM";
    public static final String LIST_TYPE_BULLETED = "BULLETED";
    public static final String LIST_TYPE_NUMBERED = "NUMBERED";
    public static final String LIST_TYPE_INDENTED = "INDENTED";
    public static final String IMAGE_SIDE_NONE = "NONE";
    public static final String IMAGE_SIDE_RIGHT = "RIGHT";
    public static final String IMAGE_SIDE_LEFT = "LEFT";

    public static final String STATUS_CODE_KEY = "status_code";
    public static final String STATUS_OK = "OK";
    public static final String STATUS_INVALID_CRENTIALS = "INVALID_CREDENTIALS";
    public static final String STATUS_NOT_LOGGED_IN = "NOT_LOGGED_IN";
    public static final String KEY_START_ARTICLE = "start_article";
    public static final String KEY_TARGET_ARTICLE = "target_article";
    public static final String KEY_USER_NAME = "user__name";
    public static final String KEY_USER_PASSWORD = "user__password";
    public static final String KEY_APP_VERSION = "app_version";
    public static final String KEY_DONT_SHOW_AGAIN_VERSION = "dont_show_again_version";
    public static final String KEY_ACHIEVEMENTS = "achievements";
    public static final String KEY_NEW_LEVEL = "new_level";

    // For extras
    public static final String BUNDLE_LEVEL0_FIRST_ARTICLE_KEY = "first_article";
    public static final String BUNDLE_LEVELS_KEY = "game_levels";
    public static final String BUNDLE_INIT_LEVEL_KEY = "init_level";
    public static final String BUNDLE_LEVEL0_TARGET_ARTICLE_KEY = "target_article";
    public static final String BUNDLE_LEVEL_KEY = "level";

    // HTTP configurations
    // use "10.0.3.2" for a Genymotion emulator running on the same computer
    public static final String SERVER_HOST  = "wikipedia-game.herokuapp.com";
    public static final int SERVER_PORT = 80;

    // WikiDisplay results states
    public static final int UPDATE_MODE_NEW_LEVEL_UNLOCKED = 0;
    public static final int UPDATE_MODE_SUCCESS_LEVEL = 1;
    public static final int UPDATE_MODE_SUCCESS_PRACTICE_MODE = 2;
    public static final int UPDATE_MODE_CHALLENGE_SENT = 3;
    public static final int UPDATE_MODE_CHALLENGE_REMOVED = 4;
    public static final int UPDATE_MODE_CHALLENGE_REMOVED_AND_CREATING_NEW = 5;
    public static final int UPDATE_MODE_QUICK_PLAY_VALUES_CHANGED = 6;

    // Modes
    public static final String TIMER_MODE = "time";
    public static final String CLICKS_MODE = "clicks";

    // Random values array
    public static final String RANDOM_ARTICLES_ARRAY[] = new String[]{"United States", "Israel", "Syria", "Apple", "Horse", "Green", "Donald Trump", "Barack Obama", "The Beatles", "Eiffel Tower", "California", "Mango", "Starbucks", "Gold", "World War II", "Family Guy", "Harry Potter", "Bird"};

}
