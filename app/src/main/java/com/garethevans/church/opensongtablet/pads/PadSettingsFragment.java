package com.garethevans.church.opensongtablet.pads;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
    private final String TAG = "PadSettingsFragment";
    private boolean padPlaying;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsPadsBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(getString(R.string.pad));
        setListeners();

        return myView.getRoot();
    }

    private void setListeners() {
        myView.padCurrent.setOnClickListener(v -> {
            // Nav home then open the pad bottom sheet
            mainActivityInterface.navHome();
            PadsBottomSheet padsBottomSheet = new PadsBottomSheet();
            padsBottomSheet.show(requireActivity().getSupportFragmentManager(),"padsBottomSheet");
        });
        myView.padCustom.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null, R.id.customPadsFragment));
        myView.padSettings.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null, R.id.padDefaultsFragment));
        padPlaying = mainActivityInterface.getPad().isPadPlaying();
        changePlayIcon();
        myView.startStopButton.setOnClickListener(v -> {
            padPlaying = mainActivityInterface.playPad();
            changePlayIcon();
        });
    }

    private void changePlayIcon() {
        if (padPlaying) {
            Log.d(TAG,"Playing");
            myView.startStopButton.setImageDrawable(ResourcesCompat.getDrawable(requireContext().getResources(),R.drawable.ic_stop_white_36dp,null));
        } else {
            Log.d(TAG,"Not playing");
            myView.startStopButton.setImageDrawable(ResourcesCompat.getDrawable(requireContext().getResources(),R.drawable.ic_play_white_36dp,null));
        }
    }
}
