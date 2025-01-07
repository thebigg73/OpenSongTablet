package com.garethevans.church.opensongtablet.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.abcnotation.ABCWebViewJSInterface;

public class InlineAbcWebView extends WebView {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "InlineAbcWebView";
    private boolean allowTouch = false;

    // Used when an InlineAbcView is created programmatically
    public InlineAbcWebView(@NonNull Context c) {
        super(c);
        this.setId(View.generateViewId());
        setJavaScriptEnabled(c);
    }

    // Used when an InlineAbcView is created in XML
    public InlineAbcWebView(@NonNull Context c, @Nullable AttributeSet attrs) {
        super(c, attrs);
        setJavaScriptEnabled(c);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setJavaScriptEnabled(Context c) {
        setAllowTouch(false);
        this.setScrollContainer(false);
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setDomStorageEnabled(true);
        this.addJavascriptInterface(new ABCWebViewJSInterface(c),"AndroidApp");
        this.setInitialScale(1);
        this.getSettings().setDomStorageEnabled(true);
        this.getSettings().setLoadWithOverviewMode(true);
        this.getSettings().setUseWideViewPort(true);
        this.getSettings().setSupportZoom(true);
        this.getSettings().setBuiltInZoomControls(true);
        this.getSettings().setDisplayZoomControls(false);
        this.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        this.setScrollbarFadingEnabled(false);
        this.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return super.onConsoleMessage(consoleMessage);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // This is the key to stopping any events from being registered
        if (allowTouch) {
            return super.dispatchTouchEvent(ev);
        } else {
            return false;
        }
    }

    public void setAllowTouch(boolean allowTouch) {
        this.allowTouch = allowTouch;
        this.setFocusable(allowTouch);
        this.setClickable(allowTouch);
        this.setFocusableInTouchMode(allowTouch);
    }

}
