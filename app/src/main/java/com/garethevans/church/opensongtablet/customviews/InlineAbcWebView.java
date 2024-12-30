package com.garethevans.church.opensongtablet.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class InlineAbcWebView extends WebView {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "InlineAbcWebView";

    private boolean webViewMeasured;
    private int webViewWidth=0;
    private int webViewHeight=0;
    private int webViewItem=-1;
    private int webViewContainingViewItem=-1;

    public InlineAbcWebView(@NonNull Context context) {
        super(context);
        setJavaScriptEnabled();
    }

    public InlineAbcWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setJavaScriptEnabled();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setJavaScriptEnabled() {
        this.setFocusable(false);
        this.setClickable(false);
        this.setFocusableInTouchMode(false);
        this.setScrollContainer(false);
        this.getSettings().setJavaScriptEnabled(true);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // This is the key to stopping any events from being registered
        return false;
    }

    // The getters
    public boolean getWebViewMeasured() {
        return webViewMeasured;
    }
    public int getWebViewWidth() {
        return webViewWidth;
    }
    public int getWebViewHeight() {
        return webViewHeight;
    }
    public int getWebViewItem() {
        return webViewItem;
    }
    public int getWebViewContainingViewItem() {
        return webViewContainingViewItem;
    }


    // The setters
    public void setWebViewMeasured(boolean webViewMeasured) {
        this.webViewMeasured = webViewMeasured;
    }
    public void setWebViewWidth(int webViewWidth) {
        this.webViewWidth = webViewWidth;
    }
    public void setWebViewHeight(int webViewHeight) {
        this.webViewHeight = webViewHeight;
    }
    public void setWebViewItem(int webViewItem) {
        this.webViewItem = webViewItem;
    }
    public void setContainingViewItem(int webViewContainingViewItem) {
        this.webViewContainingViewItem = webViewContainingViewItem;
    }
}
