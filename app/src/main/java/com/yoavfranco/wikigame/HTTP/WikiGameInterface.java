package com.yoavfranco.wikigame.HTTP;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.yoavfranco.wikigame.utils.ErrorDialogs;

import org.json.JSONObject;

public abstract class WikiGameInterface {

    private Activity activityContext;

    public WikiGameInterface(Context activityContext) {
        if (activityContext instanceof Activity)
            this.activityContext = (Activity) activityContext;
        else {
            activityContext = null;
        }
    }

    public abstract void onFinishedProcessingWikiRequest(JSONObject response);

    public void onFailedMakingWikiRequest(WikiError errorCause) {
        if (this.activityContext != null)
            ErrorDialogs.showNetworkErrorDialog(this.activityContext, false);
        else
            Log.d("WikiGameInterface", "Unhandled error case!");
    }

    public Activity getActivityContext() {
        return activityContext;
    }

    public void setActivityContext(Activity activityContext) {
        this.activityContext = activityContext;
    }

    public enum WikiError {IOException, InvalidJSON}
}
