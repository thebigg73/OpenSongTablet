package com.garethevans.church.opensongtablet.abcnotation;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsAbcnotationBinding;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

public class MusicScoreFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private Preferences preferences;
    private ABCNotation abcNotation;
    private Song song;
    private ProcessSong processSong;
    private StorageAccess storageAccess;
    private SQLiteHelper sqLiteHelper;
    private CommonSQL commonSQL;
    private NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper;
    private SettingsAbcnotationBinding myView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsAbcnotationBinding.inflate(inflater, container, false);
        mainActivityInterface.updateToolbar(null,getString(R.string.music_score));

        // Set up the helpers
        setHelpers();

        // Set up the views
        setViews();

        // Set up the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setHelpers() {
        preferences = mainActivityInterface.getPreferences();
        abcNotation = new ABCNotation();
        song = mainActivityInterface.getSong();
        processSong = mainActivityInterface.getProcessSong();
        storageAccess = mainActivityInterface.getStorageAccess();
        sqLiteHelper = mainActivityInterface.getSQLiteHelper();
        nonOpenSongSQLiteHelper = mainActivityInterface.getNonOpenSongSQLiteHelper();
        commonSQL = mainActivityInterface.getCommonSQL();
    }

    private void setViews() {
        abcNotation.setWebView(myView.abcWebView,song,mainActivityInterface.getWhattodo().equals("editabc"));
        myView.abcWebView.post(() -> myView.abcWebView.addJavascriptInterface(new JsInterface(), "AndroidApp"));
    }

    private class JsInterface {
        @JavascriptInterface
        public void receiveString(String myJsString) {
            // String received from WebView
            if (!myJsString.equals(abcNotation.getSongInfo(song))) {
                // Something has changed
                song.setAbc(myJsString);
                String songXML = processSong.getXML(song);

                // Write the updated song file
                storageAccess.doStringWriteToFile(requireContext(), preferences, "Songs",
                        song.getFolder(), song.getFilename(), songXML);

                // Update the database
                if (song.getIsSong()) {
                    sqLiteHelper.updateSong(requireContext(), commonSQL, song);
                } else {
                    nonOpenSongSQLiteHelper.updateSong(requireContext(), commonSQL, storageAccess,
                            preferences, song);
                }
                vieworedit(false);
            }
        }
    }

    private void setListeners() {
        myView.editABC.setOnClickListener(v -> vieworedit(true));
    }

    private void vieworedit(boolean edit) {
        if (edit) {
            mainActivityInterface.setWhattodo("editabc");
            myView.editABC.setImageDrawable(ResourcesCompat.getDrawable(requireContext().getResources(),R.drawable.ic_content_save_white_36dp,null));
            myView.editABC.setOnClickListener(v -> doSave());
            setViews();
        } else {
            mainActivityInterface.setWhattodo("viewabc");
            myView.editABC.setImageDrawable(ResourcesCompat.getDrawable(requireContext().getResources(),R.drawable.ic_pencil_white_36dp,null));
            myView.editABC.setOnClickListener(v -> {
                vieworedit(true);
            });
        }
        setViews();
    }

    private void doSave() {
        // Try to get the text by activating received string
        mainActivityInterface.setWhattodo("viewabc");
        myView.abcWebView.loadUrl("javascript:getTextVal()");
        vieworedit(false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivityInterface.setWhattodo("");
    }
}
