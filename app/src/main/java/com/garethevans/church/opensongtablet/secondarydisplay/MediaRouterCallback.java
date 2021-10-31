package com.garethevans.church.opensongtablet.secondarydisplay;

import android.content.Context;
import android.util.Log;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.mediarouter.media.MediaRouter;

import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.gms.cast.CastDevice;

public class MediaRouterCallback extends MediaRouter.Callback {

    private CastDevice device;
    private Context c;
    private final DisplayInterface displayInterface;
    private final String TAG = "MediaRouterCallback";
    private final MainActivityInterface mainActivityInterface;

    public MediaRouterCallback(Context c, MainActivityInterface mainActivityInterface) {
        // Empty initialiser
        Log.d(TAG,"Initialising");
        this.c = c;
        displayInterface = (DisplayInterface) c;
        this.mainActivityInterface = mainActivityInterface;
    }

    @Override
    public void onRouteSelected(@NonNull MediaRouter router, @NonNull MediaRouter.RouteInfo route, int reason) {
        super.onRouteSelected(router, route, reason);
        Log.d(TAG,"Route selected: "+router+"  "+route);
        if (router.getSelectedRoute().getPresentationDisplay() != null) {
            Display presentationDisplay = router.getSelectedRoute().getPresentationDisplay();
            if (presentationDisplay != null) {
                MyCastDisplay myCastDisplay = new MyCastDisplay(c, presentationDisplay,mainActivityInterface);
                try {
                    myCastDisplay.show();
                } catch (Exception e) {
                    Log.d(TAG,"Not allowed");
                }
            }
        }
    }

    @Override
    public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo info) {
        Log.d(TAG,"Route selected: "+info);

    }


}

    /*public MediaRouterCallback(CastDevice device, DisplayInterface displayInterface) {
        this.device = device;
        this.displayInterface = displayInterface;
    }*/
/*

    @Override
    public void onRouteSelected(@NonNull MediaRouter router, @NonNull MediaRouter.RouteInfo route, int reason) {
        super.onRouteSelected(router, route, reason);
        Log.d(TAG,"onRouteSelected(): "+route.getName());
        Display presentationDisplay = route.getPresentationDisplay();
        device = CastDevice.getFromBundle(route.getExtras());
        if (device!=null && presentationDisplay!=null) {
            Log.d(TAG,"castDevice friendly name= "+device.getFriendlyName());
            displayInterface.setupDisplay(presentationDisplay);
        }
    }

    @Override
    public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route, int reason) {
        super.onRouteUnselected(router, route, reason);
        Log.d(TAG,"onRouteUnSelected(): "+route.getName());
    }


    void teardown() {
        try {
            CastRemoteDisplayLocalService.stopService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //TODO
            */
/*if (hdmi != null) {
                hdmi.dismiss();
            }*//*

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRouteAdded(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
        Log.d(TAG,"onRouteAdded()");
    }

    @Override
    public void onRouteRemoved(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
        Log.d(TAG,"onRouteRemoved()");
    }

    @Override
    public void onRouteChanged(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
        //Log.d(TAG,"onRouteChanged()");
    }

    @Override
    public void onRouteVolumeChanged(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
        //Log.d(TAG,"onRouteVolumeChanged()");
    }
*/
