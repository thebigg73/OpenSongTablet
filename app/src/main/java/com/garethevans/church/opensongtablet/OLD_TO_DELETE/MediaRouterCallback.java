/*
package com.garethevans.church.opensongtablet.secondarydisplay;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.mediarouter.media.MediaRouter;

import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.google.android.gms.cast.CastDevice;

public class MediaRouterCallback extends MediaRouter.Callback {

    private final String TAG = "MediaRouterCallback";
    private CastDevice device;
    private Context c;
    private final DisplayInterface displayInterface;

    public MediaRouterCallback(Context c) {
        super();
        this.c = c;
        displayInterface = (DisplayInterface) c;
    }

    @Override
    public void onRouteSelected(@NonNull MediaRouter router, @NonNull MediaRouter.RouteInfo route, int reason) {
        super.onRouteSelected(router, route, reason);
        device = CastDevice.getFromBundle(route.getExtras());
        if (device!=null) {
            Log.d(TAG, "device=" + device.getFriendlyName());
            Log.d(TAG, "onRouteSelected(): router=" + router + "  reason=" + reason + "  route=" + route);
            Log.d(TAG, "presentationDisplay=" + route.getPresentationDisplay());
        }
        if (route.getPresentationDisplay()!=null) {
            Log.d(TAG,"presentationDisplay found!!");
            displayInterface.setupDisplay(route.getPresentationDisplay());
        }
    }


    @Override
    public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route, int reason) {
        super.onRouteUnselected(router, route, reason);
        Log.d(TAG,"onRouteUnselected()");
        device = null;
    }

    @Override
    public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
        super.onRouteAdded(router, route);
        Log.d(TAG,"onRouteAdded(): router="+router+"  route="+route);

    }

    @Override
    public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
        super.onRouteRemoved(router, route);
        Log.d(TAG,"onRouteRemoved():  router="+router+"  route="+route);

    }

    @Override
    public void onRouteChanged(MediaRouter router, MediaRouter.RouteInfo route) {
        super.onRouteChanged(router, route);
        //Log.d(TAG,"onRouteChanged():  router="+router+"  route="+route);
    }

    @Override
    public void onRouteVolumeChanged(MediaRouter router, MediaRouter.RouteInfo route) {
        super.onRouteVolumeChanged(router, route);
        //Log.d(TAG,"onRouteVolumeChanged(): router="+router+"  route="+route);

    }

    @Override
    public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo route) {
        super.onRoutePresentationDisplayChanged(router, route);
        //Log.d(TAG,"onRoutePresentationDisplayChanged(): router="+router+"  route="+route);

    }

    @Override
    public void onProviderAdded(MediaRouter router, MediaRouter.ProviderInfo provider) {
        super.onProviderAdded(router, provider);
        //Log.d(TAG,"onProviderAdded(): router="+router+"  provider="+provider);
    }

    @Override
    public void onProviderRemoved(MediaRouter router, MediaRouter.ProviderInfo provider) {
        super.onProviderRemoved(router, provider);
        //Log.d(TAG,"onProviderRemoved(): router="+router+"  provider="+provider);
    }

    @Override
    public void onProviderChanged(MediaRouter router, MediaRouter.ProviderInfo provider) {
        super.onProviderChanged(router, provider);
        //Log.d(TAG,"onProviderChanged(): router="+router+"  provider="+provider);
    }


}*/
