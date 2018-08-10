package com.yoavfranco.wikigame.utils;

/**
 * Created by Tomer on 31/03/2017.
 */

public class WikipediaUtils {

    public static String WIKI_BASE_URL = "https://en.m.wikipedia.org/wiki/";

    public static String wikiURLForArticleName(String articleName) {
        return WIKI_BASE_URL + articleName.replaceAll(" ", "_");
    }
}
