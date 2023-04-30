package com.garethevans.church.opensongtablet.backupandrestore;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsImportIosBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ImportIOSFragment extends Fragment {
    // This class imports OnSong/iOS songs from an OnSong .backup file
    // These files are zip files which contain 'loose' songs (pdf, etc) and an sqlite database

    private final String TAG = "ImportIOSFrag";
    private MainActivityInterface mainActivityInterface;
    private SettingsImportIosBinding myView;
    private InputStream inputStream;
    private ZipInputStream zipInputStream;
    private ZipEntry ze;
    private int zipProgress=0, filesCopied=0, totalSongs;
    private final byte[] buffer = new byte[8192];
    private boolean alive = true;
    private boolean allowOverwrite;
    private final String[] excludedFiles = new String[] {".sqlite3","Media","OnSong.preferences",".doc"};
    private String items, folder, onsong_import="", website_import_onsongbackup="", songs_string,
            error="", processing="", success="", error_song_not_saved="", file_exists="";
    private StringBuilder errorFiles, notOverwritten;
    private File onsongdbfile;
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(onsong_import);
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
        myView = SettingsImportIosBinding.inflate(inflater, container, false);

        prepareStrings();

        webAddress = website_import_onsongbackup;

        myView.filename.setText(mainActivityInterface.getImportFilename());

        // Attach the extended FAB to the scrollview
        myView.nestedScrollView.setExtendedFabToAnimate(myView.importButton);

        // Get folders
        getFolders();

        // Set up the views and begin getting info from the backup
        indexBackup();

        myView.importButton.setOnClickListener(view->doImport());

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            onsong_import = getString(R.string.onsong_import);
            website_import_onsongbackup = getString(R.string.website_import_onsongbackup);
            songs_string = getString(R.string.songs);
            error = getString(R.string.error);
            processing = getString(R.string.processing);
            success = getString(R.string.success);
            error_song_not_saved = getString(R.string.error_song_not_saved);
            file_exists = getString(R.string.file_exists);
        }
    }
    private void getFolders() {
        ArrayList<String> folders = mainActivityInterface.getSQLiteHelper().getFolders();
        if (!folders.contains("OnSong")) {
            folders.add("OnSong");
            Collections.sort(folders);
        }
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.folder, R.layout.view_exposed_dropdown_item, folders);
            myView.folder.setAdapter(exposedDropDownArrayAdapter);
        }
        myView.folder.setText("OnSong");
    }

    private void setupViews(boolean processing) {
        myView.progressEndMessage.setVisibility(View.GONE);
        if (processing) {
            myView.scrim.setVisibility(View.VISIBLE);
            myView.importButton.setVisibility(View.GONE);
        } else {
            myView.scrim.setVisibility(View.GONE);
            if (totalSongs>0) {
                myView.importButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void indexBackup() {
        // Run this as a new thread
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> setupViews(true));

            // Count 'loose' songs into zipContents
            // If a db is found, index that too
            countSongs();

            handler.post(() -> myView.filename.setHint(items));
            handler.post(() -> setupViews(false));
        });
    }

    private void countSongs() {
        alive = true;
        ArrayList<String> allZipItems = new ArrayList<>();
        totalSongs = 0;

        try {
            int countSQL = 0;
            inputStream = mainActivityInterface.getStorageAccess().
                    getInputStream(mainActivityInterface.getImportUri());
            zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));

            // Look for directories
            while ((ze = zipInputStream.getNextEntry()) != null) {
                boolean isExcluded = false;
                for (String exclude:excludedFiles) {
                    if (ze.getName().contains(exclude)) {
                        isExcluded = true;
                    }
                }
                if (ze.getName().equals("OnSong.sqlite3") || ze.getName().equals("OnSong.Backup.sqlite3")) {
                    File loc;
                    if (getContext()!=null) {
                        loc = new File(getContext().getExternalFilesDir("OnSong"), "Database");

                        if (!loc.mkdirs()) {
                            Log.d(TAG, "Database file already exists - ok");
                        }
                        onsongdbfile = new File(loc, "OnSong.sqlite3");
                        OutputStream outputStream = new FileOutputStream(onsongdbfile);
                        final BufferedOutputStream bout = new BufferedOutputStream(outputStream);
                        try {
                            while ((countSQL = zipInputStream.read(buffer)) != -1) {
                                bout.write(buffer, 0, countSQL);
                            }
                            bout.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                bout.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(onsongdbfile, null);
                        String query = "SELECT title FROM Song";
                        Cursor cursor;

                        try {
                            cursor = sqLiteDatabase.rawQuery(query, null);
                            countSQL = cursor.getCount();
                            cursor.close();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        sqLiteDatabase.close();
                    }
                } else if (!isExcluded) {
                    allZipItems.add(ze.getName());
                }
            }

            // Get the number of items
            int zipContents = allZipItems.size();
            totalSongs = zipContents;

            if (countSQL>0) {
                totalSongs = zipContents + countSQL;
            }
            if (totalSongs>0) {
                items = songs_string+ ": " + totalSongs;
            } else {
                items = error;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doImport() {
        setupViews(true);
        allowOverwrite = myView.overwrite.isChecked();
        folder = myView.folder.getText().toString();
        filesCopied = 0;
        zipProgress = 0;
        errorFiles = new StringBuilder();
        notOverwritten = new StringBuilder();
        myView.progressText.setText("");

        // Do this in a separate thread
        // Anything requiring the main UI checks this fragment is still alive
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());

            // Check the chosen folder (likely OnSong) exists
            // Create the folder (if it doesn't already exist).
            mainActivityInterface.getStorageAccess().createFolder("Songs","",folder,false);

            // Now process the backup file
            // Firstly copy the loose files across
            Uri uri;
            boolean uriExists;
            try {
                inputStream = mainActivityInterface.getStorageAccess().
                        getInputStream(mainActivityInterface.getImportUri());
                zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));

                while ((ze = zipInputStream.getNextEntry()) != null && alive) {
                    boolean isExcluded = false;
                    for (String exclude : excludedFiles) {
                        if (ze.getName().contains(exclude)) {
                            isExcluded = true;
                        }
                    }

                    if (!isExcluded) {
                        // Only copy if we allow overwrite or the file doesn't exist
                        uri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",folder, ze.getName());
                        uriExists = mainActivityInterface.getStorageAccess().uriExists(uri);
                        if (ze.getName()!=null && !ze.getName().isEmpty() && (allowOverwrite || !uriExists)) {
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+"Create Songs/"+folder+"/"+ze.getName()+"  deleteOld="+uriExists);
                            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(uriExists,
                                    uri,null,"Songs",folder,ze.getName());
                            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);

                            zipProgress++;
                            handler.post(() -> {
                                String name;
                                if (ze==null || ze.getName()==null) {
                                    name = "";
                                } else {
                                    name = ze.getName();
                                }
                                if (alive) {
                                    String message = processing + " (" + zipProgress + "/" + totalSongs + "):\n" + name;
                                    myView.progressText.setText(message);
                                }
                            });
                            if (writeTheFile(outputStream)) {
                                // Add to the actual database (persistent is created next index if it isn't already there)
                                mainActivityInterface.getSQLiteHelper().createSong(folder,ze.getName());
                                filesCopied++;
                            } else {
                                errorFiles.append(ze.getName()).append("\n");
                            }
                        } else {
                            notOverwritten.append(ze.getName()).append("\n");
                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Now we deal with extracting the songs from the database
            SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(onsongdbfile,null);

            String query = "SELECT title,content FROM Song";
            Cursor cursor;

            try {
                cursor = sqLiteDatabase.rawQuery(query, null);
                if (cursor.moveToFirst()) {

                    do {
                        zipProgress++;
                        try {
                            Song newSong = new Song();
                            // Set basic title/filename that may be improved from the content
                            newSong.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                            newSong.setFilename(newSong.getTitle());
                            newSong.setFolder(folder);
                            newSong.setLyrics(cursor.getString(cursor.getColumnIndexOrThrow("content")));

                            if (alive) {
                                String file = newSong.getFilename();
                                handler.post(() -> {
                                    try {
                                        String message = processing + " (" + zipProgress + "/" + totalSongs + "):\n" + file;
                                        myView.progressText.setText(message);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                            }

                            // Now extract the song as an OpenSong song and improve the title/filename
                            newSong = mainActivityInterface.getConvertOnSong().convertTextToTags(null, newSong);
                            newSong.setFilename(mainActivityInterface.getStorageAccess().safeFilename(newSong.getFilename()));
                            newSong.setFilename(newSong.getFilename().replace("/","_"));

                            uri = mainActivityInterface.getStorageAccess().getUriForItem("Songs", folder, newSong.getFilename());
                            uriExists = mainActivityInterface.getStorageAccess().uriExists(uri);

                            // Only proceed if we allow overwrite or the file doesn't exist
                            if (allowOverwrite || !uriExists) {

                                // Write the new song as OpenSong xml
                                if (newSong.getFilename()!=null && !newSong.getFilename().isEmpty()) {
                                    // Save the song.  This also calls lollipopCreateFile with 'true' to deleting old
                                    String xml = mainActivityInterface.getProcessSong().getXML(newSong);
                                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" updateFragment doStringWriteToFile Songs/"+folder+"/"+newSong.getFilename()+" with: "+xml);
                                    if (mainActivityInterface.getStorageAccess().doStringWriteToFile("Songs",
                                            folder, newSong.getFilename(), xml)) {
                                        // Add to the actual database
                                        mainActivityInterface.getSQLiteHelper().createSong(folder,newSong.getFilename());
                                        mainActivityInterface.getSQLiteHelper().updateSong(newSong);
                                        filesCopied++;
                                    } else {
                                        errorFiles.append(newSong.getTitle()).append("\n");
                                    }
                                } else {
                                    errorFiles.append(newSong.getTitle()).append("\n");
                                }
                            } else {
                                notOverwritten.append(newSong.getTitle()).append("\n");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } while (cursor.moveToNext() && alive);
                }
                cursor.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            sqLiteDatabase.close();

            if (alive) {
                handler.post(() -> {
                    String message  = songs_string+ ": " + totalSongs;
                    message += "\n\n" + success + ": " + filesCopied + " / " + totalSongs;
                    if (!errorFiles.toString().trim().isEmpty()) {
                        message += "\n" + error_song_not_saved + ": \n" + errorFiles.toString().trim();
                    }
                    if (!notOverwritten.toString().trim().isEmpty()) {
                        message += "\n\n" + file_exists + ": \n" + notOverwritten.toString().trim();
                    }
                    setupViews(false);
                    myView.progressEndMessage.setHint(message);
                    myView.progressEndMessage.setVisibility(View.VISIBLE);
                });
            }

            // Trigger a song menu update
            if (filesCopied>0) {
                if (alive) {
                    try {
                        mainActivityInterface.updateSongList();
                        mainActivityInterface.getShowToast().doIt(success);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        });
    }

    private boolean writeTheFile(OutputStream outputStream) {
        int count;
        boolean success = true;
        try {
            if (outputStream != null && alive) {
                while ((count = zipInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, count);
                }
            } else {
                success = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        alive = false;
    }
}
