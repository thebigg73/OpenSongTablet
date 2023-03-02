package com.garethevans.church.opensongtablet.backupandrestore;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.StorageBackupBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.sqlite.SQLite;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupOSBFragment extends Fragment {
    // This Fragment allows the user to create an OpenSongApp backup file
    // Show the user which folders are detected and can be backed up.
    // By default it will be all of them
    // It will also include the persistent database and highlighter notes

    private StorageBackupBinding myView;
    private MainActivityInterface mainActivityInterface;

    private final String TAG = "BackupOSB";
    private String backupFilename;
    private ArrayList<String> checkedFolders;
    private boolean error = false;
    private boolean alive = true;
    private String string_backup="", string_backup_info="", string_website_backup="", string_export="",
            string_processing="", string_mainfoldername="", string_error="";
    boolean wantHighlighter, wantPersistentDB;

    private ExecutorService executorService;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = StorageBackupBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(string_backup);
        mainActivityInterface.updateToolbarHelp(string_website_backup);

        setupStrings();
        setupViews();

        return myView.getRoot();
    }

    private void setupStrings() {
        // To avoid context being null due to user cancelling async task
        if (getContext()!=null) {
            string_backup = getString(R.string.backup);
            string_backup_info = getString(R.string.backup_info);
            string_website_backup = getString(R.string.website_backup);
            string_export = getString(R.string.export);
            string_processing = getString(R.string.processing);
            string_mainfoldername = getString(R.string.mainfoldername);
            string_error = getString(R.string.error);
        }
    }

    private void setupViews() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());

            // Hide the progress text view for new
            handler.post(() -> myView.progressText.setVisibility(View.GONE));

            // Get the default file name
            String deffilename = defaultFilename();
            handler.post(() -> myView.backupName.setText(deffilename));

            // Get a list of available folders in the app
            ArrayList<String> folders = mainActivityInterface.getCommonSQL().getFolders(mainActivityInterface.getSQLiteHelper().getDB());

            // Create a new checkbox entry (default to ticked) for each one
            handler.post(() -> {
                for (String folder:folders) {
                    CheckBox checkBox = new CheckBox(getContext());
                    checkBox.setText(folder);
                    checkBox.setTag(folder);
                    checkBox.setChecked(true);
                    checkBox.setPadding(16,32,16,32);
                    myView.foundFoldersListView.addView(checkBox);
                }

                myView.createBackupFAB.setText(string_export);
                if (getContext()!=null) {
                    myView.createBackupFAB.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.database_export));
                }
                myView.createBackupFAB.setOnClickListener(v -> doSave());

                // Set the persistent database to backup by default
                myView.includePersistentDB.setChecked(true);
            });


        });

        myView.nestedScrollView.setExtendedFabToAnimate(myView.createBackupFAB);

        myView.selectAll.setChecked(true);
        myView.selectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (int x=0; x<myView.foundFoldersListView.getChildCount(); x++) {
                CheckBox checkBox = (CheckBox) myView.foundFoldersListView.getChildAt(x);
                checkBox.setChecked(isChecked);
            }
        });
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
        wantHighlighter = myView.includeHighlighter.isChecked();
        wantPersistentDB = myView.includePersistentDB.getChecked();

        getCheckedFolders();

        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());

            // Get the backup file name and checked folders
            handler.post(() -> {
                if (alive) {
                    // Check the backup file name
                    if (myView.backupName.getText()!=null) {
                        backupFilename = myView.backupName.getText().toString();
                    } else {
                        backupFilename = defaultFilename();
                    }
                    // Make the progressText Visible
                    myView.progressText.setVisibility(View.VISIBLE);
                    myView.progressBar.setVisibility(View.VISIBLE);
                    myView.createBackupFAB.setEnabled(false);
                }
            });

            // Check the file list is up to date
            ArrayList<String> allFiles = mainActivityInterface.getStorageAccess().listSongs();

            // Prepare the uris, inputStreams and outputStreams
            Uri fileUriToCopy;
            InputStream inputStream;

            // The zip stuff
            byte[] tempBuff = new byte[1024];
            // Check the temp folder exists
            Uri backupUri = mainActivityInterface.getStorageAccess().getUriForItem("Backups","",backupFilename);
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" Create Backups/"+backupFilename+"  deleteOld=true");
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,
                    backupUri,null,"Backups","",backupFilename);
            OutputStream outputStream;
            ZipOutputStream zipOutputStream = null;
            try {
                outputStream = mainActivityInterface.getStorageAccess().getOutputStream(backupUri);
                zipOutputStream = new ZipOutputStream(outputStream);
            } catch (Exception e) {
                e.printStackTrace();
                error = true;
            }
            ZipEntry ze;

            // Now go through each one in turn and check if we want it backed up
            // Include the database
            if (wantPersistentDB) {
                // Copy the current appDB to the userDB (the one in Settings)
                handler.post(() -> {
                    String message = string_processing + ": " + SQLite.NON_OS_DATABASE_NAME;
                    myView.progressText.setText(message);
                });
                Log.d(TAG,"DB copied: "+mainActivityInterface.getNonOpenSongSQLiteHelper().
                        copyUserDatabase());
                Uri uriDB = mainActivityInterface.getStorageAccess().getUriForItem("Settings","", SQLite.NON_OS_DATABASE_NAME);
                InputStream dbInputStream = mainActivityInterface.getStorageAccess().getInputStream(uriDB);
                ze = new ZipEntry(SQLite.NON_OS_DATABASE_NAME);
                try {
                    if (zipOutputStream != null) {
                        zipOutputStream.putNextEntry(ze);
                        if (!ze.isDirectory()) {
                            int len;
                            while ((len = dbInputStream.read(tempBuff)) > 0) {
                                zipOutputStream.write(tempBuff, 0, len);
                            }
                        }
                        zipOutputStream.closeEntry();
                        dbInputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Now deal with the songs
            for (String file:allFiles) {
                // Get folder from file string
                try {
                    if (file.contains("/")) {
                        String thisFolder = file.substring(0, file.lastIndexOf("/"));
                        String thisFile = file.replace(thisFolder+"/","");
                        // Now check if we want it added
                        for (String folder : checkedFolders) {
                            if (alive && folder.equals(thisFolder)) {
                                // Get the uri for this item
                                fileUriToCopy = mainActivityInterface.getStorageAccess().getUriForItem("Songs", thisFolder, thisFile);
                                inputStream = mainActivityInterface.getStorageAccess().getInputStream(fileUriToCopy);
                                if (thisFolder.equals(string_mainfoldername) || thisFolder.equals("MAIN")) {
                                    ze = new ZipEntry(thisFile);
                                } else {
                                    ze = new ZipEntry(thisFolder + "/" + thisFile);
                                }
                                if (zipOutputStream != null) {
                                    // Update the screen
                                    handler.post(() -> {
                                        String message = string_processing + ": " + file;
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

            // Now add the matching highligher files
            if (wantHighlighter) {
                ArrayList<String> highlighterFiles = mainActivityInterface.getStorageAccess().listFilesInFolder("Highlighter", "");
                for (String file : highlighterFiles) {
                    ArrayList<String> bits = mainActivityInterface.getProcessSong().getInfoFromHighlighterFilename(file);
                    String thisFolder = bits.get(0).replace("_","/");
                    String fileName = bits.get(1);
                    Log.d(TAG, "thisFolder: "+thisFolder+"  fileName="+fileName);
                    // The folder is the first bit of the filename
                    if (file.contains("_")) {

                        // Now check if we want it added
                        for (String wantedFolder : checkedFolders) {
                            if (alive && wantedFolder.equals(thisFolder)) {
                                try {
                                    // Get the uri for this item
                                    fileUriToCopy = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter", "", file);
                                    inputStream = mainActivityInterface.getStorageAccess().getInputStream(fileUriToCopy);
                                    ze = new ZipEntry("_Highlighter/" + file);
                                    if (zipOutputStream != null) {
                                        // Update the screen
                                        handler.post(() -> {
                                            String message = string_processing + ": " + file;
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
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }

            try {
                if (zipOutputStream!=null) {
                    zipOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                error = true;
            }

            // Update the view
            if (alive) {
                handler.post(() -> {
                    if (alive) {
                        myView.progressBar.setVisibility(View.GONE);
                        if (error) {
                            String message = string_processing + ": " + string_error;
                            myView.progressText.setText(message);

                        } else {
                            myView.progressText.setText("");
                            myView.progressText.setVisibility(View.GONE);
                            exportBackup();
                        }
                        myView.createBackupFAB.setEnabled(true);
                    }
                });
            }
        });
    }

    private void exportBackup() {
        // Make sure we have an available backup folder
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Backups","",backupFilename);

        mainActivityInterface.getPreferences().setMyPreferenceInt("runssincebackup",0);

        Intent intent = mainActivityInterface.getExportActions().exportBackup(uri,backupFilename);
        startActivity(Intent.createChooser(intent,string_backup_info));
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
        alive = false;
        if (executorService!=null) {
            try {
                executorService.shutdownNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}