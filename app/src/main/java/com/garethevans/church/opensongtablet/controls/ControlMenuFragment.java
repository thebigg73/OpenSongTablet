package com.garethevans.church.opensongtablet.controls;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.MainActivity;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsControlBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class ControlMenuFragment extends Fragment {

    private SettingsControlBinding myView;
    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsControlBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(null,getString(R.string.controls));

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setListeners() {
        myView.pedals.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.pedalsFragment));
        myView.pageButtons.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.pageButtonFragment));
        myView.customGestures.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.gesturesFragment));
        myView.swipe.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.swipeFragment));
    }
}
