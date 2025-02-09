package com.garethevans.church.opensongtablet.export;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialTextView;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.setprocessing.ImportSetBundleSetFragment;
import com.garethevans.church.opensongtablet.setprocessing.ImportSetBundleSongFragment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class OpenSongSetBundle {

    // This class deals with OpenSongSetBundle files (.ossb)
    // A bundle contains a set file (ending with .osts) in the root folder (MAIN)
    // The set file contains references to the file locations (sub folders)
    // All OpenSong formatted songs are given the extension .ost
    // Any songs that are PDFs/images get copied as is
    // The OpenSongSetBundle will be a zip file with the .ossb extension

    private final String TAG = "OpenSongSetBundle";
    private final MainActivityInterface mainActivityInterface;
    private final String bundleExtension = ".ossb", setExtension = ".osts";
    private final String processing_string;
    private Uri bundleUri;
    private boolean alive = true;
    private MyMaterialTextView progressText;
    private ZipEntry ze;
    private File setBundleFolder, justChordsBundleFolder, setFile;
    private Uri setBundleUri;
    private File[] extractedFiles;
    private ImportSetBundleSetFragment importSetBundleSetFragment;
    private ImportSetBundleSongFragment importSetBundleSongFragment;
    private String setCurrent_backup="", setCurrentBeforeEdits_backup="", setCurrentLastName_backup="";
    private boolean importSuccess;

    public OpenSongSetBundle(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        processing_string = c.getString(R.string.processing);
        prepareSetBundleFolder();
    }

    public void setProgressText(MyMaterialTextView progressText) {
        this.progressText = progressText;
    }

    public void zipFiles(String bundleFilename, ArrayList<Uri> uris, boolean ossb) {
        // The file uris are in the passed array list, so we just zip them
        alive = true;
        InputStream inputStream;
        byte[] tempBuff = new byte[1024];

        bundleUri = mainActivityInterface.getStorageAccess().getUriForItem("Export",
                "", bundleFilename + bundleExtension);
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG +
                " Create OpenSongSetBundle/" + bundleFilename + bundleExtension + "  deleteOld=true");
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,
                bundleUri, null, "Export", "",
                bundleFilename + bundleExtension);

        if (ossb) {
            setBundleUri = bundleUri;
        }

        OutputStream outputStream;
        ZipOutputStream zipOutputStream = null;
        try {
            outputStream = mainActivityInterface.getStorageAccess().getOutputStream(bundleUri);
            zipOutputStream = new ZipOutputStream(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Now deal with the files
        for (Uri uri : uris) {
            if (alive && uri!=null) {
                // Get the uri for this item
                inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
                String thisFilename = mainActivityInterface.getStorageAccess().getFileNameFromUri(uri);
                ze = new ZipEntry(thisFilename);

                if (zipOutputStream != null) {
                    // Update the screen
                    mainActivityInterface.getMainHandler().post(() -> {
                        String message = processing_string + ": " + thisFilename;
                        if (progressText != null) {
                            progressText.setText(message);
                        }
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
                    }
                }

                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            if (zipOutputStream!=null) {
                zipOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("IOStreamConstructor")
    public void unzipFiles(Uri uri, String folder, String subfolder) {
        // Take the importUri and unzip the files in it
        // All songs will end with .osts, .ost, .pdf or (less likely) image files
        // The set will contain the correct location of the songs (subfolders)

        // Read in the set file to the current set
        mainActivityInterface.getSetActions().clearCurrentSet();

        // Extract the files to our app storage location and deal with them from there
        prepareSetBundleFolder();
        alive = true;

        if (mainActivityInterface.getWhattodo().equals("justchordsset")) {
            mainActivityInterface.getConvertJustChords().createSongsFromImportedSet();

        } else {
            byte[] buffer = new byte[1024];
            mainActivityInterface.getStorageAccess().emptyFileFolder(
                    mainActivityInterface.getStorageAccess().getAppSpecificFile(folder, subfolder, ""));
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
            try {
                while ((ze = zipInputStream.getNextEntry()) != null) {
                    File extractedFile = mainActivityInterface.getStorageAccess().getAppSpecificFile(folder, subfolder, ze.getName());
                    if (extractedFile != null) {
                        OutputStream outputStream = new FileOutputStream(extractedFile);
                        // Write the file
                        int count;
                        try {
                            while ((count = zipInputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, count);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                outputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // List the files we have extracted into this folder
        File extractedFolder = mainActivityInterface.getStorageAccess().getAppSpecificFile(folder,subfolder,"");
        extractedFiles = null;
        if (extractedFolder!=null && extractedFolder.exists()) {
            extractedFiles = extractedFolder.listFiles();
        }

        setFile = findSetFile();

        if (setFile!=null) {
            // Update the set name of the set tab
            if (importSetBundleSetFragment!=null) {
                importSetBundleSetFragment.setSetName(getSetFileName());
            }

            // Read in the set file to the current set
            mainActivityInterface.getSetActions().extractSetFile(Uri.fromFile(setFile), mainActivityInterface.getCurrentSet(),false);
        }

    }

    public File findSetFile() {
        if (extractedFiles != null) {
            for (File file:extractedFiles) {
                if (file.getName().endsWith(setExtension)) {
                    return file;
                }
            }
        }
        return null;
    }

    public File getSetFile() {
        return setFile;
    }
    public String getSetFileName() {
        return setFile.getName().replace(setExtension,"").
                replace(mainActivityInterface.getConvertJustChords().getExtension(),"").
                replace(bundleExtension,"");
    }

    private void prepareSetBundleFolder() {
        setBundleFolder = mainActivityInterface.getStorageAccess().
                getAppSpecificFile("SetBundle", "openSong", null);

        justChordsBundleFolder = mainActivityInterface.getStorageAccess().
                getAppSpecificFile("SetBundle", "justChords", null);

        // Make sure the directory is empty
        File[] files = setBundleFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                Log.d(TAG, "deleted old file:" + file.delete());
            }
        }

        files = justChordsBundleFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                Log.d(TAG, "deleted old file:" + file.delete());
            }
        }
    }

    public Uri getSetBundleUri() {
        return setBundleUri;
    }

    public void setImportFragments(ImportSetBundleSetFragment importSetBundleSetFragment,
                                   ImportSetBundleSongFragment importSetBundleSongFragment) {
        this.importSetBundleSetFragment = importSetBundleSetFragment;
        this.importSetBundleSongFragment = importSetBundleSongFragment;
    }

    public void setImportSetBundleSetFragment(ImportSetBundleSetFragment importSetBundleSetFragment) {
        this.importSetBundleSetFragment = importSetBundleSetFragment;
    }

    public void setImportSetBundleSongFragment(ImportSetBundleSongFragment importSetBundleSongFragment) {
        this.importSetBundleSongFragment = importSetBundleSongFragment;
    }

    public void resetVariables() {
        bundleUri = null;
        alive = false;
        progressText = null;
        ze = null;
        setBundleFolder = null;
        justChordsBundleFolder = null;
        setFile = null;
        extractedFiles = null;
        importSetBundleSongFragment = null;
        importSetBundleSetFragment = null;
        prepareSetBundleFolder();
        importSuccess = false;
    }

    public void getBackupOfCurrentSet() {
        // Store a backup of the currentSet preferences
        setCurrent_backup = mainActivityInterface.getPreferences().getMyPreferenceString("setCurrent","");
        setCurrentBeforeEdits_backup = mainActivityInterface.getPreferences().getMyPreferenceString("setCurrentBeforeEdits","");
        setCurrentLastName_backup = mainActivityInterface.getPreferences().getMyPreferenceString("setCurrentLastName","");
    }

    public void updateActualCurrentSet() {
        if (importSuccess) {
            mainActivityInterface.getCurrentSet().updateCurrentSetPreferences();
            mainActivityInterface.getSetActions().saveTheSet();
        } else {
            // Restore the backup
            mainActivityInterface.getPreferences().setMyPreferenceString("setCurrent", setCurrent_backup);
            mainActivityInterface.getPreferences().setMyPreferenceString("setCurrentBeforeEdits", setCurrentBeforeEdits_backup);
            mainActivityInterface.getPreferences().setMyPreferenceString("setCurrentLastName", setCurrentLastName_backup);
        }
        mainActivityInterface.getSetActions().parseCurrentSet();
    }

    public File getItemFile(String folder, String filename) {
        String actualFilename = folder + mainActivityInterface.getSetActions().getSetCategorySeparator() + filename;
        for (File file:extractedFiles) {
            if (file.getName().startsWith(actualFilename)) {
                return file;
            }
        }
        return null;
    }

    public void destroy() {
        alive = false;
    }

    public boolean getAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
