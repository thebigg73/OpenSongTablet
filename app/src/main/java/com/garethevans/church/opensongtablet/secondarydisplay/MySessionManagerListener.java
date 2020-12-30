package com.garethevans.church.opensongtablet.secondarydisplay;

import android.app.Activity;

import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;

public class MySessionManagerListener implements SessionManagerListener<CastSession> {

    CastSession castSession;
    Activity activity;

    public MySessionManagerListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onSessionEnded(CastSession session, int error) {
        if (session == castSession) {
            castSession = null;
        }
        activity.invalidateOptionsMenu();
    }

    @Override
    public void onSessionResumed(CastSession session, boolean wasSuspended) {
        castSession = session;
        activity.invalidateOptionsMenu();
    }

    @Override
    public void onSessionStarted(CastSession session, String sessionId) {
        castSession = session;
        activity.invalidateOptionsMenu();
    }

    @Override
    public void onSessionStarting(CastSession session) {
    }

    @Override
    public void onSessionStartFailed(CastSession session, int error) {
    }

    @Override
    public void onSessionEnding(CastSession session) {
    }

    @Override
    public void onSessionResuming(CastSession session, String sessionId) {
    }

    @Override
    public void onSessionResumeFailed(CastSession session, int error) {
    }

    @Override
    public void onSessionSuspended(CastSession session, int reason) {
    }
}