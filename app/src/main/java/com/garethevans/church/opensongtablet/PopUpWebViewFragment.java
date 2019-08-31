package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.TextView;

public class PopUpWebViewFragment extends DialogFragment {

    static PopUpWebViewFragment newInstance() {
        PopUpWebViewFragment frag;
        frag = new PopUpWebViewFragment();
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String mTitle;
        switch (FullscreenActivity.whattodo) {
            case "errorlog":
                mTitle = getResources().getString(R.string.search_log);
                break;
            case "browsefonts":
                mTitle = getString(R.string.googlefontbrowse);
                break;
            default:
                mTitle = getString(R.string.websearch);
                break;
        }
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View V = inflater.inflate(R.layout.popup_webview, container, false);

        Preferences preferences = new Preferences();

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(mTitle);
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        WebView webview = V.findViewById(R.id.webview);
        TextView textview = V.findViewById(R.id.textview);

        webview.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    WebView webView = (WebView) v;
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (webView.canGoBack()) {
                            webView.goBack();
                            return true;
                        }
                    }
                }
                return true; // Stops the window closing
            }
        });

        switch (FullscreenActivity.whattodo) {
            case "errorlog":
                webview.setVisibility(View.GONE);
                textview.setVisibility(View.VISIBLE);
                textview.setText(FullscreenActivity.indexlog);
                break;
            case "browsefonts":
                webview.setVisibility(View.VISIBLE);
                textview.setVisibility(View.GONE);
                webview.getSettings().setJavaScriptEnabled(true);
                webview.canGoBackOrForward(1);
                webview.loadUrl(FullscreenActivity.webpage);
                closeMe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FullscreenActivity.whattodo = "changefonts";
                    }
                });
                break;
            default:
                webview.setVisibility(View.VISIBLE);
                textview.setVisibility(View.GONE);
                webview.canGoBackOrForward(1);
                webview.loadUrl(FullscreenActivity.webpage);
                break;
        }
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);
        return V;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
