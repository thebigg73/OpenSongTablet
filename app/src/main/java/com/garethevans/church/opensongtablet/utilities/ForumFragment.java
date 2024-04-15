package com.garethevans.church.opensongtablet.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsForumWebviewBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class ForumFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsForumWebviewBinding myView;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "Forum";
    private String forum_string="", forum_address="", not_connected_string="";

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings();
        mainActivityInterface.updateToolbar(forum_string);
        mainActivityInterface.updateToolbarHelp(forum_address);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsForumWebviewBinding.inflate(inflater, container, false);

        prepareStrings();

        // Setup views
        setupViews();

        // Listeners
        setupListeners();

        // Check connection and load the forum into the webview (desktop mode)
        checkConnection();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            forum_string = getString(R.string.forum);
            forum_address = getString(R.string.website_forum);
            not_connected_string = getString(R.string.requires_internet);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupViews() {
        // This spoofs a desktop browser required for UG if we have set that option
        myView.forumWebView.getSettings().setUserAgentString("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:123.0) Gecko/20100101 Firefox/123.0");
        myView.forumWebView.getSettings().getJavaScriptEnabled();
        myView.forumWebView.getSettings().setJavaScriptEnabled(true);
        myView.forumWebView.getSettings().setLoadWithOverviewMode(true);
        myView.forumWebView.getSettings().setUseWideViewPort(true);
        myView.forumWebView.getSettings().setSupportZoom(true);
        myView.forumWebView.getSettings().setBuiltInZoomControls(true);
        myView.forumWebView.getSettings().setDisplayZoomControls(false);
        myView.forumWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        myView.forumWebView.setScrollbarFadingEnabled(false);

    }

    private void setupListeners() {
        myView.forumWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Set the back/forward buttons as enabled/disabled
                myView.backArrow.setEnabled(myView.forumWebView.canGoBack());
                myView.forwardArrow.setEnabled(myView.forumWebView.canGoForward());
            }
        });

        myView.backArrow.setOnClickListener(v -> {
            if (myView.forumWebView.canGoBack()) {
                myView.forumWebView.goBack();
            }
        });
        myView.forwardArrow.setOnClickListener(v -> {
            if (myView.forumWebView.canGoForward()) {
                myView.forumWebView.goForward();
            }
        });
    }

    private void checkConnection() {
        mainActivityInterface.getWindowFlags().hideKeyboard();
        // Check we have an internet connection
        mainActivityInterface.getCheckInternet().checkConnection(getContext(), this, R.id.forumFragment, mainActivityInterface);
    }

    public void isConnected(boolean connected) {
        Log.d(TAG, "connected=" + connected);
        // Received back from the MainActivity after being told if we have a valid internet connection or not
        if (connected) {
            mainActivityInterface.getMainHandler().post(() -> myView.forumWebView.loadUrl(forum_address));
        } else {
            mainActivityInterface.getMainHandler().post(() -> mainActivityInterface.getShowToast().doIt(not_connected_string));
        }
    }
}
