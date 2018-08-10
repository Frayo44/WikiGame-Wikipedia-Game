package com.yoavfranco.wikigame.fragments;


import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.InputType;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.afollestad.materialdialogs.MaterialDialog;
import com.heinrichreimersoftware.singleinputform.steps.Step;
import com.heinrichreimersoftware.singleinputform.steps.TextStep;
import com.yoavfranco.wikigame.HTTP.WikiGameAPI;
import com.yoavfranco.wikigame.HTTP.WikiGameInterface;
import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.activities.MainActivity;
import com.yoavfranco.wikigame.helpers.SingleInputFragment;
import com.yoavfranco.wikigame.utils.Consts;
import com.yoavfranco.wikigame.utils.ErrorDialogs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yoav on 03/03/17.
 */

public class RegisterationScreen extends SingleInputFragment {

    WikiGameAPI wikiGameAPI;
    Action nextScreenAction;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        wikiGameAPI = new WikiGameAPI();
    }

    @Override
    protected List<Step> onCreateSteps() {
        final List<Step> steps = new ArrayList<>();

        setInputGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);

        steps.add(new TextStep.Builder(getActivity(), "email")
                .titleResId(R.string.username)
                .detailsResId(R.string.username_instructions)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                .validator(new TextStep.Validator() {
                    @Override
                    public boolean validate(String input) {
                        validateUsername(input);
                        return Patterns.EMAIL_ADDRESS.matcher(input).matches();
                    }
                })
                .build());

        return steps;
    }

    private void validateUsername(final String username) {

        final MaterialDialog loadingDialog = new MaterialDialog.Builder(getActivity())
                .title("Please Wait")
                .content("Registering...")
                .backgroundColor(Color.WHITE).contentColor(Color.BLACK).titleColor(Color.BLACK)
                .progress(true, 0)
                .show();

        wikiGameAPI.registerUserAsync(username, new WikiGameInterface(getActivity()) {
            @Override
            public void onFinishedProcessingWikiRequest(JSONObject response) {
                loadingDialog.dismiss();
                try {
                    switch (response.getString(Consts.STATUS_CODE_KEY)) {
                        case "USERNAME_EXISTS":
                            showError("Username already exists");
                            break;
                        case "INVALID_USERNAME":
                            showError("Username must be alphanumeric, 3-16 characters");
                            break;
                        case Consts.STATUS_OK:
                            finishRegiterationProcess(username);
                            break;
                        default:
                            ErrorDialogs.showSomethingWentWrongDialog(getActivity(), false);
                            loadingDialog.dismiss();
                            break;
                    }
                } catch (JSONException e) {
                    ErrorDialogs.showBadResponseDialog(getActivity(), false);
                    loadingDialog.dismiss();
                    e.printStackTrace();
                }
            }
        });
    }

    public void setNextScreenAction(Action nextScreenAction) {
        this.nextScreenAction = nextScreenAction;
    }

    private void finishRegiterationProcess(String username) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putString(Consts.KEY_USER_NAME, username);
        prefEditor.apply();
        ((MainActivity)getActivity()).onRegistrationCompleted(username);
        screenChanger.onScreenChange(this, nextScreenAction, null);

    }

    @Override
    protected void onFormFinished(Bundle data) {

    }


    @Override
    public void clear() {
        Animation slideDownAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_to_top);
        slideDownAnimation.setInterpolator(new FastOutSlowInInterpolator());
        slideDownAnimation.setFillAfter(true);
        containerScrollView.startAnimation(slideDownAnimation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (onClearListener != null)
                    onClearListener.clearDone();
            }
        }, 100);
    }
}
