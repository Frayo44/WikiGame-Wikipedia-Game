package com.yoavfranco.wikigame.utils;

import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/*
 * Created by yoav on 07/03/17.
 */

public class SocialInfo implements Serializable {

    private ArrayList<Friend> friends;
    private ArrayList<SuggestedFriend> suggestedFriends;
    private ArrayList<FriendRequest> pendingFriendRequests;
    private ArrayList<FriendRequest> sentFriendRequests;
    private ArrayList<Challenge> challengesFromStrangers;
    private ArrayList<Challenge> challengesToStrangers;

    public SocialInfo(ArrayList<Friend> friends, ArrayList<FriendRequest> pendingFriendRequests, ArrayList<FriendRequest> sentFriendRequests, ArrayList<Challenge> challengesFromStrangers, ArrayList<Challenge> challengesToStrangers) {
        this.friends = friends;
        this.pendingFriendRequests = pendingFriendRequests;
        this.sentFriendRequests = sentFriendRequests;
        this.challengesFromStrangers = challengesFromStrangers;
        this.challengesToStrangers = challengesToStrangers;
    }

    public static SocialInfo fromJSON(JSONObject json) throws JSONException {
        JSONArray friendsArray = json.getJSONArray("friends");
        ArrayList<Friend> friends = new ArrayList<>();
        for (int i = 0; i < friendsArray.length(); i++) {
            JSONObject friendObject = friendsArray.getJSONObject(i);
            Friend friend = Friend.fromJSON(friendObject);
            friends.add(friend);
        }
        ArrayList<FriendRequest> pendingFriendRequests = new ArrayList<>();
        JSONArray pendingFriendRequestsArray = json.getJSONArray("pending_friend_requests");
        for (int i = 0; i < pendingFriendRequestsArray.length(); i++) {
            pendingFriendRequests.add(FriendRequest.fromJSON(pendingFriendRequestsArray.getJSONObject(i)));
        }
        ArrayList<FriendRequest> sentFriendRequests = new ArrayList<>();
        JSONArray sentFriendRequestsArray = json.getJSONArray("sent_friend_requests");
        for (int i = 0; i < sentFriendRequestsArray.length(); i++) {
            sentFriendRequests.add(FriendRequest.fromJSON(sentFriendRequestsArray.getJSONObject(i)));
        }
        ArrayList<Challenge> challengesFromStrangers = new ArrayList<>();
        JSONArray pendingChallengesArray = json.getJSONArray("pending_challenges");
        for (int i = 0; i < pendingChallengesArray.length(); i++) {
            Challenge challenge = Challenge.fromJSON(pendingChallengesArray.getJSONObject(i));
            boolean foundFriend = false;
            for (int j = 0; j < friends.size(); j++) {
                if (friends.get(j).getUsername().equals(challenge.getChallengingUsername())) {
                    // this user challenged us, so let's mark it
                    friends.get(j).setChallenge(challenge);
                    foundFriend = true;
                    break;
                }
            }
            if (!foundFriend) {
                challengesFromStrangers.add(challenge);
            }
        }
        ArrayList<Challenge> challengesToStrangers = new ArrayList<>();
        JSONArray sentChallengesArray = json.getJSONArray("sent_challenges");
        for (int i = 0; i < sentChallengesArray.length(); i++) {
            JSONObject challengeObject = sentChallengesArray.getJSONObject(i);
            Challenge challenge = Challenge.fromJSON(challengeObject);
            boolean foundFriend = false;
            for (int j = 0; j < friends.size(); j++) {
                if (friends.get(j).getUsername().equals(challenge.getChallengedUsername())) {
                    // we challenged this user, so let's mark it
                    friends.get(j).setChallenge(challenge);
                    foundFriend = true;
                    break;
                }
            }
            if (!foundFriend) {
                challengesToStrangers.add(challenge);
            }
        }

        return new SocialInfo(friends, pendingFriendRequests, sentFriendRequests, challengesFromStrangers, challengesToStrangers);
    }

    public Challenge getChallengeFromStranger(String username) {
        for (int i=0;i<this.challengesFromStrangers.size();i++) {
            if (this.challengesFromStrangers.get(i).getChallengingUsername().equals(username)) return this.challengesFromStrangers.get(i);
        }

        return null;
    }

    public Challenge getChallengeToStranger(String username) {
        for (int i=0;i<this.challengesToStrangers.size();i++) {
            if (this.challengesToStrangers.get(i).getChallengedUsername().equals(username)) return this.challengesToStrangers.get(i);
        }

        return null;
    }

    public boolean hasChallengeFromStranger(String username) {
        return getChallengeFromStranger(username) != null;
    }

    public boolean hasChallengeToStranger(String username) {
        return getChallengeToStranger(username) != null;
    }

    public ArrayList<FriendRequest> getSentFriendRequests() {
        return sentFriendRequests;
    }

    public void setSentFriendRequests(ArrayList<FriendRequest> sentFriendRequests) {
        this.sentFriendRequests = sentFriendRequests;
    }

    public ArrayList<Friend> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<Friend> friends) {
        this.friends = friends;
    }

    public ArrayList<FriendRequest> getPendingFriendRequests() {
        return pendingFriendRequests;
    }

    public void setPendingFriendRequests(ArrayList<FriendRequest> pendingFriendRequests) {
        this.pendingFriendRequests = pendingFriendRequests;
    }

    public ArrayList<Challenge> getChallengesToStrangers() {
        return challengesToStrangers;
    }

    public void setChallengesToStrangers(ArrayList<Challenge> challengesToStrangers) {
        this.challengesToStrangers = challengesToStrangers;
    }

    public ArrayList<Challenge> getChallengesFromStrangers() {
        return challengesFromStrangers;
    }

    public void setChallengesFromStrangers(ArrayList<Challenge> challengesFromStrangers) {
        this.challengesFromStrangers = challengesFromStrangers;
    }

}
