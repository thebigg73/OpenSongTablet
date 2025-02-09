package com.garethevans.church.opensongtablet.openchords;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.RetrofitInterface;
import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.InputStreamReader;
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
            openChordsFolderBaseShareable = "https://openchords.net/?fld=",
            serverTagsFile = "serverTags.json", localTagsFile = "localTags.json",
            serverSetsFile = "serverSets.json", serverSongsFile = "localSets.json",
            localSongsFile = "localSongs.json", localSetsFile="localSets.json";

    // The retrofit, server and fragment declarations
    private final RetrofitInterface retrofitInterface;
    private OpenChordsFragment openChordsFragment;
    private boolean isServerResponse = false;

    // Initialise the class
    public OpenChordsAPI(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        this.c = c;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(openChordsFolderBase)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        // TODO these are testing uuids
        //uuid = "C526B910-DE91-479F-80E9-42FEC96B39A4";
        //uuid = "4F71A53D-EC7C-47A1-BF0C-21ED28854F59";
        Log.d(TAG,"Creating openChordsAPI");
        retrofitInterface = retrofit.create(RetrofitInterface.class);
        openChordsFolderName = mainActivityInterface.getPreferences().getMyPreferenceString("openChordsFolderName",mainActivityInterface.getMainfoldername());
        openChordsFolderUuid = mainActivityInterface.getPreferences().getMyPreferenceString("openChordsFolderUuid",mainActivityInterface.getStorageAccess().getUUIDForSongFolder(openChordsFolderName));

        // Check we have the file
        //Uri localUri = mainActivityInterface.getStorageAccess().getUriForItem("Settings","",localTagsFile);
        //mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false,localUri,null,"Settings","",localTagsFile);
        //OpenChordsTag[] tagsLocalStringArray = gson.fromJson(new InputStreamReader(mainActivityInterface.getStorageAccess().getInputStream(localUri)),OpenChordsTag[].class);
        //localTags = new ArrayList<>();
        //if (tagsLocalStringArray!=null) {
        //    Collections.addAll(localTags, tagsLocalStringArray);
        //}
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
    public void setServerFolder(OpenChordsFolderObject serverFolder) {
        this.serverFolder = serverFolder;
    }
    public void setOpenChordsFolderUuid(String openChordsFolderName, String openChordsFolderUuid) {
        if (openChordsFolderName==null || openChordsFolderName.isEmpty()) {
            // Try to look up a matching local folder
            openChordsFolderName = mainActivityInterface.getStorageAccess().getSongFolderForUUID(null,openChordsFolderUuid);
        }

        this.openChordsFolderName = openChordsFolderName;
        this.openChordsFolderUuid = openChordsFolderUuid;
        Log.d(TAG,"openChordsFolderName:"+openChordsFolderName+"  openChordsFolderUuid:"+openChordsFolderUuid);
        mainActivityInterface.getPreferences().setMyPreferenceString("openChordsFolderName",openChordsFolderName);
        mainActivityInterface.getPreferences().setMyPreferenceString("openChordsFolderUuid",openChordsFolderUuid);
    }
    public void setOpenChordsFolderName(String openChordsFolderName) {
        // This is called by the user choosing a new local folder from the OpenChordsFragment
        // We only have the folderName, so we need to lookup the UUID (or create it)
        this.openChordsFolderName = openChordsFolderName;
        openChordsFolderUuid = mainActivityInterface.getStorageAccess().getUUIDForSongFolder(openChordsFolderName);
        Log.d(TAG,"Set the folder to: "+openChordsFolderName + " ("+openChordsFolderUuid+")");
    }


    // The server objects (songs, sets, tags)
    public ArrayList<OpenChordsSong> getServerSongs() {
        return serverSongs;
    }
    public ArrayList<OpenChordsSetList> getServerSetLists() {
        return serverSetLists;
    }
    public ArrayList<OpenChordsTag> getServerTags() {
        return serverTags;
    }

    // The local objects (songs, sets, tags)
    public ArrayList<OpenChordsSong> getLocalSongs() {
        return localSongs;
    }
    public ArrayList<OpenChordsSetList> getLocalSetLists() {
        return localSetLists;
    }
    public OpenChordsSong getOpenChordsSong(Song thisSong) {
        // This converts an OpenSong song into an OpenChords song ready for sync
        OpenChordsSong openChordsSong = new OpenChordsSong();
        openChordsSong.setId(jsonNullIfEmpty(thisSong.getUuid()));
        openChordsSong.setTitle(jsonNullIfEmpty(thisSong.getTitle()));
        openChordsSong.setArtist(jsonNullIfEmpty(thisSong.getAuthor()));
        String capo = thisSong.getCapo();
        if (capo!=null && !capo.replaceAll("\\D","").isEmpty()) {
            openChordsSong.setCapo(Integer.parseInt(capo.replaceAll("\\D","")));
        } else {
            openChordsSong.setCapo(0);
        }
        openChordsSong.setCcli(jsonNullIfEmpty(thisSong.getCcli()));
        String duration = thisSong.getAutoscrolllength();
        if (duration!=null && !duration.replaceAll("\\D","").isEmpty()) {
            openChordsSong.setDuration(jsonNullIfEmpty(mainActivityInterface.getTimeTools().timeFormatFixer(Integer.parseInt(duration.replaceAll("\\D","")))));
        } else {
            openChordsSong.setDuration(null);
        }
        openChordsSong.setCopyright(jsonNullIfEmpty(thisSong.getCopyright()));
        if (thisSong.getKey()!=null && !thisSong.getKey().isEmpty()) {
            openChordsSong.setKey(jsonNullIfEmpty(thisSong.getKey().replace("m","")));
            openChordsSong.setKeyIsMinor(thisSong.getKey().endsWith("m"));
        }
        if (thisSong.getTheme()!=null && !thisSong.getTheme().isEmpty()) {
            ArrayList<String> tagsToAdd = getTagStrings(thisSong);
            if (!tagsToAdd.isEmpty()) {
                String[] tags = tagsToAdd.toArray(new String[0]);
                openChordsSong.setTags(tags);
            } else {
                openChordsSong.setTags(null);
            }
        } else {
            openChordsSong.setTags(null);
        }
        openChordsSong.setLastUpdated(jsonNullIfEmpty(thisSong.getLastModified()));
        openChordsSong.setNotes(jsonNullIfEmpty(thisSong.getNotes()));
        if (thisSong.getTempo()!=null && !thisSong.getTempo().isEmpty()) {
            String tempo = thisSong.getTempo().replaceAll("\\D","");
            if (!tempo.isEmpty()) {
                openChordsSong.setTempo(Integer.parseInt(tempo));
            }
        }
        openChordsSong.setTimeSignature(jsonNullIfEmpty(thisSong.getTimesig()));
        //openChordsSong.setTranspose(thisSong.getKey());
        openChordsSong.setTranspose(null);
        openChordsSong.setRawData(jsonNullIfEmpty(mainActivityInterface.getConvertChoPro().fromOpenSongToChordPro(thisSong.getLyrics())));

        return openChordsSong;
    }
    private ArrayList<String> getTagStrings(Song thisSong) {
        String[] tagTitles = thisSong.getTheme().split(";");
        // OpenChords saves tags as UUIDs in the song that are referenced in the tags section of the json
        // We need to look up the tags in our local json file
        ArrayList<String> tagsToAdd = new ArrayList<>();
        for (String tagTitle:tagTitles) {
            boolean found = false;
            // Try looking through the server tags for a match
            for (OpenChordsTag serverTag:serverTags) {
                if (serverTag.getTitle().equals(tagTitle)) {
                    tagsToAdd.add(serverTag.getId());
                    found = true;
                    break;
                }
            }

            if (!found) {
                // We need to create a new tag
                OpenChordsTag newTag = new OpenChordsTag();
                newTag.setTitle(tagTitle);
                newTag.setId(String.valueOf(UUID.randomUUID()));
                newTag.setColor(mainActivityInterface.getMyThemeColors().getNonAlphaHexColorFromInt(c.getResources().getColor(R.color.colorAltPrimary)));
                localTags.add(newTag);
            }
        }
        return tagsToAdd;
    }


    // The comparison logic between the server and local
    public ArrayList<OpenChordsCompareObject> getLocalSongsCompareObjects() {
        return localSongsCompareObjects;
    }
    public ArrayList<OpenChordsCompareObject> getServerSongsCompareObjects() {
        return serverSongsCompareObjects;
    }
    public ArrayList<OpenChordsCompareObject> getSongsNotOnLocal() {
        return songsNotOnLocal;
    }
    public ArrayList<OpenChordsCompareObject> getSongsNotOnServer() {
        return songsNotOnServer;
    }
    public ArrayList<OpenChordsCompareObject> getSongsOnLocalOlder() {
        return songsOnLocalOlder;
    }
    public ArrayList<OpenChordsCompareObject> getSongsOnServerOlder() {
        return songsOnServerOlder;
    }
    public ArrayList<OpenChordsCompareObject> getSongsWithNoChanges() {
        return songsWithNoChanges;
    }
    public ArrayList<OpenChordsCompareObject> getSetListsNotOnLocal() {
        return setListsNotOnLocal;
    }
    public ArrayList<OpenChordsCompareObject> getSetListsNotOnServer() {
        return setListsNotOnServer;
    }
    public ArrayList<OpenChordsCompareObject> getSetListsOnLocalOlder() {
        return setListsOnLocalOlder;
    }
    public ArrayList<OpenChordsCompareObject> getSetListsOnServerOlder() {
        return setListsOnServerOlder;
    }
    public ArrayList<OpenChordsCompareObject> getSetListsWithNoChanges() {
        return setListsWithNoChanges;
    }
    public int getSongsNotOnLocalCount() {
        songsNotOnLocalCount = getSongsNotOnLocal().size();
        return songsNotOnLocalCount;
    }
    public int getSongsNotOnServerCount() {
        songsNotOnServerCount = getSongsNotOnServer().size();
        return songsNotOnServerCount;
    }
    public int getSongsWithNoChangesCount() {
        songsWithNoChangesCount = getSongsWithNoChanges().size();
        return songsWithNoChangesCount;
    }
    public int getSongsOnLocalOlderCount() {
        songsOnLocalOlderCount = getSongsOnLocalOlder().size();
        return songsOnLocalOlderCount;
    }
    public int getSongsOnServerOlderCount() {
        songsOnServerOlderCount = getSongsOnServerOlder().size();
        return songsOnServerOlderCount;
    }
    public int getSetListsNotOnLocalCount() {
        setListsNotOnLocalCount = getSetListsNotOnLocal().size();
        return setListsNotOnLocalCount;
    }
    public int getSetListsNotOnServerCount() {
        setListsNotOnServerCount = getSetListsNotOnServer().size();
        return setListsNotOnServerCount;
    }
    public int getSetListsWithNoChangesCount() {
        setListsWithNoChangesCount = getSetListsWithNoChanges().size();
        return setListsWithNoChangesCount;
    }
    public int getSetListsOnLocalOlderCount() {
        setListsOnLocalOlderCount = setListsOnLocalOlder.size();
        return setListsOnLocalOlderCount;
    }
    public int getSetListsOnServerOlderCount() {
        setListsOnServerOlderCount = setListsOnServerOlder.size();
        return setListsOnServerOlderCount;
    }

    public String getSongsNotOnLocalString() {
        return getStringFromCompareObjects(getSongsNotOnLocal());
    }
    public String getSongsNotOnServerString() {
        return getStringFromCompareObjects(getSongsNotOnServer());
    }
    public String getSongsOnLocalOlderString() {
        return getStringFromCompareObjects(getSongsOnLocalOlder());
    }
    public String getSongsOnServerOlderString() {
        return getStringFromCompareObjects(getSongsOnServerOlder());
    }
    public String getSongsWithNoChangesString() {
        return getStringFromCompareObjects(getSongsWithNoChanges());
    }
    public String getSetListsNotOnLocalString() {
        return getStringFromCompareObjects(getSetListsNotOnLocal());
    }
    public String getSetListsNotOnServerString() {
        return getStringFromCompareObjects(getSetListsNotOnServer());
    }
    public String getSetListsOnLocalOlderString() {
        return getStringFromCompareObjects(getSetListsOnLocalOlder());
    }
    public String getSetListsOnServerOlderString() {
        return getStringFromCompareObjects(getSetListsOnServerOlder());
    }
    public String getSetListsWithNoChangesString() {
        return getStringFromCompareObjects(getSetListsWithNoChanges());
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
    public void createLocalObjects() {
        // This creates OpenChords formatted songs, sets, tags from local files
        // Get a list of songs in this local folder
        ArrayList<Song> localOpenSongSongs = mainActivityInterface.getSQLiteHelper().openChordsSyncGetSongsFromFolder(openChordsFolderName);
        // For each found song, create an OpenChordsSong object and add it to the array
        for (Song localOpenSongSong : localOpenSongSongs) {
            updateProgress(c.getString(R.string.sync_checking_local_item)+": "+localOpenSongSong.getTitle());
            localSongs.add(getOpenChordsSong(localOpenSongSong));
            localSongsCompareObjects.add(createOpenChordsCompareObject(localOpenSongSong.getUuid(),
                    localOpenSongSong.getTitle(),localOpenSongSong.getLastModified(),"song"));
        }

        // Go through our sets and look for sets with a category matching OpenChords
        for (String setName : mainActivityInterface.getStorageAccess().listFilesInFolder("Sets", "")) {
            if (setName.startsWith("OpenChords" + mainActivityInterface.getSetActions().getSetCategorySeparator())) {
                String actualSetName = setName.replace("OpenChords" + mainActivityInterface.getSetActions().getSetCategorySeparator(), "");

                Log.d(TAG,"actualSetName:"+actualSetName);
                // Parse the set XML file and get the contents
                ArrayList<String> thisSet = new ArrayList<>();
                thisSet.add(setName);
                String[] bits = mainActivityInterface.getExportActions().parseSets(thisSet);
                // bits[0] = folder/filename pair
                // bits[1] = text for export (not needed here) which includes author, hymn num and key
                // bits[2] = preferred key
                // bits[3] = uuid (creates new if null)
                // bits[4] = lastModified (creates new if null)
                // bits[5] = notes

                OpenChordsSetList localSetList = new OpenChordsSetList();
                localSetList.setTitle(actualSetName);
                localSetList.setId(bits[3]);
                localSetList.setLastUpdated(bits[4]);
                localSetList.setNotes(bits[5]);

                // Now we need to go through the set items and add them
                // TODO deal with separators and slides/notes rather than just songs
                String[] folderAndFiles = bits[0].split("\n");
                String[] preferredKeys = bits[2].split("\n");
                ArrayList<OpenChordsSetListItem> localSetListItems = new ArrayList<>();
                for (int i = 0; i < folderAndFiles.length; i++) {
                    String folderAndFile = folderAndFiles[i];
                    String preferredKey = preferredKeys[i];
                    Log.d(TAG, "folderAndFile:" + folderAndFile + "  preferredKey:" + preferredKey);

                    // Now we need to get the id of the song item
                    String[] songInfo = mainActivityInterface.getSQLiteHelper().getUuidFromFolderAndFile(folderAndFile);
                    OpenChordsSetListItem localSetListItem = createOpenChordsSetListItem(songInfo, preferredKey);
                    Log.d(TAG, "uuid:" + songInfo[0] + "  title:" + songInfo[1]);
                    localSetListItems.add(localSetListItem);
                }
                localSetList.setItems(localSetListItems);
                localSetLists.add(localSetList);
                localSetListsCompareObjects.add(createOpenChordsCompareObject(bits[3],actualSetName,bits[4],"set"));
            }
        }

        // Save the most recent local sets
        //String setsJson = gson.toJson(localSetLists);
        //mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings", "", localSetsFile, setsJson);
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
        openChordsSetListSongItem.setTranspose(transposeKey.replace("m",""));
        return openChordsSetListSongItem;
    }

    public void createServerCompareObjects() {
        // Get a list of the server songs
        serverSongsCompareObjects.clear();
        for (OpenChordsSong serverSong : serverSongs) {
            updateProgress(c.getString(R.string.sync_checking_remote_item)+": "+serverSong.getTitle());
            OpenChordsCompareObject serverSongCompareObject = createOpenChordsCompareObject(serverSong.getId(),
                    serverSong.getTitle(), serverSong.getLastUpdated(), "song");
            serverSongsCompareObjects.add(serverSongCompareObject);
            long openChordsLastModifiedMillis = Instant.parse(serverSong.getLastUpdated()).toEpochMilli();
            // Check to see if this song is in our local folder
            // Look using the uuid (not the filename/title)
            boolean dealtwith = false;
            for (OpenChordsCompareObject localSongCompare : localSongsCompareObjects) {
                if (localSongCompare.getUuid().equals(serverSong.getId())) {
                    Log.d(TAG,"match found localSong.getUuid():"+localSongCompare.getUuid()+"  ("+localSongCompare.getTitle()+")");

                    // Ok, we already have this so check the last edited date
                    String lastModified = localSongCompare.getLastModified();
                    if (lastModified == null || lastModified.isEmpty()) {
                        Song tempSong = new Song();
                        tempSong.setFolder(openChordsFolderName);
                        tempSong.setFilename(mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(localSongCompare.getTitle()));
                        lastModified = mainActivityInterface.getTimeTools().getIsoTimeFromSongFileMetadata(tempSong);
                        localSongCompare.setLastModified(lastModified);
                    }

                    long localLastModified = 1000 * (Instant.parse(localSongCompare.getLastModified()).toEpochMilli() / 1000);

                    Log.d(TAG,"localLastModified:"+localLastModified);
                    Log.d(TAG,"openChordsLastModifiedMillis:"+openChordsLastModifiedMillis);
                    // Compare with the one from the openChords server
                    if (localLastModified < openChordsLastModifiedMillis) {
                        // We need to update our song with the new values
                        // Log results
                        Log.d(TAG, "diff_local_older:" + localSongCompare.getTitle());
                        songsOnLocalOlder.add(localSongCompare);
                        dealtwith = true;
                        break;

                    } else if (localLastModified > openChordsLastModifiedMillis) {
                        // The local version is newer than the version on the server
                        Log.d(TAG, "diff_server_older:" + localSongCompare.getTitle());
                        songsOnServerOlder.add(localSongCompare);
                        dealtwith = true;
                        break;

                    } else {
                        // No changes required
                        Log.d(TAG,"no_changes:"+localSongCompare.getTitle());
                        songsWithNoChanges.add(localSongCompare);
                        dealtwith = true;
                        break;
                    }
                }
            }

            if (!dealtwith) {
                // We don't have the song, so add it
                Log.d(TAG, "diff_not_on_local:" + serverSong.getTitle());
                songsNotOnLocal.add(serverSongCompareObject);
                updateTotalChanges();
            }
        }


        for (OpenChordsSetList serverSetList : serverSetLists) {
            updateProgress(c.getString(R.string.sync_checking_remote_item)+": "+serverSetList.getTitle());
            OpenChordsCompareObject serverSongCompareObject = createOpenChordsCompareObject(serverSetList.getId(),
                    serverSetList.getTitle(), serverSetList.getLastUpdated(), "set");
            serverSongsCompareObjects.add(serverSongCompareObject);

            if (serverSetList.getLastUpdated()==null) {
                serverSetList.setLastUpdated(mainActivityInterface.getTimeTools().getNowIsoTime());
            }
            long openChordsLastModifiedMillis = Instant.parse(serverSetList.getLastUpdated()).toEpochMilli();
            // Check to see if this song is in our local folder
            // Look using the uuid (not the filename/title)
            boolean dealtwith = false;
            for (OpenChordsCompareObject localSetCompare : localSetListsCompareObjects) {
                if (localSetCompare.getUuid().equals(serverSetList.getId())) {
                    Log.d(TAG,"match found localSetList.getUuid():"+localSetCompare.getUuid()+"  ("+localSetCompare.getTitle()+")");

                    // Ok, we already have this so check the last edited date
                    String lastModified = localSetCompare.getLastModified();
                    long localLastModified = 1000 * (Instant.parse(localSetCompare.getLastModified()).toEpochMilli() / 1000);

                    Log.d(TAG,"localLastModified:"+localLastModified);
                    Log.d(TAG,"openChordsLastModifiedMillis:"+openChordsLastModifiedMillis);
                    // Compare with the one from the openChords server
                    if (localLastModified < openChordsLastModifiedMillis) {
                        // We need to update our song with the new values
                        // Log results
                        Log.d(TAG, "diff_local_older:" + localSetCompare.getTitle());
                        setListsOnLocalOlder.add(localSetCompare);
                        dealtwith = true;
                        break;

                    } else if (localLastModified > openChordsLastModifiedMillis) {
                        // The local version is newer than the version on the server
                        Log.d(TAG, "diff_server_older:" + localSetCompare.getTitle());
                        setListsOnServerOlder.add(localSetCompare);
                        dealtwith = true;
                        break;

                    } else {
                        // No changes required
                        Log.d(TAG,"no_changes:"+localSetCompare.getTitle());
                        songsWithNoChanges.add(localSetCompare);
                        dealtwith = true;
                        break;
                    }
                }
            }

            if (!dealtwith) {
                // We don't have the set, so add it
                Log.d(TAG, "diff_not_on_local:" + serverSetList.getTitle());
                setListsNotOnLocal.add(serverSongCompareObject);
                updateTotalChanges();
            }
        }

        /*// Now go through the local songs and check for files that aren't on the server
        for (OpenChordsCompareObject localSongCompare : mainActivityInterface.getOpenChordsAPI().getLocalSongsCompare()) {
            boolean found = false;
            for (OpenChordsCompareObject serverSongCompare : mainActivityInterface.getOpenChordsAPI().getServerSongsCompare()) {
                if (localSongCompare.getUuid().equals(serverSongCompare.getUuid())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                mainActivityInterface.getOpenChordsAPI().getNotOnServer().add(localSongCompare);
                mainActivityInterface.getOpenChordsAPI().updateTotalChanges();
            }
        }*/
    }

    public int getTotalChanges() {
        return getSongsNotOnLocalCount() + getSongsNotOnServerCount() +
                getSongsOnLocalOlderCount() + getSongsOnServerOlderCount();
    }

    // The callbacks from the server
    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
        // Reset the list of objects found in the local and server and any differences
        clearSyncObjects();

        Log.d(TAG,"response:"+response.isSuccessful());
        if (response.isSuccessful()) {
            isServerResponse = true;
            serverFolder = (OpenChordsFolderObject)response.body();

            if (serverFolder!=null) {
                // Lets get the server objects we have found!
                updateProgress(c.getString(R.string.sync_reading_remote_folder));
                serverTags = serverFolder.getTags();
                serverSongs = serverFolder.getSongs();
                serverSetLists = serverFolder.getSetLists();

                // Save this to a separate json file
                //String json = gson.toJson(serverTags);
                //mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings", "", serverTagsFile, json);
                // Save this to a separate json file
                //json = gson.toJson(serverSongs);
                //mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings", "", serverSongsFile, json);
                // Save this to a separate json file
                //json = gson.toJson(serverSetLists);
                //mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings", "", serverSetsFile, json);

                // Now check our local folders
                updateProgress(c.getString(R.string.sync_reading_local_folder));
                //prepareLocalSongs();
                //prepareLocalTags();
                //prepareLocalSetLists();

                // Now create the local compare objects
                createLocalObjects();

                // Now create the server compare objects
                createServerCompareObjects();


                Log.d(TAG,"serverSetLists.size():"+serverSetLists.size());
                // TODO testing
                for (OpenChordsSetList serverSetList : serverSetLists) {
                    convertOpenChordsSetList(serverSetList);
                }




                // Send the info back to the openChordsFragment
                if (serverFolder != null && openChordsFragment != null) {
                    // We just want the folder title for now
                    // Make sure we have a record of this in our Settings
                    String title = serverFolder.getTitle();
                    mainActivityInterface.getStorageAccess().checkSongFolderUUIDExist(title, openChordsFolderUuid);
                    //setOpenChordsFolderNameServer(title);
                    setOpenChordsFolderUuid(title, openChordsFolderUuid);
                    mainActivityInterface.setWhattodo("");
                    openChordsFragment.updateFolderTitle(serverFolder.getTitle());
                    isServerResponse = false;
                    openChordsFragment.logChanges();
                }
            }
        } else {
            Log.d(TAG, "not successful");
            serverFolder = null;
            serverSongs = null;
            serverTags = null;
            serverSetLists = null;
            isServerResponse = false;
            if (openChordsFragment != null) {
                openChordsFragment.openChordsFolderNotFound();
            }
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
    public String convertOpenChordsSetList(OpenChordsSetList serverSetList) {
        CurrentSet localSet = new CurrentSet(c);
        localSet.setUuid(serverSetList.getId());
        localSet.setSetCurrentLastName("OpenChords" + mainActivityInterface.getSetActions().getSetCategorySeparator() + serverSetList.getTitle());
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
            // Check the server songs first
            Log.d(TAG,"itemId:"+itemId);
            Log.d(TAG,"itemTitle:"+itemTitle);
            Log.d(TAG,"itemType:"+itemType);
            Log.d(TAG,"itemCustomData:"+itemCustomData);
            Log.d(TAG,"itemLastUpdated:"+itemLastUpdated);
            Log.d(TAG,"itemNotes:"+itemNotes);

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

            }
        }
        String xml = mainActivityInterface.getSetActions().createSetXML(localSet);
        Log.d(TAG,"xml:\n"+xml);
        return xml;
    }


    public void downloadFromServer() {
        if (getTotalChanges()==0) {
            mainActivityInterface.getShowToast().doIt(c.getString(R.string.sync_no_changes_required));
        } else if (getServerFolder() != null) {
           // Make sure we actually have the folder in local storage!
            Uri folderUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",openChordsFolderName,null);
            if (!mainActivityInterface.getStorageAccess().uriExists(folderUri)) {
                Log.d(TAG, "folder didn't exist on local, so create");
                mainActivityInterface.getStorageAccess().createFolder("Songs",
                        "", openChordsFolderName, false);
                // Now also make sure we have an entry in the UUID.txt file
                mainActivityInterface.getStorageAccess().checkSongFolderUUIDExist(
                        openChordsFolderName, openChordsFolderUuid);
                Log.d(TAG, "updated uuid file");
            }

            Song existingSong = null;

            // Let's work through the song items we need to download from the server
            for (OpenChordsSong openChordsSong : getServerSongs()) {
                boolean dealtwith = false;
                for (OpenChordsCompareObject notOnLocalObject : songsNotOnLocal) {
                    if (notOnLocalObject.getUuid().equals(openChordsSong.getId())) {
                        // Add this songs to the local folder
                        String title = notOnLocalObject.getTitle();
                        String filename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(title);
                        updateProgress(openChordsSong.getTitle() + ": " + c.getString(R.string.sync_creating));
                        Log.d(TAG, "Song:" + openChordsSong.getTitle() + " didn't exist, so create it");
                        Uri songUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",
                                openChordsFolderName, filename);
                            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, songUri, null,
                                    "Songs", openChordsFolderName, filename);
                            Log.d(TAG, "songUri:" + songUri);
                            existingSong = new Song();
                            existingSong.setFolder(openChordsFolderName);
                            existingSong.setFilename(filename);
                            existingSong.setTitle(title);
                            existingSong.setLastModified(notOnLocalObject.getLastModified());
                            existingSong = updateOpenSongSong(existingSong, openChordsSong);
                            // Save the song
                            mainActivityInterface.getSQLiteHelper().createSong(openChordsFolderName, filename);
                            mainActivityInterface.getSaveSong().setResetLastModified(false);
                            mainActivityInterface.getSaveSong().updateSong(existingSong, false);
                            mainActivityInterface.getSaveSong().setResetLastModified(true);
                            dealtwith = true;
                            break;
                        }
                    }

                    if (!dealtwith) {
                        for (OpenChordsCompareObject localOlderObject : songsOnLocalOlder) {
                            if (localOlderObject.getUuid().equals(openChordsSong.getId())) {
                                // Update these songs on the local folder
                                String title = localOlderObject.getTitle();
                                String filename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(title);
                                updateProgress(openChordsSong.getTitle() + ": " + c.getString(R.string.sync_updating));
                                existingSong = new Song();
                                existingSong.setFolder(openChordsFolderName);
                                existingSong.setFilename(filename);
                                existingSong.setTitle(title);
                                existingSong = updateOpenSongSong(existingSong, openChordsSong);
                                Log.d(TAG, "updating song:" + filename);
                                mainActivityInterface.getSaveSong().setResetLastModified(false);
                                mainActivityInterface.getSaveSong().updateSong(existingSong, false);
                                mainActivityInterface.getSaveSong().setResetLastModified(true);
                                break;
                            }
                        }
                    }
                }


            for (OpenChordsSetList openChordsSetList : getServerSetLists()) {
                boolean dealtwith = false;
                for (OpenChordsCompareObject notOnLocalObject : setListsNotOnLocal) {
                    if (notOnLocalObject.getUuid().equals(openChordsSetList.getId())) {
                        // Add this setlist to the local folder
                        String title = notOnLocalObject.getTitle();
                        String filename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename("OpenChords"+mainActivityInterface.getSetActions().getSetCategorySeparator()+title);
                        updateProgress(openChordsSetList.getTitle() + ": " + c.getString(R.string.sync_creating));
                        Log.d(TAG, "Song:" + openChordsSetList.getTitle() + " didn't exist, so create it");
                        Uri setUri = mainActivityInterface.getStorageAccess().getUriForItem("Sets",
                                "", filename);
                        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, setUri, null,
                                "Sets", "", filename);
                        Log.d(TAG, "setUri:" + setUri);
                        String setListXML = convertOpenChordsSetList(openChordsSetList);
                        mainActivityInterface.getStorageAccess().doStringWriteToFile("Sets","",filename,setListXML);
                        dealtwith = true;
                        break;
                    }
                }

                if (!dealtwith) {
                    for (OpenChordsCompareObject localOlderObject : setListsOnLocalOlder) {
                        if (localOlderObject.getUuid().equals(openChordsSetList.getId())) {
                            // Update these songs on the local folder
                            String title = localOlderObject.getTitle();
                            String filename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(title);
                            updateProgress(openChordsSetList.getTitle() + ": " + c.getString(R.string.sync_updating));
                            String setListXML = convertOpenChordsSetList(openChordsSetList);
                            mainActivityInterface.getStorageAccess().doStringWriteToFile("Sets","",filename,setListXML);
                            break;
                        }
                    }
                }
            }

                // Now delete the songs we have on our local folder that aren't on the server
//                if (!mainActivityInterface.getOpenChordsAPI().getNotOnServer().isEmpty()) {
//                    for (OpenChordsCompareObject openChordsCompareObject : mainActivityInterface.getOpenChordsAPI().getNotOnServer()) {
//                        // Delete this song from our local folder as it isn't on the server
//                        Log.d(TAG,"deleting song as not on server:"+openChordsCompareObject.getTitle());
//                        *//*mainActivityInterface.getStorageAccess().doDeleteFile("Songs",
//                                mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName(),
//                                openChordsCompareObject.getTitle());
//                        mainActivityInterface.getSQLiteHelper().deleteSong(
//                                mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName(),
//                                openChordsCompareObject.getTitle());*//*
//                    }
//                }

                // Now update the song menu
                mainActivityInterface.getSongListBuildIndex().setIndexRequired(true);
                mainActivityInterface.getSongListBuildIndex().setFullIndexRequired(true);
                mainActivityInterface.fullIndex();

                // Now query the server again to compare
            if (openChordsFragment!=null) {
                openChordsFragment.queryOpenChordsServer();
            }

        }

    }


    // Convert OpenSong objects into OpenChords objects
    public Song updateOpenSongSong(Song song,OpenChordsSong openChordsSong) {
        song.setTitle(openChordsSong.getTitle());
        song.setUuid(openChordsSong.getId());
        Log.d(TAG,"openChordsSong.getLastUpdated():"+openChordsSong.getLastUpdated());
        song.setLastModified(openChordsSong.getLastUpdated());
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
                Log.d(TAG,song.getTitle()+"  song tag:"+tag);
                // Try to find this tag in the folder tags array
                for (OpenChordsTag openChordsTag : serverTags) {
                    Log.d(TAG,"looking up folder tag: "+openChordsTag.getTitle()+"  "+openChordsTag.getColor()+"  "+openChordsTag.getId());
                    if (tag.equals(openChordsTag.getId())) {
                        Log.d(TAG,"matching tag id.  Adding string:"+openChordsTag.getTitle());
                        tagStringBuilder.append(openChordsTag.getTitle()).append("\n");
                        break;
                    }
                }
            }
        }
        song.setTheme(tagStringBuilder.toString().trim().replace("\n",";"));
        return song;
    }
    public String fixOpenChordsLyrics(String lyrics) {
        // TODO OpenSong does not allow colours or styles in lyrics, so we need to parse
        return lyrics;
    }


    private String getEmptyForZero(int integer) {
        return integer==0 ? "" : String.valueOf(integer);
    }





    public void updateTotalChanges() {
        int totalChanges = getSongsNotOnLocalCount() + getSongsNotOnServerCount() +
                getSongsOnLocalOlderCount() + getSongsOnServerOlderCount();
        Log.d(TAG,"totalChanges:"+ totalChanges);
    }






    // Prepare upload information
    public String prepareMyTagsJson() {
        OpenChordsFolderObject localFolder = new OpenChordsFolderObject();

        // Go through the songs in the OpenChordsFolder and build the objects!
        ArrayList<OpenChordsSetList> myOpenChordsSetLists = new ArrayList<>();

        // Add the songs to the folder object
        localFolder.setSongs(localSongs);

        // Now add the tags
        localFolder.setTags(localTags);

        // Now add the sets
        localFolder.setSetLists(localSetLists);

        String json = gson.toJson(localFolder);
        mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings","","localFolder.json",json);
        return json;
    }
    public void prepareLocalSongs() {
        ArrayList<Song> songsInFolder = mainActivityInterface.getSQLiteHelper().openChordsSyncGetSongsFromFolder(openChordsFolderName);
        for (Song song : songsInFolder) {
            OpenChordsSong openChordsSong = getOpenChordsSong(song);
            localSongs.add(openChordsSong);
        }

        // Save the most recent local songs
        String songsJson = gson.toJson(localSongs);
        mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings","",localSongsFile,songsJson);
    }
    public void prepareLocalTags() {
        // Go through the songs in our folder and get the tags and their uuids
        // The last synced tag files are stored int the localTagsFile

        // Firstly get the tag (titles) from the songs in our folder
        ArrayList<OpenChordsTag> tagsFromFiles = mainActivityInterface.getSQLiteHelper().getThemesFromFilesInFolder(openChordsFolderName);

        // Every time we sync, we create a localTagsFile json object that has all of the relevant info
        Uri localTagsUri = mainActivityInterface.getStorageAccess().getUriForItem("Settings","",localTagsFile);
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false,localTagsUri,null,"Settings","",localTagsFile);
        OpenChordsTag[] tempLocalTags = gson.fromJson(new InputStreamReader(mainActivityInterface.getStorageAccess().getInputStream(localTagsUri)),OpenChordsTag[].class);

        for (OpenChordsTag localTag : tempLocalTags) {
            boolean found = false;
            for (OpenChordsTag tagFromFiles:tagsFromFiles) {
                if (localTag.getTitle().equals(tagFromFiles.getTitle())) {
                    tagFromFiles.setId(localTag.getId());
                    tagFromFiles.setColor(localTag.getColor());
                    localTags.add(tagFromFiles);
                    found = true;
                }
                if (found) {
                    break;
                }
            }
            if (!found) {
                // This is a new tag
                localTag.setId(String.valueOf(UUID.randomUUID()));
                localTag.setColor(mainActivityInterface.getMyThemeColors().getHexFromIntNoAlpha(c.getResources().getColor(R.color.colorPrimary)));
                localTags.add(localTag);
            }
        }

        // Save the most recent local tags
        String tagsJson = gson.toJson(localTags);
        mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings","",localTagsFile,tagsJson);
    }
    public void prepareLocalSetLists() {
        // Go through our sets and look for sets with a category matching OpenChords
        for (String setName : mainActivityInterface.getStorageAccess().listFilesInFolder("Sets", "")) {
            if (setName.startsWith("OpenChords" + mainActivityInterface.getSetActions().getSetCategorySeparator())) {
                String actualSetName = setName.replace("OpenChords" + mainActivityInterface.getSetActions().getSetCategorySeparator(), "");

                // Parse the set XML file and get the contents
                ArrayList<String> thisSet = new ArrayList<>();
                thisSet.add(setName);
                String[] bits = mainActivityInterface.getExportActions().parseSets(thisSet);
                // bits[0] = folder/filename pair
                // bits[1] = text for export (not needed here) which includes author, hymn num and key
                // bits[2] = preferred key
                // bits[3] = uuid (creates new if null)
                // bits[4] = lastModified (creates new if null)
                // bits[5] = notes

                OpenChordsSetList localSetList = new OpenChordsSetList();
                localSetList.setTitle(actualSetName);
                localSetList.setId(bits[3]);
                localSetList.setLastUpdated(bits[4]);
                localSetList.setNotes(bits[5]);

                // Now we need to go through the set items and add them
                // TODO deal with separators and slides/notes rather than just songs
                String[] folderAndFiles = bits[0].split("\n");
                String[] preferredKeys = bits[2].split("\n");
                ArrayList<OpenChordsSetListItem> localSetListItems = new ArrayList<>();
                for (int i = 0; i < folderAndFiles.length; i++) {
                    String folderAndFile = folderAndFiles[i];
                    String preferredKey = preferredKeys[i];
                    Log.d(TAG, "folderAndFile:" + folderAndFile + "  preferredKey:" + preferredKey);

                    // Now we need to get the id of the song item
                    String[] songInfo = mainActivityInterface.getSQLiteHelper().getUuidFromFolderAndFile(folderAndFile);
                    OpenChordsSetListItem localSetListItem = new OpenChordsSetListItem();
                    localSetListItem.setId(songInfo[0]);
                    // Set list items don't include the song title
                    localSetListItem.setTitle("");
                    localSetListItem.setType("song");
                    OpenChordsSetListSongItem openChordsSetListSongItem = new OpenChordsSetListSongItem();
                    openChordsSetListSongItem.setSongId(songInfo[0]);
                    openChordsSetListSongItem.setTranspose(preferredKey.replace("m",""));
                    localSetListItem.setSongItem(openChordsSetListSongItem);
                    Log.d(TAG, "uuid:" + songInfo[0] + "  title:" + songInfo[1]);
                    localSetListItems.add(localSetListItem);
                }
                localSetList.setItems(localSetListItems);
                localSetLists.add(localSetList);
            }
        }

        // Save the most recent local sets
        String setsJson = gson.toJson(localSetLists);
        mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings", "", localSetsFile, setsJson);
    }


    private String jsonNullIfEmpty(String string) {
        return string==null || string.isEmpty() ? null : string;
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

}
