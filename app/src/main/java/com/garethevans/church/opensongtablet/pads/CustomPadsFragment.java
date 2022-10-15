package com.garethevans.church.opensongtablet.pads;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MaterialEditText;
import com.garethevans.church.opensongtablet.databinding.SettingsPadsCustomBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class CustomPadsFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsPadsCustomBinding myView;
    private ActivityResultLauncher<String> activityResultLauncher;
    private final String TAG = "CustomPadsFragment";
    private MaterialEditText materialEditText;
    private String prefName, prefValue;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsPadsCustomBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(getString(R.string.pad) + " (" + getString(R.string.custom) + ")");
        mainActivityInterface.updateToolbarHelp(getString(R.string.website_pad));

        // Set up the file launcher listener
        setupLauncher();

        // Set up the user preferences and listeners
        setupPadOptions();

        return myView.getRoot();
    }

    private void setupLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    // Handle the returned Uri
                    Log.d(TAG,"uri="+uri);
                    String text;
                    if (uri!=null) {
                        prefValue = mainActivityInterface.getStorageAccess().fixUriToLocal(uri);
                        text = prefValue;
                    } else {
                        prefValue = "";
                        text = getString(R.string.pad_auto);
                    }
                    mainActivityInterface.getPreferences().setMyPreferenceString(prefName, prefValue);
                    materialEditText.setText(text);
                });
    }

    private void setupPadOptions() {
        setPref(myView.padAb,"customPadAb");
        setPref(myView.padA,"customPadA");
        setPref(myView.padBb,"customPadBb");
        setPref(myView.padB,"customPadB");
        setPref(myView.padC,"customPadC");
        setPref(myView.padDb,"customPadDb");
        setPref(myView.padD,"customPadD");
        setPref(myView.padEb,"customPadEb");
        setPref(myView.padE,"customPadE");
        setPref(myView.padF,"customPadF");
        setPref(myView.padGb,"customPadGb");
        setPref(myView.padG,"customPadG");
        setPref(myView.padAbm,"customPadAbm");
        setPref(myView.padAm,"customPadAm");
        setPref(myView.padBbm,"customPadBbm");
        setPref(myView.padBm,"customPadBm");
        setPref(myView.padCm,"customPadCm");
        setPref(myView.padDbm,"customPadDbm");
        setPref(myView.padDm,"customPadDm");
        setPref(myView.padEbm,"customPadEbm");
        setPref(myView.padEm,"customPadEm");
        setPref(myView.padFm,"customPadFm");
        setPref(myView.padGbm,"customPadGbm");
        setPref(myView.padGm,"customPadGm");
    }

    private void setPref(MaterialEditText materialEditText, String prefName) {
        String pref = mainActivityInterface.getPreferences().getMyPreferenceString(prefName,"");
        if (pref==null || pref.isEmpty() || pref.equals("auto")) {
            pref = getString(R.string.pad_auto);
        }
        materialEditText.setText(pref);
        materialEditText.setFocusable(false);
        final String prefVal = pref;
        materialEditText.setOnClickListener(view -> selectFile(materialEditText,prefName,prefVal));
    }

    private void selectFile(MaterialEditText materialEditText, String prefName, String prefValue) {
        // Set the value to auto.  If the user cancel, this becomes the new value
        this.materialEditText = materialEditText;
        this.prefName = prefName;
        this.prefValue = prefValue;
        this.materialEditText.setText(getString(R.string.pad_auto));
        activityResultLauncher.launch("audio/*");
    }
}
