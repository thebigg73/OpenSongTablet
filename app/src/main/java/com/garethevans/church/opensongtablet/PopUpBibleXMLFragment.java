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

    class MyClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view,String url,Bitmap favicon){
            super.onPageStarted(view,url,favicon);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view,String Url) {
            view.loadUrl(Url);
            return true;
        }
        @Override
        public void onPageFinished(WebView view,String url) {
            super.onPageFinished(view,url);
        }
    }
    class GoogleClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view,int newProgress) {
            super.onProgressChanged(view,newProgress);
        }
    }

    private void updateBibleFiles() {
        // This looks for bible files inside the OpenSong/OpenSong Scripture/ folder
        new Thread(new Runnable() {
            @Override

            // Initialise the views
            public void run() {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                        previewTextView.setText("");
                    }
                });

                // Initialise the spinners
                initialiseTheSpinners(true,true,true);
                quickUpdate = null;

                // Get a list of the bible files in the folder
                bibleFileNames = storageAccess.listFilesInFolder(getActivity(), preferences, "OpenSong Scripture", "");
                bibleFileNames.add(0,"");
                bibleFileNames.add(1,getActivity().getString(R.string.download_new));
                bibleFileNames.add(2,"");

                // Set the array adapter
                aa = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, bibleFileNames);

                getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    // Set up the spinner
                                                    aa.notifyDataSetChanged();
                                                    bibleFileSpinner.setAdapter(aa);
                                                    bibleFileSpinner.setEnabled(true);
                                                }
                                            });

                // Decide if we have already selected our favourite bible file, if so, set it
                selectedItem = -1;
                if (bibleFileNames.size()>3) { // Must have files in the OpenSong Scripture folder
                    for (int i = 3; i < bibleFileNames.size(); i++) {
                        if (bibleFileNames.get(i).equals(preferences.getMyPreferenceString(getActivity(),"bibleCurrentFile",""))) {
                            // This is the one we had previously used and it is available
                            selectedItem = i;
                        }
                    }
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Set the Spinner selection if set
                        if (selectedItem>-1) {
                            bibleFileSpinner.setSelection(selectedItem);
                        }
                        // Set a listener on the spinner to load the appropriate file
                        bibleFileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                if (bibleFileNames.size()>=i && i>2) {
                                    loadABible(bibleFileNames.get(i), true);
                                } else if (i==1) {
                                    // Download a new bible
                                    hideViewsIfNeeded(true);
                                } else {
                                    // Blank line selected (0 or 2)
                                    preferences.setMyPreferenceString(getActivity(),"bibleCurrentFile","");
                                    initialiseTheSpinners(false,true,true);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {}
                        });
                    }
                });

                // If we selected a bible, load it
                if (selectedItem>-1) {
                    loadABible(preferences.getMyPreferenceString(getActivity(),"bibleCurrentFile",""),false);
                } else {
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Hide the loading bar for now
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).start();
    }

    private void loadABible(final String selectedBible, final boolean biblechanged) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });

                // Get the bible Uri
                bibleFile = storageAccess.getUriForItem(getActivity(), preferences, "OpenSong Scripture", "", selectedBible);

                if (biblechanged) {
                    preferences.setMyPreferenceString(getActivity(),"bibleCurrentFile",selectedBible);
                }

                // Work out the Scripture title to use
                if (bibleC.bibleFormat.equals("Zefania")) {
                    bible = bibleC.getZefaniaBibleName(getActivity(), bibleFile);

                } else {
                    if (bibleFile!=null && bibleFile.getLastPathSegment()!=null) {
                        bible = bibleFile.getLastPathSegment();
                        if (bible.contains("/OpenSong/OpenSong Scripture")) {
                            bible = bible.substring(bible.indexOf("/OpenSong/OpenSong Scripture/") + 29);
                        }
                        bible = bible.toUpperCase(StaticVariables.locale);
                            bible = bible.replace(".XML", "");
                            bible = bible.replace(".XMM", "");
                    } else {
                        bible = "";
                    }
                }

                quickUpdate = null;

                // Now get the chapters ready
                updateBibleBooks();
            }
        }).start();
    }

    private void updateBibleBooks() {
        // This is only called when the bible is loaded or changed
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Initialise the other spinners beyond this
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                        initialiseTheSpinners(false,true,true);
                        previewTextView.setText("");
                    }
                });

                quickUpdate = null;

                // Get the bible book names if the bible file is set correctly
                bibleBookNames = bibleC.getBibleBookNames(getActivity(),bibleFile);

                // Build the adapter
                b_aa = new ArrayAdapter<>(getActivity(),R.layout.my_spinner,bibleBookNames);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        b_aa.notifyDataSetChanged();
                        bibleBookSpinner.setAdapter(b_aa);
                        bibleBookSpinner.setSelection(0);
                        bibleBookSpinner.setEnabled(true);

                        // Set the book listener
                        bibleBookSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                // Get the Bible book name
                                if (bibleBookNames.size()>0 && bibleBookNames.size()>=i && i>0) {
                                    updateBibleChapters(bibleBookNames.get(i));
                                }
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {}
                        });
                        progressBar.setVisibility(View.GONE);
                    }
                });
                quickUpdate = null;
            }
        }).start();
    }

    private void updateBibleChapters(final String bibleBookName) {
        // This is only called when a book is selected
        new Thread(new Runnable() {
            @Override
            public void run() {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Initialise the other spinners
                        progressBar.setVisibility(View.VISIBLE);
                        previewTextView.setText("");
                    }
                });
                initialiseTheSpinners(false,false,true);

                quickUpdate = null;

                // Get a list of the chapters for this book
                bibleChapters = bibleC.getChaptersForBook(getActivity(), bibleFile, bibleBookName);
                c_aa = new ArrayAdapter<>(getActivity(),R.layout.my_spinner,bibleChapters);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bibleChapterSpinner.setAdapter(c_aa);
                        c_aa.notifyDataSetChanged();
                        bibleChapterSpinner.setSelection(0);
                        bibleChapterSpinner.setEnabled(true);
                        bibleChapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                if (bibleChapters!=null && bibleChapters.size()>0 && bibleChapters.size()>=i && i>0) {
                                    updateBibleVerses(bibleBookName, bibleChapters.get(i));
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {}
                        });
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    private void updateBibleVerses(final String bibleBookName, final String bibleChapter) {
        // This is called when the chapter has been changed/set
        new Thread(new Runnable() {
            @Override
            public void run() {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Initialise the other spinners
                        progressBar.setVisibility(View.VISIBLE);
                        previewTextView.setText("");
                    }
                });

                initialiseTheSpinners(false,false,false);

                quickUpdate = null;

                // Get the verses available for this chapter
                bibleC.getVersesForChapter(getActivity(),bibleFile, bibleBookName, bibleChapter);
                v_aa = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, bibleVerses);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (bibleVerses != null && bibleVerses.size() > 0) {
                            v_aa.notifyDataSetChanged();
                            bibleVerseFromSpinner.setAdapter(v_aa);
                            bibleVerseToSpinner.setAdapter(v_aa);
                            bibleVerseFromSpinner.setSelection(0);
                            bibleVerseToSpinner.setSelection(0);
                            bibleVerseFromSpinner.setEnabled(true);
                            bibleVerseToSpinner.setEnabled(true);
                            bibleVerseFromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    if (bibleVerses!=null && bibleVerses.size() > 0 && bibleVerses.size() >= i && i>0) {
                                        String bibleVerseFrom = bibleVerses.get(i);

                                        // Whatever this value is, if the 'to' spinner is before this, make it match this
                                        if (bibleVerseToSpinner.getSelectedItemPosition() < i) {
                                            bibleVerseToSpinner.setSelection(i);
                                        }
                                        String bibleVerseTo = bibleVerses.get(bibleVerseToSpinner.getSelectedItemPosition());
                                        getBibleText(bibleBookName, bibleChapter, bibleVerseFrom, bibleVerseTo);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {
                                }
                            });
                            bibleVerseToSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    if (bibleVerses.size() > 0 && bibleVerses.size() >= i) {

                                        String bibleVerseTo = bibleVerses.get(i);

                                        // Whatever this value is, if the 'from' spinner is after this, make it match this
                                        if (bibleVerseFromSpinner.getSelectedItemPosition() > i) {
                                            bibleVerseFromSpinner.setSelection(i);
                                        }
                                        String bibleVerseFrom = bibleVerses.get(bibleVerseFromSpinner.getSelectedItemPosition());
                                        getBibleText(bibleBookName, bibleChapter, bibleVerseFrom, bibleVerseTo);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {
                                }
                            });
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    private void getBibleText(final String bibleBookName, final String bibleChapter, final String bibleVerseFrom, final String bibleVerseTo) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });

                quickUpdate = new ArrayList<>();
                quickUpdate.add(bible);
                quickUpdate.add(bibleBookName);
                quickUpdate.add(bibleChapter);
                quickUpdate.add(bibleVerseFrom);
                quickUpdate.add(bibleVerseTo);

                int from;
                int to;
                try {
                    from = Integer.parseInt(bibleVerseFrom);
                    to = Integer.parseInt(bibleVerseTo);
                } catch (Exception e) {
                    e.printStackTrace();
                    from = 0;
                    to = 0;
                }
                StringBuilder s = new StringBuilder();
                if (from > 0 && to >= from && bibleText.size() >= to) {

                    for (int i=from; i<=to; i++) {
                        if (includeVersNums) {
                            s.append("(").append(bibleVerses.get(i - 1)).append(") ");
                        }
                        s.append(bibleText.get(i - 1)).append(" ");
                    }
                    // Trim and fix new sentence double spaces
                    s = new StringBuilder(s.toString().trim());
                    s = new StringBuilder(s.toString().replace(".  ", ". "));
                    s = new StringBuilder(s.toString().replace(". ", ".  "));
                }

                final StringBuilder finalS = s;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        previewTextView.setText(finalS.toString());
                    }
                });

                String verses;
                if (from==to) {
                    verses = "" + from;
                } else {
                    verses = from + "-" + to;
                }
                FullscreenActivity.scripture_title = bibleBookName + " " + bibleChapter + ":" + verses + " (" + bible + ")";
                FullscreenActivity.scripture_verse = bibleC.shortenTheLines(s.toString(),40,6);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    private void doSave() {
        try {
            StaticVariables.whattodo = "customreusable_scripture";
            if (mListener != null) {
                mListener.openFragment();
            }
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("d", "Error grabbing details");
        }
    }

    private void initialiseTheSpinners(final boolean bibles, final boolean books, final boolean chapters) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (bibles) {
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bibleFileSpinner.setAdapter(blank_array);
                            bibleFileSpinner.setOnItemSelectedListener(null);
                            bibleFileSpinner.setEnabled(false);
                        }
                    });
                }
                if (books) {
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bibleBookSpinner.setAdapter(blank_array);
                            bibleBookSpinner.setOnItemSelectedListener(null);
                            bibleBookSpinner.setEnabled(false);
                        }
                    });
                }
                if (chapters) {
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bibleChapterSpinner.setAdapter(blank_array);
                            bibleChapterSpinner.setOnItemSelectedListener(null);
                            bibleChapterSpinner.setEnabled(false);
                        }
                    });
                }
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bibleVerseFromSpinner.setAdapter(blank_array);
                        bibleVerseToSpinner.setAdapter(blank_array);
                        bibleVerseFromSpinner.setOnItemSelectedListener(null);
                        bibleVerseToSpinner.setOnItemSelectedListener(null);
                        bibleVerseFromSpinner.setEnabled(false);
                        bibleVerseToSpinner.setEnabled(false);
                    }
                });
            }
        }).start();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initialiseTheWebView() {
        webViewBibleDownload.setWebViewClient(new MyClient());
        webViewBibleDownload.setWebChromeClient(new GoogleClient());
        WebSettings webSettings = webViewBibleDownload.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webViewBibleDownload.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webViewBibleDownload.clearCache(true);
        webViewBibleDownload.clearHistory();
        webViewBibleDownload.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimetype);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading file...");
                String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
                request.setTitle(filename);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
                downloadedFile = Uri.fromFile(file);
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                DownloadManager dm = (DownloadManager) Objects.requireNonNull(getActivity()).getSystemService(DOWNLOAD_SERVICE);
                if (dm != null) {
                    dm.enqueue(request);
                }
            }
        });
        // Hide the webview to begin with
        hideViewsIfNeeded(false);
        webViewCloseFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideViewsIfNeeded(false);
            }
        });
    }

    private void hideViewsIfNeeded(boolean showWebView) {
        if (showWebView) {
            xmlscrollview.setVisibility(View.GONE);
            webViewBibleDownload.setVisibility(View.VISIBLE);
            webViewBibleDownload.loadUrl("https://sourceforge.net/projects/zefania-sharp/files/Bibles/");
            webViewCloseFAB.show();
        } else {
            xmlscrollview.setVisibility(View.VISIBLE);
            webViewBibleDownload.setVisibility(View.GONE);
            webViewCloseFAB.hide();
        }
    }

    private void dealWithDownloadFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                extractTheZipFile(downloadedFile);
                deleteTheZipFile(downloadedFile);
                // Update the list of bible files available
                updateBibleFiles();
            }
        }).start();
    }

    private void extractTheZipFile(Uri newuri) {
        // Unzip the file
        StorageAccess storageAccess = new StorageAccess();
        storageAccess.extractBibleZipFile(getActivity(), preferences, newuri);
    }

    private void deleteTheZipFile(Uri newuri) {
            StorageAccess storageAccess = new StorageAccess();
            storageAccess.deleteFile(getActivity(), newuri);
    }

    private final BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            hideViewsIfNeeded(false);
            StaticVariables.myToastMessage = Objects.requireNonNull(getActivity()).getString(R.string.wait);
            _ShowToast.showToast(getActivity());

            // Copy the zip file
            dealWithDownloadFile();
        }
    };
}*/
