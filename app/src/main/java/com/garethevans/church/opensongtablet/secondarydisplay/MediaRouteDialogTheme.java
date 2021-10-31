package com.garethevans.church.opensongtablet.secondarydisplay;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.mediarouter.app.MediaRouteChooserDialog;
import androidx.mediarouter.app.MediaRouteChooserDialogFragment;
import androidx.mediarouter.app.MediaRouteDynamicChooserDialog;
import androidx.mediarouter.media.MediaRouteSelector;

import com.garethevans.church.opensongtablet.R;

public class MediaRouteDialogTheme extends MediaRouteChooserDialogFragment {

    private Context context;
    public MediaRouteDialogTheme() {
        super();
    }

    @Override
    public MediaRouteSelector getRouteSelector() {
        return super.getRouteSelector();
    }

    @Override
    public void setRouteSelector(MediaRouteSelector selector) {
        super.setRouteSelector(selector);
    }

    @Override
    public MediaRouteDynamicChooserDialog onCreateDynamicChooserDialog(Context context) {
        MediaRouteDynamicChooserDialog dialog = new MediaRouteDynamicChooserDialog(context);
        return dialog;
    }

    @Override
    public MediaRouteChooserDialog onCreateChooserDialog(Context context, Bundle savedInstanceState) {
        return super.onCreateChooserDialog(context, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(context);
        return dialog;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    //private static final int myTheme = androidx.mediarouter.R.style.Theme_MediaRouter;
    private static final int myTheme = R.style.CastAppThemeMediaRouter;

}

