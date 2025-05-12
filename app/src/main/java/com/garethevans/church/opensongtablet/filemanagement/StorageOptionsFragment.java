package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsStorageOptionsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class StorageOptionsFragment extends Fragment {

    private final String TAG = "StorageOptionsFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsStorageOptionsBinding myView;
    private String storage_string="";
    private String deeplink_manage_storage, deeplink_openchords, deeplink_sync;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings();
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsStorageOptionsBinding.inflate(inflater, container, false);

        prepareStrings();
        mainActivityInterface.updateToolbar(storage_string);

        setupViews();

        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            storage_string = getString(R.string.storage);
            deeplink_openchords = getString(R.string.deeplink_openchords);
            deeplink_manage_storage = getString(R.string.deeplink_manage_storage);
            deeplink_sync = getString(R.string.deeplink_sync);
        }
    }

    private void setupViews() {
        // TODO Still to implement this
        myView.syncWithConnectedLayout.setVisibility(View.GONE);
        // If we aren't at least using nearby connections, hide the sync menu item
        if (!mainActivityInterface.getNearbyConnections().getUsingNearby()) {
            myView.syncWithConnectedLayout.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        myView.storageManage.setOnClickListener(view -> mainActivityInterface.navigateToFragment(deeplink_manage_storage,R.id.storageManagementFragment));
        myView.openChords.setOnClickListener(view -> mainActivityInterface.navigateToFragment(deeplink_openchords,R.id.openChordsFragment));
        myView.syncWithConnected.setOnClickListener(v -> {
            // We must already have the required permissions
            mainActivityInterface.setWhattodo("songs");
            mainActivityInterface.navigateToFragment(deeplink_sync, R.id.syncNearbyFragment);
        });
    }
}
