package com.garethevans.church.opensongtablet.importsongs;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.exifinterface.media.ExifInterface;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsImportBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.CreateSongBottomSheet;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImportOptionsFragment extends Fragment {

    // This class asks the user which type of file should be imported.

    private MainActivityInterface mainActivityInterface;
    private final String TAG = "ImportOptionsFragment";
    private SettingsImportBinding myView;
    private final String[] validFiles = new String[] {"text/plain","image/*","text/xml","application/xml","application/pdf","application/octet-stream","application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
    private final String[] validBackups = new String[] {"application/zip","application/octet-stream","application/*"};
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ActivityResultLauncher<String> cameraPermission;
    private ActivityResultLauncher<Uri> takePhoto;
    private int whichFileType;
    private Uri uri;
    private File file;
    private String cameraFilename, import_main_string="", deeplink_browse_host_files="",
            deeplink_import_osb_string="", network_error_string="";

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(import_main_string);
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsImportBinding.inflate(inflater,container,false);

        prepareStrings();

        // Set up launcher
        setupLauncher();

        // Set up views
        setupViews();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        /* Not got this working yet, so leave hidden
        if (getContext()!=null && mainActivityInterface!=null && myView!=null) {
            myView.browseHostLayout.setVisibility((!mainActivityInterface.getNearbyConnections().getIsHost() &&
                    !mainActivityInterface.getNearbyConnections().getConnectedEndpoints().isEmpty() &&
                    mainActivityInterface.getNearbyConnections().getUsingNearby()) ? View.VISIBLE:View.GONE);
        //}*/
        myView.browseHostLayout.setVisibility(View.GONE);
    }
    private void prepareStrings() {
        if (getContext()!=null) {
            import_main_string = getString(R.string.import_main);
            deeplink_import_osb_string = getString(R.string.deeplink_import_osb);
            network_error_string = getString(R.string.network_error);
            deeplink_browse_host_files = getString(R.string.deeplink_browse_host_files);
        }
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
            if (mainActivityInterface.getAppPermissions().hasCameraPermission()) {
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
                // Save the image to the app private folder for now
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss",mainActivityInterface.getLocale());
                cameraFilename = "Camera_"+sdf.format(new Date())+".jpg";
                Log.d(TAG,"cameraFilename:"+cameraFilename);
                file = mainActivityInterface.getStorageAccess().getAppSpecificFile("files","export",cameraFilename);
                if (getContext()!=null) {
                    uri = FileProvider.getUriForFile(getContext(), "com.garethevans.church.opensongtablet.fileprovider", file);
                    takePhoto.launch(uri);
                }
            }
        });

        takePhoto = registerForActivityResult(new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        Log.d(TAG,"success");

                        // Do this with a delay handler to give time for the file to finish
                        myView.importCamera.postDelayed(() -> {
                            // Change the exif data to match the device orientation
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                try {
                                    int surfaceRotation = myView.getRoot().getDisplay().getRotation();
                                    int newExifRotation = 0;
                                    switch (surfaceRotation) {
                                        case Surface.ROTATION_0:
                                            Log.d(TAG,"surfaceRotation:ROTATION_0");
                                            newExifRotation = ExifInterface.ORIENTATION_NORMAL;
                                            break;
                                        case Surface.ROTATION_90:
                                            newExifRotation = ExifInterface.ORIENTATION_ROTATE_90;
                                            Log.d(TAG,"surfaceRotation:ROTATION_90");
                                            break;
                                        case Surface.ROTATION_180:
                                            Log.d(TAG,"surfaceRotation:ROTATION_180");
                                            newExifRotation = ExifInterface.ORIENTATION_ROTATE_180;
                                            break;
                                        case Surface.ROTATION_270:
                                            Log.d(TAG,"surfaceRotation:ROTATION_270");
                                            newExifRotation = ExifInterface.ORIENTATION_ROTATE_270;
                                            break;
                                    }
                                    Log.d(TAG,"newExifRotation:"+newExifRotation);
                                    ExifInterface ei = new ExifInterface(file.getPath());
                                    String currentOrientation = ei.getAttribute(ExifInterface.TAG_ORIENTATION);
                                    Log.d(TAG,"current exif orientation:"+currentOrientation);
                                    ei.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(newExifRotation));
                                    ei.saveAttributes();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            // Now ask the user for a file name and folder using the bottom sheet
                            CreateSongBottomSheet createSongBottomSheet = new CreateSongBottomSheet(uri);
                            createSongBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"CreateSongBottomSheet");
                        },100);
                    }
                });
    }

    private void setListeners() {
        myView.createSong.setOnClickListener(v -> {
            // Open the bottom sheet to create a new song folder/filename
            CreateSongBottomSheet createSongBottomSheet = new CreateSongBottomSheet();
            createSongBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"CreateSongBottomSheet");
        });
        myView.importFile.setOnClickListener(v -> selectFile(mainActivityInterface.getPreferences().getFinalInt("REQUEST_FILE_CHOOSER"),validFiles));
        myView.importOSB.setOnClickListener(v -> selectFile(mainActivityInterface.getPreferences().getFinalInt("REQUEST_OSB_FILE"),validBackups));
        myView.importiOS.setOnClickListener(v -> selectFile(mainActivityInterface.getPreferences().getFinalInt("REQUEST_IOS_FILE"),validBackups));
        myView.importBulk.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.importBulkFragment));
        myView.importCamera.setOnClickListener(v -> getCamera());
        myView.importOnline.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.importOnlineFragment));
        myView.importChurch.setOnClickListener(v -> {
            // Check connection
            mainActivityInterface.setWhattodo("importChurchSample");
            mainActivityInterface.getCheckInternet().checkConnection(getContext(),this, R.id.importOSBFragment, mainActivityInterface);
        });
        myView.importBand.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("importBandSample");
            mainActivityInterface.getCheckInternet().checkConnection(getContext(),this, R.id.importOSBFragment, mainActivityInterface);
        });
        myView.browseHost.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("browsesongs");
            mainActivityInterface.navigateToFragment(deeplink_browse_host_files,0);
        });
    }

    private void selectFile(int id, String[] mimeTypes) {
        whichFileType = id;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addFlags(mainActivityInterface.getStorageAccess().getAddReadUriFlags());
        activityResultLauncher.launch(intent);
    }

    private void getCamera() {
        // Check permission and go for it if ok
        cameraPermission.launch(mainActivityInterface.getAppPermissions().getCameraPermissions());
    }

    public void isConnected(boolean isConnected) {
        if (isConnected) {
            mainActivityInterface.navigateToFragment(deeplink_import_osb_string,0);
        } else {
            mainActivityInterface.setWhattodo("");
            mainActivityInterface.getShowToast().doIt(network_error_string);
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
