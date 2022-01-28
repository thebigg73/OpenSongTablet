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
import android.content.Intent;
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
import android.webkit.MimeTypeMap;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class StorageAccess {

    public final String appFolder = "OpenSong";
    private final String TAG = "StorageAccess";
    private final String[] rootFolders = {"Backgrounds", "Export", "Highlighter", "Images", "Media",
            "Notes", "OpenSong Scripture", "Pads", "Profiles", "Received", "Scripture",
            "Sets", "Settings", "Slides", "Songs", "Variations", "Backups"};
    private final String[] cacheFolders = {"Backgrounds/_cache", "Images/_cache", "Notes/_cache",
            "OpenSong Scripture/_cache", "Scripture/_cache", "Slides/_cache", "Variations/_cache"};
    private Uri uriTree = null, uriTreeHome = null; // This is the home folder.  Set as required from preferences.

    // These are used primarily on start up to initialise stuff
    private String getStoragePreference(Context c, MainActivityInterface mainActivityInterface) {
        return mainActivityInterface.getPreferences().getMyPreferenceString(c, "uriTree", null);
    }


    // Used to decide on the best storage method (using tree or not)
    public boolean lollipopOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }


    // This gets the uri for the uriTreeHome (the uri of the ..../OpenSong folder
    // This may or may not be the same as uriTree as this could be the parent folder
    public Uri homeFolder(Context c, Uri uri, MainActivityInterface mainActivityInterface) {
        // The user specified a storage folder when they started the app
        // However, this might not be the OpenSong folder, but the folder containing it
        // This function is called once when the app starts and fixes that
        // We need a reference to the OpenSong as the root for the app

        String uriTree_String;
        if (uri == null) {
            // No uri has been sent, so retrieve it from the preferences
            uriTree_String = getStoragePreference(c, mainActivityInterface);
        } else {
            // Use the uri sent as the base start point
            uriTree_String = uri.toString();
        }

        try {
            if (uriTree_String != null) {
                if (lollipopOrLater()) {
                    uri = homeFolder_SAF(c, uriTree_String);
                } else {
                    uri = homeFolder_File(uriTree_String);
                }
            } else {
                uri = null;
            }

        } catch (Exception e) {
            // Could be called if the uri stored is for a different version of Android. e.g. after upgrade
            uri = null;
        }
        return uri;
    }
    public boolean uriTreeValid(Context c, Uri uri) {
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
    private Uri homeFolder_SAF(Context c, String uriTree_String) {
        // When using a document tree, the uri needed for DocumentsContract is more complex than the uri chosen.
        // Create a document file to get a contract uri

        Uri uri = Uri.parse(uriTree_String);
        if (uri != null) {
            DocumentFile df = documentFileFromRootUri(c, uri, uriTree_String);
            if (df == null || !df.exists()) {
                uri = null;
            } else {
                uri = df.getUri();
                uriTree = uri;
            }

            // If uri doesn't end with /OpenSong/, fix that
            if (uri != null && uri.getLastPathSegment() != null && !uri.getLastPathSegment().endsWith("OpenSong")) {
                String s = uri.toString();
                s = s + "%2FOpenSong";
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

        if (!uriTree_String.endsWith("OpenSong") && !uriTree_String.endsWith("OpenSong/")) {
            uriTree_String = uriTree_String + "/OpenSong/";
            uriTree_String = uriTree_String.replace("//OpenSong/", "/OpenSong/");


            f = new File(uriTree_String);
            if (f.mkdirs()) {
                Log.d(TAG, "Created or identified OpenSong folder");
            }
        } else {
            f = new File(uriTree_String);
        }
        return Uri.fromFile(f);
    }


    // Sort the initial default folders and files needed when the app installs changes storage location
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public String createOrCheckRootFolders(Context c, Uri uri, MainActivityInterface mainActivityInterface) {
        // uri if the uriTree.  If not sent/null, load from preferences
        if (uri == null) {
            uri = Uri.parse(mainActivityInterface.getPreferences().getMyPreferenceString(c, "uriTree", ""));
        }

        if (lollipopOrLater()) {
            return createOrCheckRootFolders_SAF(c, uri, mainActivityInterface);
        } else {
            return createOrCheckRootFolders_File(c, mainActivityInterface);
        }
    }
    private String createOrCheckRootFolders_File(Context c, MainActivityInterface mainActivityInterface) {
        File rootFolder = new File(stringForFile(c, mainActivityInterface, ""));
        // Go through the main folders and try to create
        for (String folder : rootFolders) {
            File nf = new File(rootFolder, folder);
            if (!nf.exists()) {
                if (!nf.mkdirs()) {
                    Log.d("d", "Error creating folder: " + folder);
                }
            }
        }

        // Go through the sub folders and try to create
        for (String subfolder : cacheFolders) {
            File nf = new File(rootFolder, subfolder);
            if (!nf.exists()) {
                if (!nf.mkdirs()) {
                    Log.d("d", "Error creating subfolder: " + subfolder);
                }
            }
        }
        copyAssets(c, mainActivityInterface);
        return "Success";
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private String createOrCheckRootFolders_SAF(Context c, Uri uri, MainActivityInterface mainActivityInterface) {
        uriTreeHome = homeFolder(c, uri, mainActivityInterface);

        // Check the OpenSong folder exists (may be specified in the uriTree and/or uriTreeHome
        if (uriTree != null && uriTree.getLastPathSegment() != null &&
                (!uriTree.getLastPathSegment().endsWith("OpenSong") || !uriTree.getLastPathSegment().endsWith("OpenSong/"))) {
            // We need to check the uri points to the OpenSong folder
            if (!uriExists(c, uriTreeHome)) {
                try {
                    uriTreeHome = DocumentsContract.createDocument(c.getContentResolver(), uriTree, DocumentsContract.Document.MIME_TYPE_DIR, "OpenSong");
                } catch (Exception e) {
                    Log.d(TAG, "Unable to create OpenSong folder at " + uriTree);
                }
            }
        }

        // Go through the main folders and try to create them
        for (String folder : rootFolders) {
            try {
                Uri thisFolder = getUriForItem(c, mainActivityInterface, folder, "", "");
                if (!uriExists(c, thisFolder)) {
                    DocumentsContract.createDocument(c.getContentResolver(), uriTreeHome, DocumentsContract.Document.MIME_TYPE_DIR, folder);
                }
            } catch (Exception e) {
                Log.d("d", folder + " error creating");
            }
        }

        // Now for the cache folders
        for (String folder : cacheFolders) {
            String[] bits = folder.split("/");
            try {
                Uri dirUri = getUriForItem(c, mainActivityInterface, bits[0], "", "");
                Uri thisFolder = getUriForItem(c, mainActivityInterface, bits[0], bits[1], "");
                if (!uriExists(c, thisFolder)) {
                    try {
                        DocumentsContract.createDocument(c.getContentResolver(), dirUri, DocumentsContract.Document.MIME_TYPE_DIR, bits[1]);
                    } catch (Exception e3) {
                        Log.d(TAG, "Error creating folder at " + thisFolder);
                    }
                }
            } catch (Exception e2) {
                Log.d("d", "Error creating cache: " + folder);
            }
        }

        // Now copy the assets if they aren't already there
        copyAssets(c, mainActivityInterface);
        return "Success";
    }
    private void copyAssets(Context c, MainActivityInterface mainActivityInterface) {
        try {
            // Copies the background assets
            AssetManager assetManager = c.getAssets();
            String[] files = new String[2];
            files[0] = "backgrounds/ost_bg.png";
            files[1] = "backgrounds/ost_logo.png";
            Uri backgrounds = getUriForItem(c, mainActivityInterface, "Backgrounds", "", "");

            DocumentFile df = documentFileFromUri(c, backgrounds, backgrounds.getPath());
            for (String filename : files) {
                String filetocopy = filename.replace("backgrounds/", "");
                // See if they are already there first
                Uri uritocheck = getUriForItem(c, mainActivityInterface, "Backgrounds", "", filetocopy);
                if (!uriExists(c, uritocheck)) {
                    if (lollipopOrLater()) {
                        DocumentsContract.createDocument(c.getContentResolver(), backgrounds, "image/png", filetocopy);
                    } else {
                        df.createFile("image/png", filetocopy);
                    }
                    if (uritocheck != null) {
                        OutputStream out = getOutputStream(c, uritocheck);
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


    // Deal with parsing, creating, editing file and folder names
    // This gets the File location for the app as a String (for appending).  PreLollipop only
    public String[] niceUriTree(Context c, MainActivityInterface mainActivityInterface, Uri uri) {
        if (lollipopOrLater()) {
            return niceUriTree_SAF(c, mainActivityInterface, uri, new String[]{"", ""});
        } else {
            return niceUriTree_File(c, uri, new String[]{"", ""});
        }
    }
    private String[] niceUriTree_SAF(Context c, MainActivityInterface mainActivityInterface, Uri uri, String[] storageDetails) {
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
                ArrayList<String> songIds = listSongs(c, mainActivityInterface);

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
    public String[] niceUriTree_File(Context c, Uri uri, String[] storageDetails) {
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
    private String stringForFile(Context c, MainActivityInterface mainActivityInterface, String folderpath) {
        if (uriTreeHome == null) {
            String uriTreeHome_String = mainActivityInterface.getPreferences().getMyPreferenceString(c, "uriTreeHome", "");
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
    private String[] fixFoldersAndFiles(Context c, String folder, String subfolder, String filename) {
        // This fixes incorrect folders that would cause problems
        String[] returnvals = new String[3];
        if (subfolder.startsWith("**")) {
            // This is used when custom slides are created as part of a set, making the folder look more obvious
            subfolder = subfolder.replace("**", "../");
            subfolder = subfolder.replace("Images", "Images/_cache");
            subfolder = subfolder.replace("Slides", "Slides/_cache");
            subfolder = subfolder.replace("Scripture", "Scripture/_cache");
            subfolder = subfolder.replace("Variations", "Variation");
            subfolder = subfolder.replace("Variation", "Variations");
            subfolder = subfolder.replace("Notes", "Notes/_cache");
        }
        if (subfolder.contains("../")) {
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

        if (subfolder.equals(c.getResources().getString(R.string.mainfoldername)) || subfolder.equals("MAIN")) {
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
        filename = filename.replaceAll("[*?/<>&!#$+\"':{}@\\\\]", " "); // Removes bad characters
        filename = filename.replaceAll("\\s{2,}", " ");  // Removes double spaces
        return filename.trim();  // Returns the trimmed value
    }
    public Uri fixLocalisedUri(Context c, MainActivityInterface mainActivityInterface, String uriString) {
        // This checks for localised filenames first and fixes the Uri
        if (uriString.equals("ost_logo.png") || uriString.equals("ost_bg.png")) {
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
            return getUriForItem(c,mainActivityInterface,rootFolder,"",uriString);
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
            uriString = uriString.substring(uriString.indexOf("OpenSong/Songs/") + 15);
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
        if (path != null && path.contains("OpenSong/Songs")) {
            path = path.substring(path.lastIndexOf("OpenSong/Songs"));
        }
        return path;
    }


    // Get information about the files
    public String getUTFEncoding(Context c, Uri uri) {
        // Try to determine the BOM for UTF encoding
        String utf = "UTF-8";
        InputStream is = null;
        try {
            is = getInputStream(c, uri);
            utf = getBOMEncoding(is);
        } catch (Exception e) {
            Log.d("d", "Unable to get encoding for " + uri);
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
    public float getFileSizeFromUri(Context c, Uri uri) {
        if (lollipopOrLater()) {
            return getFileSizeFromUri_SAF(c, uri);
        } else {
            return getFileSizeFromUri_File(uri);
        }
    }
    private float getFileSizeFromUri_SAF(Context c, Uri uri) {
        DocumentFile df = documentFileFromUri(c, uri, uri.getPath());
        if (df != null && df.exists()) {
            return (float) df.length() / (float) 1024;
        } else {
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
    public boolean uriExists(Context c, Uri uri) {
        if (lollipopOrLater()) {
            return uriExists_SAF(c, uri);
        } else {
            return uriExists_File(c, uri);
        }
    }
    private boolean uriExists_SAF(Context c, Uri uri) {
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
    private boolean uriExists_File(Context c, Uri uri) {
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
                    c.getContentResolver().openInputStream(uri);
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
    }
    public boolean uriIsFile(Context c, Uri uri) {
        if (lollipopOrLater()) {
            return uriIsFile_SAF(c, uri);
        } else {
            return uriIsFile_File(uri);
        }
    }
    private boolean uriIsFile_SAF(Context c, Uri uri) {
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
    public boolean canWrite(Context c, Uri uri) {
        if (lollipopOrLater()) {
            return canWrite_SAF(c, uri);
        } else {
            return canWrite_File(uri);
        }
    }
    private boolean canWrite_SAF(Context c, Uri uri) {
        DocumentFile df = DocumentFile.fromSingleUri(c, uri);
        if (df != null) {
            return df.canWrite();
        } else {
            return false;
        }
    }
    private boolean canWrite_File(Uri uri) {
        if (uri != null && uri.getPath() != null) {
            File f = new File(uri.getPath());
            return f.canWrite();
        } else {
            return false;
        }
    }
    boolean containsXMLTags(Context c, Uri uri) {
        try {
            boolean found = false;
            String utf = getUTFEncoding(c, uri);
            InputStream inputStream = getInputStream(c, uri);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(inputStream, utf);
            int eventType;
            eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT && !found) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("lyrics")) {
                        found = true; // It's a song
                        Log.d(TAG,"found song");
                    } else if (xpp.getName().equals("set")) {
                        found = true; // It's a set
                        Log.d(TAG,"found set");
                    }
                }
                // If it isn't an xml file, an error is about to be thrown
                try {
                    eventType = xpp.next();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return found;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean isTextFile(Uri uri) {
        boolean istext = false;
        if (uri != null && uri.getLastPathSegment() != null) {
            String name = uri.getLastPathSegment().toLowerCase(Locale.ROOT);
            if ((!name.contains(".pdf") && !name.contains(".doc") &&
                    !name.contains(".docx") && !name.contains(".png") &&
                    !name.contains(".jpg") && !name.contains(".gif") &&
                    !name.contains(".jpeg")) || name.endsWith(".txt")) {
                istext = true;
            }
        }
        return istext;
    }
    boolean checkFileExtensionValid(Uri uri) {
        // This lets us know if the file is appropriate to read the title/author/key from during indexing
        String filename;
        if (uri != null && uri.getLastPathSegment() != null) {
            filename = uri.getLastPathSegment().toLowerCase(Locale.ROOT);
        } else {
            filename = "";
        }
        boolean isvalid = true;
        String type = null;
        if (filename.lastIndexOf(".") > 1 && filename.lastIndexOf(".") < filename.length() - 1) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            int index = filename.lastIndexOf('.') + 1;
            String ext = filename.substring(index).toLowerCase(Locale.ROOT);
            type = mime.getMimeTypeFromExtension(ext);
        }

        if (type != null && !type.equals("")) {
            if (type.contains("image") || type.contains("application") || type.contains("pdf") || type.contains("video") || type.contains("audio")) {
                isvalid = false;
            }
        }

        if (filename.endsWith(".pdf") || filename.endsWith(".jpg") ||
                filename.endsWith(".png") || filename.endsWith(".gif") ||
                filename.endsWith(".doc") || filename.endsWith(".docx") ||
                filename.endsWith(".zip") || filename.endsWith(".apk") ||
                filename.endsWith(".tar") || filename.endsWith(".backup")) {
            isvalid = false;
        }
        return isvalid;
    }
    public boolean determineFileTypeByExtension(Song song) {
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
        } else if (file_ext.endsWith(".jpg") || file_ext.endsWith(".bmp") ||
                file_ext.endsWith(".png") || file_ext.endsWith(".gif")) {
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
    public Uri getUriForItem(Context c, MainActivityInterface mainActivityInterface, String folder, String subfolder, String filename) {
            String[] fixedfolders = fixFoldersAndFiles(c, folder, subfolder, filename);
        if (lollipopOrLater()) {
            return getUriForItem_SAF(c, mainActivityInterface, fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        } else {
            return getUriForItem_File(c, mainActivityInterface, fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        }
    }
    @SuppressLint("NewApi")
    private Uri getUriForItem_SAF(Context c, MainActivityInterface mainActivityInterface, String folder, String subfolder, String filename) {
        // Get the home folder as our start point
        if (uriTreeHome == null) {
            uriTreeHome = homeFolder(c, null, mainActivityInterface);
        }
        Uri uri = uriTreeHome;

        // Now point to the specific folder (Songs, Sets, Backgrounds, etc.)
        if (folder != null && !folder.isEmpty() && uri != null) {
            uri = Uri.withAppendedPath(uri, Uri.encode(folder));
        }

        // Now go through the subfolder(s)
        if (subfolder != null && !subfolder.equals(c.getString(R.string.mainfoldername)) && !subfolder.equals("MAIN") && uri != null) {
            String[] sfs = subfolder.split("/");
            for (String sf : sfs) {
                if (sf != null && !sf.equals("") && !sf.equals(c.getString(R.string.mainfoldername)) && !sf.equals("MAIN")) {
                    uri = Uri.withAppendedPath(uri, Uri.encode(sf));
                }
            }
        }

        // Now add the filename
        if (filename != null && !filename.equals("") && uri != null) {
            // Might have sent subfolder info
            String[] sfs = filename.split("/");
            for (String sf : sfs) {
                if (sf != null && !sf.equals("") && !sf.equals(c.getString(R.string.mainfoldername)) && !sf.equals("MAIN")) {
                    uri = Uri.withAppendedPath(uri, Uri.encode(sf));
                }
            }
        }

        // Now return the Uri in encoded format
        if (uri != null) {

            String uristring = uri.toString();

            int pos = uristring.lastIndexOf("OpenSong/");
            if (pos > 0) {
                String start = uristring.substring(0, pos);
                String end = uristring.substring(pos);
                end = end.replace("/", "%2F");
                uristring = start + end;
                uri = Uri.parse(uristring);
            }
        }
        return uri;
    }
    private Uri getUriForItem_File(Context c, MainActivityInterface mainActivityInterface, String folder, String subfolder, String filename) {
        String s = stringForFile(c, mainActivityInterface, folder);
        File f = new File(s);
        if (subfolder != null && !subfolder.isEmpty() && !subfolder.equals(c.getString(R.string.mainfoldername)) && !subfolder.equals("MAIN")) {
            f = new File(f, subfolder);
        }
        if (filename != null && !filename.isEmpty() && !filename.equals(c.getString(R.string.mainfoldername)) && !filename.equals("MAIN")) {
            f = new File(f, filename);
        }
        return Uri.fromFile(f);
    }
    public InputStream getInputStream(Context c, Uri uri) {
        try {
            return c.getContentResolver().openInputStream(uri);
        } catch (Exception e) {
            return null;
        }
    }
    public OutputStream getOutputStream(Context c, Uri uri) {
        if (uriExists(c, uri) || !lollipopOrLater()) {
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
    Uri getFileProviderUri(Context c, MainActivityInterface mainActivityInterface, String folder, String subfolder, String filename) {
        if (lollipopOrLater()) {
            return getFileProviderUri_SAF(c, mainActivityInterface, folder, subfolder, filename);
        } else {
            return getFileProviderUri_File(c, mainActivityInterface, folder, subfolder, filename);
        }
    }
    private Uri getFileProviderUri_SAF(Context c, MainActivityInterface mainActivityInterface, String folder, String subfolder, String filename) {
        // No real need as we are using content uris anyway
        return getUriForItem(c, mainActivityInterface, folder, subfolder, filename);
    }
    private Uri getFileProviderUri_File(Context c, MainActivityInterface mainActivityInterface, String folder, String subfolder, String filename) {
        String s = stringForFile(c, mainActivityInterface, folder);
        File f = new File(s);
        if (subfolder != null && !subfolder.isEmpty() && !subfolder.equals(c.getString(R.string.mainfoldername)) && !subfolder.equals("MAIN")) {
            f = new File(f, subfolder);
        }
        if (filename != null && !filename.isEmpty() && !filename.equals(c.getString(R.string.mainfoldername)) && !filename.equals("MAIN")) {
            f = new File(f, filename);
        }
        // Convert to a FileProvider uri
        return FileProvider.getUriForFile(c, "OpenSongAppFiles", f);
    }
    public DocumentFile documentFileFromRootUri(Context c, Uri uri, String path) {
        if (uri != null && lollipopOrLater()) {
            return DocumentFile.fromTreeUri(c, uri);
        } else if (path != null && !lollipopOrLater()) {
            File f = new File(path);
            return DocumentFile.fromFile(f);
        } else {
            return null;
        }
    }
    public DocumentFile documentFileFromUri(Context c, Uri uri, String path) {
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
    private boolean docContractCreate(Context c, Uri uri, String mimeType, String name) {
        try {
            return DocumentsContract.createDocument(c.getContentResolver(), uri, mimeType, name) != null;
        } catch (Exception e) {
            Log.d(TAG, "Error creating " + name + " at " + uri);
            return false;
        }
    }
    public String getUriString(Uri uri) {
        String uriString = "";
        if (uri!=null) {
            uriString = uri.toString();
            if (uriString.contains("OpenSong/")) {
                // Localised preLollipop
                uriString = uriString.substring(uriString.lastIndexOf("OpenSong/")+9);
                uriString = "../" + uriString;
            } else if (uriString.contains("OpenSong%2F")) {
                // Localised storageAccessFramework
                uriString = uriString.substring(uriString.lastIndexOf("OpenSong%2F")+11);
                uriString = uriString.replace("%2F","/");
                uriString = uriString.replace("%20"," ");
                uriString = "../" + uriString;
            }
        }
        Log.d(TAG,"uriString="+uriString);
        return uriString;
    }

    // Basic file actions (read, create, copy, delete, write)
    public boolean saveSongFile(Context c, MainActivityInterface mainActivityInterface) {
        Log.w(TAG,"saveSongFile() called");
        // This is called from the SaveSong class and uses the current Song object in MainActivity
        // First get the song uri
        // Because it may not be in the songs folder, lets check!
        ArrayList<String> newLocation = fixNonSongs(mainActivityInterface.getSong().getFolder());
        // Write the string file
        return doStringWriteToFile(c,mainActivityInterface,newLocation.get(0), newLocation.get(1),
                mainActivityInterface.getSong().getFilename(),
                mainActivityInterface.getProcessSong().getXML(c,mainActivityInterface, mainActivityInterface.getSong()));
    }
    public ArrayList<String> fixNonSongs(String folderToCheck) {
        // Return any subfolder and change the 'Songs' folder as required
        ArrayList<String> fixedFolders = new ArrayList<>();
        String where = "Songs";
        if (folderToCheck.contains("../")) {
            where = folderToCheck.replace("../","");
            if (where.contains("_cache")) {
                folderToCheck = "_cache";
                where = where.substring(0,where.indexOf("/_cache"));
            } else {
                folderToCheck = "";
                where = folderToCheck;
            }
        }
        fixedFolders.add(where);
        fixedFolders.add(folderToCheck);
        return fixedFolders;
    }
    public String[] getActualFoldersFromNice(Context c, String folder) {
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
        }
        return location;
    }
    public void lollipopCreateFileForOutputStream(Context c, MainActivityInterface mainActivityInterface, Uri uri, String mimeType, String folder, String subfolder, String filename) {
        if (lollipopOrLater() && !uriExists(c, uri)) {
            // Only need to do this for Lollipop or later
            createFile(c, mainActivityInterface, mimeType, folder, subfolder, filename);
        } else if (!lollipopOrLater() && !uriExists(c, uri)) {
            // Check it exists
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
    public Uri copyFromTo(Context c, MainActivityInterface mainActivityInterface, String fromFolder, String fromSubfolder, String fromName, String toFolder, String toSubfolder, String toName) {
        Uri fromUri = getUriForItem(c,mainActivityInterface, fromFolder, fromSubfolder, fromName);
        Uri toUri = getUriForItem(c, mainActivityInterface, toFolder, toSubfolder, toName);
        // Make sure the newUri is valid and exists
        lollipopCreateFileForOutputStream(c,mainActivityInterface,toUri,null,toFolder,toSubfolder,toName);
        // Get the input and output streams
        InputStream inputStream = getInputStream(c, fromUri);
        OutputStream outputStream = getOutputStream(c, toUri);
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
    public boolean doStringWriteToFile(Context c, MainActivityInterface mainActivityInterface, String folder, String subfolder, String filename, String string) {
        try {
            Uri uri = getUriForItem(c, mainActivityInterface, folder, subfolder, filename);
            // If the file exists, delete it, otherwise it doesn't work
            if (uriExists(c,uri)) {
                deleteFile(c, uri);
            }
            lollipopCreateFileForOutputStream(c, mainActivityInterface, uri, null, folder, subfolder, filename);
            OutputStream outputStream = getOutputStream(c, uri);
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
            if (outputStream!=null) {
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
            bmp.recycle();
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
            } catch (Exception e) {
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

        /*
        // Older method
        if (in != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            try {
                while ((len = in.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.flush();
                outputStream.close();
                in.close();
            } catch (Exception e) {
                Log.d(TAG, "Error reading text file");
                e.printStackTrace();
                return "";
            } catch (OutOfMemoryError e2) {
                e2.printStackTrace();
                return "";
            }
            try {
                return outputStream.toString();
            } catch (Exception | OutOfMemoryError e) {
                return "";
            }*/

        } else {
            return "";
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean createFile(Context c, MainActivityInterface mainActivityInterface, String mimeType, String folder, String subfolder, String filename) {
        String[] fixedfolders = fixFoldersAndFiles(c, folder, subfolder, filename);
        if (lollipopOrLater()) {
            return createFile_SAF(c, mainActivityInterface, mimeType, fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        } else {
            return createFile_File(c, mainActivityInterface, fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean createFile_SAF(Context c, MainActivityInterface mainActivityInterface, String mimeType, String folder, String subfolder, String filename) {
        // Try this instead
        Uri parentUri;
        folder = removeStartAndEndSlashes(folder);
        subfolder = removeStartAndEndSlashes(subfolder);
        filename = removeStartAndEndSlashes(filename);

        Uri uritest = getUriForItem(c, mainActivityInterface, folder, subfolder, filename);

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

            parentUri = getUriForItem(c, mainActivityInterface, folder, subfolder, "");

            // Only create if it doesn't exist
            if (!uriExists(c, uritest)) {
                if (!docContractCreate(c, parentUri, mimeType, foldertocreate)) {
                    // Error (likely parent directory doesn't exist
                    // Go through each folder and create the ones we need starting
                    String[] bits = subfolder.split("/");
                    String bit = "";
                    for (String s : bits) {
                        parentUri = getUriForItem(c, mainActivityInterface, folder, bit, "");
                        docContractCreate(c, parentUri, mimeType, s);
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

            parentUri = getUriForItem(c, mainActivityInterface, folder, completesubfolder, "");

            if (uriTreeHome == null) {
                uriTreeHome = homeFolder(c, null, mainActivityInterface);
            }

            if (folder.isEmpty() && completesubfolder.isEmpty()) {
                // A temp file in the root folder
                docContractCreate(c, uriTreeHome, null, filename);

            } else if (!uriExists(c, uritest)) {

                if (!docContractCreate(c, parentUri, mimeType, completefilename)) {
                    // Error (likely parent directory doesn't exist)
                    // Go through each folder and create the ones we need starting at the 'folder'
                    String[] bits = completesubfolder.split("/");
                    String bit = "";
                    for (String s : bits) {
                        parentUri = getUriForItem(c, mainActivityInterface, folder, bit, "");
                        docContractCreate(c, parentUri, DocumentsContract.Document.MIME_TYPE_DIR, s);
                        bit = bit + "/" + s;
                    }
                    // Try again!
                    parentUri = getUriForItem(c, mainActivityInterface, folder, completesubfolder, "");
                    return docContractCreate(c, parentUri, mimeType, completefilename);
                }
            }
        }
        return true;
    }
    private boolean createFile_File(Context c, MainActivityInterface mainActivityInterface, String folder, String subfolder, String filename) {
        boolean stuffCreated = false;
        String filepath = stringForFile(c, mainActivityInterface, folder);
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
            try {
                stuffCreated = f.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                stuffCreated = false;
            }
        }
        return stuffCreated;
    }
    public boolean deleteFile(Context c, Uri uri) {
        if (lollipopOrLater()) {
            return deleteFile_SAF(c, uri);
        } else {
            return deleteFile_File(uri);
        }
    }
    private boolean deleteFile_SAF(Context c, Uri uri) {
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
    public boolean doDeleteFile(Context c, MainActivityInterface mainActivityInterface, String location, String subfolder, String filename) {
        Uri uri = getUriForItem(c, mainActivityInterface, location, subfolder, filename);
        return deleteFile(c, uri);
    }
    boolean renameSetFile(Context c, MainActivityInterface mainActivityInterface, String oldname, String newname) {
        String folder = "Sets";
        String oldsubfolder = "";
        String newsubfolder = "";
        Uri olduri = getUriForItem(c, mainActivityInterface, folder, oldsubfolder, oldname);
        InputStream inputStream = getInputStream(c, olduri);
        Uri newuri = getUriForItem(c, mainActivityInterface, folder, newsubfolder, newname);
        if (!uriExists(c, newuri)) {
            // Create a new blank file ready
            createFile(c, mainActivityInterface, null, folder, newsubfolder, newname);
        }
        OutputStream outputStream = getOutputStream(c, newuri);
        try {
            // Copy the file
            copyFile(inputStream, outputStream);
            // All is good, so delete the old one
            if (lollipopOrLater()) {
                DocumentFile df = DocumentFile.fromSingleUri(c, olduri);
                if (df != null) {
                    return df.delete();
                } else {
                    return false;
                }
            } else {
                if (olduri.getPath() != null) {
                    File f = new File(olduri.getPath());
                    return f.delete();
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public String getImageSlide(Context c, String loc) {
        String b = "";
        Uri uri = Uri.parse(loc);
        if (uriExists(c, uri)) {
            try {
                InputStream inputStream = getInputStream(c, uri);
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
    public Intent selectFileIntent(String[] mimeTypes) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        if (mimeTypes != null && mimeTypes.length > 0) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        }
        return intent;
    }
    public String getActualFilename(Context c, String string) {
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
    public boolean renameFileFromUri(Context c, Uri oldUri, Uri newUri, String newName) {
        if (lollipopOrLater()) {
            return renameFileFromUri_SAF(c, oldUri, newName);
        } else {
            return renameFileFromUri_File(oldUri, newUri);
        }
    }
    private boolean renameFileFromUri_File(Uri oldUri, Uri newUri) {
        File file = new File(oldUri.getPath());
        return file.renameTo(new File(newUri.getPath()));
    }
    private boolean renameFileFromUri_SAF(Context c, Uri oldUri, String newName) {
        DocumentFile documentFile = DocumentFile.fromTreeUri(c,oldUri);
        if (documentFile!=null) {
            return documentFile.renameTo(newName);
        } else {
            return false;
        }
    }
    public String getFileNameFromUri(Context c, Uri uri) {
        if (uri!=null) {
            String scheme = uri.getScheme();
            if (scheme.equals("file")) {
                return uri.getLastPathSegment();
            } else if (scheme.equals("content")) {
                Cursor cursor = c.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int i = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (i >= 0) {
                        return cursor.getString(i);
                    } else {
                        return uri.getLastPathSegment();
                    }
                } else {
                    return uri.toString();
                }
            } else {
                return uri.toString();
            }
        } else {
            return "";
        }
    }

    // Actions for folders (create, delete, rename, clear)
    boolean renameFolder(Context c, MainActivityInterface mainActivityInterface, ShowToast showToast, Song song, String oldsubfolder, String newsubfolder) {
        if (lollipopOrLater()) {
            return renameFolder_SAF(c, mainActivityInterface, showToast, song, oldsubfolder, newsubfolder);
        } else {
            return renameFolder_File(c, mainActivityInterface, showToast, song, oldsubfolder, newsubfolder);
        }
    }
    private boolean renameFolder_File(Context c, MainActivityInterface mainActivityInterface, ShowToast showToast,
                                      Song song, String oldsubfolder, String newsubfolder) {
        // Now the long bit.  Go through the original folder and copy the files to the new location
        Uri oldUri = getUriForItem(c, mainActivityInterface, "Songs", oldsubfolder, "");
        Uri newUri = getUriForItem(c, mainActivityInterface, "Songs", newsubfolder, "");
        if (!uriExists(c, newUri)) {
            if (oldUri != null && newUri != null && oldUri.getPath() != null && newUri.getPath() != null) {
                File oldfile = new File(oldUri.getPath());
                File newfile = new File(newUri.getPath());
                if (oldfile.renameTo(newfile)) {
                    showToast.doIt(c.getString(R.string.rename) + " - " +
                            c.getString(android.R.string.ok));
                    song.setFolder(newsubfolder);
                    return true;
                } else {
                    showToast.doIt(c.getString(R.string.rename) + " - " +
                            c.getString(R.string.create_folder_error));
                    return false;
                }
            } else {
                showToast.doIt(c.getString(R.string.rename) + " - " +
                        c.getString(R.string.create_folder_error));
                return false;
            }
        } else {
            showToast.doIt(c.getString(R.string.rename) +
                    " - " + c.getString(R.string.folder_exists));
            return false;
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean renameFolder_SAF(Context c, MainActivityInterface mainActivityInterface, ShowToast showToast,
                                     Song song, String oldsubfolder, String newsubfolder) {
        // SAF can only rename final name (can't move within directory structure) - No / allowed!
        Uri oldUri = getUriForItem(c, mainActivityInterface, "Songs", oldsubfolder, "");
        // Only rename the last section
        if (newsubfolder.contains("/")) {
            newsubfolder = newsubfolder.substring(newsubfolder.lastIndexOf("/"));
            newsubfolder = newsubfolder.replace("/", "");
        }
        try {
            DocumentsContract.renameDocument(c.getContentResolver(), oldUri, newsubfolder);
            showToast.doIt(c.getString(R.string.rename) + " - " +
                    c.getString(android.R.string.ok));
            song.setFolder(newsubfolder);
            return true;
        } catch (Exception e) {
            showToast.doIt(c.getString(R.string.rename) + " - " +
                    c.getString(R.string.create_folder_error));
            return false;
        }
    }
    public void wipeFolder(Context c, MainActivityInterface mainActivityInterface,
                           String folder, String subfolder) {
        Uri uri = getUriForItem(c,mainActivityInterface,folder,subfolder,null);
        if (uriExists(c,uri)) {
            if (lollipopOrLater()) {
                wipeFolder_SAF(c,uri);
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
    public void wipeFolder_SAF(Context c, Uri uri) {
        // Get a contract for the desired folder
        Uri desiredUri = DocumentsContract.buildDocumentUriUsingTree(uriTreeHome,DocumentsContract.getDocumentId(uri));
        try {
            DocumentsContract.deleteDocument(c.getContentResolver(), desiredUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean createFolder(Context c, MainActivityInterface mainActivityInterface, String currentDir, String currentSubDir, String newFolder) {
        // Get the uri for the parent
        Uri dirUri = getUriForItem(c, mainActivityInterface, currentDir, currentSubDir, "");
        if (lollipopOrLater()) {
            return createFolder_SAF(c, dirUri, newFolder);
        } else {
            return createFolder_File(dirUri, newFolder);
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean createFolder_SAF(Context c, Uri dirUri, String newFolder) {
        try {
            DocumentsContract.createDocument(c.getContentResolver(), dirUri, DocumentsContract.Document.MIME_TYPE_DIR, newFolder);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
    private boolean createFolder_File(Uri root, String newName) {
        File f = new File(root.getPath(), newName);
        return f.mkdirs();
    }
    public ArrayList<String> getSongFolders(Context c, ArrayList<String> songIDs, boolean addMain, String toIgnore) {
        ArrayList<String> availableFolders = new ArrayList<>();
        // Add the MAIN folder
        if (addMain) {
            songIDs.add(0, c.getString(R.string.mainfoldername) + "/");
        }
        for (String entry : songIDs) {
            if (entry.endsWith("/")) {
                String newtext = entry.substring(0, entry.lastIndexOf("/"));
                if (!newtext.equals(toIgnore) && !availableFolders.contains(newtext)) {
                    availableFolders.add(newtext);
                }
            }
        }
        Collections.sort(availableFolders);
        return availableFolders;
    }

    // This builds an index of all the songs on the device
    @SuppressLint("NewApi")
    public ArrayList<String> listSongs(Context c, MainActivityInterface mainActivityInterface) {
        ArrayList<String> noSongs = new ArrayList<>();
        // We need to make sure the locale version of MAIN is correct (change language during run)
        Configuration configuration = new Configuration(c.getResources().getConfiguration());
        configuration.setLocale(mainActivityInterface.getLocale());
        String mainfolder = c.createConfigurationContext(configuration).getResources().getString(R.string.mainfoldername);
        try {
            // Decide if we are using storage access framework or not
            if (lollipopOrLater()) {
                return listSongs_SAF(c, mainActivityInterface, mainfolder);
            } else {
                return listSongs_File(c, mainActivityInterface, mainfolder);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return noSongs;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ArrayList<String> listSongs_SAF(Context c, MainActivityInterface mainActivityInterface, String mainfolder) {
        // This gets all songs (including any subfolders)
        ArrayList<String> songIds = new ArrayList<>();
        Uri uri = getUriForItem(c, mainActivityInterface, "Songs", "", "");

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
        return songIds;
    }
    private ArrayList<String> listSongs_File(Context c, MainActivityInterface mainActivityInterface, String mainfolder) {
        // We must be using an older version of Android, so stick with File access
        ArrayList<String> songIds = new ArrayList<>();  // These will be the file locations

        String songsFolder = stringForFile(c, mainActivityInterface, "Songs");
        ArrayList<String> foldersToIndex = new ArrayList<>();

        // Add the main songs folder to the foldersToIndex.  More will be added for subfolders
        foldersToIndex.add(songsFolder);
        int num = foldersToIndex.size();
        // Go through all the folders
        for (int i = 0; i < num; i++) {
            File folder = new File(foldersToIndex.get(i));
            File[] contents = folder.listFiles();
            // Go through each item and add songs or folders
            assert contents != null;
            for (File item : Objects.requireNonNull(contents)) {
                if (item.isDirectory()) {
                    foldersToIndex.add(item.getPath());
                    songIds.add(songFolderAndFileOnly(item.getPath(), mainfolder) + "/");
                    num = foldersToIndex.size();
                } else if (item.isFile()) {
                    songIds.add(songFolderAndFileOnly(item.getPath(), mainfolder));
                }
            }
        }
        return songIds;
    }


    // Dealing with indexing the songs on the device
    public void writeSongIDFile(Context c, MainActivityInterface mainActivityInterface, ArrayList<String> songIds) {
        // This creates a file in the app storage with a list of song folders/filenames
        StringBuilder stringBuilder = new StringBuilder();

        // Sort the array
        Collator collator;
        if (mainActivityInterface.getLocale()==null) {
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
        songIDFile.delete();
        songIDFile = new File(c.getExternalFilesDir("Database"), "SongIds.txt");
        Uri uri = Uri.fromFile(songIDFile);
        boolean fileexists;
        if (!uriExists(c, uri)) {
            try {
                fileexists = songIDFile.createNewFile();
            } catch (Exception e) {
                fileexists = false;
            }
        } else {
            fileexists = true;
        }

        if (fileexists) {
            OutputStream outputStream = getOutputStream(c, Uri.fromFile(songIDFile));
            if (outputStream != null) {
                writeFileFromString(stringBuilder.toString(), outputStream);
            }
        }
    }
    public ArrayList<String> getSongIDsFromFile(Context c) {
        File songIDFile = new File(c.getExternalFilesDir("Database"), "SongIds.txt");
        Uri uri = Uri.fromFile(songIDFile);
        InputStream is = getInputStream(c, uri);
        String text = readTextFileToString(is);
        // Split the text into line and add to the new array
        ArrayList<String> songIDs = new ArrayList<>();
        String[] lines = text.split("\n");
        Collections.addAll(songIDs, lines);
        return songIDs;
    }
    @SuppressLint("NewApi")
    public ArrayList<String> listFilesInFolder(Context c, MainActivityInterface mainActivityInterface, String folder, String subfolder) {
        Log.d(TAG,"folder: "+folder+"  subfolder: "+subfolder);

        if (subfolder.startsWith("../") || subfolder.startsWith("**")) {
            folder = subfolder.replace("../","");
            folder = subfolder.replace("**","");
            subfolder = "";
        }

        Log.d(TAG,"folder: "+folder+"  subfolder: "+subfolder);
        if (lollipopOrLater()) {
            return listFilesInFolder_SAF(c, mainActivityInterface, folder, subfolder);
        } else {
            return listFilesInFolder_File(c, mainActivityInterface, folder, subfolder);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ArrayList<String> listFilesInFolder_SAF(Context c, MainActivityInterface mainActivityInterface, String folder, String subfolder) {
        ArrayList<String> al = new ArrayList<>();

        Uri locationtoindex = getUriForItem(c, mainActivityInterface, folder, subfolder, "");

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
        return al;
    }
    private ArrayList<String> listFilesInFolder_File(Context c, MainActivityInterface mainActivityInterface, String folder, String subfolder) {
        ArrayList<String> al = new ArrayList<>();
        String filebuilder = stringForFile(c, mainActivityInterface, folder);
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

}