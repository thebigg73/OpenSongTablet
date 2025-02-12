package com.garethevans.church.opensongtablet.openchords;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.RetrofitInterface;
import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
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

public class OpenChordsAPI implements Callback<OpenChordsFolderObject> {
    public static final Gson gson = new Gson();

    // This deals with intents that allow us to POST and GET synchronise with JustChords using the OpenChords interface

    private final MainActivityInterface mainActivityInterface;
    private final Context c;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "OpenChordsAPI";
    @SuppressWarnings("FieldCanBeLocal")
    private final String getAppFolderTrigger = "opensongapp://openchords?folder=",
            openChordsFolderBase = "https://openchords.net/swagger/folder/",
            openChordsApiBase = "https://openchords.net/api/folder/",
            openChordsFolderBaseShareable = "https://openchords.net/?fld=";

    // The retrofit, server and fragment declarations
    private final RetrofitInterface retrofitInterface;
    private OpenChordsFragment openChordsFragment;
    private boolean isServerResponse = false;
    private final String conflictCheckFile = "conflictCheck.json";
    private OpenChordsConflictCheck openChordsConflictCheck;
    private ArrayList<OpenChordsConflictObject> openChordsConflictObjects = new ArrayList<>();

    // Initialise the class
    public OpenChordsAPI(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        this.c = c;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(openChordsApiBase)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitInterface = retrofit.create(RetrofitInterface.class);
        openChordsFolderName = mainActivityInterface.getPreferences().getMyPreferenceString("openChordsFolderName",mainActivityInterface.getMainfoldername());
        openChordsFolderUuid = mainActivityInterface.getPreferences().getMyPreferenceString("openChordsFolderUuid",mainActivityInterface.getStorageAccess().getUUIDForSongFolder(openChordsFolderName));
    }

    public void initialise() {
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
            openChordsConflictCheck = gson.fromJson(mainActivityInterface.getStorageAccess().readTextFileToString(mainActivityInterface.getStorageAccess().getInputStream(conflictCheckUri)),OpenChordsConflictCheck.class);
            openChordsConflictObjects = openChordsConflictCheck.getConflictObjects();
        }
    }
    // Get a reference to the openChordsFragment
    public void setOpenChordsFragment(OpenChordsFragment openChordsFragment) {
        this.openChordsFragment = openChordsFragment;
    }

    // The objects retrieved from the server
    private OpenChordsFolderObject serverFolder;
    private String openChordsFolderName, openChordsFolderUuid;
    private ArrayList<OpenChordsSong> serverSongs = new ArrayList<>();
    private ArrayList<OpenChordsSetList> serverSetLists = new ArrayList<>();
    private ArrayList<OpenChordsTag> serverTags = new ArrayList<>();
    private final ArrayList<OpenChordsTag> newTagsForUpload = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> serverSongsCompareObjects = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> serverSetListsCompareObjects= new ArrayList<>();

    // The local objects
    private final ArrayList<OpenChordsSong> localSongs = new ArrayList<>();
    private final ArrayList<OpenChordsSetList> localSetLists = new ArrayList<>();
    private final ArrayList<OpenChordsTag> localTags = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> localSongsCompareObjects = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> localSetListsCompareObjects = new ArrayList<>();

    // The objects that hold the differences between the local and server
    private final ArrayList<OpenChordsCompareObject> songsNotOnLocal = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> songsNotOnServer = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> songsOnLocalOlder = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> songsOnServerOlder = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> songsWithNoChanges = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> setListsNotOnLocal = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> setListsNotOnServer = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> setListsOnLocalOlder = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> setListsOnServerOlder = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> setListsWithNoChanges = new ArrayList<>();

    // The objects for uploading
    private ArrayList<OpenChordsSong> songsForUpload = new ArrayList<>();
    private ArrayList<OpenChordsSetList> setsForUpload = new ArrayList<>();
    private ArrayList<OpenChordsTag> tagsForUpload = new ArrayList<>();
    private OpenChordsFolderObject folderUploadObject = null;

    // The variables used to display counts
    private int songsNotOnLocalCount = 0, songsNotOnServerCount = 0, songsWithNoChangesCount = 0,
            songsOnLocalOlderCount = 0, songsOnServerOlderCount = 0, setListsNotOnLocalCount = 0,
            setListsNotOnServerCount = 0, setListsOnLocalOlderCount = 0, setListsOnServerOlderCount = 0,
            setListsWithNoChangesCount = 0;

    // Get and set the folder information
    public void getFolderContentsFromUUID() {
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
    public OpenChordsFolderObject getServerFolder() {
        return serverFolder;
    }
    public void setOpenChordsFolderUuid(String openChordsFolderName, String openChordsFolderUuid) {
        if (openChordsFolderName==null || openChordsFolderName.isEmpty()) {
            // Try to look up a matching local folder
            openChordsFolderName = mainActivityInterface.getStorageAccess().getSongFolderForUUID(null,openChordsFolderUuid);
        }

        this.openChordsFolderName = openChordsFolderName;
        this.openChordsFolderUuid = openChordsFolderUuid;

        mainActivityInterface.getPreferences().setMyPreferenceString("openChordsFolderName",openChordsFolderName);
        mainActivityInterface.getPreferences().setMyPreferenceString("openChordsFolderUuid",openChordsFolderUuid);
    }
    public void setOpenChordsFolderName(String openChordsFolderName) {
        // This is called by the user choosing a new local folder from the OpenChordsFragment
        // We only have the folderName, so we need to lookup the UUID (or create it)
        this.openChordsFolderName = openChordsFolderName;
        openChordsFolderUuid = mainActivityInterface.getStorageAccess().getUUIDForSongFolder(openChordsFolderName);
    }


    // Create the local objects and populate the localCompareObjects
    private void createLocalCompareObjects() {
        // This creates OpenChords formatted songs, sets, tags from local files
        // Get a list of songs in this local folder
        ArrayList<Song> localOpenSongSongs = mainActivityInterface.getSQLiteHelper().openChordsSyncGetSongsFromFolder(openChordsFolderName);
        // For each found song, create an OpenChordsSong object and add it to the array
        for (Song localOpenSongSong : localOpenSongSongs) {
            updateProgress(c.getString(R.string.sync_checking_local_item)+"\n"+localOpenSongSong.getTitle());
            localSongs.add(convertOpenSongToOpenChords(localOpenSongSong));
            localSongsCompareObjects.add(createOpenChordsCompareObject(localOpenSongSong.getUuid(),
                    localOpenSongSong.getTitle(),localOpenSongSong.getLastModified(),"song"));
        }

        // Go through our sets and look for sets with a category matching OpenChords
        for (String setName : mainActivityInterface.getStorageAccess().listFilesInFolder("Sets", "")) {
            if (setName.startsWith(getOpenSongSetCategoryStart())) {
                OpenChordsSetList openChordsSetList = convertOpenSongSetToOpenChordsSetList(setName);
                localSetLists.add(openChordsSetList);
                localSetListsCompareObjects.add(createOpenChordsCompareObject(openChordsSetList.getId(),openChordsSetList.getTitle(),openChordsSetList.getLastUpdated(),"set"));
            }
        }
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
        openChordsCompareObject.setTitle(title);
        openChordsCompareObject.setLastModified(lastModified);
        openChordsCompareObject.setType(type);
        return openChordsCompareObject;
    }
    private OpenChordsSetListItem createOpenChordsSetListItem(String[] songInfo, String preferredKey) {
        OpenChordsSetListItem localSetListItem = new OpenChordsSetListItem();
        localSetListItem.setId(songInfo[0]);
        // Set list items don't include the song title
        localSetListItem.setTitle("");
        localSetListItem.setType("song");
        localSetListItem.setSongItem(createOpenChordsSetListSongItem(songInfo[0],preferredKey));
        return localSetListItem;
    }
    private OpenChordsSetListSongItem createOpenChordsSetListSongItem(String uuid, String transposeKey) {
        OpenChordsSetListSongItem openChordsSetListSongItem = new OpenChordsSetListSongItem();
        openChordsSetListSongItem.setSongId(uuid);
        // TODO something wrong with the server logic for transpose
        // openChordsSetListSongItem.setTranspose(transposeKey.replace("m",""));
        return openChordsSetListSongItem;
    }


    // The comparison information between the server and local
    private void findSongsNotOnLocal() {
        for (OpenChordsCompareObject serverObject : serverSongsCompareObjects) {
            boolean found = false;
            for (OpenChordsCompareObject localObject : localSongsCompareObjects) {
                if (localObject.getUuid().equals(serverObject.getUuid())) {
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
                if (serverObject.getUuid().equals(localObject.getUuid())) {
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
                if (localObject.getUuid().equals(serverObject.getUuid())) {
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
                if (serverObject.getUuid().equals(localObject.getUuid())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Add this local object
                setListsNotOnServer.add(localObject);
            }
        }
    }
    private void findSongsNeedingUpdated() {
        // We have already logged the missing files, so now we deal with matches/updates
        String lastDownloadSongChanges = getLastModified("lastDownloadSongChanges");
        long lastDownloadSongChangesMillis = 0;
        if (lastDownloadSongChanges!=null) {
            lastDownloadSongChangesMillis = Instant.parse(lastDownloadSongChanges).toEpochMilli();
        }
        for (OpenChordsCompareObject serverObject : serverSongsCompareObjects) {
            for (OpenChordsCompareObject localObject : localSongsCompareObjects) {
                if (localObject.getUuid().equals(serverObject.getUuid())) {
                    // This is a match, now decide if it needs updated or not
                    boolean serverObjectHasLastModified = true;
                    if (serverObject.getLastModified()==null || serverObject.getLastModified().isEmpty()) {
                        serverObject.setLastModified(mainActivityInterface.getTimeTools().getNowIsoTime());
                        serverObjectHasLastModified = false;
                    }
                    long serverObjectLastModified = Instant.parse(serverObject.getLastModified()).toEpochMilli();
                    boolean localObjectHasLastModified = true;
                    if (localObject.getLastModified()==null || localObject.getLastModified().isEmpty()) {
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
        if (lastDownloadSetChanges!=null) {
            lastDownloadSetChangesMillis = Instant.parse(lastDownloadSetChanges).toEpochMilli();
        }
        for (OpenChordsCompareObject serverObject : serverSetListsCompareObjects) {
            for (OpenChordsCompareObject localObject : localSetListsCompareObjects) {
                if (localObject.getUuid().equals(serverObject.getUuid())) {
                    // This is a match, now decide if it needs updated or not
                    boolean serverObjectHasLastModified = true;
                    if (serverObject.getLastModified()==null || serverObject.getLastModified().isEmpty()) {
                        serverObject.setLastModified(mainActivityInterface.getTimeTools().getNowIsoTime());
                        serverObjectHasLastModified = false;
                    }
                    long serverObjectLastModified = Instant.parse(serverObject.getLastModified()).toEpochMilli();
                    boolean localObjectHasLastModified = true;
                    if (localObject.getLastModified()==null || localObject.getLastModified().isEmpty()) {
                        localObject.setLastModified(mainActivityInterface.getTimeTools().getNowIsoTime());
                        localObjectHasLastModified = false;
                    }
                    long localObjectLastModified = Instant.parse(localObject.getLastModified()).toEpochMilli();

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
            stringBuilder.append(compareObject.getTitle().trim()).append(", ");
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
        Log.d(TAG,"server responded!");
        // Make sure we create a conflictObject for the folder if it doesn't exist
        checkForConflictObject();

        // Update the query time
        updateConflictItem("lastQuery");
        updateConflictFile();

        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Reset the list of objects found in the local and server and any differences
            clearSyncObjects();

            if (response.isSuccessful()) {
                isServerResponse = true;
                serverFolder = (OpenChordsFolderObject) response.body();

                if (serverFolder != null) {
                    // Lets get the server objects we have found!
                    updateProgress(c.getString(R.string.sync_reading_remote_folder)+"\n");
                    serverTags = serverFolder.getTags();
                    serverSongs = serverFolder.getSongs();
                    serverSetLists = serverFolder.getSetLists();

                    // Now create the server compare objects
                    createServerCompareObjects();
                }

            } else {
                Log.d(TAG, "Unsuccessful response from the server");
                serverFolder = null;
                serverSongs.clear();
                serverTags.clear();
                serverSetLists.clear();
                isServerResponse = false;
                if (openChordsFragment != null) {
                    openChordsFragment.openChordsFolderNotFound();
                }
            }

            // Now check our local folder
            updateProgress(c.getString(R.string.sync_reading_local_folder)+"\n");

            // Now create the local compare objects
            createLocalCompareObjects();

            // Now compare the local and server objects
            updateProgress(c.getString(R.string.sync_comparing_local_and_remote)+"\n");
            findSongsNotOnLocal();
            findSetsNotOnLocal();
            findSongsNotOnServer();
            findSetsNotOnServer();
            findSongsNeedingUpdated();
            findSetsNeedingUpdated();

            // Send the info back to the openChordsFragment
            mainActivityInterface.setWhattodo("");

            if (serverFolder != null) {
                // We just want the folder title for now
                // Make sure we have a record of this in our Settings
                String title = serverFolder.getTitle();
                mainActivityInterface.getStorageAccess().checkSongFolderUUIDExist(title, openChordsFolderUuid);
                setOpenChordsFolderUuid(title, openChordsFolderUuid);
            }

            if (openChordsFragment != null) {
                openChordsFragment.updateFolderTitle(openChordsFolderName);
                isServerResponse = false;
                openChordsFragment.logChanges();
            }
        });
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull Throwable throwable) {
        Log.d(TAG,"failure!!!!");
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
        song.setFolder(openChordsFolderName);
        song.setFilename(filename);
        song.setTitle(title);
        song.setLastModified(lastModified);
        song.setUuid(openChordsSong.getId());
        song.setAuthor(openChordsSong.getArtist());
        song.setLyrics(mainActivityInterface.getConvertJustChords().getOpenSongLyrics(openChordsSong.getRawData()));
        song.setAutoscrolllength(getEmptyForZero(mainActivityInterface.getTimeTools().getTotalSecsFromColonTimes(openChordsSong.getDuration())));
        song.setTimesig(openChordsSong.getTimeSignature());
        String key = openChordsSong.getKey();
        if (openChordsSong.isKeyIsMinor()) {
            key = key + "m";
        }
        song.setKey(key);
        song.setTempo(getEmptyForZero(openChordsSong.getTempo()));
        song.setCapo(getEmptyForZero(openChordsSong.getCapo()));
        song.setNotes(openChordsSong.getNotes());
        song.setCopyright(openChordsSong.getCopyright());
        song.setCcli(openChordsSong.getCcli());
        // Now get the tags
        StringBuilder tagStringBuilder = new StringBuilder();
        String[] tags = openChordsSong.getTags();
        if (tags!=null) {
            for (String tag:tags) {
                // Try to find this tag in the folder tags array
                for (OpenChordsTag openChordsTag : serverTags) {
                    if (tag.equals(openChordsTag.getId())) {
                        tagStringBuilder.append(openChordsTag.getTitle()).append("\n");
                        break;
                    }
                }
            }
        }
        song.setTheme(tagStringBuilder.toString().trim().replace("\n",";"));
        return song;
    }
    public String convertOpenChordsSetList(OpenChordsSetList serverSetList) {
        CurrentSet localSet = new CurrentSet(c);
        localSet.setUuid(serverSetList.getId());
        localSet.setSetCurrentLastName(convertOpenSongSetNameToOpenChordsSetName(serverSetList.getTitle()));
        localSet.setNotes(serverSetList.getNotes());
        localSet.setLastModified(serverSetList.getLastUpdated());

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

            if (itemType.equals("song")) {
                for (OpenChordsSong openChordsSong : serverSongs) {
                    if (openChordsSong.getId().equals(serverSetListItem.getId())) {
                        filename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(openChordsSong.getTitle());
                        title = serverSetListItem.getTitle();
                        key = openChordsSong.getKey() + (openChordsSong.isKeyIsMinor() ? "m" : "");
                        found = true;
                    }
                    if (found) {
                        break;
                    }
                }
                if (!found) {
                    for (OpenChordsSong openChordsSong : localSongs) {
                        if (openChordsSong.getId().equals(serverSetListItem.getId())) {
                            filename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(openChordsSong.getTitle());
                            key = openChordsSong.getKey() + (openChordsSong.isKeyIsMinor() ? "m" : "");
                            found = true;
                        }
                        if (found) {
                            break;
                        }
                    }
                }
                if (filename != null && title != null) {
                    localSet.addItemToSet(openChordsFolderName, filename, title, key, false);
                }

            } else if (itemType.equals("divider")) {
                // TODO deal with divider
                Log.d(TAG,"adding divider");
                Log.d(TAG,"itemTitle:"+itemTitle);
                Log.d(TAG,"itemId:"+itemId);
                localSet.addItemToSet("/", mainActivityInterface.getSetActions().getDividerIdentifier(),
                        mainActivityInterface.getSetActions().getDividerIdentifier(), null, false);

            } else if (itemType.equals("slide")) {
                // TODO deal with a slide/note
                Log.d(TAG,"adding slide");
                Log.d(TAG,"itemTitle:"+itemTitle);
                Log.d(TAG,"itemNotes:"+itemNotes);
                Log.d(TAG,"itemId:"+itemId);

                itemTitle = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(itemTitle);
                Song tempSong = mainActivityInterface.getProcessSong().initialiseSong("**Slides",itemTitle);
                tempSong.setLyrics(itemNotes);
                tempSong.setUuid(itemId);
                tempSong.setTitle(itemTitle);

                // Save this temp song so we can recover the contents when we build the set file
                mainActivityInterface.getStorageAccess().saveThisSongFile(tempSong);
                localSet.addItemToSet(tempSong);

            }
        }
        // Update the last modified to match the value we want (default setXML is now time)
        mainActivityInterface.getSetActions().setUseThisLastModifiedDate(serverSetList.getLastUpdated());
        String xml = mainActivityInterface.getSetActions().createSetXML(localSet);
        mainActivityInterface.getSetActions().setUseThisLastModifiedDate(null);
        return xml;
    }
    private String convertOpenChordsSetNameToOpenSongSetName(String openChordsSetName) {
        return "OpenChords"+mainActivityInterface.getSetActions().getSetCategorySeparator()+
                mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(openChordsSetName);
    }
    private String getEmptyForZero(int integer) {
        return integer==0 ? "" : String.valueOf(integer);
    }


    // Convert OpenSong objects into OpenChords objects
    public OpenChordsSong convertOpenSongToOpenChords(Song openSongSong) {
        OpenChordsSong openChordsSong = new OpenChordsSong();
        openChordsSong.setId(openSongSong.getUuid());
        openChordsSong.setTitle(openSongSong.getFilename());
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
        // openChordsSong.setTranspose(key); // Don't use this for now
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
                    if (serverTag.getTitle().equals(localTag)) {
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
        // Parse the set XML file and get the contents
        ArrayList<String> thisSet = new ArrayList<>();
        thisSet.add(filename);
        String[] bits = mainActivityInterface.getExportActions().parseSets(thisSet);

        // bits[0] = folder/filename pair
        // bits[1] = text for export (not needed here) which includes author, hymn num and key
        // bits[2] = preferred key
        // bits[3] = uuid (creates new if null)
        // bits[4] = lastModified (creates new if null)
        // bits[5] = notes

        OpenChordsSetList openChordsSetList = new OpenChordsSetList();

        String actualSetName = convertOpenSongSetNameToOpenChordsSetName(filename);
        openChordsSetList.setTitle(actualSetName);
        openChordsSetList.setId(bits[3]);
        openChordsSetList.setLastUpdated(bits[4]);
        openChordsSetList.setNotes(jsonNullIfEmpty(bits[5]));

        // Now we need to go through the set items and add them
        // TODO deal with separators and slides/notes rather than just songs
        String[] folderAndFiles = bits[0].split("\n");
        String[] preferredKeys = bits[2].split("\n");
        ArrayList<OpenChordsSetListItem> localSetListItems = new ArrayList<>();
        for (int i = 0; i < folderAndFiles.length; i++) {
            String folderAndFile = folderAndFiles[i];
            String preferredKey = preferredKeys[i];
            // Now we need to get the id of the song item
            String[] songInfo = mainActivityInterface.getSQLiteHelper().getUuidFromFolderAndFile(folderAndFile);
            localSetListItems.add(createSetListItem(songInfo, preferredKey));
        }
        openChordsSetList.setItems(localSetListItems);
        return openChordsSetList;
    }
    private OpenChordsSetListItem createSetListItem(String[] songInfo, String preferredKey) {
        OpenChordsSetListItem localSetListItem = new OpenChordsSetListItem();
        localSetListItem.setId(songInfo[0]);
        // Set list items don't include the song title
        localSetListItem.setTitle(null);
        localSetListItem.setType("song");
        OpenChordsSetListSongItem openChordsSetListSongItem = new OpenChordsSetListSongItem();
        openChordsSetListSongItem.setSongId(songInfo[0]);
        // TODO something wrong with the server logic for transpose
        //if (preferredKey!=null && !preferredKey.isEmpty() && !preferredKey.equals("ignore")) {
        //    openChordsSetListSongItem.setTranspose(preferredKey.replace("m",""));
        //}
        localSetListItem.setSongItem(openChordsSetListSongItem);
        return localSetListItem;
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
        boolean songMenuUpdate = false;
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
                if (compareObject.getUuid().equals(serverSong.getId())) {
                    // This is a song we want
                    String title = compareObject.getTitle();
                    String filename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(title);
                    updateProgress(c.getString(R.string.sync_creating_new_item) + " (" + c.getString(R.string.song) + ")\n" + title);
                    Uri songUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",
                            openChordsFolderName, filename);
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, songUri, null,
                            "Songs", openChordsFolderName, filename);
                    Song newOpenSongSong = convertOpenChordsToOpenSong(filename,title,compareObject.getLastModified(),serverSong);

                    // Save the song
                    mainActivityInterface.getSQLiteHelper().createSong(openChordsFolderName, filename);
                    mainActivityInterface.getSaveSong().setResetLastModified(false);
                    mainActivityInterface.getSaveSong().updateSong(newOpenSongSong, false);
                    mainActivityInterface.getSaveSong().setResetLastModified(true);

                    // Remove this item from the compareObjects since we have dealt with it
                    songsNotOnLocal.remove(compareObject);

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
        for (OpenChordsSong serverSong : serverSongs) {
            for (OpenChordsCompareObject compareObject : songsOnLocalOlder) {
                if (compareObject.getUuid().equals(serverSong.getId())) {
                    // This is a song we want
                    String title = serverSong.getTitle();
                    String filename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(title);
                    updateProgress(c.getString(R.string.sync_updating_item) + " (" + c.getString(R.string.song) + ")\n" + title);

                    // Get the existing song so we only update the info held by OpenChords
                    Song existingSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(openChordsFolderName,filename);
                    Uri songUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",
                            openChordsFolderName, filename);
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, songUri, null,
                            "Songs", openChordsFolderName, filename);
                    Song newOpenSongSong = convertOpenChordsToOpenSong(filename,title,serverSong.getLastUpdated(),serverSong);

                    // If we have changed the title/filename, we need to update the database
                    String oldtitle = compareObject.getTitle();
                    String oldfilename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(oldtitle);
                    if (!oldfilename.equals(filename)) {
                        mainActivityInterface.getSQLiteHelper().deleteSong(openChordsFolderName, mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(compareObject.getTitle()));
                        mainActivityInterface.getSQLiteHelper().createSong(openChordsFolderName, filename);
                    }

                    // Update the existing song with the info received (not all OpenSong stuff is in OpenChords!)
                    updateExistingOpenSongWithOpenChords(existingSong, newOpenSongSong);

                    // Save the song
                    mainActivityInterface.getSaveSong().setResetLastModified(false);
                    mainActivityInterface.getSaveSong().updateSong(existingSong, false);
                    mainActivityInterface.getSaveSong().setResetLastModified(true);

                    // Remove this item from the compareObjects since we have dealt with it
                    songsOnLocalOlder.remove(compareObject);

                    // Skip to the next server song
                    break;
                }
            }
        }
        updateConflictItem("lastDownloadSongChanges");
    }
    private void downloadNewServerSetLists() {
        // Go through the serverSetLists and download the ones we have a record of in the compare objects
        for (OpenChordsSetList serverSetList : serverSetLists) {
            for (OpenChordsCompareObject compareObject : setListsNotOnLocal) {
                if (compareObject.getUuid().equals(serverSetList.getId())) {
                    // This is a setList we want
                    String title = compareObject.getTitle();
                    String filename = convertOpenChordsSetNameToOpenSongSetName(title);
                    updateProgress(c.getString(R.string.sync_creating_new_item) + " (" + c.getString(R.string.set)+")\n" + title);

                    String setXML = convertOpenChordsSetList(serverSetList);

                    // Save the set
                    mainActivityInterface.getStorageAccess().doStringWriteToFile("Sets","",filename,setXML);

                    // Remove this item from the compareObjects since we have dealt with it
                    setListsNotOnLocal.remove(compareObject);

                    // Skip to the next server set
                    break;
                }
            }
        }
        updateConflictItem("lastDownloadNewSets");
    }
    private void updateLocalSetLists() {
        // Go through the serverSetLists and update the ones we have a record of in the compare objects
        for (OpenChordsSetList serverSetList : serverSetLists) {
            for (OpenChordsCompareObject compareObject : setListsOnLocalOlder) {
                if (compareObject.getUuid().equals(serverSetList.getId())) {
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

        // Clear the existing songs from the database
        updateProgress(c.getString(R.string.sync_deleting_local_items));
        ArrayList<Song> songsInFolder = mainActivityInterface.getSQLiteHelper().openChordsSyncGetSongsFromFolder(openChordsFolderName);
        for (Song song : songsInFolder) {
            mainActivityInterface.getSQLiteHelper().deleteSong(openChordsFolderName, song.getFilename());
        }
        songsInFolder.clear();

        // Now remove the song files in the local folder
        mainActivityInterface.getStorageAccess().wipeFolder("Songs",openChordsFolderName);

        // Delete OpenChords__ set files
        ArrayList<String> setFiles = mainActivityInterface.getStorageAccess().listFilesInFolder("Sets","");
        for (String setFile : setFiles) {
            if (setFile.startsWith("OpenChords"+mainActivityInterface.getSetActions().getSetCategorySeparator())) {
                mainActivityInterface.getStorageAccess().doDeleteFile("Sets","",setFile);
            }
        }

        // Now to download the stuff from the server
        // Go through the serverSongs and download them all
        for (OpenChordsSong serverSong : serverSongs) {
            String title = serverSong.getTitle();
            updateProgress(c.getString(R.string.sync_creating_new_item) + " (" + c.getString(R.string.song) + ")\n" + title);
            String filename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(title);
            Song newOpenSongSong = convertOpenChordsToOpenSong(filename,title,serverSong.getLastUpdated(),serverSong);
            // Save the song
            mainActivityInterface.getSQLiteHelper().createSong(openChordsFolderName, filename);
            mainActivityInterface.getSaveSong().setResetLastModified(false);
            mainActivityInterface.getSaveSong().updateSong(newOpenSongSong, false);
            mainActivityInterface.getSaveSong().setResetLastModified(true);
        }

        // Go through the serverSongs and download them all
        for (OpenChordsSetList serverSetList : serverSetLists) {
            String title = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(serverSetList.getTitle());
            String filename = convertOpenChordsSetNameToOpenSongSetName(title);
            updateProgress(c.getString(R.string.sync_creating_new_item) + " (" + c.getString(R.string.set_list) + ")\n" + title);

            // Get the xml for the setlist
            String xml = convertOpenChordsSetList(serverSetList);
            mainActivityInterface.getStorageAccess().doStringWriteToFile("Sets","",filename,xml);
        }

        updateConflictItem("lastForcePull");
        updateConflictFile();
    }

    // The upload logic
    public void prepareUpload(boolean newSongs, boolean updateSongs, boolean newSetLists, boolean updateSetLists) {
        // Prepare the upload folder object
        OpenChordsFolderObject uploadFolderObject = new OpenChordsFolderObject();
        uploadFolderObject.setTitle(openChordsFolderName);
        uploadFolderObject.setOwnerId(openChordsFolderUuid);

        // Deal with the songs
        updateProgress(c.getString(R.string.sync_reading_local_folder)+"\n");
        if (newSongs) {
            prepareUploadServerSongsAndNewLocal();
        } else if (updateSongs) {
            prepareUploadServerSongsAndUpdates();
        } else {
            songsForUpload = serverSongs;
        }
        uploadFolderObject.setSongs(songsForUpload);

        // Deal with the sets
        if (newSetLists) {
            prepareUploadServerSetsAndNewLocal();
        } else if (updateSetLists) {
            prepareUploadServerSetsAndUpdates();
        } else {
            setsForUpload = serverSetLists;
        }
        uploadFolderObject.setSetLists(setsForUpload);

        // Deal with the tags
        prepareUploadServerTagsAndNewLocal();
        uploadFolderObject.setTags(tagsForUpload);

        String json = gson.toJson(uploadFolderObject);
        // Replace unneccessary items
        json = removeUnnecessaryBitsFromJson(json);
        mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings", "", "uploadFolderObject.json", json);

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
        songsForUpload = new ArrayList<>(serverSongs);
        for (OpenChordsCompareObject compareObject : songsNotOnServer) {
            updateProgress(c.getString(R.string.sync_preparing_item) + " ("+c.getString(R.string.song)+")\n" + compareObject.getTitle());
            Song song = mainActivityInterface.getSQLiteHelper().getOpenChordsSong(openChordsFolderName,compareObject.getUuid());
            OpenChordsSong newSong = convertOpenSongToOpenChords(song);
            songsForUpload.add(newSong);
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
                if (serverSong.getId().equals(compareObject.getUuid())) {
                    updateProgress(c.getString(R.string.sync_preparing_item) + " ("+c.getString(R.string.song)+")\n" + compareObject.getTitle());
                    Song song = mainActivityInterface.getSQLiteHelper().getOpenChordsSong(openChordsFolderName,compareObject.getUuid());
                    OpenChordsSong newSong = convertOpenSongToOpenChords(song);
                    songsForUpload.add(newSong);
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
        setsForUpload = new ArrayList<>(serverSetLists);
        for (OpenChordsCompareObject compareObject : setListsNotOnServer) {
            updateProgress(c.getString(R.string.sync_preparing_item) + " ("+c.getString(R.string.set_list)+")\n" + compareObject.getTitle());
            OpenChordsSetList openChordsSetList = convertOpenSongSetToOpenChordsSetList(getOpenSongSetCategoryStart()+compareObject.getTitle());
            setsForUpload.add(openChordsSetList);
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
                if (serverSetList.getId().equals(compareObject.getUuid())) {
                    updateProgress(c.getString(R.string.sync_preparing_item) + " ("+c.getString(R.string.set)+")\n" + compareObject.getTitle());
                    OpenChordsSetList openChordsSetList = convertOpenSongSetToOpenChordsSetList(getOpenSongSetCategoryStart()+compareObject.getTitle());
                    setsForUpload.add(openChordsSetList);
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
        tagsForUpload = new ArrayList<>(serverTags);
        tagsForUpload.addAll(newTagsForUpload);
    }
    private String jsonNullIfEmpty(String string) {
        return string==null || string.isEmpty() ? null : string;
    }
    private String removeUnnecessaryBitsFromJson(String json) {
        json = json.replace("\"capo\":0,","");
        json = json.replace("\"tempo\":0,","");
        json = json.replace("\"duration\":0,","");
        return json;
    }
    public void forcePush() {
        // This ignores the current server content and just uploads what we have on local
        updateProgress(c.getString(R.string.sync_upload_to_openchords)+"\n");

        // Prepare the upload folder object
        OpenChordsFolderObject uploadFolderObject = new OpenChordsFolderObject();
        uploadFolderObject.setTitle(openChordsFolderName);
        uploadFolderObject.setOwnerId(openChordsFolderUuid);

        // Deal with the songs
        songsForUpload = new ArrayList<>(localSongs);
        uploadFolderObject.setSongs(songsForUpload);

        // Deal with the sets
        setsForUpload = new ArrayList<>(localSetLists);
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
        serverSongs.clear();
        serverTags.clear();
        serverSetLists.clear();
        serverFolder = null;

        // The local objects
        localSongs.clear();
        localSetLists.clear();
        localTags.clear();
        newTagsForUpload.clear();

        // The upload objects
        if (songsForUpload!=null) {
            songsForUpload.clear();
        }
        if (setsForUpload!=null) {
            setsForUpload.clear();
        }
        if (tagsForUpload!=null) {
            tagsForUpload.clear();
        }
        folderUploadObject = null;

        // The comparison objects
        songsNotOnLocal.clear();
        songsNotOnServer.clear();
        songsWithNoChanges.clear();
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
    private void updateConflictItem(String which) {
        for (OpenChordsConflictObject conflictObject : openChordsConflictObjects) {
            if (conflictObject.getUuid().equals(openChordsFolderUuid)) {
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
        String json = gson.toJson(openChordsConflictCheck);
        mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings", "", conflictCheckFile, json);
    }
    private void checkForConflictObject() {
        boolean found = false;
        for (OpenChordsConflictObject conflictObject : openChordsConflictObjects) {
            if (conflictObject.getUuid().equals(openChordsFolderUuid)) {
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
    private String getLastModified(String which) {
        for (OpenChordsConflictObject conflictObject : openChordsConflictObjects) {
            if (conflictObject.getUuid().equals(openChordsFolderUuid)) {
                switch (which) {
                    case "lastQuery":
                        return conflictObject.getLastQuery();
                    case "lastUploadNewSongs":
                        return conflictObject.getLastUploadNewSongs();
                    case "lastUploadNewSets":
                        return conflictObject.getLastUploadNewSets();
                    case "lastUploadSongChanges":
                        return conflictObject.getLastUploadSongChanges();
                    case "lastUploadSetChanges":
                        return conflictObject.getLastUploadSetChanges();
                    case "lastDownloadNewSongs":
                        return conflictObject.getLastDownloadNewSongs();
                    case "lastDownloadNewSets":
                        return conflictObject.getLastDownloadNewSets();
                    case "lastDownloadSongChanges":
                        return conflictObject.getLastDownloadSongChanges();
                    case "lastDownloadSetChanges":
                        return conflictObject.getLastDownloadSetChanges();
                }
            }
        }
        return null;
    }
}
