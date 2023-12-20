package com.garethevans.church.opensongtablet.setprocessing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsSetsBackupsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupRestoreSetsFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsSetsBackupsBinding myView;
    private ArrayList<String> chosenSets;
    private String backupFilename;
    private Uri backupUri;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private boolean success = false;
    private final String setSeparator = "__";
    private final String TAG = "BackupRestoreSets";
    private String restore_sets_string="", website_set_restore_string="", backup_string="",
            website_set_backup_string="", unknown_string="", backup_sets_string="",
            import_basic_string="", mainfoldername_string="", backup_info_string="",
            success_string="", error_string="", toolBarTitle="";
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(toolBarTitle);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsSetsBackupsBinding.inflate(inflater, container, false);

        prepareStrings();

        Window w = null;
        if (getActivity()!=null) {
            w = getActivity().getWindow();
        }
        if (w != null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }

        if (mainActivityInterface.getWhattodo().equals("restoresets")) {
            toolBarTitle = restore_sets_string;
            webAddress = website_set_restore_string;
            setupFileChooserListener();
            initialiseLauncher();
            openFilePicker();
        } else if (mainActivityInterface.getWhattodo().equals("intentlaunch")) {
            toolBarTitle = restore_sets_string;
            webAddress = website_set_restore_string;
            backupUri = mainActivityInterface.getImportUri();
            myView.backupName.setText(mainActivityInterface.getImportFilename());
            setupViews();
        } else {
            // Set up views
            setupViews();
            toolBarTitle = backup_sets_string;
            webAddress = website_set_backup_string;
        }

        myView.nestedScrollView.setExtendedFabToAnimate(myView.createBackupFAB);
        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            restore_sets_string = getString(R.string.restore_sets);
            website_set_restore_string = getString(R.string.website_set_restore);
            website_set_backup_string = getString(R.string.website_set_backup);
            unknown_string = getString(R.string.unknown);
            backup_sets_string = getString(R.string.backup_sets);
            backup_string = getString(R.string.backup);
            import_basic_string = getString(R.string.import_basic);
            mainfoldername_string = getString(R.string.mainfoldername);
            backup_info_string = getString(R.string.backup_info);
            success_string = getString(R.string.success);
            error_string = getString(R.string.error);
        }
    }

    private void initialiseLauncher () {
        // Initialise the launcher if we are importing/restoring
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                try {
                    Intent data = result.getData();
                    if (data != null) {
                        backupUri = data.getData();
                        String importFilename = mainActivityInterface.getStorageAccess().getFileNameFromUri(backupUri);
                        if (importFilename.endsWith(".osbs")) {
                            myView.backupName.setText(importFilename);
                            setupViews();
                        } else {
                            myView.backupName.setText(unknown_string);
                            myView.createBackupFAB.setEnabled(false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupFileChooserListener() {
        myView.backupName.setFocusable(false);
        myView.backupName.setFocusableInTouchMode(false);
        myView.backupName.setOnClickListener(v -> openFilePicker());
    }

    private void openFilePicker() {
        // Open the file picker and when the user has picked a file, deal with it
        Intent loadIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        Uri uri = mainActivityInterface.getStorageAccess().
                getUriForItem("Backups","",null);
        loadIntent.setDataAndType(uri,"application/*");
        loadIntent.putExtra("android.provider.extra.INITIAL_URI", uri);
        loadIntent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        activityResultLauncher.launch(loadIntent);
    }

    private void setupViews() {
        // Clear any existing items!
        myView.foundSetsListView.removeAllViews();

        if (mainActivityInterface.getWhattodo().equals("backupsets")) {
            // Create a temp filename
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd", mainActivityInterface.getLocale());
            String formattedDate = df.format(cal.getTime());
            myView.backupName.setText("OpenSongSetBackup_" + formattedDate + ".osbs");

            toolBarTitle = backup_sets_string;

            myView.overWrite.setVisibility(View.GONE);

            myView.createBackupFAB.setText(backup_string);
            myView.createBackupFAB.setOnClickListener(view -> doBackup());

            // Add the checkboxes
            addCheckBoxes(mainActivityInterface.getStorageAccess().listFilesInFolder("Sets",""));


        } else {
            // Filename is set when user selects a file
            myView.overWrite.setVisibility(View.VISIBLE);

            myView.createBackupFAB.setText(import_basic_string);
            myView.createBackupFAB.setOnClickListener(view -> doImport());

            myView.progressBar.setVisibility(View.VISIBLE);

            // Get a list of the sets in the zip file (alphabetically)
            // Do this in a new Thread
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                Handler handler = new Handler(Looper.getMainLooper());
                ArrayList<String> setList = new ArrayList<>();

                InputStream inputStream = null;
                if (mainActivityInterface.getWhattodo().equals("restoresets")) {
                    inputStream = mainActivityInterface.getStorageAccess().getInputStream(backupUri);

                } else {
                    if (getActivity()!=null) {
                        File file = mainActivityInterface.getStorageAccess().getAppSpecificFile("Import","",mainActivityInterface.getImportFilename());
                        //File folder = new File(getActivity().getExternalCacheDir(), "Import");
                        //Log.d(TAG, "created: " + folder.mkdirs());
                        //File file = new File(folder, mainActivityInterface.getImportFilename());
                        try {
                            inputStream = new FileInputStream(file);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (inputStream!=null) {
                    ZipInputStream zipInputStream = new ZipInputStream(inputStream);
                    ZipEntry ze;

                    try {
                        while ((ze = zipInputStream.getNextEntry()) != null) {
                            setList.add(ze.getName());
                        }
                        zipInputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    handler.post(() -> {
                        // Add the checkboxes
                        addCheckBoxes(setList);
                        myView.progressBar.setVisibility(View.GONE);
                    });
                }
            });
        }
    }

    private void addCheckBoxes(ArrayList<String> setList) {
        // To make this more readable, keep the MAIN sets separate from folder sets
        ArrayList<String> mainSets = new ArrayList<>();
        ArrayList<String> folderSets = new ArrayList<>();

        for (String set:setList) {
            if (!set.contains(setSeparator)) {
                mainSets.add(set);
            } else {
                folderSets.add(set);
            }
        }
        Collator coll = Collator.getInstance(mainActivityInterface.getLocale());
        coll.setStrength(Collator.SECONDARY);
        Collections.sort(mainSets, coll);
        Collections.sort(folderSets, coll);

        // Now we've sorted, Go through the list and add a new checkbox item for each one
        addSetCheckBoxes(mainSets);
        addSetCheckBoxes(folderSets);

        myView.createBackupFAB.setEnabled(myView.foundSetsListView.getChildCount() > 0);
    }

    private void addSetCheckBoxes(ArrayList<String> set) {
        for (String setItem:set) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(niceSetItem(setItem));
            checkBox.setTag(setItem);
            checkBox.setChecked(true);
            checkBox.setPadding(16, 32, 16, 32);
            myView.foundSetsListView.addView(checkBox);
        }
    }

    private String niceSetItem(String setItem) {
        // This returns sets with categories in brackets
        if (!setItem.contains(setSeparator)) {
            return "(" + mainfoldername_string + ") " + setItem;
        } else {
            String[] bits = setItem.split(setSeparator);
            if (bits.length==2) {
                return "(" + bits[0] + ") " + bits[1];
            } else {
                return setItem;
            }
        }
    }

    private void doBackup() {
        // Now we make a zip file from the selected sets (.osbs file to be precise!)
        myView.progressBar.setVisibility(View.VISIBLE);

        // Get a note of the chosen set filenames
        getChosenSets();

        backupFilename = myView.backupName.getText().toString();

        // Do the main lifting in a new thread
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            if (backupFilename.isEmpty()) {
                backupFilename = "OpenSongSetBackup.osbs";
            }
            if (!backupFilename.endsWith(".osbs")) {
                backupFilename = backupFilename + ".osbs";
            }
            Uri backupUri = mainActivityInterface.getStorageAccess().getUriForItem("Backups","",backupFilename);
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doBackup Create Backups"+backupFilename+" deleteOld=true");
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, backupUri,null,"Backups","",backupFilename);
            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(backupUri);
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            ZipEntry ze;
            byte[] tempBuff = new byte[1024];

            // For each selected set, get a uri reference and input stream and add to the zip output stream
            for (int x=0; x<chosenSets.size(); x++) {
                try {
                    Uri thisUri = mainActivityInterface.getStorageAccess().getUriForItem("Sets", "", chosenSets.get(x));
                    InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(thisUri);
                    ze = new ZipEntry(chosenSets.get(x));
                    zipOutputStream.putNextEntry(ze);
                    if (!ze.isDirectory()) {
                        int len;
                        while ((len = inputStream.read(tempBuff)) > 0) {
                            zipOutputStream.write(tempBuff, 0, len);
                        }
                    }
                    zipOutputStream.closeEntry();
                    inputStream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                zipOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                if (myView!=null) {
                    myView.progressBar.setVisibility(View.GONE);
                }
                Intent intent = mainActivityInterface.getExportActions().exportBackup(backupUri, backupFilename);
                startActivity(Intent.createChooser(intent, backup_info_string));
            });
        });

    }

    private void doImport() {
        myView.progressBar.setVisibility(View.VISIBLE);
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(backupUri);
        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
        success = false;
        boolean overwrite = myView.overWrite.isChecked();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            // Get a note of the chosen sets
            getChosenSets();

            ZipEntry ze;
            byte[] buffer = new byte[8192];

            try {
                while ((ze = zipInputStream.getNextEntry()) != null) {
                    if (chosenSets.contains(ze.getName())) {
                        Uri file_uri = mainActivityInterface.getStorageAccess().getUriForItem("Sets", "", ze.getName());
                        boolean exists = mainActivityInterface.getStorageAccess().uriExists(file_uri);
                        if (!exists || overwrite) {
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doImport Create Sets/"+ze.getName()+" deleteOld=true");
                            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, file_uri, null, "Sets", "", ze.getName());
                            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(file_uri);
                            // Write the file
                            int count;
                            while ((count = zipInputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, count);
                            }
                            outputStream.close();
                        }
                    }
                }
                success = true;
                zipInputStream.closeEntry();

            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }

            handler.post(() -> {
                myView.progressBar.setVisibility(View.GONE);
                if (success) {
                    mainActivityInterface.getShowToast().doIt(success_string);
                } else {
                    mainActivityInterface.getShowToast().doIt(error_string);
                }
            });
        });
    }

    private void getChosenSets() {
        chosenSets = new ArrayList<>();
        for (int x=0; x<myView.foundSetsListView.getChildCount(); x++) {
            if (((CheckBox) myView.foundSetsListView.getChildAt(x)).isChecked()) {
                chosenSets.add(myView.foundSetsListView.getChildAt(x).getTag().toString());
            }
        }
    }
}
