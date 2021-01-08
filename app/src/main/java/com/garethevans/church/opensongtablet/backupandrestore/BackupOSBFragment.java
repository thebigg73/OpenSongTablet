package com.garethevans.church.opensongtablet.backupandrestore;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.FragmentOsbbackupBinding;
import com.garethevans.church.opensongtablet.export.ExportActions;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupOSBFragment extends Fragment {
    // This Fragment allows the user to create an OpenSongApp backup file
    // Show the user which folders are detected and can be backed up.
    // By default it will be all of them

    private FragmentOsbbackupBinding myView;
    private MainActivityInterface mainActivityInterface;
    private StorageAccess storageAccess;
    private Preferences preferences;
    private CommonSQL commonSQL;
    private SQLiteHelper sqLiteHelper;
    private ExportActions exportActions;

    private String backupFilename;
    private ArrayList<String> checkedFolders;
    private boolean error = false;
    private boolean alive = true;

    private Thread thread;
    private Runnable runnable;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = FragmentOsbbackupBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(null,getString(R.string.settings) + " / " + getString(R.string.storage) +
                " / " + getString(R.string.backup));

        getHelpers();
        
        setupViews();
        
        return myView.getRoot();
    }

    private void getHelpers() {
        storageAccess = mainActivityInterface.getStorageAccess();
        preferences = mainActivityInterface.getPreferences();
        commonSQL = mainActivityInterface.getCommonSQL();
        sqLiteHelper = mainActivityInterface.getSQLiteHelper();
        exportActions = mainActivityInterface.getExportActions();
    }

    private void setupViews() {
        new Thread(() -> {
            // Get the default file name
            String deffilename = defaultFilename();
            requireActivity().runOnUiThread(() -> myView.backupName.getEditText().setText(deffilename));

            // Get a list of available folders in the app
            ArrayList<String> folders = commonSQL.getFolders(sqLiteHelper.getDB(getContext()));

            // Create a new checkbox entry (default to ticked) for each one
            requireActivity().runOnUiThread(() -> {
                for (String folder:folders) {
                    CheckBox checkBox = new CheckBox(getContext());
                    checkBox.setText(folder);
                    checkBox.setTag(folder);
                    checkBox.setChecked(true);
                    checkBox.setPadding(16,32,16,32);
                    myView.foundFoldersListView.addView(checkBox);
                }

                myView.createBackupFAB.setOnClickListener(v -> doSave());
            });

        }).start();    
    }
    
    private String defaultFilename() {
        // Get the date for the file
        Calendar cal = Calendar.getInstance();
        System.out.println("Current time => " + cal.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd", mainActivityInterface.getLocale());
        String formattedDate = df.format(cal.getTime());
        return "OpenSongBackup_" + formattedDate + ".osb";
    }

    private void getCheckedFolders() {
        checkedFolders = new ArrayList<>();
        for (int x=0; x<myView.foundFoldersListView.getChildCount();x++) {
            CheckBox checkBox = (CheckBox) myView.foundFoldersListView.getChildAt(x);
            if (checkBox!=null && checkBox.isChecked() && checkBox.getTag()!=null) {
                checkedFolders.add(checkBox.getTag().toString());
            }
        }
    }

    private void doSave() {
        // Get the checked folders
        getCheckedFolders();
        runnable = () -> {

            // Get the backup file name and checked folders
            requireActivity().runOnUiThread(() -> {
                if (alive) {
                    // Check the backup file name
                    backupFilename = myView.backupName.getEditText().getText().toString();
                    // Make the progressText Visible
                    myView.progressText.setVisibility(View.VISIBLE);
                    myView.progressBar.setVisibility(View.VISIBLE);
                    myView.createBackupFAB.setEnabled(false);
                }
            });

            // Check the file list is up to date
            ArrayList<String> allFiles = storageAccess.listSongs(getContext(),preferences);

            // Prepare the uris, inputStreams and outputStreams
            Uri fileUriToCopy;
            InputStream inputStream;

            // The zip stuff
            byte[] tempBuff = new byte[1024];
            File backupFile = new File(getContext().getExternalFilesDir("Backup"),backupFilename);
            FileOutputStream outputStream;
            ZipOutputStream zipOutputStream = null;
            try {
                outputStream = new FileOutputStream(backupFile);
                zipOutputStream = new ZipOutputStream(outputStream);
            } catch (Exception e) {
                e.printStackTrace();
                error = true;
            }
            ZipEntry ze;

            // Now go through each one in turn and check if we want it backed up
            for (String file:allFiles) {
                // Get folder from file string
                try {
                    if (file.contains("/")) {
                        String thisFolder = file.substring(0, file.lastIndexOf("/"));
                        String thisFile = file.substring(file.lastIndexOf("/") + 1);
                        if (thisFile.isEmpty()) {
                            thisFile = "/";
                        }
                        // Now check if we want it added
                        for (String folder : checkedFolders) {
                            if (alive && folder.equals(thisFolder)) {
                                // Get the uri for this item
                                fileUriToCopy = storageAccess.getUriForItem(getContext(), preferences, "Songs", thisFolder, thisFile);
                                inputStream = storageAccess.getInputStream(getContext(), fileUriToCopy);
                                if (thisFolder.equals(getString(R.string.mainfoldername)) || thisFolder.equals("MAIN")) {
                                    ze = new ZipEntry(thisFile);
                                } else {
                                    ze = new ZipEntry(thisFolder + "/" + thisFile);
                                }
                                if (zipOutputStream != null) {
                                    // Update the screen
                                    requireActivity().runOnUiThread(() -> {
                                        String message = getString(R.string.processing) + ": " + file;
                                        myView.progressText.setText(message);
                                    });
                                    try {
                                        zipOutputStream.putNextEntry(ze);
                                        if (!ze.isDirectory()) {
                                            int len;
                                            while ((len = inputStream.read(tempBuff)) > 0) {
                                                zipOutputStream.write(tempBuff, 0, len);
                                            }
                                        }
                                        zipOutputStream.closeEntry();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        error = true;
                                    }
                                }
                                inputStream.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                zipOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                error = true;
            }

            // Update the view
            requireActivity().runOnUiThread(() -> {
                if (alive) {
                    myView.progressBar.setVisibility(View.GONE);
                    if (error) {
                        String message = getString(R.string.processing) + ": " + getString(R.string.error);
                        myView.progressText.setText(message);

                    } else {
                        myView.progressText.setText("");
                        myView.progressText.setVisibility(View.GONE);
                        exportBackup();
                    }
                    myView.createBackupFAB.setEnabled(true);
                }
            });
        };

        thread = new Thread(runnable);
        thread.start();
    }

    private void exportBackup() {
        File backupFile = new File(getContext().getExternalFilesDir("Backup"),backupFilename);
        Uri uri = FileProvider.getUriForFile(getContext(),"OpenSongAppFiles",backupFile);
        Intent intent = exportActions.exportBackup(getContext(),uri,backupFilename);
        requireActivity().startActivityForResult(Intent.createChooser(intent, getString(R.string.backup_info)), 12345);
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
    }

    private void killThread() {
        alive = false;
        if (thread!=null) {
            thread.interrupt();
            runnable = null;
            thread = null;
        }
    }
}