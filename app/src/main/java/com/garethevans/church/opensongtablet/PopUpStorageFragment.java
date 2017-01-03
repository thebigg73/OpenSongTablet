package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

import java.io.File;

public class PopUpStorageFragment extends DialogFragment {

    static PopUpStorageFragment newInstance() {
        PopUpStorageFragment frag;
        frag = new PopUpStorageFragment();
        return frag;
    }

    public interface MyInterface {
        void rebuildSearchIndex();
        void prepareSongMenu();
        void openFragment();
    }

    private MyInterface mListener;

    RadioButton intStorageButton;
    RadioButton extStorageButton;
    RadioButton otherStorageButton;
    Button changeCustom;
    Button wipeSongs;
    Button exitStorage;
    String numeral = "1";

    File intStorCheck;
    File extStorCheck;
    File otherStorCheck;
    private View mLayout;
    public boolean storageGranted = false;
    private static final int requestStorage = 0;
    boolean extStorageExists = false;
    boolean defStorageExists = false;
    boolean otherStorageExists = false;

    String secStorage = System.getenv("SECONDARY_STORAGE");
    String defStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static File customStorageLoc = Environment.getExternalStorageDirectory();
    @SuppressLint("SdCardPath")
    String[] secStorageOptions = {"/mnt/emmc/",
            "/FAT",
            "/Removable/MicroSD",
            "/Removable/SD",
            "/data/sdext2",
            "/sdcard/sd",
            "/mnt/flash",
            "/mnt/sdcard/tflash",
            "/mnt/nand",
            "/mnt/external1",
            "/mnt/sdcard-ext",
            "/mnt/extsd",
            "/mnt/sdcard2",
            "/mnt/sdcard/sdcard1",
            "/mnt/sdcard/sdcard2",
            "/mnt/sdcard/ext_sd",
            "/mnt/sdcard/_ExternalSD",
            "/mnt/sdcard/external_sd",
            "/mnt/sdcard/SD_CARD",
            "/mnt/sdcard/removable_sdcard",
            "/mnt/sdcard/external_sdcard",
            "/mnt/sdcard/extStorages/SdCard",
            "/mnt/ext_card",
            "/mnt/extern_sd",
            "/mnt/ext_sdcard",
            "/mnt/ext_sd",
            "/mnt/external_sd",
            "/mnt/external_sdcard",
            "/mnt/extSdCard"};

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    public void onStart() {
        super.onStart();

        // safety check
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.storage_choose));
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_storage, container, false);

        // Initialise the views
        mLayout = V.findViewById(R.id.page);
        intStorageButton = (RadioButton) V.findViewById(R.id.intStorage);
        extStorageButton = (RadioButton) V.findViewById(R.id.extStorage);
        otherStorageButton = (RadioButton) V.findViewById(R.id.otherStorage);
        changeCustom = (Button) V.findViewById(R.id.editCustomStorage);
        exitStorage = (Button) V.findViewById(R.id.exitStorage);
        wipeSongs = (Button) V.findViewById(R.id.wipeSongs);

        Log.d("d","Started PopUpStorageFragment");

        // Check we have storage permission
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Storage permission has not been granted.
            Log.d("d","Storage permission hasn't been granted yet, so asking...");
            requestStoragePermission();

        } else {
            Log.d("d","Storage permission has been granted");
            storageGranted = true;
        }

        // Set the button listeners
        exitStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveStorageLocation();
            }
        });
        wipeSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (storageGranted) {
                    FullscreenActivity.whattodo = "wipeallsongs";
                    mListener.openFragment();
                    dismiss();
                } else {
                    requestStoragePermission();
                }
            }
        });

        String text = getResources().getString(R.string.custom) + "\n(" + defStorage + ")";
        otherStorageButton.setText(text);

        intStorageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numeral = "1";
                intStorageButton.setChecked(true);
                extStorageButton.setChecked(false);
                otherStorageButton.setChecked(false);
            }
        });

        extStorageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numeral = "2";
                intStorageButton.setChecked(false);
                extStorageButton.setChecked(true);
                otherStorageButton.setChecked(false);
            }
        });

        otherStorageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numeral = "3";
                intStorageButton.setChecked(false);
                extStorageButton.setChecked(false);
                otherStorageButton.setChecked(true);
            }
        });

        changeCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.whattodo = "customstoragefind";
                mListener.openFragment();
                dismiss();
            }
        });

        Log.d("d","Numeral = "+numeral+"\n1=Internal, 2=External, 3=Custom");

        // If user has set their storage preference, set the appropriate radio button
        switch (FullscreenActivity.prefStorage) {
            default:
            case "int":
                intStorageButton.setChecked(true);
                extStorageButton.setChecked(false);
                otherStorageButton.setChecked(false);
                numeral = "1";
                break;
            case "ext":
                intStorageButton.setChecked(false);
                extStorageButton.setChecked(true);
                otherStorageButton.setChecked(false);
                numeral = "2";
                break;
            case "other":
                intStorageButton.setChecked(false);
                extStorageButton.setChecked(false);
                otherStorageButton.setChecked(true);
                numeral = "3";
                break;
        }

        // Set the text for the various storage locations
        checkStorageLocations();

        return V;
    }

    public void checkStorageLocations() {
        // Decide if internal and external storage storage exists
        if (secStorage!=null) {
            if (secStorage.contains(":")) {
                secStorage = secStorage.substring(0,secStorage.indexOf(":"));
            }
            Log.d("d","secStorage="+secStorage);
        } else {
            // Lets look for alternative secondary storage positions
            for (String secStorageOption : secStorageOptions) {
                File testaltsecstorage = new File(secStorageOption);
                if (testaltsecstorage.exists() && testaltsecstorage.canWrite()) {
                    secStorage = secStorageOption;
                }
            }
            Log.d("d","secStorageOption (alternative) = "+secStorage);
        }

        // If secondary and default storage are the same thing, hide secStorage
        if (defStorage.equals(secStorage)) {
            secStorage = null;
        }

        otherStorCheck = new File(FullscreenActivity.customStorage);
        if (!FullscreenActivity.customStorage.isEmpty() && otherStorCheck.exists() && otherStorCheck.isDirectory()) {
            customStorageLoc = otherStorCheck;
            String textother = getResources().getString(R.string.custom) + "\n(" + customStorageLoc.getAbsolutePath() + ")";
            otherStorageButton.setText(textother);
            otherStorageExists = true;
            Log.d("d","customStorageLoc="+customStorageLoc);
        }

        // If external storage isn't found, disable this radiobutton
        intStorCheck = new File(defStorage);
        if (intStorCheck.exists()) {
            defStorageExists = true;
        }
        if (secStorage!=null) {
            extStorCheck = new File(secStorage);
            if (extStorCheck.exists()) {
                extStorageExists = true;
            }
        }

        if (!defStorageExists) {
            intStorageButton.setClickable(false);
            intStorageButton.setChecked(false);
            if (FullscreenActivity.prefStorage.equals("ext")) {
                extStorageButton.setChecked(true);
                otherStorageButton.setChecked(false);
            } else {
                extStorageButton.setChecked(false);
                otherStorageButton.setChecked(true);
            }
            intStorageButton.setAlpha(0.4f);
            String radiotext = getResources().getString(R.string.storage_int) + " - " + getResources().getString(R.string.storage_notavailable);
            intStorageButton.setText(radiotext);
        } else {
            // Try to get free space
            String freespace = "?";
            if (intStorCheck.exists()) {
                long temp = intStorCheck.getFreeSpace();
                if (temp>0) {
                    int num = (int) ((float)temp/(float)1000000);
                    freespace = "" + num;
                }
            }
            String inttext = getResources().getString(R.string.storage_int) + "\n(" + defStorage + "/documents)\n" + getResources().getString(R.string.storage_space) + " - " + freespace + " MB";
            intStorageButton.setText(inttext);
        }
        if (!extStorageExists || !extStorCheck.canWrite()) {
            extStorageButton.setClickable(false);
            extStorageButton.setAlpha(0.4f);
            if (FullscreenActivity.prefStorage.equals("int")) {
                intStorageButton.setChecked(true);
                otherStorageButton.setChecked(false);
            } else {
                intStorageButton.setChecked(false);
                otherStorageButton.setChecked(true);
            }
            String exttext = getResources().getString(R.string.storage_ext) + "\n" + getResources().getString(R.string.storage_notavailable);
            extStorageButton.setText(exttext);
        } else {
            // Try to get free space
            String freespace = "?";
            if (extStorCheck.exists()) {
                long temp = extStorCheck.getFreeSpace();
                if (temp>0) {
                    int num = (int) ((float)temp/(float)1000000);
                    freespace = "" + num;
                }
            }
            String exttext2 = getResources().getString(R.string.storage_ext) + "\n(" + secStorage + "/documents)\n" + getResources().getString(R.string.storage_space) + " - " + freespace + " MB";
            extStorageButton.setText(exttext2);
        }
    }

    // The permission requests
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(mLayout, R.string.storage_rationale,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestStorage);
                }
            }).show();
        } else {
            // Storage permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestStorage);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == requestStorage) {
            storageGranted = grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            Log.d("d", "onRequestPermissionResult\nstorageGranted=" + storageGranted);

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void saveStorageLocation() {
        //Rewrite the shared preference
        switch (numeral) {
            case "2":
                FullscreenActivity.prefStorage = "ext";
                FullscreenActivity.root = extStorCheck;
                getOtherFolders();
                break;

            case "3":
                FullscreenActivity.prefStorage = "other";
                FullscreenActivity.root = customStorageLoc;
                getOtherFoldersCustom();
                break;
            default:
                FullscreenActivity.prefStorage = "int";
                FullscreenActivity.root = Environment.getExternalStorageDirectory();
                getOtherFolders();
                break;
        }
        Log.d("d","numeral="+numeral);
        Log.d("d","prefStorage="+FullscreenActivity.prefStorage);
        Log.d("d","dir="+FullscreenActivity.dir);
        if (checkDirectories()) {
            Preferences.savePreferences();
            ListSongFiles.getAllSongFolders();
            mListener.rebuildSearchIndex();
            mListener.prepareSongMenu();
            dismiss();
        } else {
            FullscreenActivity.myToastMessage = getResources().getString(R.string.storage_issues);
            ShowToast.showToast(getActivity());
        }
    }

    public void getOtherFolders() {
        FullscreenActivity.homedir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong");
        FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs");
        FullscreenActivity.dironsong = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs/OnSong");
        FullscreenActivity.dirsets = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Sets");
        FullscreenActivity.dirPads = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Pads");
        FullscreenActivity.dirbackgrounds = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Backgrounds");
        FullscreenActivity.dirbibles = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/OpenSong Scripture");
        FullscreenActivity.dirbibleverses = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/OpenSong Scripture/_cache");
        FullscreenActivity.dirscripture = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Scripture");
        FullscreenActivity.dirscriptureverses = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Scripture/_cache");
        FullscreenActivity.dircustomslides = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Slides/_cache");
        FullscreenActivity.dircustomnotes = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Notes/_cache");
        FullscreenActivity.dircustomimages = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Images/_cache");
        FullscreenActivity.dirvariations = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Variations");
        FullscreenActivity.dirprofiles = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Profiles");
        Log.d("d","getOtherFolders() called with prefStorage="+ FullscreenActivity.prefStorage);
        Log.d("d","homedir="+ FullscreenActivity.homedir);
    }

    public void getOtherFoldersCustom() {
        FullscreenActivity.homedir = new File(FullscreenActivity.root.getAbsolutePath() + "/OpenSong");
        FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/OpenSong/Songs");
        FullscreenActivity.dironsong = new File(FullscreenActivity.root.getAbsolutePath() + "/OpenSong/Songs/OnSong");
        FullscreenActivity.dirsets = new File(FullscreenActivity.root.getAbsolutePath() + "/OpenSong/Sets");
        FullscreenActivity.dirPads = new File(FullscreenActivity.root.getAbsolutePath() + "/OpenSong/Pads");
        FullscreenActivity.dirbackgrounds = new File(FullscreenActivity.root.getAbsolutePath() + "/OpenSong/Backgrounds");
        FullscreenActivity.dirbibles = new File(FullscreenActivity.root.getAbsolutePath() + "/OpenSong/OpenSong Scripture");
        FullscreenActivity.dirbibleverses = new File(FullscreenActivity.root.getAbsolutePath() + "/OpenSong/OpenSong Scripture/_cache");
        FullscreenActivity.dirscripture = new File(FullscreenActivity.root.getAbsolutePath() + "/OpenSong/Scripture");
        FullscreenActivity.dirscriptureverses = new File(FullscreenActivity.root.getAbsolutePath() + "/OpenSong/Scripture/_cache");
        FullscreenActivity.dircustomslides = new File(FullscreenActivity.root.getAbsolutePath() + "/OpenSong/Slides/_cache");
        FullscreenActivity.dircustomnotes = new File(FullscreenActivity.root.getAbsolutePath() + "/OpenSong/Notes/_cache");
        FullscreenActivity.dircustomimages = new File(FullscreenActivity.root.getAbsolutePath() + "/OpenSong/Images/_cache");
        FullscreenActivity.dirvariations = new File(FullscreenActivity.root.getAbsolutePath() + "/OpenSong/Variations");
        FullscreenActivity.dirprofiles = new File(FullscreenActivity.root.getAbsolutePath() + "/OpenSong/Profiles");
        Log.d("d","getOtherFoldersCustom() called");
        Log.d("d","homedir="+ FullscreenActivity.homedir);
    }

    public static boolean checkDirectories(){
        boolean homedir_success = createDirectory(FullscreenActivity.homedir);
        boolean dir_success = createDirectory(FullscreenActivity.dir);
        boolean dirsets_success = createDirectory(FullscreenActivity.dirsets);
        boolean dirPads_success = createDirectory(FullscreenActivity.dirPads);
        boolean dirbackgrounds_success = createDirectory(FullscreenActivity.dirbackgrounds);
        boolean dirbibles_success = createDirectory(FullscreenActivity.dirbibles);
        boolean dirverses_success = createDirectory(FullscreenActivity.dirbibleverses);
        boolean dirscripture_success = createDirectory(FullscreenActivity.dirscripture);
        boolean dirscriptureverses_success = createDirectory(FullscreenActivity.dirscriptureverses);
        boolean dircustomimages_success = createDirectory(FullscreenActivity.dircustomimages);
        boolean dircustomnotes_success = createDirectory(FullscreenActivity.dircustomnotes);
        boolean dircustomslides_success = createDirectory(FullscreenActivity.dircustomslides);
        boolean dirvariations_success = createDirectory(FullscreenActivity.dirvariations);
        boolean dirprofiles_success = createDirectory(FullscreenActivity.dirprofiles);
        boolean success;
        if (homedir_success && dir_success && dirsets_success && dirPads_success && dirbackgrounds_success &&
                dirbibles_success && dirverses_success && dirscripture_success && dirscriptureverses_success &&
                dircustomimages_success && dircustomnotes_success && dircustomslides_success &&
                dirvariations_success && dirprofiles_success) {
            success = true;
        } else {
            success = false;
        }
        return success;
    }

    public static boolean createDirectory(File folder){
        boolean success = true;
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Log.d("d","Error creating directory - "+folder);
                success = false;
            } else {
                Log.d("d","Successfully created directory - "+folder);
            }
        } else {
            Log.d("d",folder+" already exist, trying to create it...");
        }
        if (folder.canWrite()) {
            Log.d("d",folder+" is writeable.");
        } else {
            Log.d("d",folder+" is not writeable");
            success = false;
        }
        return success;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}