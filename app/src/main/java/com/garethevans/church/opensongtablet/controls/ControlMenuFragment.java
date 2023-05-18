package com.garethevans.church.opensongtablet.controls;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsControlBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class ControlMenuFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ControlMenuFrag";

    private SettingsControlBinding myView;
    private MainActivityInterface mainActivityInterface;
    private String controls_string="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(controls_string);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsControlBinding.inflate(inflater,container,false);

        prepareStrings();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            controls_string = getString(R.string.controls);
        }
    }
    private void setListeners() {
        myView.pageButtons.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.pageButtonFragment));
        myView.pedals.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.pedalsFragment));
        myView.customGestures.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.gesturesFragment));
        myView.swipe.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.swipeFragment));
        myView.hotZones.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.hotZonesSettingsFragment));
    }
}
