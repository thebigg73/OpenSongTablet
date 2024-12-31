package com.garethevans.church.opensongtablet.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.abcnotation.ABCWebViewJSInterface;

public class InlineAbcWebView extends WebView {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "InlineAbcWebView";

    private boolean webViewMeasured;
    private int webViewWidth=0;
    private int webViewHeight=0;
    private int webViewItem=-1;
    private int webViewContainingViewItem=-1;
    private boolean isForPresentation = false;
    private boolean isForExport = false;

    public InlineAbcWebView(@NonNull Context c) {
        super(c);
        this.setId(View.generateViewId());
        setJavaScriptEnabled(c);
    }

    public InlineAbcWebView(@NonNull Context c, @Nullable AttributeSet attrs) {
        super(c, attrs);
        this.setId(View.generateViewId());
        setJavaScriptEnabled(c);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setJavaScriptEnabled(Context c) {
        this.setFocusable(false);
        this.setClickable(false);
        this.setFocusableInTouchMode(false);
        this.setScrollContainer(false);
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setDomStorageEnabled(true);
        this.addJavascriptInterface(new ABCWebViewJSInterface(c),"AndroidApp");

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
    public boolean getIsForPresentation() {
        return isForPresentation;
    }
    public boolean getIsForExport() {
        return isForExport;
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
    public void setIsForPresentation(boolean isForPresentation) {
        this.isForPresentation = isForPresentation;
    }
    public void setIsForExport(boolean isForExport) {
        this.isForExport = isForExport;
    }

    public void setNewSizes(int width, int height) {
        ViewGroup.LayoutParams vglp = this.getLayoutParams();
        vglp.width = width;
        vglp.height = height;
        this.setLayoutParams(vglp);
        this.setBackgroundColor(((int)(Math.random()*16777215)) | (0xFF << 24));
        this.invalidate();
    }
}
