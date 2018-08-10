package com.yoavfranco.wikigame.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.squareup.picasso.Picasso;
import com.yoavfranco.wikigame.HTTP.WikiGameAPI;
import com.yoavfranco.wikigame.HTTP.WikiGameInterface;
import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.activities.LoadingActivity;
import com.yoavfranco.wikigame.activities.MainActivity;
import com.yoavfranco.wikigame.adapters.AboutAdapter;
import com.yoavfranco.wikigame.utils.Consts;
import com.yoavfranco.wikigame.utils.ErrorDialogs;
import com.yoavfranco.wikigame.utils.Item;
import com.yoavfranco.wikigame.utils.Level;
import com.yoavfranco.wikigame.utils.SocialInfo;
import com.yoavfranco.wikigame.utils.SuggestedFriend;
import com.yoavfranco.wikigame.utils.UserInfo;
import com.yoavfranco.wikigame.utils.Utils;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SettingsScreen extends BaseScreen {

    SocialInfo socialInfo;
    WikiGameAPI wikiGameAPI;

    ListView listView;
    TextView tvUserName;
    TextView tvPoints;
    ImageView userCountryImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_screen, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = (ListView) view.findViewById(R.id.lvAbout);
        tvUserName = (TextView) view.findViewById(R.id.tvUserName);
        tvPoints = (TextView) view.findViewById(R.id.tvPoints);
        userCountryImageView = (ImageView)view.findViewById(R.id.userCountryImageView);
        updateUI();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT,
                                "Hey! Download WikiGame at https://play.google.com/store/apps/details?id=com.yoavfranco.wikigame");
                        sendIntent.setType("text/plain");
                        startActivity(sendIntent);
                        break;
                    case 1:
                        boolean notificationsEnabled = ((MainActivity)getActivity()).getUserInfo().isNotificationsEnabled();
                        if (notificationsEnabled) {
                            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
                            new MaterialStyledDialog.Builder(getActivity())
                                    // TODO: add strings to xml/strings file
                                    .setTitle("Disable notifications?")
                                    .setDescription("You won't be notified for new friend requests or new challenges.\nYou can always re-enable notifications later in this screen.")
                                    .setStyle(Style.HEADER_WITH_TITLE)
                                    .setCancelable(false)
                                    .withIconAnimation(false)
                                    .withDialogAnimation(true)
                                    .withDivider(true)
                                    .setPositiveText("DISABLE")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.dismiss();
                                            disableNotifications();
                                        }
                                    }).setNegativeText("CANCEL")
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }
                        else {
                            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
                            new MaterialStyledDialog.Builder(getActivity())
                                    // TODO: add strings to xml/strings file
                                    .setTitle("Enable notifications?")
                                    .setDescription("You will be notified for new friend requests or new challenges.\nYou can always disable notifications later in this screen.")
                                    .setStyle(Style.HEADER_WITH_TITLE)
                                    .setCancelable(false)
                                    .withIconAnimation(false)
                                    .withDialogAnimation(true)
                                    .withDivider(true)
                                    .setPositiveText("ENABLE")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.dismiss();
                                            enableNotifications();
                                        }
                                    }).setNegativeText("CANCEL")
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }
                        break;
                    case 2:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.yoavfranco.wikigame")));
                        break;
                    case 3:
                        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
                        new MaterialStyledDialog.Builder(getActivity())
                                // TODO: add strings to xml/strings file
                                .setTitle("Warning")
                                .setDescription("This account's credentials will be deleted from this device and you will become a guest again. \nThis is an irreversible process!")
                                .setStyle(Style.HEADER_WITH_TITLE)
                                .setCancelable(false)
                                .withIconAnimation(false)
                                .withDialogAnimation(true)
                                .withDivider(true)
                                .setPositiveText("LOG OUT")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                        logOut();
                                    }
                                }).setNegativeText("CANCEL")
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                        break;
                    case 4:
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"yoav.francoo@gmail.com"});
                        i.putExtra(Intent.EXTRA_SUBJECT, "WikiGame feedback");
                        try {
                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });

          /*
          new MaterialDialog.Builder(getActivity())
                                .title("New Username")
                                .content("Type your new username")
                                .inputRangeRes(3, 15, R.color.material_red_500)
                                .inputType(InputType.TYPE_CLASS_TEXT)
                                .alwaysCallInputCallback()
                                .input("Username", null, new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog dialog, CharSequence input) {
                                        Pattern pattern = Pattern.compile("[~`!@#$%^&*()]");
                                        Matcher matcher = pattern.matcher(input);
                                        if (matcher.find()) {
                                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                           // dialog.setE
                                        } else {
                                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                        }

                                    }
                                }).show();
                        break;
          */
    }

    public void updateUI() {
        listView.setFocusable(true);
        listView.setClickable(true);
        ArrayList<Item> items = new ArrayList<>();

        wikiGameAPI = new WikiGameAPI();

        UserInfo userInfo = ((MainActivity)getActivity()).getUserInfo();

        boolean isGuest = Utils.isGuestUsername(userInfo.getUsername());
        boolean notificationsEnabled = ((MainActivity)getActivity()).getUserInfo().isNotificationsEnabled();
        tvUserName.setText(!isGuest ? userInfo.getUsername() : "--Guest--");
        Picasso.with(userCountryImageView.getContext()).load(userInfo.getFlagURL()).fit().placeholder(R.drawable.progress_animation).into(userCountryImageView);
        tvPoints.setText(userInfo.getTotalPoints() + " points");

        // We currently do not support changing username
        items.add(new Item("Share", "Share your username with your friends", MaterialDrawableBuilder.with(getActivity().getApplicationContext())
                .setIcon(MaterialDrawableBuilder.IconValue.SHARE).setColor(Color.MAGENTA).build()));

        items.add(new Item("Notifications", notificationsEnabled ? "Turn off notifications" : "Turn on notifications", MaterialDrawableBuilder.with(getActivity().getApplicationContext())
                .setIcon(MaterialDrawableBuilder.IconValue.MESSAGE_ALERT).setColor(Color.MAGENTA).build()));

        items.add(new Item("Rate WikiGame", "Rate WikiGame on google play", MaterialDrawableBuilder.with(getActivity().getApplicationContext())
                .setIcon(MaterialDrawableBuilder.IconValue.STAR).setColor(Color.MAGENTA).build()));

        items.add(new Item("Account Logout", "Remove account from this device", MaterialDrawableBuilder.with(getActivity().getApplicationContext())
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_SWITCH).setColor(Color.MAGENTA).build()));

        items.add(new Item("Feedback", "We promise to answer!", MaterialDrawableBuilder.with(getActivity().getApplicationContext())
                .setIcon(MaterialDrawableBuilder.IconValue.MESSAGE).setColor(Color.MAGENTA).build()));

        listView.setAdapter(new AboutAdapter(getActivity().getApplicationContext(), items));
        listView.setDivider(null);
    }

    public void logOut() {
        wikiGameAPI.logoutAsync(new WikiGameInterface(getActivity()) {
            @Override
            public void onFinishedProcessingWikiRequest(JSONObject response) {
                try {
                    if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                        registerAsGuestAgain();
                    } else {
                        ErrorDialogs.showSomethingWentWrongDialog(getActivityContext(), false);
                    }
                } catch (JSONException e) {
                    ErrorDialogs.showBadResponseDialog(getActivityContext(), false);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailedMakingWikiRequest(WikiError errorCause) {
                ErrorDialogs.showNetworkErrorDialog(getActivityContext(), false);
            }
        });

    }

    public void registerAsGuestAgain() {
        // trying to register again
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userCountry = Utils.getUserCountry(getActivity().getApplicationContext());
        String appVersion = Utils.getAppVersion(getActivity().getApplicationContext());
        String phoneModel = Utils.getDeviceName();

        wikiGameAPI.registerGuestAsync(android.os.Build.VERSION.RELEASE, userCountry, appVersion, phoneModel, new WikiGameInterface(getActivity()) {
            @Override
            public void onFinishedProcessingWikiRequest(JSONObject response) {
                try {
                    if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                        JSONObject userInfo = response.getJSONObject("user_info");
                        String username = userInfo.getString("username");
                        String password = userInfo.getString("password");
                        SharedPreferences.Editor prefEditor = prefs.edit();
                        prefEditor.putString(Consts.KEY_USER_NAME, username);
                        prefEditor.putString(Consts.KEY_USER_PASSWORD, password);
                        prefEditor.apply();
                        requestGuestDetails(username, password);
                    } else {
                        ErrorDialogs.showSomethingWentWrongDialog(getActivityContext(), false);
                        // TODO: Handle error in registration
                    }
                } catch (JSONException e) {
                    ErrorDialogs.showBadResponseDialog(getActivityContext(), false);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailedMakingWikiRequest(WikiError errorCause) {
                ErrorDialogs.showNetworkErrorDialog(getActivityContext(), false);
            }
        });
    }

    public void requestGuestDetails(String username, String password) {
        final MainActivity mainActivity = ((MainActivity)getActivity());
        wikiGameAPI.welcomeRequestAsync(username, password, new WikiGameInterface(getActivity()) {
            @Override
            public void onFinishedProcessingWikiRequest(JSONObject response) {
                try {
                    if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                        JSONObject userInfoObject = response.getJSONObject("user_info");
                        UserInfo userInfo = UserInfo.fromJSON(userInfoObject);
                        SocialInfo socialInfo = SocialInfo.fromJSON(userInfoObject);

                        JSONArray levelsObject = userInfoObject.getJSONArray("levels_info");
                        JSONArray suggestedFriendsObject = response.getJSONArray("suggested_friends");
                        ArrayList<Level> gameLevels = new ArrayList<>();
                        for (int i = 0; i < levelsObject.length(); i++) {
                            gameLevels.add(Level.fromJSON(levelsObject.getJSONObject(i)));
                        }
                        ArrayList<SuggestedFriend> suggestedFriends = new ArrayList<>();
                        for (int i = 0; i < suggestedFriendsObject.length(); i++) {
                            suggestedFriends.add(SuggestedFriend.fromJSON(suggestedFriendsObject.getJSONObject(i)));
                        }
                        for (int i = levelsObject.length(); i < response.getInt("num_levels"); i++) {
                            Level level = new Level((i + 1) + "", true, "unknown", 0, 0);
                            gameLevels.add(level);
                        }
                        mainActivity.setLevels(gameLevels);
                        mainActivity.updateSocialInfo(socialInfo, suggestedFriends);
                        mainActivity.setUserInfo(userInfo);
                        mainActivity.populateScreensWithLocalData();
                        updateUI();
                    } else {
                        ErrorDialogs.showSomethingWentWrongDialog(getActivityContext(), true);
                    }
                } catch (JSONException e) {
                    ErrorDialogs.showBadResponseDialog(getActivityContext(), true);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailedMakingWikiRequest(WikiError errorCause) {
                ErrorDialogs.showNetworkErrorDialog(getActivityContext(), false);
            }
        });
    }

    public void disableNotifications() {
        wikiGameAPI.disableNotificationsAsync(new WikiGameInterface(getActivity()) {
            @Override
            public void onFinishedProcessingWikiRequest(JSONObject response) {
                try {
                    if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                        ((MainActivity)getActivity()).getUserInfo().setNotificationsEnabled(false);
                        Toast.makeText(getActivityContext(), "Notifications were disabled successfully", Toast.LENGTH_SHORT).show();
                        updateUI();
                    } else {
                        Toast.makeText(getActivityContext(), "An error has occurred while trying to disable notifications", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivityContext(), "An error has occurred while trying to disable notifications", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailedMakingWikiRequest(WikiError errorCause) {
                Toast.makeText(getActivityContext(), "An error has occurred while trying to disable notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void enableNotifications() {
        wikiGameAPI.enableNotificationsAsync(new WikiGameInterface(getActivity()) {
            @Override
            public void onFinishedProcessingWikiRequest(JSONObject response) {
                try {
                    if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                        ((MainActivity)getActivity()).getUserInfo().setNotificationsEnabled(true);
                        Toast.makeText(getActivityContext(), "Notifications were enabled successfully", Toast.LENGTH_SHORT).show();
                        updateUI();
                    } else {
                        Toast.makeText(getActivityContext(), "An error has occurred while trying to enable notifications", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivityContext(), "An error has occurred while trying to enable notifications", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailedMakingWikiRequest(WikiError errorCause) {
                Toast.makeText(getActivityContext(), "An error has occurred while trying to enable notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void clear() {

    }

    public SocialInfo getSocialInfo() {
        return socialInfo;
    }

    public void setSocialInfo(SocialInfo socialInfo) {
        this.socialInfo = socialInfo;
    }
}
