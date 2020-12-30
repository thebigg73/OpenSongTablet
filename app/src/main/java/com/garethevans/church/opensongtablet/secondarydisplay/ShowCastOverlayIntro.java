package com.garethevans.church.opensongtablet.secondarydisplay;

import android.app.Activity;
import android.os.Handler;
import android.view.MenuItem;

import com.garethevans.church.opensongtablet.R;
import com.google.android.gms.cast.framework.IntroductoryOverlay;

public class ShowCastOverlayIntro {

    private IntroductoryOverlay introductoryOverlay;

    public void showIntroductoryOverlay(Activity activity, MenuItem mediaRouteMenuItem) {
        if (introductoryOverlay != null) {
            introductoryOverlay.remove();
        }
        if ((mediaRouteMenuItem != null) && mediaRouteMenuItem.isVisible()) {
            new Handler().post(() -> {
                introductoryOverlay = new IntroductoryOverlay.Builder(
                        activity, mediaRouteMenuItem)
                        .setTitleText(activity.getString(R.string.welcome))
                        .setOverlayColor(R.color.showcaseColor)
                        //.setSingleTime()
                        .setOnOverlayDismissedListener(
                                () -> introductoryOverlay = null)
                        .build();
                introductoryOverlay.show();
            });
        }
    }
}
