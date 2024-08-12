package com.garethevans.church.opensongtablet.backupandrestore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.StorageBackupBinding;
import com.garethevans.church.opensongtablet.importsongs.WebDownload;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.sqlite.SQLite;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ImportOSBFragment extends Fragment {

    // This fragment allows users to import an OSB backup file
    // It uses the same layout as the export fragment, but changes the appopriate text

    private MainActivityInterface mainActivityInterface;
    private StorageBackupBinding myView;
    private final String TAG = "ImportOSBFragment";
    private String importFilename, message, import_basic="", website_restore="", unknown="",
            import_osb="", processing="", mainfoldername="", error_string="", songs_string="",
            folder_string="", connections_searching="";
    private Uri importUri;
    private ArrayList<String> foundFolders, checkedFolders, allZipItems;
    private boolean error, hasPersistentDB, hasHighlighterNotes, alive = true, canoverwrite;
    private InputStream inputStream;
    private ZipInputStream zipInputStream;
    private ZipEntry ze;
    private OutputStream outputStream;
    private int zipContents, zipProgress, item;
    private File tempDBFile;

    private ActivityResultLauncher<Intent> activityResultLauncher;
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(import_basic);
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

        prepareStrings();

        webAddress = website_restore;

        myView.nestedScrollView.setExtendedFabToAnimate(myView.createBackupFAB);

        // Set up helpers
        setupHelpers();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            import_basic = getString(R.string.import_basic);
            website_restore = getString(R.string.website_restore);
            unknown = getString(R.string.unknown);
            import_osb = getString(R.string.import_osb);
            processing = getString(R.string.processing);
            mainfoldername = getString(R.string.mainfoldername);
            error_string = getString(R.string.error);
            songs_string = getString(R.string.songs);
            folder_string = getString(R.string.folder);
            connections_searching = getString(R.string.connections_searching);
        }
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
        if (activityResultLauncher == null) {
            // Initialise the launcher
            activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri contentUri = data.getData();
                            importFilename = mainActivityInterface.getStorageAccess().getFileNameFromUri(contentUri);
                            if (importFilename.endsWith(".osb")) {
                                importUri = contentUri;
                                setupValues();
                                findFolders();
                            } else {
                                setFilename(unknown);
                                myView.backupName.setText(unknown);
                                myView.progressText.setText(unknown);
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
    }

    private void setFilename(String thisFilename) {
        importFilename = thisFilename;
    }

    private void setupValues() {
        myView.importTitle.setText(import_osb);
        myView.backupName.setEnabled(true);
        myView.backupName.setText(importFilename);
        myView.backupName.setFocusable(false);
        myView.overWrite.setVisibility(View.VISIBLE);
        myView.foundFoldersListView.removeAllViews();
        myView.includeHighlighter.setVisibility(View.GONE);
        myView.includePersistentDB.setVisibility(View.GONE);

        myView.selectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // If we select all, scroll throught the found folders and select them
            // Otherwise, unselect them all
            if (myView.foundFoldersListView.getChildCount()>0) {
                for (int i=0; i<myView.foundFoldersListView.getChildCount(); i++) {
                    ((CheckBox)myView.foundFoldersListView.getChildAt(i)).setChecked(isChecked);
                }
            }
        });
    }

    private void findFolders() {
        // We need to parse the .osb (zip) file to extract a list of folders it contains as a new thread
        error = false;
        allZipItems = new ArrayList<>();

        hasHighlighterNotes = false;
        hasPersistentDB = false;

        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            mainActivityInterface.getMainHandler().post(() -> {
               if (alive) {
                   myView.progressBar.setVisibility(View.VISIBLE);
                   myView.progressText.setVisibility(View.VISIBLE);
                   myView.progressText.setText(processing);
               }
            });

            try {
                zipContents = 0;
                inputStream = mainActivityInterface.getStorageAccess().getInputStream(importUri);
                zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));

                // Add the main folder
                foundFolders = new ArrayList<>();
                if (alive) {
                    foundFolders.add(mainfoldername);
                }

                // Look for directories
                while ((ze = zipInputStream.getNextEntry()) != null) {
                    if (ze.getName().contains("/")) {
                        allZipItems.add(ze.getName());
                    } else {
                        if (alive) {
                            allZipItems.add(mainfoldername + "/" + ze.getName());
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

            mainActivityInterface.getMainHandler().post(() -> {
                if (error && alive) {
                    myView.progressText.setText(error_string);
                } else if (alive) {
                    message = songs_string + ": " + zipContents;
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
                                message = songs_string + ": " + songs;
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
        if (!checkedFolders.isEmpty()) {
            for (String item:allZipItems) {
                if (item.contains("/")) {
                    item = item.substring(0,item.lastIndexOf("/"));
                } else {
                    item = mainfoldername;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,
                    mainActivityInterface.getStorageAccess().getUriForItem("Backups","",""));
        }
        intent.addFlags(mainActivityInterface.getStorageAccess().getAddReadUriFlags());
        activityResultLauncher.launch(intent);

    }

    private void doImport() {
        // Get the folders we've selected
        getCheckedFolders();
        zipProgress = 0;
        item = 0;

        // The actual importing runs in a new thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            inputStream = mainActivityInterface.getStorageAccess().getInputStream(importUri);
            zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));

            mainActivityInterface.getMainHandler().post(() -> {
                if (alive) {
                    myView.progressBar.setVisibility(View.VISIBLE);
                    canoverwrite = myView.overWrite.isChecked();
                    myView.createBackupFAB.setEnabled(false);
                    mainActivityInterface.allowNavigationUp(false);
                }
            });

            // Go through the checked folders and check they exist on the local storage
            // If not, create them
            StringBuilder stringBuilder = new StringBuilder();
            for (String folder : checkedFolders) {
                mainActivityInterface.getMainHandler().post(() -> {
                    if (alive) {
                        message = folder_string + ": " + folder;
                        myView.progressText.setText(message);
                    }
                });
                if (alive) {
                    // Because the folder could have subdirectories, we need to start at the beginning
                    ArrayList<String> allBits = new ArrayList<>();
                    if (folder.contains("/")) {
                        String[] bits = folder.split("/");
                        for (String bit:bits) {
                            stringBuilder.append(bit);
                            allBits.add(stringBuilder.toString());
                            stringBuilder.append("/");
                        }
                    } else {
                        allBits.add(folder);
                    }

                    DocumentFile df = mainActivityInterface.getStorageAccess().getSongsDF();
                    for (String folderBit:allBits) {
                        if (df!=null && !folderBit.equals(mainfoldername) && !folderBit.equals("MAIN")) {
                            DocumentFile subdf = df.findFile(folderBit);
                            if (subdf == null || !subdf.exists()) {
                                df.createDirectory(folderBit);
                                df = df.findFile(folderBit);
                                if (df != null) {
                                    stringBuilder.append(TAG).append(" create folders ").append(df.getUri()).append("\n");
                                } else {
                                    stringBuilder.append(TAG).append(" failed to create folder: ").append(folderBit).append("\n");
                                }
                            } else {
                                stringBuilder.append(TAG).append(" folder already exists: ").append(folderBit).append("\n");
                            }
                        }
                    }

                }
            }
            if (!stringBuilder.toString().isEmpty()) {
                mainActivityInterface.getStorageAccess().updateFileActivityLog("\n" + stringBuilder + "\n");
            }

            // Now deal with the zip entries
            stringBuilder = new StringBuilder();
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
                        boolean isDB = false;
                        if (alive) {
                            if (ze.getName().startsWith("_Highlighter")) {
                                file_uri = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter", "", ze.getName().replace("_Highlighter/",""));
                            } else if (ze.getName().equals(SQLite.NON_OS_DATABASE_NAME)) {
                                // Put the database into our app folder
                                tempDBFile = mainActivityInterface.getStorageAccess().getAppSpecificFile("Database","","importedDB.db");
                                //tempDBFile = new File(requireContext().getExternalFilesDir("Database"), "importedDB.db");
                                file_uri = Uri.fromFile(tempDBFile);
                                isDB = true;
                            } else {
                                file_uri = mainActivityInterface.getStorageAccess().getUriForItem("Songs", "", ze.getName());
                                if (alive) {
                                    filefolder = mainfoldername;
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
                        if (alive && wantit && (!exists || canoverwrite || isDB) && !ze.getName().startsWith(".")) {
                            // We want it and either it doesn't exist, or we've selected overwriting
                            // Update the display
                            zipProgress++;
                            mainActivityInterface.getMainHandler().post(() -> {
                                String name;
                                if (ze==null || ze.getName()==null) {
                                    name = "";
                                } else {
                                    name = ze.getName();
                                }
                                if (alive) {
                                    message = processing + " (" + zipProgress + "/" + zipContents + "):\n" + name;
                                    myView.progressText.setText(message);
                                }
                            });

                            // Make sure the file exists (might be non-existent)
                            if (!exists && alive) {
                                if (ze.getName().contains("_Highlighter/")) {
                                    filename = ze.getName().replace("_Highlighter/","");
                                    stringBuilder.append("\n").append(TAG).append(" Create Highlighlighter/").append(filename);
                                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(
                                            false, file_uri,null,"Highlighter","",filename);
                                } else if (ze.getName().equals(SQLite.NON_OS_DATABASE_NAME)) {
                                    // the file_uri is actually pointing to the app folder as we will save it there then SQL insert or replace in the existing DB
                                    Uri final_file_uri = mainActivityInterface.getStorageAccess().getUriForItem("Settings","",SQLite.NON_OS_DATABASE_NAME);
                                    stringBuilder.append("\n").append(TAG).append(" Create Settings/").append(SQLite.NON_OS_DATABASE_NAME);
                                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(
                                            false, final_file_uri,null,"Settings","",SQLite.NON_OS_DATABASE_NAME);
                                } else {
                                    filename = ze.getName().replace(filefolder, "").replace("/", "");
                                    stringBuilder.append("\n").append(TAG).append(" Create Songs/").append(filefolder).append("/").append(filename);
                                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(
                                            true, file_uri, null, "Songs", filefolder, filename);
                                }
                            }
                            if (alive) {
                                if (ze.getName().equals(SQLite.NON_OS_DATABASE_NAME)) {
                                    outputStream = new FileOutputStream(tempDBFile);
                                } else {
                                    outputStream = mainActivityInterface.getStorageAccess().getOutputStream(file_uri);
                                }
                            }

                            // Write the file
                            int count;
                            try {
                                if (outputStream != null && alive) {
                                    while ((count = zipInputStream.read(buffer)) != -1) {
                                        outputStream.write(buffer, 0, count);
                                    }
                                } else {
                                    Log.d(TAG,"error = "+ze.getName());
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
                                    mainActivityInterface.getMainHandler().post(() -> myView.progressText.setText(error_string));
                                }
                            }
                        } else {
                            if (alive) {
                                mainActivityInterface.getMainHandler().post(() -> {
                                    message = connections_searching + " (" + item + "/" + allZipItems.size() + ")";
                                    myView.progressText.setText(message);
                                });
                            }
                        }
                    }

                    zipInputStream.closeEntry();
                }
                if (!stringBuilder.toString().isEmpty()) {
                    mainActivityInterface.getStorageAccess().updateFileActivityLog("\n" + stringBuilder + "\n");
                }

                if (alive) {
                    // Deal with the database
                    if (hasPersistentDB && tempDBFile != null) {
                        // We will use SQL to merge the database to our existing one
                        // If we are allowing overwrite, we use REPLACE, if not we use INSERT OR IGNORE
                        String dbPath = tempDBFile.getPath();
                        mainActivityInterface.getNonOpenSongSQLiteHelper().importDB(dbPath,canoverwrite);
                        tempDBFile = null;
                    }

                    mainActivityInterface.getMainHandler().post(() -> {
                        mainActivityInterface.allowNavigationUp(true);
                        myView.progressBar.setVisibility(View.GONE);
                        myView.progressText.setText("");
                        myView.progressText.setVisibility(View.GONE);
                        myView.createBackupFAB.setEnabled(true);
                        mainActivityInterface.closeDrawer(true);

                        // Update the songid file
                        ArrayList<String> songids = mainActivityInterface.getStorageAccess().listSongs(false);
                        mainActivityInterface.getStorageAccess().writeSongIDFile(songids);

                        // Update the song index
                        if (myView.includePersistentDB.getChecked()) {
                            mainActivityInterface.getNonOpenSongSQLiteHelper().copyUserDatabase();
                        }
                        mainActivityInterface.getSQLiteHelper().insertFast();
                        mainActivityInterface.getSongListBuildIndex().setIndexRequired(true);
                        mainActivityInterface.getSongListBuildIndex().setFullIndexRequired(false);
                        // The index will be rebuilt on returning to the Performance/Presenter fragment

                        // Navigate back to the home
                        mainActivityInterface.navHome();
                    });
                }

            } catch (Exception e) {
                // Likely the user navigated away before the process completed
                e.printStackTrace();
                mainActivityInterface.allowNavigationUp(true);
                if (getContext()!=null && alive) {
                    mainActivityInterface.getMainHandler().post(() -> {
                        myView.progressText.setText(error_string);
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
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
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
    }

    private void okToLoad() {
        if (myView.importTitle.getText()!=null && foundFolders!=null && !foundFolders.isEmpty() &&
                !myView.importTitle.getText().toString().isEmpty() && importUri!=null) {
            // Udpate as post to keep on UI thread
            myView.createBackupFAB.post(() -> myView.createBackupFAB.setVisibility(View.VISIBLE));
        } else {
            myView.createBackupFAB.post(() -> myView.createBackupFAB.setVisibility(View.GONE));
        }
    }
}
