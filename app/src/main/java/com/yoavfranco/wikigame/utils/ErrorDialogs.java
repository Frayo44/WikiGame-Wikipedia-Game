package com.yoavfranco.wikigame.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.yoavfranco.wikigame.R;

/**
 * Created by Tomer on 09/12/2016.
 */

public class ErrorDialogs {

    public static void showNetworkErrorDialog(final Activity context, final boolean shouldEndActivity) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        new MaterialStyledDialog.Builder(context)
                .setTitle(R.string.error_1_no_connection)
                .setDescription(R.string.error_1_no_connection_desc)
                .setIcon(R.drawable.ic_no_internet_connection)
                .setCancelable(false)
                .withIconAnimation(false)
                .withDivider(true)
                .setPositiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        if (shouldEndActivity) context.finish();
                    }
                })
                .show();
    }

    public static void showBadResponseDialog(final Activity context, final boolean shouldEndActivity) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        new MaterialStyledDialog.Builder(context)
                .setTitle("Bad response")
                .setDescription("The server returned me something I did not expect!")
                .setIcon(R.drawable.ic_no_internet_connection)
                .setCancelable(false)
                .withIconAnimation(false)
                .withDivider(true)
                .setPositiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        if (shouldEndActivity) context.finish();
                    }
                })
                .show();
    }

    public static void showSomethingWentWrongDialog(final Activity context, final boolean shouldEndActivity) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        new MaterialStyledDialog.Builder(context)
                .setTitle("Whoops...")
                .setDescription("Something went wrong. Maybe try restarting the app.")
                .setIcon(R.drawable.ic_no_internet_connection)
                .setCancelable(false)
                .withIconAnimation(false)
                .withDivider(true)
                .setPositiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        if (shouldEndActivity) context.finish();
                    }
                })
                .show();
    }

    public static void showOnlyWikipediaDialog(final Activity context) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        new MaterialStyledDialog.Builder(context)
                .setTitle("Only wikipedia!")
                .setDescription("You can't follow external links.")
                .setCancelable(false)
                .withIconAnimation(false)
                .withDivider(true)
                .setPositiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
