package com.garethevans.church.opensongtablet.bible;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.databinding.BottomSheetBibleOnlineBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BibleGatewayBottomSheet extends BottomSheetDialogFragment {

    // This is used to query the BibleGateway site and grab the desired scripture for a new set item

    private BottomSheetBibleOnlineBinding myView;
    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetBibleOnlineBinding.inflate(inflater, container, false);
        myView.dialogHeader.setClose(this);

        // Set up webview
        setupWebView();

        // Set up the search views
        setupViews(myView.searchOptions);

        return myView.getRoot();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        myView.webView.setWebViewClient(new WebViewClient());
        WebSettings websettings = myView.webView.getSettings();
        websettings.setDisplayZoomControls(true);
        websettings.setJavaScriptEnabled(true);
    }
    private void setupViews(View whichToShow) {
        myView.progressBar.setVisibility(View.GONE);
        myView.searchOptions.setVisibility(View.GONE);
        myView.searchResults.setVisibility(View.GONE);
        myView.searchWeb.setVisibility(View.GONE);
        whichToShow.setVisibility(View.VISIBLE);
    }
}
