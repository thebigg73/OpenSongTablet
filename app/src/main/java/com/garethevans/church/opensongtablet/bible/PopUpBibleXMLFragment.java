/*
package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.OLD_TO_DELETE._CustomAnimations;
import com.garethevans.church.opensongtablet.OLD_TO_DELETE._ShowToast;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static android.content.Context.DOWNLOAD_SERVICE;

public class PopUpBibleXMLFragment extends DialogFragment {

    static PopUpBibleXMLFragment newInstance() {
        PopUpBibleXMLFragment frag;
        frag = new PopUpBibleXMLFragment();
        return frag;
    }

    public interface MyInterface {
        void openFragment();
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
        try {
            Objects.requireNonNull(getActivity()).unregisterReceiver(onComplete);
        } catch (Exception e) {
            Log.d("d","No need to unregister receiver");
        }
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        try {
            Objects.requireNonNull(getActivity()).registerReceiver(onComplete,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            Objects.requireNonNull(getActivity()).unregisterReceiver(onComplete);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> bibleFileNames, bibleBookNames, bibleChapters, quickUpdate;
    static ArrayList<String> bibleVerses, bibleText;
    private ArrayAdapter<String> blank_array, aa, b_aa, c_aa, v_aa;
    private Spinner bibleFileSpinner, bibleBookSpinner, bibleChapterSpinner, bibleVerseFromSpinner, bibleVerseToSpinner;
    private FloatingActionButton closeMe;
    private FloatingActionButton saveMe;
    private ProgressBar progressBar;
    private TextView previewTextView;
    private String bible;
    private WebView webViewBibleDownload;
    private FloatingActionButton webViewCloseFAB;
    private View V;
    private ScrollView xmlscrollview;
    private Uri bibleFile;
    private Uri downloadedFile;
    private int selectedItem;

    private StorageAccess storageAccess;
    private _Preferences preferences;
    private Bible bibleC;
    private boolean includeVersNums = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        storageAccess = new StorageAccess();
        preferences = new _Preferences();
        bibleC = new Bible();

        V = inflater.inflate(R.layout.popup_biblexml, container, false);

        // Be nice and run the actions as a separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Set the views
                        setTheViews(V);

                        // Hide the webview before initialising it
                        hideViewsIfNeeded(false);

                        // Initialise the webview
                        initialiseTheWebView();
                    }
                });

                // Prepare an empty array
                ArrayList<String> emptyArray = new ArrayList<>();
                emptyArray.add("");
                blank_array = new ArrayAdapter<>(getActivity(),R.layout.my_spinner,emptyArray);

                quickUpdate = null;

                // Update the bible file spinner and select the appropriate one
                updateBibleFiles();
            }
        }).start();

        return V;
    }

    private void setTheViews(View V) {
        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.bibleXML));
        closeMe = V.findViewById(R.id.closeMe);
        saveMe = V.findViewById(R.id.saveMe);
        webViewBibleDownload = V.findViewById(R.id.webViewBibleDownload);
        xmlscrollview = V.findViewById(R.id.xmlscrollview);
        webViewCloseFAB = V.findViewById(R.id.webViewCloseFAB);
        bibleFileSpinner = V.findViewById(R.id.bibleFileSpinner);
        bibleBookSpinner = V.findViewById(R.id.bibleBookSpinner);
        bibleChapterSpinner = V.findViewById(R.id.bibleChapterSpinner);
        bibleVerseFromSpinner = V.findViewById(R.id.bibleVerseFromSpinner);
        bibleVerseToSpinner = V.findViewById(R.id.bibleVerseToSpinner);
        previewTextView = V.findViewById(R.id.previewTextView);
        progressBar = V.findViewById(R.id.progressBar);
        SwitchCompat includeVersNumsSwitch = V.findViewById(R.id.includeVersNumsSwitch);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _CustomAnimations.animateFAB(saveMe, getActivity());
                saveMe.setEnabled(false);
                doSave();
            }
        });
        includeVersNumsSwitch.setChecked(includeVersNums);
        includeVersNumsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, final boolean b) {
                if (quickUpdate!=null) {
                    includeVersNums = b;
                    getBibleText(quickUpdate.get(1), quickUpdate.get(2),
                            quickUpdate.get(3), quickUpdate.get(4));
                }

            }
        });
    }





}*/
