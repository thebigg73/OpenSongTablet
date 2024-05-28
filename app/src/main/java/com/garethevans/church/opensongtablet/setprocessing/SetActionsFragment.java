package com.garethevans.church.opensongtablet.setprocessing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsSetsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class SetActionsFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private SettingsSetsBinding myView;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SetActionsFragment";
    private String set_manage_string="", set_new_string="", deeplink_sets_manage_string="",
            file_type_string="", unknown_string="", deeplink_browse_host_files_set="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(set_manage_string);
        prepareStrings();
        setupViews();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsSetsBinding.inflate(inflater, container, false);

        prepareStrings();
        initialiseLauncher();
        setupViews();
        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            set_manage_string = getString(R.string.set_manage);
            set_new_string = getString(R.string.set_new);
            deeplink_sets_manage_string = getString(R.string.deeplink_sets_manage);
            deeplink_browse_host_files_set = getString(R.string.deeplink_browse_host_files_set);
            file_type_string = getString(R.string.file_type);
            unknown_string = getString(R.string.unknown);
        }
    }

    private void initialiseLauncher() {
        // Initialise the launcher
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                try {
                    Intent data = result.getData();

                    if (data != null) {
                        Uri contentUri = data.getData();
                        String importFilename = mainActivityInterface.getStorageAccess().getFileNameFromUri(contentUri);
                        if (importFilename.endsWith(".osts") || !importFilename.contains(".") || !importFilename.endsWith(".xml")) {
                            // Keep a record of the uri
                            mainActivityInterface.setImportUri(contentUri);
                            mainActivityInterface.setImportFilename(importFilename);

                            // Now go the to manage set fragment
                            mainActivityInterface.setWhattodo("importset");
                            mainActivityInterface.navigateToFragment(deeplink_sets_manage_string, 0);
                        } else {
                            mainActivityInterface.getShowToast().doIt(file_type_string+" "+unknown_string);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupViews() {
        if (getContext()!=null && mainActivityInterface!=null && myView!=null) {
            myView.browseHostLayout.setVisibility((!mainActivityInterface.getNearbyConnections().getIsHost() &&
                    mainActivityInterface.getNearbyConnections().getUsingNearby()) ? View.VISIBLE:View.GONE);
        }
    }

    private void setupListeners() {
        if (getContext()!=null && myView!=null && mainActivityInterface!=null) {
            myView.createSet.setOnClickListener(v -> mainActivityInterface.displayAreYouSure("newSet", set_new_string, null, "SetActionsFragment", this, null));
            myView.loadSet.setOnClickListener(v -> {
                mainActivityInterface.setWhattodo("loadset");
                mainActivityInterface.navigateToFragment(null, R.id.setManageFragment);
            });
            myView.saveSet.setOnClickListener(v -> {
                mainActivityInterface.setWhattodo("saveset");
                mainActivityInterface.navigateToFragment(null, R.id.setManageFragment);
            });
            myView.renameSet.setOnClickListener(v -> {
                mainActivityInterface.setWhattodo("renameset");
                mainActivityInterface.navigateToFragment(null, R.id.setManageFragment);
            });
            myView.deleteSet.setOnClickListener(v -> {
                mainActivityInterface.setWhattodo("deleteset");
                mainActivityInterface.navigateToFragment(null, R.id.setManageFragment);
            });
            myView.bibleButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null, R.id.bible_graph));
            myView.slideButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null, R.id.customSlideFragment));
            myView.exportSet.setOnClickListener(v -> {
                mainActivityInterface.setWhattodo("exportset");
                mainActivityInterface.navigateToFragment(null, R.id.setManageFragment);
            });
            myView.backupSets.setOnClickListener(v -> {
                mainActivityInterface.setWhattodo("backupsets");
                mainActivityInterface.navigateToFragment(null, R.id.backupRestoreSetsFragment);
            });
            myView.restoreSets.setOnClickListener(v -> {
                mainActivityInterface.setWhattodo("restoresets");
                mainActivityInterface.navigateToFragment(null, R.id.backupRestoreSetsFragment);
            });
            myView.importSet.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                String[] mimetypes = {"text/xml", "application/octet-stream"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                intent.addFlags(mainActivityInterface.getStorageAccess().getAddReadUriFlags());
                activityResultLauncher.launch(intent);
            });
            myView.browseHost.setOnClickListener(v -> {
                mainActivityInterface.setWhattodo("browsesets");
                mainActivityInterface.navigateToFragment(deeplink_browse_host_files_set,0);
            });
        }
    }
}
