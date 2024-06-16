package com.garethevans.church.opensongtablet.preferences;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsProfilesBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class ProfileFragment extends Fragment {

    private SettingsProfilesBinding myView;
    private MainActivityInterface mainActivityInterface;
    private String profile_string="", website_profiles_string="",
            deeplink_browse_host_files="";
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(profile_string);
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
        myView = SettingsProfilesBinding.inflate(inflater,container,false);

        mainActivityInterface.setWhattodo("");

        prepareStrings();
        webAddress = website_profiles_string;

        // Setup helpers
        setupHelpers();

        // Set up views
        setupViews();

        // Setup listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        if (getContext()!=null && mainActivityInterface!=null && myView!=null) {
            myView.browseHostLayout.setVisibility((!mainActivityInterface.getNearbyConnections().getIsHost() &&
                    mainActivityInterface.getNearbyConnections().getConnectedEndpoints().size()>0 &&
                    mainActivityInterface.getNearbyConnections().getUsingNearby()) ? View.VISIBLE:View.GONE);
        }
    }
    private void prepareStrings() {
        if (getContext()!=null) {
            profile_string = getString(R.string.profile);
            website_profiles_string = getString(R.string.website_profiles);
            deeplink_browse_host_files = getString(R.string.deeplink_browse_host_files);
        }
    }

    private void setupHelpers() {
        mainActivityInterface.registerFragment(this,"ProfileFragment");
    }

    private void setupListeners() {
        myView.loadButton.setOnClickListener(v -> loadProfile());
        myView.saveButton.setOnClickListener(v -> saveProfile());
        myView.resetButton.setOnClickListener(v -> resetPreferences());
        myView.browseHost.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("browseprofiles");
            mainActivityInterface.navigateToFragment(deeplink_browse_host_files,0);
        });
    }

    private void loadProfile() {
        // Open the bottom sheet
        mainActivityInterface.setWhattodo("loadprofile");
        ProfileBottomSheet profileBottomSheet = new ProfileBottomSheet();
        profileBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"ProfileBottomSheet");
    }

    private void saveProfile() {
        // Open the bottom sheet
        // Used to use Intent.ACTION_CREATE_DOCUMENT but this wouldn't allow overwrite
        mainActivityInterface.setWhattodo("saveprofile");
        ProfileBottomSheet profileBottomSheet = new ProfileBottomSheet();
        profileBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"ProfileBottomSheet");
    }

    private void resetPreferences() {
        // Reset the preferences and start again
        if (getContext()!=null) {
            mainActivityInterface.getProfileActions().resetPreferences();
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.setStorageLocationFragment, true)
                    .build();
            NavHostFragment.findNavController(this)
                    .navigate(Uri.parse(getString(R.string.deeplink_set_storage)), navOptions);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainActivityInterface.registerFragment(null,"ProfileFragment");
    }
}
