package com.garethevans.church.opensongtablet.openchords;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.RetrofitInterface;
import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
import com.garethevans.church.opensongtablet.setprocessing.SetObject;
import com.garethevans.church.opensongtablet.setprocessing.SetSlideGroupObject;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


//TODO OpenChords setTags(String[] tags) isn't being used

public class OpenChordsAPI implements Callback<OpenChordsFolderObject> {
    public static final Gson gson = new Gson();

    // This deals with intents that allow us to POST and GET synchronise with JustChords using the OpenChords interface

    private final MainActivityInterface mainActivityInterface;
    private final Context c;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "OpenChordsAPI";
    @SuppressWarnings("FieldCanBeLocal")
    private final String getAppFolderTrigger = "opensongapp://openchords?folder=",
            openChordsApiBase = "https://openchords.net/api/folder/",
            openChordsFolderBaseShareable = "https://openchords.net/?fld=",
            songFolderUUIDsFile = "songFolderUUIDs.json";
    private boolean receivedFolderLink = false;

    // The retrofit, server and fragment declarations
    private final RetrofitInterface retrofitInterface;
    private OpenChordsFragment openChordsFragment;
    private boolean isServerResponse = false;
    private final String conflictCheckFile = "conflictCheck.json";
    private OpenChordsConflictCheck openChordsConflictCheck;
    private ArrayList<OpenChordsConflictObject> openChordsConflictObjects = new ArrayList<>();
    private OpenSongFolderObject openSongFolderObject;
    private ArrayList<OpenSongFolderRecordObject> openSongFolderRecordObjects = new ArrayList<>();
    private ArrayList<OpenChordsConflictItemObject> conflictItemRecords = new ArrayList<>();

    // Initialise the class
    public OpenChordsAPI(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        this.c = c;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(openChordsApiBase)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitInterface = retrofit.create(RetrofitInterface.class);
    }
    public void initialiseRecords() {
        // Deal with the OpenSongFolderObject
        loadAndCheckOpenSongFolderObject();

        // Deal with the conflict check json
        loadConflictObject();
    }
    public void initialiseOpenChordsFolderAndUuid() {
        // If we got here via an intent, we should look for a local folder matching the intent uuid
        if (receivedFolderLink) {
            // Look to see if we have a folder that matches the uuid already
            // The uuid was set in the intent already, so don't update that
            // Set our foldername to null or the matching folder
            openChordsFolderName = getOpenSongFolderNameFromUUID(openChordsFolderUuid);
            // If this isn't null, then we have a matching folder, so we can set that name
            // If it is null, we will get the new folder name from the server later
            if (openChordsFolderName!=null) {
                // Because we have a matching folder, we want to save our new preference
                mainActivityInterface.getPreferences().setMyPreferenceString(
                        "openChordsFolderName",openChordsFolderName);
            }
            // If we come to the openChordsFragment again, we will use our preference instead
            // (unless we receive an intent again)
            receivedFolderLink = false;
        } else {
            // We set this using a local folder, so just find the uuid using our save preference
            openChordsFolderName = mainActivityInterface.getPreferences().getMyPreferenceString(
                    "openChordsFolderName",mainActivityInterface.getMainfoldername());
            openChordsFolderUuid = getOpenSongFolderUuidFromName(openChordsFolderName);
        }
    }

    // Get a reference to the openChordsFragment
    public void setOpenChordsFragment(OpenChordsFragment openChordsFragment) {
        this.openChordsFragment = openChordsFragment;
    }

    // This is set true if we received an intent to get an OpenChords folder
    // It is set false after we query the server (regardless of outcome)
    public boolean getReceivedFolderLink() {
        return receivedFolderLink;
    }
    public void setReceivedFolderLink(boolean receivedFolderLink) {
        this.receivedFolderLink = receivedFolderLink;
    }

    // The objects retrieved from the server
    private OpenChordsFolderObject serverFolder;
    private String openChordsFolderName, openChordsFolderUuid, localFolderName;
    private ArrayList<OpenChordsSong> serverSongs = new ArrayList<>();
    private ArrayList<OpenChordsSetList> serverSetLists = new ArrayList<>();
    private ArrayList<OpenChordsTag> serverTags = new ArrayList<>();
    private final ArrayList<OpenChordsTag> newTagsForUpload = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> serverSongsCompareObjects = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> serverSetListsCompareObjects= new ArrayList<>();

    // The local objects
    private final ArrayList<OpenChordsSong> localSongs = new ArrayList<>();
    private final ArrayList<OpenChordsSetList> localSetLists = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> localSongsCompareObjects = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> localSetListsCompareObjects = new ArrayList<>();

    // The objects that hold the differences between the local and server
    private final ArrayList<OpenChordsCompareObject> songsNotOnLocal = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> setListsNotOnLocal = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> songsNotOnServer = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> setListsNotOnServer = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> songsOnLocalOlder = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> setListsOnLocalOlder = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> songsOnServerOlder = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> setListsOnServerOlder = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> localSongNeedsServerUUID = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> localSetListNeedsServerUUID = new ArrayList<>();

    // The objects for uploading
    private ArrayList<OpenChordsSong> songsForUpload = new ArrayList<>();
    private ArrayList<OpenChordsSetList> setsForUpload = new ArrayList<>();
    private ArrayList<OpenChordsTag> tagsForUpload = new ArrayList<>();

    // The variables used to display counts
    private int songsNotOnLocalCount = 0, songsNotOnServerCount = 0,
            songsOnLocalOlderCount = 0, songsOnServerOlderCount = 0, setListsNotOnLocalCount = 0,
            setListsNotOnServerCount = 0, setListsOnLocalOlderCount = 0, setListsOnServerOlderCount = 0;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private int songsWithNoChangesCount = 0, setListsWithNoChangesCount = 0;

    // Get and set the folder information
    public void getFolderContentsFromUUID() {
        receivedFolderLink = false;
        Call<OpenChordsFolderObject> call = retrofitInterface.getOpenChordsFolder(openChordsFolderUuid);
        call.enqueue(this);
    }
    public String getAppFolderTrigger() {
        return getAppFolderTrigger;
    }
    public String getOpenChordsFolderName() {
        return openChordsFolderName;
    }
    public String getOpenChordsFolderUuid() {
        return openChordsFolderUuid;
    }
    public Bitmap getOpenChordsQRCode() {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(getOpenChordsAddress(), BarcodeFormat.QR_CODE, 800, 800);

            int w = bitMatrix.getWidth();
            int h = bitMatrix.getHeight();
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    pixels[y * w + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
            return bitmap;
        } catch (Exception e) {
            mainActivityInterface.getStorageAccess().updateCrashLog(e.toString());
            return null;
        }
    }
    public String getOpenChordsAddress() {
        // The OpenChords shareable link
        return openChordsFolderBaseShareable + openChordsFolderUuid;
    }
    public void setOpenChordsFolderUuid(String openChordsFolderUuid) {
        this.openChordsFolderUuid = openChordsFolderUuid;
    }

    public void setOpenChordsFolderName(String openChordsFolderName) {
        this.openChordsFolderName = openChordsFolderName;
    }
    public String getLocalFolderName() {
        if (localFolderName==null) {
            return openChordsFolderName;
        } else {
            return localFolderName;
        }
    }
    public void setLocalFolderName(String localFolderName) {
        if (localFolderName==null) {
            this.localFolderName = openChordsFolderName;
        } else {
            this.localFolderName = localFolderName;
        }
    }

    // Create the local objects and populate the localCompareObjects
    private void createLocalCompareObjects() {
        // This creates OpenChords formatted songs, sets, tags from local files
        // Get a list of songs in this local folder
        Log.d(TAG,"Local folder name:"+getLocalFolderName());
        ArrayList<Song> localOpenSongSongs = mainActivityInterface.getSQLiteHelper().openChordsSyncGetSongsFromFolder(getLocalFolderName());
        // For each found song, create an OpenChordsSong object and add it to the array
        for (Song localOpenSongSong : localOpenSongSongs) {
            Log.d(TAG,"localOpenSongSong:"+localOpenSongSong.getFilename() + " ("+localOpenSongSong.getUuid()+")");
            // Only allow xml songs (no PDF/images)
            if (!mainActivityInterface.getStorageAccess().isIMGorPDF(localOpenSongSong)) {
                updateProgress(c.getString(R.string.sync_checking_local_item) + "\n" + localOpenSongSong.getTitle());
                localSongs.add(convertOpenSongToOpenChords(localOpenSongSong));
                localSongsCompareObjects.add(createOpenChordsCompareObject(localOpenSongSong.getUuid(),
                        localOpenSongSong.getFilename(), localOpenSongSong.getLastModified(), "song"));
            }
        }
        removePointlessStuffFromSongs(localSongs);

        // Go through our sets and look for sets with a category matching OpenChords
        for (String setName : mainActivityInterface.getStorageAccess().listFilesInFolder("Sets", "")) {
            if (setName.startsWith(getOpenSongSetCategoryStart())) {
                OpenChordsSetList openChordsSetList = convertOpenSongSetToOpenChordsSetList(setName);
                localSetLists.add(openChordsSetList);
                localSetListsCompareObjects.add(createOpenChordsCompareObject(openChordsSetList.getId(),openChordsSetList.getTitle(),openChordsSetList.getLastUpdated(),"set"));
            }
        }
        removePointlessStuffFromSetLists(localSetLists);

    }
    private void createServerCompareObjects() {
        // This goes through the server objects and converts them to compareObjects
        for (OpenChordsSong serverObject : serverSongs) {
            serverSongsCompareObjects.add(createOpenChordsCompareObject(serverObject.getId(),serverObject.getTitle(),serverObject.getLastUpdated(),"song"));
        }
        for (OpenChordsSetList serverObject : serverSetLists) {
            serverSetListsCompareObjects.add(createOpenChordsCompareObject(serverObject.getId(),serverObject.getTitle(),serverObject.getLastUpdated(),"set"));
        }
    }
    private OpenChordsCompareObject createOpenChordsCompareObject(String uuid, String title,
                                                                  String lastModified, String type) {
        OpenChordsCompareObject openChordsCompareObject = new OpenChordsCompareObject();
        openChordsCompareObject.setUuid(uuid);
        openChordsCompareObject.setTitle(jsonNullIfEmpty(title));
        openChordsCompareObject.setLastModified(lastModified);
        openChordsCompareObject.setType(type);

        return openChordsCompareObject;
    }

    // The comparison information between the server and local
    private void findSongsNotOnLocal() {
        for (OpenChordsCompareObject serverObject : serverSongsCompareObjects) {
            boolean found = false;
            for (OpenChordsCompareObject localObject : localSongsCompareObjects) {
                // We can have matching uuid or filename
                Log.d(TAG,"checking songs not on local   local:"+localObject.getTitle()+"  server:"+serverObject.getTitle());

                if (localObject.getUuid()!=null && localObject.getUuid().equalsIgnoreCase(serverObject.getUuid())) {
                    found = true;
                    Log.d(TAG,"local song on server");
                    break;
                }
                if (localObject.getTitle()!=null && localObject.getTitle().equals(serverObject.getTitle())) {
                    localSongNeedsServerUUID.add(serverObject);
                    Log.d(TAG,"local song on server but needs updated uuid");
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Add this server object
                songsNotOnLocal.add(serverObject);
            }
        }
    }
    private void findSongsNotOnServer() {
        for (OpenChordsCompareObject localObject : localSongsCompareObjects) {
            boolean found = false;
            for (OpenChordsCompareObject serverObject : serverSongsCompareObjects) {
                if (serverObject.getUuid() != null && serverObject.getUuid().equalsIgnoreCase(localObject.getUuid())) {
                    found = true;
                    break;
                }
                if (localObject.getTitle() != null && localObject.getTitle().equals(serverObject.getTitle())) {
                    // Just ignore for now.  We will update the local uuid
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Add this local object
                songsNotOnServer.add(localObject);
            }
        }
    }
    private void findSetsNotOnLocal() {
        for (OpenChordsCompareObject serverObject : serverSetListsCompareObjects) {
            boolean found = false;
            for (OpenChordsCompareObject localObject : localSetListsCompareObjects) {
                if (localObject.getUuid()!=null && localObject.getUuid().equalsIgnoreCase(serverObject.getUuid())) {
                    found = true;
                    break;
                }
                if (localObject.getTitle()!=null && localObject.getTitle().equals(serverObject.getTitle())) {
                    localSetListNeedsServerUUID.add(serverObject);
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Add this server object
                setListsNotOnLocal.add(serverObject);
            }
        }
    }
    private void findSetsNotOnServer() {
        for (OpenChordsCompareObject localObject : localSetListsCompareObjects) {
            boolean found = false;
            for (OpenChordsCompareObject serverObject : serverSetListsCompareObjects) {
                if ((localObject.getUuid()!=null && serverObject.getUuid()!=null && serverObject.getUuid().equalsIgnoreCase(localObject.getUuid())) ||
                        (localObject.getTitle()!=null && localObject.getTitle().equals(serverObject.getTitle()))) {
                    found = true;
                    break;
                }
            }
            if (!found && localObject.getTitle()!=null) {
                // Add this local object
                setListsNotOnServer.add(localObject);
            }
        }
    }
    private void findSongsNeedingUpdated() {
        // We have already logged the missing files, so now we deal with matches/updates
        String lastDownloadSongChanges = getLastModified("lastDownloadSongChanges");
        long lastDownloadSongChangesMillis = 0;
        if (lastDownloadSongChanges!=null && !lastDownloadSongChanges.equals(c.getString(R.string.is_not_set))) {
            lastDownloadSongChangesMillis = Instant.parse(lastDownloadSongChanges).toEpochMilli();
        }
        for (OpenChordsCompareObject serverObject : serverSongsCompareObjects) {
            for (OpenChordsCompareObject localObject : localSongsCompareObjects) {
                if ((localObject.getUuid()!=null && localObject.getUuid().equalsIgnoreCase(serverObject.getUuid())) ||
                        (localObject.getTitle()!=null && localObject.getTitle().equals(serverObject.getTitle()))) {
                    // This is a match, now decide if it needs updated or not
                    boolean serverObjectHasLastModified = true;
                    if (serverObject.getLastModified()==null || serverObject.getLastModified().equals(c.getString(R.string.is_not_set))) {
                        serverObject.setLastModified(mainActivityInterface.getTimeTools().getNowIsoTime());
                        serverObjectHasLastModified = false;
                    }
                    long serverObjectLastModified = Instant.parse(serverObject.getLastModified()).toEpochMilli();
                    boolean localObjectHasLastModified = true;
                    if (localObject.getLastModified()==null || localObject.getLastModified().isEmpty() ||
                            localObject.getLastModified().equals(c.getString(R.string.is_not_set))) {
                        localObject.setLastModified(mainActivityInterface.getTimeTools().getNowIsoTime());
                        localObjectHasLastModified = false;
                    }
                    long localObjectLastModified = Instant.parse(localObject.getLastModified()).toEpochMilli();

                    if (!serverObjectHasLastModified && localObjectHasLastModified) {
                        // The server version doesn't have a last modified date, but the local does, we need to update the server
                        songsOnServerOlder.add(localObject);

                    } else if (serverObjectHasLastModified && !localObjectHasLastModified) {
                        // The local version doesn't have a last modified date, but the server does, we need to update the local
                        songsOnLocalOlder.add(serverObject);

                    } else {
                        if (localObjectLastModified > serverObjectLastModified) {
                            // The server object needs updated
                            songsOnServerOlder.add(localObject);

                        /*
                        Was trying to use this, but doesn't work as I don't force a pull
                        } else if (localObjectLastModified < serverObjectLastModified &&
                                (localObjectLastModified < lastDownloadSongChangesMillis ||
                                        lastDownloadSongChangesMillis==0)) {
                        */
                        } else if (localObjectLastModified < serverObjectLastModified) {
                            // The local object needs updated as it is older than the server
                            // And also older the last download time (or it has never been downloaded)
                            songsOnLocalOlder.add(serverObject);
                        }
                    }
                    // Skip to the next song
                    break;
                }
            }
        }
    }
    private void findSetsNeedingUpdated() {
        // We have already logged the missing files, so now we deal with matches/updates
        String lastDownloadSetChanges = getLastModified("lastDownloadSetChanges");
        long lastDownloadSetChangesMillis = 0;
        if (lastDownloadSetChanges!=null && !lastDownloadSetChanges.equals(c.getString(R.string.is_not_set))) {
            lastDownloadSetChangesMillis = Instant.parse(lastDownloadSetChanges).toEpochMilli();
        }
        for (OpenChordsCompareObject serverObject : serverSetListsCompareObjects) {
            for (OpenChordsCompareObject localObject : localSetListsCompareObjects) {
                if ((localObject.getUuid()!=null && localObject.getUuid().equalsIgnoreCase(serverObject.getUuid())) ||
                        (localObject.getTitle()!=null && localObject.getTitle().equals(serverObject.getTitle()))) {
                    // This is a match, now decide if it needs updated or not
                    boolean serverObjectHasLastModified = true;
                    long serverObjectLastModified = 0;
                    if (serverObject.getLastModified()==null || serverObject.getLastModified().isEmpty()) {
                        serverObject.setLastModified(mainActivityInterface.getTimeTools().getNowIsoTime());
                        serverObjectHasLastModified = false;
                        serverObjectLastModified = Instant.parse(serverObject.getLastModified()).toEpochMilli();
                    }
                    boolean localObjectHasLastModified = true;
                    if (localObject.getLastModified()==null || localObject.getLastModified().isEmpty()) {
                        localObject.setLastModified(mainActivityInterface.getTimeTools().getNowIsoTime());
                        localObjectHasLastModified = false;
                    }
                    long localObjectLastModified = Instant.parse(localObject.getLastModified()).toEpochMilli();
                    if (serverObjectLastModified==0) {
                        serverObjectLastModified = localObjectLastModified;
                    }
                    if (!serverObjectHasLastModified && localObjectHasLastModified) {
                        // The server version doesn't have a last modified date, but the local does, we need to update the server
                        setListsOnServerOlder.add(localObject);

                    } else if (serverObjectHasLastModified && !localObjectHasLastModified) {
                        // The local version doesn't have a last modified date, but the server does, we need to update the local
                        setListsOnLocalOlder.add(serverObject);

                    } else {
                        if (localObjectLastModified > serverObjectLastModified) {
                            // The server object needs updated
                            setListsOnServerOlder.add(localObject);

                        /*
                        Was trying to use this, but doesn't work as I don't force a pull
                        } else if (localObjectLastModified < serverObjectLastModified &&
                                (localObjectLastModified < lastDownloadSetChangesMillis ||
                                        lastDownloadSetChangesMillis==0)) {
                        */
                        } else if (localObjectLastModified < serverObjectLastModified) {
                            // The local object needs updated
                            setListsOnLocalOlder.add(serverObject);
                        }
                    }
                    // Skip to the next song
                    break;
                }
            }
        }
    }

    public int getSongsNotOnLocalCount() {
        songsNotOnLocalCount = songsNotOnLocal.size();
        return songsNotOnLocalCount;
    }
    public int getSongsNotOnServerCount() {
        songsNotOnServerCount = songsNotOnServer.size();
        return songsNotOnServerCount;
    }
    public int getSongsOnLocalOlderCount() {
        songsOnLocalOlderCount = songsOnLocalOlder.size();
        return songsOnLocalOlderCount;
    }
    public int getSongsOnServerOlderCount() {
        songsOnServerOlderCount = songsOnServerOlder.size();
        return songsOnServerOlderCount;
    }
    public int getSetListsNotOnLocalCount() {
        setListsNotOnLocalCount = setListsNotOnLocal.size();
        return setListsNotOnLocalCount;
    }
    public int getSetListsNotOnServerCount() {
        setListsNotOnServerCount = setListsNotOnServer.size();
        return setListsNotOnServerCount;
    }
    public int getSetListsOnLocalOlderCount() {
        setListsOnLocalOlderCount = setListsOnLocalOlder.size();
        return setListsOnLocalOlderCount;
    }
    public int getSetListsOnServerOlderCount() {
        setListsOnServerOlderCount = setListsOnServerOlder.size();
        return setListsOnServerOlderCount;
    }
    public int getUploadCount() {
        return getSongsNotOnServerCount() + getSongsOnServerOlderCount() +
                getSetListsNotOnServerCount() + getSetListsOnServerOlderCount();
    }
    public int getDownloadCount() {
        return getSongsNotOnLocalCount() + getSongsOnLocalOlderCount() +
                getSetListsNotOnLocalCount() + getSetListsOnLocalOlderCount();
    }
    public String getSongsNotOnLocalString() {
        return getStringFromCompareObjects(songsNotOnLocal);
    }
    public String getSongsNotOnServerString() {
        return getStringFromCompareObjects(songsNotOnServer);
    }
    public String getSongsOnLocalOlderString() {
        return getStringFromCompareObjects(songsOnLocalOlder);
    }
    public String getSongsOnServerOlderString() {
        return getStringFromCompareObjects(songsOnServerOlder);
    }
    public String getSetListsNotOnLocalString() {
        return getStringFromCompareObjects(setListsNotOnLocal);
    }
    public String getSetListsNotOnServerString() {
        return getStringFromCompareObjects(setListsNotOnServer);
    }
    public String getSetListsOnLocalOlderString() {
        return getStringFromCompareObjects(setListsOnLocalOlder);
    }
    public String getSetListsOnServerOlderString() {
        return getStringFromCompareObjects(setListsOnServerOlder);
    }
    public String getStringFromCompareObjects(ArrayList<OpenChordsCompareObject> compareObjects) {
        StringBuilder stringBuilder = new StringBuilder();
        for (OpenChordsCompareObject compareObject : compareObjects) {
            if (compareObject.getTitle()!=null) {
                stringBuilder.append(compareObject.getTitle().trim()).append(", ");
            }
        }
        String string = stringBuilder.toString();
        if (string.endsWith(", ")) {
            string = string.substring(0, string.lastIndexOf(", "));
        }
        return string;
    }


    // The callbacks from the server
    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Make sure we create a conflictObject for the folder if it doesn't exist
            checkForConflictObject();

            // Update the query time
            updateConflictItem("lastQuery");
            updateConflictFile();

            // Reset the list of objects found in the local and server and any differences
            clearSyncObjects();

            if (response.isSuccessful()) {
                isServerResponse = true;
                serverFolder = (OpenChordsFolderObject) response.body();

                if (serverFolder != null) {
                    // Lets get the server objects we have found!
                    updateProgress(c.getString(R.string.sync_reading_remote_folder)+"\n");

                    if (serverFolder.getTitle()!=null) {
                        openChordsFolderName = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(serverFolder.getTitle());
                        serverTags = serverFolder.getTags();
                        serverSongs = serverFolder.getSongs();
                        if (serverSongs!=null) {
                            removePointlessStuffFromSongs(serverSongs);
                        }
                        serverSetLists = serverFolder.getSetLists();
                        if (serverSetLists!=null) {
                            removePointlessStuffFromSetLists(serverSetLists);
                        }


                        // Now create the server compare objects
                        createServerCompareObjects();
                    }
                }

            } else {
                mainActivityInterface.getShowToast().doIt(c.getString(R.string.sync_server_no_matching_folder));
                serverFolder = null;
                serverSongs.clear();
                serverTags.clear();
                serverSetLists.clear();
                isServerResponse = false;
                if (openChordsFragment != null) {
                    openChordsFragment.openChordsFolderNotFound();
                }
            }

            // Now compare the local and server objects
            updateProgress(c.getString(R.string.sync_comparing_local_and_remote)+"\n");
            //if (haveFolder) {
                // We have a matching local folder (and uuid now if not before)

                // Now check our local folder
                updateProgress(c.getString(R.string.sync_reading_local_folder) + "\n");

                // Now create the local compare objects
                createLocalCompareObjects();

            // We can find out what we don't have on the server that is on the local
            findSongsNotOnServer();
            findSetsNotOnServer();
            // Now we check what we don't have on the local folder that is on the server
            findSongsNotOnLocal();
            findSetsNotOnLocal();
            // Check for songs or sets that need updated
            findSongsNeedingUpdated();
            findSetsNeedingUpdated();

            // Send the info back to the openChordsFragment
            mainActivityInterface.setWhattodo("");

            if (openChordsFragment != null) {
                //if (haveFolder && serverFolder!=null &&
                if (serverFolder!=null &&
                        getDownloadCount()==0 && getUploadCount()==0) {
                    // We are already fully synchronised
                    openChordsFragment.openChordsFolderFullySynced();
                }
                if (serverFolder!=null) {
                    openChordsFolderName = serverFolder.getTitle();
                    openChordsFragment.updateFolderTitle(openChordsFolderName);
                }
                isServerResponse = false;
                openChordsFragment.logChanges();
            }
        });
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull Throwable throwable) {
        mainActivityInterface.getShowToast().doIt(c.getString(R.string.sync_server_noresponse_error));
        if (openChordsFragment != null) {
            openChordsFragment.openChordsFolderNotFound();
        }
    }
    public boolean getIsServerResponse() {
        return isServerResponse;
    }
    public void setIsServerResponse(boolean isServerResponse) {
        this.isServerResponse = isServerResponse;
    }
    private void updateProgress(String message) {
        if (openChordsFragment!=null) {
            openChordsFragment.updateProgress(message);
        }
    }


    // Convert OpenChords objects into OpenSong objects
    public Song convertOpenChordsToOpenSong(String filename, String title, String lastModified, OpenChordsSong openChordsSong) {
        Song song = new Song();
        song.setFolder(getLocalFolderName());
        song.setFilename(filename);
        song.setTitle(title);
        song.setLastModified(lastModified);
        song.setUuid(openChordsSong.getId());
        song.setAuthor(openChordsSong.getArtist());
        song.setLyrics(mainActivityInterface.getConvertJustChords().getOpenSongLyrics(openChordsSong.getRawData()));
        song.setAutoscrolllength(getEmptyForZero(mainActivityInterface.getTimeTools().getTotalSecsFromColonTimes(openChordsSong.getDuration())));
        song.setTimesig(openChordsSong.getTimeSignature());
        String key = openChordsSong.getKey();
        Boolean keyIsMinor = openChordsSong.isKeyIsMinor();
        if (key!=null && keyIsMinor!=null) {
            key = key + "m";
        }
        song.setKey(key);
        if (openChordsSong.getTempo()!=null) {
            song.setTempo(String.valueOf(openChordsSong.getTempo()));
        }
        if (openChordsSong.getCapo()!=null) {
            song.setCapo(String.valueOf(openChordsSong.getCapo()));
        }
        song.setNotes(openChordsSong.getNotes());
        song.setCopyright(openChordsSong.getCopyright());
        song.setCcli(openChordsSong.getCcli());
        // Now get the tags
        song.setTheme(getTagsFromOpenChordsForOpenSong(openChordsSong));
        return song;
    }
    private String getTagsFromOpenChordsForOpenSong(OpenChordsSong openChordsSong) {
        StringBuilder tagStringBuilder = new StringBuilder();
        String[] tags = openChordsSong.getTags();
        if (tags!=null) {
            for (String tag:tags) {
                // Try to find this tag in the folder tags array
                for (OpenChordsTag openChordsTag : serverTags) {
                    if (tag.equalsIgnoreCase(openChordsTag.getId())) {
                        tagStringBuilder.append(openChordsTag.getTitle()).append("\n");
                        break;
                    }
                }
            }
        }
        return tagStringBuilder.toString().trim().replace("\n",";");
    }
    public String convertOpenChordsSetList(OpenChordsSetList serverSetList) {
        CurrentSet localSet = new CurrentSet(c);
        localSet.setUuid(serverSetList.getId());
        localSet.setSetCurrentLastName(convertOpenSongSetNameToOpenChordsSetName(serverSetList.getTitle()));
        localSet.setNotes(jsonNullIfEmpty(serverSetList.getNotes()));
        localSet.setLastModified(serverSetList.getLastUpdated());

        if (serverSetList.getItems()!=null) {
            for (OpenChordsSetListItem serverSetListItem : serverSetList.getItems()) {
                String itemId = serverSetListItem.getId();
                String itemTitle = serverSetListItem.getTitle();
                OpenChordsSetListSongItem openChordsSetListSongItem = serverSetListItem.getSongItem();
                String itemType = serverSetListItem.getType();
                String itemCustomData = serverSetListItem.getCustomData();
                String itemLastUpdated = serverSetListItem.getLastUpdated();
                String itemNotes = serverSetListItem.getNotes();
                // Get the filename from the id
                boolean found = false;
                String filename = null;
                String key = null;
                String title = null;

                if (itemType != null && itemType.equals("song")) {
                    for (OpenChordsSong openChordsSong : serverSongs) {
                        if (openChordsSong.getId() != null && openChordsSong.getId().equalsIgnoreCase(serverSetListItem.getId()) &&
                                openChordsSong.getTitle() != null) {
                            filename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(openChordsSong.getTitle());
                            title = serverSetListItem.getTitle();
                            key = openChordsSong.getKey();
                            Boolean keyisMinor = openChordsSong.isKeyIsMinor();
                            if (keyisMinor != null && key != null) {
                                key = key + (keyisMinor ? "m" : "");
                            }
                            found = true;
                        }
                        if (found) {
                            break;
                        }
                    }
                    if (!found) {
                        for (OpenChordsSong openChordsSong : localSongs) {
                            if (openChordsSong.getTitle()!=null &&
                                    openChordsSong.getId()!=null &&
                                    openChordsSong.getId().equalsIgnoreCase(serverSetListItem.getId())) {
                                filename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(openChordsSong.getTitle());
                                key = openChordsSong.getKey();
                                Boolean isKeyMinor = openChordsSong.isKeyIsMinor();
                                if (key!=null && isKeyMinor!=null) {
                                    key = key + (isKeyMinor ? "m" : "");
                                }
                                if (key==null) {
                                    key = "";
                                }
                                found = true;
                            }
                            if (found) {
                                break;
                            }
                        }
                    }
                    if (filename != null && title != null) {
                        localSet.addItemToSet(getLocalFolderName(), filename, title, key, false);
                    }

                } else if (itemType!=null && itemType.equals("divider")) {
                    localSet.addItemToSet("/", mainActivityInterface.getSetActions().getDividerIdentifier(),
                            mainActivityInterface.getSetActions().getDividerIdentifier(), null, false);

                } else if (itemType!=null && itemType.equals("slide") && itemTitle!=null) {
                    itemTitle = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(itemTitle);
                    Song tempSong = mainActivityInterface.getProcessSong().initialiseSong("**Slides", itemTitle);
                    tempSong.setLyrics(itemNotes);
                    tempSong.setUuid(itemId);
                    tempSong.setTitle(itemTitle);

                    // Save this temp song so we can recover the contents when we build the set file
                    mainActivityInterface.getStorageAccess().saveThisSongFile(tempSong);
                    localSet.addItemToSet(tempSong);
                }
            }
        }
        // Update the last modified to match the value we want (default setXML is now time)
        mainActivityInterface.getSetActions().setUseThisLastModifiedDate(serverSetList.getLastUpdated());
        String xml = mainActivityInterface.getSetActions().createSetXML(localSet);
        mainActivityInterface.getSetActions().setUseThisLastModifiedDate(null);
        return xml;
    }
    private String convertOpenChordsSetNameToOpenSongSetName(String openChordsSetName) {
        if (openChordsSetName!=null && openChordsSetName.startsWith(getOpenSongSetCategoryStart())) {
            return openChordsSetName;
        } else if (openChordsSetName!=null){
            return getOpenSongSetCategoryStart() +
                    mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(openChordsSetName);
        } else {
            return null;
        }
    }
    private String getEmptyForZero(int integer) {
        return integer==0 ? "" : String.valueOf(integer);
    }


    // Convert OpenSong objects into OpenChords objects
    public OpenChordsSong convertOpenSongToOpenChords(Song openSongSong) {
        OpenChordsSong openChordsSong = new OpenChordsSong();
        openChordsSong.setId(openSongSong.getUuid());
        openChordsSong.setTitle(jsonNullIfEmpty(openSongSong.getFilename()));
        openChordsSong.setRawData(jsonNullIfEmpty(mainActivityInterface.getConvertJustChords().getJustChordsLyrics(openSongSong)));
        openChordsSong.setArtist(jsonNullIfEmpty(openSongSong.getAuthor()));
        openChordsSong.setDuration(jsonNullIfEmpty(openSongSong.getAutoscrolllength()));
        String tempo = openSongSong.getTempo();
        if (tempo!=null) {
            tempo = tempo.replaceAll("\\D","").trim();
            if (!tempo.isEmpty()) {
                openChordsSong.setTempo(Integer.parseInt(tempo));
            }
        }
        openChordsSong.setTimeSignature(jsonNullIfEmpty(openSongSong.getTimesig()));
        String key = openSongSong.getKey();
        if (key!=null && !key.isEmpty()) {
            openChordsSong.setKey(key.replace("m",""));
            openChordsSong.setKeyIsMinor(key.endsWith("m"));
        }
        String capo = openSongSong.getCapo();
        if (capo!=null) {
            capo = capo.replaceAll("\\D","").trim();
            if (!capo.isEmpty()) {
                openChordsSong.setCapo(Integer.parseInt(capo));
            }
        }
        //openChordsSong.setTranspose(key);
        openChordsSong.setNotes(jsonNullIfEmpty(openSongSong.getNotes()));
        openChordsSong.setCopyright(jsonNullIfEmpty(openSongSong.getCopyright()));
        openChordsSong.setCcli(jsonNullIfEmpty(openSongSong.getCcli()));
        openChordsSong.setLastUpdated(jsonNullIfEmpty(openSongSong.getLastModified()));
        // To add tags, we need to cycle through our tags
        // Look for the tag id already saved in the server
        // If they are found, get their uuid, if not, create a new one
        if (openSongSong.getTheme()!=null) {
            StringBuilder newTags = new StringBuilder();
            String[] localTags = openSongSong.getTheme().split(";");
            for (String localTag : localTags) {
                boolean found = false;
                for (OpenChordsTag serverTag : serverTags) {
                    if (serverTag.getTitle()!=null && serverTag.getTitle().equals(localTag)) {
                        newTags.append(serverTag.getId());
                        found = true;
                    }
                    if (found) {
                        break;
                    }
                }
                if (!found && !localTag.trim().isEmpty()) {
                    String newUUID = String.valueOf(UUID.randomUUID());
                    newTags.append(newUUID).append("\n");
                    OpenChordsTag newTag = new OpenChordsTag();
                    newTag.setId(newUUID);
                    newTag.setTitle(localTag.trim());
                    newTag.setColor(mainActivityInterface.getMyThemeColors().getHexFromIntNoAlpha(ContextCompat.getColor(c,R.color.colorPrimary)));
                    newTagsForUpload.add(newTag);
                }
            }
            if (!newTags.toString().trim().isEmpty()) {
                String[] tags = newTags.toString().split("\n");
                openChordsSong.setTags(tags);
            }
        }
        return openChordsSong;
    }
    public OpenChordsSetList convertOpenSongSetToOpenChordsSetList(String filename) {
        // This is a newer method that parsers the set into a setObject first
        SetObject setObject = mainActivityInterface.getSetActions().createSetObjectFromFilename(filename);

        // Hopefully the setObject isn't empty and we can proceed
        OpenChordsSetList openChordsSetList = new OpenChordsSetList();
        if (setObject!=null) {
            // Put the @Nullable values into the openChordsSetList
            if (setObject.getUuid()==null) {
                openChordsSetList.setId(String.valueOf(UUID.randomUUID()));
            } else {
                openChordsSetList.setId(setObject.getUuid());
            }
            openChordsSetList.setLastUpdated(setObject.getLastModified());
            openChordsSetList.setNotes(jsonNullIfEmpty(setObject.getNotes()));
            openChordsSetList.setTitle(jsonNullIfEmpty(setObject.getSetName()));
            // Now we need to go through the set items and add them
            ArrayList<OpenChordsSetListItem> openChordsSetListItems = null;
            if (setObject.getSlideGroups()!=null) {
                for (SetSlideGroupObject slideGroupObject : setObject.getSlideGroups()) {
                    OpenChordsSetListItem openChordsSetListItem;
                    if (slideGroupObject.getType()!=null) {
                        switch (slideGroupObject.getType()) {
                            case "song":
                                openChordsSetListItem = mainActivityInterface.getSetActions().getOpenChordsSetListItemForSong(slideGroupObject);
                                if (openChordsSetListItems == null) {
                                    openChordsSetListItems = new ArrayList<>();
                                }
                                openChordsSetListItems.add(openChordsSetListItem);
                                break;

                            case "divider":
                                openChordsSetListItem = mainActivityInterface.getSetActions().getOpenChordsSetListItemForDivider(slideGroupObject);
                                if (openChordsSetListItems == null) {
                                    openChordsSetListItems = new ArrayList<>();
                                }
                                openChordsSetListItems.add(openChordsSetListItem);
                                break;

                            case "custom":
                            case "image":
                            case "scripture":
                            case "variation":
                                openChordsSetListItem = mainActivityInterface.getSetActions().getOpenChordsSetListItemForCustom(slideGroupObject);
                                if (openChordsSetListItems == null) {
                                    openChordsSetListItems = new ArrayList<>();
                                }
                                openChordsSetListItems.add(openChordsSetListItem);
                                break;
                        }
                    }
                }
            }

            // Add the slide groups (even if they are null)
            openChordsSetList.setItems(openChordsSetListItems);
        }

        String json = gson.toJson(openChordsSetList);
        mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings","","testingSetObject.json",json);

        return openChordsSetList;
    }

    private String convertOpenSongSetNameToOpenChordsSetName(String openSongSetName) {
        return mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(openSongSetName).
                replace(getOpenSongSetCategoryStart(),"");
    }
    private String getOpenSongSetCategoryStart() {
        return "OpenChords" + mainActivityInterface.getSetActions().getSetCategorySeparator();
    }


    // The download logic
    public void prepareDownload(boolean newSongs, boolean updateSongs, boolean newSets, boolean updateSets) {
        checkForConflictObject();
        conflictItemRecords = new ArrayList<>();

        boolean songMenuUpdate = false;

        // If we don't yet have a matching folder, we need to create it!
        checkCreateLocalFolder();

        // If we need to update the uuid of local songs, do it
        updateLocalSongsUuid();

        // If we need to update the uuid of local sets, do it
        updateLocalSetsUuid();

        // Now we can continue!
        if (newSongs) {
            downloadNewServerSongs();
            songMenuUpdate = true;
        } else if (updateSongs) {
            updateLocalSongs();
            songMenuUpdate = true;
        } else if (newSets) {
            downloadNewServerSetLists();
        } else if (updateSets) {
            updateLocalSetLists();
        }

        updateConflictFile();

        if (songMenuUpdate) {
            updateTheSongMenu();
        }
    }
    private void downloadNewServerSongs() {
        // Go through the serverSongs and download the ones we have a record of in the compare objects
        for (OpenChordsSong serverSong : serverSongs) {
            for (OpenChordsCompareObject compareObject : songsNotOnLocal) {
                if (compareObject.getUuid()!=null && compareObject.getTitle()!=null &&
                        compareObject.getUuid().equalsIgnoreCase(serverSong.getId())) {
                    // This is a song we want
                    String title = compareObject.getTitle();
                    String filename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(title);
                    updateProgress(c.getString(R.string.sync_creating_new_item) + " (" + c.getString(R.string.song) + ")\n" + title);
                    Uri songUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",
                            getLocalFolderName(), filename);
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, songUri, null,
                            "Songs", getLocalFolderName(), filename);
                    Song newOpenSongSong = convertOpenChordsToOpenSong(filename,title,compareObject.getLastModified(),serverSong);

                    // Save the song
                    mainActivityInterface.getSQLiteHelper().createSong(getLocalFolderName(), filename);
                    mainActivityInterface.getSaveSong().setResetLastModified(false);
                    mainActivityInterface.getSaveSong().updateSong(newOpenSongSong, false);
                    mainActivityInterface.getSaveSong().setResetLastModified(true);

                    // Remove this item from the compareObjects since we have dealt with it
                    songsNotOnLocal.remove(compareObject);

                    String nowTime = mainActivityInterface.getTimeTools().getNowIsoTime();
                    for (OpenChordsSetList set : localSetLists) {
                        OpenChordsConflictItemObject openChordsConflictItemObject = new OpenChordsConflictItemObject();
                        openChordsConflictItemObject.setAction(c.getString(R.string.sync_song_downloaded));
                        openChordsConflictItemObject.setItem(set.getTitle());
                        openChordsConflictItemObject.setDate(nowTime);
                        conflictItemRecords.add(openChordsConflictItemObject);
                    }

                    // Skip to the next server song
                    break;
                }
            }
        }
        updateConflictItem("lastDownloadNewSongs");
    }
    private void updateLocalSongs() {
        // Go through the serverSongs and download the ones we have a record of in the compare objects
        // We actually only replace the necessary stuff in the existing songs though!
        String nowTime = mainActivityInterface.getTimeTools().getNowIsoTime();
        for (OpenChordsSong serverSong : serverSongs) {
            for (OpenChordsCompareObject compareObject : songsOnLocalOlder) {
                if (compareObject.getUuid()!=null && serverSong.getTitle()!=null && compareObject.getTitle()!=null &&
                        compareObject.getUuid().equalsIgnoreCase(serverSong.getId())) {
                    // This is a song we want
                    String title = serverSong.getTitle();
                    String filename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(title);
                    updateProgress(c.getString(R.string.sync_updating_item) + " (" + c.getString(R.string.song) + ")\n" + title);

                    // Get the existing song so we only update the info held by OpenChords
                    Song existingSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(getLocalFolderName(),filename);
                    Uri songUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",
                            getLocalFolderName(), filename);
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, songUri, null,
                            "Songs", getLocalFolderName(), filename);
                    Song newOpenSongSong = convertOpenChordsToOpenSong(filename,title,serverSong.getLastUpdated(),serverSong);

                    // If we have changed the title/filename, we need to update the database
                    String oldtitle = compareObject.getTitle();
                    String oldfilename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(oldtitle);
                    if (!oldfilename.equals(filename)) {
                        mainActivityInterface.getSQLiteHelper().deleteSong(getLocalFolderName(), mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(compareObject.getTitle()));
                        mainActivityInterface.getSQLiteHelper().createSong(getLocalFolderName(), filename);
                    }

                    // Update the existing song with the info received (not all OpenSong stuff is in OpenChords!)
                    updateExistingOpenSongWithOpenChords(existingSong, newOpenSongSong);

                    // Save the song
                    mainActivityInterface.getSaveSong().setResetLastModified(false);
                    mainActivityInterface.getSaveSong().updateSong(existingSong, false);
                    mainActivityInterface.getSaveSong().setResetLastModified(true);

                    // Remove this item from the compareObjects since we have dealt with it
                    songsOnLocalOlder.remove(compareObject);

                    OpenChordsConflictItemObject openChordsConflictItemObject = new OpenChordsConflictItemObject();
                    openChordsConflictItemObject.setAction(c.getString(R.string.sync_song_update_downloaded));
                    openChordsConflictItemObject.setItem(compareObject.getTitle());
                    openChordsConflictItemObject.setDate(nowTime);
                    conflictItemRecords.add(openChordsConflictItemObject);

                    // Skip to the next server song
                    break;
                }
            }
        }
        updateConflictItem("lastDownloadSongChanges");
    }
    private void downloadNewServerSetLists() {
        // Go through the serverSetLists and download the ones we have a record of in the compare objects
        String nowTime = mainActivityInterface.getTimeTools().getNowIsoTime();
        for (OpenChordsSetList serverSetList : serverSetLists) {
            for (OpenChordsCompareObject compareObject : setListsNotOnLocal) {
                if (compareObject.getUuid()!=null && compareObject.getTitle()!=null &&
                        compareObject.getUuid().equalsIgnoreCase(serverSetList.getId())) {
                    // This is a setList we want
                    String title = compareObject.getTitle();
                    String filename = convertOpenChordsSetNameToOpenSongSetName(title);
                    updateProgress(c.getString(R.string.sync_creating_new_item) + " (" + c.getString(R.string.set)+")\n" + title);

                    String setXML = convertOpenChordsSetList(serverSetList);

                    // Save the set
                    mainActivityInterface.getStorageAccess().doStringWriteToFile("Sets","",filename,setXML);

                    // Remove this item from the compareObjects since we have dealt with it
                    setListsNotOnLocal.remove(compareObject);

                    OpenChordsConflictItemObject openChordsConflictItemObject = new OpenChordsConflictItemObject();
                    openChordsConflictItemObject.setAction(c.getString(R.string.sync_set_downloaded));
                    openChordsConflictItemObject.setItem(compareObject.getTitle());
                    openChordsConflictItemObject.setDate(nowTime);
                    conflictItemRecords.add(openChordsConflictItemObject);

                    // Skip to the next server set
                    break;
                }
            }
        }
        updateConflictItem("lastDownloadNewSets");
    }
    private void updateLocalSetLists() {
        // Go through the serverSetLists and update the ones we have a record of in the compare objects
        String nowTime = mainActivityInterface.getTimeTools().getNowIsoTime();
        for (OpenChordsSetList serverSetList : serverSetLists) {
            for (OpenChordsCompareObject compareObject : setListsOnLocalOlder) {
                if (compareObject.getUuid()!=null && serverSetList.getTitle()!=null &&
                        compareObject.getUuid().equalsIgnoreCase(serverSetList.getId())) {
                    // This is a setList we want
                    String title = serverSetList.getTitle();
                    String filename = convertOpenChordsSetNameToOpenSongSetName(title);

                    updateProgress(c.getString(R.string.sync_updating_item) + " (" + c.getString(R.string.set)+")\n" + title);

                    // Delete the old setlist if the filename has changed
                    String oldtitle = compareObject.getTitle();
                    String oldfilename = convertOpenChordsSetNameToOpenSongSetName(oldtitle);
                    if (!filename.equals(oldfilename)) {
                        try {
                            mainActivityInterface.getStorageAccess().doDeleteFile("Sets", "", oldfilename);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    String setXML = convertOpenChordsSetList(serverSetList);

                    // Save the set
                    mainActivityInterface.getStorageAccess().doStringWriteToFile("Sets","",filename,setXML);

                    // Remove this item from the compareObjects since we have dealt with it
                    setListsOnLocalOlder.remove(compareObject);

                    OpenChordsConflictItemObject openChordsConflictItemObject = new OpenChordsConflictItemObject();
                    openChordsConflictItemObject.setAction(c.getString(R.string.sync_set_update_downloaded));
                    openChordsConflictItemObject.setItem(compareObject.getTitle());
                    openChordsConflictItemObject.setDate(nowTime);
                    conflictItemRecords.add(openChordsConflictItemObject);

                    // Skip to the next server set
                    break;
                }
            }
        }
        updateConflictItem("lastDownloadSetChanges");
    }
    private void updateExistingOpenSongWithOpenChords(Song existingSong, Song newOpenSongSong) {
        // existingSong is the one currently in local storage
        // newOpenSongSong is the one received from the server
        // OpenChords only holds some song information, so just update those bits

        existingSong.setFolder(newOpenSongSong.getFolder());
        existingSong.setFilename(newOpenSongSong.getFilename());
        existingSong.setUuid(newOpenSongSong.getUuid());
        existingSong.setTitle(newOpenSongSong.getTitle());
        existingSong.setLyrics(newOpenSongSong.getLyrics());
        existingSong.setAuthor(newOpenSongSong.getAuthor());
        existingSong.setAutoscrolllength(newOpenSongSong.getAutoscrolllength());
        existingSong.setTempo(newOpenSongSong.getTempo());
        existingSong.setTimesig(newOpenSongSong.getTimesig());
        existingSong.setKey(newOpenSongSong.getKey());
        existingSong.setCapo(newOpenSongSong.getCapo());
        existingSong.setNotes(newOpenSongSong.getNotes());
        existingSong.setCcli(newOpenSongSong.getCcli());
        existingSong.setLastModified(newOpenSongSong.getLastModified());
        existingSong.setTheme(newOpenSongSong.getTheme());
    }
    public void forcePull() {
        // We have requested a force pull.  This wipes the content of our local folder
        // It also removes any setlists with the OpenChords__ prefix
        // We then download all the server songs

        checkForConflictObject();
        conflictItemRecords = new ArrayList<>();

        // If we don't yet have a matching folder, we need to create it!
        checkCreateLocalFolder();

        // If we need to update the uuid of local songs, do it
        updateLocalSongsUuid();

        // If we need to update the uuid of local sets, do it
        updateLocalSetsUuid();

        // Clear the existing songs from the database
        updateProgress(c.getString(R.string.sync_deleting_local_items));
        ArrayList<Song> songsInFolder = mainActivityInterface.getSQLiteHelper().openChordsSyncGetSongsFromFolder(getLocalFolderName());

        String nowTime = mainActivityInterface.getTimeTools().getNowIsoTime();
        for (Song song : songsInFolder) {
            mainActivityInterface.getSQLiteHelper().deleteSong(getLocalFolderName(), song.getFilename());
        }
        songsInFolder.clear();

        // Now remove the song files in the local folder
        mainActivityInterface.getStorageAccess().wipeFolder("Songs",getLocalFolderName());

        // Delete OpenChords__ set files
        ArrayList<String> setFiles = mainActivityInterface.getStorageAccess().listFilesInFolder("Sets","");
        for (String setFile : setFiles) {
            if (setFile.startsWith(getOpenSongSetCategoryStart())) {
                mainActivityInterface.getStorageAccess().doDeleteFile("Sets","",setFile);
            }
        }

        // Now to download the stuff from the server
        // Go through the serverSongs and download them all
        for (OpenChordsSong serverSong : serverSongs) {
            if (serverSong.getTitle() != null) {
                String title = serverSong.getTitle();
                updateProgress(c.getString(R.string.sync_creating_new_item) + " (" + c.getString(R.string.song) + ")\n" + title);
                String filename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(title);
                Song newOpenSongSong = convertOpenChordsToOpenSong(filename, title, serverSong.getLastUpdated(), serverSong);
                // Save the song
                mainActivityInterface.getSQLiteHelper().createSong(getLocalFolderName(), filename);
                mainActivityInterface.getSaveSong().setResetLastModified(false);
                mainActivityInterface.getSaveSong().updateSong(newOpenSongSong, false);
                mainActivityInterface.getSaveSong().setResetLastModified(true);

                OpenChordsConflictItemObject openChordsConflictItemObject = new OpenChordsConflictItemObject();
                openChordsConflictItemObject.setAction(c.getString(R.string.sync_song_force_downloaded));
                openChordsConflictItemObject.setItem(title);
                openChordsConflictItemObject.setDate(nowTime);
                conflictItemRecords.add(openChordsConflictItemObject);

            }
        }

        // Go through the serverSongs and download them all
        for (OpenChordsSetList serverSetList : serverSetLists) {
            if (serverSetList.getTitle() != null) {
                String title = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(serverSetList.getTitle());
                String filename = convertOpenChordsSetNameToOpenSongSetName(title);
                updateProgress(c.getString(R.string.sync_creating_new_item) + " (" + c.getString(R.string.set_list) + ")\n" + title);

                // Get the xml for the setlist
                String xml = convertOpenChordsSetList(serverSetList);
                mainActivityInterface.getStorageAccess().doStringWriteToFile("Sets", "", filename, xml);
                OpenChordsConflictItemObject openChordsConflictItemObject = new OpenChordsConflictItemObject();
                openChordsConflictItemObject.setAction(c.getString(R.string.sync_set_force_downloaded));
                openChordsConflictItemObject.setItem(title);
                openChordsConflictItemObject.setDate(nowTime);
                conflictItemRecords.add(openChordsConflictItemObject);

            }
        }

        updateConflictItem("lastForcePull");
        updateConflictFile();
    }

    // The upload logic
    public void prepareUpload(boolean newSongs, boolean updateSongs, boolean newSetLists, boolean updateSetLists) {
        // Make sure we create a conflictObject for the folder if it doesn't exist
        checkForConflictObject();
        conflictItemRecords = new ArrayList<>();

        // Prepare the upload folder object
        OpenChordsFolderObject uploadFolderObject = new OpenChordsFolderObject();
        uploadFolderObject.setTitle(getLocalFolderName());
        uploadFolderObject.setOwnerId(openChordsFolderUuid);

        // If we need to update the uuid of local songs, do it
        updateLocalSongsUuid();

        // If we need to update the uuid of local sets, do it
        updateLocalSetsUuid();

        // Deal with the songs
        updateProgress(c.getString(R.string.sync_reading_local_folder)+"\n");
        if (newSongs) {
            prepareUploadServerSongsAndNewLocal();
        } else if (updateSongs) {
            prepareUploadServerSongsAndUpdates();
        } else {
            songsForUpload = serverSongs;
        }

        // Deal with the sets
        if (newSetLists) {
            prepareUploadServerSetsAndNewLocal();
        } else if (updateSetLists) {
            prepareUploadServerSetsAndUpdates();
        } else {
            setsForUpload = serverSetLists;
        }

        // Make sure setlists getting uploaded don't have the OpenChords__ prefix
        if (setsForUpload!=null) {
            for (OpenChordsSetList setForUpload : setsForUpload) {
                if (setForUpload.getTitle()!=null &&
                        setForUpload.getTitle().startsWith(getOpenSongSetCategoryStart())) {
                    setForUpload.setTitle(setForUpload.getTitle().replace(getOpenSongSetCategoryStart(),""));
                }
            }
        }

        // Deal with the tags
        prepareUploadServerTagsAndNewLocal();

        // Now set all item records to the same time
        String nowTime = mainActivityInterface.getTimeTools().getNowIsoTime();
        for (OpenChordsConflictItemObject conflictItemRecord : conflictItemRecords) {
            conflictItemRecord.setDate(nowTime);
        }

        uploadFolderObject.setSongs(songsForUpload);
        uploadFolderObject.setSetLists(setsForUpload);
        uploadFolderObject.setTags(tagsForUpload);

        String json = gson.toJson(uploadFolderObject);
        // Replace unneccessary items
        json = removeUnnecessaryBitsFromJson(json);
        mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings", "", "uploadFolderObject2.json", json);

        updateProgress(c.getString(R.string.sync_uploading_changes)+"\n");

        Call<OpenChordsFolderObject> call = retrofitInterface.postOpenChordsFolder(uploadFolderObject.getOwnerId(), uploadFolderObject);
        call.enqueue(new Callback<OpenChordsFolderObject>() {
            @Override
            public void onResponse(@NonNull Call<OpenChordsFolderObject> call, @NonNull Response<OpenChordsFolderObject> response) {
                // this method is called when we get response from our api.
                if (openChordsFragment!=null) {
                    openChordsFragment.changeButtonsEnable(false);
                    updateProgress(c.getString(R.string.wait)+"\n");
                    mainActivityInterface.getMainHandler().postDelayed(() -> {
                        if (openChordsFragment!=null) {
                            openChordsFragment.queryOpenChordsServer();
                        }
                    },1000);
                }
            }

            @Override
            public void onFailure(@NonNull Call<OpenChordsFolderObject> call, @NonNull Throwable t) {
                if (openChordsFragment!=null) {
                    openChordsFragment.changeButtonsEnable(false);
                    updateProgress(c.getString(R.string.wait)+"\n");
                    mainActivityInterface.getMainHandler().postDelayed(() -> {
                        if (openChordsFragment!=null) {
                            openChordsFragment.queryOpenChordsServer();
                        }
                    },1000);
                }
            }
        });
        updateConflictFile();
    }
    private void prepareUploadServerSongsAndNewLocal() {
        // Combine the current server songs with the new local songs for uploading
        if (serverSongs!=null) {
            songsForUpload = new ArrayList<>(serverSongs);
        } else {
            songsForUpload = new ArrayList<>();
        }
        for (OpenChordsCompareObject compareObject : songsNotOnServer) {
            updateProgress(c.getString(R.string.sync_preparing_item) + " ("+c.getString(R.string.song)+")\n" + compareObject.getTitle());
            Song song = mainActivityInterface.getSQLiteHelper().getOpenChordsSong(getLocalFolderName(),compareObject.getUuid());
            OpenChordsSong newSong = convertOpenSongToOpenChords(song);
            Log.d(TAG,"songForUpload:"+newSong.getTitle()+"  ("+newSong.getId()+")");
            songsForUpload.add(newSong);
            OpenChordsConflictItemObject openChordsConflictItemObject = new OpenChordsConflictItemObject();
            openChordsConflictItemObject.setAction(c.getString(R.string.sync_song_uploaded));
            openChordsConflictItemObject.setItem(newSong.getTitle());
            conflictItemRecords.add(openChordsConflictItemObject);
        }
        if (songsNotOnServerCount>0) {
            updateConflictItem("lastUploadNewSongs");
        }
    }
    private void prepareUploadServerSongsAndUpdates() {
        // Combine the current server songs with the newer local songs for uploading
        songsForUpload = new ArrayList<>();
        for (OpenChordsSong serverSong : serverSongs) {
            boolean found = false;
            for (OpenChordsCompareObject compareObject : songsOnServerOlder) {
                if (serverSong.getId()!=null &&
                        serverSong.getId().equalsIgnoreCase(compareObject.getUuid())) {
                    updateProgress(c.getString(R.string.sync_preparing_item) + " ("+c.getString(R.string.song)+")\n" + compareObject.getTitle());
                    Song song = mainActivityInterface.getSQLiteHelper().getOpenChordsSong(getLocalFolderName(),compareObject.getUuid());
                    OpenChordsSong newSong = convertOpenSongToOpenChords(song);
                    songsForUpload.add(newSong);
                    OpenChordsConflictItemObject openChordsConflictItemObject = new OpenChordsConflictItemObject();
                    openChordsConflictItemObject.setAction(c.getString(R.string.sync_song_update_uploaded));
                    openChordsConflictItemObject.setItem(newSong.getTitle());
                    conflictItemRecords.add(openChordsConflictItemObject);
                    found = true;
                }
                if (found) {
                    break;
                }
            }
            if (!found) {
                songsForUpload.add(serverSong);
            }
        }
        if (songsOnServerOlderCount>0) {
            updateConflictItem("lastUploadSongChanges");
        }
    }
    private void prepareUploadServerSetsAndNewLocal() {
        // Combine the current server sets with the new local sets for uploading
        if (serverSetLists!=null) {
            setsForUpload = new ArrayList<>(serverSetLists);
        } else {
            setsForUpload = new ArrayList<>();
        }
        for (OpenChordsCompareObject compareObject : setListsNotOnServer) {
            updateProgress(c.getString(R.string.sync_preparing_item) + " ("+c.getString(R.string.set_list)+")\n" + compareObject.getTitle());
            OpenChordsSetList openChordsSetList = convertOpenSongSetToOpenChordsSetList(compareObject.getTitle());
            setsForUpload.add(openChordsSetList);
            OpenChordsConflictItemObject openChordsConflictItemObject = new OpenChordsConflictItemObject();
            openChordsConflictItemObject.setAction(c.getString(R.string.sync_set_uploaded));
            openChordsConflictItemObject.setItem(openChordsSetList.getTitle());
            conflictItemRecords.add(openChordsConflictItemObject);
        }
        if (setListsNotOnServerCount>0) {
            updateConflictItem("lastUploadNewSets");
        }
    }
    private void prepareUploadServerSetsAndUpdates() {
        // Combine the current server sets with the new local sets for uploading
        setsForUpload = new ArrayList<>();
        for (OpenChordsSetList serverSetList : serverSetLists) {
            boolean found = false;
            for (OpenChordsCompareObject compareObject : setListsOnServerOlder) {
                if (serverSetList.getId()!=null && serverSetList.getId().equalsIgnoreCase(compareObject.getUuid())) {
                    updateProgress(c.getString(R.string.sync_preparing_item) + " ("+c.getString(R.string.set)+")\n" + compareObject.getTitle());
                    OpenChordsSetList openChordsSetList = convertOpenSongSetToOpenChordsSetList(getOpenSongSetCategoryStart()+compareObject.getTitle());
                    setsForUpload.add(openChordsSetList);
                    OpenChordsConflictItemObject openChordsConflictItemObject = new OpenChordsConflictItemObject();
                    openChordsConflictItemObject.setAction(c.getString(R.string.sync_set_update_uploaded));
                    openChordsConflictItemObject.setItem(openChordsSetList.getTitle());
                    conflictItemRecords.add(openChordsConflictItemObject);
                    found = true;
                }
                if (found) {
                    break;
                }
            }
            if (!found) {
                setsForUpload.add(serverSetList);
            }
            if (setListsOnServerOlderCount>0) {
                updateConflictItem("lastUploadSetChanges");
            }
        }
    }
    private void prepareUploadServerTagsAndNewLocal() {
        // Combine the current server tags with the new local song tags for uploading
        if (serverTags!=null) {
            tagsForUpload = new ArrayList<>(serverTags);
        } else {
            tagsForUpload = new ArrayList<>();
        }
        tagsForUpload.addAll(newTagsForUpload);
    }
    private String jsonNullIfEmpty(String string) {
        return (string==null || string.trim().isEmpty()) ? null : string;
    }
    private void removePointlessStuffFromSongs(ArrayList<OpenChordsSong> songobjects) {
        for (OpenChordsSong songobject : songobjects) {
            songobject.setTitle(trimmedOrNull(songobject.getTitle()));
            songobject.setLastUpdated(trimmedOrNull(songobject.getLastUpdated()));
            songobject.setCapo(nullFromZero(songobject.getCapo()));
            songobject.setArtist(trimmedOrNull(songobject.getArtist()));
            songobject.setCcli(trimmedOrNull(songobject.getCcli()));
            songobject.setCopyright(trimmedOrNull(songobject.getCopyright()));
            songobject.setTempo(nullFromZero(songobject.getTempo()));
            songobject.setDuration(trimmedOrNull(songobject.getDuration()));
            songobject.setKey(trimmedOrNull(songobject.getKey()));
            if (songobject.getKey()==null) {
                songobject.setKeyIsMinor(null);
            }
            songobject.setLastUpdated(trimmedOrNull(songobject.getLastUpdated()));
            songobject.setNotes(trimmedOrNull(songobject.getNotes()));
            songobject.setRawData(trimmedOrNull(songobject.getRawData()));
            songobject.setTimeSignature(trimmedOrNull(songobject.getTimeSignature()));
            songobject.setTranspose(trimmedOrNull(songobject.getTranspose()));
        }
    }
    private void removePointlessStuffFromSetLists(ArrayList<OpenChordsSetList> setLists) {
        for (OpenChordsSetList setList : setLists) {
            setList.setTitle(trimmedOrNull(setList.getTitle()));
            setList.setNotes(trimmedOrNull(setList.getNotes()));
            setList.setLastUpdated(trimmedOrNull(setList.getLastUpdated()));
            if (setList.getItems()!=null) {
                for (OpenChordsSetListItem setListItem : setList.getItems()) {
                    setListItem.setTitle(trimmedOrNull(setListItem.getTitle()));
                    setListItem.setLastUpdated(trimmedOrNull(setListItem.getLastUpdated()));
                    setListItem.setNotes(trimmedOrNull(setListItem.getNotes()));
                    setListItem.setType(trimmedOrNull(setListItem.getType()));
                    setListItem.setCustomData(trimmedOrNull(setListItem.getCustomData()));
                    if (setListItem.getSongItem()!=null) {
                        OpenChordsSetListSongItem songItem = setListItem.getSongItem();
                        songItem.setCapo(nullFromZero(songItem.getCapo()));
                        songItem.setTranspose(trimmedOrNull(songItem.getTranspose()));
                    }
                }
            }
        }
    }



    private String trimmedOrNull(String string) {
        return (string==null || string.trim().isEmpty()) ? null : string.trim();
    }
    private Integer nullFromZero(Integer integer) {
        return (integer==null || integer==0) ? null : integer;
    }

    private String removeUnnecessaryBitsFromJson(String json) {
        json = json.replace("\"capo\": 0,","");
        json = json.replace("\"tempo\": 0,","");
        json = json.replace("\"duration\": 0,","");
        json = json.replace("\"title\": \"\",","");
        return json;
    }
    public void forcePush() {
        conflictItemRecords = new ArrayList<>();
        checkForConflictObject();

        // If we need to update the uuid of local songs, do it
        updateLocalSongsUuid();

        // If we need to update the uuid of local sets, do it
        updateLocalSetsUuid();

        // This ignores the current server content and just uploads what we have on local
        updateProgress(c.getString(R.string.sync_upload_to_openchords)+"\n");

        // Prepare the upload folder object
        OpenChordsFolderObject uploadFolderObject = new OpenChordsFolderObject();
        uploadFolderObject.setTitle(getLocalFolderName());
        uploadFolderObject.setOwnerId(openChordsFolderUuid);

        // Deal with the songs
        songsForUpload = new ArrayList<>(localSongs);
        String nowTime = mainActivityInterface.getTimeTools().getNowIsoTime();
        for (OpenChordsSong song : localSongs) {
            OpenChordsConflictItemObject openChordsConflictItemObject = new OpenChordsConflictItemObject();
            openChordsConflictItemObject.setAction(c.getString(R.string.sync_last_force_uploaded));
            openChordsConflictItemObject.setItem(song.getTitle());
            openChordsConflictItemObject.setDate(nowTime);
            conflictItemRecords.add(openChordsConflictItemObject);
        }
        uploadFolderObject.setSongs(songsForUpload);

        // Deal with the sets
        setsForUpload = new ArrayList<>(localSetLists);
        for (OpenChordsSetList set : localSetLists) {
            OpenChordsConflictItemObject openChordsConflictItemObject = new OpenChordsConflictItemObject();
            openChordsConflictItemObject.setAction(c.getString(R.string.sync_set_force_uploaded));
            openChordsConflictItemObject.setItem(set.getTitle());
            openChordsConflictItemObject.setDate(nowTime);
            conflictItemRecords.add(openChordsConflictItemObject);
        }
        uploadFolderObject.setSetLists(setsForUpload);

        // Deal with the tags
        uploadFolderObject.setTags(newTagsForUpload);

        updateConflictItem("lastForcePush");

        String json = gson.toJson(uploadFolderObject);
        // Replace unneccessary items
        json = removeUnnecessaryBitsFromJson(json);
        mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings", "", "uploadFolderObject.json", json);

        Call<OpenChordsFolderObject> call = retrofitInterface.postOpenChordsFolder(uploadFolderObject.getOwnerId(), uploadFolderObject);
        call.enqueue(new Callback<OpenChordsFolderObject>() {
            @Override
            public void onResponse(@NonNull Call<OpenChordsFolderObject> call, @NonNull Response<OpenChordsFolderObject> response) {
                // this method is called when we get response from our api.
                if (openChordsFragment!=null) {
                    openChordsFragment.changeButtonsEnable(false);
                    updateProgress(c.getString(R.string.wait)+"\n");
                    mainActivityInterface.getMainHandler().postDelayed(() -> {
                        if (openChordsFragment!=null) {
                            openChordsFragment.queryOpenChordsServer();
                        }
                    },1000);
                }
            }

            @Override
            public void onFailure(@NonNull Call<OpenChordsFolderObject> call, @NonNull Throwable t) {
                if (openChordsFragment!=null) {
                    openChordsFragment.changeButtonsEnable(false);
                    updateProgress(c.getString(R.string.wait)+"\n");
                    mainActivityInterface.getMainHandler().postDelayed(() -> {
                        if (openChordsFragment!=null) {
                            openChordsFragment.queryOpenChordsServer();
                        }
                    },1000);
                }
            }
        });

        updateConflictFile();
    }


    // Update the song menu if we have downloaded files
    private void updateTheSongMenu() {
        // Now update the song menu as we have downloaded files
        mainActivityInterface.getSongListBuildIndex().setIndexRequired(true);
        mainActivityInterface.getSongListBuildIndex().setFullIndexRequired(true);
        mainActivityInterface.fullIndex();
    }


    // Clear sync objects to free memory
    // Called when closing the OpenChordsFragment and starting sync check
    public void clearSyncObjects() {
        // The server objects
        if (serverSongs!=null) {
            serverSongs.clear();
        } else {
            serverSongs = new ArrayList<>();
        }
        if (serverTags!=null) {
            serverTags.clear();
        } else {
            serverTags = new ArrayList<>();
        }
        if (serverSetLists!=null) {
            serverSetLists.clear();
        } else {
            serverSetLists = new ArrayList<>();
        }
        serverFolder = null;

        // The local objects
        localSongs.clear();
        localSetLists.clear();

        // The upload objects
        songsForUpload.clear();
        setsForUpload.clear();
        tagsForUpload.clear();
        newTagsForUpload.clear();

        // The comparison objects
        songsNotOnLocal.clear();
        songsNotOnServer.clear();
        songsOnLocalOlder.clear();
        songsOnServerOlder.clear();
        localSongsCompareObjects.clear();
        localSetListsCompareObjects.clear();
        setListsNotOnLocal.clear();
        setListsNotOnServer.clear();
        setListsOnLocalOlder.clear();
        setListsOnServerOlder.clear();
        serverSongsCompareObjects.clear();
        serverSetListsCompareObjects.clear();
        localSongNeedsServerUUID.clear();
        localSetListNeedsServerUUID.clear();

        // Reset the counts
        songsNotOnLocalCount = 0;
        songsNotOnServerCount = 0;
        songsWithNoChangesCount = 0;
        songsOnLocalOlderCount = 0;
        songsOnServerOlderCount = 0;
        setListsNotOnLocalCount = 0;
        setListsNotOnServerCount = 0;
        setListsOnLocalOlderCount = 0;
        setListsOnServerOlderCount = 0;
        setListsWithNoChangesCount = 0;
    }

    // Deal with maintaining the conflict file record
    public void loadConflictObject() {
        Uri conflictCheckUri = mainActivityInterface.getStorageAccess().getUriForItem("Settings", "", conflictCheckFile);
        if (!mainActivityInterface.getStorageAccess().uriExists(conflictCheckUri)) {
            openChordsConflictCheck = new OpenChordsConflictCheck();
            OpenChordsConflictObject openChordsConflictObject = new OpenChordsConflictObject();
            openChordsConflictObject.setUuid(openChordsFolderUuid);
            openChordsConflictObjects.add(openChordsConflictObject);
            openChordsConflictCheck.setConflictObects(openChordsConflictObjects);
            if (mainActivityInterface.getStorageAccess().getUriTreeHome()!=null) {
                updateConflictFile();
            }
        } else {
            openChordsConflictCheck = gson.fromJson(
                    mainActivityInterface.getStorageAccess().readTextFileToString(
                            mainActivityInterface.getStorageAccess().getInputStream(conflictCheckUri)),
                    OpenChordsConflictCheck.class);
            openChordsConflictObjects = openChordsConflictCheck.getConflictObjects();
        }
    }

    private void updateConflictItem(String which) {
        for (OpenChordsConflictObject conflictObject : openChordsConflictObjects) {
            if (conflictObject.getUuid()!=null && conflictObject.getUuid().equalsIgnoreCase(openChordsFolderUuid)) {
                String isoTime = mainActivityInterface.getTimeTools().getNowIsoTime();
                switch (which) {
                    case "lastQuery":
                        conflictObject.setLastQuery(isoTime);
                        break;
                    case "lastUploadNewSongs":
                        conflictObject.setLastUploadNewSongs(isoTime);
                        break;
                    case "lastUploadNewSets":
                        conflictObject.setLastUploadNewSets(isoTime);
                        break;
                    case "lastUploadSongChanges":
                        conflictObject.setLastUploadSongChanges(isoTime);
                        break;
                    case "lastUploadSetChanges":
                        conflictObject.setLastUploadSetChanges(isoTime);
                        break;
                    case "lastDownloadNewSongs":
                        conflictObject.setLastDownloadNewSongs(isoTime);
                        break;
                    case "lastDownloadNewSets":
                        conflictObject.setLastDownloadNewSets(isoTime);
                        break;
                    case "lastDownloadSongChanges":
                        conflictObject.setLastDownloadSongChanges(isoTime);
                        break;
                    case "lastDownloadSetChanges":
                        conflictObject.setLastDownloadSetChanges(isoTime);
                        break;
                    case "lastForcePush":
                        conflictObject.setLastForcePush(isoTime);
                        break;
                    case "lastForcePull":
                        conflictObject.setLastForcePull(isoTime);
                        break;
                }
                break;
            }
        }
        openChordsConflictCheck.setConflictObects(openChordsConflictObjects);
    }
    private void updateConflictFile() {
        ArrayList<OpenChordsConflictObject> openChordsConflictObjects = openChordsConflictCheck.getConflictObjects();
        for (OpenChordsConflictObject conflictObject : openChordsConflictObjects) {
            if (conflictObject.getUuid()!=null && conflictObject.getUuid().equals(openChordsFolderUuid)) {
                conflictObject.setItems(conflictItemRecords);
                break;
            }
        }
        openChordsConflictCheck.setConflictObects(openChordsConflictObjects);
        String json = gson.toJson(openChordsConflictCheck);
        mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings", "", conflictCheckFile, json);
    }
    private void checkForConflictObject() {
        boolean found = false;
        for (OpenChordsConflictObject conflictObject : openChordsConflictObjects) {
            if (conflictObject.getUuid() != null && conflictObject.getUuid().equalsIgnoreCase(openChordsFolderUuid)) {
                found = true;
                break;
            }
        }
        if (!found) {
            OpenChordsConflictObject openChordsConflictObject = new OpenChordsConflictObject();
            openChordsConflictObject.setUuid(openChordsFolderUuid);
            openChordsConflictObjects.add(openChordsConflictObject);
            openChordsConflictCheck.setConflictObects(openChordsConflictObjects);
            updateConflictFile();
        }
    }
    public String getLastModified(String which) {
        String returnVal = c.getString(R.string.is_not_set);
        for (OpenChordsConflictObject conflictObject : openChordsConflictObjects) {
            if (conflictObject.getUuid()!=null && conflictObject.getUuid().equalsIgnoreCase(openChordsFolderUuid)) {
                switch (which) {
                    case "lastQuery":
                        returnVal = conflictObject.getLastQuery();
                        break;
                    case "lastUploadNewSongs":
                        returnVal = conflictObject.getLastUploadNewSongs();
                        break;
                    case "lastUploadNewSets":
                        returnVal = conflictObject.getLastUploadNewSets();
                        break;
                    case "lastUploadSongChanges":
                        returnVal = conflictObject.getLastUploadSongChanges();
                        break;
                    case "lastUploadSetChanges":
                        returnVal = conflictObject.getLastUploadSetChanges();
                        break;
                    case "lastDownloadNewSongs":
                        returnVal = conflictObject.getLastDownloadNewSongs();
                        break;
                    case "lastDownloadNewSets":
                        returnVal = conflictObject.getLastDownloadNewSets();
                        break;
                    case "lastDownloadSongChanges":
                        returnVal = conflictObject.getLastDownloadSongChanges();
                        break;
                    case "lastDownloadSetChanges":
                        returnVal = conflictObject.getLastDownloadSetChanges();
                        break;
                    case "lastForcePush":
                        returnVal = conflictObject.getLastForcePush();
                        break;
                    case "lastForcePull":
                        returnVal = conflictObject.getLastForcePull();
                        break;
                }
            }
        }
        if (returnVal==null) {
            returnVal = c.getString(R.string.is_not_set);
        }
        return returnVal;
    }


    // Deal with the local folder uuid records
    public ArrayList<String> getValidFolders() {
        ArrayList<String> validFolders = new ArrayList<>();
        for (String folder : mainActivityInterface.getSQLiteHelper().getFolders()) {
            if (!folder.contains("/")) {
                validFolders.add(folder);
            }
        }
        return validFolders;
    }
    private void loadAndCheckOpenSongFolderObject() {
        // This checks our json file/object for our record of folders and uuids
        // If the object doesn't exist, a new one is created
        // If the object is out of date (i.e. has different folders), update it
        // This is called when initialising the class and when we open the OpenChordsFragment
        Uri openSongFolderUri = mainActivityInterface.getStorageAccess().getUriForItem("Settings", "", songFolderUUIDsFile);
        ArrayList<String> validFolders = getValidFolders();

        if (!mainActivityInterface.getStorageAccess().uriExists(openSongFolderUri)) {
            // Create a new one
            openSongFolderObject = new OpenSongFolderObject();
            openSongFolderObject.setOwnerID(String.valueOf(UUID.randomUUID()));
            OpenSongFolderRecordObject openSongFolderRecordObject;
            // Go through each folder in our system and create a UUID
            openSongFolderRecordObjects = new ArrayList<>();
            for (String folder : validFolders) {
                // Create a new record
                openSongFolderRecordObject = new OpenSongFolderRecordObject();
                openSongFolderRecordObject.setFolderName(folder);
                openSongFolderRecordObject.setFolderUuid(String.valueOf(UUID.randomUUID()));
                openSongFolderRecordObject.setFolderOwnerUuid(String.valueOf(UUID.randomUUID()));
                // Add the record to the list
                openSongFolderRecordObjects.add(openSongFolderRecordObject);
            }
            // Add the records to our folderObject
            openSongFolderObject.setOpenSongFolderRecordObjects(openSongFolderRecordObjects);
            // Now save the new json file
            saveOpenSongFolderObject();
        } else {
            // Load the existing information
            openSongFolderObject = gson.fromJson(
                    mainActivityInterface.getStorageAccess().readTextFileToString(
                            mainActivityInterface.getStorageAccess().getInputStream(openSongFolderUri)),
                    OpenSongFolderObject.class);
            openSongFolderRecordObjects = openSongFolderObject.getOpenSongFolderRecordObjects();

            // Now check if the entries are valid (i.e. the folder exists)
            boolean changes = false;
            // Check for references to folders that are no longer valid
            if (openSongFolderRecordObjects!=null) {
                for (OpenSongFolderRecordObject openSongFolderRecordObject : openSongFolderRecordObjects) {
                    if (!validFolders.contains(openSongFolderRecordObject.getFolderName())) {
                        // This folder is no longer valid, so remove it
                        openSongFolderRecordObjects.remove(openSongFolderRecordObject);
                        changes = true;
                    }
                }
            }

            // Now check that we have a record of all existing folders
            if (openSongFolderRecordObjects!=null) {
                for (String folder : validFolders) {
                    boolean found = false;
                    for (OpenSongFolderRecordObject openSongFolderRecordObject : openSongFolderRecordObjects) {
                        if (openSongFolderRecordObject.getFolderName()!=null && openSongFolderRecordObject.getFolderName().equals(folder)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // We don't have a record of this folder, so add it
                        openSongFolderRecordObjects.add(createNewFolderRecordObject(folder,
                                String.valueOf(UUID.randomUUID())));
                        changes = true;
                    }
                }
            }
            if (changes) {
                saveOpenSongFolderObject();
            }
        }
    }
    private void saveOpenSongFolderObject() {
        openSongFolderObject.setOpenSongFolderRecordObjects(openSongFolderRecordObjects);
        mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings", "",
                songFolderUUIDsFile, gson.toJson(openSongFolderObject));
    }
    public String getOpenSongFolderUuidFromName(@Nullable String folderName) {
        if (folderName!=null) {
            for (OpenSongFolderRecordObject openSongFolderRecordObject : openSongFolderRecordObjects) {
                if (openSongFolderRecordObject.getFolderName()!=null &&
                        openSongFolderRecordObject.getFolderName().equals(folderName)) {
                    return openSongFolderRecordObject.getFolderUuid();
                }
            }
        }
        return null;
    }
    public @Nullable String getOpenSongFolderNameFromUUID(@Nullable String folderUuid) {
        if (folderUuid!=null) {
            for (OpenSongFolderRecordObject openSongFolderRecordObject : openSongFolderRecordObjects) {
                if (openSongFolderRecordObject.getFolderUuid()!=null &&
                        openSongFolderRecordObject.getFolderUuid().equalsIgnoreCase(folderUuid)) {
                    return openSongFolderRecordObject.getFolderName();
                }
            }
        }
        return null;
    }
    public void changeOpenSongFolderUUID(String oldUuid, String newUuid) {
        // This will be called if we receive a link for an OpenChords folder
        // This is needed as we already have this folder, but with our own uuid
        // We need to update our local uuid to match the OpenChords folder
        for (OpenSongFolderRecordObject openSongFolderRecordObject : openSongFolderRecordObjects) {
            if (openSongFolderRecordObject.getFolderUuid()!=null && openSongFolderRecordObject.getFolderUuid().equalsIgnoreCase(oldUuid)) {
                openSongFolderRecordObject.setFolderUuid(newUuid);
                openSongFolderRecordObject.setFolderOwnerUuid(String.valueOf(newUuid));
                break;
            }
        }
        // Now save the json file
        saveOpenSongFolderObject();
    }

    private OpenSongFolderRecordObject createNewFolderRecordObject(String folderName,
                                                                   String folderUuid) {
        OpenSongFolderRecordObject openSongFolderRecordObject = new OpenSongFolderRecordObject();
        openSongFolderRecordObject.setFolderName(folderName);
        openSongFolderRecordObject.setFolderUuid(folderUuid);
        openSongFolderRecordObject.setFolderOwnerUuid(folderUuid);
        return openSongFolderRecordObject;
    }
    private void checkCreateLocalFolder() {
        // If we don't have a folder, create it
        String folderName = getOpenSongFolderNameFromUUID(openChordsFolderUuid);
        if (folderName==null && !openChordsFolderName.equalsIgnoreCase("MAIN") &&
            !openChordsFolderName.equalsIgnoreCase(mainActivityInterface.getMainfoldername())) {
            mainActivityInterface.getStorageAccess().createFolder("Songs","",openChordsFolderName,false);
            localFolderName = openChordsFolderName;
            openSongFolderRecordObjects.add(createNewFolderRecordObject(openChordsFolderName, openChordsFolderUuid));
            saveOpenSongFolderObject();
        }
    }

    public void updateLocalSongsUuid() {
        if (!localSongNeedsServerUUID.isEmpty()) {
            for (OpenChordsCompareObject openChordsCompareObject : localSongNeedsServerUUID) {
                // Get the existing local song
                Song localSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(getLocalFolderName(), openChordsCompareObject.getTitle());
                if (localSong != null) {
                    localSong.setUuid(openChordsCompareObject.getUuid());
                    mainActivityInterface.getSaveSong().setResetLastModified(false);
                    mainActivityInterface.getSQLiteHelper().updateSong(localSong);
                    mainActivityInterface.getSaveSong().updateSong(localSong, false);
                    mainActivityInterface.getSaveSong().setResetLastModified(true);
                }
            }
        }
    }

    public void updateLocalSetsUuid() {
        if (!localSetListNeedsServerUUID.isEmpty()) {
            for (OpenChordsCompareObject openChordsCompareObject : localSongNeedsServerUUID) {
                // Get the existing local song
                String setName = getOpenSongSetCategoryStart() + openChordsCompareObject.getTitle();
                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Sets", "", setName);
                if (mainActivityInterface.getStorageAccess().uriExists(uri)) {
                    String xml = mainActivityInterface.getStorageAccess().readTextFileToString(
                            mainActivityInterface.getStorageAccess().getInputStream(uri));
                    String substring = null;
                    if (xml.contains("<uuid>") && xml.contains("</uuid>")) {
                        int startpos = xml.indexOf("<uuid>") + 6;
                        int endpos = xml.indexOf("</uuid>");
                        if (endpos>startpos) {
                            substring = xml.substring(startpos, endpos);
                        }
                    }
                    if (substring!=null && openChordsCompareObject.getUuid()!=null) {
                        xml = xml.replace(substring, openChordsCompareObject.getUuid());
                        mainActivityInterface.getStorageAccess().doStringWriteToFile("Sets","",setName,xml);
                    }

                }



                Song localSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(getLocalFolderName(), openChordsCompareObject.getTitle());
                if (localSong != null) {
                    localSong.setUuid(openChordsCompareObject.getUuid());
                    mainActivityInterface.getSQLiteHelper().updateSong(localSong);
                    mainActivityInterface.getSaveSong().updateSong(localSong, false);
                }
            }
        }
    }
}
