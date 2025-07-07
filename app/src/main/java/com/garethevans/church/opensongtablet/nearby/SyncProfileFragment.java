package com.garethevans.church.opensongtablet.nearby;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.databinding.SettingsSyncBinding;

public class SyncProfileFragment extends Fragment {

    private SyncNearbyFragment syncNearbyFragment;
    private SettingsSyncBinding myView;

    public void setMainFragment(SyncNearbyFragment syncNearbyFragment) {
        this.syncNearbyFragment = syncNearbyFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings();
        setupViews();
        setupListeners();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = SettingsSyncBinding.inflate(inflater,container,false);
        return myView.getRoot();
    }

    private void prepareStrings() {

    }
    private void setupViews() {

    }
    private void setupListeners() {

    }
}
