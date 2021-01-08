package com.garethevans.church.opensongtablet.preferences;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.databinding.SettingsProfilesBinding;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import static android.provider.DocumentsContract.EXTRA_INITIAL_URI;

public class ProfileFragment extends Fragment {

    private SettingsProfilesBinding myView;
    private MainActivityInterface mainActivityInterface;
    private Preferences preferences;
    private StorageAccess storageAccess;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsProfilesBinding.inflate(inflater,container,false);

        // Setup helpers
        setupHelpers();

        // Setup listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupHelpers() {
        preferences = mainActivityInterface.getPreferences();
        storageAccess = mainActivityInterface.getStorageAccess();
        mainActivityInterface.registerFragment(this,"ProfileFragment");
    }

    private void setupListeners() {
        myView.loadButton.setOnClickListener(v -> loadProfile());
        myView.saveButton.setOnClickListener(v -> saveProfile(new Intent()));
    }

    private void loadProfile() {
        // Open the file picker and when the user has picked a file, on activity result will

    }


    boolean doSaveProfile(Context c, Preferences preferences, StorageAccess storageAccess, Uri to) {
        boolean result = true;  // Returns true on success.  Catches throw to false
        try {
            // This is used to copy the current preferences xml file to the chosen name / location
            // Check the file exists, if not create it
            if (!storageAccess.uriExists(c, to)) {
                String name = to.getLastPathSegment();
                storageAccess.lollipopCreateFileForOutputStream(c, preferences, to, null, "Profiles", "", name);
            }

            // Different versions of Android save the preferences in different locations.
            //Uri prefsFile = getPrefsFile(c, storageAccess);

            //InputStream inputStream = storageAccess.getInputStream(c, prefsFile);
            //OutputStream outputStream = storageAccess.getOutputStream(c, to);


            //storageAccess.copyFile(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    private void saveProfile(Intent intent) {

    }

    Intent saveIntent() {
        Intent intent = new Intent();
        intent.setType("application/*");
        String [] mimeTypes = {"application/*", "application/xml", "text/xml"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.setAction(Intent.ACTION_CREATE_DOCUMENT);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            Uri uri = storageAccess.getUriForItem(getContext(), preferences, "Profiles", "", "");
            DocumentFile file = DocumentFile.fromTreeUri(getContext(), uri);
            if (file!=null) {
                intent.putExtra(EXTRA_INITIAL_URI, file.getUri());
                file = file.findFile("Profiles");
                if (file!=null) {
                    intent.putExtra(EXTRA_INITIAL_URI, file.getUri());
                }
            }
        }
        intent.putExtra(Intent.EXTRA_TITLE, preferences.getMyPreferenceString(getContext(),"profileName","Profile"));
        return intent;
    }
}
