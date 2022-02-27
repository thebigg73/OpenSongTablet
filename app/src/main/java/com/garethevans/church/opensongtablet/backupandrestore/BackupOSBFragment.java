package com.garethevans.church.opensongtablet.backupandrestore;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    boolean wantHighlighter, wantPersistentDB;

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
        myView = StorageBackupBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(getString(R.string.backup));

        setupViews();

        return myView.getRoot();
    }

    private void setupViews() {
        new Thread(() -> {
            // Hide the progress text view for new
            myView.progressText.setVisibility(View.GONE);

            // Get the default file name
            String deffilename = defaultFilename();
            requireActivity().runOnUiThread(() -> myView.backupName.setText(deffilename));

            // Get a list of available folders in the app
            ArrayList<String> folders = mainActivityInterface.getCommonSQL().getFolders(mainActivityInterface.getSQLiteHelper().getDB());

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

                myView.createBackupFAB.setText(getString(R.string.export));
                myView.createBackupFAB.setOnClickListener(v -> doSave());
            });

            // Set the persistent database to backup by default
            myView.includePersistentDB.setChecked(true);

        }).start();

        myView.nestedScrollView.setExtendedFabToAnimate(myView.createBackupFAB);
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
        runnable = () -> {

            // Get the backup file name and checked folders
            requireActivity().runOnUiThread(() -> {
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
                requireActivity().runOnUiThread(() -> {
                    String message = getString(R.string.processing) + ": " + SQLite.NON_OS_DATABASE_NAME;
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
        // Make sure we have an available backup folder
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Backups","",backupFilename);

        mainActivityInterface.getPreferences().setMyPreferenceInt("runssincebackup",0);

        Intent intent = mainActivityInterface.getExportActions().exportBackup(uri,backupFilename);
        startActivity(Intent.createChooser(intent,getString(R.string.backup_info)));
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
        if (thread!=null) {
            thread.interrupt();
            runnable = null;
            thread = null;
        }
    }
}