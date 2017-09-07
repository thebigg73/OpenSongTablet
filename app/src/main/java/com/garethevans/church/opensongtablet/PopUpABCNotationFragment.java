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
import android.webkit.JavascriptInterface;
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

    private MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
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
        View V = inflater.inflate(R.layout.popup_abcnotation, container, false);

        TextView title = (TextView) V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getString(R.string.music_score));
        final FloatingActionButton closeMe = (FloatingActionButton) V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = (FloatingActionButton) V.findViewById(R.id.saveMe);
        if (FullscreenActivity.whattodo.equals("abcnotation")) {
            saveMe.setVisibility(View.GONE);
        }
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSave();
            }
        });

        // Initialise the views
        abcWebView = (WebView) V.findViewById(R.id.abcWebView);

        abcWebView.getSettings().setJavaScriptEnabled(true);
        abcWebView.addJavascriptInterface(new JsInterface(), "AndroidApp");
        abcWebView.loadUrl("file:///android_asset/ABC/abc.html");
        abcWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                if (FullscreenActivity.mNotation.equals("")) {
                    updateContent(getSongInfo());
                } else {
                    updateContent(FullscreenActivity.mNotation);
                }
            }
        });
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
        void receiveString(String value) {
            // String received from WebView
            Log.d("MyApp", value);
            if (!value.equals(getSongInfo())) {
                // Something has changed
                FullscreenActivity.mNotation = value;
                String ABCPlaceHolder = ";"+getActivity().getString(R.string.music_score);
                if (!FullscreenActivity.mLyrics.contains(ABCPlaceHolder)) {
                    FullscreenActivity.mLyrics = ABCPlaceHolder + "\n\n" + FullscreenActivity.mLyrics;
                }
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
