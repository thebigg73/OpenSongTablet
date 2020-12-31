package com.garethevans.church.opensongtablet.secondarydisplay;

import androidx.annotation.NonNull;
import androidx.mediarouter.media.MediaRouter;

import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;

public class MediaRouterCallback extends MediaRouter.Callback {

    CastDevice device;
    DisplayInterface displayInterface;

    public MediaRouterCallback() {
        // Empty initialiser
    }

    public MediaRouterCallback(CastDevice device, DisplayInterface displayInterface) {
        this.device = device;
        this.displayInterface = displayInterface;
    }

    @Override
    public void onRouteSelected(@NonNull MediaRouter router, @NonNull MediaRouter.RouteInfo route, int reason) {
        super.onRouteSelected(router, route, reason);
    }

    @Override
    public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route, int reason) {
        super.onRouteUnselected(router, route, reason);
    }


    void teardown() {
        try {
            CastRemoteDisplayLocalService.stopService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //TODO
            /*if (hdmi != null) {
                hdmi.dismiss();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRouteAdded(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
    }

    @Override
    public void onRouteRemoved(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
    }

    @Override
    public void onRouteChanged(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
    }

    @Override
    public void onRouteVolumeChanged(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
    }
}