package com.garethevans.church.opensongtablet.abcnotation;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
        mainActivityInterface.updateToolbar(getString(R.string.music_score));

        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // Set up the views
        setViews();

        // Set up the listeners
        setListeners();

        return myView.getRoot();
    }


    private void setViews() {
        mainActivityInterface.getAbcNotation().setWebView(myView.abcWebView,mainActivityInterface,
                true);
        myView.abcWebView.post(() -> myView.abcWebView.addJavascriptInterface(new JsInterface(), "AndroidApp"));
    }

    private void setListeners() {
        myView.editABC.setOnClickListener(v -> doSave());
        myView.nestedScrollView.setExtendedFabToAnimate(myView.editABC);
    }

    private class JsInterface {
        @JavascriptInterface
        public void receiveString(String myJsString) {
            // String received from WebView
            if (!myJsString.equals(mainActivityInterface.getAbcNotation().getSongInfo(mainActivityInterface))) {
                // Something has changed
                mainActivityInterface.getSong().setAbc(myJsString);
                mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong());
            }
        }
    }

    private void doSave() {
        // Try to get the text by activating received string
        mainActivityInterface.setWhattodo("viewabc");
        myView.abcWebView.loadUrl("javascript:getTextVal()");
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
