package com.garethevans.church.opensongtablet.importsongs;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsImportBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ImportOptionsFragment extends Fragment {

    // This class asks the user which type of file should be imported.

    private MainActivityInterface mainActivityInterface;
    private SettingsImportBinding myView;
    private final String[] validFiles = new String[] {"text/plain","image/*","text/xml","application/xml","application/pdf","application/octet-stream"};
    private final String[] validBackups = new String[] {"application/zip","application/octet-stream","application/*"};
    private Thread thread;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ActivityResultLauncher<String> cameraPermission;
    private ActivityResultLauncher<Uri> takePhoto;
    private int whichFileType;
    private String TAG = "ImportOptionsFrag";
    private Uri uri;
    private String cameraFilename;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsImportBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(getString(R.string.import_main));

        // Set up launcher
        setupLauncher();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setupLauncher() {
        // Initialise the launchers
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                try {
                    Intent data = result.getData();
                    if (data != null) {
                        mainActivityInterface.setImportUri(data.getData());
                        String filename;
                        if (data.getDataString() != null) {
                            filename = mainActivityInterface.getStorageAccess().
                                    getActualFilename(data.getDataString());
                            mainActivityInterface.setImportFilename(filename);
                        }
                        int where = R.id.importFile;
                        if (whichFileType == mainActivityInterface.getPreferences().getFinalInt("REQUEST_OSB_FILE")) {
                            where = R.id.importOSBFragment;
                        } else if (whichFileType == mainActivityInterface.getPreferences().getFinalInt("REQUEST_IOS_FILE")) {
                            where = R.id.importiOS;
                        }
                        mainActivityInterface.navigateToFragment(null, where);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        cameraPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (!isGranted) {
                // Permission hasn't been allowed and we are due to explain why
                try {
                    Snackbar.make(myView.getRoot(), R.string.camera_info,
                            LENGTH_INDEFINITE).setAction(android.R.string.ok, view -> cameraPermission.launch(Manifest.permission.CAMERA)).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Get a date for the file name
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss",mainActivityInterface.getLocale());
                cameraFilename = "Camera_"+sdf.format(new Date())+".png";
                uri = mainActivityInterface.getStorageAccess().getUriForItem("Songs", mainActivityInterface.getSong().getFolder(),
                        cameraFilename);
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,uri,null,"Songs",
                        mainActivityInterface.getSong().getFolder(),cameraFilename);

                takePhoto.launch(uri);
            }
        });
        takePhoto = registerForActivityResult(new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        Log.d(TAG, "uri=" + uri);
                        mainActivityInterface.getSong().setFilename(cameraFilename);
                        // Add to the database
                        mainActivityInterface.getNonOpenSongSQLiteHelper().createSong(
                                mainActivityInterface.getSong().getFolder(), cameraFilename);
                        mainActivityInterface.updateSongMenu(mainActivityInterface.getSong());
                        mainActivityInterface.getPreferences().setMyPreferenceString(
                                "songfilename",cameraFilename);
                        mainActivityInterface.navHome();
                    }
                });
    }

    private void setListeners() {
        myView.createSong.setOnClickListener(v -> {
            mainActivityInterface.setSong(new Song());
            mainActivityInterface.navigateToFragment("opensongapp://settings/edit",0);
        });
        myView.importFile.setOnClickListener(v -> selectFile(mainActivityInterface.getPreferences().getFinalInt("REQUEST_FILE_CHOOSER"),validFiles));
        myView.importOSB.setOnClickListener(v -> selectFile(mainActivityInterface.getPreferences().getFinalInt("REQUEST_OSB_FILE"),validBackups));
        myView.importiOS.setOnClickListener(v -> selectFile(mainActivityInterface.getPreferences().getFinalInt("REQUEST_IOS_FILE"),validBackups));
        myView.importCamera.setOnClickListener(v -> getCamera());
        myView.importOnline.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.importOnlineFragment));
        myView.importChurch.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("importChurchSample");
            mainActivityInterface.navigateToFragment(null,R.id.importOSBFragment);
        });
        myView.importBand.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("importBandSample");
            mainActivityInterface.navigateToFragment(null,R.id.importOSBFragment);
        });
    }

    private void selectFile(int id, String[] mimeTypes) {
        whichFileType = id;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activityResultLauncher.launch(intent);
    }

    private void getCamera() {
        // Check permission and go for it if ok
        cameraPermission.launch(Manifest.permission.CAMERA);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        killThread();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        killThread();
        myView = null;
    }

    private void killThread() {
        if (thread!=null) {
            thread.interrupt();
            thread = null;
        }
    }

}
