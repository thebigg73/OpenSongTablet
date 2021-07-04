package com.garethevans.church.opensongtablet.setprocessing;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsSetsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class SetActionsFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SettingsSetsBinding myView = SettingsSetsBinding.inflate(inflater, container, false);

        myView.createSet.setOnClickListener(v -> mainActivityInterface.displayAreYouSure("newSet",getString(R.string.set_new),null,"SetActionsFragment",this,null));
        myView.loadSet.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("loadset");
            mainActivityInterface.navigateToFragment(null,R.id.setManageFragment);
        });
        myView.saveSet.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("saveset");
            mainActivityInterface.navigateToFragment(null,R.id.setManageFragment);
        });
        return myView.getRoot();
    }
}
