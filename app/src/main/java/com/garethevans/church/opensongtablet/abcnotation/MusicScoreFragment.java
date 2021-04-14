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
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class MusicScoreFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
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

        // Set up the views
        setViews();

        // Set up the listeners
        setListeners();

        return myView.getRoot();
    }


    private void setViews() {
        mainActivityInterface.getAbcNotation().setWebView(myView.abcWebView,mainActivityInterface,
                mainActivityInterface.getWhattodo().equals("editabc"));
        myView.abcWebView.post(() -> myView.abcWebView.addJavascriptInterface(new JsInterface(), "AndroidApp"));
    }

    private class JsInterface {
        @JavascriptInterface
        public void receiveString(String myJsString) {
            // String received from WebView
            if (!myJsString.equals(mainActivityInterface.getAbcNotation().getSongInfo(mainActivityInterface))) {
                // Something has changed
                mainActivityInterface.getSong().setAbc(myJsString);
                String songXML = mainActivityInterface.getProcessSong().getXML(mainActivityInterface.getSong());

                // Write the updated song file
                mainActivityInterface.getStorageAccess().doStringWriteToFile(requireContext(), mainActivityInterface.getPreferences(), "Songs",
                        mainActivityInterface.getSong().getFolder(), mainActivityInterface.getSong().getFilename(), songXML);

                // Update the database
                if (mainActivityInterface.getSong().getIsSong()) {
                    mainActivityInterface.getSQLiteHelper().updateSong(requireContext(), mainActivityInterface.getCommonSQL(), mainActivityInterface.getSong());
                } else {
                    mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(requireContext(),
                            mainActivityInterface.getCommonSQL(), mainActivityInterface.getStorageAccess(),
                            mainActivityInterface.getPreferences(), mainActivityInterface.getSong());
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
            myView.editABC.setOnClickListener(v -> vieworedit(true));
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
