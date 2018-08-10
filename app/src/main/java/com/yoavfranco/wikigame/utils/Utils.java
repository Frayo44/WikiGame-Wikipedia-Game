package com.yoavfranco.wikigame.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.yoavfranco.wikigame.BuildConfig;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class Utils {
    public static String getTag(Class c) {
        return c != null ? c.getSimpleName() : "com.yoavfranco.wikigame";
    }

    public static String getTag(Object o) {
        Class c = o.getClass();
        return c != null ? c.getSimpleName() : "com.yoavfranco.wikigame";
    }

    private static void log(LOG_FLAGS flag, Object var1, Object var2) {
        if (!BuildConfig.DEBUG)
            return;
        if (var1 != null && var2 != null)
            switch (flag) {
                case ERROR:
                    Log.e(var1.toString(), var2.toString());
                    break;
                case DEBUG:
                    Log.d(var1.toString(), var2.toString());
                    break;
                case INFO:
                    Log.i(var1.toString(), var2.toString());
                    break;
            }
    }

    public static <T> ArrayList<T> toList(T[] array) {
        return new ArrayList<T>(Arrays.asList(array));
    }

    public static void logDebug(Object o, Object var2) {
        log(LOG_FLAGS.DEBUG, getTag(o), var2);
    }

    public static void logDebug(String var1, Object var2) {
        log(LOG_FLAGS.DEBUG, var1, var2);
    }

    public static void logError(String var1, String var2) {
        log(LOG_FLAGS.ERROR, var1, var2);
    }

    public static void logInfo(String var1, String var2) {
        log(LOG_FLAGS.INFO, var1, var2);
    }

    public static void logInfo(Object o, Object var2) {
        log(LOG_FLAGS.INFO, getTag(o), var2);
    }

    public static boolean isAndroidNewerThanKK() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean isAndroidNewerThan(int buildNumber) {
        return Build.VERSION.SDK_INT >= buildNumber;
    }

    public static int randInt(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    public static void openPlayStoreUrl(String appName, Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
        } catch (Exception e) {
            Utils.openURL(context, "https://play.google.com/store/apps/details?id=" + appName);
        }
    }

    public static void openURL(Context context, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browserIntent);
    }

    public static String getUserCountry(Context context) {
        try {
             final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.US);
            }
            else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US);
                }
            }
        }
        catch (Exception e) { }
        return null;
    }

    public static String toServerURL(String subPath) {
        return "http://" + Consts.SERVER_HOST + ":" + Consts.SERVER_PORT + subPath.replace("\\", "/");
    }

    public static boolean isGuestUsername(String username) {
        return username.startsWith("_");
    }

    public static Date parseDate(String dateString) {
        SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        ISO8601DATEFORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return ISO8601DATEFORMAT.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getAppVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            return version;
        }
        catch (PackageManager.NameNotFoundException e) {
            return "0.0";
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private enum LOG_FLAGS {
        ERROR,
        DEBUG,
        INFO
    }

}
