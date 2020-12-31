package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsImportBinding;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

public class ImportOptionsFragment extends Fragment {

    // This class asks the user which type of file should be imported.

    StorageAccess storageAccess;
    MainActivityInterface mainActivityInterface;
    SettingsImportBinding myView;
    String[] validFiles = new String[] {"text/plain","image/*","text/xml","application/xml","application/pdf","application/octet-stream"};
    String[] validBackups = new String[] {"application/zip"};

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsImportBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(null,getString(R.string.import_file));

        // Set the helpers
        setHelpers();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setHelpers() {
        storageAccess = mainActivityInterface.getStorageAccess();
    }

    private void setListeners() {
        myView.importFile.setOnClickListener(v -> selectFile(StaticVariables.REQUEST_FILE_CHOOSER,validFiles));
        myView.importOSB.setOnClickListener(v -> selectFile(StaticVariables.REQUEST_OSB_FILE,validBackups));
        myView.importiOS.setOnClickListener(v -> selectFile(StaticVariables.REQUEST_OSB_FILE,validBackups));
        myView.importOnline.setOnClickListener(v -> onlineSearch());
        myView.importBand.setOnClickListener(v -> importSample("https://drive.google.com/uc?export=download&id=0B-GbNhnY_O_leDR5bFFjRVVxVjA"));
        myView.importChurch.setOnClickListener(v -> importSample("https://drive.google.com/uc?export=download&id=0B-GbNhnY_O_lbVY3VVVOMkc5OGM"));
    }

    private void selectFile(int id, String[] mimeTypes) {
        Intent intent = storageAccess.selectFileIntent(mimeTypes);
        requireActivity().startActivityForResult(intent, id);
    }

    private void onlineSearch() {

    }

    private void importSample(String url) {

    }

}
