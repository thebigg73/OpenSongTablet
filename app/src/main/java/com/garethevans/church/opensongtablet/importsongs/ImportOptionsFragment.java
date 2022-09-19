package com.garethevans.church.opensongtablet.importsongs;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE;

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
    private final String TAG = "ImportOptionsFragment";
    private SettingsImportBinding myView;
    private final String[] validFiles = new String[] {"text/plain","image/*","text/xml","application/xml","application/pdf","application/octet-stream"};
    private final String[] validBackups = new String[] {"application/zip","application/octet-stream","application/*"};
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ActivityResultLauncher<String> cameraPermission;
    private ActivityResultLauncher<Uri> takePhoto;
    private int whichFileType;
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
                        int where = R.id.importFileFragment;
                        String fileExtension = "";
                        if (mainActivityInterface.getImportFilename().contains(".")) {
                            fileExtension = mainActivityInterface.getImportFilename().substring(mainActivityInterface.getImportFilename().lastIndexOf("."));
                        }
                        if (fileExtension.equals(".osb") ||
                                whichFileType == mainActivityInterface.getPreferences().getFinalInt("REQUEST_OSB_FILE")) {
                            where = R.id.importOSBFragment;
                        } else if (fileExtension.equals(".backup") ||
                                whichFileType == mainActivityInterface.getPreferences().getFinalInt("REQUEST_IOS_FILE")) {
                            where = R.id.importIOSFragment;
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
                            LENGTH_INDEFINITE).setAction(android.R.string.ok, view -> {
                                try {
                                    cameraPermission.launch(mainActivityInterface.getAppPermissions().getCameraPermissions());
                                } catch (Exception e) {
                                    Log.d(TAG,"User probably left this fragment");
                                }
                    }).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Get a date for the file name
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss",mainActivityInterface.getLocale());
                cameraFilename = "Camera_"+sdf.format(new Date())+".png";
                uri = mainActivityInterface.getStorageAccess().getUriForItem("Songs", mainActivityInterface.getSong().getFolder(),
                        cameraFilename);
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" Create camerafile Songs/"+mainActivityInterface.getSong().getFolder()+"/"+cameraFilename+"  deleteOld=false");
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, uri,null,"Songs",
                        mainActivityInterface.getSong().getFolder(),cameraFilename);

                Log.d(TAG,"uri:" +uri);
                takePhoto.launch(uri);
            }
        });
        takePhoto = registerForActivityResult(new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        Log.d(TAG,"success");
                        mainActivityInterface.getSong().setFilename(cameraFilename);
                        // Add to the database
                        mainActivityInterface.getNonOpenSongSQLiteHelper().createSong(
                                mainActivityInterface.getSong().getFolder(), cameraFilename);
                        mainActivityInterface.getSQLiteHelper().createSong(
                                mainActivityInterface.getSong().getFolder(), cameraFilename);
                        mainActivityInterface.updateSongMenu(mainActivityInterface.getSong());
                        mainActivityInterface.getPreferences().setMyPreferenceString(
                                "songFilename",cameraFilename);
                        mainActivityInterface.navHome();
                    }
                });
    }

    private void setListeners() {
        myView.createSong.setOnClickListener(v -> {
            mainActivityInterface.setSong(new Song());
            mainActivityInterface.navigateToFragment(getString(R.string.deeplink_edit),0);
        });
        myView.importFile.setOnClickListener(v -> selectFile(mainActivityInterface.getPreferences().getFinalInt("REQUEST_FILE_CHOOSER"),validFiles));
        myView.importOSB.setOnClickListener(v -> selectFile(mainActivityInterface.getPreferences().getFinalInt("REQUEST_OSB_FILE"),validBackups));
        myView.importiOS.setOnClickListener(v -> selectFile(mainActivityInterface.getPreferences().getFinalInt("REQUEST_IOS_FILE"),validBackups));
        myView.importCamera.setOnClickListener(v -> getCamera());
        myView.importOnline.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.importOnlineFragment));
        myView.importChurch.setOnClickListener(v -> {
            // Check connection
            mainActivityInterface.setWhattodo("importChurchSample");
            mainActivityInterface.getCheckInternet().checkConnection(this, R.id.importOSBFragment, mainActivityInterface);
        });
        myView.importBand.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("importBandSample");
            mainActivityInterface.getCheckInternet().checkConnection(this, R.id.importOSBFragment, mainActivityInterface);
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
        cameraPermission.launch(mainActivityInterface.getAppPermissions().getCameraPermissions());
    }

    public void isConnected(boolean isConnected) {
        if (isConnected) {
            mainActivityInterface.navigateToFragment(getString(R.string.deeplink_import_osb),0);
        } else {
            mainActivityInterface.setWhattodo("");
            mainActivityInterface.getShowToast().doIt(getString(R.string.network_error));
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myView = null;
    }

}
