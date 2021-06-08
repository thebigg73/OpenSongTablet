package com.garethevans.church.opensongtablet.secondarydisplay;

import android.content.Context;

import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.R;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;

import java.util.List;

public class CastOptionsProvider implements OptionsProvider {

    @NonNull
    @Override
    public CastOptions getCastOptions(Context context) {
        return new CastOptions.Builder()
                .setReceiverApplicationId(context.getString(R.string.app_id)).build();
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(@NonNull Context context) {
        return null;
    }
}