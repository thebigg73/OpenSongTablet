package com.garethevans.church.opensongtablet.pads;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsPadsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class PadSettingsFragment extends Fragment {

    private SettingsPadsBinding myView;
    private MainActivityInterface mainActivityInterface;
    private boolean padPlaying;
    private String pad_string="", website_pad_string="";
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(pad_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsPadsBinding.inflate(inflater, container, false);

        prepareStrings();

        webAddress = website_pad_string;

        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            pad_string = getString(R.string.pad);
            website_pad_string = getString(R.string.website_pad);
        }
    }
    private void setListeners() {
        myView.padCurrent.setOnClickListener(v -> {
            // Nav home then open the pad bottom sheet
            mainActivityInterface.navHome();
            if (getActivity()!=null) {
                PadsBottomSheet padsBottomSheet = new PadsBottomSheet();
                padsBottomSheet.show(getActivity().getSupportFragmentManager(), "padsBottomSheet");
            }
        });
        myView.padCustom.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null, R.id.customPadsFragment));
        myView.padSettings.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null, R.id.padDefaultsFragment));
        padPlaying = mainActivityInterface.getPad().isPadPlaying();
        changePlayIcon();

        myView.nestedScrollView.setFabToAnimate(myView.startStopButton);
        
        myView.startStopButton.setOnClickListener(v -> {
            padPlaying = mainActivityInterface.playPad();
            changePlayIcon();
        });
    }

    private void changePlayIcon() {
        if (getContext()!=null) {
            if (padPlaying) {
                myView.startStopButton.setImageDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.stop, null));
            } else {
                myView.startStopButton.setImageDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.play, null));
            }
        }
    }
}
