package com.garethevans.church.opensongtablet.filemanagement;

// This class deals with accessing the app storage
// There are two sets of methods
// Newer versions of Android (Lollipop and later) will use content uris (SAF)
// These are based on the ACTION_OPEN_DOCUMENT_TREE location
// KitKat and below will use file uris based on built in folder chooser (File)
// The older method is now deprecated and legacy storage flag is ignored in Android 11+

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class StorageAccess {

    public StorageAccess(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        fileWriteLog = mainActivityInterface.getPreferences().getMyPreferenceBoolean("fileWriteLog",true);
        fileViewLog  = mainActivityInterface.getPreferences().getMyPreferenceBoolean("fileViewLog",true);
    }

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private boolean fileWriteLog = true, fileViewLog = true;
    public final String appFolder = "OpenSong";
    private final String TAG = "StorageAccess";
    private final String[] rootFolders = {"Backgrounds", "Export", "Fonts", "Highlighter", "Images", "Media",
            "Notes", "OpenSong Scripture", "Pads", "Profiles", "Received", "Scripture",
            "Sets", "Settings", "Slides", "Songs", "Variations", "Backups"};
    private final String[] cacheFolders = {"Backgrounds/_cache", "Images/_cache", "Notes/_cache",
            "OpenSong Scripture/_cache", "Scripture/_cache", "Slides/_cache", "Variations/_cache"};
    private Uri uriTree = null, uriTreeHome = null; // This is the home folder.  Set as required from preferences.

    private DocumentFile uriTreeDF, songsDF;

    // These are used primarily on start up to initialise stuff
    private String getStoragePreference() {
        return mainActivityInterface.getPreferences().getMyPreferenceString("uriTree", null);
    }


    // Used to decide on the best storage method (using tree or not)
    public boolean lollipopOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    // This gets the uri for the uriTreeHome (the uri of the ..../OpenSong folder
    // This may or may not be the same as uriTree as this could be the parent folder
    public Uri homeFolder(Uri uri) {
        // The user specified a storage folder when they started the app
        // However, this might not be the OpenSong folder, but the folder containing it
        // This function is called once when the app starts and fixes that
        // We need a reference to the OpenSong as the root for the app

        String uriTree_String;
        if (uri == null) {
            // No uri has been sent, so retrieve it from the preferences
            uriTree_String = getStoragePreference();
        } else {
            // Use the uri sent as the base start point
            uriTree_String = uri.toString();
        }

        try {
            if (uriTree_String != null) {
                if (lollipopOrLater()) {
                    uri = homeFolder_SAF(uriTree_String);
                } else {
                    uri = homeFolder_File(uriTree_String);
                }
            }

        } catch (Exception e) {
            // Could be called if the uri stored is for a different version of Android. e.g. after upgrade
            uri = null;
        }
        return uri;
    }
    public boolean uriTreeValid(Uri uri) {
        if (uri == null) {
            return false;
        } else {
            if (lollipopOrLater()) {
                DocumentFile df = DocumentFile.fromTreeUri(c, uri);
                if (df != null) {
                    return df.canWrite();
                } else {
                    return false;
                }
            } else {
                if (uri.getPath() != null) {
                    File f = new File(uri.getPath());
                    return f.canWrite();
                } else {
                    return false;
                }
            }
        }
    }
    private Uri homeFolder_SAF(String uriTree_String) {
        // When using a document tree, the uri needed for DocumentsContract is more complex than the uri chosen.
        // Create a document file to get a contract uri
        Uri uri = Uri.parse(uriTree_String);
        if (uri != null) {
            DocumentFile df = documentFileFromRootUri(uri, uriTree_String);
            if (df == null || !df.exists()) {
                uri = null;
            } else {
                uri = df.getUri();
                uriTree = uri;
            }

            // If uri doesn't end with /OpenSong/, fix that
            if (uri != null && uri.getLastPathSegment() != null && !uri.getLastPathSegment().endsWith("OpenSong")) {
                String s = uri.toString();
                s = s + "%2F"+appFolder;
                uri = Uri.parse(s);
            }
        }
        uriTreeHome = uri;
        return uri;
    }
    private Uri homeFolder_File(String uriTree_String) {
        File f;
        // Now get rid of the file start as it'll get added again later
        uriTree_String = uriTree_String.replace("file://", "");

        if (!uriTree_String.endsWith(appFolder) && !uriTree_String.endsWith(appFolder+"/")) {
            uriTree_String = uriTree_String + "/" + appFolder +"/";
            uriTree_String = uriTree_String.replace("//"+appFolder+"/", "/"+appFolder+"/");


            f = new File(uriTree_String);
            if (f.mkdirs()) {
                Log.d(TAG, "Created or identified OpenSong folder");
            }
        } else {
            f = new File(uriTree_String);
        }
        return Uri.fromFile(f);
    }

    public void setUriTree(Uri uriTree) {
        this.uriTree = uriTree;
    }
    public void setUriTreeHome(Uri uriTreeHome) {
        this.uriTreeHome = uriTreeHome;
    }

    // Sort the initial default folders and files needed when the app installs changes storage location
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public String createOrCheckRootFolders(Uri uri) {
        // uri if the uriTree.  If not sent/null, load from preferences
        if (uri == null) {
            uri = Uri.parse(mainActivityInterface.getPreferences().getMyPreferenceString("uriTree", ""));
        }

        if (lollipopOrLater()) {
            return createOrCheckRootFolders_SAF(uri);
        } else {
            return createOrCheckRootFolders_File();
        }
    }
    private String createOrCheckRootFolders_File() {
        File rootFolder = new File(stringForFile(""));
        // Go through the main folders and try to create
        for (String folder : rootFolders) {
            File nf = new File(rootFolder, folder);
            if (!nf.exists()) {
                if (!nf.mkdirs()) {
                    Log.d(TAG, "Error creating folder: " + folder);
                }
            }
        }

        // Go through the sub folders and try to create
        for (String subfolder : cacheFolders) {
            File nf = new File(rootFolder, subfolder);
            if (!nf.exists()) {
                if (!nf.mkdirs()) {
                    Log.d(TAG, "Error creating subfolder: " + subfolder);
                }
            }
        }

        uriTreeDF = DocumentFile.fromFile(rootFolder);
        songsDF = DocumentFile.fromFile(new File(rootFolder,"Songs"));
        copyAssets();
        return "Success";
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private String createOrCheckRootFolders_SAF(Uri uri) {
        uriTreeHome = homeFolder(uri);

        // Prepare for the fileWriteActivity.txt log
        StringBuilder stringBuilder = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        stringBuilder.append("--------------------\nBootup:")
                .append(sdf.format(new Date())).append("\n").append(TAG);
        // Look to see if uriTreeHome actually exists
        DocumentFile documentFile = DocumentFile.fromTreeUri(c,uri);
        if (documentFile!=null && documentFile.exists() && documentFile.getUri().toString().endsWith(appFolder)) {
            // This is the correct uriTreeHome
            uriTreeHome = documentFile.getUri();
        } else if (documentFile!=null) {
            DocumentFile openSongDf = documentFile.findFile(appFolder);
            if (openSongDf == null) {
                openSongDf = documentFile.createDirectory(appFolder);
                if (openSongDf!=null) {
                    uriTreeHome = openSongDf.getUri();
                    documentFile = openSongDf;
                }
            } else {
                uriTreeHome = openSongDf.getUri();
                documentFile = openSongDf;
            }
        }

        // Keep a reference to the OpenSongs/ documentFile
        uriTreeDF = documentFile;

        setUriTreeHome(uriTreeHome);

        // Go through the main folders and try to create them
        // We have a reference to the OpenSong/ folder now from above
        if (documentFile!=null) {
            stringBuilder.append("\nuriTreeHome:").append(documentFile.getUri());
            for (String folder : rootFolders) {
                try {
                    DocumentFile dfFolder = documentFile.findFile(folder);
                    stringBuilder.append("\nLooking for:").append(folder);
                    if (dfFolder==null || !dfFolder.exists()) {
                        stringBuilder.append(" - Not found, so create");
                        DocumentFile thisDF = documentFile.createDirectory(folder);
                        if (thisDF!=null) {
                            stringBuilder.append(":").append(thisDF.getUri());
                        } else {
                            stringBuilder.append(" - failed as thisDF==null");
                        }

                    } else {
                        stringBuilder.append(" - Found, so skip:").append(dfFolder.getUri());
                    }

                } catch (Exception e) {
                    Log.d(TAG, folder + " error creating");
                    stringBuilder.append(" - error creating\n");
                }
            }

            // Now we know we have the main folders, get a permanent reference to OpenSong/Songs
            songsDF = uriTreeDF.findFile("Songs");
            stringBuilder.append("\nRoot folders done, now check _cache folder");

            // Now for the cache folders
            for (String folder : cacheFolders) {
                String[] bits = folder.split("/");
                stringBuilder.append("\nChecking cache folder:").append(folder);
                try {
                    // The main folder exists (dealt with above).  Get a reference
                    DocumentFile dfFolder = documentFile.findFile(bits[0]);
                    if (dfFolder!=null) {
                        DocumentFile dfSubFolder = dfFolder.findFile(bits[1]);
                        if (dfSubFolder==null || !dfSubFolder.exists()) {
                            stringBuilder.append(" - parent folder exists, so create _cache");
                            DocumentFile thisDF = dfFolder.createDirectory(bits[1]);
                            if (thisDF!=null) {
                                stringBuilder.append(" - created:").append(thisDF.getUri());
                            } else {
                                stringBuilder.append(" - tried to create, but failed:").append(bits[1]);
                            }
                        } else {
                            stringBuilder.append(" - _cache folder already exists, so skip:").append(dfSubFolder.getUri());
                        }
                    } else {
                        stringBuilder.append(" - parent folder didn't exist, so can't create _cache");
                    }

                } catch (Exception e2) {
                    Log.d(TAG, "Error creating cache: " + folder);
                    stringBuilder.append("\nError creating cache:").append(folder);
                }
            }

            // Update the log
            updateFileActivityLog(stringBuilder.toString());

            // Now copy the assets if they aren't already there
            copyAssets();
            return "Success";
        } else {
            // Update the log
            stringBuilder.append("\nuriTreeHome not set/working");
            updateFileActivityLog(stringBuilder.toString());
            return "Failure";
        }
    }
    private void copyAssets() {
        try {
            // Copies the background assets
            AssetManager assetManager = c.getAssets();
            String[] files = new String[2];
            files[0] = "backgrounds/OpenSongApp_Background.png";
            files[1] = "backgrounds/OpenSongApp_Logo.png";
            Uri backgrounds = getUriForItem("Backgrounds", "", "");

            DocumentFile df = documentFileFromUri(backgrounds, backgrounds.getPath());
            for (String filename : files) {
                String filetocopy = filename.replace("backgrounds/", "");
                // See if they are already there first
                Uri uritocheck = getUriForItem("Backgrounds", "", filetocopy);
                if (!uriExists(uritocheck)) {
                    if (lollipopOrLater()) {
                        DocumentsContract.createDocument(c.getContentResolver(), backgrounds, "image/png", filetocopy);
                    } else {
                        df.createFile("image/png", filetocopy);
                    }
                    if (uritocheck != null) {
                        OutputStream out = getOutputStream(uritocheck);
                        try {
                            InputStream in = assetManager.open(filename);
                            copyFile(in, out);
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

    public DocumentFile getSongsDF() {
        return songsDF;
    }


    // Deal with parsing, creating, editing file and folder names
    // This gets the File location for the app as a String (for appending).  PreLollipop only
    public String[] niceUriTree(Uri uri) {
        if (lollipopOrLater()) {
            return niceUriTree_SAF(uri, new String[]{"", ""});
        } else {
            return niceUriTree_File(uri, new String[]{"", ""});
        }
    }
    private String[] niceUriTree_SAF(Uri uri, String[] storageDetails) {
        // storageDetails is currently empty, but will be [0]=extra info  [1]=nice location
        if (storageDetails == null) {
            storageDetails = new String[2];
        }
        storageDetails[0] = "";
        storageDetails[1] = "";

        if (uri != null) {
            try {
                storageDetails[1] = uri.getPath();

                if (storageDetails[1] == null) {
                    storageDetails[1] = "";
                }

                // When not an internal path (more patterns may be needed) indicate as external
                if (!storageDetails[1].contains("/tree/primary")) {
                    storageDetails[0] = c.getString(R.string.storage_ext);
                }

                // The  storage location getPath is likely something like /tree/primary:/document/primary:/OpenSong
                // This is due to the content using a document contract
                if (storageDetails[1].contains("primary:")) {
                    storageDetails[1] = storageDetails[1].substring(storageDetails[1].lastIndexOf("primary"));
                }

                if (storageDetails[1].contains(":") && !storageDetails[1].endsWith(":")) {
                    storageDetails[1] = "/" + storageDetails[1].substring(storageDetails[1].lastIndexOf(":") + 1);
                } else {
                    storageDetails[1] = "/" + storageDetails[1];
                }
                storageDetails[1] = storageDetails[1].replace("//", "/");

                // Add the 'OpenSong' bit to the end if it isn't there already
                if (!storageDetails[1].endsWith("/" + appFolder)) {
                    storageDetails[1] += "/" + appFolder;
                }

            } catch (Exception e) {
                e.printStackTrace();
                storageDetails[1] = "" + uri;
            }


            // If we have a path try to give extra info of a 'songs' count
            try {
                ArrayList<String> songIds = listSongs();

                // Only items that don't end with / are songs!
                int count = 0;
                for (String s : songIds) {
                    if (!s.endsWith("/")) {
                        count++;
                    }
                }

                if (storageDetails[0].length() > 0) {
                    storageDetails[0] = storageDetails[0] + ", ";
                }
                storageDetails[0] = "(" + storageDetails[0] + count + " " + c.getString(R.string.songs) + ")";
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return storageDetails;
    }
    public String[] niceUriTree_File(Uri uri, String[] storageDetails) {
        storageDetails[1] = uri.getPath().replace("file://", "");
        if (!storageDetails[1].startsWith("/")) {
            storageDetails[1] = "/" + storageDetails[1];
        }
        storageDetails[1] = "¬" + storageDetails[1];
        storageDetails[1] = storageDetails[1].
                replace("¬/storage/sdcard0/", "/").
                replace("¬/storage/emulated/0/", "/").
                replace("¬/storage/emulated/legacy/", "/").
                replace("¬/storage/self/primary/", "/");

        // IV - Handle others paths as 'External'
        if (storageDetails[1].startsWith("¬/storage/")) {
            storageDetails[1] = storageDetails[1].substring(10);
            storageDetails[1] = storageDetails[1].substring(storageDetails[1].indexOf("/")) + " (" +
                    c.getString(R.string.storage_ext) + " " + storageDetails[1].substring(0, storageDetails[1].indexOf("/")) + ")";
        }

        // Add the 'OpenSong' bit to the end if it isn't there already
        if (!storageDetails[1].endsWith("/" + appFolder)) {
            storageDetails[1] += "/" + appFolder;
        }

        // Prepare an arraylist for any song folders so we can count the items
        ArrayList<File> foldersToIndex = new ArrayList<>();
        if (uri.getPath() != null) {
            foldersToIndex.add(new File(uri.getPath()));
        }
        int count = 0;
        try {
            for (int x = 0; x < foldersToIndex.size(); x++) {
                File[] fs = foldersToIndex.get(x).listFiles();
                if (fs != null) {
                    for (File ff : fs) {
                        if (ff.isDirectory()) {
                            foldersToIndex.add(ff);
                        } else {
                            count++;
                        }
                    }
                }
            }
            storageDetails[0] = "(" + count + " " + c.getString(R.string.songs) + ")";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return storageDetails;
    }
    public Uri fixBadStorage(Uri uri) {
        if (uri != null) {
            String text = uri.getPath();
            // IV: Exclude raw storage
            if (text.startsWith("/tree/raw:") || text.startsWith("/tree/msd:")) {
                uri = null;
                uriTree = null;
                uriTreeHome = null;
            }
        }
        return uri;
    }
    private String stringForFile(String folderpath) {
        if (uriTreeHome == null) {
            String uriTreeHome_String = mainActivityInterface.getPreferences().getMyPreferenceString("uriTreeHome", "");
            uriTreeHome = Uri.parse(uriTreeHome_String);
        }

        if (uriTreeHome != null) {
            String file = uriTreeHome.getPath();
            if (file != null && !file.endsWith(appFolder)) {
                file = file + "/" + appFolder;
            }
            return file + "/" + folderpath;
        } else {
            return folderpath;
        }
    }
    private String[] fixFoldersAndFiles(String folder, String subfolder, String filename) {
        // This fixes incorrect folders that would cause problems
        String[] returnvals = new String[3];
        if (subfolder!=null && subfolder.startsWith("**")) {
            // Change out translated versions
            subfolder = subfolder.replace(c.getString(R.string.variation),"Variation");
            //subfolder = subfolder.replace(c.getString(R.string.slide),"Slides");
            subfolder = subfolder.replace(c.getString(R.string.scripture),"Scripture");
            //subfolder = subfolder.replace(c.getString(R.string.note),"Note");
            // This is used when custom slides are created as part of a set, making the folder look more obvious
            subfolder = subfolder.replace("**", "../");
            subfolder = subfolder.replace("Images", "Images/_cache");
            subfolder = subfolder.replace("Slides", "Slides/_cache");
            subfolder = subfolder.replace("Scripture", "Scripture/_cache");
            subfolder = subfolder.replace("Variations", "Variation");
            subfolder = subfolder.replace("Variation", "Variations");
            subfolder = subfolder.replace("Notes", "Notes/_cache");
        }
        if (subfolder!=null && subfolder.contains("../")) {
            // Custom set item or a received file
            subfolder = subfolder.replace("../","");
            if (subfolder.contains("/_cache")) {
                folder = subfolder.replace("/_cache","");
                subfolder = "/_cache";
            } else {
                folder = subfolder;
                subfolder = "";
            }
        }

        if (folder == null || folder.equals(c.getResources().getString(R.string.mainfoldername)) || folder.equals("MAIN")) {
            folder = "";
        }

        if (subfolder==null || subfolder.equals(c.getResources().getString(R.string.mainfoldername)) || subfolder.equals("MAIN")) {
            subfolder = "";
        }

        if (filename != null && filename.contains("/")) {
            // Filename is everything after the last one
            // We need to add the rest onto the subfolder name
            int indexforfilename = filename.lastIndexOf("/");
            subfolder = subfolder + "/" + filename.substring(0, indexforfilename);
            if (subfolder.endsWith("/")) {
                subfolder = subfolder.substring(0, subfolder.length() - 1);
            }
            filename = filename.substring(indexforfilename);
            if (filename.contains("/")) {
                filename = filename.replace("/", "");
            }
        }

        returnvals[0] = folder;
        returnvals[1] = subfolder;
        returnvals[2] = filename;

        return returnvals;
    }
    private String removeStartAndEndSlashes(String s) {
        if (s != null && s.startsWith("/")) {
            s = s.replaceFirst("/", "");
        }
        if (s != null && s.endsWith("/")) {
            s = s.substring(0, s.lastIndexOf("/"));
        }
        return s;
    }
    public String safeFilename(String filename) {
        // Remove bad characters from filenames
        filename = filename.replaceAll("[*?<>&!#$+\":{}@\\\\]", " "); // Removes bad characters - leave ' and / though
        filename = filename.replaceAll("\\s{2,}", " ");  // Removes double spaces
        // Don't allow the name OpenSong
        filename = filename.replace(appFolder,"Open_Song");
        return filename.trim();  // Returns the trimmed value
    }
    public Uri fixLocalisedUri(String uriString) {
        // This checks for localised filenames first and fixes the Uri
        if (uriString.equals("OpenSongApp_Logo.png") || uriString.equals("OpenSongApp_Background.png")) {
            uriString = "../OpenSong/Backgrounds/" + uriString;
        }

        // Basically replace ../OpenSong/ with the root app home folder
        if (uriString.startsWith("../OpenSong/")) {
            uriString = uriString.replace("../OpenSong/","");
            String rootFolder = "";
            if (uriString.contains("/")) {
                // Get the first folder as the new root
                rootFolder = uriString.substring(0,uriString.indexOf("/"));
                uriString = uriString.replace(rootFolder+"/","");
            }
            // Try to get the actual uri
            return getUriForItem(rootFolder,"",uriString);
        } else {
            // Now get the actual uri
            return Uri.parse(uriString);
        }
    }
    public String fixUriToLocal(Uri uri) {
        // If a file is in the OpenSong/ folder, let's localise it (important for sync)
        String path = "";
        if (uri != null && uri.getPath() != null) {
            path = uri.getPath();
            if (path.contains("OpenSong/")) {
                path = path.substring(path.lastIndexOf("OpenSong/") + 9);
                path = "../OpenSong/" + path;
            } else {
                path = uri.toString();
            }
        }
        return path;
    }
    private String songFolderAndFileOnly(String uriString, String mainfolder) {
        // Get rid of all the uri info up to the end of /OpenSong/Songs
        // Also adds mainfoldername if the song isn't in a subfolder
        if (uriString.contains("OpenSong/Songs/")) {
            uriString = uriString.substring(uriString.lastIndexOf("OpenSong/Songs/") + 15);
        }
        if (!uriString.contains("/")) {
            uriString = mainfolder + "/" + uriString;
        }
        uriString = uriString.replace("//", "/");
        return uriString;
    }
    public String getPartOfUri(Uri uri) {
        // This gets the filename
        String path = uri.getPath();
        if (path != null && path.contains("OpenSong/")) {
            path = path.substring(path.lastIndexOf("OpenSong/"));
        }
        return path;
    }


    // Get information about the files
    public String getUTFEncoding(Uri uri) {
        // Try to determine the BOM for UTF encoding
        String utf = "UTF-8";
        InputStream is = null;
        try {
            is = getInputStream(uri);
            utf = getBOMEncoding(is);
        } catch (Exception e) {
            Log.d(TAG, "Unable to get encoding for " + uri);
        }
        try {
            if (is != null) {
                is.close();
            }
        } catch (Exception e) {
            // Error closing
        }
        return utf;
    }
    private String getBOMEncoding(InputStream inputStream) {
        String utf = "UTF-8";
        if (inputStream == null) {
            return utf;
        } else {
            try {
                PushbackInputStream in = new PushbackInputStream(inputStream, 4);
                byte[] bom = new byte[4];
                int read = in.read(bom);
                switch (read) {
                    case 4:
                        if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)
                                && (bom[2] == (byte) 0x00) && (bom[3] == (byte) 0x00)) {
                            utf = "UTF-32LE";
                            break;
                        } else if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00) &&
                                (bom[2] == (byte) 0xFE) && (bom[3] == (byte) 0xFF)) {
                            utf = "UTF-32BE";
                            break;
                        }

                    case 3:
                        if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) &&
                                (bom[2] == (byte) 0xBF)) {
                            utf = "UTF-8";
                            break;
                        }

                    case 2:
                        if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
                            utf = "UTF-16LE";
                            break;
                        } else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
                            utf = "UTF-16BE";
                            break;
                        }
                }

                if (read > 0) {
                    in.unread(bom, 0, read);
                }
                in.close();
                return utf;
            } catch (Exception e) {
                e.printStackTrace();
                return utf;
            }
        }
    }
    public float getFileSizeFromUri(Uri uri) {
        if (lollipopOrLater()) {
            return getFileSizeFromUri_SAF(uri);
        } else {
            return getFileSizeFromUri_File(uri);
        }
    }
    private float getFileSizeFromUri_SAF(Uri uri) {
        if (uri!=null && uri.getPath()!=null) {
            try {
                DocumentFile df = documentFileFromUri(uri, uri.getPath());
                if (df != null && df.exists()) {
                    return (float) df.length() / (float) 1024;
                } else {
                    return 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        } else {
            Log.d(TAG, "getFileSizeFromUri called on null uri");
            return 0;
        }
    }
    private float getFileSizeFromUri_File(Uri uri) {
        File df = null;
        if (uri != null && uri.getPath() != null) {
            df = new File(uri.getPath());
        }
        if (df != null && df.exists()) {
            return (float) df.length() / (float) 1024;
        } else {
            return 0;
        }
    }
    public boolean uriExists(Uri uri) {
        if (lollipopOrLater()) {
            return uriExists_SAF(uri);
        } else {
            return uriExists_File(uri);
        }
    }
    private boolean uriExists_SAF(Uri uri) {
        try {
            InputStream is = c.getContentResolver().openInputStream(uri);
            if (is != null) {
                is.close();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private boolean uriExists_File(Uri uri) {
        if (uri != null && uri.getScheme() != null && uri.getScheme().equals("file")) {
            File df = null;
            if (uri.getPath() != null) {
                df = new File(uri.getPath());
            }
            if (df != null) {
                return df.exists();
            } else {
                return false;
            }
        } else {
            try {
                if (uri != null) {
                    InputStream is = c.getContentResolver().openInputStream(uri);
                    is.close();
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
    }
    public boolean uriIsFile(Uri uri) {
        if (lollipopOrLater()) {
            return uriIsFile_SAF(uri);
        } else {
            return uriIsFile_File(uri);
        }
    }
    private boolean uriIsFile_SAF(Uri uri) {
        DocumentFile df = DocumentFile.fromSingleUri(c, uri);
        if (df == null) {
            return false;
        } else {
            try {
                return df.isFile();
            } catch (Exception e) {
                return false;
            }
        }
    }
    private boolean uriIsFile_File(Uri uri) {
        if (uri != null && uri.getPath() != null) {
            File f = new File(uri.getPath());
            return f.isFile();
        } else {
            return false;
        }
    }
    public boolean isTextFile(Uri uri) {
        boolean istext = false;
        if (uri != null && uri.getLastPathSegment() != null) {
            String name = uri.getLastPathSegment().toLowerCase(Locale.ROOT);
            if (name.endsWith(".txt") ||
                    (!name.contains(".pdf") && !name.contains(".zip") &&
                    !name.contains(".doc") && !name.contains(".docx") &&
                    !filenameIsImage(name))) {
                istext = true;
            }
        }
        return istext;
    }
    public boolean isIMGorPDF(Song song) {
        // Determines if we can load song as text, image or pdf
        String file_ext = song.getFilename();
        boolean isImgOrPDF = false;

        if (file_ext != null) {
            file_ext = file_ext.toLowerCase(Locale.ROOT);
        } else {
            file_ext = "";
        }
        if (file_ext.endsWith(".pdf")) {
            song.setFiletype("PDF");
            isImgOrPDF = true;
        } else if (filenameIsImage("addingextracharsfortest"+file_ext)) {
            song.setFiletype("IMG");
            isImgOrPDF = true;
        }
        return isImgOrPDF;
    }
    public boolean isSpecificFileExtension(String whichType, String filename) {
        String toCheck = "";
        switch (whichType) {
            case "image":
                toCheck = ".jpg.jpeg.gif.bmp.png";
                break;
            case "pdf":
                toCheck = ".pdf";
                break;
            case "imageorpdf":
                toCheck = ".jpg.jpeg.gif.bmp.pdf.png";
                break;
            case "chordpro":
                toCheck = ".cho.crd.chopro.pro";
                break;
            case "onsong":
                toCheck = ".onsong";
                break;
            case "text":
                toCheck = ".txt";
                break;
        }
        // This is a simple check for file extensions that tell the app which database to use
        filename = filename.toLowerCase(Locale.ROOT);
        if (filename.contains(".")) {
            filename = filename.substring(filename.lastIndexOf("."));
            return toCheck.contains(filename);
        } else {
            return false;
        }
    }



    // Get references to the files and folders
    // This is used for files we know exist already
    public Uri getUriForItem(String folder, String subfolder, String filename) {
            String[] fixedfolders = fixFoldersAndFiles(folder, subfolder, filename);
        if (lollipopOrLater()) {
            return getUriForItem_SAF(fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        } else {
            return getUriForItem_File(fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        }
    }
    @SuppressLint("NewApi")
    private Uri getUriForItem_SAF(String folder, String subfolder, String filename) {

        // Get the home folder as our start point
        if (uriTreeHome == null) {
            uriTreeHome = homeFolder(null);
        }

        if (uriTreeHome!=null) {
            String uriTreeHomeId = DocumentsContract.getTreeDocumentId(uriTreeHome);
            String path = "";
            if (!uriTreeHomeId.endsWith(appFolder)) {
                path = "/" + appFolder;
            }
            if (folder!=null && !folder.isEmpty()) {
                path += "/" + folder;
            }
            if (subfolder!=null && !subfolder.isEmpty() &&
                    !subfolder.equals(c.getString(R.string.mainfoldername)) &&
                    !subfolder.equals("MAIN")) {
                path += "/" + subfolder;
            }
            if (filename!=null && !filename.isEmpty()) {
                path += "/" + filename;
            }
            return DocumentsContract.buildDocumentUriUsingTree(uriTreeHome, DocumentsContract.getTreeDocumentId(uriTreeHome) + path);
        } else {
            return null;
        }
    }
    private Uri getUriForItem_File(String folder, String subfolder, String filename) {
        String s = stringForFile(folder);
        File f = new File(s);
        if (subfolder != null && !subfolder.isEmpty() && !subfolder.equals(c.getString(R.string.mainfoldername)) && !subfolder.equals("MAIN")) {
            f = new File(f, subfolder);
        }
        if (filename != null && !filename.isEmpty() && !filename.equals(c.getString(R.string.mainfoldername)) && !filename.equals("MAIN")) {
            f = new File(f, filename);
        }
        return Uri.fromFile(f);
    }

    public InputStream getInputStream(Uri uri) {
        if (c!=null && c.getContentResolver()!=null && uri!=null) {
            try {
                return c.getContentResolver().openInputStream(uri);
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }
    public OutputStream getOutputStream(Uri uri) {
        if (uriExists(uri) || !lollipopOrLater()) {
            try {
                return c.getContentResolver().openOutputStream(uri,"wt");  // Truncate to 0 to wipe
            } catch (Exception e) {
                Log.d(TAG, "Error getting outputstream");
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
    private DocumentFile documentFileFromRootUri(Uri uri, String path) {
        if (uri != null && lollipopOrLater()) {
            return DocumentFile.fromTreeUri(c, uri);
        } else if (path != null && !lollipopOrLater()) {
            File f = new File(path);
            return DocumentFile.fromFile(f);
        } else {
            return null;
        }
    }
    private DocumentFile documentFileFromUri(Uri uri, String path) {
        if (uri != null && lollipopOrLater()) {
            return DocumentFile.fromSingleUri(c, uri);
        } else if (path != null) {
            File f = new File(path);
            return DocumentFile.fromFile(f);
        } else {
            return null;
        }
    }
    @SuppressLint("NewApi")
    private String getDocumentsContractId(Uri uri) {
        try {
            if (lollipopOrLater()) {
                return DocumentsContract.getDocumentId(uri);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Uri getChildren(Uri uri, String id) {
        if (lollipopOrLater()) {
            return DocumentsContract.buildChildDocumentsUriUsingTree(uri, id);
        } else {
            return DocumentsContract.buildChildDocumentsUri(uri.getAuthority(), id);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean docContractCreate(Uri uri, String mimeType, String name) {
        if (uri!=null && name!=null && !name.isEmpty()) {
            try {
                //updateFileActivityLog("\ndocContractCreate() called.  uri:"+uri+"  mimeType:"+mimeType+"  name:"+name);
                return DocumentsContract.createDocument(c.getContentResolver(), uri, mimeType, name) != null;
            } catch (Exception e) {
                Log.d(TAG, "Error creating " + name + " at " + uri);
                return false;
            }
        } else {
            Log.d(TAG,"Uri for "+name+" was null");
            return false;
        }
    }


    // Basic file actions (read, create, copy, delete, write)
    public boolean saveThisSongFile(Song thisSong) {
        // This is called from the SaveSong class and uses the sent Song object
        // First get the song uri
        // Because it may not be in the songs folder, lets check!
        String[] fixLocations = getActualFoldersFromNice(thisSong.getFolder());
        //ArrayList<String> newLocation = fixNonSongs(thisSong.getFolder());
        // Write the string file
        return doStringWriteToFile(fixLocations[0], fixLocations[1],
                thisSong.getFilename(),
                mainActivityInterface.getProcessSong().getXML(thisSong));
    }

    public String[] getActualFoldersFromNice(String folder) {
        String[] location = new String[2];
        location[0] = "Songs";
        location[1] = folder;
        if (folder.contains("../") || folder.contains("**")) {
            folder = folder.replace("../", "");
            folder = folder.replace("**", "");
            folder = folder.replace("/_cache", "");
            if (folder.contains(c.getString(R.string.variation)) || folder.contains("Variation")) {
                location[0] = "Variations";
                location[1] = "";
            } else if (folder.contains(c.getString(R.string.note)) || folder.contains("Note")) {
                location[0] = "Notes";
                location[1] = "_cache";
            } else if (folder.contains(c.getString(R.string.image)) || folder.contains("Image")) {
                location[0] = "Images";
                location[1] = "_cache";
            } else if (folder.contains(c.getString(R.string.scripture)) || folder.contains("Scripture")) {
                location[0] = "Scripture";
                location[1] = "_cache";
            } else if (folder.contains(c.getString(R.string.slide)) || folder.contains("Slide")) {
                location[0] = "Slides";
                location[1] = "_cache";
            }
            if (folder.contains("_cache")) {
                location[1] = "_cache";
            }
        }
        return location;
    }
    public void lollipopCreateFileForOutputStream(boolean deleteOld, Uri uri, String mimeType,
                                                  String folder, String subfolder, String filename) {
        // deleteOld will remove any existing file before creating a new one (avoids artefacts) - xml files only
        // We will only delete when the file isn't empty or null, otherwise folders are cleared!

        if (lollipopOrLater()) {
            // Only need to do this for Lollipop or later
            if (uriExists(uri) && deleteOld && filename != null && !filename.isEmpty()) {
                // Delete it to avoid overwrite errors that leaves old stuff at the end of the file
                deleteFile_SAF(uri);
            }
            // Create the new file
            if (!uriExists(uri)) {
                //Log.d(TAG,"uri "+uri+" doesn't exist, so create it");
                createFile(mimeType, folder, subfolder, filename);
            }

        } else {
            // Check it exists
            if (uriExists(uri) && deleteOld && filename!=null && !filename.isEmpty()) {
                deleteFile_File(uri);
            }
            try {
                if (uri != null && uri.getPath() != null) {
                    File f = new File(uri.getPath());
                    if (!f.exists()) {
                        if (mimeType.equals(DocumentsContract.Document.MIME_TYPE_DIR)) {
                            if (!f.mkdirs()) {
                                Log.d(TAG, "Unable to create file " + f);
                            }
                        } else {
                            if (!f.createNewFile()) {
                                Log.d(TAG, "Unable to create file " + f);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public Uri copyFromTo(String fromFolder, String fromSubfolder, String fromName, String toFolder, String toSubfolder, String toName) {
        // Fix folders
        String[] fixedFrom = fixFoldersAndFiles(fromFolder,fromSubfolder,fromName);
        String[] fixedTo   = fixFoldersAndFiles(toFolder,toSubfolder,toName);
        Uri fromUri = getUriForItem(fixedFrom[0], fixedFrom[1], fixedFrom[2]);
        Uri toUri = getUriForItem(fixedTo[0], fixedTo[1], fixedTo[2]);
        // Make sure the newUri is valid and exists
        lollipopCreateFileForOutputStream(true, toUri,null,fixedTo[0],fixedTo[1],fixedTo[2]);
        // Get the input and output streams
        InputStream inputStream = getInputStream(fromUri);
        OutputStream outputStream = getOutputStream(toUri);
        if (copyFile(inputStream, outputStream)) {
            return toUri;
        } else {
            return null;
        }
    }
    public boolean copyFile(InputStream in, OutputStream out) {
        if (in != null && out != null) {
            try {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }
    public boolean doStringWriteToFile(String folder, String subfolder, String filename, String string) {
        try {
            Uri uri = getUriForItem(folder, subfolder, filename);
            lollipopCreateFileForOutputStream(true, uri, null, folder, subfolder, filename);
            OutputStream outputStream = getOutputStream(uri);
            return writeFileFromString(string, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean writeFileFromString(String s, OutputStream outputStream) {
        BufferedOutputStream bufferedOutputStream = null;
        try {
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            if (outputStream!=null && s!=null) {
                bufferedOutputStream.write(s.getBytes(StandardCharsets.UTF_8));
                bufferedOutputStream.flush();
                bufferedOutputStream.close();
                // All good (this also closes the output stream).  Return true
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Oops.  We need to try closing the streams again
        }
        // If there was a problem, close the outputstream and return false
        if (bufferedOutputStream!=null) {
            try {
                bufferedOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (outputStream!=null) {
            try {
                outputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public void writeFileFromDecodedImageString(OutputStream os, byte[] bytes) {
        try {
            os.write(bytes);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void writeImage(OutputStream outputStream, Bitmap bmp) {
        try {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            //bmp.recycle();
        } catch (Exception | OutOfMemoryError e) {
            e.printStackTrace();
        }
    }
    public String readTextFileToString(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = null;
        InputStreamReader inputStreamReader = null;
        if (inputStream != null) {
            String string;
            try {
                inputStreamReader = new InputStreamReader(inputStream);
                reader = new BufferedReader(inputStreamReader);
                while ((string = reader.readLine()) != null) {
                    stringBuilder.append(string).append("\n");
                }
            } catch (Exception | OutOfMemoryError e) {
                e.printStackTrace();
            }
            if (reader!=null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (inputStreamReader!=null) {
                try {
                    inputStreamReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return stringBuilder.toString();
        } else {
            return "";
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void createFile(String mimeType, String folder, String subfolder, String filename) {
        String[] fixedfolders = fixFoldersAndFiles(folder, subfolder, filename);
        if (lollipopOrLater()) {
            createFile_SAF(mimeType, fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        } else {
            createFile_File(fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createFile_SAF(String mimeType, String folder, String subfolder, String filename) {
        // Try this instead
        Uri parentUri;
        folder = removeStartAndEndSlashes(folder);
        subfolder = removeStartAndEndSlashes(subfolder);
        filename = removeStartAndEndSlashes(filename);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("createFile() called for folder:").append(folder)
                .append("  subfolder:").append(subfolder).append("  filename:").append(filename);
        Uri uritest = getUriForItem(folder, subfolder, filename);

        stringBuilder.append("\nuritest:").append(uritest);
        if (mimeType != null && mimeType.equals(DocumentsContract.Document.MIME_TYPE_DIR)) {
            // Check the filename is empty.  If not, add it to the subfolder
            // If something is sent as a filename, add it to the subfolder
            if (filename != null && !filename.isEmpty()) {
                subfolder = subfolder + "/" + filename;
                subfolder = subfolder.replace("//", "/");
            }

            // We first attempt to create the folder as it is (assuming all parent directories exist)
            // Split the subfolder up into parent director (may include lots of subfolders) and child to create

            String foldertocreate = subfolder;
            if (foldertocreate.contains("/")) {
                // We only want the last part to be the one to create, so split the subfolder up
                subfolder = foldertocreate.substring(0, foldertocreate.lastIndexOf("/"));
                foldertocreate = foldertocreate.replace(subfolder + "/", "");
            } else {
                // We need to create the subfolder, so it shouldn't be part of the parent
                subfolder = "";
            }

            subfolder = removeStartAndEndSlashes(subfolder);
            foldertocreate = removeStartAndEndSlashes(foldertocreate);

            parentUri = getUriForItem(folder, subfolder, "");
            stringBuilder.append("\nFolder creation required.\nparentUri:").append(parentUri);

            // Only create if it doesn't exist
            // From #187 Millerthegorilla
            if (!uriExists(uritest)) {
                boolean created = false;
                if (uriExists(parentUri)) {
                    created = docContractCreate(parentUri, mimeType, foldertocreate);
                    stringBuilder.append("\nuritest didn't exist, but parentUri did so creating:")
                            .append("docContractCreate(").append(parentUri).append(",")
                            .append(mimeType).append(",").append(foldertocreate).append(")");
                }

                if (!created) {
                    stringBuilder.append("\ncreated:false.  parent didn't exist?");

                    // Error (likely parent directory doesn't exist
                    // Go through each folder and create the ones we need starting
                    String[] bits = subfolder.split("/");
                    String bit = "";
                    for (String s : bits) {
                        parentUri = getUriForItem(folder, bit, "");
                        Uri newUri = parentUri;
                        newUri.buildUpon().appendPath(s).build();
                        if (!uriExists(newUri)) {
                            docContractCreate(parentUri, mimeType, s);
                            stringBuilder.append("\ntry again, docContractCreate(").
                                    append(parentUri).append(",").append(mimeType)
                                    .append(",").append(s).append(")");

                        }
                        bit = bit + "/" + s;
                    }
                }
            }
        } else {
            // Must be a file

            // Filename could be sent as Band/temp/Song 1
            // Subfolder could be sent as Secular
            // Giving total subfolder as Secular/Band/temp and song as Song 1

            String completesubfolder = subfolder;
            String completefilename = filename;

            if (completefilename.contains("/")) {
                completesubfolder = completesubfolder + "/" + completefilename.substring(0, completefilename.lastIndexOf("/"));
                completefilename = completefilename.substring(completefilename.lastIndexOf("/"));

                // Replace double //
                completesubfolder = completesubfolder.replace("//", "/");
                completefilename = completefilename.replace("//", "/");
            }

            // Remove start and end /
            completesubfolder = removeStartAndEndSlashes(completesubfolder);
            completefilename = removeStartAndEndSlashes(completefilename);

            parentUri = getUriForItem(folder, completesubfolder, "");

            stringBuilder.append("\nFile creation required.\nparentUri:").append(parentUri);
            stringBuilder.append("\nfolder:").append(folder).append("  completesubfolder:")
                    .append(completesubfolder).append("  completefilename:").append(completefilename);

            if (uriTreeHome == null) {
                uriTreeHome = homeFolder(null);
            }

            if (!filename.isEmpty() && completefilename != null && !completefilename.isEmpty()) {
                if (folder.isEmpty() && completesubfolder.isEmpty()) {
                    // A temp file in the root folder
                    docContractCreate(uriTreeHome, null, filename);
                    stringBuilder.append("\nTemp file in the root folder:").append(filename);


                } else if (!uriExists(uritest)) {
                    boolean created = false;
                    if (mimeType == null && uriExists(parentUri)) {
                        // Just a document at this location, so create
                        created = docContractCreate(parentUri, null, completefilename);
                        stringBuilder.append("\nJust create the document.\nparentUri:").append(parentUri)
                                .append("  completefilename:").append(completefilename);

                    }

                    if (!created) {
                        // Error (likely parent directory doesn't exist)
                        // Go through each folder and create the ones we need starting at the 'folder'
                        String[] bits = completesubfolder.split("/");
                        String bit = "";
                        for (String s : bits) {
                            parentUri = getUriForItem(folder, bit, "");
                            Uri checkUri = null;
                            if (parentUri!=null && !s.isEmpty()) {
                                checkUri = Uri.withAppendedPath(parentUri, s);
                            }
                            if (mimeType == null && uriExists(parentUri) && (checkUri==null || !uriExists(checkUri))) {
                                stringBuilder.append("\nCreate sub directory.\nparentUri:").append(parentUri)
                                        .append("  create subdir:").append(s);
                                docContractCreate(parentUri, DocumentsContract.Document.MIME_TYPE_DIR, s);
                            }
                            bit = bit + "/" + s;
                        }
                        // Try again!
                        parentUri = getUriForItem(folder, completesubfolder, "");
                        if (mimeType == null || uriExists(parentUri)) {
                            stringBuilder.append("\nTry again to create the document.\nparentUri:").append(parentUri)
                                    .append("  completefilename:").append(completefilename);
                            updateFileActivityLog(stringBuilder.toString());
                            docContractCreate(parentUri, mimeType, completefilename);
                            return;
                        } else {
                            updateFileActivityLog(stringBuilder.toString());
                            return;
                        }
                    }
                }
            }
        }
        updateFileActivityLog(stringBuilder.toString());
    }
    private void createFile_File(String folder, String subfolder, String filename) {
        boolean stuffCreated = false;
        String filepath = stringForFile(folder);
        File f = new File(filepath);
        if (subfolder != null && !subfolder.isEmpty()) {
            f = new File(f, subfolder);
        }

        if (!f.exists()) {
            try {
                stuffCreated = f.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (filename != null && !filename.isEmpty()) {
            f = new File(f, filename);
            Uri uri = Uri.fromFile(f);
            try {
                if (f.exists() && uriExists(uri)) {
                    // IV - Delete any existing file (does not touch folder)
                    boolean deleted = f.delete();
                    Log.d(TAG,"Removing preexisting file: filename - "+ deleted);
                }
                stuffCreated = f.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                stuffCreated = false;
            }
        }
        Log.d(TAG,"stuffCreated:"+stuffCreated);
    }
    public boolean deleteFile(Uri uri) {
        if (lollipopOrLater()) {
            return deleteFile_SAF(uri);
        } else {
            return deleteFile_File(uri);
        }
    }
    private boolean deleteFile_SAF(Uri uri) {
        try {
            DocumentFile df = DocumentFile.fromSingleUri(c, uri);
            if (df != null) {
                if (df.exists()) {
                    return df.delete();
                } else {
                    return false;
                }

            } else {
                Log.d(TAG, "Documentfile is null so can't delete");
                return false;
            }
        } catch (Exception e) {
            Log.d(TAG, "Unable to delete " + uri);
            e.printStackTrace();
            return false;
        }
    }
    private boolean deleteFile_File(Uri uri) {
        try {
            if (uri != null && uri.getPath() != null) {
                File f = new File(uri.getPath());
                // If this is a directory, empty it first
                if (f.isDirectory() && f.listFiles() != null) {
                    wipeFolder_File(f);
                }
                return f.delete();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    public boolean doDeleteFile(String location, String subfolder, String filename) {
        Uri uri = getUriForItem(location, subfolder, filename);
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doDeleteFile deleteFile "+location+"/"+subfolder+"/"+filename);
        return deleteFile(uri);
    }
    public String getImageSlide(String loc) {
        String b = "";
        Uri uri;
        if (loc.startsWith("../")) {
            uri = fixLocalisedUri(loc);
        } else {
            uri = Uri.parse(loc);
        }
        if (uriExists(uri)) {
            try {
                InputStream inputStream = getInputStream(uri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                b = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Error getting image bytes");
            }
        }
        return b;
    }
    public String getActualFilename(String string) {
        Uri uri = Uri.parse(string);
        if (lollipopOrLater()) {
            try {

                Cursor cursor = c.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    cursor.moveToFirst();
                    String name = cursor.getString(nameIndex);
                    cursor.close();
                    return name;
                } else {
                    return uri.getPath();
                }
            } catch (Exception e) {
                return uri.getPath();
            }
        } else {
            if (uri == null) {
                return string;
            } else {
                return uri.getLastPathSegment();
            }
        }
    }
    public void renameFileFromUri(Uri oldUri, Uri newUri, String newFolder, String newSubfolder, String newName) {
        if (lollipopOrLater()) {
            renameFileFromUri_SAF(oldUri, newUri, newFolder, newSubfolder, newName);
        } else {
            renameFileFromUri_File(oldUri, newUri);
        }
    }
    private void renameFileFromUri_File(Uri oldUri, Uri newUri) {
        File file = new File(oldUri.getPath());
        boolean renamed = file.renameTo(new File(newUri.getPath()));
        Log.d(TAG,"renamed:"+renamed);
    }
    private void renameFileFromUri_SAF(Uri oldUri, Uri newUri, String newFolder, String newSubfolder, String newName) {
        // Don't use document file rename as it can end badly if there is an issue
        // This can rename the root folder.  So instead copy the old file contents
        // Write the new file and delete the old one

        // If the new file already exists, delete it to avoid overwrite errors
        // Now create a blank file
        lollipopCreateFileForOutputStream(true, newUri,null,newFolder,newSubfolder,newName);

        // Now get an InputStream from the oldUri and an OutputStream for the newUri
        InputStream inputStream = getInputStream(oldUri);
        OutputStream outputStream = getOutputStream(newUri);
        // Copy the file, which also closes the streams and on success, delete the old file
        if (copyFile(inputStream, outputStream)) {
            // Likely the inputStream or outputStream was null, so don't delete the old file!
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" renameFileFromUri_SAF deleteFile "+oldUri);
            deleteFile(oldUri);
        }
    }
    public String getFileNameFromUri(Uri uri) {
        if (uri!=null) {
            String scheme = uri.getScheme();
            if (scheme.equals("file")) {
                return uri.getLastPathSegment();
            } else if (scheme.equals("content")) {
                String returnString;
                Cursor cursor = c.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int i = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (i >= 0) {
                        returnString = cursor.getString(i);
                    } else {
                        returnString = uri.getLastPathSegment();
                    }
                } else {
                    returnString = uri.toString();
                }
                if (cursor!=null) {
                    cursor.close();
                }
                return returnString;
            } else {
                return uri.toString();
            }
        } else {
            return "";
        }
    }

    // Actions for folders (create, delete, rename, clear)
    public boolean renameFolder(String oldsubfolder, String newsubfolder, boolean showToast) {
        if (lollipopOrLater()) {
            return renameFolder_SAF(oldsubfolder, newsubfolder, showToast);
        } else {
            return renameFolder_File(oldsubfolder, newsubfolder, showToast);
        }
    }
    private boolean renameFolder_File(String oldsubfolder, String newsubfolder, boolean showToast) {
        // Now the long bit.  Go through the original folder and copy the files to the new location
        Uri oldUri = getUriForItem("Songs", oldsubfolder, "");
        Uri newUri = getUriForItem("Songs", newsubfolder, "");
        String message;
        boolean outcome;
        if (!uriExists(newUri)) {
            if (oldUri != null && newUri != null && oldUri.getPath() != null && newUri.getPath() != null) {
                File oldfile = new File(oldUri.getPath());
                File newfile = new File(newUri.getPath());
                if (oldfile.renameTo(newfile)) {
                    message = c.getString(R.string.success);
                    outcome = true;
                } else {
                    message = c.getString(R.string.create_folder_error);
                    outcome = false;
                }
            } else {
                message = c.getString(R.string.create_folder_error);
                outcome = false;
            }
        } else {
            message = c.getString(R.string.folder_exists);
            outcome = false;
        }
        if (showToast) {
            mainActivityInterface.getShowToast().doIt(message);
        }
        return outcome;
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean renameFolder_SAF(String oldsubfolder, String newsubfolder, boolean showToast) {
        // SAF can only rename final name (can't move within directory structure) - No / allowed!
        Uri oldUri = getUriForItem("Songs", oldsubfolder, "");
        // Work out what the new uri should be
        Uri newUri;
        // Get rid of the old last folder and replace it
        if (oldsubfolder.contains("/")) {
            oldsubfolder = oldsubfolder.substring(0,oldsubfolder.lastIndexOf("/"));
        } else {
            // Just the one subfolder (no sub-sub folders)
            oldsubfolder = "";
        }

        if (oldsubfolder.isEmpty()) {
            newUri = getUriForItem("Songs", newsubfolder, "");
        } else {
           String update = oldsubfolder + "/" + newsubfolder;
           update = update.replace("//","/");
           newUri = getUriForItem("Songs", update, "");
        }
        String message;
        boolean outcome;
        if (!uriExists(newUri)) {
            // Only rename the last section
            if (newsubfolder.contains("/")) {
                newsubfolder = newsubfolder.substring(newsubfolder.lastIndexOf("/"));
                newsubfolder = newsubfolder.replace("/", "");
            }
            try {
                Uri renamed = DocumentsContract.renameDocument(c.getContentResolver(), oldUri, newsubfolder);
                if (renamed!=null && renamed.equals(newUri)) {
                    message = c.getString(R.string.success);
                } else {
                    message = c.getString(R.string.create_folder_error);
                }
                outcome = true;
            } catch (Exception e) {
                message = c.getString(R.string.create_folder_error);
                outcome = false;
                e.printStackTrace();
            }
        } else {
            message = c.getString(R.string.folder_exists);
            outcome = false;
        }
        if (showToast) {
            mainActivityInterface.getShowToast().doIt(message);
        }
        return outcome;
    }
    public void wipeFolder(String folder, String subfolder) {
        Uri uri = getUriForItem(folder,subfolder,null);
        if (uriExists(uri)) {
            if (lollipopOrLater()) {
                wipeFolder_SAF(uri,folder,subfolder);
            } else {
                File f = new File(uri.getPath());
                wipeFolder_File(f);
            }
        }
    }
    public void wipeFolder_File(File f) {
        File[] fs = f.listFiles();
        if (fs != null) {
            for (File child : fs) {
                Log.d(TAG, "Deleting " + child + " = " + child.delete());
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void wipeFolder_SAF(Uri uri, String folder, String subfolder) {
        // Get a contract for the desired folder
        Uri desiredUri = DocumentsContract.buildDocumentUriUsingTree(uriTreeHome,DocumentsContract.getDocumentId(uri));
        try {
            DocumentsContract.deleteDocument(c.getContentResolver(), desiredUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Now make the folder again
        Uri folderUri = getUriForItem_SAF(folder,"","");
        if (!uriExists_SAF(folderUri)) {
            createFolder_SAF(uriTreeHome,folder,false);
        }
        if (subfolder!=null && !subfolder.isEmpty()) {
            createFolder_SAF(folderUri,subfolder,false);
        }
    }
    public boolean createFolder(String currentDir, String currentSubDir, String newFolder, boolean showToast) {
        // Get the uri for the parent
        Uri dirUri = getUriForItem(currentDir, currentSubDir, "");
        Uri desireduri = getUriForItem(currentDir,currentSubDir,newFolder);
        if (!uriExists(desireduri)) {
            if (lollipopOrLater()) {
                return createFolder_SAF(dirUri, newFolder, showToast);
            } else {
                return createFolder_File(dirUri, newFolder, showToast);
            }
        } else {
            if (showToast) {
                mainActivityInterface.getShowToast().doIt(c.getString(R.string.folder_exists));
            }
            return false;
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean createFolder_SAF(Uri dirUri, String newFolder, boolean showToast) {
        String message;
        boolean outcome;
        try {
            Uri createduri = DocumentsContract.createDocument(c.getContentResolver(), dirUri, DocumentsContract.Document.MIME_TYPE_DIR, newFolder);
            if (createduri!=null && !createduri.equals(dirUri)) {
                message = c.getString(R.string.success);
                outcome = true;
            } else {
                message = c.getString(R.string.create_folder_error);
                outcome = false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            message = c.getString(R.string.create_folder_error);
            outcome = false;
        }
        if (showToast) {
            mainActivityInterface.getShowToast().doIt(message);
        }
        return outcome;
    }
    private boolean createFolder_File(Uri root, String newName, boolean showToast) {
        File f = new File(root.getPath(), newName);
        String message;
        boolean outcome;
        if (f.mkdirs()) {
            message = c.getString(R.string.success);
            outcome = true;
        } else {
            message = c.getString(R.string.create_folder_error);
            outcome = false;
        }
        if (showToast) {
            mainActivityInterface.getShowToast().doIt(message);
        }
        return outcome;
    }
    public ArrayList<String> getSongFolders(ArrayList<String> songIDs, boolean addMain, String toIgnore) {
        ArrayList<String> availableFolders = new ArrayList<>();
        for (String entry : songIDs) {
            if (entry.endsWith("/")) {
                String newtext = entry.substring(0, entry.lastIndexOf("/"));
                if (!newtext.equals(toIgnore) && !availableFolders.contains(newtext)) {
                    availableFolders.add(newtext);
                }
            }
        }
        Comparator<String> comparator = (o1, o2) -> {
            Collator collator = Collator.getInstance(mainActivityInterface.getLocale());
            collator.setStrength(Collator.SECONDARY);
            return collator.compare(o1,o2);
        };
        Collections.sort(availableFolders,comparator);
        // Add the MAIN folder
        if (addMain) {
            availableFolders.add(0, c.getString(R.string.mainfoldername));
        }
        return availableFolders;
    }

    // This builds an index of all the songs on the device
    @SuppressLint("NewApi")
    public ArrayList<String> listSongs() {
        ArrayList<String> noSongs = new ArrayList<>();
        // We need to make sure the locale version of MAIN is correct (change language during run)
        Configuration configuration = new Configuration(c.getResources().getConfiguration());
        configuration.setLocale(mainActivityInterface.getLocale());
        String mainfolder = c.createConfigurationContext(configuration).getResources().getString(R.string.mainfoldername);
        try {
            // Decide if we are using storage access framework or not
            if (lollipopOrLater()) {
                return listSongs_SAF(mainfolder);
            } else {
                return listSongs_File(mainfolder);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return noSongs;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ArrayList<String> listSongs_SAF(String mainfolder) {
        // This gets all songs (including any subfolders)
        ArrayList<String> songIds = new ArrayList<>();
        Uri uri = getUriForItem("Songs", "", "");

        // Now get a documents contract at this location
        String songFolderId = getDocumentsContractId(uri);

        // Get the child folders
        Uri children = getChildren(uri, songFolderId);
        ContentResolver contentResolver = c.getContentResolver();

        // Keep track of our directory hierarchy
        List<Uri> dirNodes = new LinkedList<>();
        dirNodes.add(children);

        while (!dirNodes.isEmpty()) {
            children = dirNodes.remove(0); // get the item from top
            Cursor cursor = contentResolver.query(children, new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE}, null, null, null);
            try {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        final String docId = cursor.getString(0);
                        final String mime = cursor.getString(2);
                        if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mime) && docId.contains("OpenSong")) {
                            final Uri newNode = getChildren(children, docId);
                            dirNodes.add(newNode);
                            if (docId.contains("OpenSong/Songs/")) {
                                songIds.add(songFolderAndFileOnly(docId + "/", mainfolder)); // In case the folder is empty add it as a songId
                            }
                        } else if (docId.contains("OpenSong/Songs/")) {
                            songIds.add(songFolderAndFileOnly(docId, mainfolder));
                        }
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Check we only have valid songIds
        ArrayList<String> checkedSongIds = new ArrayList<>();
        for (String songId:songIds) {
            if (!badFileExtension(songId)) {
                checkedSongIds.add(songId);
            }
        }
        return checkedSongIds;
    }
    private ArrayList<String> listSongs_File(String mainfolder) {
        // We must be using an older version of Android, so stick with File access
        ArrayList<String> songIds = new ArrayList<>();  // These will be the file locations

        String songsFolder = stringForFile("Songs");
        ArrayList<String> foldersToIndex = new ArrayList<>();

        // Add the main songs folder to the foldersToIndex.  More will be added for subfolders
        foldersToIndex.add(songsFolder);
        int num = foldersToIndex.size();
        // Go through all the folders
        for (int i = 0; i < num; i++) {
            File folder = new File(foldersToIndex.get(i));
            File[] contents = folder.listFiles();
            // Go through each item and add songs or folders
            if (contents!=null) {
                for (File item : contents) {
                    if (item.isDirectory()) {
                        foldersToIndex.add(item.getPath());
                        songIds.add(songFolderAndFileOnly(item.getPath(), mainfolder) + "/");
                        num = foldersToIndex.size();
                    } else if (item.isFile()) {
                        songIds.add(songFolderAndFileOnly(item.getPath(), mainfolder));
                    }
                }
            }
        }
        // Check we only have valid songIds
        ArrayList<String> checkedSongIds = new ArrayList<>();
        for (String songId:songIds) {
            if (!badFileExtension(songId)) {
                checkedSongIds.add(songId);
            }
        }
        return checkedSongIds;
    }

    public boolean badFileExtension(String filename) {
        filename = filename.toLowerCase();
        // Check for ".xxx" or ".xxxx" extension that isn't wanted
        if (filename.contains(".") &&
                filename.length()>=5 &&
                (filename.lastIndexOf(".")==filename.length()-4 ||
                        filename.lastIndexOf(".")==filename.length()-5) &&
                !filenameIsImage(filename) && !filename.endsWith(".pdf") &&
                !filename.endsWith(".txt") && !filename.endsWith(".xml")) {
            updateFileActivityLog("BAD file:"+filename+" should not be in an OpenSong song folder - please move it as soon as possible!");
            return true;
        }
        return false;
    }

    public boolean filenameIsImage(String filename) {
        filename = filename.toLowerCase();
        return filename.endsWith(".jpg") || filename.endsWith(".jpeg") ||
                filename.endsWith(".gif") || filename.endsWith(".bmp") ||
                filename.endsWith(".png") || filename.endsWith(".webp") ||
                filename.endsWith(".heif") || filename.endsWith(".heic");
    }

    // Dealing with indexing the songs on the device
    public void writeSongIDFile(ArrayList<String> songIds) {
        // This creates a file in the app storage with a list of song folders/filenames
        StringBuilder stringBuilder = new StringBuilder();

        // Sort the array
        Collator collator;
        if (mainActivityInterface.getLocale() == null) {
            collator = Collator.getInstance(Locale.getDefault());
        } else {
            collator = Collator.getInstance(mainActivityInterface.getLocale());
        }
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(songIds, collator);
        for (String songId : songIds) {
            stringBuilder.append(songId).append("\n");
        }
        // Get the file reference
        File songIDFile = new File(c.getExternalFilesDir("Database"), "SongIds.txt");
        // Let's delete this file and then create a new blank one
        Log.d(TAG,"Deleting old songIDFile success="+songIDFile.delete());
        songIDFile = new File(c.getExternalFilesDir("Database"), "SongIds.txt");
        try {
            Log.d(TAG,"Creating new songIDFile success="+songIDFile.createNewFile());
            OutputStream outputStream = getOutputStream(Uri.fromFile(songIDFile));
            if (outputStream != null) {
                writeFileFromString(stringBuilder.toString(), outputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public ArrayList<String> getSongIDsFromFile() {
        File songIDFile = new File(c.getExternalFilesDir("Database"), "SongIds.txt");
        Uri uri = Uri.fromFile(songIDFile);
        InputStream is = getInputStream(uri);
        String text = readTextFileToString(is);
        // Split the text into line and add to the new array
        ArrayList<String> songIDs = new ArrayList<>();
        String[] lines = text.split("\n");
        Collections.addAll(songIDs, lines);
        return songIDs;
    }
    @SuppressLint("NewApi")
    public ArrayList<String> listFilesInFolder(String folder, String subfolder) {
        if (subfolder.startsWith("../") || subfolder.startsWith("**")) {
            folder = subfolder.replace("../","");
            folder = folder.replace("**","");
            subfolder = "";
        }

        if (lollipopOrLater()) {
            return listFilesInFolder_SAF(folder, subfolder);
        } else {
            return listFilesInFolder_File(folder, subfolder);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ArrayList<String> listFilesInFolder_SAF(String folder, String subfolder) {
        ArrayList<String> al = new ArrayList<>();

        Uri locationtoindex = getUriForItem(folder, subfolder, "");

        // Now get a documents contract at this location
        String id = getDocumentsContractId(locationtoindex);

        // Get the children
        Uri children = getChildren(locationtoindex, id);
        ContentResolver contentResolver = c.getContentResolver();

        // Keep track of our directory hierarchy
        List<Uri> dirNodes = new LinkedList<>();
        dirNodes.add(children);

        while (!dirNodes.isEmpty()) {
            try {

                children = dirNodes.remove(0); // get the item from top
                Cursor cursor = contentResolver.query(children, new String[]{
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_MIME_TYPE}, null, null, null);

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        final String name = cursor.getString(1);
                        final String mime = cursor.getString(2);
                        if (!DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) {
                            al.add(name);
                        }
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Comparator<String> comparator = (o1, o2) -> {
            Collator collator = Collator.getInstance(mainActivityInterface.getLocale());
            collator.setStrength(Collator.SECONDARY);
            return collator.compare(o1,o2);
        };
        Collections.sort(al, comparator);
        return al;
    }
    private ArrayList<String> listFilesInFolder_File(String folder, String subfolder) {
        ArrayList<String> al = new ArrayList<>();
        String filebuilder = stringForFile(folder);
        File f = new File(filebuilder);
        if (subfolder != null && !subfolder.isEmpty()) {
            f = new File(f, subfolder);
        }
        File[] fs = f.listFiles();
        if (fs != null && fs.length > 0) {
            for (File fi : fs) {
                if (fi.isFile()) {
                    al.add(fi.getName());
                }
            }
        }
        Comparator<String> comparator = (o1, o2) -> {
            Collator collator = Collator.getInstance(mainActivityInterface.getLocale());
            collator.setStrength(Collator.SECONDARY);
            return collator.compare(o1,o2);
        };
        Collections.sort(al, comparator);
        return al;
    }
    int songCountAtLocation(File f) {
        // Prepare an arraylist for any song folders
        ArrayList<File> foldersToIndex = new ArrayList<>();
        foldersToIndex.add(f);
        int count = 0;
        try {
            for (File folderToIndex : foldersToIndex) {
                File[] fs = folderToIndex.listFiles();
                if (fs != null) {
                    for (File ff : fs) {
                        if (ff.isDirectory()) {
                            foldersToIndex.add(ff);
                        } else {
                            count++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    private boolean creatingLogFile = false;
    public void updateFileActivityLog(String logText) {
        if (!creatingLogFile && fileWriteLog && logText!=null) {
            try {
                logText = logText.trim();
                Uri logUri = getUriForItem("Settings", "", "fileWriteActivity.txt");
                if (logUri != null) {
                    if (!uriExists(logUri)) {
                        creatingLogFile = true;
                        lollipopCreateFileForOutputStream(false, logUri, null, "Settings", "", "fileWriteActivity.txt");
                        creatingLogFile = false;
                    }
                    OutputStream outputStream;
                    if (getFileSizeFromUri(logUri) > 900) {
                        outputStream = c.getContentResolver().openOutputStream(logUri, "wt");
                    } else {
                        outputStream = c.getContentResolver().openOutputStream(logUri, "wa");
                    }
                    mainActivityInterface.getStorageAccess().writeFileFromString(logText + "\n", outputStream);
                } else {
                    Log.d(TAG, "logUri was null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateFileUsageLog(Song thisSong) {
        // List the song being viewed
        if (fileViewLog && thisSong != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String logText = "\"" + sdf.format(new Date()) + "\"," +
                        "\"" + fixNull(thisSong.getFolder()).replaceAll("\"", "'") + "\"," +
                        "\"" + fixNull(thisSong.getFilename()).replaceAll("\"", "'") + "\"," +
                        "\"" + fixNull(thisSong.getTitle()).replaceAll("\"", "'") + "\"," +
                        "\"" + fixNull(thisSong.getAuthor()).replaceAll("\"", "'") + "\"," +
                        "\"" + fixNull(thisSong.getCopyright()).replaceAll("\"", "'") + "\"," +
                        "\"" + fixNull(thisSong.getCcli()).replaceAll("\"", "'") + "\"," +
                        "\"" + fixNull(thisSong.getKey()).replaceAll("\"", "'") + "\"," +
                        "\"" + fixNull(thisSong.getCapo()).replaceAll("\"", "'") + "\"," +
                        "\"" + fixNull(thisSong.getAka()).replaceAll("\"", "'") + "\"," +
                        "\"" + fixNull(thisSong.getTheme()).replaceAll("\"", "'") + "\"," +
                        "\"" + fixNull(thisSong.getUser1()).replaceAll("\"", "'") + "\"," +
                        "\"" + fixNull(thisSong.getUser2()).replaceAll("\"", "'") + "\"," +
                        "\"" + fixNull(thisSong.getUser3()).replaceAll("\"", "'") + "\"," +
                        "\"" + fixNull(thisSong.getHymnnum()).replaceAll("\"", "'") + "\"," +
                        "\"" + fixNull(thisSong.getTempo()).replaceAll("\"", "'") + "\"," +
                        "\"" + fixNull(thisSong.getTimesig()).replaceAll("\"", "'") + "\"";

                String topline = "\"" + c.getString(R.string.date) + "/" +
                        c.getString(R.string.time) + "\"," +
                        "\"" + c.getString(R.string.folder) + "\"," +
                        "\"" + c.getString(R.string.filename) + "\"," +
                        "\"" + c.getString(R.string.title) + "\"," +
                        "\"" + c.getString(R.string.author) + "\"," +
                        "\"" + c.getString(R.string.copyright) + "\"," +
                        "\"" + c.getString(R.string.ccli) + "\"," +
                        "\"" + c.getString(R.string.key) + "\"," +
                        "\"" + c.getString(R.string.capo) + "\"," +
                        "\"" + c.getString(R.string.edit_song_aka) + "\"," +
                        "\"" + c.getString(R.string.tag) + "\"," +
                        "\"" + c.getString(R.string.user_1) + "\"," +
                        "\"" + c.getString(R.string.user_2) + "\"," +
                        "\"" + c.getString(R.string.user_3) + "\"," +
                        "\"" + c.getString(R.string.hymn_number) + "\"," +
                        "\"" + c.getString(R.string.tempo) + "\"," +
                        "\"" + c.getString(R.string.time_signature) + "\"";

                String writeThis = logText;
                Uri logUri = getUriForItem("Settings", "", "fileHistory.csv");
                if (logUri != null) {
                    if (!uriExists(logUri)) {
                        lollipopCreateFileForOutputStream(false, logUri, null, "Settings", "", "fileHistory.csv");
                        writeThis = topline + "\n" + logText;
                    }
                    OutputStream outputStream;
                    if (getFileSizeFromUri(logUri) > 300) {
                        writeThis = topline + "\n" + logText;
                        outputStream = c.getContentResolver().openOutputStream(logUri, "wt");
                    } else {
                        outputStream = c.getContentResolver().openOutputStream(logUri, "wa");
                    }
                    mainActivityInterface.getStorageAccess().writeFileFromString(writeThis + "\n", outputStream);
                } else {
                    Log.d(TAG, "logUri was null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String fixNull(String text) {
        if (text==null) {
            return "";
        } else {
            return text;
        }
    }

    public void setFileWriteLog(boolean fileWriteLog) {
        this.fileWriteLog = fileWriteLog;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("fileWriteLog",fileWriteLog);
    }
    public boolean getFileWriteLog() {
        return fileWriteLog;
    }
    public void setFileViewLog(boolean fileViewLog) {
        this.fileViewLog = fileViewLog;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("fileViewLog",fileViewLog);
    }
    public boolean getFileViewLog() {
        return fileViewLog;
    }

}