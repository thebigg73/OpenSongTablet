package com.garethevans.church.opensongtablet.secondarydisplay;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.common.api.Status;

public class MySessionManagerListener implements SessionManagerListener<CastSession>, CastRemoteDisplayLocalService.Callbacks {

    private final String TAG = "MySessMan";
    private DisplayInterface displayInterface;

    public MySessionManagerListener(Context c) {
        displayInterface = (DisplayInterface) c;
    }

    @Override
    public void onSessionStarting(@NonNull CastSession castSession) {
        Log.d(TAG,"onSessionStarting");
    }

    @Override
    public void onSessionStarted(@NonNull CastSession castSession, @NonNull String s) {
        Log.d(TAG,"onSessionStarted");
        Log.d(TAG,"Name: "+castSession.getCastDevice().getFriendlyName());
        displayInterface.setupCastDevice(castSession.getCastDevice());
    }

    @Override
    public void onSessionStartFailed(@NonNull CastSession castSession, int i) {
        Log.d(TAG,"onSessionStartFailed");
    }

    @Override
    public void onSessionEnding(@NonNull CastSession castSession) {
        Log.d(TAG,"onSessionEnding");
    }

    @Override
    public void onSessionEnded(@NonNull CastSession castSession, int i) {
        Log.d(TAG,"onSessionEnded");
    }

    @Override
    public void onSessionResuming(@NonNull CastSession castSession, @NonNull String s) {
        Log.d(TAG,"onSessionResuming");
    }

    @Override
    public void onSessionResumed(@NonNull CastSession castSession, boolean b) {
        Log.d(TAG,"onSessionResumed");
    }

    @Override
    public void onSessionResumeFailed(@NonNull CastSession castSession, int i) {
        Log.d(TAG,"onSessionResumeFailed");
    }

    @Override
    public void onSessionSuspended(@NonNull CastSession castSession, int i) {
        Log.d(TAG,"onSessionSuspended");
    }

    @Override
    public void onServiceCreated(@NonNull CastRemoteDisplayLocalService castRemoteDisplayLocalService) {

    }

    @Override
    public void onRemoteDisplaySessionStarted(@NonNull CastRemoteDisplayLocalService castRemoteDisplayLocalService) {

    }

    @Override
    public void onRemoteDisplaySessionError(@NonNull Status status) {

    }

    @Override
    public void onRemoteDisplaySessionEnded(@NonNull CastRemoteDisplayLocalService castRemoteDisplayLocalService) {

    }

    @Override
    public void zza() {

    }
}