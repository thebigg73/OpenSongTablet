package com.garethevans.church.opensongtablet.abcnotation;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsAbcnotationBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

public class MusicScoreFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsAbcnotationBinding myView;
    private final String TAG = "MusicScoreFragment";

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

        // Check if we have song and abc keys set and adjust the options accordingly
        checkSongAndABCKey();

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
        myView.sizeSlider.setValue((int)(100*mainActivityInterface.getPreferences().getMyPreferenceFloat("abcPopupWidth",0.95f)));
        myView.sizeSlider.setLabelFormatter(value -> ((int)value)+"%");
        myView.sizeSlider.setHint((int)myView.sizeSlider.getValue()+"%");
        myView.zoomSlider.setValue(mainActivityInterface.getPreferences().getMyPreferenceInt("abcZoom",2));
        myView.zoomSlider.setHint((int)myView.zoomSlider.getValue()+"");
        myView.zoomSlider.setLabelFormatter(value -> (int)value+"");
        myView.transposeSlider.setValue(getTransposeValue());
        myView.transposeSlider.setHint((int)myView.transposeSlider.getValue()+"");
        myView.transposeSlider.setLabelFormatter(value -> (int)value+"");
    }

    private void checkSongAndABCKey() {
        if (!mainActivityInterface.getSong().getKey().isEmpty() && mainActivityInterface.getSong().getAbc().contains("K:")) {
            myView.autoTranspose.setVisibility(View.VISIBLE);
            myView.autoTranspose.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("abcAutoTranspose",true));
            Log.d(TAG,"both song and abc key set");
        } else {
            myView.autoTranspose.setVisibility(View.GONE);
            myView.autoTranspose.setChecked(false);
            Log.d(TAG,"one of the keys isn't set");
        }
    }

    private int getTransposeValue() {
        int transposeVal = 0;
        if (myView.autoTranspose.getChecked()) {
            myView.transposeSlider.setEnabled(false);
            transposeVal = mainActivityInterface.getAbcNotation().getABCTransposeFromSongKey(mainActivityInterface);
        } else if (!mainActivityInterface.getSong().getAbcTranspose().isEmpty()) {
            myView.transposeSlider.setEnabled(true);
            transposeVal = Integer.parseInt(mainActivityInterface.getSong().getAbcTranspose());
        } else {
            myView.transposeSlider.setEnabled(true);
        }
        Log.d(TAG,"transposeVal:"+transposeVal);
        return transposeVal;
    }

    private void setListeners() {
        myView.editABC.setOnClickListener(v -> doSave());
        myView.nestedScrollView.setExtendedFabToAnimate(myView.editABC);
        myView.autoTranspose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainActivityInterface.getPreferences().setMyPreferenceBoolean("abcTransposeAuto",isChecked);
                if (isChecked) {
                    int transpVal = getTransposeValue();
                    myView.transposeSlider.setValue(transpVal);
                    myView.transposeSlider.setHint(""+transpVal);
                }
                myView.transposeSlider.setEnabled(!isChecked);
                mainActivityInterface.getAbcNotation().updateTranspose(myView.abcWebView,(int)myView.transposeSlider.getValue());
            }
        });
        myView.sizeSlider.addOnChangeListener((slider, value, fromUser) -> myView.sizeSlider.setHint((int)value+"%"));
        myView.zoomSlider.addOnChangeListener((slider, value, fromUser) -> myView.zoomSlider.setHint((int)value+""));
        myView.transposeSlider.addOnChangeListener((slider, value, fromUser) -> myView.transposeSlider.setHint((int)value+""));
        myView.sizeSlider.addOnSliderTouchListener(new MySliderTouchListener("abcPopupWidth"));
        myView.zoomSlider.addOnSliderTouchListener(new MySliderTouchListener("abcZoom"));
        myView.transposeSlider.addOnSliderTouchListener(new MySliderTouchListener("abcTranspose"));
    }

    private class MySliderTouchListener implements Slider.OnSliderTouchListener {
        private final String prefName;
        MySliderTouchListener(String prefName) {
            this.prefName = prefName;
        }
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {}

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            switch (prefName) {
                case "abcPopupWidth":
                    mainActivityInterface.getPreferences().setMyPreferenceFloat("abcPopupWidth",myView.sizeSlider.getValue()/100f);
                    break;
                case "abcZoom":
                    mainActivityInterface.getPreferences().setMyPreferenceInt("abcZoom",(int)myView.zoomSlider.getValue());
                    mainActivityInterface.getAbcNotation().updateZoom(myView.abcWebView,(int)myView.zoomSlider.getValue());
                    break;
                case "abcTranspose":
                    // This isn't a preference, but a song specific value
                    mainActivityInterface.getSong().setAbcTranspose((int)myView.transposeSlider.getValue()+"");
                    mainActivityInterface.getAbcNotation().updateTranspose(myView.abcWebView,(int)myView.transposeSlider.getValue());
                    break;
            }
        }
    }

    private class JsInterface {
        @JavascriptInterface
        public void receiveString(String myJsString) {
            Log.d(TAG,"string: "+myJsString);
            // String received from WebView
            if (!myJsString.equals(mainActivityInterface.getAbcNotation().getSongInfo(mainActivityInterface))) {
                // Something has changed
                mainActivityInterface.getSong().setAbc(myJsString);
                if (mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong())) {
                    mainActivityInterface.getShowToast().doIt(getString(R.string.success));
                } else {
                    mainActivityInterface.getShowToast().doIt(getString(R.string.error));
                }

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
