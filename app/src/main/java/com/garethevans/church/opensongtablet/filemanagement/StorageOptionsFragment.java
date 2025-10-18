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
import com.garethevans.church.opensongtablet.preferences.AreYouSureBottomSheet;

public class StorageOptionsFragment extends Fragment {

    @SuppressWarnings({"unused","fieldCanBeLocal"})
    private final String TAG = "StorageOptionsFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsStorageOptionsBinding myView;
    private String storage_string="";
    private String deeplink_manage_storage, deeplink_openchords, deeplink_sync,warning_string="",
            index_songs_wait_string="";

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

        myView.getRoot().setBackgroundColor(mainActivityInterface.getPalette().background);
        
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
            warning_string = getString(R.string.synchronise_need_update_warning);
            index_songs_wait_string = getString(R.string.index_songs_wait);
        }
    }

    private void setupViews() {
        // If we aren't at least using nearby connections, hide the sync menu item
        if (!mainActivityInterface.getNearbyActions().getNearbyConnectionManagement().getUsingNearby() ||
                mainActivityInterface.getNearbyActions().getNearbyConnectionManagement().getConnectedDevices().isEmpty()) {
            myView.syncWithConnectedLayout.setVisibility(View.GONE);
        } else {
            myView.syncWithConnectedLayout.setVisibility(View.VISIBLE);
        }
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("needToSaveAgain",false)) {
            myView.updateSongsLayout.setVisibility(View.VISIBLE);
        } else {
            myView.updateSongsLayout.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        myView.storageManage.setOnClickListener(view -> mainActivityInterface.navigateToFragment(deeplink_manage_storage,R.id.storageManagementFragment));
        myView.openChords.setOnClickListener(view -> mainActivityInterface.navigateToFragment(deeplink_openchords,R.id.openChordsFragment));
        myView.syncWithConnected.setOnClickListener(v -> {
            if (mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
                mainActivityInterface.getShowToast().doIt(index_songs_wait_string);
            } else {
                // We must already have the required permissions
                mainActivityInterface.setWhattodo("songs");
                mainActivityInterface.navigateToFragment(deeplink_sync, R.id.syncNearbyFragment);
            }
        });
        myView.updateSongs.setOnClickListener(view -> {
            if (mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
                mainActivityInterface.getShowToast().doIt(index_songs_wait_string);
            } else {
                // Show a confirmation
                AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet("addUUIDLastMod", warning_string, null, "StorageOptionsFragment", this, null);
                areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "AreYouSureBottomSheet");
            }
        });
    }
}
