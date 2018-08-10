package com.yoavfranco.wikigame.adapters;

import android.support.annotation.Nullable;

import com.yoavfranco.wikigame.utils.Friend;
import com.yoavfranco.wikigame.utils.FriendRequest;

public interface FriendRequestAdapterInterface
{
    void onFriendRequestRemoved(FriendRequest friendRequest, boolean isConfirmed, @Nullable Friend newFriend);
}
