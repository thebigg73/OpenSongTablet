package com.garethevans.church.opensongtablet.secondarydisplay;

import androidx.mediarouter.media.MediaControlIntent;
import androidx.mediarouter.media.MediaRouteSelector;

import com.google.android.gms.cast.CastMediaControlIntent;

public class MyMediaRouteSelector {

    public MediaRouteSelector getMediaRouteSelector() {
    return new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast("4E2B0891"))
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
                .build();
    }
}
