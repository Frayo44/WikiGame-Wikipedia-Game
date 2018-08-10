package com.yoavfranco.wikigame.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.activities.LoadingActivity;
import com.yoavfranco.wikigame.activities.MainActivity;

public class MyAndroidFirebaseMsgService extends FirebaseMessagingService {

    private static final String TAG = "MyAndroidFCMService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Log data to Log Cat
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        //create notification
        createNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
    }
 
    private void createNotification(String title, String messageBody) {
        Intent intent = new Intent( this , LoadingActivity.class );
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent resultIntent = PendingIntent.getActivity( this , 0, intent,
        PendingIntent.FLAG_ONE_SHOT);
 
        Uri notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder( this)
                        .setSmallIcon(R.mipmap.wikigame_final_logo)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel( true )
                        .setSound(notificationSoundURI)
                        .setVibrate(new long[] { 1000, 1000})
                        .setContentIntent(resultIntent);
 
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 
        notificationManager.notify(0, mNotificationBuilder.build());
    }
}