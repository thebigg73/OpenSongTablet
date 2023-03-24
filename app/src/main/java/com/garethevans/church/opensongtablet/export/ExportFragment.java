package com.garethevans.church.opensongtablet.export;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.print.PrintManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsExportBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExportFragment extends Fragment {

    private SettingsExportBinding myView;
    private MainActivityInterface mainActivityInterface;
    private final String TAG = "ExportFragment";
    private ArrayList<Uri> uris;
    private ArrayList<String> mimeTypes, setNames;
    private ArrayList<View> sectionViewsPDF = new ArrayList<>();
    private ArrayList<Integer> sectionViewWidthsPDF = new ArrayList<>(), sectionViewHeightsPDF = new ArrayList<>();
    private LinearLayout headerLayoutPDF;
    private int headerLayoutWidth, headerLayoutHeight, songsToAdd, songsProcessed;
    private String setToExport = null, exportType, shareTitle, textContent, setContent, pngName,
            export_string="", website_export_set_string="", website_export_song_string="",
            set_string="", song_string="", app_name_string="";
    private boolean openSong = false, currentFormat = false, openSongApp = false, pdf = false, image = false,
            png = false, chordPro = false, onsong = false, text = false, setPDF = false, openSongSet = false,
            setPNG = false, openSongAppSet = false, includeSongs = false, textSet = false, isPrint;
    private String[] location, setData, ids, setKeys;
    private StringBuilder songsAlreadyAdded;
    private Handler handler;
    private float scaleComments;
    private Bitmap setPNGContent;
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
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
        myView = SettingsExportBinding.inflate(inflater,container,false);

        prepareStrings();

        webAddress = export_string;

        mainActivityInterface.getProcessSong().updateProcessingPreferences();
        scaleComments = mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleComments",0.8f);

        // Empty the export folder
        mainActivityInterface.getStorageAccess().wipeFolder("Export","");

        // If we are exporting a song, we do this once, if not, we do it for each song in the set
        if (mainActivityInterface.getWhattodo().startsWith("exportset:")) {
            setToExport = mainActivityInterface.getWhattodo().replace("exportset:", "").replace("%_%","");
            setNames = mainActivityInterface.getExportActions().getListOfSets(setToExport);
            setData = mainActivityInterface.getExportActions().parseSets(setNames);
            textContent = setData[1];
            setContent = setData[1];
            Log.d(TAG,"setData[0]:"+setData[0]);
            Log.d(TAG,"setData[1]:"+setData[1]);
            Log.d(TAG,"setData[2]:"+setData[2]);

        } else {
            setContent = null;
        }

        uris = new ArrayList<>();
        mimeTypes = new ArrayList<>();

        // Some options are hidden by default and only visible if we have a proper OpenSong song
        // If exporting a set, some options aren't allowed
        showUsable();

        myView.shareButton.setOnClickListener(v -> {
            isPrint = false;
            prepareExport();
        });
        myView.print.setOnClickListener(v -> {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                handler = new Handler(Looper.getMainLooper());

                isPrint = true;
                if (mainActivityInterface.getWhattodo().startsWith("exportset:")) {
                    doPrint(true);
                } else {
                    if (sectionViewsPDF == null || sectionViewsPDF.size() == 0) {
                        createOnTheFly(mainActivityInterface.getSong(), mainActivityInterface.getSong().getFilename() + ".pdf");
                    } else {
                        doPrint(false);
                    }
                }
            });
        });

        myView.nestedScrollView.setExtendedFabToAnimate(myView.shareButton);
        myView.nestedScrollView.setExtendedFab2ToAnimate(myView.print);

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            export_string = getString(R.string.export);
            website_export_set_string = getString(R.string.website_export_set);
            website_export_song_string = getString(R.string.website_export_song);
            set_string = getString(R.string.set);
            song_string = getString(R.string.song);
            app_name_string = getString(R.string.app_name);
        }
    }

    private void showUsable() {
        // By default everything is hidden.  Only make the correct ones visible

        // Set the defaults for set export
        myView.setPDF.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportSetPDF",false));
        myView.openSongAppSet.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOpenSongAppSet",false));
        myView.openSongSet.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOpenSongSet",true));
        myView.textSet.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOpenSongTextSet",false));
        myView.setPNG.setChecked(false);

        // Set the defaults for song export
        myView.currentFormat.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportCurrentFormat",true));
        myView.pdf.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportPDF",false));
        myView.image.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportPNG",false));
        myView.openSongApp.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOpenSongApp",false));
        myView.openSong.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportDesktop",false));
        myView.onSong.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOnSong",false));
        myView.chordPro.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportChordPro",false));
        myView.text.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportText",false));

        // Set the listeners for preferences
        myView.setPDF.setOnCheckedChangeListener(new MyCheckChanged("exportSetPDF"));
        myView.openSongAppSet.setOnCheckedChangeListener(new MyCheckChanged("exportOpenSongAppSet"));
        myView.openSongSet.setOnCheckedChangeListener(new MyCheckChanged("exportOpenSongSet"));
        myView.textSet.setOnCheckedChangeListener(new MyCheckChanged("exportOpenSongTextSet"));
        myView.setPNG.setOnCheckedChangeListener(new MyCheckChanged("exportSetPNG"));
        myView.currentFormat.setOnCheckedChangeListener(new MyCheckChanged("exportCurrentFormat"));
        myView.pdf.setOnCheckedChangeListener(new MyCheckChanged("exportPDF"));
        myView.image.setOnCheckedChangeListener(new MyCheckChanged("exportPNG"));
        myView.openSongApp.setOnCheckedChangeListener(new MyCheckChanged("exportOpenSongApp"));
        myView.openSong.setOnCheckedChangeListener(new MyCheckChanged("exportDesktop"));
        myView.onSong.setOnCheckedChangeListener(new MyCheckChanged("exportOnSong"));
        myView.chordPro.setOnCheckedChangeListener(new MyCheckChanged("exportChordPro"));
        myView.text.setOnCheckedChangeListener(new MyCheckChanged("exportText"));

        // Make sure the progress info is hidden to start with
        myView.scrim.setVisibility(View.GONE);
        myView.progressText.setVisibility(View.GONE);

        // Check if we are exporting a set
        if (mainActivityInterface.getWhattodo().startsWith("exportset:")) {
            webAddress = website_export_set_string;

            myView.setExportInfo.setVisibility(View.VISIBLE);
            myView.currentFormat.setVisibility(View.VISIBLE);
            myView.openSong.setVisibility(View.GONE);

            // Include the songs views
            myView.includeSongs.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportSetSongs",true));
            myView.includeSongs.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mainActivityInterface.getPreferences().setMyPreferenceBoolean("exportSetSongs",isChecked);
                showHideSongOptions(isChecked);
            });
            showHideSongOptions(myView.includeSongs.isChecked());

            // If we are exporting songs, hide the ones not allowed for sets
            myView.image.setVisibility(View.GONE);
            myView.image.setChecked(false);

            // Now show the set view
            myView.setOptionsLayout.setVisibility(View.VISIBLE);

        } else {
            // Hide the options based on the song format
            webAddress = website_export_song_string;
            myView.setExportInfo.setVisibility(View.GONE);
            myView.currentFormat.setVisibility(View.GONE);

            if (mainActivityInterface.getSong().getFiletype().equals("IMG") ||
            mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                myView.chordPro.setVisibility(View.GONE);
                myView.text.setVisibility(View.GONE);
                myView.onSong.setVisibility(View.GONE);
                myView.openSong.setVisibility(View.GONE);
                myView.openSongApp.setVisibility(View.GONE);
                myView.text.setVisibility(View.GONE);
            }

            switch (mainActivityInterface.getSong().getFiletype()) {
                case "PDF":
                    myView.image.setVisibility(View.GONE);
                    myView.pdf.setChecked(true);
                    break;
                case "IMG":
                    myView.image.setChecked(true);
                    break;
                case "XML":
                    // Must be a song!
                    myView.openSongApp.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOpenSongApp", false));
                    myView.openSong.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportDesktop", false));
                    break;
            }

            // Now show the song options
            myView.songOptionsLayout.setVisibility(View.VISIBLE);
        }
    }

    private class MyCheckChanged implements CompoundButton.OnCheckedChangeListener {

        private final String pref;
        MyCheckChanged(String pref) {
            this.pref = pref;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (pref!=null) {
                mainActivityInterface.getPreferences().setMyPreferenceBoolean(pref,isChecked);
            }
        }
    }

    private void showHideSongOptions(boolean show) {
        if (show) {
            myView.songOptionsLayout.setVisibility(View.VISIBLE);
        } else {
            myView.songOptionsLayout.setVisibility(View.GONE);
        }
    }

    private void prepareExport() {
        // This process the export options.

        // First decide which formats we want to include based on the checkboxes
        // Sets
        setPDF = myView.setPDF.isChecked();
        openSongSet = myView.openSongSet.isChecked();
        openSongAppSet = myView.openSongAppSet.isChecked();
        textSet = myView.textSet.isChecked();
        includeSongs = myView.includeSongs.isChecked();
        setPNG = myView.setPNG.isChecked();

        // Songs
        pdf = myView.pdf.isChecked();
        image = myView.image.isChecked();
        openSongApp = myView.openSongApp.isChecked();
        openSong = myView.openSong.isChecked();
        onsong = myView.onSong.isChecked();
        chordPro = myView.chordPro.isChecked();
        text = myView.text.isChecked();
        currentFormat = myView.currentFormat.isChecked();

        myView.scrim.setVisibility(View.VISIBLE);
        myView.progressText.setVisibility(View.VISIBLE);
        myView.progressText.setHint("");
        myView.shareButton.setEnabled(false);
        myView.shareButton.hide();
        myView.print.setEnabled(false);
        myView.print.hide();

        // Deal with set exporting
        if (mainActivityInterface.getWhattodo().startsWith("exportset:")) {
            exportType = set_string;
            shareTitle = setToExport;
            doExportSet();
        } else {
            exportType = song_string;
            shareTitle = mainActivityInterface.getSong().getFilename();
            doExportSong();
        }
    }

    private void doExportSet() {
        // Do this in a new Thread
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            handler = new Handler(Looper.getMainLooper());

            // Load in the sets
            // First up add the simple set files that don't require drawing pdfs
            // Add the .osts file
            if (openSongAppSet) {
                // Copy the set(s) to an .osts file extensions and get the uri(s)
                uris.addAll(mainActivityInterface.getExportActions().addOpenSongAppSetsToUris(setNames));
                if (!mimeTypes.contains("text/xml")) {
                    mimeTypes.add("text/xml");
                }
            }

            // Add the desktop set file (no extension)
            ArrayList<Uri> setFiles = mainActivityInterface.getExportActions().addOpenSongSetsToUris(setNames);

            // Go through the set and create any custom slides required (variations, slides, etc).
            for (Uri setFile:setFiles) {
                mainActivityInterface.getSetActions().extractSetFile(setFile,true);
            }

            if (openSongSet) {
                // Just add the actual set file(s) (no extension)
                uris.addAll(setFiles);
                if (!mimeTypes.contains("text/xml")) {
                    mimeTypes.add("text/xml");
                }
            }

            setContent = setData[1];
            // Prepare a text version of the set
            if (textSet || includeSongs || setPDF) {
                if (textSet) {
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doExportSet doStringWriteToFile Export/"+setToExport+".txt with: "+setData[1]);
                    mainActivityInterface.getStorageAccess().doStringWriteToFile(
                            "Export", "", setToExport + ".txt", setData[1]);
                    if (textSet) {
                        uris.add(mainActivityInterface.getStorageAccess().getUriForItem("Export", "", setToExport + ".txt"));
                        if (!mimeTypes.contains("text/plain")) {
                            mimeTypes.add("text/plain");
                        }
                    }
                }
            }

            // Now we go through the songs if we want to include them (not pdf rendered yet though)
            ids = setData[0].split("\n");
            setKeys = setData[2].split("\n");

            if (includeSongs) {
                songsAlreadyAdded = new StringBuilder();
                songsToAdd = ids.length;
                songsProcessed = 0;

                for (int x=0; x<songsToAdd; x++) {
                    String id = ids[x];
                    Log.d(TAG,"id:"+id);

                    // Only add if we don't already have it (as we may have multiple references to
                    // songs in sets, especially is we have selected more than one set)
                    location = mainActivityInterface.getExportActions().getFolderAndFile(id);
                    updateProgressText(location[1],x+1,ids.length);
                    songsProcessed++;

                    if (!songsAlreadyAdded.toString().contains(id)) {
                        songsAlreadyAdded.append("\n").append(id);
                        boolean likelyXML = !location[1].contains(".") || location[1].toLowerCase(Locale.ROOT).endsWith(".xml");
                        boolean likelyPDF = location[1].toLowerCase(Locale.ROOT).endsWith(".pdf");

                        // If this is a variation, etc. load it.
                        // Otherwise, get from the database
                        Song song;
                        if (location[0].contains("../") || location[0].contains("**")) {
                            song = new Song();
                            song.setFolder(location[0].replace("../","**"));
                            song.setFolder("../Export");
                            song.setFilename(location[1]);
                            song = mainActivityInterface.getLoadSong().doLoadSongFile(song,false);
                            Log.d(TAG,"song:"+song.getFolder()+"/"+song.getFilename()+"  lyrics:"+song.getLyrics());
                        } else {
                            song = mainActivityInterface.getSQLiteHelper().getSpecificSong(location[0], location[1]);
                        }

                        // If we have transposed this song in the set on the fly, match the key here
                        boolean useTransposed = false;
                        if (!setKeys[x].equals("ignore") && !setKeys[x].trim().isEmpty() && song.getKey()!=null && !song.getKey().isEmpty() &&
                                !setKeys[x].trim().equals(song.getKey())) {
                            int transposeTimes = mainActivityInterface.getTranspose().getTransposeTimes(song.getKey(),setKeys[x].trim());
                            mainActivityInterface.getTranspose().checkChordFormat(song);
                            song = mainActivityInterface.getTranspose().doTranspose(song,"+1",transposeTimes,song.getDetectedChordFormat(),song.getDesiredChordFormat());
                            song.setFilename(song.getFilename()+"__"+setKeys[x]);
                            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Export", "",song.getFilename());
                            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,uri,null,"Export","",song.getFilename());
                            mainActivityInterface.getProcessSong().getXML(song);
                            mainActivityInterface.getStorageAccess().doStringWriteToFile("Export","",song.getFilename(),song.getSongXML());
                            uris.add(uri);
                            Log.d(TAG,"adding "+uri);
                            if (!mimeTypes.contains("text/xml")) {
                                mimeTypes.add("text/xml");
                            }
                            useTransposed = true;
                        }

                        // Sharing a song should initiate the CCLI Log of printed (value 6)
                        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("ccliAutomaticLogging",false)) {
                            mainActivityInterface.getCCLILog().addEntry(song,"6");
                        }

                        // Deal with the currentFormat option first, or PDF for PDF songs
                        if (!id.equals("ignore") && (currentFormat && !useTransposed) || (pdf && likelyPDF)) {
                            // Just get a uri for the song
                            uris.add(mainActivityInterface.getStorageAccess().getUriForItem("Songs", location[0], location[1]));
                            if (openSong && !mimeTypes.contains("text/xml")) {
                                mimeTypes.add("text/xml");
                            } else if (pdf && !mimeTypes.contains("application/pdf")) {
                                mimeTypes.add("application/pdf");
                            }
                        }

                        // Add OpenSongApp files
                        if (!id.equals("ignore") && openSongApp && likelyXML) {
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doExportSet CopyFromTo Songs/"+location[0]+"/"+location[1]+" to Export/"+location[1]+".ost");
                            if (useTransposed) {
                                uris.add(mainActivityInterface.getStorageAccess().copyFromTo(
                                        "Export", "",song.getFilename(),
                                        "Export", "", song.getFilename() + ".ost"));
                            } else {
                                uris.add(mainActivityInterface.getStorageAccess().copyFromTo(
                                        "Songs", location[0], location[1],
                                        "Export", "", location[1] + ".ost"));
                            }
                            if (!mimeTypes.contains("text/xml")) {
                                mimeTypes.add("text/xml");
                            }
                        }

                        // Add onSong files
                        if (!id.equals("ignore") && onsong && likelyXML) {
                            // Get the text from the file
                            String content = mainActivityInterface.getPrepareFormats().getSongAsOnSong(song);
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doExportSet doStringWriteToFile Export/"+location[1]+".onsong with: "+content);
                            if (mainActivityInterface.getStorageAccess().doStringWriteToFile("Export", "", location[1] + ".onsong", content)) {
                                uris.add(mainActivityInterface.getStorageAccess().getUriForItem("Export", "", location[1] + ".onsong"));
                                if (!mimeTypes.contains("text/plain")) {
                                    mimeTypes.add("text/plain");
                                }
                            }
                        }

                        // Add chordPro songs
                        if (!id.equals("ignore") && chordPro && likelyXML) {
                            // Get the text from the file
                            String content = mainActivityInterface.getPrepareFormats().getSongAsChoPro(song);
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doExportSet doStringWriteToFile Export/"+location[1]+".cho with: "+content);
                            if (mainActivityInterface.getStorageAccess().doStringWriteToFile("Export", "", location[1] + ".cho", content)) {
                                uris.add(mainActivityInterface.getStorageAccess().getUriForItem("Export", "", location[1] + ".cho"));
                                if (!mimeTypes.contains("text/plain")) {
                                    mimeTypes.add("text/plain");
                                }
                            }
                        }

                        // Add text songs
                        if (!id.equals("ignore") && text && likelyXML) {
                            // Get the text from the file
                            String content = mainActivityInterface.getPrepareFormats().getSongAsText(song);
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doExportSet doStringWriteToFile Export/"+location[1]+".txt with: "+content);
                            if (mainActivityInterface.getStorageAccess().doStringWriteToFile("Export", "", location[1] + ".txt", content)) {
                                uris.add(mainActivityInterface.getStorageAccess().getUriForItem("Export", "", location[1] + ".txt"));
                                if (!mimeTypes.contains("text/plain")) {
                                    mimeTypes.add("text/plain");
                                }
                            }
                        }

                    }
                }
            }
            if ((includeSongs && pdf) || setPDF || setPNG) {
                // We need to render PDFs which get drawn and added one at a time
                // From here on we need to be on the UI thread (ouch!)
                handler.post(this::renderPDFSet);
            } else {
                initiateShare();
            }

        });
    }

    private void renderPDFSet() {
        songsProcessed = 0;

        if (setPDF || setPNG) {
            mainActivityInterface.getMakePDF().setIsSetListPrinting(true);
            Song tempSong = new Song();
            tempSong.setTitle(setToExport);
            String[] items = setData[1].split("\n");
            StringBuilder setItems = new StringBuilder();
            for (String setItemEntry : items) {
                setItems.append(setItemEntry).append("\n[]\n");
            }
            tempSong.setLyrics(setItems.toString());
            createOnTheFly(tempSong,set_string +" " +setToExport+".pdf");
        } else {
            renderPDFSongs();
        }
    }

    private void renderPDFSongs() {
        mainActivityInterface.getMakePDF().setIsSetListPrinting(false);
        // Go through the songs if we are adding them as pdfs until we have processed all
        Log.d(TAG,"songsProcessed:"+songsProcessed);
        if (ids==null) {
            ids = new String[1];
        }
        Log.d(TAG,"ids.length:"+ids.length);
        Log.d(TAG,"includeSongs:"+includeSongs);
        Log.d(TAG,"pdf:"+pdf);
        Log.d(TAG,"songsToAdd:"+songsToAdd);


        if (songsProcessed>=ids.length || !includeSongs || !pdf || songsProcessed==songsToAdd) {
            initiateShare();

        } else {
            String id = ids[songsProcessed];
            String key = setKeys[songsProcessed];
            location = mainActivityInterface.getExportActions().getFolderAndFile(id);
            boolean likelyXML = !location[1].contains(".") || location[1].toLowerCase(Locale.ROOT).endsWith(".xml");

            // Only add if we haven't already added it
            Log.d(TAG,"id:"+id);
            if (likelyXML && !songsAlreadyAdded.toString().contains(id+".pdf") && !id.equals("ignore")) {
                songsAlreadyAdded.append("\n").append(id);
                updateProgressText(location[1]+".pdf", songsProcessed + 1, ids.length);
                Song song;
                if (location[0].contains("../") || location[0].contains("**")) {
                    song = new Song();
                    //song.setFolder(location[0]);
                    song.setFolder("../Export");
                    song.setFilename(location[1]);
                    song = mainActivityInterface.getLoadSong().doLoadSongFile(song,false);
                } else {
                    song = mainActivityInterface.getSQLiteHelper().getSpecificSong(location[0], location[1]);
                    // If we have transposed this song in the set on the fly, match the key here
                    if (!key.isEmpty() && song.getKey()!=null && !song.getKey().isEmpty() &&
                            !key.equals(song.getKey())) {
                        int transposeTimes = mainActivityInterface.getTranspose().getTransposeTimes(song.getKey(),key);
                        mainActivityInterface.getTranspose().checkChordFormat(song);
                        song = mainActivityInterface.getTranspose().doTranspose(song,"+1",transposeTimes,song.getDetectedChordFormat(),song.getDesiredChordFormat());
                        song.setFilename(song.getFilename()+"__"+key);
                        location[1] = location[1]+"__"+key;
                    }
                }
                createOnTheFly(song,location[1]+".pdf");
            } else if (id.equals("ignore")) {
                songsProcessed++;
                renderPDFSongs();
            }
            songsProcessed++;
        }
    }
    private void doExportSong() {
        // Do this in a new Thread
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            handler = new Handler(Looper.getMainLooper());

            // Sharing a song should initiate the CCLI Log of printed (value 6)
            if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("ccliAutomaticLogging",false)) {
                mainActivityInterface.getCCLILog().addEntry(mainActivityInterface.getSong(),"6");
            }

            String filename = mainActivityInterface.getSong().getFilename();
            String folder = mainActivityInterface.getSong().getFolder();
            String filetype = mainActivityInterface.getSong().getFiletype();
            boolean isPDF = filetype.equals("PDF");
            boolean isXML = filetype.equals("XML");
            boolean isIMG = filetype.equals("IMG");

            if (isXML) {
                textContent = mainActivityInterface.getPrepareFormats().getSongAsText(mainActivityInterface.getSong());
            } else {
                textContent = mainActivityInterface.getSong().getFilename();
            }

            if (pdf && isPDF || openSong && isXML || image && isIMG) {
                // Just add the xml or pdf song
                uris.add(mainActivityInterface.getStorageAccess().getUriForItem("Songs",
                        folder, filename));
                if (pdf && !mimeTypes.contains("application/pdf")) {
                    mimeTypes.add("application/pdf");
                } else if (openSong && !mimeTypes.contains("text/plain")) {
                    mimeTypes.add("text/plain");
                } else if (image && !mimeTypes.contains("image/*")) {
                    mimeTypes.add("image/*");
                }
            }

            if (openSongApp && isXML) {
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doExportSong CopyFromTo Songs/"+folder+"/"+filename+" to Export/"+filename+".ost");
                uris.add(mainActivityInterface.getStorageAccess().copyFromTo("Songs",
                        folder, filename, "Export", "", filename + ".ost"));
                if (!mimeTypes.contains("text/plain")) {
                    mimeTypes.add("text/plain");
                }
            }

            if (onsong && isXML) {
                String content = mainActivityInterface.getPrepareFormats().getSongAsOnSong(mainActivityInterface.getSong());
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doExportSong doStringWriteToFile Export/"+filename+".onsong with: "+content);
                if (mainActivityInterface.getStorageAccess().doStringWriteToFile("Export", "",
                        filename + ".onsong", content)) {
                    uris.add(mainActivityInterface.getStorageAccess().getUriForItem("Export", "",
                            filename + ".onsong"));
                    if (!mimeTypes.contains("text/plain")) {
                        mimeTypes.add("text/plain");
                    }
                }
            }

            if (chordPro && isXML) {
                String content = mainActivityInterface.getPrepareFormats().getSongAsChoPro(mainActivityInterface.getSong());
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doExportSong doStringWriteToFile Export/"+filename+".cho with: "+content);
                if (mainActivityInterface.getStorageAccess().doStringWriteToFile("Export", "",
                        filename + ".cho", content)) {
                    uris.add(mainActivityInterface.getStorageAccess().getUriForItem("Export", "",
                            filename + ".cho"));
                    if (!mimeTypes.contains("text/plain")) {
                        mimeTypes.add("text/plain");
                    }
                }
            }

            if (text && isXML) {
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doExportSong doStringWriteToFile Export/"+filename+".txt with: "+textContent);
                if (mainActivityInterface.getStorageAccess().doStringWriteToFile("Export", "",
                        filename + ".txt", textContent)) {
                    uris.add(mainActivityInterface.getStorageAccess().getUriForItem("Export", "",
                            filename + ".txt"));
                    if (!mimeTypes.contains("text/plain")) {
                        mimeTypes.add("text/plain");
                    }
                }
            }

            // If we are exporting an XML song as an image, it gets converted to a pdf first then
            // Put back as an image and then screenshotted!
            png = image && isXML;

            if ((pdf && isXML) || png) {
                // Create PDF song on the fly and only initiate share once done
                createOnTheFly(mainActivityInterface.getSong(),mainActivityInterface.getSong().getFilename()+".pdf");
            } else {
                // No pdf processing required, so initiate the share
                initiateShare();
            }
        });
    }

    private void initiateShare() {
        for (Uri uri:uris) {
            Log.d(TAG,"sharing:"+uri);
        }
        Intent intent = mainActivityInterface.getExportActions().setShareIntent(textContent,"*/*",null,uris);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.putExtra(Intent.EXTRA_SUBJECT, app_name_string + " " +
                exportType + ": " + shareTitle);
        if (setContent!=null) {
            intent.putExtra(Intent.EXTRA_TEXT, setContent);
        } else {
            intent.putExtra(Intent.EXTRA_TEXT, textContent);
        }

        handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            startActivity(Intent.createChooser(intent, shareTitle));
            myView.scrim.setVisibility(View.GONE);
            myView.progressText.setVisibility(View.GONE);
            myView.shareButton.setEnabled(true);
            myView.shareButton.show();
            myView.print.setEnabled(true);
            myView.print.show();
            uris = new ArrayList<>();
        });
    }

    // We can create nice views on the fly here by processing the Song, then populating the views
    private void createOnTheFly(Song thisSong, String pdfName) {
        // Make sure any current headers/sections are wiped
        handler.post(()-> {
            try {
                initialiseViews();

                // Now start preparing the views.  First up the header
                createOnTheFlyHeader(thisSong, pdfName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private void initialiseViews() {
        myView.hiddenHeader.removeAllViews();
        myView.hiddenSections.removeAllViews();
        sectionViewsPDF = new ArrayList<>();
        sectionViewWidthsPDF = new ArrayList<>();
        sectionViewHeightsPDF = new ArrayList<>();
        if (getContext()!=null) {
            headerLayoutPDF = new LinearLayout(getContext());
        }
    }

    public void createOnTheFlyHeader(Song thisSong, String pdfName) {
        Log.d(TAG,"setPNG:"+setPNG+"  pdfName:"+pdfName+"   ==   Set "+setToExport+".pdf");

        // Get the song sheet header
        // Once this has drawn, move to the next stage of the song sections

        ViewTreeObserver headerVTO = myView.hiddenHeader.getViewTreeObserver();
        headerVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                myView.hiddenHeader.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                headerLayoutWidth = myView.hiddenHeader.getMeasuredWidth();
                headerLayoutHeight = myView.hiddenHeader.getMeasuredHeight();
                if (!pdfName.equals(set_string +" " +setToExport+".pdf") && !setPNG && !png) {
                    myView.hiddenHeader.removeAllViews();
                }
                createOnTheFlySections(thisSong, pdfName);
            }
        });
        // Now draw it here for measuring via the VTO
        headerLayoutPDF = mainActivityInterface.getSongSheetHeaders().getSongSheet(thisSong,
                scaleComments, true);
        myView.hiddenHeader.addView(headerLayoutPDF);
    }
    public void createOnTheFlySections(Song thisSong, String pdfName) {
        // If we don't have any sections in the song, change the double line breaks into sections
        if (!thisSong.getLyrics().contains("\n[")) {
            String[] lines = thisSong.getLyrics().split("\n");
            StringBuilder stringBuilder = new StringBuilder();
            for (String line:lines) {
                if (line.trim().isEmpty()) {
                    stringBuilder.append("[]\n");
                } else {
                    stringBuilder.append(line).append("\n");
                }
            }
            thisSong.setLyrics(stringBuilder.toString());
        }

        // Create the content for the section views.
        sectionViewsPDF = mainActivityInterface.getProcessSong().
                setSongInLayout(thisSong,true, false);

        // Now we have the views, add them to the temp layout and set up a view tree listener to measure
        ViewTreeObserver sectionsVTO = myView.hiddenSections.getViewTreeObserver();
        sectionsVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // The views are ready so lets measure them after clearing this listener
                // If all the views are there, we can start measuring
                if (myView.hiddenSections.getChildCount()==sectionViewsPDF.size()) {
                    myView.hiddenSections.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int maxWidth = 0;
                    for (int x=0; x<myView.hiddenSections.getChildCount(); x++) {
                        View view = myView.hiddenSections.getChildAt(x);
                        int width = view.getMeasuredWidth();
                        int height = view.getMeasuredHeight();
                        sectionViewWidthsPDF.add(width);
                        sectionViewHeightsPDF.add(height);
                        if (width>maxWidth) {
                            maxWidth = width;
                        }
                    }

                    boolean isSetFile = pdfName.equals(set_string +" " +setToExport+".pdf");

                    Log.d(TAG,"setPNG:"+setPNG+"  pdfName:"+pdfName+"   ==   Set "+setToExport+".pdf");
                    // If we are exporting a setPNG and this is the set, take a bitmap!
                    if (isSetFile && setPNG) {
                        try {
                            // The header should still be in place
                            // Now take a bitmap of the layout
                            setPNGContent = Bitmap.createBitmap(maxWidth, myView.previewLayout.getHeight(), Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(setPNGContent);
                            myView.previewLayout.draw(canvas);
                            Log.d(TAG, "bitmap  width:" + setPNGContent.getWidth() + "  height:" + setPNGContent.getHeight());
                            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Export","",pdfName.replace(".pdf",".png"));
                            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,uri,null,"Export","",pdfName.replace(".pdf",".png"));
                            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
                            mainActivityInterface.getStorageAccess().writeImage(outputStream,setPNGContent);
                            if (uris==null) {
                                uris = new ArrayList<>();
                            }
                            uris.add(uri);
                            // Remove the header
                            myView.hiddenHeader.removeAllViews();

                            // Check the bitmap is released/cleared
                            if (setPNGContent!=null) {
                                setPNGContent.recycle();
                            }

                        } catch (OutOfMemoryError | Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Now trigger the next step of preparing the pdf from the views created on the fly
                    if (uris==null) {
                        uris = new ArrayList<>();
                    }
                    if (mimeTypes==null) {
                        mimeTypes = new ArrayList<>();
                    }

                    if (png) {
                        // Now take a bitmap of the layout
                        // Get the maximum width of the views
                        myView.previewLayout.setVisibility(View.VISIBLE);
                        setPNGContent = Bitmap.createBitmap(myView.previewLayout.getWidth(), myView.previewLayout.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(setPNGContent);
                        myView.previewLayout.draw(canvas);
                        pngName = pdfName.replace(".pdf",".png");
                        Log.d(TAG, "pngName:" + pngName + "  bitmap  width:" + setPNGContent.getWidth() + "  height:" + setPNGContent.getHeight());
                        Log.d(TAG,"children:"+myView.previewLayout.getChildCount());
                        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Export", "", pngName);
                        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, uri, null, "Export", "", pngName);
                        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
                        mainActivityInterface.getStorageAccess().writeImage(outputStream, setPNGContent);
                        uris.add(uri);
                        myView.previewLayout.setVisibility(View.INVISIBLE);
                        myView.hiddenHeader.removeAllViews();
                    }

                    // Now detach from this view (can only be shown in one layout)
                    myView.hiddenSections.removeAllViews();

                    // If we wanted a pdf (rather than png), add it
                    Log.d(TAG,"setPDF:"+setPDF+"  isSetFile:"+isSetFile);

                    if (setPDF || (!isSetFile && pdf)) {
                        uris.add(mainActivityInterface.getMakePDF().createTextPDF(
                                sectionViewsPDF, sectionViewWidthsPDF,
                                sectionViewHeightsPDF, headerLayoutPDF,
                                headerLayoutWidth, headerLayoutHeight,
                                pdfName, null));

                        if (!pdf) {
                            // Remove that uri as it was only created for an image screenshot
                            uris.remove(uris.size()-1);
                        } else {
                            if (!mimeTypes.contains("application/pdf")) {
                                mimeTypes.add("application/pdf");
                            }
                        }
                    }
                    if (isPrint) {
                        // We have exported a song as a print layout
                        doPrint(false);
                    } else {
                        renderPDFSongs();
                    }
                }
            }
        });

        // Add the section views and this will trigger the VTO
        for (int x=0; x<sectionViewsPDF.size(); x++) {
            myView.hiddenSections.addView(sectionViewsPDF.get(x));
        }
    }

    private void updateProgressText(String text, int curr, int total) {
        String progressText = text + " (" + curr + "/" + total + ")";
        myView.progressText.post(() -> myView.progressText.setText(progressText));
    }

    private void doPrint(boolean isSet) {
        // Get a PrintManager instance
        if (getActivity()!=null) {
            PrintManager printManager = (PrintManager) getActivity().getSystemService(Context.PRINT_SERVICE);

            // Set job name, which will be displayed in the print queue
            String jobName = app_name_string + " Document";

            // Start a print job, passing in a PrintDocumentAdapter implementation
            // to handle the generation of a print document

            if (isSet) {
                // Set the variable that will remove gaps from set items on the set list page(s)
                mainActivityInterface.getMakePDF().setIsSetListPrinting(true);

                // Go through the sets and create any custom slides required (variations, slides, etc).
                ArrayList<Uri> setFiles = mainActivityInterface.getExportActions().addOpenSongSetsToUris(setNames);
                for (Uri setFile : setFiles) {
                    mainActivityInterface.getSetActions().extractSetFile(setFile, true);
                }

                // This is sent to the MultipagePrinterAdapter class to deal with
                MultipagePrinterAdapter multipagePrinterAdapter = new MultipagePrinterAdapter(getActivity());
                multipagePrinterAdapter.updateSetList(this, setToExport, setData[0], setData[1], setData[2]);
                mainActivityInterface.getMakePDF().setPreferedAttributes();
                printManager.print(jobName, multipagePrinterAdapter, mainActivityInterface.getMakePDF().getPrintAttributes());

            } else {
                PrinterAdapter printerAdapter = new PrinterAdapter(getActivity());
                printerAdapter.updateSections(sectionViewsPDF, sectionViewWidthsPDF, sectionViewHeightsPDF,
                        headerLayoutPDF, headerLayoutWidth, headerLayoutHeight, song_string);
                mainActivityInterface.getMakePDF().setPreferedAttributes();
                printManager.print(jobName, printerAdapter, mainActivityInterface.getMakePDF().getPrintAttributes());
            }
        }
    }

    // Getters
    public ArrayList<View> getSectionViews() {
        return sectionViewsPDF;
    }
    public ArrayList<Integer> getSectionWidths() {
        return sectionViewWidthsPDF;
    }
    public ArrayList<Integer> getSectionHeights() {
        return sectionViewHeightsPDF;
    }
    public LinearLayout getHeaderLayout() {
        return headerLayoutPDF;
    }
    public void setHeaderLayoutPDF(LinearLayout headerLayoutPDF) {
        this.headerLayoutPDF = headerLayoutPDF;
    }
    public LinearLayout getHiddenHeader() {
        return myView.hiddenHeader;
    }
    public LinearLayout getHiddenSections() {
        return myView.hiddenSections;
    }
    public void resetSectionViews() {
        sectionViewsPDF = new ArrayList<>();
        sectionViewWidthsPDF = new ArrayList<>();
        sectionViewHeightsPDF = new ArrayList<>();
    }
}
