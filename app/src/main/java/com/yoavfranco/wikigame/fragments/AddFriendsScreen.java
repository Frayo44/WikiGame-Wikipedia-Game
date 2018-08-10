package com.yoavfranco.wikigame.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import com.heinrichreimersoftware.singleinputform.steps.CheckBoxStep;
import com.heinrichreimersoftware.singleinputform.steps.Step;
import com.heinrichreimersoftware.singleinputform.steps.TextStep;
import com.yoavfranco.wikigame.HTTP.WikiGameAPI;
import com.yoavfranco.wikigame.HTTP.WikiGameInterface;
import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.activities.MainActivity;
import com.yoavfranco.wikigame.helpers.SingleInputFragment;
import com.yoavfranco.wikigame.utils.Consts;
import com.yoavfranco.wikigame.utils.Friend;
import com.yoavfranco.wikigame.utils.FriendRequest;
import com.yoavfranco.wikigame.utils.SuggestedFriend;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yoav on 03/03/17.
 */

public class AddFriendsScreen extends SingleInputFragment {

    WikiGameAPI wikiGameAPI;
    FriendRequest sentRequest;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        wikiGameAPI = new WikiGameAPI();
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            SuggestedFriend suggestedFriend = (SuggestedFriend) bundle.getSerializable("suggested_friend");
            ((TextStep) getStep(0)).setText(suggestedFriend.getUsername());
        }
    }

    @Override
    protected List<Step> onCreateSteps() {
        final List<Step> steps = new ArrayList<>();

        setInputGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);

        steps.add(new TextStep.Builder(getActivity(), "username")
                .title("Add Friend")
                .details("Please enter a username to add as a friend and optionally send a challenge to him.")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .validator(new TextStep.Validator() {
                    @Override
                    public boolean validate(String input) {
                        if (isValidLengthUsername(input))
                            sendFriendRequest(input);
                        else
                            showError("Usernames must be 3-16 characters!");
                        return false;
                    }
                })
                .build());

        steps.add(new CheckBoxStep.Builder(getActivity(), "send_challenge")
                .title("Send a challenge too?")
                .details("Mark the checkbox to start a challenged game now")
                .build());

        return steps;
    }

    private boolean isValidLengthUsername(String username) {
        return username.length() >= 3 && username.length() < 16;
    }

    private void sendFriendRequest(final String username)
    {
        wikiGameAPI.sendFriendRequestAsync(username, new WikiGameInterface(getActivity()) {
            @Override
            public void onFinishedProcessingWikiRequest(JSONObject response) {
                try {
                    switch(response.getString(Consts.STATUS_CODE_KEY)) {
                        case "USERNAME_DOES_NOT_EXIST":
                            showError("This username does not exist!");
                            break;
                        case "INVALID_USERNAME":
                            showError("This username is invalid!");
                            break;
                        case "FRIEND_REQUEST_ALREADY_SENT":
                            showError("Friend request was already sent/received!");
                        case Consts.STATUS_OK:
                            sentRequest = FriendRequest.fromJSON(response.getJSONObject("sent_request"));
                            finishSendFriendRequestProcess(sentRequest);
                            nextStep2();
                            break;
                        default:
                            showError("An unknown error has occurred");
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void finishSendFriendRequestProcess(FriendRequest friendRequest)
    {
        MainActivity mainActivity = ((MainActivity)getActivity());
        mainActivity.addFriendRequest(friendRequest);

        // Close the keyboard
        View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onFormFinished(Bundle data) {
        boolean checkes = data.getBundle("send_challenge").getBoolean("data_checked");
        String username = data.getBundle("username").getString("data_text");

        if (checkes) {
            getActivity().getFragmentManager().popBackStack();
            Intent extra = new Intent();
            extra.putExtra("friend", new Friend(sentRequest.getReceiverUsername(),sentRequest.getCountryCode(), sentRequest.getFlagURL()));
            screenChanger.onScreenChange(AddFriendsScreen.this, Action.CHOOSE_MODE_CHALLENGE, extra);
        } else {
            getActivity().onBackPressed();
        }

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
