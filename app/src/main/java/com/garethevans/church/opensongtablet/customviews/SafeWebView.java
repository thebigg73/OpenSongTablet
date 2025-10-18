package com.garethevans.church.opensongtablet.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import com.garethevans.church.opensongtablet.R;

public class SafeWebView extends WebView {

    public SafeWebView(@NonNull Context context) {
        super(wrapContext(context));
        init();
    }

    public SafeWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(wrapContext(context), attrs);
        init();
    }

    public SafeWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(wrapContext(context), attrs, defStyleAttr);
        init();
    }

    private static Context wrapContext(Context base) {
        // Use a neutral overlay theme that won’t affect your Day/Night mode
        return new ContextThemeWrapper(base, R.style.AppTheme);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        // Optional: basic settings
        getSettings().setJavaScriptEnabled(true);
        getSettings().setDomStorageEnabled(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        // Prevent the WebView from propagating unwanted uiMode changes
        newConfig.uiMode = getContext().getResources().getConfiguration().uiMode;
        super.onConfigurationChanged(newConfig);
    }
}
