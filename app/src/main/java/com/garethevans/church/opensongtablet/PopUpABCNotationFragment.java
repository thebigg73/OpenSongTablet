package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
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

import java.util.Objects;

public class PopUpABCNotationFragment extends DialogFragment {

    static PopUpABCNotationFragment newInstance() {
        PopUpABCNotationFragment frag;
        frag = new PopUpABCNotationFragment();
        return frag;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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

    private WebView abcWebView;
    private Preferences preferences;
    private StorageAccess storageAccess;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);
        if (getDialog().getWindow()!=null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        View V = inflater.inflate(R.layout.popup_abcnotation, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getString(R.string.music_score));
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
            saveMe.hide();
        }
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe, getActivity());
                doSave();
            }
        });

        preferences = new Preferences();
        storageAccess = new StorageAccess();

        // Initialise the views
        abcWebView = V.findViewById(R.id.abcWebView);
        String newUA = "Mozilla/5.0 (X11; Linux x86_64; rv:54.0) Gecko/20100101 Firefox/54.0";
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
                if (StaticVariables.mNotation.equals("")) {
                    updateContent(getSongInfo());
                } else {
                    updateContent(StaticVariables.mNotation);
                }
                if (!FullscreenActivity.whattodo.equals("abcnotation")) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        abcWebView.evaluateJavascript("javascript:displayAndEdit();", null);
                    } else {
                        abcWebView.loadUrl("javascript:displayAndEdit();");
                    }
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        abcWebView.evaluateJavascript("javascript:displayOnly();", null);
                    } else {
                        abcWebView.loadUrl("javascript:displayOnly();");
                    }
                }
            }
        });
        abcWebView.loadUrl("file:///android_asset/ABC/abc.html");
        Dialog dialog = getDialog();
        if (dialog!=null && getActivity()!=null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),dialog, preferences);
        }
        return V;
    }

    private void doSave() {
        // Try to get the text
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            abcWebView.evaluateJavascript("getTextVal()", null);
        } else {
            abcWebView.loadUrl("javascript:getTextVal()");
        }
    }

    private void updateContent(String s) {
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

    private String getSongInfo() {
        String info = "";
        // Add the song time signature
        if (StaticVariables.mTimeSig.equals("")) {
            info += "M:4/4\n";
        } else {
            info += "M:" + StaticVariables.mTimeSig + "\n";
        }
        // Add the note length
        info += "L:1/8\n";

        // Add the song key
        if (StaticVariables.mKey.equals("")) {
            info += "K:C treble %treble or bass clef\n";
        } else {
            info += "K: " + StaticVariables.mKey + " %treble or bass clef\n";
        }
        info += "|";
        return info;
    }

    private class JsInterface {
        @JavascriptInterface
        public void receiveString(String value) {
            // String received from WebView
            if (!value.equals(getSongInfo())) {
                // Something has changed
                StaticVariables.mNotation = value;
                //String ABCPlaceHolder = ";"+getActivity().getString(R.string.music_score);
                // I could add a line at the start to let the user know there is score, but decided not to
                /*if (!FullscreenActivity.mLyrics.contains(ABCPlaceHolder)) {
                    FullscreenActivity.mLyrics = ABCPlaceHolder + "\n\n" + FullscreenActivity.mLyrics;
                }*/
                PopUpEditSongFragment.prepareSongXML();
                if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                    NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(getActivity());
                    NonOpenSongSQLite nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(getActivity(),storageAccess,preferences,nonOpenSongSQLiteHelper.getSongId());
                    nonOpenSongSQLiteHelper.updateSong(getActivity(),storageAccess,preferences,nonOpenSongSQLite);
                } else {
                    PopUpEditSongFragment.justSaveSongXML(getActivity(), preferences);
                }
                try {
                    dismiss();
                } catch (Exception e) {
                    Log.d("d","Problem closing");
                }
            }
        }
    }
}
