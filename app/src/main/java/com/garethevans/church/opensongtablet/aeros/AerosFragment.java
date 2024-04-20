package com.garethevans.church.opensongtablet.aeros;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsAerosCommandsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class AerosFragment extends Fragment {

    // This allows us to set basic Aeros settings and add to song commands
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "AerosFragment";
    private SettingsAerosCommandsBinding myView;
    private MainActivityInterface mainActivityInterface;
    private String aeros_string="", web_address_string="", settings_string="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings();
        mainActivityInterface.updateToolbar(aeros_string + ": "+settings_string);
        mainActivityInterface.updateToolbarHelp(web_address_string);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsAerosCommandsBinding.inflate(inflater,container,false);

        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            prepareStrings();
            setupViews();
            setupListeners();
        });

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            aeros_string = getString(R.string.aeros);
            settings_string = getString(R.string.settings);
            web_address_string = getString(R.string.website_aeros);
        }
    }

    private void setupViews() {
        myView.aerosChannel.setValue(mainActivityInterface.getAeros().getAerosChannel());
        myView.aerosChannel.setLabelFormatter(value -> String.valueOf((int)value));
        myView.aerosChannel.setAdjustableButtons(true);
        myView.aerosChannel.setHint(String.valueOf(mainActivityInterface.getAeros().getAerosChannel()));
        myView.midiDelay.setValue(mainActivityInterface.getMidi().getMidiDelay());
        myView.midiDelay.setLabelFormatter(value -> (int)value + " ms");
        myView.midiDelay.setAdjustableButtons(true);
        myView.midiDelay.setHint(mainActivityInterface.getMidi().getMidiDelay()+" ms");
    }

    private void setupListeners() {
        myView.aerosChannel.addOnChangeListener((slider, value, fromUser) -> {
            mainActivityInterface.getAeros().setAerosChannel((int)value);
            myView.aerosChannel.setHint(String.valueOf((int)value));
        });
        myView.midiDelay.addOnChangeListener((slider, value, fromUser) -> {
            mainActivityInterface.getMidi().setMidiDelay((int)value);
            myView.midiDelay.setHint((int)value+" ms");
        });
    }
}
