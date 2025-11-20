package com.garethevans.church.opensongtablet.setprocessing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
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
    private String set_manage_string="", set_new_string="";

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsSetsBinding.inflate(inflater, container, false);

        prepareStrings();
        initialiseLauncher();
        setupListeners();

        if (mainActivityInterface.getWhattodo()!=null &&
                mainActivityInterface.getWhattodo().equals("importset")) {
            mainActivityInterface.setWhattodo("");
            myView.importSet.performClick();
        }
        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            set_manage_string = getString(R.string.set_manage);
            set_new_string = getString(R.string.set_new);
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
                        mainActivityInterface.setImportUri(contentUri);
                        mainActivityInterface.setImportFilename(mainActivityInterface.getStorageAccess().getFileNameFromUri(contentUri));
                        mainActivityInterface.openFragmentBasedOnFileImport();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
                openFileIntent("Backups");
            });
            myView.importSet.setOnClickListener(v -> {
                mainActivityInterface.setWhattodo("importset");
                openFileIntent("Import");
            });
        }
    }

    private void openFileIntent(String where) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        String[] mimetypes = {"text/xml", "application/octet-stream","text/html"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,
                    mainActivityInterface.getStorageAccess().getUriForItem(where,"",""));
        }
        intent.addFlags(mainActivityInterface.getStorageAccess().getAddReadUriFlags());
        activityResultLauncher.launch(intent);
    }
}
