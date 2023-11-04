package com.garethevans.church.opensongtablet.beatbuddy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsBeatbuddyImportBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BBImportFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "BBImportFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsBeatbuddyImportBinding myView;
    private String beat_buddy_import_project="", website_beatbuddy_import="", success_string="",
            beat_buddy_import_error="", song_string="", kit_string="", error_string="";
    private BBSQLite bbsqLite;
    private ArrayList<Integer> folder_nums, song_nums, kit_nums;
    private ArrayList<String> folder_codes, folder_names, song_names, kit_names, kit_codes, song_codes;
    private ActivityResultLauncher<Intent> importCSVLauncher;
    private ActivityResultLauncher<Intent> folderChooser;
    private String webAddress;
    private Uri previousCSV;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        bbsqLite = new BBSQLite(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(beat_buddy_import_project);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsBeatbuddyImportBinding.inflate(inflater, container, false);

        prepareStrings();

        // Check if previous file exists
        checkPrevious();

        webAddress = website_beatbuddy_import;

        // Set up launcher
        setupLauncher();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void checkPrevious() {
        previousCSV = mainActivityInterface.getStorageAccess().getUriForItem("Settings","","MyBeatBuddyProject.csv");
        if (mainActivityInterface.getStorageAccess().uriExists(previousCSV)) {
            myView.importPrev.setVisibility(View.VISIBLE);
        } else {
            myView.importPrev.setVisibility(View.GONE);
            previousCSV = null;
        }
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            beat_buddy_import_project = getString(R.string.beat_buddy_import_project);
            website_beatbuddy_import = getString(R.string.website_beatbuddy_import);
            beat_buddy_import_error = getString(R.string.beat_buddy_import_error);
            song_string = getString(R.string.song);
            kit_string = getString(R.string.drum_kit);
            error_string = getString(R.string.error);
            success_string = getString(R.string.success);
        }
    }

    private void setupLauncher() {
        // Initialise the launchers
        importCSVLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                try {
                    Intent data = result.getData();
                    if (data != null) {
                        doImport(data.getData());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        folderChooser = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
                        Uri sdCard = result.getData().getData();
                        if (getContext()!=null) {
                            getContext().getContentResolver().takePersistableUriPermission(sdCard,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            DocumentFile df = DocumentFile.fromTreeUri(getContext(),sdCard);
                            if (df!=null) {
                                sdCard = df.getUri();
                            }
                        }

                        myView.importBBProgress.post(() -> myView.importBBProgress.setVisibility(View.VISIBLE));

                        // Do this on a new thread
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        Uri finalSdCard = sdCard;
                        executorService.execute(() -> {
                            String drumKits = getConfigCSVText(finalSdCard,"DRUMSETS");
                            String songFolders = getConfigCSVText(finalSdCard,"SONGS");
                            if (songFolders!=null && drumKits!=null) {
                                makeCSVFILE(drumKits, songFolders, finalSdCard);
                            }
                            myView.importBBProgress.post(() -> myView.importBBProgress.setVisibility(View.GONE));
                        });
                    }
                });
    }

    private void setupListeners() {
        myView.importCSV.setOnClickListener(view -> selectFile());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myView.browseSD.setOnClickListener(view -> selectDirectory());
        }
        myView.importPrev.setOnClickListener(view -> {
            if (previousCSV!=null) {
                doImport(previousCSV);
            }
        });
    }

    private void doImport(Uri uri) {
        myView.importBBProgress.post(() -> myView.importBBProgress.setVisibility(View.VISIBLE));

        // Do this on a new thread
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
            String content = mainActivityInterface.getStorageAccess().readTextFileToString(inputStream);
            mainActivityInterface.setImportUri(uri);

            processMyBeatBuddyProject(content);
            myView.importBBProgress.post(() -> myView.importBBProgress.setVisibility(View.GONE));
        });
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("text/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        importCSVLauncher.launch(intent);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void selectDirectory() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        // IV - 'Commented in' this extra to try to always show internal and sd card storage
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        folderChooser.launch(intent);
    }

    private String getConfigCSVText(Uri sdCard, String folder) {
        // Now we have permissions to view the SD card, we open the SONGS/config.csv file
        Uri uri = getValidUriOrNull(sdCard,folder,null);
        if (uri!=null) {
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
            return mainActivityInterface.getStorageAccess().readTextFileToString(inputStream);
        } else {
            return null;
        }
    }

    private void makeCSVFILE(String drumkits, String folders, Uri sdCard) {
        // Now we have permissions to view the SD card, we open the SONGS/XXXX/CONFIG.CSV file
        clearArrays();

        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder newCSVText = new StringBuilder();

        newCSVText.append("\"SONG_CODE\",")
                .append("\"SONG_NUM\",")
                .append("\"SONG_NAME\",")
                .append("\"FOLDER_CODE\",")
                .append("\"FOLDER_NUM\",")
                .append("\"FOLDER_NAME\",")
                .append("\"MIDI_CODE\"\n");

        // Process the SONGS/config.csv file content
        String[] folderLines = folders.split("\n");
        if (folderLines.length>0) {
            for (String folderLine:folderLines) {
                String[] folderBits = folderLine.split(",");
                // We should now have an array of code,name
                if (folderBits.length>=2) {
                    folderBits[1] = folderBits[1].replace(". ","___");
                    String[] foldernameandnum = folderBits[1].split("___");
                    int folderNum = Integer.parseInt(foldernameandnum[0].replaceAll("\\D",""));
                    String folderName = foldernameandnum[1];
                    String folderCode = folderBits[0];
                    // Now we need to open the config.csv in this SONGS folder
                    Uri configUri = getValidUriOrNull(sdCard,"SONGS",folderCode);
                    InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(configUri);
                    String folderContent = mainActivityInterface.getStorageAccess().readTextFileToString(inputStream);
                    if (folderContent!=null && !folderContent.isEmpty()) {
                        // Now we have the folder contents, let's parse it
                        String[] lines = folderContent.split("\n");
                        if (lines.length>0) {
                            for (String line:lines) {
                                // Go through each line like 2D07AA59.BBS,1. Ska
                                // Extract the number and the name
                                String[] bits = line.split(",");
                                if (bits.length>=2) {
                                    bits[1] = bits[1].replace(". ","___");
                                    String[] numname = bits[1].split("___");
                                    if (numname.length>=2) {
                                        int songNum = Integer.parseInt(numname[0].replaceAll("\\D",""));
                                        String songName = numname[1];
                                        String songCode = bits[0];
                                        String midiCode = mainActivityInterface.getBeatBuddy().getSongCode(folderNum,songNum).trim();
                                        // Now add to the array and the result
                                        folder_codes.add(folderCode);
                                        folder_nums.add(folderNum);
                                        folder_names.add(folderName);
                                        song_nums.add(songNum);
                                        song_names.add(songName);
                                        song_codes.add(songCode);
                                        String thisSong = song_string+": "+folderNum+". "+folderName+" ("+folderCode+") - "+songNum+". "+songName+" ("+songCode+")";
                                        stringBuilder.append(thisSong).append("\n");
                                        newCSVText.append("\"").append(songCode)
                                                .append("\",\"").append(songNum)
                                                .append("\",\"").append(songName)
                                                .append("\",\"").append(folderCode)
                                                .append("\",\"").append(folderNum)
                                                .append("\",\"").append(folderName)
                                                .append("\",\"").append(midiCode)
                                                .append("\"\n");

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Process the drumkits
        if (drumkits!=null) {
            newCSVText.append("\"___DRUMKITS___\"\n")
                    .append("\"KIT_CODE\",")
                    .append("\"KIT_NUM\",")
                    .append("\"KIT_NAME\",")
                    .append("\"MIDI_CODE\"\n");

            String[] kits = drumkits.split("\n");
            for (String kit : kits) {
                // Split by the comma to get the code and then num and name
                String[] kitinfo = kit.split(",");
                if (kitinfo.length >= 2) {
                    kitinfo[1] = kitinfo[1].replace(". ","___");
                    String[] kitbits = kitinfo[1].split("___");
                    if (kitbits.length >= 2) {
                        String kitCode = kitinfo[0];
                        int kitNum = Integer.parseInt(kitbits[0].replaceAll("\\D", ""));
                        String kitName = kitbits[1];
                        String midiCode = mainActivityInterface.getBeatBuddy().getDrumKitCode(kitNum).trim();
                        kit_nums.add(kitNum);
                        kit_names.add(kitName);
                        kit_codes.add(kitCode);
                        newCSVText.append("\"").append(kitCode)
                                .append("\",\"").append(kitNum)
                                .append("\",\"").append(kitName)
                                .append("\",\"").append(midiCode)
                                .append("\"\n");

                        String thisKit = kit_string+": "+kitNum+". "+kitName+" ("+kitCode+")";
                        stringBuilder.append(thisKit).append("\n");
                    }
                }
            }
        }

        if (!stringBuilder.toString().isEmpty()) {
            bbsqLite.clearMySongs();
            bbsqLite.addMySongs(song_codes, song_nums, song_names, folder_codes, folder_nums, folder_names);
            bbsqLite.clearMyDrums();
            bbsqLite.addMyDrumKits(kit_nums, kit_names, kit_codes);
            myView.outcome.setText(success_string);
            myView.outcome.setHint(stringBuilder.toString());
            // Write the file
            if (getContext()!=null) {
                DocumentFile sdCardFile = DocumentFile.fromTreeUri(getContext(), sdCard);
                if (sdCardFile != null) {
                    DocumentFile newCSV = sdCardFile.findFile("MyBeatBuddyProject.csv");
                    if (newCSV==null) {
                        newCSV = sdCardFile.createFile("text/csv", "MyBeatBuddyProject");
                    }
                    if (newCSV != null) {
                        Uri csvFileUri = newCSV.getUri();
                        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(csvFileUri);
                        mainActivityInterface.getStorageAccess().writeFileFromString(newCSVText.toString(), outputStream);
                    }
                }
            }
            mainActivityInterface.getShowToast().doIt(success_string);
            mainActivityInterface.getBeatBuddy().setBeatBuddyUseImported(true);
        } else {
            myView.outcome.setText(error_string);
            myView.outcome.setHint(beat_buddy_import_error);
            mainActivityInterface.getShowToast().doIt(error_string);
            mainActivityInterface.getBeatBuddy().setBeatBuddyUseImported(false);
        }
    }

    private Uri getValidUriOrNull(Uri sdCard, String folder, String subfolder) {
        if (getContext() != null) {
            DocumentFile dfSdCard = DocumentFile.fromTreeUri(getContext(), sdCard);
            if (dfSdCard != null) {
                DocumentFile dfFolder = dfSdCard.findFile(folder);
                if (dfFolder != null) {
                    if (subfolder!=null && !subfolder.isEmpty()) {
                        dfFolder = dfFolder.findFile(subfolder);
                    }
                    if (dfFolder != null) {
                        DocumentFile dfLower = dfFolder.findFile("config.csv");
                        DocumentFile dfUpper = dfFolder.findFile("CONFIG.CSV");
                        Uri lowerUri = null;
                        Uri upperUri = null;
                        if (dfLower != null) {
                            lowerUri = dfLower.getUri();
                        }
                        if (dfUpper != null) {
                            upperUri = dfUpper.getUri();
                        }
                        if (lowerUri != null && mainActivityInterface.getStorageAccess().uriExists(lowerUri)) {
                            return lowerUri;
                        } else if (upperUri != null && mainActivityInterface.getStorageAccess().uriExists(upperUri)) {
                            return upperUri;
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private void processMyBeatBuddyProject(String content) {
        // We now have the MyBeatBuddyProject.csv file
        // First up, split up into the two parts - songs and drums
        boolean error = content==null || content.isEmpty();
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder newCSVText = new StringBuilder();
        String cachedContent = content;
        if (!error) {
            content = content.replace("\n\"___DRUMKITS___\"\n","___SPLITHERE___");
            String[] bits = content.split("___SPLITHERE___");
            if (bits.length==2) {
                // Split the song parts up by line and then add them to the database
                String[] songEntries = bits[0].trim().split("\"\n");
                if (songEntries.length>1) {
                    // Each line will be in the format of:
                    // "SONG_CODE","SONG_NUM","SONG_NAME","FOLDER_CODE","FOLDER_NUM","FOLDER_NAME","MIDI_CODE"
                    // We don't need the MIDI_CODE as we generate in app to allow channel change
                    // We start at index=1 as index=0 is the csv headers above
                    clearArrays();
                    newCSVText.append("\"SONG_CODE\",\"SONG_NUM\",\"SONG_NAME\",\"FOLDER_CODE\",\"FOLDER_NUM\",\"FOLDER_NAME\",\"MIDI_CODE\"").append("\n");
                    for (int x=1; x<songEntries.length; x++) {
                        String[] songArray = songEntries[x].split(",");
                        if (songArray.length==7) {
                            try {
                                String SONG_CODE = songArray[0].replace("\"","");
                                int SONG_NUM = Integer.parseInt(songArray[1].replace("\"","").replaceAll("\\D", ""));
                                String SONG_NAME = songArray[2].replace("\"","");
                                String FOLDER_CODE = songArray[3].replace("\"","");
                                int FOLDER_NUM = Integer.parseInt(songArray[4].replace("\"","").replaceAll("\\D", ""));
                                String FOLDER_NAME = songArray[5].replace("\"","");
                                //String MIDI_CODE = songArray[6];

                                // We've got the values, so add to the arrays
                                folder_nums.add(FOLDER_NUM);
                                folder_codes.add(FOLDER_CODE);
                                folder_names.add(FOLDER_NAME);
                                song_nums.add(SONG_NUM);
                                song_names.add(SONG_NAME);
                                song_codes.add(SONG_CODE);

                                String thisSong = song_string+": "+FOLDER_NUM+". "+FOLDER_NAME+" ("+FOLDER_CODE+") - "+SONG_NUM+". "+SONG_NAME+" ("+SONG_CODE+")";
                                stringBuilder.append(thisSong).append("\n");
                            } catch (Exception e) {
                                e.printStackTrace();
                                error = true;
                            }
                        } else {
                            error = true;
                        }
                    }
                } else {
                    error = true;
                }
                // Split the drum info up by line and then add them to the database
                String[] kitEntries = bits[1].trim().split("\n");
                if (kitEntries.length>1) {
                    // Each line will be in the format of:
                    // "KIT_CODE","KIT_NUM","KIT_NAME","MIDI_CODE"
                    // We don't need the MIDI_CODE as we generate in app to allow channel change
                    // We start at index=1 as index=0 is the csv headers above

                    newCSVText.append("\"KIT_CODE\",\"KIT_NUM\",\"KIT_NAME\",\"MIDI_CODE\"").append("\n");
                    for (int x = 1; x < kitEntries.length; x++) {
                        String[] kitArray = kitEntries[x].split(",");
                        if (kitArray.length == 4) {
                            try {
                                String KIT_CODE = kitArray[0].replace("\"","");
                                int KIT_NUM = Integer.parseInt(kitArray[1].replace("\"","").replaceAll("\\D", ""));
                                String KIT_NAME = kitArray[2].replace("\"","");
                                //String MIDI_CODE = kitArray[3].replace("\"","");
                                kit_nums.add(KIT_NUM);
                                kit_names.add(KIT_NAME);
                                kit_codes.add(KIT_CODE);
                                String thisKit = kit_string+": "+KIT_NUM+". "+KIT_NAME+" ("+KIT_CODE+")";
                                stringBuilder.append(thisKit).append("\n");

                            } catch (Exception e) {
                                e.printStackTrace();
                                error = true;
                            }
                        } else {
                            error = true;
                        }
                    }
                } else {
                    error = true;
                }
            } else {
                error = true;
            }
        }

        if (error) {
            myView.outcome.setText(error_string);
            myView.outcome.setHint(beat_buddy_import_error);
            mainActivityInterface.getShowToast().doIt(error_string);
            mainActivityInterface.getBeatBuddy().setBeatBuddyUseImported(false);
        } else {
            bbsqLite.clearMySongs();
            bbsqLite.clearMyDrums();
            bbsqLite.addMySongs(song_codes,song_nums,song_names,folder_codes,folder_nums,folder_names);
            bbsqLite.addMyDrumKits(kit_nums, kit_names, kit_codes);
            myView.outcome.setText(success_string);
            myView.outcome.setHint(stringBuilder.toString());
            mainActivityInterface.getShowToast().doIt(success_string);
            mainActivityInterface.getBeatBuddy().setBeatBuddyUseImported(true);
            // Make a copy in the OpenSong/Settings/ folder for future callback
            mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings","","MyBeatBuddyProject.csv",cachedContent);
        }
    }

    private void clearArrays() {
        folder_nums = new ArrayList<>();
        folder_codes = new ArrayList<>();
        folder_names = new ArrayList<>();
        song_nums = new ArrayList<>();
        song_names = new ArrayList<>();
        song_codes = new ArrayList<>();
        kit_nums = new ArrayList<>();
        kit_names = new ArrayList<>();
        kit_codes = new ArrayList<>();
    }
}
