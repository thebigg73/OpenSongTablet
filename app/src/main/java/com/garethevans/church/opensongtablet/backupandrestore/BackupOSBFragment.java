package com.garethevans.church.opensongtablet.backupandrestore;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialCheckbox;
import com.garethevans.church.opensongtablet.databinding.StorageBackupBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.sqlite.SQLite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    // alive is written on the main thread and (optionally) read from the worker
    private volatile boolean alive = true;
    private String string_backup="", string_backup_info="", string_website_backup="", string_export="",
            string_processing="", string_mainfoldername="", string_error="", backup_complete_string="",
            backup_incomplete_string="", backup_error_string="", songs_string="", highlighter_string="",
            database_string="", not_added_string="";
    boolean wantHighlighter, wantPersistentDB;

    private String webAddress;

    // Everything the worker thread learns, handed back to the main thread in one go
    private static class BackupResult {
        int songsAdded = 0;
        int highlighterAdded = 0;
        boolean databaseAdded = false;
        final ArrayList<String> failed = new ArrayList<>();
        String fatal = null;   // set if the backup could not be completed
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(string_backup);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = StorageBackupBinding.inflate(inflater,container,false);

        // Tint the progressBar as the secondary color
        mainActivityInterface.getMyThemeColors().tintProgressBar(myView.progressBar);

        prepareStrings();

        webAddress = string_website_backup;

        setupViews();

        return myView.getRoot();
    }

    private void prepareStrings() {
        // To avoid context being null due to user cancelling async task
        if (getContext()!=null) {
            string_backup = getString(R.string.backup);
            string_backup_info = getString(R.string.backup_info);
            string_website_backup = getString(R.string.website_backup);
            string_export = getString(R.string.export);
            string_processing = getString(R.string.processing);
            string_mainfoldername = getString(R.string.mainfoldername);
            string_error = getString(R.string.error);
            backup_complete_string = getString(R.string.backup_complete);
            backup_incomplete_string = getString(R.string.backup_incomplete);
            backup_error_string = getString(R.string.backup_error);
            songs_string = getString(R.string.songs);
            highlighter_string = getString(R.string.highlight);
            database_string = getString(R.string.persistent_database);
            not_added_string = getString(R.string.not_added);
        }
    }

    private void setupViews() {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Hide the progress text view for new
            mainActivityInterface.getMainHandler().post(() -> {
                if (alive && myView!=null) {
                    myView.progressText.setVisibility(View.GONE);
                }
            });

            // Get the default file name
            String deffilename = defaultFilename();
            mainActivityInterface.getMainHandler().post(() -> {
                if (alive && myView!=null) {
                    myView.backupName.setText(deffilename);
                }
            });

            // Get a list of available folders in the app
            ArrayList<String> folders = mainActivityInterface.getSQLiteHelper().getFolders();

            // Create a new checkbox entry (default to ticked) for each one
            mainActivityInterface.getMainHandler().post(() -> {
                // The user may have left the page while the folders were being listed
                if (!alive || myView==null) {
                    return;
                }
                for (String folder:folders) {
                    MyMaterialCheckbox checkBox = new MyMaterialCheckbox(getContext());
                    checkBox.setText(folder);
                    checkBox.setTag(folder);
                    checkBox.setChecked(true);
                    checkBox.setPadding(16,32,16,32);
                    myView.foundFoldersListView.addView(checkBox);
                }

                myView.createBackupFAB.setText(string_export);
                if (getContext()!=null) {
                    myView.createBackupFAB.setIcon(VectorDrawableCompat.create(getResources(), R.drawable.database_export, getContext().getTheme()));
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
                MyMaterialCheckbox checkBox = (MyMaterialCheckbox) myView.foundFoldersListView.getChildAt(x);
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
            MyMaterialCheckbox checkBox = (MyMaterialCheckbox) myView.foundFoldersListView.getChildAt(x);
            if (checkBox!=null && checkBox.isChecked() && checkBox.getTag()!=null) {
                checkedFolders.add(checkBox.getTag().toString());
            }
        }
    }

    private void doSave() {
        if (myView == null) {
            return;
        }

        // Everything the worker needs is read HERE, on the main thread, before it starts.
        // (Previously the file name was set in a post() that the worker could outrun.)
        wantHighlighter = myView.includeHighlighter.isChecked();
        wantPersistentDB = myView.includePersistentDB.getChecked();
        getCheckedFolders();

        String typedName = "";
        if (myView.backupName.getText() != null) {
            typedName = myView.backupName.getText().toString().trim();
        }
        backupFilename = typedName.isEmpty() ? defaultFilename() : typedName;

        // Local copies, so the worker never depends on fields that can change under it
        final String filename = backupFilename;
        final ArrayList<String> folders = new ArrayList<>(checkedFolders);
        final boolean doHighlighter = wantHighlighter;
        final boolean doDatabase = wantPersistentDB;

        myView.progressText.setVisibility(View.VISIBLE);
        myView.progressBar.setVisibility(View.VISIBLE);
        myView.createBackupFAB.setEnabled(false);

        // execute() (unlike submit()) hands an uncaught exception to the default handler,
        // which ends the whole app. So NOTHING may escape this Runnable.
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            BackupResult result = new BackupResult();
            try {
                runBackup(filename, folders, doHighlighter, doDatabase, result);
            } catch (Throwable t) {
                Log.e(TAG, "Backup failed", t);
                result.fatal = t.toString();
                try {
                    mainActivityInterface.getStorageAccess().updateCrashLog("Backup failed\n" + t);
                } catch (Throwable ignored) {
                    // Nothing more we can do
                }
            }
            mainActivityInterface.getMainHandler().post(() -> showResult(result));
        });
    }

    // Runs on the worker thread. Deliberately does NOT stop if the user leaves the page:
    // stopping half way is what used to leave an incomplete (and unclosed) zip behind.
    private void runBackup(String filename, ArrayList<String> folders, boolean doHighlighter,
                           boolean doDatabase, BackupResult result) throws Exception {
        // Check the file list is up to date
        ArrayList<String> allFiles = mainActivityInterface.getStorageAccess().listSongs(false);
        if (allFiles == null) {
            allFiles = new ArrayList<>();
        }

        // Check the backup file exists
        Uri backupUri = mainActivityInterface.getStorageAccess().getUriForItem("Backups","",filename);
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" Create Backups/"+filename+"  deleteOld=true");
        mainActivityInterface.getStorageAccess().makeSureFileIsRegistered("Backups","",filename,true);

        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(backupUri);
        if (outputStream == null) {
            throw new IOException("Could not open Backups/" + filename + " for writing");
        }
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        byte[] tempBuff = new byte[8192];

        try {
            if (doDatabase) {
                addDatabase(zipOutputStream, tempBuff, result);
            }
            addSongs(zipOutputStream, allFiles, folders, tempBuff, result);
            if (doHighlighter) {
                addHighlighterFiles(zipOutputStream, folders, tempBuff, result);
            }
        } finally {
            // Always runs, even after an unexpected exception, so the central directory
            // is written and the .osb is a readable zip.
            try {
                zipOutputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "Closing zip failed", e);
                result.fatal = "Could not finish the backup file";
                mainActivityInterface.getStorageAccess().updateCrashLog("Close outputstream\n" + e);
            }
        }
    }

    private void addDatabase(ZipOutputStream zos, byte[] buf, BackupResult result) {
        postProgress(string_processing + ": " + SQLite.NON_OS_DATABASE_NAME);
        try {
            // Copy the current appDB to the userDB (the one in Settings)
            Log.d(TAG, "DB copied: " + mainActivityInterface.getNonOpenSongSQLiteHelper().copyUserDatabase());
            Uri uriDB = mainActivityInterface.getStorageAccess().getUriForItem("Settings", "", SQLite.NON_OS_DATABASE_NAME);
            if (addEntry(zos, SQLite.NON_OS_DATABASE_NAME, uriDB, buf)) {
                result.databaseAdded = true;
            } else {
                result.failed.add(SQLite.NON_OS_DATABASE_NAME);
            }
        } catch (Exception e) {
            Log.e(TAG, "Adding database failed", e);
            result.failed.add(SQLite.NON_OS_DATABASE_NAME);
            mainActivityInterface.getStorageAccess().updateCrashLog("Adding database\n" + e);
        }
    }

    private void addSongs(ZipOutputStream zos, ArrayList<String> allFiles, ArrayList<String> folders,
                          byte[] buf, BackupResult result) {
        for (String file : allFiles) {
            try {
                if (file == null || !file.contains("/")) {
                    continue;
                }
                // Get folder from file string
                int split = file.lastIndexOf("/");
                String thisFolder = file.substring(0, split);
                String thisFile = file.substring(split + 1);

                // Now check if we want it added
                if (!folders.contains(thisFolder)) {
                    continue;
                }

                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Songs", thisFolder, thisFile);
                String entryName;
                if (thisFolder.equals(string_mainfoldername) || thisFolder.equals("MAIN")) {
                    entryName = thisFile;
                } else {
                    entryName = thisFolder + "/" + thisFile;
                }
                postProgress(string_processing + ": " + file);
                if (addEntry(zos, entryName, uri, buf)) {
                    result.songsAdded++;
                } else {
                    result.failed.add(file);
                }
            } catch (Exception e) {
                Log.e(TAG, "Adding song failed: " + file, e);
                result.failed.add(String.valueOf(file));
                mainActivityInterface.getStorageAccess().updateCrashLog("Adding item " + file + "\n" + e);
            }
        }
    }

    private void addHighlighterFiles(ZipOutputStream zos, ArrayList<String> folders,
                                     byte[] buf, BackupResult result) {
        ArrayList<String> highlighterFiles = mainActivityInterface.getStorageAccess().listFilesInFolder("Highlighter", "");
        if (highlighterFiles == null) {
            return;
        }
        for (String file : highlighterFiles) {
            // Each file is handled on its own: one bad file is skipped and reported,
            // it can no longer take the rest (or the whole app) down.
            try {
                // A highlighter file name is folder_song..., so anything without an
                // underscore can't be one of ours (e.g. a .nomedia). Ignore it quietly.
                if (file == null || !file.contains("_")) {
                    continue;
                }

                ArrayList<String> bits = mainActivityInterface.getProcessSong().getInfoFromHighlighterFilename(file);
                if (bits == null || bits.size() < 2 || bits.get(0) == null) {
                    // Looks like an overlay but can't be understood: report it
                    result.failed.add("Highlighter/" + file);
                    continue;
                }

                // The folder is the first bit of the filename
                String thisFolder = bits.get(0).replace("_", "/");

                // Now check if we want it added
                if (!folders.contains(thisFolder)) {
                    continue;
                }

                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter", "", file);
                postProgress(string_processing + ": " + file);
                if (addEntry(zos, "_Highlighter/" + file, uri, buf)) {
                    result.highlighterAdded++;
                } else {
                    result.failed.add("Highlighter/" + file);
                }
            } catch (Exception e) {
                Log.e(TAG, "Adding highlighter file failed: " + file, e);
                result.failed.add("Highlighter/" + file);
                mainActivityInterface.getStorageAccess().updateCrashLog("Writing highlighter item " + file + "\n" + e);
            }
        }
    }

    // Copies one file into the zip. Returns false (and writes nothing) if the file can't be opened.
    // If reading fails part way through, the entry is closed so the zip stays valid
    // (that one file will be truncated, and is reported as failed).
    private boolean addEntry(ZipOutputStream zos, String entryName, Uri uri, byte[] buf) {
        InputStream in = null;
        boolean entryOpen = false;
        try {
            in = mainActivityInterface.getStorageAccess().getInputStream(uri);
            if (in == null) {
                return false;
            }
            zos.putNextEntry(new ZipEntry(entryName));
            entryOpen = true;
            int len;
            while ((len = in.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            zos.closeEntry();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Adding entry failed: " + entryName, e);
            mainActivityInterface.getStorageAccess().updateCrashLog("Adding item " + entryName + "\n" + e);
            if (entryOpen) {
                try {
                    zos.closeEntry();
                } catch (Exception ignored) {
                    // The zip itself is in trouble; close() later will report it
                }
            }
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignored) {
                    // Nothing useful to do
                }
            }
        }
    }

    private void postProgress(String message) {
        mainActivityInterface.getMainHandler().post(() -> {
            if (alive && myView != null) {
                myView.progressText.setText(message);
            }
        });
    }

    // Main thread. If the user has left the page there is nothing to update; the finished
    // backup is simply sitting in the Backups folder.
    private void showResult(BackupResult result) {
        if (!alive || myView == null) {
            return;
        }
        myView.progressBar.setVisibility(View.GONE);
        myView.createBackupFAB.setEnabled(true);
        myView.progressText.setVisibility(View.VISIBLE);
        myView.progressText.setText(buildSummary(result));

        // Only offer to share a backup that was completed
        if (result.fatal == null) {
            exportBackup();
        }
    }

    private String buildSummary(BackupResult result) {
        StringBuilder sb = new StringBuilder();
        if (result.fatal != null) {
            sb.append(string_error).append(": ").append(backup_error_string);
        } else if (result.failed.isEmpty()) {
            sb.append(backup_complete_string);
        } else {
            sb.append(backup_incomplete_string);
        }
        sb.append("\n").append(songs_string).append(": ").append(result.songsAdded);
        if (wantHighlighter) {
            sb.append("\n").append(highlighter_string).append(": ").append(result.highlighterAdded);
        }
        if (wantPersistentDB) {
            sb.append("\n").append(database_string).append(": ").append(result.databaseAdded ? "yes" : "no");
        }
        if (!result.failed.isEmpty()) {
            sb.append("\n").append(not_added_string).append(" (").append(result.failed.size()).append("):");
            int shown = Math.min(result.failed.size(), 5);
            for (int i = 0; i < shown; i++) {
                sb.append("\n  ").append(result.failed.get(i));
            }
            if (result.failed.size() > shown) {
                sb.append("\n  ...");
            }
        }
        return sb.toString();
    }

    private void exportBackup() {
        if (getContext()!=null) {
            try {
                // Make sure we have an available backup folder
                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Backups", "", backupFilename);
                InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);

                // Copy the backup file into the local app files/backups folder (for sharing permission)
                File backupFolder = mainActivityInterface.getStorageAccess().getAppSpecificFile("files", "backups", null);
                File file = new File(backupFolder, backupFilename);
                OutputStream outputStream = new FileOutputStream(file);
                Log.d(TAG, "Copy:" + mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream));

                mainActivityInterface.getPreferences().setMyPreferenceInt("runssincebackup", 0);

                // For safe sharing
                Uri newUri = FileProvider.getUriForFile(getContext(), "com.garethevans.church.opensongtablet.fileprovider", file);

                Intent intent = mainActivityInterface.getExportActions().exportBackup(newUri, backupFilename);
                startActivity(Intent.createChooser(intent, string_backup_info));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
    }
}