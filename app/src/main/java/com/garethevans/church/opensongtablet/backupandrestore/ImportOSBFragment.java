package com.garethevans.church.opensongtablet.backupandrestore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.StorageBackupBinding;
import com.garethevans.church.opensongtablet.importsongs.WebDownload;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.sqlite.SQLite;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ImportOSBFragment extends Fragment {

    // This fragment allows users to import an OSB backup file
    // It uses the same layout as the export fragment, but changes the appopriate text

    private MainActivityInterface mainActivityInterface;
    private StorageBackupBinding myView;
    private final String TAG = "ImportOSBFragment";

    private String importFilename;
    private Uri importUri;
    private ArrayList<String> foundFolders;
    private ArrayList<String> checkedFolders;
    private ArrayList<String> allZipItems;
    private boolean error, hasPersistentDB, hasHighlighterNotes;

    private ExecutorService executorService;
    private boolean alive = true;

    private InputStream inputStream;
    private ZipInputStream zipInputStream;
    private ZipEntry ze;
    private OutputStream outputStream;
    private int zipContents;
    private int zipProgress;
    private int item;
    private String message;
    private boolean canoverwrite;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = StorageBackupBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(getString(R.string.import_basic));

        myView.nestedScrollView.setExtendedFabToAnimate(myView.createBackupFAB);

        // Set up helpers
        setupHelpers();

        return myView.getRoot();
    }

    private void setupHelpers() {
        // Initialise the launcher
        initialiseLauncher();
        if (mainActivityInterface.getWhattodo().equals("importChurchSample")) {
            importSample("https://drive.google.com/uc?export=download&id=0B-GbNhnY_O_lbVY3VVVOMkc5OGM&resourcekey=0-BaKEvQTwaRk4pln4UzfGSQ","Church.osb");
        } else if (mainActivityInterface.getWhattodo().equals("importBandSample")) {
            importSample("https://drive.google.com/uc?export=download&id=0B-GbNhnY_O_leDR5bFFjRVVxVjA&resourcekey=0-vmzRuYNgGSCG6N1dmpg3dQ","Band.osb");
        } else {
            importFilename = mainActivityInterface.getImportFilename();
            importUri = mainActivityInterface.getImportUri();
            // Set up the correct values
            setupValues();

            // Find the folders
            findFolders();
        }
    }

    private void initialiseLauncher() {
        // Initialise the launcher
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                try {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri contentUri = data.getData();
                        String importFilename = mainActivityInterface.getStorageAccess().getFileNameFromUri(importUri);
                        if (importFilename.endsWith(".osb")) {
                            myView.importTitle.setText(importFilename);
                            importUri = contentUri;
                            setupValues();
                            findFolders();
                        } else {
                            myView.backupName.setText(getString(R.string.unknown));
                            myView.progressText.setText(getString(R.string.unknown));
                            importUri = null;
                            foundFolders = null;
                            myView.foundFoldersListView.removeAllViews();
                            okToLoad(); // Will be false!
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupValues() {
        myView.importTitle.setText(getString(R.string.import_osb));
        myView.backupName.setText(importFilename);
        myView.backupName.setEnabled(true);
        myView.backupName.setFocusable(false);
        myView.overWrite.setVisibility(View.VISIBLE);
        myView.foundFoldersListView.removeAllViews();
        myView.includeHighlighter.setVisibility(View.GONE);
        myView.includePersistentDB.setVisibility(View.GONE);
    }

    private void findFolders() {
        // We need to parse the .osb (zip) file to extract a list of folders it contains as a new thread
        error = false;
        allZipItems = new ArrayList<>();

        hasHighlighterNotes = false;
        hasPersistentDB = false;

        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
               if (alive) {
                   myView.progressBar.setVisibility(View.VISIBLE);
                   myView.progressText.setVisibility(View.VISIBLE);
                   myView.progressText.setText(getString(R.string.processing));
               }
            });

            try {
                zipContents = 0;
                inputStream = mainActivityInterface.getStorageAccess().getInputStream(importUri);
                zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));

                // Add the main folder
                foundFolders = new ArrayList<>();
                if (alive) {
                    foundFolders.add(getString(R.string.mainfoldername));
                }

                // Look for directories
                while ((ze = zipInputStream.getNextEntry()) != null) {
                    if (ze.getName().contains("/")) {
                        allZipItems.add(ze.getName());
                    } else {
                        if (alive) {
                            allZipItems.add(getString(R.string.mainfoldername) + "/" + ze.getName());
                        }
                    }
                    if (ze.isDirectory() || ze.getName().contains("/")) {
                        String thisfolder = ze.getName();
                        if (thisfolder.contains("/")) {
                            thisfolder = thisfolder.substring(0, thisfolder.lastIndexOf("/"));
                        }
                        if (thisfolder.equals("_Highlighter")) {
                            hasHighlighterNotes = true;
                        }
                        // Only add it if we don't already have it
                        if (!foundFolders.contains(thisfolder)) {
                            // Only add if it isn't already in the array
                            foundFolders.add(thisfolder);
                        }
                    } else if (ze.getName().equals(SQLite.NON_OS_DATABASE_NAME)) {
                        hasPersistentDB = true;
                    }
                }

                // Get the number of items
                zipContents = allZipItems.size();

            } catch (Exception e) {
                e.printStackTrace();
                error = true;
            } finally {
                if (zipInputStream != null) {
                    try {
                        zipInputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        error = true;
                    }
                }
            }

            handler.post(() -> {
                if (error && alive) {
                    myView.progressText.setText(getString(R.string.error));
                } else if (alive) {
                    message = getString(R.string.songs) + ": " + zipContents;
                    myView.progressText.setText(message);

                    // Update the found folders
                    // Sort the folders
                    Collator coll = Collator.getInstance(mainActivityInterface.getLocale());
                    coll.setStrength(Collator.SECONDARY);
                    Collections.sort(foundFolders, coll);

                    for (String folder : foundFolders) {
                        if (!folder.equals("_Highlighter")) {
                            CheckBox checkBox = new CheckBox(getContext());
                            checkBox.setText(folder);
                            checkBox.setTag(folder);
                            checkBox.setChecked(true);
                            checkBox.setPadding(16, 32, 16, 32);
                            myView.foundFoldersListView.addView(checkBox);
                            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                int songs = getCurrentSongs();
                                message = getString(R.string.songs) + ": " + songs;
                                myView.progressText.setText(message);
                            });
                        }
                    }
                    // If the _Highligher folder was found, give the user the option
                    if (hasHighlighterNotes) {
                        myView.includeHighlighter.setVisibility(View.VISIBLE);
                        myView.includeHighlighter.setChecked(true);
                    } else {
                        myView.includeHighlighter.setVisibility(View.GONE);
                        myView.includeHighlighter.setChecked(false);
                    }
                    // If the NonOpenSongSongs.db file was found, give the user the option
                    if (hasPersistentDB) {
                        myView.includePersistentDB.setVisibility(View.VISIBLE);
                        myView.includePersistentDB.setChecked(true);
                    } else {
                        myView.includePersistentDB.setVisibility(View.GONE);
                        myView.includePersistentDB.setChecked(false);
                    }
                    myView.createBackupFAB.setOnClickListener(v -> doImport());
                    myView.backupName.setOnClickListener(v -> changeBackupFile());
                }
                myView.progressBar.setVisibility(View.GONE);
            });
            okToLoad();
        });
    }

    private int getCurrentSongs() {
        // This matches how many songs are in the chosen folders
        zipContents = 0;
        getCheckedFolders();
        if (checkedFolders.size()>0) {
            for (String item:allZipItems) {
                if (item.contains("/")) {
                    item = item.substring(0,item.lastIndexOf("/"));
                } else {
                    item = getString(R.string.mainfoldername);
                }
                for (String checked:checkedFolders) {
                    if (checked.equals(item)) {
                        zipContents++;
                    }
                }
            }
            return zipContents;
        } else {
            return 0;
        }
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

    private void changeBackupFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        String[] mimetypes = {"application/zip","application/octet-stream","application/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activityResultLauncher.launch(intent);

    }

    private void doImport() {
        // Get the folders we've selected
        getCheckedFolders();
        zipProgress = 0;
        item = 0;

        // The actual importing runs in a new thread
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            inputStream = mainActivityInterface.getStorageAccess().getInputStream(importUri);
            zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));

            handler.post(() -> {
                if (alive) {
                    myView.progressBar.setVisibility(View.VISIBLE);
                    canoverwrite = myView.overWrite.isChecked();
                    myView.createBackupFAB.setEnabled(false);
                    mainActivityInterface.getMyActionBar().setHomeButtonEnabled(false);
                }
            });

            // Go through the checked folders and check they exist on the local storage
            // If not, create them
            for (String folder : checkedFolders) {
                handler.post(() -> {
                    if (alive) {
                        message = getString(R.string.folder) + ": " + folder;
                        myView.progressText.setText(message);
                    }
                });
                if (alive) {
                    mainActivityInterface.getStorageAccess().createFile(DocumentsContract.Document.MIME_TYPE_DIR,
                            "Songs", folder, "");
                }
            }

            // Now deal with the zip entries
            try {
                byte[] buffer = new byte[8192];
                while ((ze = zipInputStream.getNextEntry()) != null) {
                    item ++;
                    if (!ze.isDirectory()) {
                        // Get a uri for the song
                        Uri file_uri = null;
                        boolean exists = false;
                        boolean wantit = false;
                        String filename;
                        String filefolder = "";
                        if (alive) {
                            if (ze.getName().startsWith("_Highlighter")) {
                                file_uri = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter", "", ze.getName().replace("_Highlighter/",""));
                            } else if (ze.getName().equals(SQLite.NON_OS_DATABASE_NAME)) {
                                file_uri = mainActivityInterface.getStorageAccess().getUriForItem("Settings","",SQLite.NON_OS_DATABASE_NAME);
                            } else {
                                file_uri = mainActivityInterface.getStorageAccess().getUriForItem("Songs", "", ze.getName());
                                if (alive) {
                                    filefolder = getString(R.string.mainfoldername);
                                }
                            }
                            if (ze.getName().contains("/")) {
                                filefolder = ze.getName().substring(0, ze.getName().lastIndexOf("/"));
                            }

                            // If the file exists and we have allowed overwriting, or it doesn't exist and it is in the checked folders, write it
                            // Because the database will be in the MAIN folder, we need to check that
                            exists = mainActivityInterface.getStorageAccess().uriExists(file_uri);
                            wantit = (ze.getName().equals(SQLite.NON_OS_DATABASE_NAME) && myView.includePersistentDB.getChecked()) ||
                                    (!ze.getName().equals(SQLite.NON_OS_DATABASE_NAME) && checkedFolders.contains(filefolder)) ||
                                    (filefolder.equals("_Highlighter") && myView.includeHighlighter.isChecked());
                        }
                        if (alive && wantit && (!exists || canoverwrite)) {
                            // We want it and either it doesn't exist, or we've selected overwriting
                            // Update the disply
                            zipProgress++;
                            handler.post(() -> {
                                String name;
                                if (ze==null || ze.getName()==null) {
                                    name = "";
                                } else {
                                    name = ze.getName();
                                }
                                if (alive) {
                                    message = getString(R.string.processing) + " (" + zipProgress + "/" + zipContents + "):\n" + name;
                                    myView.progressText.setText(message);
                                }
                            });

                            // Make sure the file exists (might be non-existent)
                            Log.d(TAG,"alive: "+alive+"   exists:"+exists+"   name:"+ze.getName());
                            if (!exists && alive) {
                                Log.d(TAG,ze.getName());
                                Log.d(TAG,"file_uri="+file_uri);
                                if (ze.getName().contains("_Highlighter/")) {
                                    filename = ze.getName().replace("_Highlighter/","");
                                    Log.d(TAG,"filename="+filename);
                                    Log.d(TAG,"Into highlighter: folder="+filefolder+"  filename="+filename+"  file_uri="+file_uri);
                                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(
                                            false, file_uri,null,"Highlighter","",filename);
                                } else if (ze.getName().equals(SQLite.NON_OS_DATABASE_NAME)) {
                                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(
                                            false, file_uri,null,"Settings","",SQLite.NON_OS_DATABASE_NAME);
                                } else {
                                    filename = ze.getName().replace(filefolder, "").replace("/", "");
                                    Log.d(TAG,"Into songs: folder="+filefolder+"  filename="+filename+"  file_uri="+file_uri);
                                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(
                                            true, file_uri, null, "Songs", filefolder, filename);
                                }
                            }
                            if (alive) {
                                outputStream = mainActivityInterface.getStorageAccess().getOutputStream(file_uri);
                            }

                            // Write the file
                            int count;
                            try {
                                if (outputStream != null && alive) {
                                    while ((count = zipInputStream.read(buffer)) != -1) {
                                        outputStream.write(buffer, 0, count);
                                    }
                                } else {
                                    error = true;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    if (outputStream != null) {
                                        outputStream.close();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    error = true;
                                }
                            }
                            if (error) {
                                error = false;
                                if (alive) {
                                    handler.post(() -> myView.progressText.setText(getString(R.string.error)));
                                }
                            }
                        } else {
                            if (alive) {
                                handler.post(() -> {
                                    message = getString(R.string.connections_searching) + " (" + item + "/" + allZipItems.size() + ")";
                                    myView.progressText.setText(message);
                                });
                            }
                        }
                    }
                    zipInputStream.closeEntry();
                }

                if (alive) {
                    handler.post(() -> {
                        mainActivityInterface.getMyActionBar().setHomeButtonEnabled(true);
                        myView.progressBar.setVisibility(View.GONE);
                        myView.progressText.setText("");
                        myView.progressText.setVisibility(View.GONE);
                        myView.createBackupFAB.setEnabled(true);
                        mainActivityInterface.closeDrawer(true);

                        // Update the songid file
                        ArrayList<String> songids = mainActivityInterface.getStorageAccess().listSongs();
                        mainActivityInterface.getStorageAccess().writeSongIDFile(songids);

                        // Update the song index
                        if (myView.includePersistentDB.getChecked()) {
                            mainActivityInterface.getNonOpenSongSQLiteHelper().copyUserDatabase();
                        }
                        mainActivityInterface.getSQLiteHelper().insertFast();
                        mainActivityInterface.setFullIndexRequired(true);
                        mainActivityInterface.fullIndex();

                        // Navigate back to the home
                        mainActivityInterface.navHome();
                    });
                }

            } catch (Exception e) {
                // Likely the user navigated away before the process completed
                e.printStackTrace();
                mainActivityInterface.getMyActionBar().setHomeButtonEnabled(true);
                if (getContext()!=null && alive) {
                    handler.post(() -> {
                        myView.progressText.setText(getString(R.string.error));
                        myView.progressBar.setVisibility(View.GONE);
                        myView.createBackupFAB.setEnabled(true);
                    });
                }
            }
        });
    }

    private void importSample(String url, String filename) {
        importFilename = filename;
        // Get the WebDownload
        WebDownload webDownload = mainActivityInterface.getWebDownload();
        // Run this in a new thread
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            if (alive) {
                handler.post(() -> myView.progressBar.setVisibility(View.VISIBLE));
            }
            String[] messages = webDownload.doDownload(getContext(), url,filename);
            if (alive) {
                handler.post(() -> myView.progressBar.setVisibility(View.GONE));
            }
            if (messages[1]==null) {
                // There was a problem
                mainActivityInterface.getShowToast().doIt(messages[0]);
                if (getActivity()!=null) {
                    handler.post(() -> mainActivityInterface.navigateToFragment(null, R.id.import_graph));
                }

            } else {
                mainActivityInterface.setImportFilename(filename);

                mainActivityInterface.setImportUri(Uri.parse(messages[1]));
                importUri = Uri.parse(messages[1]);
                if (alive) {
                    handler.post(() -> {
                        // Set up the correct values
                        setupValues();

                        // Find the folders
                        findFolders();

                        // Check we are okay to load
                        okToLoad();
                    });
                }
            }
        });
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
        try {
            if (executorService!=null) {
                executorService.shutdownNow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void okToLoad() {
        if (myView.importTitle.getText()!=null && foundFolders!=null && foundFolders.size()>0 &&
                !myView.importTitle.getText().toString().isEmpty() && importUri!=null) {
            // Udpate as post to keep on UI thread
            myView.createBackupFAB.post(() -> myView.createBackupFAB.setVisibility(View.VISIBLE));
        } else {
            myView.createBackupFAB.post(() -> myView.createBackupFAB.setVisibility(View.GONE));
        }
    }
}
