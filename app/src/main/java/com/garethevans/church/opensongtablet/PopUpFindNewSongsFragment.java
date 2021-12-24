package com.garethevans.church.opensongtablet;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class PopUpFindNewSongsFragment extends DialogFragment {

    private LinearLayout searchtext_LinearLayout, newfileinfo_LinearLayout;
    private EditText searchphrase_EditText, songfilename_EditText;
    private RelativeLayout searchresults_RelativeLayout;
    private WebView webresults_WebView;
    private Button saveSong_Button;
    private ProgressBar grabSongData_ProgressBar;
    private Spinner choosefolder_Spinner;
    private String weblink = "https://www.ultimate-guitar.com/";
    private String filename;
    private String authorname;
    private String filecontents;
    private String newtext;
    private String whatfolderselected;
    private ArrayList<String> newtempfolders;
    private MyInterface mListener;
    private boolean downloadcomplete = false;
    private TextSongConvert textSongConvert;
    private StorageAccess storageAccess;
    private Preferences preferences;
    private SongFolders songFolders;
    private Uri downloadedFile;
    private ChordProConvert chordProConvert;

    static PopUpFindNewSongsFragment newInstance() {
        PopUpFindNewSongsFragment frag;
        frag = new PopUpFindNewSongsFragment();
        return frag;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        mListener = (MyInterface) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String mTitle;
        switch (FullscreenActivity.whattodo) {
            case "chordie":
                mTitle = getString(R.string.chordiesearch);
                break;
            case "songselect":
                mTitle = getString(R.string.songselect);
                break;
            case "worshiptogether":
                mTitle = getString(R.string.worshiptogether);
                break;
            case "ukutabs":
                mTitle = getString(R.string.ukutabs);
                break;
            case "worshipready":
                mTitle = getString(R.string.worshipready);
                break;
            case "holychords":
                mTitle = getString(R.string.holychords);
                break;
            default:
                mTitle = getString(R.string.ultimateguitarsearch);
                break;
        }
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }
        View V = inflater.inflate(R.layout.popup_findnewsongs, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(mTitle);
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            try {
                CustomAnimations.animateFAB(closeMe,getContext());
                closeMe.setEnabled(false);
                dismiss();
            } catch (Exception e) {
                // Error cancelling
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        textSongConvert = new TextSongConvert();
        storageAccess = new StorageAccess();
        preferences = new Preferences();
        songFolders = new SongFolders();
        chordProConvert = new ChordProConvert();

        // Initialise the views
        searchtext_LinearLayout = V.findViewById(R.id.searchtext_LinearLayout);
        searchphrase_EditText = V.findViewById(R.id.searchphrase_EditText);
        searchresults_RelativeLayout = V.findViewById(R.id.searchresults_RelativeLayout);
        webresults_WebView = V.findViewById(R.id.webresults_WebView);
        ImageButton webBack_ImageButton = V.findViewById(R.id.webBack_ImageButton);
        Button grabSongData_Button = V.findViewById(R.id.grabSongData_Button);
        grabSongData_ProgressBar = V.findViewById(R.id.grabSongData_ProgressBar);
        Button doSearch_Button = V.findViewById(R.id.doSearch_Button);
        newfileinfo_LinearLayout = V.findViewById(R.id.newfileinfo_LinearLayout);
        songfilename_EditText = V.findViewById(R.id.songfilename_EditText);
        choosefolder_Spinner = V.findViewById(R.id.choosefolder_Spinner);
        saveSong_Button = V.findViewById(R.id.saveSong_Button);

        // Set the text if it exists
        searchphrase_EditText.setText(FullscreenActivity.phrasetosearchfor);

        // Set the folder spinner
        AsyncTask<Object, Void, String> getfolders = new GetFolders();
        try {
            getfolders.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d", "Probably closed popup before folders listed\n" + e);
        }
        choosefolder_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                whatfolderselected = newtempfolders.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // By default, hide the searchresults_RelativeLayout section
        searchresults_RelativeLayout.setVisibility(View.GONE);
        searchtext_LinearLayout.setVisibility(View.VISIBLE);
        newfileinfo_LinearLayout.setVisibility(View.GONE);

        // Listen for the buttons

        doSearch_Button.setOnClickListener(view -> {
            String searchtext = searchphrase_EditText.getText().toString();
            if (!searchtext.equals("")) {
                FullscreenActivity.phrasetosearchfor = searchtext;
                doSearch(searchtext);
            }
        });

        webBack_ImageButton.setOnClickListener(view -> {
            try {
                webresults_WebView.goBack();
            } catch (Exception e) {
                // Error going back in the web view
            }
        });
        grabSongData_Button.setOnClickListener(view -> grabchordpro());

        saveSong_Button.setOnClickListener(view -> doSaveSong());

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    @SuppressLint({"SetJavaScriptEnabled", "deprecation", "AddJavascriptInterface"})
    private void doSearch(String searchtext) {

        searchresults_RelativeLayout.setVisibility(View.VISIBLE);
        searchtext_LinearLayout.setVisibility(View.GONE);

        switch (FullscreenActivity.whattodo) {
            case "chordie":
                weblink = "https://www.chordie.com/results.php?q=" + searchtext + "&np=0&ps=10&wf=2221&s=RPD&wf=2221&wm=wrd&type=&sp=1&sy=1&cat=&ul=&np=0";
                break;
            case "ultimate-guitar":
                weblink = "https://www.ultimate-guitar.com/search.php?page=1&tab_type_group=text&app_name=ugt&order=myweight&type=300&title=" + searchtext;
                //weblink = "https://www.ultimate-guitar.com/search.php?search_type=title&order=&value=" + searchtext;
                break;
            case "worshiptogether":
                weblink = "https://www.worshiptogether.com/search-results/#?cludoquery=" + searchtext;
                break;
            case "worshipready":
                weblink = "https://www.worshipready.com/chord-charts";
                //weblink = "https://www.worshipready.com/index.php?option=com_zoo&Itemid=199&app_id=2&controller=zoofilter&lang=en&search_id=74286&task=dosearch";
                break;
            case "songselect":
                weblink = "https://songselect.ccli.com/Search/Results?SearchText=" + searchtext;
                break;
            case "ukutabs":
                weblink = "https://ukutabs.com/?s=" + searchtext;
                break;
            case "holychords":
                weblink = "https://holychords.com/search/?id=" + searchtext;
                break;
        }

        webresults_WebView.setWebChromeClient(new WebChromeClient());
        webresults_WebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        //String newUA = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
        String newUA = "Mozilla/5.0 (X11; Linux i686; rv:64.0) Gecko/20100101 Firefox/64.0";
        //String oldUA = "Mozilla/5.0 (Linux; U; Android 4.0.4; en-gb; GT-I9300 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
        webresults_WebView.getSettings().setUserAgentString(newUA);
        webresults_WebView.getSettings().getJavaScriptEnabled();
        webresults_WebView.getSettings().setJavaScriptEnabled(true);
        webresults_WebView.getSettings().setDomStorageEnabled(true);
        webresults_WebView.getSettings().setLoadWithOverviewMode(true);
        webresults_WebView.getSettings().setUseWideViewPort(true);
        webresults_WebView.getSettings().setSupportZoom(true);
        webresults_WebView.getSettings().setBuiltInZoomControls(true);
        webresults_WebView.getSettings().setDisplayZoomControls(false);
        webresults_WebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        webresults_WebView.setScrollbarFadingEnabled(false);
        webresults_WebView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        try {
            requireActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } catch (Exception e) {
            Log.d("d","Error registering download complete listener");
        }
        webresults_WebView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            final String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
            if (FullscreenActivity.whattodo.equals("songselect") && (filename.endsWith(".pdf")||filename.endsWith(".PDF"))) {

                try {
                    // Hide the WebView
                    searchresults_RelativeLayout.setVisibility(View.GONE);
                    StaticVariables.myToastMessage = "Downloading...";
                    saveSong_Button.setEnabled(false);
                    ShowToast.showToast(getContext());

                    String cookie = CookieManager.getInstance().getCookie(url);

                    DownloadManager.Request request = new DownloadManager.Request(
                            Uri.parse(url));

                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
                    downloadedFile = Uri.fromFile(file);
                    DownloadManager dm = (DownloadManager) requireActivity().getSystemService(DOWNLOAD_SERVICE);
                    request.addRequestHeader("Cookie", cookie);
                    if (dm != null) {
                        dm.enqueue(request);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        webresults_WebView.loadUrl(weblink);
    }

    private final BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            downloadcomplete = true;
            saveSong_Button.setEnabled(true);
            try {
                requireActivity().unregisterReceiver(onComplete);
            } catch (Exception e) {
                Log.d("d","Error unregistering receiver");
            }
        }
    };

    @Override
    public void onDismiss(@NonNull final DialogInterface dialog) {
        if (mListener != null) {
            mListener.pageButtonAlpha("");
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        try {
            this.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void grabchordpro() {
        // Need to run a async task to grab html text
        grabSongData_ProgressBar.setVisibility(View.VISIBLE);
        weblink = webresults_WebView.getUrl();
        StaticVariables.myToastMessage = getText(R.string.chordproprogress).toString();
        ShowToast.showToast(getContext());
        DownloadWebTextTask task = new DownloadWebTextTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, weblink);

        // If we are in songselect, trigger the download to keep the stats live
        if (FullscreenActivity.whattodo.equals("songselect") && !(FullscreenActivity.phrasetosearchfor.startsWith("?"))) {
            try {
                // Trigger the download of the pdf
                webresults_WebView.loadUrl("javascript:document.getElementById('chordSheetDownloadButton').click()");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void fixChordieContent(String resultposted) {
        // Song is from Chordie!
        String temptitle = FullscreenActivity.phrasetosearchfor;

        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
        // Find the position of the start of this section
        int getstart = resultposted.indexOf("<textarea id=\"chordproContent\"");
        int startpos = resultposted.indexOf("\">", getstart) + 2;
        if (getstart < 1) {
            // We are using the other version of the file content
            getstart = resultposted.indexOf("<h1 class=\"titleLeft\"");
            startpos = resultposted.indexOf(">",getstart) + 1;

            // Remove everything before this position
            resultposted = resultposted.substring(startpos);

            // The title is everything up to &nbsp; or <span>
            int endpos = resultposted.indexOf("&nbsp;");
            if (endpos>100) {
                endpos = resultposted.indexOf("<span>");
            }
            if (endpos>0) {
                temptitle = resultposted.substring(0, endpos);
            }

            // The author is between the first <span> and </span>
            startpos = resultposted.indexOf("<span>")+6;
            endpos = resultposted.indexOf("</span>");
            if (endpos>startpos && startpos>-1 && endpos<200) {
                authorname = resultposted.substring(startpos,endpos);
            }

            // Get the rest of the song content
            startpos = resultposted.indexOf("<div");
            endpos = resultposted.indexOf("<div id=\"importantBox\"");

            if (startpos>-1 && startpos<endpos) {
                resultposted = resultposted.substring(startpos,endpos);
            }

            StringBuilder contents = new StringBuilder();
            // Split into lines
            String[] lines = resultposted.split("\n");
            for (String line:lines) {
                line = line.replaceAll("<(.*?)>", "");
                line = line.replaceAll("<(.*?)\n", "");
                line = line.replaceFirst("(.*?)>", "");
                line = line.replaceAll("&nbsp;", " ");
                line = line.replaceAll("&amp;", "&");

                if (!line.equals("")) {
                    contents.append(line);
                    contents.append("\n");
                }
            }
            filename = temptitle.trim();
            filecontents = contents.toString();

        } else {
            // Remove everything before this position - using the desktop version
            resultposted = resultposted.substring(startpos);

            // Find the position of the end of the form
            int endpos = resultposted.indexOf("</textarea>");
            if (endpos < 0) {
                endpos = resultposted.length();
            }
            resultposted = resultposted.substring(0, endpos);

            //Replace all \r with \n
            resultposted = resultposted.replace("\r", "\n");
            resultposted = resultposted.trim();

            // Try to get the title of the song from the metadata
            int startpostitle = resultposted.indexOf("{t:");
            int endpostitle = resultposted.indexOf("}", startpostitle);
            if (startpostitle >= 0 && startpostitle < endpostitle && endpostitle < startpostitle + 50) {
                temptitle = resultposted.substring(startpostitle + 3, endpostitle);
            }
            filename = temptitle.trim();
            filecontents = resultposted;
        }
    }

    private void fixWTContent(String resultposted) {
        // From Worship Together

        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);

        // Try to find the title
        // By default use the title of the page as a default

        String title_resultposted;
        String filenametosave = "WT Song";
        authorname = "";
        String copyright = "";
        String bpm = "";
        String ccli = "";
        StringBuilder lyrics = new StringBuilder();
        String key = "";

        // Get the song title
        int startpos = resultposted.indexOf("<title>");
        int endpos = resultposted.indexOf("</title>");
        if (startpos > -1 && endpos > -1 && startpos < endpos) {
            title_resultposted = resultposted.substring(startpos + 7, endpos);
            title_resultposted = title_resultposted.trim();
            int pos_of_extra = title_resultposted.indexOf(" - ");
            if (pos_of_extra>-1) {
                title_resultposted = title_resultposted.substring(0,pos_of_extra);
            }
            pos_of_extra = title_resultposted.indexOf("Lyrics and Chords");
            if (pos_of_extra>-1) {
                title_resultposted = title_resultposted.substring(0,pos_of_extra);
            }
            title_resultposted = title_resultposted.replace("|","");

            pos_of_extra = title_resultposted.indexOf("Worship Together");
            if (pos_of_extra>-1) {
                title_resultposted = title_resultposted.substring(0,pos_of_extra);
            }
            filenametosave = title_resultposted.trim();
            filename = filenametosave;
        }

        Log.d("WT","filename="+filename);

        String song_taxonomy;
        startpos = resultposted.indexOf("<div class=\"song_taxonomy\">");
        endpos = resultposted.indexOf("</body>",startpos);
        if (startpos > -1 && endpos > -1 && startpos < endpos) {
            // Extract the song taxonomy so we can edit this bit quickly
            song_taxonomy = resultposted.substring(startpos,endpos);

            // Try to get the author data
            startpos = song_taxonomy.indexOf("Writer(s):");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                authorname = getRidOfExtraCodeWT(song_taxonomy.substring(startpos+10,endpos));
            }

            // Try to get the copyright data
            startpos = song_taxonomy.indexOf("Ministry(s):");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                copyright = getRidOfExtraCodeWT(song_taxonomy.substring(startpos+12,endpos));
            }

            // Try to get the bpm data
            startpos = song_taxonomy.indexOf("BPM:");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                bpm = getRidOfExtraCodeWT(song_taxonomy.substring(startpos+4,endpos));
            }

            // Try to get the ccli data
            startpos = song_taxonomy.indexOf("CCLI #:");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                ccli = getRidOfExtraCodeWT(song_taxonomy.substring(startpos+7,endpos));
            }

            // Try to get the key data
            startpos = song_taxonomy.indexOf("Original Key(s):");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                key = getRidOfExtraCodeWT(song_taxonomy.substring(startpos+16,endpos));
            }

        }

        Log.d("WT","authorname="+authorname);
        Log.d("WT","copyright="+copyright);
        Log.d("WT","ccli="+ccli);

        String[] lines = resultposted.split("\n");

        // Now try to get the chordpro file contents
        startpos = resultposted.indexOf("<div class='chord-pro-line'>");
        endpos = resultposted.indexOf("<div class=\"song_taxonomy\">",startpos);
        if (startpos > -1 && endpos > -1 && startpos < endpos) {
            lyrics = new StringBuilder(resultposted.substring(startpos, endpos));

            // Split the lines up
            String[] ls = lyrics.toString().split("\n");
            for (String l:ls) {
                Log.d("WT","l:"+l);
            }
            StringBuilder newline = new StringBuilder();
            lyrics = new StringBuilder();
            // Go through each line and do what we need
            for (String l : lines) {
                l = l.trim();
                boolean emptystuff = false;
                if (l.equals("</div") || l.contains("<div class='chord-pro-br'>") ||
                        l.contains("<div class='chord-pro-segment'>") || l.contains("<div class='inner_col'")) {
                    emptystuff = true;
                }

                if (!emptystuff && l.contains("<div class='chord-pro-disp'")) {
                    // Start section, so initialise the newline and lyrics
                    lyrics = new StringBuilder();
                    newline = new StringBuilder();

                } else if (!emptystuff && l.contains("<div class='chord-pro-line'>")) {
                    // Starting a new line, so add the previous newline to the lyrics text
                    lyrics.append("\n").append(newline);
                    newline = new StringBuilder();

                } else if (!emptystuff && l.contains("<div class='chord-pro-note'>")) {
                    // This is a chord
                    startpos = l.indexOf("<div class='chord-pro-note'>");
                    startpos = l.indexOf("'>",startpos);
                    endpos = l.indexOf("</div>",startpos);
                    if (endpos==-1) {
                        endpos = l.length();
                    }
                    if (startpos > -1 && startpos < endpos) {
                        String chordbit = l.substring(startpos+2,endpos);
                        if (!chordbit.isEmpty()) {
                            newline.append("[").append(l, startpos + 2, endpos).append("]");
                            //newline.append("[").append(l.substring(startpos + 2, endpos)).append("]");
                        }
                    }


                } else if (!emptystuff && l.contains("<div class='chord-pro-lyric'>")) {
                    // This is lyrics
                    startpos = l.indexOf("<div class='chord-pro-lyric'>");
                    startpos = l.indexOf("'>",startpos);
                    endpos = l.indexOf("</div>",startpos);
                    if (endpos==-1) {
                        endpos = l.length();
                    }
                    if (startpos > -1 && startpos < endpos) {
                        newline.append(l, startpos + 2, endpos);
                        //newline.append(l.substring(startpos + 2, endpos));
                    }
                }
            }
        }

        // Build the chordpro file contents:
        filecontents  = "{title:"+filenametosave+"}\n";
        filecontents += "{artist:"+authorname+"}\n";
        filecontents += "{copyright:"+copyright+"}\n";
        filecontents += "{ccli:"+ccli+"}\n";
        filecontents += "{key:"+key+"}\n";
        filecontents += "{tempo:"+bpm+"}\n\n";
        filecontents += lyrics.toString().trim();

        newtext = filecontents;

        if (lyrics.toString().trim().isEmpty() || lyrics.toString().trim().equals("")) {
            filecontents = null;
        }
    }

    private String getRidOfExtraCodeWT(String s) {
        s = s.replace("<strong>","");
        s = s.replace("</strong>","");
        s = s.replace("<p>","");
        s = s.replace("</p>","");
        s = s.replace("</a>","");
        s = s.replace("<a>","");
        s = s.replace("<span>","");
        s = s.replace("</span>","");
        while (s.contains("<a href")) {
            // Remove the hypertext references
            int startpos = s.indexOf("<a href");
            int endpos = s.indexOf("'>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                String bittoremove = s.substring(startpos,endpos+2);
                s = s.replace(bittoremove,"");
            } else {
                // Problem, so just get rid of it all
                s = "";
            }
        }
        s = s.replace("\n","");
        s = s.trim();
        return s;
    }

    private String getContentUG(String s) {
        int startpos = s.indexOf("&quot;content&quot;:");
        int endpos = s.indexOf("[/tab]&quot;");
        if (startpos>-1 && endpos>-1 && endpos>-startpos) {
            s = s.substring(startpos,endpos);
        }
        return s;
    }
    private String getPageTitleUG(String s) {
        int startpos = s.indexOf("<title");
        startpos = s.indexOf(">",startpos);
        int endpos = s.indexOf("</title",startpos);
        if (startpos>-1 && endpos>startpos) {
            s = s.substring(startpos+1,endpos);
            s = s.replace("(Chords)","");
            s = stripExtraUG(s);
            return s;
        } else {
            return "";
        }
    }
    private String getShortenedPageTitleUG(String s) {
        if (s.contains("By")) {
            s = s.substring(0,s.indexOf("By"));
            s = s.trim();
        }
        return s;
    }
    private String getPageAuthorUG(String s) {
        if (s.contains("By")) {
            s = s.substring(s.indexOf("By")+2);
            s = s.trim();
            return s;
        } else {
            return "";
        }
    }
    private String getTitleUG(String s,String pagetitle) {
        int startpos = s.indexOf("Song:");
        int endpos = s.indexOf("NEW_LINE_OS",startpos);
        if (startpos > -1 && endpos > -1 && startpos < endpos) {
            s = s.substring(startpos + 5, endpos);
            s = stripExtraUG(s);
            return s;
        } else if (pagetitle==null || pagetitle.equals("")){
            try {
                return FullscreenActivity.phrasetosearchfor;
            } catch (Exception e) {
                return "UG song";
            }
        } else {
            return pagetitle;
        }
    }
    private String getAuthorUG(String s,String pageauthor) {
        int startpos = s.indexOf("Artist:");
        int endpos = s.indexOf("NEW_LINE_OS",startpos);
        if (startpos > -1 && endpos > -1 && endpos > startpos) {
            s = s.substring(startpos+7, endpos);
            s = stripExtraUG(s);
            return s;
        } else if (pageauthor==null) {
            return "";
        } else {
            return pageauthor;
        }
    }
    private String getLyricsUG(String s) {
        s = s.replace("&quot;", "");
        s = s.replace("content:","");
        String[] lines = s.split("NEW_LINE_OS");
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : lines) {
            if (line.contains("ultimate-guitar.com") && line.contains("{") &&
                    (line.contains(":true") || line.contains(":false")) && line.contains("content:")) {
                // No artist/author tags, so strip out div code line
                line = line.substring(line.indexOf("content:")+8);
            }
            line = line.replace("[tab]", "");
            line = line.replace("[/tab]", "");
            if (line.contains("[ch]")) {
                // Chord line
                line = line.replace("[ch]", "");
                line = line.replace("[/ch]", "");
                line = "." + line;
            } else {
                if (!line.startsWith("[") && !line.startsWith(" ")) {
                    line = " " + line;
                }
            }
            if (line.contains("tab_access_type:")) {
                int upto = line.indexOf("tab_access_type:");
                if (upto>0) {
                    line = line.substring(0,upto);
                    if (line.endsWith(",") && line.length()>1) {
                        line = line.substring(0,line.length()-1);
                    }
                } else {
                    line = "";
                }
            }
            stringBuilder.append(line).append("\n");
        }

        String string = stringBuilder.toString();
        string = PopUpEditSongFragment.parseToHTMLEntities(string);

        // Fix accented characters into UTF
        string = chordProConvert.parseHTML(string);

        return string;
    }
    private String stripExtraUG(String s) {
        s = s.replace(" @ Ultimate-Guitar.Com", "");
        s = s.replace("&amp;","&");
        s = s.replace("&","&amp;");
        s = s.replace("\r", "");
        s = s.replace("\n", "");
        s = PopUpEditSongFragment.parseToHTMLEntities(s);
        s = s.trim();
        return s;
    }
    private void fixUGContent(String resultposted) {
        // From ultimate guitar

        String pagetitle = getPageTitleUG(resultposted);
        String pageauthor = getPageAuthorUG(pagetitle);
        pagetitle = getShortenedPageTitleUG(pagetitle);

        String[] tl = resultposted.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String t:tl) {
            Log.d("FindNewSongs",t);
            sb.append(t).append("NEW_LINE_OS");
        }
        resultposted = sb.toString();

        // Shorten down what we need
        if (resultposted.contains("<div class=\"js-store\"")) {
            resultposted = resultposted.substring(resultposted.indexOf("<div class=\"js-store\""));
        }

        if (resultposted.contains("</div")) {
            resultposted = resultposted.substring(0,resultposted.indexOf("</div>"));
        }

        resultposted = resultposted.replace("\\r\\n","NEW_LINE_OS");

        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);

        String title_resultposted;
        String filenametosave;

        // Get the content we need from the rest of the page
        resultposted = getContentUG(resultposted);

        // Get the title and filename
        title_resultposted = getTitleUG(resultposted,pagetitle);
        filenametosave = title_resultposted;

        // Get the author
        authorname = getAuthorUG(resultposted,pageauthor);



        // Get the key


        // Get rid of possible <div first line
        StringBuilder sb2 = new StringBuilder();
        String[] lines2 = resultposted.split("NEW_LINE_OS");
        for (String line:lines2) {
            if (!line.startsWith("<div")) {
                sb2.append(line).append("NEW_LINE_OS");
            }
        }
        resultposted = sb2.toString();

        // Get the lyrics and chords
        newtext = getLyricsUG(resultposted);

        if (!filenametosave.equals("")) {
            filename = filenametosave.trim();
        } else {
            filename = FullscreenActivity.phrasetosearchfor;
        }
    }

    private void fixUkutabsContent(String resultposted) {
        // From UkuTabs.com
        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);

        // Try to find the title
        // By default use the title of the page as a default
        String title_resultposted;
        String filenametosave = "UkuTabs Song";
        authorname = "";
        newtext = "";

        int start;
        int end;

        resultposted = resultposted.replace("&quot;","'");
        resultposted = resultposted.replace("&amp;","&");
        resultposted = resultposted.replace("&#39;","'");
        resultposted = resultposted.replace("&#039;","'");

        if (resultposted.contains("<title>") && resultposted.contains("</title>")) {
            start = resultposted.indexOf("<title>") + 7;
            end = resultposted.indexOf("</title>");
            if (start>-1 && end>-1 && end>start) {
                String meta = resultposted.substring(start, end);
                if (meta.contains(" by ")) {
                    String[] bits = meta.split(" by ");
                    if (bits[0]!=null) {
                        if (bits[0].startsWith("'")) {
                            bits[0] = bits[0].substring(1);
                        }
                        if (bits[0].endsWith("'")) {
                            bits[0] = bits[0].substring(0,bits[0].length()-1);
                        }
                        filenametosave = bits[0];
                    }
                    if (bits[1]!=null) {
                        if (bits[1].startsWith("'")) {
                            bits[1] = bits[1].substring(1);
                        }
                        if (bits[1].endsWith("'")) {
                            bits[1] = bits[1].substring(0,bits[1].indexOf("'")-1);
                        }
                        authorname = bits[1];
                    }
                }
            }
        }

        // The meta stuff begins after the last typeof="v:Breadcrumb" text
        if (resultposted.contains("v:Breadcrumb") && resultposted.contains("post-meta")) {
            start = resultposted.lastIndexOf("v:Breadcrumb");
            end = resultposted.indexOf("post-meta",start);
            if (start>-1 && end>-1 && end>start) {
                String metadata = resultposted.substring(start,end);

                // Remove the rubbish and get the author
                start = metadata.indexOf("v:title");
                end = metadata.indexOf("</a>", start);
                if (start>-1 && end>-1 && end>start) {
                    metadata = metadata.substring(start);
                    start = metadata.indexOf(">") + 1;
                    authorname = metadata.substring(start, end);
                }

                // Remove the rubbish and get the title
                start = metadata.indexOf("breadcrumb_last\">");
                end = metadata.indexOf("</strong>",start);
                if (start>-1 && end>-1 && end>start) {
                    start = start + 17;
                    title_resultposted = metadata.substring(start, end);
                    title_resultposted = title_resultposted.replace("&apos;","'");
                    filenametosave = title_resultposted;
                }
            }
        }

        // Now try to extract the lyrics
        start = resultposted.indexOf("<pre class=\"qoate-code\">");
        end = resultposted.indexOf("</pre>");
        String templyrics = "";
        if (start>-1 && end>-1 && end>start) {
            templyrics = resultposted.substring(start + 24, end);
        }

        StringBuilder sb = new StringBuilder();

        // Split the lyrics into lines
        String[] lines = templyrics.split("\n");
        for (String l:lines) {

            // Remove the stuff we don't want
            l = l.replace("<span>","");
            l = l.replace("</span>","");

            // Try to sort the tags
            l = l.replace("<strong>","[");
            l = l.replace("</strong>","]");
            l = l.replace("]:","]");

            // Identify the chord lines
            boolean chordline = false;
            if (l.contains("<a")) {
                chordline = true;
            }

            // Remove any hyperlinks
            while (l.contains("<a")) {
                start = l.indexOf("<a");
                end = l.indexOf(">", start);
                String remove = l.substring(start, end);
                l = l.replace(remove,"");
            }
            while (l.contains("</a>")) {
                l = l.replace("</a>","");
            }
            while (l.contains("<a>")) {
                l = l.replace("<a>","");
            }

            if (chordline) {
                l = "." + l;
            }

            // If we have tags and chords, split them
            if (l.startsWith(".") && l.contains("[") && l.contains("]")) {
                l = l.replace(".[", "[");
                l = l.replace("]","]\n.");
            }

            // Remove italics
            while (l.contains("<i ")) {
                start = l.indexOf("<i ");
                end = l.indexOf(">", start);
                String remove = l.substring(start, end);
                l = l.replace(remove,"");
            }
            while (l.contains("</i>")) {
                l = l.replace("</i>","");
            }

            // Remove images
            while (l.contains("<img ")) {
                start = l.indexOf("<img ");
                end = l.indexOf(">", start);
                String remove = l.substring(start, end);
                l = l.replace(remove,"");
            }

            l = l.replaceAll("<(.*?)>", "");
            l = l.replaceAll("<(.*?)\n", "");
            l = l.replaceFirst("(.*?)>", "");
            l = l.replaceAll("&nbsp;", " ");
            l = l.replaceAll("&amp;", "&");

            l = l.replace(">","");
            l = l.replace("<","");

            // Add a blank space to the beginning of lyrics lines
            if (!l.startsWith(".") && !l.startsWith("[")) {
                l = " " + l;
            }

            sb.append(l).append("\n");
        }

        newtext = TextUtils.htmlEncode(sb.toString());

        if (!filenametosave.equals("")) {
            filename = filenametosave.trim();
        } else {
            filename = FullscreenActivity.phrasetosearchfor;
        }
    }

    private void fixHolyChordsContent(String resultposted) {
        // from holychords.com
        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);

        // Try to find the title
        // By default use the title of the page as a default

        String title_resultposted = "HolyChords Song";
        String filenametosave;
        authorname = "";
        int startpos;
        int endpos;

        // Try to get the best title
        // First up, use the page title
        if (resultposted.contains("<title>") && resultposted.contains("</title>")) {
            startpos = resultposted.indexOf("<title>") + 7;
            endpos = resultposted.indexOf("</title>");
            if (endpos>startpos) {
                title_resultposted = resultposted.substring(startpos,endpos);
                authorname = title_resultposted;
            }
        }

        // Fix author and title (if it was copied as the title);
        String text = authorname.replace("|","___");
        String[] titlebits = text.split("___");
        if (titlebits.length>1) {
            title_resultposted = titlebits[0].trim();
            authorname = titlebits[1].trim();
        }

        // If there is the title tag, use this instead
        startpos = resultposted.indexOf("<meta property=\"og:site_name\" content=\"") + 39;
        endpos = resultposted.indexOf(">",startpos);
        if (startpos>-1 && endpos>-1 && endpos>startpos) {
            title_resultposted = resultposted.substring(startpos,endpos);
        }

        // If there is a song tag, even better
        startpos = resultposted.indexOf("<meta property=\"music:song\" content=\"") + 37;
        endpos = resultposted.indexOf(">",startpos);
        if (startpos>-1 && endpos>-1 && endpos>startpos) {
            title_resultposted = resultposted.substring(startpos,endpos);
        }

        // If there is a musician tag, even better
        startpos = resultposted.indexOf("<meta property=\"music:musician\" content=\"") + 41;
        endpos = resultposted.indexOf(">",startpos);
        if (startpos>-1 && endpos>-1 && endpos>startpos) {
            authorname = resultposted.substring(startpos,endpos);
        }

        title_resultposted = title_resultposted.replace("/","");
        title_resultposted = title_resultposted.replace("/","");
        title_resultposted = title_resultposted.replace("\"","");
        title_resultposted = title_resultposted.replace(">","");
        title_resultposted = title_resultposted.trim();

        authorname = authorname.trim();

        filenametosave = title_resultposted;

        // Everything is found inside the <pre  and </pre> tags
        startpos = resultposted.indexOf("<pre");
        startpos = resultposted.indexOf(">",startpos) + 1;
        // Remove everything before this
        resultposted = resultposted.substring(startpos);
        // Get everything in the <pre> section
        endpos = resultposted.indexOf("</pre");
        if (endpos>0) {
            resultposted = resultposted.substring(0,endpos);
        }

        newtext = resultposted.replace("<br>","\n");
        newtext = newtext.replace("<br />","");

        newtext = PopUpEditSongFragment.parseToHTMLEntities(newtext);

        if (!filenametosave.equals("")) {
            filename = filenametosave.trim();
        } else {
            filename = FullscreenActivity.phrasetosearchfor;
        }
    }


    // Song Select Code
    private String extractSongSelectChordPro(String s, String temptitle) {
        // Get the title which also sets filename
        String title = getTitleSongSelectChordPro(s, temptitle);

        // Extract the key
        String key = getKeySongSelectChordPro(s);

        // Extract the author
        String author = getAuthorSongSelectChordPro(s);

        // Extract the tempo and time signature
        String tempo = getTempoSongSelectChordPro(s);
        String timesig = getTimeSigSongSelectChordPro(s);

        // Extract the CCLI song number
        String ccli = getCCLISongSelectChordPro(s);

        // Extract the Copyright info
        String copyright = getCopyrightSongSelectChordPro(s);

        // Extract the lyrics
        String lyrics =  getLyricsSongSelectChordPro(s);

        // Return the ChordPro version of the song
        if (lyrics.equals("")) {
            return null;
        } else {
            return title + author + copyright + ccli + key + tempo + timesig + "\n" + lyrics;
        }
    }

    private String getTitleSongSelectChordPro(String s, String temptitle) {
        // Extract the title

        // IV - Try chordpro style
        int start = s.indexOf("<span class=\"cproTitle\">");
        int end = s.indexOf("</span>",start);

        // IV - Try song viewer style
        if (start == -1) {
            start = s.indexOf("<h2 class=\"song-viewer-title\">");
            end = s.indexOf("</h2>",start);
        }

        // IV - Try page style
        if (start == -1) {
            start = s.indexOf("<div class=\"content-title\">");
            if (start > -1) {
                start = s.indexOf("<h1>", start);
                end = s.indexOf("</h1>", start);
            }
        }

        if (start>-1 && end>-1 && end>start) {
            start = s.indexOf(">",start) + 1;
            String t = s.substring(start,end);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                t = Html.fromHtml(t, 0).toString();
            } else {
                t = Html.fromHtml(t).toString();
            }
            filename = t;
            return "{title:" + t + "}\n";
        } else {
            filename = temptitle;
            return "{title:" + temptitle + "}\n";
        }
    }

    private String getKeySongSelectChordPro(String s) {
        int start = s.indexOf("<code class=\"cproSongKey\"");
        int end = s.indexOf("</code>",start);
        if (start>-1 && end>-1 && end>start) {
            // Fine tune the start
            int newstart = s.indexOf(">",start);
            if (newstart<0) {
                newstart = start;
            }
            return "{key:" + s.substring(newstart+1,end).trim() + "}\n";
        } else {
            return "";
        }
    }
    private String getAuthorSongSelectChordPro(String s) {
        // Extract the author
        // IV - Try chordpro style
        int start = s.indexOf("<span class=\"cproAuthors\">");
        int end = s.indexOf("</span>",start);

        // IV - Try song viewer style
        if (start == -1) {
            start = s.indexOf("<p class=\"contributor\">");
            end = s.indexOf("</p>",start);
        }

        if (start>-1 && end>-1 && end>start) {
            start = s.indexOf(">",start) + 1;
            // IV - Remove line breaks, replace non breaking spaces, replace use of | as separator with comma
            String a = s.substring(start,end).
                    replaceAll("<br>",", ").
                    replace("</li><li>",", ").
                    replace("<li>","").
                    replace ("</li>","").
                    replaceAll("&nbsp;"," ").
                    replaceAll("\u00A0"," ").
                    replaceAll("\\Q |\\E",",");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                a = Html.fromHtml(a, 0).toString();
            } else {
                a = Html.fromHtml(a).toString();
            }
            return "{artist:" + a + "}\n";
        } else {
            return "";
        }
    }
    private String getCCLISongSelectChordPro(String s) {
        // Extract CCLI number
        // IV - Same class for chordpro and song viewer styles
        int start = s.indexOf("<p class=\"songnumber\">");
        int end = s.indexOf("</p>",start);
        if (start>-1 && end>-1 && end>start) {
            // IV - Step over leading words
            String ccli;
            ccli = s.substring(start,end).trim();
            while (ccli.contains(" ")) {
                start = ccli.indexOf(" ");
                ccli = ccli.substring(start + 1);
            }
            return "{ccli:" + ccli + "}\n";
        } else {
            return "";
        }
    }
    private String getCopyrightSongSelectChordPro(String s) {
        // Extract copyright information
        // IV - Same class for chordpro and song viewer styles
        int start = s.indexOf("<ul class=\"copyright\">");
        if (start>-1) {
            start = s.indexOf("<li>",start);
        }
        int end = s.indexOf("</ul>",start);
        if (start>-1 && end>-1 && end>start) {
            start = s.indexOf(">",start) + 1;
            // IV - Remove line breaks, copyright, replace non breaking spaces, replace use of | as separator with comma and remove '(Admin. by)' content
            return "{copyright:" + s.substring(start,end).
                    replace("</li><li>",", ").
                    replace("<li>","").
                    replace ("</li>","").
                    replace("©","").
                    replaceAll("&nbsp;"," ").
                    replaceAll("\u00A0"," ").
                    replaceAll("\\Q |\\E",",").
                    replaceAll(" \\(Admin\\..*?\\)","").
                    trim() + "}\n";
        } else {
            return "";
        }
    }
    private String getTempoSongSelectChordPro(String s) {
        int start = s.indexOf("<span class=\"cproTempoTimeWrapper\">");
        int end = s.indexOf("</span>",start);
        if (start>-1 && end>-1 && end>start) {
            start = s.indexOf(">",start) + 1;
            String both = s.substring(start,end);
            String[] bits = both.split("\\|");
            if (bits.length>0) {
                String t = bits[0].replace("Tempo", "");
                t = t.replace("-", "");
                t = t.replace("bpm", "");
                t = t.replace("BPM", "");
                t = t.replace("Bpm", "");
                t = t.trim();
                return "{tempo:" + t + "}\n";
            } else {
                return "";
            }
        }
        return "";
    }
    private String getTimeSigSongSelectChordPro(String s) {
        int start = s.indexOf("<span class=\"cproTempoTimeWrapper\">");
        int end = s.indexOf("</span>",start);
        if (start>-1 && end>-1 && end>start) {
            start = s.indexOf(">",start) + 1;
            String both = s.substring(start,end);
            String[] bits = both.split("\\|");
            if (bits.length>1) {
                String t = bits[1].replace("Time","");
                t = t.replace("-","");
                t = t.replace("&nbsp;"," ");
                t = t.trim();
                return "{time:" + t + "}\n";
            } else {
                return "";
            }
        }
        return "";
    }
    private String getLyricsSongSelectChordPro(String s) {
        // IV - Added handling of lyrics with with nore than one cproSongBody as split over pages
        StringBuilder lyrics = new StringBuilder();
        while (s.contains("<pre class=\"cproSongBody\">")) {
            int start = s.indexOf("<pre class=\"cproSongBody\">");
            int end = s.indexOf("</pre>",start);

            // IV - Overwrite so that next loop finds any further SongBody
            s = s.replaceFirst("<pre class=\"cproSongBody\">", "<xxxxxxxxxxxxxxxxxxxxxxxx>");

            if (start>-1 && end>-1 && end>start) {
                lyrics.append(s.substring(start + 26, end));

                // Fix the song section headers
                while (lyrics.toString().contains("<span class=\"cproSongSection\"><span class=\"cproComment\">")) {
                    start = lyrics.indexOf("<span class=\"cproSongSection\"><span class=\"cproComment\">");
                    lyrics = new StringBuilder(lyrics.substring(0, start) +
                            (lyrics.substring(start).
                            replaceFirst("<span class=\"cproSongSection\"><span class=\"cproComment\">","#[").
                            replaceFirst("</span>", "]")));
                }

                // Fix the chords
                // Chords are found in a bit like this: <span class="chordWrapper"><code class="chord" data-chordname="D<sup>2</sup>">D<sup>2</sup></code>
                // We replace start with [< and end with ] to give: [<"D<sup>2</sup>">D<sup>2</sup>]
                // The following clean up removes all "<...>" to give [D2]

                while (lyrics.toString().contains("<span class=\"chordWrapper\"><code class=\"chord\" data-chordname=\"")) {
                    start = lyrics.indexOf("<span class=\"chordWrapper\"><code class=\"chord\" data-chordname=\"");
                    lyrics = new StringBuilder(lyrics.substring(0, start) +
                            (lyrics.substring(start).
                            replaceFirst("<span class=\"chordWrapper\"><code class=\"chord\" data-chordname=\"","[<").
                            replaceFirst("</code>","]")));
                }
            }
        }
        // Get rid of code that we don't need
        return getRidOfRogueCode(lyrics.toString());
    }

    private String extractSongSelectUsr(String s, String temptitle) {
        String title = temptitle;
        String author = "";
        String copyright = "";
        String ccli = "";
        String lyrics = "";

        int start;
        int end;

        start = s.indexOf("<div class=\"song-viewer lyrics\" id=\"song-viewer\">");
        end = s.indexOf("</div>", start);
        if (start > -1 && end > -1 && end > start) {
            start = s.indexOf(">", start) + 1;
            String text = s.substring(start, end).trim();
            // IV - Drop the bottom copyright info
            end = text.indexOf("<div class=\"copyright-info\">");
            if (end >-1) {
                text = text.substring(0, end);
            }
            // IV - Mark a song viewer part as a tag
            text = text.replaceAll("<h3 class=\"song-viewer-part\">","<h3 class=\"song-viewer-part\">#[").
                replaceAll("</h3>","]</h3>");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                text = Html.fromHtml(text, 0).toString();
            } else {
                text = Html.fromHtml(text).toString();
            }
            // IV - Remove extra line after tag
            text = text.replaceAll("]\n\n","]\n");

            // The first line is the title normally
            end = text.indexOf("\n");
            if (end > -1) {
                title = "{title:" + text.substring(0, end).trim() + "}\n";
                filename = text.substring(0, end).trim();
                text = text.substring(end).trim();
            }

            // IV - Use the get routines which are also song viewer style aware
            copyright = getCopyrightSongSelectChordPro(s);
            ccli = getCCLISongSelectChordPro(s);
            author = getAuthorSongSelectChordPro(s);

            lyrics = text;
        }
        if (lyrics.equals("")) {
            return null;
        } else {
            return title + author + copyright + ccli + "\n" + lyrics;
        }
    }

    private String getRidOfRogueCode(String lyrics) {
        // Get rid of lyric indications (IV - etc. Remove any remaining <...>)
        lyrics = lyrics.replaceAll("\\<.*?\\>","");

        // Fix accented characters into UTF
        lyrics = chordProConvert.parseHTML(lyrics);

        // Finally, end trim the lyrics
        return (lyrics.replaceAll("\\s+$", ""));
    }

    private void setFileNameAndFolder() {

        // Hide the searchresults_RelativeLayout
        try {
            searchresults_RelativeLayout.setVisibility(View.GONE);
        } catch (Exception e){
            Log.d("d","Error hiding the search results");
        }
        try {
            newfileinfo_LinearLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.d("d","Error showing the new file info");
        }

        if (FullscreenActivity.phrasetosearchfor==null) {
            FullscreenActivity.phrasetosearchfor = "Untitled";
        }

        // Set the file name if we know it
        if (songfilename_EditText!=null) {
            if (filename != null && !filename.equals("")) {
                songfilename_EditText.setText(filename);
            } else {
                songfilename_EditText.setText(FullscreenActivity.phrasetosearchfor);
            }
        }
    }

    private void doSaveSong() {

        // Get the name of the file we want to use
        String nameoffile = songfilename_EditText.getText().toString();
        if (nameoffile.equals("")) {
            nameoffile = filename;
        }

        // Fix the filename
        String nameofpdffile = nameoffile.replace(".pdf","")+".pdf";   // Gets rid of multilple .pdf extensions

        nameoffile = nameoffile.replace(".pdf","");
        if ((FullscreenActivity.whattodo.equals("chordie") || FullscreenActivity.whattodo.equals("songselect") ||
                FullscreenActivity.whattodo.equals("worshiptogether")) && !nameoffile.endsWith(".chopro")) {
            // Fix the title line in the lyrics

            if (filecontents == null) {
                filecontents = "";
            }
            String[] lines = filecontents.split("\n");
            StringBuilder sb = new StringBuilder();
            for (String l:lines) {
                if (l.contains("{title:")) {
                    l = "{title:"+nameoffile+"}";
                }
                sb.append(l).append("\n");
            }
            filecontents = sb.toString();
            nameoffile = nameoffile + ".chopro";

        } else {
            // Last check of best practice OpenSong formatting
            newtext = textSongConvert.convertText(getContext(),newtext);

            filecontents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<song>\n<title>" + PopUpEditSongFragment.parseToHTMLEntities(filename)
                    + "</title>\n<author>"
                    + PopUpEditSongFragment.parseToHTMLEntities(authorname) + "</author>\n<copyright></copyright>\n<lyrics>"
                    + PopUpEditSongFragment.parseToHTMLEntities(newtext) //Issues cause with &, so fix
                    + "</lyrics>\n</song>";
        }

        // Get the uri for the new file
        nameoffile = storageAccess.safeFilename(nameoffile);
        Uri uri_newfile = storageAccess.getUriForItem(getContext(), preferences, "Songs", whatfolderselected, nameoffile);
        Uri uri_newpdffile;

        // Check the uri exists for the outputstream to be valid
        storageAccess.lollipopCreateFileForOutputStream(getContext(), preferences, uri_newfile, null,
                "Songs", whatfolderselected, nameoffile);

        // Get the outputstream
        OutputStream outputStream = storageAccess.getOutputStream(getContext(),uri_newfile);
        OutputStream outputStreamPDF = null;

        // Get the inputstream of the downloaded file song select
        InputStream inputStream = null;

        // Get the song select pdf file stuff
        if (FullscreenActivity.whattodo.equals("songselect") && downloadcomplete) {
            nameofpdffile = storageAccess.safeFilename(nameofpdffile);
            uri_newpdffile = storageAccess.getUriForItem(getContext(), preferences, "Songs", whatfolderselected, nameofpdffile);

            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(getContext(), preferences, uri_newpdffile, null,
                    "Songs", whatfolderselected, nameofpdffile);

            outputStreamPDF = storageAccess.getOutputStream(getContext(),uri_newpdffile);
            inputStream = storageAccess.getInputStream(getContext(),downloadedFile);
        }

        // Get the database ready
        SQLiteHelper sqLiteHelper = new SQLiteHelper(getContext());

        if (filecontents.equals("") || filecontents.equals("\n")) {
            filecontents = null;
        }

        try {
            if (filecontents!=null) {
                storageAccess.writeFileFromString(filecontents,outputStream);
                // Add song to the database
                sqLiteHelper.createSong(getContext(),whatfolderselected,nameoffile);
            }

            if (FullscreenActivity.whattodo.equals("songselect") && downloadcomplete && outputStreamPDF!=null && inputStream!=null) {
                // Copy the orignal pdf file
                storageAccess.copyFile(inputStream,outputStreamPDF);
                // Add song to the database
                sqLiteHelper.createSong(getContext(),whatfolderselected,nameofpdffile);
                }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set the folder and song to the one we've set here
        StaticVariables.whichSongFolder = whatfolderselected;
        if (filecontents!=null) {
            StaticVariables.songfilename = nameoffile;
        } else if (FullscreenActivity.whattodo.equals("songselect") && downloadcomplete) {
            StaticVariables.songfilename = nameofpdffile;
        }

        // Indicate after loading song (which renames it), we need to build the database and song index
        FullscreenActivity.needtorefreshsongmenu = true;

        if (mListener != null) {
            mListener.loadSong();
            // IV - Moved after load to better report details of the song
            // If we are autologging CCLI information
            if (preferences.getMyPreferenceBoolean(getContext(),"ccliAutomaticLogging",false)) {
                PopUpCCLIFragment.addUsageEntryToLog(getContext(), preferences, StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename,
                    StaticVariables.songfilename, StaticVariables.mAuthor,
                    StaticVariables.mCopyright, StaticVariables.mCCLI, "1"); // Created
            }
            dismiss();
        }
    }

    public interface MyInterface {
        void pageButtonAlpha(String s);
        void loadSong();
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadWebTextTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... addresses) {
            String response = "";
            StringBuilder sb = new StringBuilder();
            for (String address : addresses) {
                URL url;
                HttpURLConnection urlConnection = null;
                try {
                    url = new URL(address);

                    urlConnection = (HttpURLConnection) url.openConnection();

                    InputStream in = urlConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
                    String s;
                    while ((s = buffer.readLine()) != null) {
                        sb.append("\n").append(s);
                        if (s.contains("<div class=\"fb-meta\">") ||
                                s.contains("<div class=\"plus-minus\">") ||
                                s.contains("<section class=\"ugm-ad ugm-ad__bottom\">")) {
                            // Force s to be null as we've got all we need!
                            break;
                        }
                    }
                    response = sb.toString();
                } catch (Exception | OutOfMemoryError e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            // Split the result into lines
            //Now look to see if the webcontent has the ChordPro text in it
            // Check we aren't trying to use the tab-pro page!

            try {
                String address = webresults_WebView.getUrl();
                if (address != null && (address.contains("/tab-pro/") || address.contains("/chords-pro/"))) {
                    StaticVariables.myToastMessage = getText(R.string.not_allowed).toString();
                    ShowToast.showToast(getContext());
                    grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
                } else if (result != null && (result.contains("<textarea id=\"chordproContent\"") ||
                result.contains("<h1 class=\"titleLeft\""))) {
                    // Using Chordie
                    fixChordieContent(result);
                    setFileNameAndFolder();

                } else if (result !=null && result.contains("https://www.worshiptogether.com/")) {
                    // Using WorshipTogether
                    fixWTContent(result);
                    setFileNameAndFolder();

                } else if (result != null && (result.contains("<div class=\"tb_ct\">") || (result.contains("ultimate-guitar")) && result.contains("data-action="))) {
                    // Using UG
                    fixUGContent(result);
                    setFileNameAndFolder();

                } else if (result !=null && result.contains("UkuTabs")) {
                    // Using UkuTabs.com
                    fixUkutabsContent(result);
                    setFileNameAndFolder();

                } else if (result!=null && result.contains("CCLI")) {
                    // Using SongSelect chord page
                    webresults_WebView.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

                } else if (result!=null && result.contains("<div id=\"LyricsText\"")) {
                    // Using SongSelect USR page
                    webresults_WebView.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

                } else if (result!=null && result.contains("holychords.com")) {
                    // Using Holychords page
                    fixHolyChordsContent(result);
                    setFileNameAndFolder();

                } else {
                    StaticVariables.myToastMessage = getText(R.string.chordpro_false).toString();
                    ShowToast.showToast(getContext());
                    grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
                }
            } catch (Exception | OutOfMemoryError e) {
                e.printStackTrace();
                if (getContext()!=null) {
                    StaticVariables.myToastMessage = getContext().getResources().getText(R.string.chordpro_false).toString();
                    ShowToast.showToast(getContext());
                    grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetFolders extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            try {
                newtempfolders = songFolders.prepareSongFolders(getContext(),preferences);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String s) {
            try {
                // The song folder
                ArrayAdapter<String> folders = new ArrayAdapter<>(requireContext(), R.layout.my_spinner, newtempfolders);
                folders.setDropDownViewResource(R.layout.my_spinner);
                choosefolder_Spinner.setAdapter(folders);

                // Select the current folder as the preferred one - i.e. rename into the same folder
                choosefolder_Spinner.setSelection(0);
                for (int w = 0; w < newtempfolders.size(); w++) {
                    if (FullscreenActivity.currentFolder.equals(newtempfolders.get(w)) ||
                            FullscreenActivity.currentFolder.equals("(" + newtempfolders.get(w) + ")")) {
                        choosefolder_Spinner.setSelection(w);
                        FullscreenActivity.newFolder = newtempfolders.get(w);
                    }
                }
            } catch (Exception e) {
                // Oops error
            }
        }
    }

    private class MyJavaScriptInterface {
        @JavascriptInterface
        public void processHTML(final String html) {
            GetSourceCode getsource = new GetSourceCode(html);
            getsource.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetSourceCode extends AsyncTask<Object, String, String> {

        final String html;
        GetSourceCode(String s) {
            html = s;
        }

        @Override
        protected String doInBackground(Object... objects) {
            if (html.contains("<div id=\"LyricsText\"")) {
                filecontents = extractSongSelectUsr(html, FullscreenActivity.phrasetosearchfor);
            } else {
                filecontents = extractSongSelectChordPro(html, FullscreenActivity.phrasetosearchfor);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (getContext()!=null) {
                grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
                if (filecontents == null || filecontents.equals("")) {
                    if (downloadcomplete) {
                        StaticVariables.myToastMessage = getContext().getString(R.string.pdfonly);
                    } else {
                        StaticVariables.myToastMessage = getContext().getResources().getText(R.string.chordpro_false).toString();
                    }
                    ShowToast.showToast(getContext());
                }
                // IV - Make sure of save
                if (newfileinfo_LinearLayout.getVisibility()!=View.VISIBLE) {
                    setFileNameAndFolder();
                }
            }
        }
    }
}