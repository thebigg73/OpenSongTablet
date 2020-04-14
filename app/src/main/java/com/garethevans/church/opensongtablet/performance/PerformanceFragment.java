package com.garethevans.church.opensongtablet.performance;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavHost;
import androidx.navigation.NavHostController;

import com.garethevans.church.opensongtablet.Preferences;
import com.garethevans.church.opensongtablet.SQLiteHelper;
import com.garethevans.church.opensongtablet.StaticVariables;
import com.garethevans.church.opensongtablet.StorageAccess;
import com.garethevans.church.opensongtablet.databinding.FragmentPerformanceBinding;
import com.garethevans.church.opensongtablet.screensetup.AppActionBar;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ConvertOnSong;
import com.garethevans.church.opensongtablet.songprocessing.LoadSong;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.SongXML;

public class PerformanceFragment extends Fragment {

    // Helper classes for the heavy lifting
    private StorageAccess storageAccess;
    private Preferences preferences;
    private ProcessSong processSong;
    private LoadSong loadSong;
    private SongXML songXML;
    private SQLiteHelper sqLiteHelper;
    private ConvertChoPro convertChoPro;
    private ConvertOnSong convertOnSong;
    private AppActionBar appActionBar;

    public interface MyInterface {
        void updateToolbar();
    }

    private MyInterface mListener;

    WebView webView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (MyInterface) getActivity();
    }

    private FragmentPerformanceBinding myView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        StaticVariables.homeFragment = true;  // Set to true for Performance/Stage/Presentation only
        myView = FragmentPerformanceBinding.inflate(inflater, container, false);
        View root = myView.getRoot();

        webView = myView.webView;
        webView.setInitialScale(100);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);

        String s = StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename;
        myView.textView.setText(s);

        // Initialise the helper classes that do the heavy lifting
        initialiseHelpers();

        doSongLoad();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }

    private void initialiseHelpers() {
        storageAccess = new StorageAccess();
        preferences = new Preferences();
        loadSong = new LoadSong();
        processSong = new ProcessSong();
        songXML = new SongXML();
        sqLiteHelper = new SQLiteHelper(getActivity());
        convertOnSong = new ConvertOnSong();
        convertChoPro = new ConvertChoPro();
        appActionBar = new AppActionBar();
    }

    private void doSongLoad() {
        // Load up the song
        loadSong.doLoadSong(getActivity(),storageAccess,preferences,songXML,processSong,sqLiteHelper,
                convertOnSong, convertChoPro);

        if (mListener!=null) {
            mListener.updateToolbar();
        }

        // Get the HTML text.
        String encodedHtml = processSong.songHTML(getActivity(), storageAccess, preferences, 0xffffffff, 0xff000000, 0xff0000ff);
        WebView webView = myView.webView;
        webView.loadData(encodedHtml, "text/html", "base64");
        webView.setBackgroundColor(0xffffff);


        myView.textView.setText(StaticVariables.mLyrics);
    }

    public void onBackPressed() {
        Log.d("PerformanceFragment","On back press!!!");
    }
}
