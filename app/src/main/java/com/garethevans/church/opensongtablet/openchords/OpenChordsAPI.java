package com.garethevans.church.opensongtablet.openchords;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.drummer.DrumCalculations;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import okhttp3.OkHttpClient;
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
    @SuppressWarnings({"unused", "FieldCanBeLocal" })
    private final String TAG = "OpenChordsAPI";
    @SuppressWarnings("FieldCanBeLocal")
    private final String getAppFolderTrigger = "opensongapp://openchords?folder=",
            openChordsApiBase = "https://openchords.net/api/v2/",
            openChordsFolderBaseShareable = "https://openchords.net/?fld=",
            songFolderUUIDsFile = "songFolderUUIDs.json";
    private boolean receivedFolderLink = false, isOwner, isReadOnly, folderIsDifferentUuid;
    private String jwtToken;

    // The retrofit, server and fragment declarations
    private RetrofitInterface retrofitInterface;
    private OpenChordsFragment openChordsFragment;
    private boolean isServerResponse = false;
    private final String conflictCheckFile = "conflictCheck.json";
    private OpenChordsConflictCheck openChordsConflictCheck;
    private ArrayList<OpenChordsConflictObject> openChordsConflictObjects = new ArrayList<>();
    private OpenSongFolderObject openSongFolderObject;
    private ArrayList<OpenSongFolderRecordObject> openSongFolderRecordObjects = new ArrayList<>();
    private ArrayList<OpenChordsConflictItemObject> conflictItemRecords = new ArrayList<>();
    private final String openChordsUsername;
    private final String openChordsPassword;

    // Initialise the class
    public OpenChordsAPI(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        this.c = c;

        // Get the login credentials
        openChordsUsername = mainActivityInterface.getPreferences().getKey(c, "openchordsusername");
        openChordsPassword = mainActivityInterface.getPreferences().getKey(c, "openchordspassword");
        openChordsUserUuid = mainActivityInterface.getPreferences().getMyPreferenceString("openChordsUserUuid",null);
        if (openChordsUserUuid == null) {
            openChordsUserUuid = UUID.randomUUID().toString();
            mainActivityInterface.getPreferences().setMyPreferenceString("openChordsUserUuid",openChordsUserUuid);
        }

        // Get our jwtToken.  If it is notset or has expired, we will get the token when we open the fragment
        SharedPreferences prefs = c.getSharedPreferences("OpenChords", Context.MODE_PRIVATE);
        jwtToken = prefs.getString("jwtToken", "notset");
    }

    private void rebuildRetrofitInterface(){
        AuthInterceptor interceptor = new AuthInterceptor(jwtToken);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        retrofitInterface = new Retrofit.Builder()
                .baseUrl(openChordsApiBase)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build().create(RetrofitInterface.class);
    }

    private void getJwtToken() {
        @SuppressLint("HardwareIds") OpenChordsLoginRequest loginRequest = new OpenChordsLoginRequest(openChordsUsername,
                openChordsPassword, Settings.Secure.getString(c.getContentResolver(),
                Settings.Secure.ANDROID_ID));

        if (retrofitInterface == null) {
            rebuildRetrofitInterface();
        }

        Call<OpenChordsLoginResponse> call = retrofitInterface.getAuthToken(loginRequest);
        call.enqueue(new Callback<OpenChordsLoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<OpenChordsLoginResponse> call, @NonNull Response<OpenChordsLoginResponse> response) {
                if (response.isSuccessful()) {
                    OpenChordsLoginResponse loginResponse = response.body();
                    if (loginResponse != null) {
                        jwtToken = loginResponse.getToken();

                        SharedPreferences prefs = c.getSharedPreferences("OpenChords", Context.MODE_PRIVATE);
                        prefs.edit().putString("jwtToken", jwtToken).apply();

                        rebuildRetrofitInterface();

                        // Now we can run the query if the openChordsFragment isn't null
                        if (openChordsFragment != null) {
                            openChordsFragment.queryOpenChordsServer();
                        }
                    }
                } else {
                    mainActivityInterface.getShowToast().doIt(c.getString(R.string.network_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<OpenChordsLoginResponse> call, @NonNull Throwable t) {
                // Handle network errors or other failures
                Log.e("Login Failure", t.getMessage());
            }
        });
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
            if (openChordsFolderName != null) {
                // Because we have a matching folder, we want to save our new preference
                mainActivityInterface.getPreferences().setMyPreferenceString(
                        "openChordsFolderName", openChordsFolderName);
            }
            // If we come to the openChordsFragment again, we will use our preference instead
            // (unless we receive an intent again)
            receivedFolderLink = false;
        } else {
            // We set this using a local folder, so just find the uuid using our save preference
            openChordsFolderName = mainActivityInterface.getPreferences().getMyPreferenceString(
                    "openChordsFolderName", mainActivityInterface.getMainfoldername());
            openChordsFolderUuid = getOpenSongFolderUuidFromName(openChordsFolderName);
        }
    }

    // Get a reference to the openChordsFragment
    public void setOpenChordsFragment(OpenChordsFragment openChordsFragment) {
        this.openChordsFragment = openChordsFragment;
    }

    public void setReceivedFolderLink(boolean receivedFolderLink) {
        this.receivedFolderLink = receivedFolderLink;
    }

    // The objects retrieved from the server
    private OpenChordsFolderObject serverFolder;
    private String openChordsFolderName, openChordsFolderUuid, openChordsUserUuid, localFolderName;
    private ArrayList<OpenChordsSong> serverSongs = new ArrayList<>();
    private ArrayList<OpenChordsSetList> serverSetLists = new ArrayList<>();
    private ArrayList<OpenChordsTag> serverTags = new ArrayList<>();
    private final ArrayList<OpenChordsTag> newTagsForUpload = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> serverSongsCompareObjects = new ArrayList<>();
    private final ArrayList<OpenChordsCompareObject> serverSetListsCompareObjects = new ArrayList<>();

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
    @SuppressWarnings({"unused", "FieldCanBeLocal" })
    private int songsWithNoChangesCount = 0, setListsWithNoChangesCount = 0;

    // Get and set the folder information
    public void getFolderContentsFromUUID() {
        receivedFolderLink = false;
        if (retrofitInterface==null) {
            rebuildRetrofitInterface();
        }
        Call<OpenChordsFolderObject> call = retrofitInterface.getOpenChordsFolder(openChordsFolderUuid, openChordsUserUuid);
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
        if (localFolderName == null) {
            return openChordsFolderName;
        } else {
            return localFolderName;
        }
    }

    public void setLocalFolderName(String localFolderName) {
        if (localFolderName == null) {
            this.localFolderName = openChordsFolderName;
        } else {
            this.localFolderName = localFolderName;
        }
    }

    // Create the local objects and populate the localCompareObjects
    private void createLocalCompareObjects() {
        // This creates OpenChords formatted songs, sets, tags from local files
        // Get a list of songs in this local folder
        // If we have received a folder from the server, we need to match it
        if (openChordsFolderName!=null && !openChordsFolderName.isEmpty()) {
            localFolderName = openChordsFolderName;
        }

        if (localFolderName != null) {
            ArrayList<Song> localOpenSongSongs = mainActivityInterface.getSQLiteHelper().openChordsSyncGetSongsFromFolder(getLocalFolderName());
            // For each found song, create an OpenChordsSong object and add it to the array
            for (int i = 0; i < localOpenSongSongs.size(); i++) {
                Song localOpenSongSong = localOpenSongSongs.get(i);
                // Only allow xml songs (no PDF/images)
                if (!mainActivityInterface.getStorageAccess().isIMGorPDF(localOpenSongSong)) {
                    updateProgress(c.getString(R.string.sync_checking_local_item) + "\n" + localOpenSongSong.getTitle());
                    localSongs.add(convertOpenSongToOpenChords(localOpenSongSong));
                    localSongsCompareObjects.add(createOpenChordsCompareObject(localOpenSongSong.getUuid(),
                            localOpenSongSong.getFilename(), localOpenSongSong.getLastModified(), "song"));
                }
            }
            if (!localSongs.isEmpty()) {
                removePointlessStuffFromSongs(localSongs);
            }

            // Go through our sets and look for sets with a category matching OpenChords
            ArrayList<String> setNames = mainActivityInterface.getStorageAccess().listFilesInFolder("Sets", "");
            if (setNames != null && !setNames.isEmpty()) {
                for (int i = 0; i < setNames.size(); i++) {
                    String setName = setNames.get(i);
                    if (setName!=null && setName.startsWith(getOpenSongSetCategoryStart())) {
                        OpenChordsSetList openChordsSetList = convertOpenSongSetToOpenChordsSetList(setName);
                        if (openChordsSetList!=null && openChordsSetList.getTitle()!=null) {
                            openChordsSetList.setTitle(openChordsSetList.getTitle().replace("¦¦", "|"));
                            localSetLists.add(openChordsSetList);
                            localSetListsCompareObjects.add(createOpenChordsCompareObject(openChordsSetList.getId(), openChordsSetList.getTitle(), openChordsSetList.getLastUpdated(), "set"));
                        }
                    }
                }
            }
            if (!localSetLists.isEmpty()) {
                removePointlessStuffFromSetLists(localSetLists);
            }
        }
    }

    private void createServerCompareObjects() {
        // This goes through the server objects and converts them to compareObjects
        for (int i = 0; i < serverSongs.size(); i++) {
            OpenChordsSong serverObject = serverSongs.get(i);
            serverSongsCompareObjects.add(createOpenChordsCompareObject(serverObject.getId(), serverObject.getTitle(), serverObject.getLastUpdated(), "song"));
        }
        for (int i = 0; i < serverSetLists.size(); i++) {
            OpenChordsSetList serverObject = serverSetLists.get(i);
            serverSetListsCompareObjects.add(createOpenChordsCompareObject(serverObject.getId(), serverObject.getTitle(), serverObject.getLastUpdated(), "set"));
        }
    }

    private OpenChordsCompareObject createOpenChordsCompareObject(String uuid, String title,
                                                                  String lastModified, String type) {
        OpenChordsCompareObject openChordsCompareObject = new OpenChordsCompareObject();
        openChordsCompareObject.setUuid(uuid);
        String newTitle = jsonNullIfEmpty(title);
        if (newTitle!=null) {
            newTitle = newTitle.replace("|","¦¦");
        }
        openChordsCompareObject.setTitle(newTitle);
        openChordsCompareObject.setLastModified(lastModified);
        openChordsCompareObject.setType(type);

        return openChordsCompareObject;
    }

    // The comparison information between the server and local
    private void findSongsNotOnLocal() {
        for (int i = 0; i < serverSongsCompareObjects.size(); i++) {
            OpenChordsCompareObject serverObject = serverSongsCompareObjects.get(i);
            boolean found = false;
            for (int j = 0; j < localSongsCompareObjects.size(); j++) {
                OpenChordsCompareObject localObject = localSongsCompareObjects.get(j);
                // We can have matching uuid or filename
                if (localObject.getUuid() != null && localObject.getUuid().equalsIgnoreCase(serverObject.getUuid())) {
                    found = true;
                    break;
                }
                if (localObject.getUuid()==null || (localObject.getUuid()!=null && !localObject.getUuid().equals(serverObject.getUuid()) && localObject.getTitle() != null && localObject.getTitle().equals(serverObject.getTitle()) && localObject.getLastModified()!=null && localObject.getLastModified().equals(serverObject.getLastModified()))) {
                    // We have it, but we need the server UUID
                    localSongNeedsServerUUID.add(serverObject);
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
        for (int i = 0; i < localSongsCompareObjects.size(); i++) {
            OpenChordsCompareObject localObject = localSongsCompareObjects.get(i);
            boolean found = false;
            for (int j = 0; j < serverSongsCompareObjects.size(); j++) {
                OpenChordsCompareObject serverObject = serverSongsCompareObjects.get(j);
                if (serverObject.getUuid() != null && serverObject.getUuid().equalsIgnoreCase(localObject.getUuid())) {
                    found = true;
                    break;
                }
                /*if (localObject.getTitle() != null && localObject.getTitle().equals(serverObject.getTitle())) {
                    // Just ignore for now.  We will update the local uuid
                    found = true;
                    break;
                }*/
            }
            if (!found) {
                // Add this local object
                songsNotOnServer.add(localObject);
            }
        }
    }

    private void findSetsNotOnLocal() {
        for (int i = 0; i < serverSetListsCompareObjects.size(); i++) {
            OpenChordsCompareObject serverObject = serverSetListsCompareObjects.get(i);
            boolean found = false;
            for (int j = 0; j < localSetListsCompareObjects.size(); j++) {
                OpenChordsCompareObject localObject = localSetListsCompareObjects.get(j);
                if (localObject.getUuid() != null && localObject.getUuid().equalsIgnoreCase(serverObject.getUuid())) {
                    found = true;
                    break;
                } else if (localObject.getTitle() != null && localObject.getTitle().equals(serverObject.getTitle())) {
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
        for (int i = 0; i < localSetListsCompareObjects.size(); i++) {
            OpenChordsCompareObject localObject = localSetListsCompareObjects.get(i);
            boolean found = false;
            for (int j = 0; j < serverSetListsCompareObjects.size(); j++) {
                OpenChordsCompareObject serverObject = serverSetListsCompareObjects.get(j);
                if ((localObject.getUuid() != null && serverObject.getUuid() != null && serverObject.getUuid().equalsIgnoreCase(localObject.getUuid())) ||
                        (localObject.getTitle() != null && localObject.getTitle().equals(serverObject.getTitle()))) {
                    found = true;
                    break;
                }
            }
            if (!found && localObject.getTitle() != null) {
                // Add this local object
                setListsNotOnServer.add(localObject);
            }
        }
    }

    private void findSongsNeedingUpdated() {
        // We have already logged the missing files, so now we deal with matches/updates
        for (int i = 0; i < serverSongsCompareObjects.size(); i++) {
            OpenChordsCompareObject serverObject = serverSongsCompareObjects.get(i);
            for (int j = 0; j < localSongsCompareObjects.size(); j++) {
                OpenChordsCompareObject localObject = localSongsCompareObjects.get(j);
                if ((localObject.getUuid() != null && localObject.getUuid().equalsIgnoreCase(serverObject.getUuid()))) {
                    // This is a match, now decide if it needs updated or not
                    boolean serverObjectHasLastModified = true;
                    if (serverObject.getLastModified() == null || serverObject.getLastModified().equals(c.getString(R.string.is_not_set))) {
                        serverObject.setLastModified(mainActivityInterface.getTimeTools().getNowIsoTime());
                        serverObjectHasLastModified = false;
                    }

                    // Ensure the last modified is in UTC
                    // Parse the input string
                    OffsetDateTime odtServer = OffsetDateTime.parse(serverObject.getLastModified());
                    OffsetDateTime utcTimeServer = odtServer.withOffsetSameInstant(ZoneOffset.UTC);
                    long serverObjectLastModified = Instant.parse(utcTimeServer.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)).toEpochMilli();

                    boolean localObjectHasLastModified = true;
                    if (localObject.getLastModified() == null || localObject.getLastModified().isEmpty() ||
                            localObject.getLastModified().equals(c.getString(R.string.is_not_set))) {
                        localObject.setLastModified(mainActivityInterface.getTimeTools().getNowIsoTime());
                        localObjectHasLastModified = false;
                    }
                    // Ensure the last modified is in UTC
                    // Parse the input string
                    OffsetDateTime odtLocal = OffsetDateTime.parse(serverObject.getLastModified());
                    OffsetDateTime utcTimeLocal = odtLocal.withOffsetSameInstant(ZoneOffset.UTC);
                    long localObjectLastModified = Instant.parse(utcTimeLocal.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)).toEpochMilli();

                    boolean useLocalLastModified = !serverObjectHasLastModified && localObjectHasLastModified;
                    boolean useServerLastModified = serverObjectHasLastModified && !localObjectHasLastModified;
                    boolean localNewer = localObjectLastModified > serverObjectLastModified;
                    boolean serverNewer = localObjectLastModified < serverObjectLastModified;

                    if (useLocalLastModified) {
                        // The server version doesn't have a last modified date, but the local does, we need to update the server
                        songsOnServerOlder.add(localObject);

                    } else if (useServerLastModified) {
                        // The local version doesn't have a last modified date, but the server does, we need to update the local
                        songsOnLocalOlder.add(serverObject);

                    } else {
                        if (localNewer) {
                            // The server object needs updated
                            songsOnServerOlder.add(localObject);

                        /*
                        Was trying to use this, but doesn't work as I don't force a pull
                        } else if (localObjectLastModified < serverObjectLastModified &&
                                (localObjectLastModified < lastDownloadSongChangesMillis ||
                                        lastDownloadSongChangesMillis==0)) {
                        */
                        } else if (serverNewer) {
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
        for (int i = 0; i < serverSetListsCompareObjects.size(); i++) {
            OpenChordsCompareObject serverObject = serverSetListsCompareObjects.get(i);
            for (int j = 0; j < localSetListsCompareObjects.size(); j++) {
                OpenChordsCompareObject localObject = localSetListsCompareObjects.get(j);
                if ((localObject.getUuid() != null && localObject.getUuid().equalsIgnoreCase(serverObject.getUuid())) ||
                        (localObject.getTitle() != null && localObject.getTitle().equals(serverObject.getTitle()))) {
                    // This is a match, now decide if it needs updated or not
                    boolean serverObjectHasLastModified = true;
                    if (serverObject.getLastModified() == null || serverObject.getLastModified().isEmpty()) {
                        serverObject.setLastModified(mainActivityInterface.getTimeTools().getNowIsoTime());
                        serverObjectHasLastModified = false;
                    }
                    OffsetDateTime odtServer = OffsetDateTime.parse(serverObject.getLastModified());
                    OffsetDateTime utcTimeServer = odtServer.withOffsetSameInstant(ZoneOffset.UTC);
                    long serverObjectLastModified = Instant.parse(utcTimeServer.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)).toEpochMilli();
                    //long serverObjectLastModified = Instant.parse(serverObject.getLastModified()).toEpochMilli();

                    boolean localObjectHasLastModified = true;
                    if (localObject.getLastModified() == null || localObject.getLastModified().isEmpty()) {
                        localObject.setLastModified(mainActivityInterface.getTimeTools().getNowIsoTime());
                        localObjectHasLastModified = false;
                    }
                    OffsetDateTime odtLocal = OffsetDateTime.parse(serverObject.getLastModified());
                    OffsetDateTime utcTimeLocal = odtLocal.withOffsetSameInstant(ZoneOffset.UTC);
                    long localObjectLastModified = Instant.parse(utcTimeLocal.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)).toEpochMilli();
                    //long localObjectLastModified = Instant.parse(localObject.getLastModified()).toEpochMilli();
                    if (serverObjectLastModified == 0) {
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
                getSetListsNotOnServerCount() + getSetListsOnServerOlderCount() +
                getSongsNotOnLocalCount() + getSetListsNotOnLocalCount();
    }

    public int getDownloadCount() {
        return getSongsNotOnLocalCount() + getSongsOnLocalOlderCount() +
                getSetListsNotOnLocalCount() + getSetListsOnLocalOlderCount() +
                getSongsNotOnServerCount() + getSetListsNotOnServerCount();
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
        for (int i = 0; i < compareObjects.size(); i++) {
            OpenChordsCompareObject compareObject = compareObjects.get(i);
            if (compareObject.getTitle() != null) {
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
            if (response.code() == 401) {
                // We need to get the auth token again
                Log.d(TAG,"We need to get the auth token again");
                if (openChordsFragment!=null) {
                    openChordsFragment.changeButtonsEnable(true);
                }
                getJwtToken();
                openChordsFragment.queryOpenChordsServer();

            } else {
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
                    folderIsDifferentUuid = false;

                    if (serverFolder != null) {

                        // Lets get the server objects we have found!
                        updateProgress(c.getString(R.string.sync_reading_remote_folder) + "\n");

                        //String content = MainActivity.gson.toJson(serverFolder, OpenChordsFolderObject.class);
                        //mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings","","ReceivedObject.json",content);

                        // Decide if we are the folder owner
                        if (serverFolder.getIsOwner()!=null) {
                            isOwner = serverFolder.getIsOwner();
                        } else {
                            isOwner = true;
                        }
                        // Decide if the folder is marked as read only
                        if (serverFolder.getReadonly()!=null) {
                            isReadOnly = serverFolder.getReadonly();
                        } else {
                            isReadOnly = false;
                        }

                        if (serverFolder.getTitle() != null) {
                            openChordsFolderName = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(serverFolder.getTitle());
                            serverTags = serverFolder.getTags();
                            serverSongs = serverFolder.getSongs();
                            if (serverSongs != null) {
                                removePointlessStuffFromSongs(serverSongs);
                            }
                            serverSetLists = serverFolder.getSetLists();
                            if (serverSetLists != null) {
                                removePointlessStuffFromSetLists(serverSetLists);
                            }

                            // Now create the server compare objects
                            createServerCompareObjects();
                        }
                    }

                } else {
                    serverFolder = null;
                    serverSongs.clear();
                    serverTags.clear();
                    serverSetLists.clear();
                    isServerResponse = false;
                }

                // Now compare the local and server objects
                updateProgress(c.getString(R.string.sync_comparing_local_and_remote) + "\n");

                boolean haveFolder = false;
                // Do we have a local folder with the same UUID
                String localFolderName = getOpenSongFolderNameFromUUID(openChordsFolderUuid);
                if (localFolderName == null) {
                    // We don't have a folder with this UUID.
                    // Let's check if we have a folder with the same name
                    for (int i = 0; i < openSongFolderRecordObjects.size(); i++) {
                        OpenSongFolderRecordObject openSongFolderRecordObject = openSongFolderRecordObjects.get(i);
                        if (openSongFolderRecordObject.getFolderName() != null && openSongFolderRecordObject.getFolderName().equals(openChordsFolderName)) {
                            // We have this folder, but the uuid is wrong!
                            // Change the UUID and then tell the user
                            openSongFolderRecordObject.setFolderUuid(openChordsFolderUuid);
                            openSongFolderRecordObject.setFolderOwnerUuid(openChordsFolderUuid);
                            saveOpenSongFolderObject();
                            folderIsDifferentUuid = true;
                            haveFolder = true;
                            break;
                        }
                    }
                } else {
                    haveFolder = true;
                }
                // Check we don't somehow have a local folder with the same uuid, but different name
                if (!haveFolder) {
                    for (int i = 0; i < openSongFolderRecordObjects.size(); i++) {
                        OpenSongFolderRecordObject openSongFolderRecordObject = openSongFolderRecordObjects.get(i);
                        if (openSongFolderRecordObject.getFolderUuid() != null &&
                                openSongFolderRecordObject.getFolderUuid().equalsIgnoreCase(openChordsFolderUuid)) {
                            // We have somehow managed to have no folder names that match,
                            // However, one of our folders (different name) has the same uuid as the OpenChords folder
                            // We need to change the id of the offending folder to a new random value
                            changeOpenSongFolderUUID(openSongFolderRecordObject.getFolderUuid(), String.valueOf(UUID.randomUUID()));
                            saveOpenSongFolderObject();
                        }
                    }
                }

                // Now compare the local and server objects
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
                    if (serverFolder != null) {
                        openChordsFolderName = serverFolder.getTitle();
                        openChordsFragment.updateFolderTitle(openChordsFolderName);
                    }
                    isServerResponse = false;
                    openChordsFragment.logChanges();
                }
            }
        });
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull Throwable throwable) {
        mainActivityInterface.getShowToast().doIt(c.getString(R.string.sync_server_noresponse_error));
        if (openChordsFragment != null) {
            openChordsFragment.updateFolderMessage();
        }
    }

    public boolean getIsServerResponse() {
        return isServerResponse;
    }

    public void setIsServerResponse(boolean isServerResponse) {
        this.isServerResponse = isServerResponse;
    }

    private void updateProgress(String message) {
        if (openChordsFragment != null) {
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
        song.setLyrics(song.getLyrics().replace("Pre-[C]","P"));
        song.setAutoscrolllength(getEmptyForZero(mainActivityInterface.getTimeTools().getTotalSecsFromColonTimes(openChordsSong.getDuration())));
        song.setTimesig(openChordsSong.getTimeSignature());
        String key = openChordsSong.getKey();
        Boolean keyIsMinor = openChordsSong.isKeyIsMinor();
        if (key != null && keyIsMinor != null && keyIsMinor) {
            key = key + "m";
        }
        song.setKey(key);
        if (openChordsSong.getTempo() != null) {
            song.setTempo(String.valueOf(openChordsSong.getTempo()));
        }
        /*if (openChordsSong.getCapo() != null) {
            song.setCapo(String.valueOf(openChordsSong.getCapo()));
        }*/
        song.setNotes(openChordsSong.getNotes());
        song.setCopyright(openChordsSong.getCopyright());
        song.setCcli(openChordsSong.getCcli());
        // Now get the tags
        song.setTheme(getTagsFromOpenChordsForOpenSong(openChordsSong));
        // Now get the presentation order
        if (openChordsSong.getStructure()!=null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i=0; i<openChordsSong.getStructure().size(); i++) {
                String sectionHeading = openChordsSong.getStructure().get(i).getSectionName();
                String instructionBefore = openChordsSong.getStructure().get(i).getInstructionBefore();
                String instructionAfter = openChordsSong.getStructure().get(i).getInstructionAfter();
                if (instructionAfter!=null && !instructionAfter.isEmpty()) {
                    song.setLyrics(song.getLyrics().replace("["+sectionHeading+"]\n","["+sectionHeading+"]\n"+instructionAfter+"\n"));
                }
                if (instructionBefore!=null && !instructionBefore.isEmpty()) {
                    song.setLyrics(song.getLyrics().replace("["+sectionHeading+"]\n","["+sectionHeading+"]\n"+instructionBefore+"\n"));
                }
                boolean numberedSectionCheck = true;
                if (sectionHeading.startsWith(c.getString(R.string.chorus))) {
                    sectionHeading = sectionHeading.replace(c.getString(R.string.chorus),"C");
                } else if (sectionHeading.startsWith(c.getString(R.string.verse))) {
                    sectionHeading = sectionHeading.replace(c.getString(R.string.verse),"V");
                } else if (sectionHeading.startsWith(c.getString(R.string.bridge))) {
                    sectionHeading = sectionHeading.replace(c.getString(R.string.bridge),"B");
                } else if (sectionHeading.startsWith(c.getString(R.string.tag))) {
                    sectionHeading = sectionHeading.replace(c.getString(R.string.tag),"T");
                } else if (sectionHeading.startsWith(c.getString(R.string.prechorus))) {
                    sectionHeading = sectionHeading.replace(c.getString(R.string.prechorus),"P");
                } else {
                    numberedSectionCheck = false;
                }
                if (numberedSectionCheck && !sectionHeading.replaceAll("\\D","").isEmpty()) {
                    sectionHeading = sectionHeading.replace(" ", "");
                }

                stringBuilder.append(sectionHeading).append(" ");
            }
            String presentationOrder = stringBuilder.toString().replace("  "," ");
            song.setPresentationorder(presentationOrder.trim());
        }
        return song;
    }

    private String getTagsFromOpenChordsForOpenSong(OpenChordsSong openChordsSong) {
        StringBuilder tagStringBuilder = new StringBuilder();
        String[] tags = openChordsSong.getTags();
        if (tags != null) {
            for (String tag : tags) {
                // Try to find this tag in the folder tags array
                if (serverTags!=null && !serverTags.isEmpty()) {
                    for (int j = 0; j < serverTags.size(); j++) {
                        OpenChordsTag openChordsTag = serverTags.get(j);
                        if (tag.equalsIgnoreCase(openChordsTag.getId())) {
                            tagStringBuilder.append(openChordsTag.getTitle()).append("\n");
                            break;
                        }
                    }
                }
            }
        }
        return tagStringBuilder.toString().trim().replace("\n", ";");
    }

    public String convertOpenChordsSetList(OpenChordsSetList serverSetList) {
        CurrentSet localSet = new CurrentSet(c);
        localSet.setUuid(serverSetList.getId());
        String setFilename = serverSetList.getTitle();
        if (setFilename == null) {
            setFilename = getOpenChordsFolderName() + c.getString(R.string.unknown);
        }
        if (!setFilename.startsWith(getOpenSongSetCategoryStart())) {
            setFilename = getOpenSongSetCategoryStart() + setFilename;
        }
        localSet.setSetCurrentLastName(setFilename);
        localSet.setNotes(jsonNullIfEmpty(serverSetList.getNotes()));
        localSet.setLastModified(serverSetList.getLastUpdated());

        if (serverSetList.getItems() != null) {
            ArrayList<OpenChordsSetListItem> setListItems = serverSetList.getItems();
            for (int i = 0; i < setListItems.size(); i++) {
                OpenChordsSetListItem serverSetListItem = setListItems.get(i);
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
                String prefKey = null;
                if (openChordsSetListSongItem!=null) {
                    prefKey = openChordsSetListSongItem.getTranspose();
                }

                if (itemType != null && itemType.equals("song")) {
                    for (int j = 0; j < serverSongs.size(); j++) {

                        OpenChordsSong openChordsSong = serverSongs.get(j);
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
                        for (int k = 0; k < localSongs.size(); k++) {
                            OpenChordsSong openChordsSong = localSongs.get(k);
                            if (openChordsSong.getTitle() != null &&
                                    openChordsSong.getId() != null &&
                                    openChordsSong.getId().equalsIgnoreCase(serverSetListItem.getId())) {
                                filename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(openChordsSong.getTitle());
                                key = openChordsSong.getKey();
                                Boolean isKeyMinor = openChordsSong.isKeyIsMinor();
                                if (key != null && isKeyMinor != null) {
                                    key = key + (isKeyMinor ? "m" : "");
                                }
                                if (key == null) {
                                    key = "";
                                }
                                found = true;
                            }
                            if (found) {
                                break;
                            }
                        }
                    }

                    if (prefKey!=null && !prefKey.isEmpty() && !prefKey.equals(key)) {
                        key = prefKey;
                    }

                    if (serverFolder!=null && serverFolder.getTitle()!=null && !serverFolder.getTitle().isEmpty() &&
                            (filename!=null || title!=null)) {
                        filename = filename == null ? title : filename;
                        title = title == null ? filename : title;
                        localSet.addItemToSet(serverFolder.getTitle(), filename, title, key, false);
                    }

                } else if (itemType != null && itemType.equals("divider")) {
                    localSet.addItemToSet("/", mainActivityInterface.getSetActions().getDividerIdentifier(),
                            mainActivityInterface.getSetActions().getDividerIdentifier(), null, false);

                } else if (itemType != null && itemType.equals("slide") && itemTitle != null) {
                    itemTitle = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(itemTitle);
                    Song tempSong = mainActivityInterface.getProcessSong().initialiseSong("**Slides", itemTitle);
                    tempSong.setLyrics(itemNotes);
                    tempSong.setUuid(itemId);
                    tempSong.setTitle(itemTitle);

                    // Save this temp song so we can recover the contents when we build the set file
                    //mainActivityInterface.getStorageAccess().saveThisSongFile(tempSong);
                    mainActivityInterface.getStorageAccess().writeSongFile(tempSong);
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
        if (openChordsSetName != null && openChordsSetName.startsWith(getOpenSongSetCategoryStart())) {
            return openChordsSetName;
        } else if (openChordsSetName != null) {
            return getOpenSongSetCategoryStart() +
                    mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(openChordsSetName);
        } else {
            return null;
        }
    }

    private String getEmptyForZero(int integer) {
        return integer == 0 ? "" : String.valueOf(integer);
    }


    // Convert OpenSong objects into OpenChords objects
    public OpenChordsSong convertOpenSongToOpenChords(Song openSongSong) {
        OpenChordsSong openChordsSong = new OpenChordsSong();

        openChordsSong.setId(openSongSong.getUuid());
        openChordsSong.setTitle(jsonNullIfEmpty(openSongSong.getFilename()));
        openChordsSong.setRawData(jsonNullIfEmpty(mainActivityInterface.getConvertJustChords().getJustChordsLyrics(openSongSong)));
        openChordsSong.setArtist(jsonNullIfEmpty(openSongSong.getAuthor()));
        openChordsSong.setDuration(jsonNullIfEmpty(openSongSong.getAutoscrolllength()));

        // Make sure tempo and timeSignature is valid!
        //mainActivityInterface.getDrumViewModel().prepareSongValues(openSongSong);
        String fixedTempoString = jsonNullIfEmpty(DrumCalculations.getFixedTempoString(openSongSong.getTempo(),false));
        if (fixedTempoString!=null && !fixedTempoString.isEmpty()) {
            openChordsSong.setTempo(Integer.parseInt(fixedTempoString));
        } else {
            openChordsSong.setTempo(null);
        }
        /*String tempo = openSongSong.getTempo();
        if (tempo != null) {
            tempo = tempo.replaceAll("\\D", "").trim();
            if (!tempo.isEmpty()) {
                openChordsSong.setTempo(Integer.parseInt(tempo));
            }
        }*/
        openChordsSong.setTimeSignature(jsonNullIfEmpty(
                DrumCalculations.getFixedTimeSignatureString(openSongSong.getTimesig(),false)));
        String key = openSongSong.getKey();
        if (key != null && !key.isEmpty()) {
            openChordsSong.setKey(key.replace("m", ""));
            openChordsSong.setKeyIsMinor(key.endsWith("m"));
        }
        /*String capo = openSongSong.getCapo();
        if (capo != null) {
            capo = capo.replaceAll("\\D", "").trim();
            if (!capo.isEmpty()) {
                openChordsSong.setCapo(Integer.parseInt(capo));
            }
        }*/
        // TODO
        //openChordsSong.setTranspose(jsonNullIfEmpty(key));
        openChordsSong.setNotes(jsonNullIfEmpty(openSongSong.getNotes()));
        openChordsSong.setCopyright(jsonNullIfEmpty(openSongSong.getCopyright()));
        openChordsSong.setCcli(jsonNullIfEmpty(openSongSong.getCcli()));
        openChordsSong.setLastUpdated(jsonNullIfEmpty(openSongSong.getLastModified()));
        // To add tags, we need to cycle through our tags
        // Look for the tag id already saved in the server
        // If they are found, get their uuid, if not, create a new one
        if (openSongSong.getTheme() != null) {
            StringBuilder newTags = new StringBuilder();
            String[] localTags = openSongSong.getTheme().split(";");
            for (String localTag : localTags) {
                boolean found = false;
                if (serverTags!=null && !serverTags.isEmpty()) {
                    for (int j = 0; j < serverTags.size(); j++) {
                        OpenChordsTag serverTag = serverTags.get(j);
                        if (serverTag.getTitle() != null && serverTag.getTitle().equals(localTag)) {
                            newTags.append(serverTag.getId());
                            found = true;
                        }
                        if (found) {
                            break;
                        }
                    }
                }
                if (!found && !localTag.trim().isEmpty()) {
                    String newUUID = String.valueOf(UUID.randomUUID());
                    newTags.append(newUUID).append("\n");
                    OpenChordsTag newTag = new OpenChordsTag();
                    newTag.setId(newUUID);
                    newTag.setTitle(localTag.trim());
                    newTag.setColor(mainActivityInterface.getMyThemeColors().getHexFromIntNoAlpha(mainActivityInterface.getPalette().hintColor));
                    newTagsForUpload.add(newTag);
                }
            }
            if (!newTags.toString().trim().isEmpty()) {
                String[] tags = newTags.toString().split("\n");
                openChordsSong.setTags(tags);
            }
        }
        // Now get the presentation order if it exists
        if (openSongSong.getPresentationorder()!=null &&
                !openSongSong.getPresentationorder().isEmpty()) {
            openChordsSong.setStructure(getOpenChordsStructure(openSongSong));
        }
        return openChordsSong;
    }

    private ArrayList<OpenChordsSongStructureItem> getOpenChordsStructure(Song openSongSong) {
        // OpenSong desktop looked like this: V1 V2 C B C V3
        // OpenSongApp pre v6.4.1 presentation order looked like this: Verse 1;V2;C;Break;

        ArrayList<OpenChordsSongStructureItem> structureItems = new ArrayList<>();
        // Try to extract OpenSong presentation order
        // Get a note of the section headings in the song
        openSongSong.setSongSectionHeadings(new ArrayList<>());
        openSongSong.setGroupedSections(new ArrayList<>());
        String[] lines = openSongSong.getLyrics().split("\n");
        for (String line:lines) {
            if (line.startsWith("[")) {
                openSongSong.getSongSectionHeadings().add(line.replace("[", "").replace("]", ""));
                openSongSong.getGroupedSections().add(line);
            }
        }
        mainActivityInterface.getProcessSong().matchPresentationOrder(openSongSong,false);
        if (openSongSong.getPresoOrderSongHeadings()!=null &&
            !openSongSong.getPresoOrderSongHeadings().isEmpty()) {
            for (String heading : openSongSong.getPresoOrderSongHeadings()) {
                OpenChordsSongStructureItem structureItem = new OpenChordsSongStructureItem();
                structureItem.setSectionName(heading);
                structureItems.add(structureItem);
            }
        }
        if (structureItems.isEmpty()) {
            return null;
        } else {
            return structureItems;
        }
    }

    public OpenChordsSetList convertOpenSongSetToOpenChordsSetList(String filename) {
        // This is a newer method that parsers the set into a setObject first
        if (filename!=null) {
            // The set should start with OpenChords__, but we need to check there aren't 2 of them!
            if (filename.startsWith(getOpenSongSetCategoryStart())) {
                filename = filename.replaceFirst(getOpenSongSetCategoryStart(), "");
                filename = getOpenSongSetCategoryStart() + filename;
            }
            SetObject setObject = mainActivityInterface.getSetActions().createSetObjectFromFilename(filename);

            if (setObject != null) {
                // Hopefully the setObject isn't empty and we can proceed
                OpenChordsSetList openChordsSetList = new OpenChordsSetList();
                // Put the @Nullable values into the openChordsSetList
                if (setObject.getUuid() == null) {
                    openChordsSetList.setId(String.valueOf(UUID.randomUUID()));
                } else {
                    openChordsSetList.setId(setObject.getUuid());
                }
                openChordsSetList.setLastUpdated(setObject.getLastModified());
                openChordsSetList.setNotes(jsonNullIfEmpty(setObject.getNotes()));
                String title = jsonNullIfEmpty(setObject.getSetName());
                if (title!=null) {
                    // Don't include the OpenChords category in the title here
                    title = title.replace(getOpenSongSetCategoryStart(),"");
                }
                openChordsSetList.setTitle(title);
                // Now we need to go through the set items and add them
                ArrayList<OpenChordsSetListItem> openChordsSetListItems = null;
                if (setObject.getSlideGroups() != null) {
                    ArrayList<SetSlideGroupObject> setSlideGroupObjects = setObject.getSlideGroups();
                    for (int i = 0; i < setSlideGroupObjects.size(); i++) {
                        SetSlideGroupObject slideGroupObject = setSlideGroupObjects.get(i);
                        OpenChordsSetListItem openChordsSetListItem;
                        if (slideGroupObject.getType() != null) {
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

                return openChordsSetList;

            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private String convertOpenSongSetNameToOpenChordsSetName(String openSongSetName) {
        return mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(openSongSetName).
                replace(getOpenSongSetCategoryStart(), "");
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
        for (int i = 0; i < serverSongs.size(); i++) {
            OpenChordsSong serverSong = serverSongs.get(i);
            for (int j = 0; j < songsNotOnLocal.size(); j++) {
                OpenChordsCompareObject compareObject = songsNotOnLocal.get(j);
                if (compareObject.getUuid() != null && compareObject.getTitle() != null &&
                        compareObject.getUuid().equalsIgnoreCase(serverSong.getId())) {
                    // This is a song we want
                    String title = compareObject.getTitle();
                    String filename = title;
                    if (title!=null) {
                        title = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(title);
                        filename = mainActivityInterface.getStorageAccess().safeFilename(mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(title)).replace("/","-");
                    }
                    updateProgress(c.getString(R.string.sync_creating_new_item) + " (" + c.getString(R.string.song) + ")\n" + title);
                    Uri songUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",
                            getLocalFolderName(), filename);

                    // If we already have created a song with this filename, this is because JustChords allows multiple songs to have the same title
                    // We can't do this as they will have identical titles.  We rename them by appending a number to the end
                    int num = 1;
                    String tempfilename = filename;
                    String temptitle = title;
                    while (mainActivityInterface.getStorageAccess().uriExists(songUri)) {
                        // Ok, this already exists, so try again
                        tempfilename = filename + " (" + num + ")";
                        temptitle = title + " (" + num + ")";
                        num ++;
                        songUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",
                                getLocalFolderName(), tempfilename);
                    }
                    // If we had to change the filename above, get the new one
                    String lastModified = compareObject.getLastModified();
                    boolean resetLastModified = false;
                    if (!filename.equals(tempfilename)) {
                        filename = tempfilename;
                        title = temptitle;
                        resetLastModified = true;
                        lastModified = mainActivityInterface.getTimeTools().getNowIsoTime();
                    }

                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, songUri, null,
                            "Songs", getLocalFolderName(), filename);
                    Song newOpenSongSong = convertOpenChordsToOpenSong(filename, title, lastModified, serverSong);

                    // Save the song
                    mainActivityInterface.getSQLiteHelper().createSong(getLocalFolderName(), filename);
                    mainActivityInterface.getSaveSong().setResetLastModified(resetLastModified);
                    if (resetLastModified) {
                        newOpenSongSong.setLastModified(mainActivityInterface.getTimeTools().getNowIsoTime());
                    }
                    mainActivityInterface.getSaveSong().updateSong(newOpenSongSong, false);
                    mainActivityInterface.getSaveSong().setResetLastModified(true);

                    // Remove this item from the compareObjects since we have dealt with it
                    songsNotOnLocal.remove(songsNotOnLocal.get(j));

                    String nowTime = mainActivityInterface.getTimeTools().getNowIsoTime();
                    for (int k = 0; k < localSetLists.size(); k++) {
                        OpenChordsSetList set = localSetLists.get(k);
                        addNewConflictItemObject(c.getString(R.string.sync_song_downloaded), set.getTitle(), nowTime);
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
        for (int i = 0; i < serverSongs.size(); i++) {
            OpenChordsSong serverSong = serverSongs.get(i);
            for (int j = 0; j < songsOnLocalOlder.size(); j++) {
                OpenChordsCompareObject compareObject = songsOnLocalOlder.get(j);
                if (compareObject.getUuid() != null && serverSong.getTitle() != null && compareObject.getTitle() != null &&
                        compareObject.getUuid().equalsIgnoreCase(serverSong.getId())) {
                    // This is a song we want
                    String title = serverSong.getTitle();
                    String filename = mainActivityInterface.getStorageAccess().safeFilename(mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(title)).replace("/","-");
                    updateProgress(c.getString(R.string.sync_updating_item) + " (" + c.getString(R.string.song) + ")\n" + title);

                    // Get the existing song so we only update the info held by OpenChords
                    Song existingSong = mainActivityInterface.getSQLiteHelper().getSongByUuid(serverSong.getId());
                    Uri songUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",
                            getLocalFolderName(), existingSong.getFilename());
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, songUri, null,
                            "Songs", getLocalFolderName(), existingSong.getFilename());
                    Song newOpenSongSong = convertOpenChordsToOpenSong(filename, title, serverSong.getLastUpdated(), serverSong);

                    // If we have changed the title/filename, we need to update the database
                    String oldtitle = existingSong.getTitle();
                    String oldfilename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(existingSong.getFilename());
                    if (!oldfilename.equals(filename)) {
                        mainActivityInterface.getSQLiteHelper().deleteSong(getLocalFolderName(), mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(existingSong.getFilename()));
                        mainActivityInterface.getSQLiteHelper().createSong(getLocalFolderName(), filename);
                    }

                    // Update the existing song with the info received (not all OpenSong stuff is in OpenChords!)
                    updateExistingOpenSongWithOpenChords(existingSong, newOpenSongSong);

                    // Save the song
                    mainActivityInterface.getSaveSong().setResetLastModified(false);
                    mainActivityInterface.getSaveSong().updateSong(existingSong, false);
                    mainActivityInterface.getSaveSong().setResetLastModified(true);

                    // Remove this item from the compareObjects since we have dealt with it
                    songsOnLocalOlder.remove(songsOnLocalOlder.get(j));

                    addNewConflictItemObject(c.getString(R.string.sync_song_update_downloaded), compareObject.getTitle(), nowTime);

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
        for (int i = 0; i < serverSetLists.size(); i++) {
            OpenChordsSetList serverSetList = serverSetLists.get(i);
            for (int j = 0; j < setListsNotOnLocal.size(); j++) {
                OpenChordsCompareObject compareObject = setListsNotOnLocal.get(j);
                if (compareObject.getUuid() != null && compareObject.getTitle() != null &&
                        compareObject.getUuid().equalsIgnoreCase(serverSetList.getId())) {
                    // This is a setList we want
                    String title = compareObject.getTitle();
                    String filename = convertOpenChordsSetNameToOpenSongSetName(title);
                    updateProgress(c.getString(R.string.sync_creating_new_item) + " (" + c.getString(R.string.set) + ")\n" + title);

                    String setXML = convertOpenChordsSetList(serverSetList);

                    // Save the set
                    mainActivityInterface.getStorageAccess().writeFileFromString("Sets", "", filename, setXML);

                    // Remove this item from the compareObjects since we have dealt with it
                    setListsNotOnLocal.remove(setListsNotOnLocal.get(j));

                    addNewConflictItemObject(c.getString(R.string.sync_set_downloaded), compareObject.getTitle(), nowTime);

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
        for (int i = 0; i < serverSetLists.size(); i++) {
            OpenChordsSetList serverSetList = serverSetLists.get(i);
            for (int j = 0; j < setListsOnLocalOlder.size(); j++) {
                OpenChordsCompareObject compareObject = setListsOnLocalOlder.get(j);
                if (compareObject.getUuid() != null && serverSetList.getTitle() != null &&
                        compareObject.getUuid().equalsIgnoreCase(serverSetList.getId())) {
                    // This is a setList we want
                    String title = serverSetList.getTitle();
                    String filename = convertOpenChordsSetNameToOpenSongSetName(title);

                    updateProgress(c.getString(R.string.sync_updating_item) + " (" + c.getString(R.string.set) + ")\n" + title);

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
                    mainActivityInterface.getStorageAccess().writeFileFromString("Sets", "", filename, setXML);

                    // Remove this item from the compareObjects since we have dealt with it
                    setListsOnLocalOlder.remove(setListsOnLocalOlder.get(j));

                    addNewConflictItemObject(c.getString(R.string.sync_set_update_downloaded), compareObject.getTitle(), nowTime);

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
        existingSong.setTempo(DrumCalculations.getFixedTempoString(newOpenSongSong.getTempo(),false));
        /*if (mainActivityInterface.getDrumViewModel().getBpm()>-1) {
            existingSong.setTempo(String.valueOf(mainActivityInterface.getDrumViewModel().getBpm()));
        } else {
            existingSong.setTempo("");
        }*/
        //existingSong.setTempo(newOpenSongSong.getTempo());
        existingSong.setTimesig(DrumCalculations.getFixedTimeSignatureString(newOpenSongSong.getTimesig(),false));
        existingSong.setKey(newOpenSongSong.getKey());
        //existingSong.setCapo(newOpenSongSong.getCapo());
        existingSong.setNotes(newOpenSongSong.getNotes());
        existingSong.setCcli(newOpenSongSong.getCcli());
        existingSong.setLastModified(newOpenSongSong.getLastModified());
        existingSong.setTheme(newOpenSongSong.getTheme());
        existingSong.setPresentationorder(newOpenSongSong.getPresentationorder());
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
        for (int i = 0; i < songsInFolder.size(); i++) {
            Song song = songsInFolder.get(i);
            mainActivityInterface.getSQLiteHelper().deleteSong(getLocalFolderName(), song.getFilename());
        }
        songsInFolder.clear();

        // Now remove the song files in the local folder
        mainActivityInterface.getStorageAccess().wipeFolder("Songs", getLocalFolderName());

        // Delete OpenChords__ set files
        ArrayList<String> setFiles = mainActivityInterface.getStorageAccess().listFilesInFolder("Sets", "");
        for (int i = 0; i < setFiles.size(); i++) {
            String setFile = setFiles.get(i);
            if (setFile.startsWith(getOpenSongSetCategoryStart())) {
                mainActivityInterface.getStorageAccess().doDeleteFile("Sets", "", setFile);
            }
        }

        // Now to download the stuff from the server
        // Go through the serverSongs and download them all
        for (int i = 0; i < serverSongs.size(); i++) {
            OpenChordsSong serverSong = serverSongs.get(i);
            if (serverSong.getTitle() != null) {
                String title = serverSong.getTitle();
                updateProgress(c.getString(R.string.sync_creating_new_item) + " (" + c.getString(R.string.song) + ")\n" + title);
                String filename = mainActivityInterface.getStorageAccess().safeFilename(mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(title)).replace("/","-");
                Song newOpenSongSong = convertOpenChordsToOpenSong(filename, title, serverSong.getLastUpdated(), serverSong);
                // Save the song
                mainActivityInterface.getSQLiteHelper().createSong(getLocalFolderName(), filename);
                mainActivityInterface.getSaveSong().setResetLastModified(false);
                mainActivityInterface.getSaveSong().updateSong(newOpenSongSong, false);
                mainActivityInterface.getSaveSong().setResetLastModified(true);

                addNewConflictItemObject(c.getString(R.string.sync_song_force_downloaded), title, nowTime);
            }
        }

        // Go through the serverSongs and download them all
        for (int i = 0; i < serverSetLists.size(); i++) {
            OpenChordsSetList serverSetList = serverSetLists.get(i);
            if (serverSetList.getTitle() != null) {
                String title = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(mainActivityInterface.getStorageAccess().safeFilename(serverSetList.getTitle()));
                String filename = convertOpenChordsSetNameToOpenSongSetName(title);
                updateProgress(c.getString(R.string.sync_creating_new_item) + " (" + c.getString(R.string.set_list) + ")\n" + title);

                // Get the xml for the setlist
                String xml = convertOpenChordsSetList(serverSetList);
                mainActivityInterface.getStorageAccess().writeFileFromString("Sets", "", filename, xml);
                addNewConflictItemObject(c.getString(R.string.sync_set_force_downloaded), title, nowTime);
            }
        }

        updateConflictItem("lastForcePull");
        updateConflictFile();

        // Now update the song menu
        updateTheSongMenu();
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
        //updateLocalSongsUuid();

        // If we need to update the uuid of local sets, do it
        updateLocalSetsUuid();

        // Deal with the songs
        updateProgress(c.getString(R.string.sync_reading_local_folder) + "\n");
        if (newSongs) {
            prepareUploadServerSongsAndNewLocal();
        } else if (updateSongs) {
            prepareUploadServerSongsAndUpdates();
        } else {
            songsForUpload = serverSongs;
        }

        // Because JustChords allows non-safe filename characters in song titles that we can't cope with
        // We need to convert these back
        if (songsForUpload!=null) {
            for (int i = 0; i < songsForUpload.size(); i++) {
                songsForUpload.get(i).setTitle(mainActivityInterface.getStorageAccess().safeFilename(songsForUpload.get(i).getTitle()).replace("¦¦","|"));
            }
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
        if (setsForUpload != null) {
            for (int i = 0; i < setsForUpload.size(); i++) {
                OpenChordsSetList setForUpload = setsForUpload.get(i);
                if (setForUpload.getTitle() != null &&
                        setForUpload.getTitle().startsWith(getOpenSongSetCategoryStart())) {
                    setForUpload.setTitle(setForUpload.getTitle().replace(getOpenSongSetCategoryStart(), "").replace("¦¦","|"));
                }
            }
        }

        // Deal with the tags
        prepareUploadServerTagsAndNewLocal();

        // Now set all item records to the same time
        String nowTime = mainActivityInterface.getTimeTools().getNowIsoTime();
        for (int i = 0; i < conflictItemRecords.size(); i++) {
            OpenChordsConflictItemObject conflictItemRecord = conflictItemRecords.get(i);
            conflictItemRecord.setDate(nowTime);
        }

        uploadFolderObject.setSongs(songsForUpload);
        uploadFolderObject.setSetLists(setsForUpload);
        uploadFolderObject.setTags(tagsForUpload);

        //String json = gson.toJson(uploadFolderObject);
        // Replace unneccessary items
        //json = removeUnnecessaryBitsFromJson(json);
        //mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings", "", "uploadFolderObject2.json", json);

        updateProgress(c.getString(R.string.sync_uploading_changes) + "\n");

        Call<OpenChordsFolderObject> call = retrofitInterface.postOpenChordsFolder(uploadFolderObject.getOwnerId(), uploadFolderObject);
        call.enqueue(new Callback<OpenChordsFolderObject>() {
            @Override
            public void onResponse(@NonNull Call<OpenChordsFolderObject> call, @NonNull Response<OpenChordsFolderObject> response) {
                // this method is called when we get response from our api.
                if (openChordsFragment != null) {
                    openChordsFragment.changeButtonsEnable(false);
                    updateProgress(c.getString(R.string.wait) + "\n");
                    mainActivityInterface.getMainHandler().postDelayed(() -> {
                        if (openChordsFragment != null) {
                            openChordsFragment.queryOpenChordsServer();
                        }
                    }, 1000);
                }
            }

            @Override
            public void onFailure(@NonNull Call<OpenChordsFolderObject> call, @NonNull Throwable t) {
                if (openChordsFragment != null) {
                    openChordsFragment.changeButtonsEnable(false);
                    updateProgress(c.getString(R.string.wait) + "\n");
                    mainActivityInterface.getMainHandler().postDelayed(() -> {
                        if (openChordsFragment != null) {
                            openChordsFragment.queryOpenChordsServer();
                        }
                    }, 1000);
                }
            }
        });
        updateConflictFile();
    }

    private void prepareUploadServerSongsAndNewLocal() {
        // Combine the current server songs with the new local songs for uploading
        if (serverSongs != null) {
            songsForUpload = new ArrayList<>(serverSongs);
        } else {
            songsForUpload = new ArrayList<>();
        }
        for (int i = 0; i < songsNotOnServer.size(); i++) {
            OpenChordsCompareObject compareObject = songsNotOnServer.get(i);
            updateProgress(c.getString(R.string.sync_preparing_item) + " (" + c.getString(R.string.song) + ")\n" + compareObject.getTitle());
            Song song = mainActivityInterface.getSQLiteHelper().getOpenChordsSong(getLocalFolderName(), compareObject.getUuid());
            OpenChordsSong newSong = convertOpenSongToOpenChords(song);
            songsForUpload.add(newSong);
            addNewConflictItemObject(c.getString(R.string.sync_song_uploaded), newSong.getTitle(), null);
        }
        if (songsNotOnServerCount > 0) {
            updateConflictItem("lastUploadNewSongs");
        }
    }

    private void prepareUploadServerSongsAndUpdates() {
        // Combine the current server songs with the newer local songs for uploading
        songsForUpload = new ArrayList<>();
        for (int i = 0; i < serverSongs.size(); i++) {
            OpenChordsSong serverSong = serverSongs.get(i);
            boolean found = false;
            for (int j = 0; j < songsOnServerOlder.size(); j++) {
                OpenChordsCompareObject compareObject = songsOnServerOlder.get(j);
                if (serverSong.getId() != null &&
                        serverSong.getId().equalsIgnoreCase(compareObject.getUuid())) {
                    updateProgress(c.getString(R.string.sync_preparing_item) + " (" + c.getString(R.string.song) + ")\n" + compareObject.getTitle());
                    Song song = mainActivityInterface.getSQLiteHelper().getSpecificSong(getLocalFolderName(), compareObject.getTitle());
                    OpenChordsSong newSong = convertOpenSongToOpenChords(song);
                    songsForUpload.add(newSong);
                    addNewConflictItemObject(c.getString(R.string.sync_song_update_uploaded), newSong.getTitle(), null);
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
        if (songsOnServerOlderCount > 0) {
            updateConflictItem("lastUploadSongChanges");
        }
    }

    private void prepareUploadServerSetsAndNewLocal() {
        // Combine the current server sets with the new local sets for uploading
        if (serverSetLists != null) {
            setsForUpload = new ArrayList<>(serverSetLists);
        } else {
            setsForUpload = new ArrayList<>();
        }
        for (int i = 0; i < setListsNotOnServer.size(); i++) {
            OpenChordsCompareObject compareObject = setListsNotOnServer.get(i);
            updateProgress(c.getString(R.string.sync_preparing_item) + " (" + c.getString(R.string.set_list) + ")\n" + compareObject.getTitle());
            OpenChordsSetList openChordsSetList = convertOpenSongSetToOpenChordsSetList(compareObject.getTitle());
            setsForUpload.add(openChordsSetList);
            addNewConflictItemObject(c.getString(R.string.sync_set_uploaded), openChordsSetList.getTitle(), null);
        }
        if (setListsNotOnServerCount > 0) {
            updateConflictItem("lastUploadNewSets");
        }
    }

    private void prepareUploadServerSetsAndUpdates() {
        // Combine the current server sets with the new local sets for uploading
        setsForUpload = new ArrayList<>();
        for (int i = 0; i < serverSetLists.size(); i++) {
            OpenChordsSetList serverSetList = serverSetLists.get(i);
            boolean found = false;
            for (int j = 0; j < setListsOnServerOlder.size(); j++) {
                OpenChordsCompareObject compareObject = setListsOnServerOlder.get(j);
                if (serverSetList.getId() != null && serverSetList.getId().equalsIgnoreCase(compareObject.getUuid())) {
                    updateProgress(c.getString(R.string.sync_preparing_item) + " (" + c.getString(R.string.set) + ")\n" + compareObject.getTitle());
                    OpenChordsSetList openChordsSetList = convertOpenSongSetToOpenChordsSetList(getOpenSongSetCategoryStart() + compareObject.getTitle());
                    setsForUpload.add(openChordsSetList);
                    addNewConflictItemObject(c.getString(R.string.sync_set_update_uploaded), openChordsSetList.getTitle(), null);
                    found = true;
                }
                if (found) {
                    break;
                }
            }
            if (!found) {
                setsForUpload.add(serverSetList);
            }
            if (setListsOnServerOlderCount > 0) {
                updateConflictItem("lastUploadSetChanges");
            }
        }
    }

    private void prepareUploadServerTagsAndNewLocal() {
        // Combine the current server tags with the new local song tags for uploading
        if (serverTags != null) {
            tagsForUpload = new ArrayList<>(serverTags);
        } else {
            tagsForUpload = new ArrayList<>();
        }
        tagsForUpload.addAll(newTagsForUpload);
    }

    private String jsonNullIfEmpty(String string) {
        return (string == null || string.trim().isEmpty()) ? null : string;
    }

    private void removePointlessStuffFromSongs(ArrayList<OpenChordsSong> songobjects) {
        // This removes stuff that isn't set or required from the local or server songs
        // Empty values are set to nulls so the aren't uploaded to the json files
        for (int i = 0; i < songobjects.size(); i++) {
            OpenChordsSong songobject = songobjects.get(i);
            if (songobject!=null) {
                songobject.setTitle(trimmedOrNull(songobject.getTitle()));
                songobject.setLastUpdated(trimmedOrNull(songobject.getLastUpdated()));
                //songobject.setCapo(nullFromZero(songobject.getCapo()));
                songobject.setArtist(trimmedOrNull(songobject.getArtist()));
                songobject.setCcli(trimmedOrNull(songobject.getCcli()));
                songobject.setCopyright(trimmedOrNull(songobject.getCopyright()));
                songobject.setTempo(nullFromZero(songobject.getTempo()));
                songobject.setDuration(trimmedOrNull(songobject.getDuration()));
                songobject.setKey(trimmedOrNull(songobject.getKey()));
                if (songobject.getKey() == null) {
                    songobject.setKeyIsMinor(null);
                }
                songobject.setLastUpdated(trimmedOrNull(songobject.getLastUpdated()));
                songobject.setNotes(trimmedOrNull(songobject.getNotes()));
                songobject.setRawData(trimmedOrNull(songobject.getRawData()));
                songobject.setTimeSignature(trimmedOrNull(songobject.getTimeSignature()));
                songobject.setTranspose(trimmedOrNull(songobject.getTranspose()));
            }
        }
    }

    private void removePointlessStuffFromSetLists(ArrayList<OpenChordsSetList> setLists) {
        for (int i = 0; i < setLists.size(); i++) {
            OpenChordsSetList setList = setLists.get(i);
            setList.setTitle(trimmedOrNull(setList.getTitle()));
            setList.setNotes(trimmedOrNull(setList.getNotes()));
            setList.setLastUpdated(trimmedOrNull(setList.getLastUpdated()));
            if (setList.getItems() != null) {
                ArrayList<OpenChordsSetListItem> setListItems = setList.getItems();
                for (int j = 0; j < setListItems.size(); j++) {
                    OpenChordsSetListItem setListItem = setListItems.get(j);
                    setListItem.setTitle(trimmedOrNull(setListItem.getTitle()));
                    setListItem.setLastUpdated(trimmedOrNull(setListItem.getLastUpdated()));
                    setListItem.setNotes(trimmedOrNull(setListItem.getNotes()));
                    setListItem.setType(trimmedOrNull(setListItem.getType()));
                    setListItem.setCustomData(trimmedOrNull(setListItem.getCustomData()));
                    if (setListItem.getSongItem() != null) {
                        OpenChordsSetListSongItem songItem = setListItem.getSongItem();
                        //songItem.setCapo(nullFromZero(songItem.getCapo()));
                        //songItem.setTranspose(trimmedOrNull(songItem.getTranspose()));
                    }
                }
            }
        }
    }


    public void deleteLocalSongs() {
        // Do this in a new thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // This gets the songs that are in the local folder that aren't in the remote one
            // These get moved to the OpenSong/Backups/ folder
            checkForConflictObject();
            String nowTime = mainActivityInterface.getTimeTools().getNowIsoTime();
            for (int i = 0; i < songsNotOnServer.size(); i++) {
                OpenChordsCompareObject openChordsCompareObject = songsNotOnServer.get(i);
                updateProgress(c.getString(R.string.sync_local_song_deleted) + ":\n" + openChordsCompareObject.getTitle());

                // Get the oldUri and move to the newUri (append the .ost file extension)
                Uri uriOld = mainActivityInterface.getStorageAccess().getUriForItem("Songs",
                        mainActivityInterface.getOpenChordsAPI().getLocalFolderName(), openChordsCompareObject.getTitle());
                Uri uriNew = mainActivityInterface.getStorageAccess().getUriForItem("Backups", "",
                        openChordsCompareObject.getTitle() + ".ost");
                mainActivityInterface.getStorageAccess().renameFileFromUri(uriOld, uriNew, "Backups",
                        "", openChordsCompareObject.getTitle() + ".ost");
                addNewConflictItemObject(c.getString(R.string.sync_local_song_deleted), openChordsCompareObject.getTitle(), nowTime);
                // Remove from the database
                mainActivityInterface.getSQLiteHelper().deleteSong(getLocalFolderName(), openChordsCompareObject.getTitle());
                // Update the song menu
                updateTheSongMenu();
            }
            updateConflictFile();
            if (openChordsFragment != null) {
                openChordsFragment.queryOpenChordsServer();
            }
        });
    }

    public void deleteRemoteSongs() {
        // Do this in a new thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            checkForConflictObject();
            songsForUpload = new ArrayList<>();
            updateProgress(c.getString(R.string.sync_delete_remote_not_in_local) + "\n" + c.getString(R.string.songs));

            ArrayList<OpenChordsSong> songsToBackup = new ArrayList<>();

            if (localSongs.isEmpty()) {
                songsToBackup = serverSongs;
            } else {
                for (int i = 0; i < localSongs.size(); i++) {
                    OpenChordsSong localSong = localSongs.get(i);
                    for (int j = 0; j < serverSongs.size(); j++) {
                        OpenChordsSong serverSong = serverSongs.get(j);
                        // If this song exists on the local, we keep it
                        if ((localSong.getId() != null && localSong.getId().equals(serverSong.getId())) ||
                                (localSong.getTitle() != null && localSong.getTitle().equals(serverSong.getTitle()))) {
                            songsForUpload.add(serverSong);
                        } else {
                            songsToBackup.add(serverSong);
                        }
                    }
                }
            }

            // First backup the songs
            String nowTime = mainActivityInterface.getTimeTools().getNowIsoTime();
            for (int k = 0; k < songsToBackup.size(); k++) {
                OpenChordsSong songToBackup = songsToBackup.get(k);
                String title = songToBackup.getTitle();
                Song backup = convertOpenChordsToOpenSong(title, title, nowTime, songToBackup);
                backup.setFolder("Backups");
                String xml = mainActivityInterface.getProcessSong().getXML(backup);
                if (!mainActivityInterface.getStorageAccess().writeFileFromString("Backups", "", title + ".ost", xml)) {
                    // There was an error, so don't delete!
                    songsForUpload.add(songToBackup);
                }
            }

            OpenChordsFolderObject uploadFolderObject = new OpenChordsFolderObject();
            uploadFolderObject.setTitle(getLocalFolderName());
            uploadFolderObject.setOwnerId(openChordsFolderUuid);
            uploadFolderObject.setSongs(songsForUpload);
            uploadFolderObject.setSetLists(serverSetLists);
            uploadFolderObject.setTags(serverTags);

            //String json = gson.toJson(uploadFolderObject);
            // Replace unneccessary items
            //json = removeUnnecessaryBitsFromJson(json);
            //mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings", "", "uploadFolderObject2.json", json);

            updateProgress(c.getString(R.string.sync_uploading_changes) + "\n");
            doQueryCall(uploadFolderObject);
            updateConflictFile();

            if (openChordsFragment != null) {
                openChordsFragment.queryOpenChordsServer();
            }
        });
    }

    public void deleteLocalSets() {
        // Do this in a new thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            checkForConflictObject();
            // This gets the sets that are in the local folder that aren't in the remote one
            // These get moved to the OpenSong/Backups/ folder
            String nowTime = mainActivityInterface.getTimeTools().getNowIsoTime();
            for (int i = 0; i < setListsNotOnServer.size(); i++) {
                OpenChordsCompareObject openChordsCompareObject = setListsNotOnServer.get(i);
                String title = openChordsCompareObject.getTitle();
                updateProgress(c.getString(R.string.sync_local_set_deleted) + ":\n" + title);

                // Get the oldUri and move to the newUri (append the .osts file extension)
                if (title != null && !title.startsWith(getOpenSongSetCategoryStart())) {
                    title = getOpenSongSetCategoryStart() + title;
                }
                Uri uriOld = mainActivityInterface.getStorageAccess().getUriForItem("Sets",
                        "", title);
                Uri uriNew = mainActivityInterface.getStorageAccess().getUriForItem("Backups", "",
                        title + ".osts");
                mainActivityInterface.getStorageAccess().renameFileFromUri(uriOld, uriNew, "Backups",
                        "", title + ".osts");
                addNewConflictItemObject(c.getString(R.string.sync_local_set_deleted), title, nowTime);
            }
            updateConflictFile();
            if (openChordsFragment != null) {
                openChordsFragment.queryOpenChordsServer();
            }
        });
    }

    public void deleteRemoteSets() {
        // Do this in a new thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            checkForConflictObject();
            setsForUpload = new ArrayList<>();
            updateProgress(c.getString(R.string.sync_delete_remote_not_in_local) + "\n" + c.getString(R.string.set_lists));

            ArrayList<OpenChordsSetList> setsToBackup = new ArrayList<>();

            if (localSetLists.isEmpty()) {
                setsToBackup = serverSetLists;
            } else {
                for (int i = 0; i < localSetLists.size(); i++) {
                    OpenChordsSetList localSetList = localSetLists.get(i);
                    for (int j = 0; j < serverSetLists.size(); j++) {
                        OpenChordsSetList serverSetList = serverSetLists.get(j);
                        // If this set exists on the local, we keep it
                        if ((localSetList.getId() != null && localSetList.getId().equals(serverSetList.getId()))) {
                            setsForUpload.add(serverSetList);
                        } else {
                            setsToBackup.add(serverSetList);
                        }
                    }
                }
            }

            for (int i = 0; i < setsToBackup.size(); i++) {
                // First backup the sets
                String nowTime = mainActivityInterface.getTimeTools().getNowIsoTime();
                for (int k = 0; k < setsToBackup.size(); k++) {
                    OpenChordsSetList setToBackup = setsToBackup.get(k);
                    setToBackup.setLastUpdated(nowTime);
                    String title = setToBackup.getTitle();
                    title = title == null ? getOpenSongSetCategoryStart() + "backup" : title;
                    if (!title.startsWith(getOpenSongSetCategoryStart())) {
                        title = getOpenSongSetCategoryStart() + title;
                    }
                    String xml = convertOpenChordsSetList(setToBackup);
                    if (!mainActivityInterface.getStorageAccess().writeFileFromString("Backups", "", title + ".osts", xml)) {
                        // There was an error, so don't delete!
                        setsForUpload.add(setToBackup);
                    }
                }

                OpenChordsFolderObject uploadFolderObject = new OpenChordsFolderObject();
                uploadFolderObject.setTitle(getLocalFolderName());
                uploadFolderObject.setOwnerId(openChordsFolderUuid);
                uploadFolderObject.setSongs(serverSongs);
                uploadFolderObject.setSetLists(setsForUpload);
                uploadFolderObject.setTags(serverTags);

                //String json = gson.toJson(uploadFolderObject);
                // Replace unneccessary items
                //json = removeUnnecessaryBitsFromJson(json);
                //mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings", "", "uploadFolderObject2.json", json);

                updateProgress(c.getString(R.string.sync_uploading_changes) + "\n");
                doQueryCall(uploadFolderObject);
                updateConflictFile();
            }
            if (openChordsFragment != null) {
                openChordsFragment.queryOpenChordsServer();
            }
        });
    }

    private void doQueryCall(OpenChordsFolderObject uploadFolderObject) {
        Call<OpenChordsFolderObject> call = retrofitInterface.postOpenChordsFolder(uploadFolderObject.getOwnerId(), uploadFolderObject);
        call.enqueue(new Callback<OpenChordsFolderObject>() {
            @Override
            public void onResponse(@NonNull Call<OpenChordsFolderObject> call, @NonNull Response<OpenChordsFolderObject> response) {
                // this method is called when we get response from our api.
                if (openChordsFragment != null) {
                    openChordsFragment.changeButtonsEnable(false);
                    updateProgress(c.getString(R.string.wait) + "\n");
                    mainActivityInterface.getMainHandler().postDelayed(() -> {
                        if (openChordsFragment != null) {
                            openChordsFragment.queryOpenChordsServer();
                        }
                    }, 1000);
                }
            }

            @Override
            public void onFailure(@NonNull Call<OpenChordsFolderObject> call, @NonNull Throwable t) {
                if (openChordsFragment != null) {
                    openChordsFragment.changeButtonsEnable(false);
                    updateProgress(c.getString(R.string.wait) + "\n");
                    mainActivityInterface.getMainHandler().postDelayed(() -> {
                        if (openChordsFragment != null) {
                            openChordsFragment.queryOpenChordsServer();
                        }
                    }, 1000);
                }
            }
        });
    }

    public void addNewConflictItemObject(String action, String title, @Nullable String date) {
        OpenChordsConflictItemObject openChordsConflictItemObject = new OpenChordsConflictItemObject();
        openChordsConflictItemObject.setAction(action);
        openChordsConflictItemObject.setItem(title);
        if (date != null) {
            openChordsConflictItemObject.setDate(date);
        }
        conflictItemRecords.add(openChordsConflictItemObject);
    }

    private String trimmedOrNull(String string) {
        return (string == null || string.trim().isEmpty()) ? null : string.trim();
    }

    private Integer nullFromZero(Integer integer) {
        return (integer == null || integer == 0) ? null : integer;
    }

    private String removeUnnecessaryBitsFromJson(String json) {
        //json = json.replace("\"capo\": 0,", "");
        json = json.replace("\"tempo\": 0,", "");
        json = json.replace("\"duration\": 0,", "");
        json = json.replace("\"title\": \"\",", "");
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
        updateProgress(c.getString(R.string.sync_upload_to_openchords) + "\n");

        // Prepare the upload folder object
        OpenChordsFolderObject uploadFolderObject = new OpenChordsFolderObject();
        uploadFolderObject.setTitle(getLocalFolderName());
        uploadFolderObject.setOwnerId(openChordsFolderUuid);

        // Deal with the songs
        songsForUpload = new ArrayList<>(localSongs);
        String nowTime = mainActivityInterface.getTimeTools().getNowIsoTime();
        for (int i = 0; i < localSongs.size(); i++) {
            OpenChordsSong song = localSongs.get(i);
            addNewConflictItemObject(c.getString(R.string.sync_last_force_uploaded), song.getTitle(), nowTime);
        }
        uploadFolderObject.setSongs(songsForUpload);

        // Deal with the sets
        setsForUpload = new ArrayList<>(localSetLists);
        for (int i = 0; i < localSetLists.size(); i++) {
            OpenChordsSetList set = localSetLists.get(i);
            addNewConflictItemObject(c.getString(R.string.sync_set_force_uploaded), set.getTitle(), nowTime);
        }
        uploadFolderObject.setSetLists(setsForUpload);

        // Deal with the tags
        uploadFolderObject.setTags(newTagsForUpload);

        updateConflictItem("lastForcePush");

        //String json = gson.toJson(uploadFolderObject);
        // Replace unneccessary items
        //json = removeUnnecessaryBitsFromJson(json);
        //mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings", "", "forcePush.json", json);

        Call<OpenChordsFolderObject> call = retrofitInterface.postOpenChordsFolder(uploadFolderObject.getOwnerId(), uploadFolderObject);
        call.enqueue(new Callback<OpenChordsFolderObject>() {
            @Override
            public void onResponse(@NonNull Call<OpenChordsFolderObject> call, @NonNull Response<OpenChordsFolderObject> response) {
                // this method is called when we get response from our api.
                if (openChordsFragment != null) {
                    openChordsFragment.changeButtonsEnable(false);
                    updateProgress(c.getString(R.string.wait) + "\n");
                    mainActivityInterface.getMainHandler().postDelayed(() -> {
                        if (openChordsFragment != null) {
                            openChordsFragment.queryOpenChordsServer();
                        }
                    }, 1000);
                }
            }

            @Override
            public void onFailure(@NonNull Call<OpenChordsFolderObject> call, @NonNull Throwable t) {
                if (openChordsFragment != null) {
                    openChordsFragment.changeButtonsEnable(false);
                    updateProgress(c.getString(R.string.wait) + "\n");
                    mainActivityInterface.getMainHandler().postDelayed(() -> {
                        if (openChordsFragment != null) {
                            openChordsFragment.queryOpenChordsServer();
                        }
                    }, 1000);
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
        String folder = null;
        if (serverFolder!=null) {
            folder = serverFolder.getTitle();
        }
        mainActivityInterface.fullIndex(folder);
    }


    // Clear sync objects to free memory
    // Called when closing the OpenChordsFragment and starting sync check
    public void clearSyncObjects() {
        // The server objects
        if (serverSongs != null) {
            serverSongs.clear();
        } else {
            serverSongs = new ArrayList<>();
        }
        if (serverTags != null) {
            serverTags.clear();
        } else {
            serverTags = new ArrayList<>();
        }
        if (serverSetLists != null) {
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
            if (mainActivityInterface.getStorageAccess().getUriTreeHome() != null) {
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
        for (int i = 0; i < openChordsConflictObjects.size(); i++) {
            OpenChordsConflictObject conflictObject = openChordsConflictObjects.get(i);
            if (conflictObject.getUuid() != null && conflictObject.getUuid().equalsIgnoreCase(openChordsFolderUuid)) {
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
        for (int i = 0; i < openChordsConflictObjects.size(); i++) {
            OpenChordsConflictObject conflictObject = openChordsConflictObjects.get(i);
            if (conflictObject.getUuid() != null && conflictObject.getUuid().equals(openChordsFolderUuid)) {
                conflictObject.setItems(conflictItemRecords);
                break;
            }
        }
        openChordsConflictCheck.setConflictObects(openChordsConflictObjects);
        String json = gson.toJson(openChordsConflictCheck);
        mainActivityInterface.getStorageAccess().writeFileFromString("Settings", "", conflictCheckFile, json);
    }

    private void checkForConflictObject() {
        boolean found = false;
        for (int i = 0; i < openChordsConflictObjects.size(); i++) {
            OpenChordsConflictObject conflictObject = openChordsConflictObjects.get(i);
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
        for (int i = 0; i < openChordsConflictObjects.size(); i++) {
            OpenChordsConflictObject conflictObject = openChordsConflictObjects.get(i);
            if (conflictObject.getUuid() != null && conflictObject.getUuid().equalsIgnoreCase(openChordsFolderUuid)) {
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
        if (returnVal == null) {
            returnVal = c.getString(R.string.is_not_set);
        }
        return returnVal;
    }


    // Deal with the local folder uuid records
    public ArrayList<String> getValidFolders() {
        ArrayList<String> validFolders = new ArrayList<>();
        ArrayList<String> allFolders = mainActivityInterface.getSQLiteHelper().getFolders();
        for (int i = 0; i < allFolders.size(); i++) {
            String folder = allFolders.get(i);
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
            for (int i = 0; i < validFolders.size(); i++) {
                // Create a new record
                String folder = validFolders.get(i);
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
            if (openSongFolderRecordObjects != null) {
                for (int i = 0; i < openSongFolderRecordObjects.size(); i++) {
                    OpenSongFolderRecordObject openSongFolderRecordObject = openSongFolderRecordObjects.get(i);
                    if (!validFolders.contains(openSongFolderRecordObject.getFolderName())) {
                        // This folder is no longer valid, so remove it
                        openSongFolderRecordObjects.remove(openSongFolderRecordObjects.get(i));
                        changes = true;
                    }
                }
            }

            // Now check that we have a record of all existing folders
            if (openSongFolderRecordObjects != null) {
                for (int i = 0; i < validFolders.size(); i++) {
                    String folder = validFolders.get(i);
                    boolean found = false;
                    for (int j = 0; j < openSongFolderRecordObjects.size(); j++) {
                        OpenSongFolderRecordObject openSongFolderRecordObject = openSongFolderRecordObjects.get(j);
                        if (openSongFolderRecordObject.getFolderName() != null && openSongFolderRecordObject.getFolderName().equals(folder)) {
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
        mainActivityInterface.getStorageAccess().writeFileFromString("Settings", "",
                songFolderUUIDsFile, gson.toJson(openSongFolderObject));
    }

    public String getOpenSongFolderUuidFromName(@Nullable String folderName) {
        if (folderName != null) {
            for (int i = 0; i < openSongFolderRecordObjects.size(); i++) {
                OpenSongFolderRecordObject openSongFolderRecordObject = openSongFolderRecordObjects.get(i);
                if (openSongFolderRecordObject.getFolderName() != null &&
                        openSongFolderRecordObject.getFolderName().equals(folderName)) {
                    return openSongFolderRecordObject.getFolderUuid();
                }
            }
        }
        return null;
    }

    public @Nullable String getOpenSongFolderNameFromUUID(@Nullable String folderUuid) {
        if (folderUuid != null) {
            for (int i = 0; i < openSongFolderRecordObjects.size(); i++) {
                OpenSongFolderRecordObject openSongFolderRecordObject = openSongFolderRecordObjects.get(i);
                if (openSongFolderRecordObject.getFolderUuid() != null &&
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
        for (int i = 0; i < openSongFolderRecordObjects.size(); i++) {
            OpenSongFolderRecordObject openSongFolderRecordObject = openSongFolderRecordObjects.get(i);
            if (openSongFolderRecordObject.getFolderUuid() != null && openSongFolderRecordObject.getFolderUuid().equalsIgnoreCase(oldUuid)) {
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
        if (folderName == null && !openChordsFolderName.equalsIgnoreCase("MAIN") &&
                !openChordsFolderName.equalsIgnoreCase(mainActivityInterface.getMainfoldername())) {
            mainActivityInterface.getStorageAccess().createFolder("Songs", "", openChordsFolderName, false);
            localFolderName = openChordsFolderName;
            openSongFolderRecordObjects.add(createNewFolderRecordObject(openChordsFolderName, openChordsFolderUuid));
            saveOpenSongFolderObject();
        }
    }

    public void updateLocalSongsUuid() {
        if (!localSongNeedsServerUUID.isEmpty()) {
            for (int i = 0; i < localSongNeedsServerUUID.size(); i++) {
                OpenChordsCompareObject openChordsCompareObject = localSongNeedsServerUUID.get(i);
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
            for (int i = 0; i < localSetListNeedsServerUUID.size(); i++) {
                OpenChordsCompareObject openChordsCompareObject = localSetListNeedsServerUUID.get(i);
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
                        if (endpos > startpos) {
                            substring = xml.substring(startpos, endpos);
                        }
                    }
                    if (substring != null && openChordsCompareObject.getUuid() != null) {
                        xml = xml.replace(substring, openChordsCompareObject.getUuid());
                        mainActivityInterface.getStorageAccess().writeFileFromString("Sets", "", setName, xml);
                    }

                }


                /*Song localSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(getLocalFolderName(), openChordsCompareObject.getTitle());
                if (localSong != null) {
                    localSong.setUuid(openChordsCompareObject.getUuid());
                    mainActivityInterface.getSQLiteHelper().updateSong(localSong);
                    mainActivityInterface.getSaveSong().updateSong(localSong, false);
                }*/
            }
        }
    }

    public boolean getIsOwner() {
        return isOwner;
    }
    public boolean getIsReadOnly() {
        return isReadOnly;
    }
    public void changeReadOnly(boolean isReadOnly) {
        OpenChordsFolderPermissionsObject permissionsObject = new OpenChordsFolderPermissionsObject();
        permissionsObject.setReadonly(isReadOnly);
        permissionsObject.setUserID(openChordsUserUuid);
        Call<OpenChordsReturnMessageObject> call = retrofitInterface.postOpenChordsFolderReadOnly(openChordsFolderUuid,permissionsObject);
        call.enqueue(new Callback<OpenChordsReturnMessageObject>() {
            @Override
            public void onResponse(@NonNull Call<OpenChordsReturnMessageObject> call, @NonNull Response<OpenChordsReturnMessageObject> response) {
                // this method is called when we get response from our api.
                if (response!=null && response.body()!=null) {
                    openChordsFragment.changeButtonsEnable(false);
                    updateProgress(c.getString(R.string.wait) + "\n");
                    mainActivityInterface.getMainHandler().postDelayed(() -> {
                        if (openChordsFragment != null) {
                            openChordsFragment.queryOpenChordsServer();
                        }
                    }, 1000);
                }
            }

            @Override
            public void onFailure(@NonNull Call<OpenChordsReturnMessageObject> call, @NonNull Throwable t) {
                if (openChordsFragment != null) {
                    openChordsFragment.changeButtonsEnable(false);
                    updateProgress(c.getString(R.string.wait) + "\n");
                    mainActivityInterface.getMainHandler().postDelayed(() -> {
                        if (openChordsFragment != null) {
                            openChordsFragment.queryOpenChordsServer();
                        }
                    }, 1000);
                }
            }
        });
    }

    public OpenChordsFolderObject getServerFolder() {
        return serverFolder;
    }

    public boolean getFolderIsDifferentUuid() {
        return folderIsDifferentUuid;
    }
}