package com.garethevans.church.opensongtablet;

import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.CastDevice;

class MyMediaRouterCallback extends MediaRouter.Callback {
    CastDevice mSelectedDevice;

    @Override
    public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
        CastDevice mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
        String routeId = info.getId();
    }

    @Override
    public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
        teardown();
        mSelectedDevice = null;
    }

    public void teardown() {}

}