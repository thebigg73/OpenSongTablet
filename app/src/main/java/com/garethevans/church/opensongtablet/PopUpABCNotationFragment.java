package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class PopUpABCNotationFragment extends DialogFragment {

    static PopUpABCNotationFragment newInstance() {
        PopUpABCNotationFragment frag;
        frag = new PopUpABCNotationFragment();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        //mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        //mListener = null;
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (abcWebView!=null) {
            abcWebView.getSettings().setJavaScriptEnabled(false);
        }
    }

    WebView abcWebView;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        View V = inflater.inflate(R.layout.popup_abcnotation, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getString(R.string.music_score));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        if (FullscreenActivity.whattodo.equals("abcnotation")) {
            saveMe.setVisibility(View.GONE);
        }
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe, getActivity());
                doSave();
            }
        });

        // Initialise the views
        abcWebView = V.findViewById(R.id.abcWebView);
        String newUA = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
        abcWebView.getSettings().setUserAgentString(newUA);
        abcWebView.getSettings().getJavaScriptEnabled();
        abcWebView.getSettings().setJavaScriptEnabled(true);
        abcWebView.getSettings().setDomStorageEnabled(true);
        abcWebView.getSettings().setLoadWithOverviewMode(true);
        abcWebView.getSettings().setUseWideViewPort(true);
        abcWebView.getSettings().setSupportZoom(true);
        abcWebView.getSettings().setBuiltInZoomControls(true);
        abcWebView.getSettings().setDisplayZoomControls(false);
        abcWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        abcWebView.setScrollbarFadingEnabled(false);
        abcWebView.addJavascriptInterface(new JsInterface(), "AndroidApp");
        abcWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("MyApplication", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());
                return super.onConsoleMessage(consoleMessage);
            }
        });
        abcWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                if (FullscreenActivity.mNotation.equals("")) {
                    updateContent(getSongInfo());
                } else {
                    updateContent(FullscreenActivity.mNotation);
                }
                if (!FullscreenActivity.whattodo.equals("abcnotation")) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        abcWebView.evaluateJavascript("javascript:displayAndEdit();", null);
                    } else {
                        abcWebView.loadUrl("javascript:displayAndEdit();");
                        Log.d("d","pre kitkat load display and edit");
                    }
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        abcWebView.evaluateJavascript("javascript:displayOnly();", null);
                    } else {
                        abcWebView.loadUrl("javascript:displayOnly();");
                        Log.d("d","pre kitkat load display only");
                    }
                }
            }
        });
        abcWebView.loadUrl("file:///android_asset/ABC/abc.html");
        return V;
    }

    public void doSave() {
        // Try to get the text
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            abcWebView.evaluateJavascript("getTextVal()", null);
        } else {
            abcWebView.loadUrl("javascript:getTextVal()");
        }
    }

    public void updateContent(String s) {
        try {
            s = Uri.encode(s, "UTF-8");
        } catch  (Exception e) {
            Log.d("d","Error encoding");
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            abcWebView.evaluateJavascript("javascript:updateABC('"+s+"');", null);
        } else {
            abcWebView.loadUrl("javascript:updateABC('"+s+"');");
        }

        if (FullscreenActivity.whattodo.equals("abcnotation")) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                abcWebView.evaluateJavascript("javascript:displayOnly();", null);
            } else {
                abcWebView.loadUrl("javascript:displayOnly();");
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                abcWebView.evaluateJavascript("javascript:displayAndEdit();", null);
            } else {
                abcWebView.loadUrl("javascript:displayAndEdit();");
            }
        }
    }

    public String getSongInfo() {
        String info = "";
        // Add the song time signature
        if (FullscreenActivity.mTimeSig.equals("")) {
            info += "M:4/4\n";
        } else {
            info += "M:" + FullscreenActivity.mTimeSig + "\n";
        }
        // Add the note length
        info += "L:1/8\n";

        // Add the song key
        if (FullscreenActivity.mKey.equals("")) {
            info += "K:C treble %treble or bass clef\n";
        } else {
            info += "K: " + FullscreenActivity.mKey + " %treble or bass clef\n";
        }
        info += "|";
        return info;
    }

    private class JsInterface {
        @JavascriptInterface
        public void receiveString(String value) {
            // String received from WebView
            Log.d("MyApp", value);
            if (!value.equals(getSongInfo())) {
                // Something has changed
                FullscreenActivity.mNotation = value;
                //String ABCPlaceHolder = ";"+getActivity().getString(R.string.music_score);
                // I could add a line at the start to let the user know there is score, but decided not to
                /*if (!FullscreenActivity.mLyrics.contains(ABCPlaceHolder)) {
                    FullscreenActivity.mLyrics = ABCPlaceHolder + "\n\n" + FullscreenActivity.mLyrics;
                }*/
                PopUpEditSongFragment.prepareSongXML();
                try {
                    PopUpEditSongFragment.justSaveSongXML();
                } catch (Exception e) {
                    Log.d("d","Error saving");
                }
                try {
                    /*if (mListener!=null) {
                        mListener.refreshAll();
                    }*/
                    dismiss();
                } catch (Exception e) {
                    Log.d("d","Problem closing");
                }
            }
        }
    }
}
