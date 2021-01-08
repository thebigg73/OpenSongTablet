package com.garethevans.church.opensongtablet.secondarydisplay;

import android.app.Activity;

import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.cast.framework.SessionManagerListener;

public class MySessionManagerListener implements SessionManagerListener<Session>{

    private final Activity activity;

    public MySessionManagerListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onSessionStarting(Session session) {

    }

    @Override
    public void onSessionStarted(Session session, String sessionId) {
        activity.invalidateOptionsMenu();
    }

    @Override
    public void onSessionStartFailed(Session session, int i) {

    }

    @Override
    public void onSessionEnding(Session session) {

    }

    @Override
    public void onSessionEnded(Session session, int i) {

    }

    @Override
    public void onSessionResuming(Session session, String s) {

    }

    @Override
    public void onSessionResumed(Session session, boolean wasSuspended) {
        activity.invalidateOptionsMenu();
    }

    @Override
    public void onSessionResumeFailed(Session session, int i) {

    }

    @Override
    public void onSessionSuspended(Session session, int i) {

    }
}