package com.yoavfranco.wikigame.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tomer on 09/12/2016.
 */

public class JSONUtils {
    public static String[] ToStringsArray(JSONArray arr) {
        List<String> list = new ArrayList<>();
        try {
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.getString(i));
            }
        } catch (JSONException e) {
            return null;
        }

        String[] array = new String[list.size()];
        array = list.toArray(array);
        return array;
    }

    public static boolean isValidJSON(String string) {
        try {
            new JSONObject(string);
        } catch (JSONException ex) {
            // may be a valid JSONArray as well...
            try {
                new JSONArray(string);
            } catch (JSONException ex1) {
                return false;
            }
        }

        return true;
    }
}
