package com.garethevans.church.opensongtablet.export;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.print.PrintManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.abcnotation.InlineAbcObject;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsExportBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.slider.LabelFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class ExportFragment extends Fragment {

    private SettingsExportBinding myView;
    private MainActivityInterface mainActivityInterface;
    private final String TAG = "ExportFragment";
    private ArrayList<Uri> uris, urisSetBundle;
    private ArrayList<String> mimeTypes, setNames;
    private ArrayList<View> sectionViewsPDF = new ArrayList<>(), sectionViewsScreenshot = new ArrayList<>();
    private ArrayList<Integer> sectionViewWidthsPDF = new ArrayList<>(), sectionViewHeightsPDF = new ArrayList<>();
    private ArrayList<Integer> sectionViewWidthsScreenshot = new ArrayList<>(), sectionViewHeightsScreenshot = new ArrayList<>();
    private LinearLayout headerLayoutPDF;
    private int headerLayoutWidth, headerLayoutHeight, songsToAdd, songsProcessed;
    private String setToExport = null, exportType, shareTitle, textContent, setContent, pngName,
            export_string="", website_export_set_string="", website_export_song_string="",
            set_string="", song_string="", app_name_string="", screenshot_string="", toolBarTitle="",
            mode_performance_string="", merged_text_file_string="";
    private boolean openSong = false, currentFormat = false, openSongApp = false, pdf = false,
            image = false, png = false, chordPro = false, justChords = false, onsong = false,
            text = false, merged = false, setPDF = false, openSongSet = false, setPNG = false,
            openSongAppSet = false, includeSongs = false, textSet = false, isPrint,
            setPDFDone = false, setPNGDone = false, screenShot = false, openSongSetBundle=true,
            justChordsSetBundle=false;
    private String[] location, setData, ids, setKeys;
    private StringBuilder songsAlreadyAdded;
    private float scaleComments;
    private Bitmap setPNGContent;
    private String webAddress;
    private String default_string="", dark_string="", light_string="",
            custom1_string="", custom2_string="", pdf_print_string="";
    private StringBuilder combinedSetText;
    private String currentSongFolder, currentSongFile;
    private Song tempSong;
    private Song thisSong;
    private String pdfName;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(toolBarTitle);
        mainActivityInterface.updateToolbarHelp(webAddress);
        resetSectionViews();
        if (currentSongFolder!=null && currentSongFile!=null) {
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                mainActivityInterface.getSong().setFolder(currentSongFolder);
                mainActivityInterface.getSong().setFilename(currentSongFile);
                mainActivityInterface.getLoadSong().doLoadSong(mainActivityInterface.getSong(), false);
            });
        }

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        mainActivityInterface.getSongSheetHeaders().setForExport(true);
        mainActivityInterface.getAbcNotation().setExportFragment(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsExportBinding.inflate(inflater,container,false);

        mainActivityInterface.getProcessSong().setMakingImageOrScreenShot(false);
        mainActivityInterface.getProcessSong().setMakingScaledScreenShot(false);

        // Tint the progress text box
        mainActivityInterface.getMyThemeColors().tintPopup(myView.progressText);

        // Get a note of the currently loaded song so we can force it back onDestroy()
        currentSongFolder = mainActivityInterface.getSong().getFolder();
        currentSongFile = mainActivityInterface.getSong().getFilename();

        // Clear the PDF printing
        mainActivityInterface.getProcessSong().setPdfPrinting(false);

        prepareStrings();

        webAddress = export_string;

        // Empty the export folder - this is a backup location (for user manual access)
        // The files shared are places in the app private files/ folder and are shared via the FileProvider
        mainActivityInterface.getStorageAccess().wipeFolder("Export","");
        File exportFolder = mainActivityInterface.getStorageAccess().getAppSpecificFile("Export","",null);
        File[] filesInExportFolder = exportFolder.listFiles();
        if (filesInExportFolder!=null) {
            for (File fileInExportFolder : filesInExportFolder) {
                // DON'T REMOVE!!  Deletes the file
                Log.d(TAG, "Deleted file " + fileInExportFolder.getName() + ": " + fileInExportFolder.delete());
            }
        }

        mainActivityInterface.getProcessSong().updateProcessingPreferences();
        scaleComments = mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleComments",0.8f);

        // If we are exporting a song, we do this once, if not, we do it for each song in the set
        if (mainActivityInterface.getWhattodo().startsWith("exportset:")) {
            setToExport = mainActivityInterface.getWhattodo().replace("exportset:", "").
                    replace(mainActivityInterface.getSetActions().getItemStart(),"").
                    replace(mainActivityInterface.getSetActions().getItemEnd(),"");
            setNames = mainActivityInterface.getExportActions().getListOfSets(setToExport);
            setData = mainActivityInterface.getExportActions().parseSets(setNames);
            myView.exportTextAsMessage.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportTextAsMessage",true));
            textContent = setData[1];
            setContent = setData[1];
            toolBarTitle = export_string+" ("+set_string+")";
        } else {
            toolBarTitle = export_string+" ("+song_string+")";
            setContent = null;
        }

        uris = new ArrayList<>();
        urisSetBundle = new ArrayList<>();
        mimeTypes = new ArrayList<>();

        // Some options are hidden by default and only visible if we have a proper OpenSong song
        // If exporting a set, some options aren't allowed
        showUsable();

        myView.shareButton.setOnClickListener(v -> {
            isPrint = false;
            prepareExport();
        });
        myView.print.setOnClickListener(v -> mainActivityInterface.getThreadPoolExecutor().execute(() -> {

            isPrint = true;
            mainActivityInterface.getProcessSong().setPdfPrinting(true);
            mainActivityInterface.getProcessSong().setForceSinglePagePDF(myView.forceSinglePage.getChecked());
            if (mainActivityInterface.getWhattodo().startsWith("exportset:")) {
                doPrint(true);
            } else {
                tempSong = mainActivityInterface.getSong();
                doPrint(false);
            }
        }));
        myView.maxPDFScaling.addOnChangeListener((slider, value, fromUser) -> updatePDFMaxScaling((int)value));

        myView.exportTextAsMessage.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getPreferences().setMyPreferenceBoolean("exportTextAsMessage",b));
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
            screenshot_string = getString(R.string.screenshot).toLowerCase();
            mode_performance_string = getString(R.string.mode_performance);
            ArrayList<String> pdfThemes = new ArrayList<>();
            default_string = getString(R.string.use_default);
            dark_string = getString(R.string.theme_dark);
            light_string = getString(R.string.theme_light);
            custom1_string = getString(R.string.theme_custom1);
            custom2_string = getString(R.string.theme_custom2);
            pdfThemes.add(default_string);
            pdfThemes.add(dark_string);
            pdfThemes.add(light_string);
            pdfThemes.add(custom1_string);
            pdfThemes.add(custom2_string);
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),myView.pdfTheme,R.layout.view_exposed_dropdown_item, pdfThemes);
            myView.pdfTheme.setAdapter(exposedDropDownArrayAdapter);
            pdf_print_string = getString(R.string.theme)+ ": " + getString(R.string.pdf) + "/" + getString(R.string.print);
            merged_text_file_string = getString(R.string.merged_text_file);
        }
    }

    private void showUsable() {
        // By default everything is hidden.  Only make the correct ones visible

        // Set the defaults for set export
        myView.setBundle.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportSetBundle",true));
        myView.justChordsBundle.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportSetBundleJustChords",false));
        myView.setPDF.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportSetPDF",false));
        myView.openSongAppSet.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOpenSongAppSet",false));
        myView.openSongSet.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOpenSongSet",false));
        myView.textSet.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOpenSongTextSet",false));
        myView.setPNG.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportSetPNG",false));

        // Set the defaults for song export
        myView.currentFormat.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportCurrentFormat",false));
        myView.pdf.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportPDF",false));
        myView.image.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportPNG",false));
        myView.openSongApp.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOpenSongApp",false));
        myView.openSong.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportDesktop",false));
        myView.justChords.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportJustChords",false));
        myView.onSong.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOnSong",false));
        myView.chordPro.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportChordPro",false));
        myView.text.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportText",false));
        myView.merged.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportMergedText",false));
        myView.screenShot.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportScreenshot",false));

        // Set the pdf/print theme
        myView.maxPDFScaling.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return (int)value+"%";
            }
        });
        setMaxPDFScaling();
        myView.forceSinglePage.setChecked(mainActivityInterface.getMakePDF().getForceSinglePage());
        myView.forceSinglePage.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getMakePDF().setForceSinglePage(b));
        myView.pdfTheme.setHint(pdf_print_string);
        myView.pdfTheme.setText(getPDFThemeText());
        myView.pdfTheme.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                setPDFTheme();
            }
        });

        // Set the listeners for preferences
        myView.setBundle.setOnCheckedChangeListener(new MyCheckChanged("exportSetBundle"));
        myView.justChordsBundle.setOnCheckedChangeListener(new MyCheckChanged("exportSetBundleJustChords"));
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
        myView.justChords.setOnCheckedChangeListener(new MyCheckChanged("exportJustChords"));
        myView.onSong.setOnCheckedChangeListener(new MyCheckChanged("exportOnSong"));
        myView.chordPro.setOnCheckedChangeListener(new MyCheckChanged("exportChordPro"));
        myView.text.setOnCheckedChangeListener(new MyCheckChanged("exportText"));
        myView.merged.setOnCheckedChangeListener(new MyCheckChanged("exportMergedText"));
        myView.screenShot.setOnCheckedChangeListener(new MyCheckChanged("exportScreenshot"));

        // Make sure the progress info is hidden to start with
        myView.scrim.setVisibility(View.GONE);
        myView.progressText.setVisibility(View.GONE);

        // Check if we are exporting a set
        if (mainActivityInterface.getWhattodo().startsWith("exportset:")) {
            webAddress = website_export_set_string;

            myView.setOptionsLayout.setVisibility(View.VISIBLE);
            myView.setExportInfo.setVisibility(View.VISIBLE);
            myView.currentFormat.setVisibility(View.VISIBLE);
            myView.openSong.setVisibility(View.GONE);
            //myView.screenShot.setVisibility(View.GONE);

            // Include the songs views
            myView.includeSongs.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportSetSongs",false));
            myView.includeSongs.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mainActivityInterface.getPreferences().setMyPreferenceBoolean("exportSetSongs",isChecked);
                showHideSongOptions(isChecked);
            });
            showHideSongOptions(myView.includeSongs.getChecked());

            // Now show the set view
            myView.setOptionsLayout.setVisibility(View.VISIBLE);

        } else {
            // Hide the options based on the song format
            webAddress = website_export_song_string;
            myView.setOptionsLayout.setVisibility(View.GONE);
            myView.setExportInfo.setVisibility(View.GONE);
            myView.currentFormat.setVisibility(View.GONE);
            myView.merged.setVisibility(View.GONE);

            if (mainActivityInterface.getSong()!=null &&
                    mainActivityInterface.getSong().getFiletype()!=null &&
                    (mainActivityInterface.getSong().getFiletype().equals("IMG") ||
            mainActivityInterface.getSong().getFiletype().equals("PDF"))) {
                myView.chordPro.setVisibility(View.GONE);
                myView.text.setVisibility(View.GONE);
                myView.onSong.setVisibility(View.GONE);
                myView.openSong.setVisibility(View.GONE);
                myView.openSongApp.setVisibility(View.GONE);
                myView.text.setVisibility(View.GONE);
                myView.screenShot.setVisibility(View.GONE);
            }

            if (mainActivityInterface.getSong()!=null &&
                    mainActivityInterface.getSong().getFiletype()!=null) {
                switch (mainActivityInterface.getSong().getFiletype()) {
                    case "PDF":
                        myView.image.setVisibility(View.GONE);
                        myView.pdf.setCheckBox(true);
                        break;
                    case "IMG":
                        myView.image.setCheckBox(true);
                        break;
                    case "XML":
                        // Must be a song!
                        myView.openSongApp.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOpenSongApp", false));
                        myView.openSong.setCheckBox(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportDesktop", false));
                        break;
                }
            }

            // Now show the song options
            myView.songOptionsLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setMaxPDFScaling() {
        // Get the user preference and convert to an int 10-200
        int maxScaling = Math.round(100f * mainActivityInterface.getMakePDF().getMaxPDFScaling());
        myView.maxPDFScaling.setValue(maxScaling);
        myView.maxPDFScaling.setHint(maxScaling + "%");
    }

    private void updatePDFMaxScaling(int value) {
        // Convert to an int and sent it to the MakePDF (where it is saved)
        mainActivityInterface.getMakePDF().setMaxPDFScaling((float)value/100f);
        myView.maxPDFScaling.setHint(value+"%");
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

    private String getPDFThemeText() {
        String theme = default_string;
        if (mainActivityInterface!=null && mainActivityInterface.getMyThemeColors()!=null) {
            switch (mainActivityInterface.getMyThemeColors().getPdfTheme()) {
                case "default":
                default:
                    theme = default_string;
                    break;
                case "dark":
                    theme = dark_string;
                    break;
                case "light":
                    theme = light_string;
                    break;
                case "custom1":
                    theme = custom1_string;
                    break;
                case "custom2":
                    theme = custom2_string;
                    break;
            }
        }
        return theme;
    }

    private void setPDFTheme() {
        if (myView!=null && myView.pdfTheme.getText()!=null) {
            String pref;
            String theme = myView.pdfTheme.getText().toString();
            if (theme.equals(dark_string)) {
                pref = "dark";
            } else if (theme.equals(light_string)) {
                pref = "light";
            } else if (theme.equals(custom1_string)) {
                pref = "custom1";
            } else if (theme.equals(custom2_string)) {
                pref = "custom2";
            } else {
                pref = "default";
            }
            // Save the preference and build the theme
            mainActivityInterface.getMyThemeColors().updatePDFTheme(pref,true);
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

        // Set bundles
        openSongSetBundle = myView.setBundle.isChecked();
        justChordsSetBundle = myView.justChordsBundle.isChecked();
        if (openSongSetBundle && getContext() != null) {
            // Initialise the helper
            mainActivityInterface.getOpenSongSetBundle().setProgressText(myView.progressText);
        }

        // Sets (separate files)
        setPDF = myView.setPDF.isChecked();
        openSongSet = myView.openSongSet.isChecked();
        openSongAppSet = myView.openSongAppSet.isChecked();
        textSet = myView.textSet.isChecked();
        includeSongs = myView.includeSongs.getChecked();
        setPNG = myView.setPNG.isChecked();

        if (setContent == null || myView.includeSongs.getChecked()) {
            // Songs (separate files)
            pdf = myView.pdf.isChecked();
            image = myView.image.isChecked();
            openSongApp = myView.openSongApp.isChecked();
            openSong = myView.openSong.isChecked();
            justChords = myView.justChords.isChecked();
            onsong = myView.onSong.isChecked();
            chordPro = myView.chordPro.isChecked();
            text = myView.text.isChecked();
            merged = myView.merged.isChecked();
            currentFormat = myView.currentFormat.isChecked();
            screenShot = myView.screenShot.isChecked();

        } else {
            pdf = false;
            image = false;
            openSongApp = false;
            openSong = false;
            justChords = false;
            onsong = false;
            chordPro = false;
            text = false;
            merged = false;
            currentFormat = false;
            screenShot = false;
        }

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
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Keep all songs in a combined string for additional export
            combinedSetText = new StringBuilder();

            mainActivityInterface.getConvertJustChords().resetVariables();

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
            if (openSongSetBundle) {
                urisSetBundle.addAll(mainActivityInterface.getExportActions().addOpenSongAppSetsToUris(setNames));
            }

            // Go through the set and create any custom slides required (variations, slides, etc).
            ArrayList<Uri> setFiles = mainActivityInterface.getExportActions().addOpenSongSetsToUris(setNames);
            for (Uri setFile:setFiles) {
                mainActivityInterface.getSetActions().extractSetFile(setFile,mainActivityInterface.getCurrentSet(),true);
            }


            // Add the desktop set file (no extension)
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
                    mainActivityInterface.getStorageAccess().writeFileFromString(
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

            if (includeSongs || openSongSetBundle || justChordsSetBundle) {
                songsAlreadyAdded = new StringBuilder();
                songsToAdd = ids.length;
                songsProcessed = 0;

                for (int x=0; x<songsToAdd; x++) {
                    String id = ids[x];

                    // Only add if we don't already have it (as we may have multiple references to
                    // songs in sets, especially is we have selected more than one set)
                    location = mainActivityInterface.getExportActions().getFolderAndFile(id);
                    updateProgressText(location[1],x+1,ids.length);
                    songsProcessed++;

                    if (!songsAlreadyAdded.toString().contains(id)) {
                        songsAlreadyAdded.append("\n").append(id);
                        boolean likelyXML = !location[1].contains(".") || location[1].toLowerCase(Locale.ROOT).endsWith(".xml");
                        boolean likelyPDF = location[1].toLowerCase(Locale.ROOT).endsWith(".pdf");
                        boolean likelyIMG = mainActivityInterface.getStorageAccess().filenameIsImage(location[1]);

                        // If this is a variation, etc. load it.
                        // Otherwise, get from the database
                        Song song;
                        if (location[0].contains("../") || location[0].contains("**")) {
                            song = new Song();
                            song.setFolder(location[0].replace("../","**"));
                            song.setFolder("../Export");
                            song.setFilename(location[1]);
                            song = mainActivityInterface.getLoadSong().doLoadSongFile(song,false);
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
                            float fileSize = mainActivityInterface.getStorageAccess().getFileSizeFromUri(uri);
                            mainActivityInterface.getProcessSong().getXML(song);
                            mainActivityInterface.getStorageAccess().writeFileFromString("Export", "", song.getFilename(), song.getSongXML());
                            if (includeSongs && fileSize>0) {
                                uris.add(uri);
                            }
                            if (!mimeTypes.contains("text/xml")) {
                                mimeTypes.add("text/xml");
                            }
                            if (openSongSetBundle) {
                                urisSetBundle.add(uri);
                            }
                            useTransposed = true;
                        }

                        // Sharing a song should initiate the CCLI Log of printed (value 6)
                        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("ccliAutomaticLogging",false)) {
                            mainActivityInterface.getCCLILog().addEntry(song,"6");
                        }

                        // Deal with the currentFormat option first, or PDF or IMG songs
                        if (!id.equals("ignore") && ((currentFormat && !useTransposed) ||
                                ((openSongSetBundle || pdf) && likelyPDF) ||
                                ((openSongSetBundle || image) && likelyIMG))) {
                            // Just get a uri for the song
                            if (song.getFolder()!=null && song.getFolder().equals("../Export")) {
                                location[0] = "../Export";
                            }
                            Uri uricopied = mainActivityInterface.getStorageAccess().copyFromTo(
                                    "Songs", fixSubfolder(location[0]), location[1],
                                    "Export", "", location[1]);
                            if (includeSongs && ((pdf && likelyPDF) || (image && likelyIMG))) {
                                uris.add(uricopied);
                            }
                            if (openSongSetBundle &&
                                    mainActivityInterface.getStorageAccess().isIMGorPDF(location[1])) {
                                Uri uricopiedWithFolder = mainActivityInterface.getStorageAccess().copyFromTo(
                                        "Songs", fixSubfolder(location[0]), location[1],
                                        "Export", "", location[0] +
                                                mainActivityInterface.getSetActions().getSetCategorySeparator() +
                                                location[1]);
                                urisSetBundle.add(uricopiedWithFolder);
                            }

                            if (openSong && !mimeTypes.contains("text/xml")) {
                                mimeTypes.add("text/xml");
                            } else if (pdf && likelyPDF && !mimeTypes.contains("application/pdf")) {
                                mimeTypes.add("application/pdf");
                            } else if (image && likelyIMG && !mimeTypes.contains("image/*")) {
                                mimeTypes.add("image/*");
                            }
                        }

                        // Add OpenSongApp files
                        if (!id.equals("ignore") && likelyXML &&
                                (openSongApp || openSongSetBundle)) {
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doExportSet CopyFromTo Songs/"+location[0]+"/"+location[1]+" to Export/"+location[1]+".ost");
                            if (useTransposed) {
                                Uri uricopied = mainActivityInterface.getStorageAccess().copyFromTo(
                                        "Export", "",song.getFilename(),
                                        "Export", "", song.getFilename() + ".ost");
                                if (openSongSetBundle) {
                                    Uri uricopiedWithFolder = mainActivityInterface.getStorageAccess().copyFromTo(
                                            "Export", "", song.getFilename(),
                                            "Export", "", song.getFolder() +
                                                    mainActivityInterface.getSetActions().getSetCategorySeparator() +
                                                    song.getAutoscrolllength() + ".ost");
                                    urisSetBundle.add(uricopiedWithFolder);
                                }

                                if (openSongApp) {
                                    uris.add(uricopied);
                                }

                            } else {
                                Uri uricopied = mainActivityInterface.getStorageAccess().copyFromTo(
                                        "Songs", fixSubfolder(location[0]), location[1],
                                        "Export", "", location[1] + ".ost");
                                if (openSongSetBundle) {
                                    Uri uricopiedWithFolder = mainActivityInterface.getStorageAccess().copyFromTo(
                                            "Songs", fixSubfolder(location[0]), location[1],
                                            "Export", "", location[0] +
                                                    mainActivityInterface.getSetActions().getSetCategorySeparator() +
                                                    location[1] + ".ost");
                                    urisSetBundle.add(uricopiedWithFolder);
                                }
                                if (openSongApp) {
                                    uris.add(uricopied);
                                }
                            }
                            if (!mimeTypes.contains("text/xml")) {
                                mimeTypes.add("text/xml");
                            }
                        }

                        // Add justChord files
                        if (!id.equals("ignore") && (justChords || justChordsSetBundle) && likelyXML) {
                            // If we want the single song, add it
                            if (justChords) {
                                Uri uri = mainActivityInterface.getConvertJustChords().buildJustChordsSingleSong(song);
                                if (uri!=null) {
                                    uris.add(uri);
                                }
                                if (!mimeTypes.contains("text/plain")) {
                                    mimeTypes.add("text/plain");
                                }
                            }
                            // If we want a set bundle, add the song to the class - deal with it at the end
                            if (justChordsSetBundle) {
                                mainActivityInterface.getConvertJustChords().addOpenSongToArray(song);
                            }
                        }

                        // Add onSong files
                        if (!id.equals("ignore") && onsong && likelyXML) {
                            // Get the text from the file
                            String content = mainActivityInterface.getPrepareFormats().getSongAsOnSong(song);
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doExportSet doStringWriteToFile Export/"+location[1]+".onsong with: "+content);
                            if (mainActivityInterface.getStorageAccess().writeFileFromString("Export", "", location[1] + ".onsong", content)) {
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
                            if (mainActivityInterface.getStorageAccess().writeFileFromString("Export", "", location[1] + ".cho", content)) {
                                uris.add(mainActivityInterface.getStorageAccess().getUriForItem("Export", "", location[1] + ".cho"));
                                if (!mimeTypes.contains("text/plain")) {
                                    mimeTypes.add("text/plain");
                                }
                            }
                        }

                        // Add text songs
                        if (!id.equals("ignore") && (text||merged) && likelyXML) {
                            // Get the text from the file
                            String content = mainActivityInterface.getPrepareFormats().getSongAsText(song);
                            if (merged) {
                                combinedSetText.append(content).append("\n\n");
                            }
                            if (text) {
                                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " doExportSet doStringWriteToFile Export/" + location[1] + ".txt with: " + content);
                                if (mainActivityInterface.getStorageAccess().writeFileFromString("Export", "", location[1] + ".txt", content)) {
                                    uris.add(mainActivityInterface.getStorageAccess().getUriForItem("Export", "", location[1] + ".txt"));
                                    if (!mimeTypes.contains("text/plain")) {
                                        mimeTypes.add("text/plain");
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (openSongSetBundle && !urisSetBundle.isEmpty()) {
                exportOpenSongSetBundle();
                if (!mimeTypes.contains("application/zip")) {
                    mimeTypes.add("application/zip");
                    mimeTypes.add("application/x-zip");
                    mimeTypes.add("application/octet-stream");
                }
            }

            if (justChordsSetBundle) {
                Uri uri = mainActivityInterface.getConvertJustChords().buildJustChordsSet(setToExport);
                if (uri!=null) {
                    uris.add(uri);
                    if (!mimeTypes.contains("text/plain")) {
                        mimeTypes.add("text/plain");
                    }
                }
            }

            if ((includeSongs && (pdf || png || image || screenShot)) || setPDF || setPNG) {
                // We need to render PDFs which get drawn and added one at a time
                // From here on we need to be on the UI thread (ouch!)
                mainActivityInterface.getMainHandler().post(this::renderPDFSet);
            } else {
                initiateShare();
            }
        });
    }

    private Uri createMergedTextFile() {
        // If exporting songs in text format, they will also be merged
        Uri uri = null;
        if (merged && !combinedSetText.toString().isEmpty()) {
            uri = mainActivityInterface.getStorageAccess().getUriForItem("Export","",merged_text_file_string+".txt");
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,uri,null,"Export","",merged_text_file_string+".txt");
            mainActivityInterface.getStorageAccess().writeFileFromString("Export","",merged_text_file_string+".txt",combinedSetText.toString());
        }
        return uri;
    }

    private String fixSubfolder(String subfolder) {
        if (subfolder.contains("../")) {
            subfolder = subfolder.replace("../Notes","../Notes/_cache");
            subfolder = subfolder.replace("../Scripture","../Scripture/_cache");
            subfolder = subfolder.replace("../Slides","../Slides/_cache");
            if (subfolder.contains("Variation")) {
                subfolder = mainActivityInterface.getVariations().getKeyVariationsFolder();
            }
            subfolder = subfolder.replace("/_cache/_cache","/_cache");
        }
        return subfolder;
    }

    private void renderPDFSet() {
        mainActivityInterface.getProcessSong().setPdfPrinting(true);
        mainActivityInterface.getProcessSong().setForceSinglePagePDF(myView.forceSinglePage.getChecked());

        songsProcessed = 0;
        setPDFDone = false;
        setPNGDone = false;
        if (setPDF || setPNG) {
            mainActivityInterface.getMakePDF().setIsSetListPrinting(true);
            tempSong = new Song();
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
        mainActivityInterface.getProcessSong().setPdfPrinting(true);
        mainActivityInterface.getProcessSong().setForceSinglePagePDF(myView.forceSinglePage.getChecked());
        mainActivityInterface.getMakePDF().setIsSetListPrinting(false);
        // Go through the songs if we are adding them as pdfs until we have processed all
        if (ids==null) {
            ids = new String[1];
        }

        if (songsProcessed>=ids.length || !includeSongs || (!pdf && !png && !screenShot && !image) || songsProcessed==songsToAdd) {
            initiateShare();

        } else {
            String id = ids[songsProcessed];
            String key = setKeys[songsProcessed];
            location = mainActivityInterface.getExportActions().getFolderAndFile(id);
            boolean likelyXML = !location[1].contains(".") || location[1].toLowerCase(Locale.ROOT).endsWith(".xml");

            // Only add if we haven't already added it
            if (likelyXML && (!songsAlreadyAdded.toString().contains(id+".pdf") ||
                    !songsAlreadyAdded.toString().contains(id+".png")) && !id.equals("ignore")) {
                songsAlreadyAdded.append("\n").append(id);
                if (screenShot) {
                    updateProgressText(location[1] + "_"+screenshot_string+".png", songsProcessed + 1, ids.length);
                } else {
                    updateProgressText(location[1] + ".pdf", songsProcessed + 1, ids.length);
                }
                if (location[0].contains("../") || location[0].contains("**")) {
                    tempSong = new Song();
                    tempSong.setFolder("../Export");
                    tempSong.setFilename(location[1]);
                    tempSong = mainActivityInterface.getLoadSong().doLoadSongFile(tempSong,false);
                } else {
                    tempSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(location[0], location[1]);
                    // If we have transposed this song in the set on the fly, match the key here
                    if (!key.isEmpty() && tempSong.getKey()!=null && !tempSong.getKey().isEmpty() &&
                            !key.equals(tempSong.getKey())) {
                        int transposeTimes = mainActivityInterface.getTranspose().getTransposeTimes(tempSong.getKey(),key);
                        mainActivityInterface.getTranspose().checkChordFormat(tempSong);
                        tempSong = mainActivityInterface.getTranspose().doTranspose(tempSong,"+1",transposeTimes,tempSong.getDetectedChordFormat(),tempSong.getDesiredChordFormat());
                        tempSong.setFilename(tempSong.getFilename()+"__"+key);
                        location[1] = location[1]+"__"+key;
                    }
                }
                Log.d(TAG,"about to createOnTheFly:"+location[1]+".pdf");
                createOnTheFly(tempSong,location[1]+".pdf");
            } else if (id.equals("ignore") || !likelyXML) {
                songsProcessed++;
                renderPDFSongs();
            }
            songsProcessed++;
        }
    }
    private void doExportSong() {
        // Do this in a new Thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
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

                uris.add(mainActivityInterface.getStorageAccess().copyFromTo(
                        "Songs", fixSubfolder(folder), filename,
                        "Export", "", filename));

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

            if (justChords && isXML) {
                Uri uri = mainActivityInterface.getConvertJustChords().buildJustChordsSingleSong(mainActivityInterface.getSong());
                if (uri!=null) {
                    uris.add(uri);
                }
                if (!mimeTypes.contains("application/json")) {
                    mimeTypes.add("application/json");
                }
            }

            if (onsong && isXML) {
                String content = mainActivityInterface.getPrepareFormats().getSongAsOnSong(mainActivityInterface.getSong());
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doExportSong doStringWriteToFile Export/"+filename+".onsong with: "+content);
                if (mainActivityInterface.getStorageAccess().writeFileFromString("Export", "",
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
                if (mainActivityInterface.getStorageAccess().writeFileFromString("Export", "",
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
                if (mainActivityInterface.getStorageAccess().writeFileFromString("Export", "",
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

            if ((pdf && isXML) || (screenShot && isXML) || png) {
                // Create PDF song on the fly and only initiate share once done
                tempSong = mainActivityInterface.getSong();
                mainActivityInterface.getProcessSong().setMakingImageOrScreenShot(true);
                createOnTheFly(mainActivityInterface.getSong(),mainActivityInterface.getSong().getFilename()+".pdf");
                mainActivityInterface.getProcessSong().setMakingImageOrScreenShot(false);
            } else {
                // No pdf processing required, so initiate the share
                initiateShare();
            }
        });
    }

    private void initiateShare() {
        // Copy the exported folder into the local app files/export folder (for sharing permission)
        File exportFolder = mainActivityInterface.getStorageAccess().getAppSpecificFile("Export","",null);
        // Don't delete - does something!
        Log.d(TAG,"makedirs:"+exportFolder.mkdirs());
        ArrayList<Uri> newUris = new ArrayList<>();
        ArrayList<String> newFilenames = new ArrayList<>();

        // If we are exporting songs in text format, also add the merged one
        Uri mergedTextFile = createMergedTextFile();
        if (mergedTextFile!=null) {
            uris.add(mergedTextFile);
        }

        if (getContext()!=null) {
            for (Uri uri : uris) {
                if (uri != null) {
                    try {
                        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
                        // Get the file name after the OpenSong/Export/ bit
                        File file = getFileFromUri(uri, exportFolder);
                        if (file!=null) {
                            OutputStream outputStream = new FileOutputStream(file);
                            // Don't delete - does something!
                            Log.d(TAG, "Copy:" + mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream));
                            Uri newUri = FileProvider.getUriForFile(getContext(), "com.garethevans.church.opensongtablet.fileprovider", file);
                            newUris.add(newUri);
                            newFilenames.add(file.getName());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        Intent intent;
        if (setContent!=null && !myView.exportTextAsMessage.getChecked()) {
            intent = mainActivityInterface.getExportActions().setShareIntent(null, "*/*", null, newUris);
        } else {
            intent = mainActivityInterface.getExportActions().setShareIntent(textContent, "*/*", null, newUris);
        }
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.putExtra(Intent.EXTRA_SUBJECT, app_name_string + " " +
                exportType + ": " + shareTitle);
        if (setContent!=null && myView.exportTextAsMessage.getChecked()) {
            intent.putExtra(Intent.EXTRA_TEXT, setContent);
        } else if (setContent == null){
            intent.putExtra(Intent.EXTRA_TEXT, textContent);
        }


        if (newUris.size() > 1) {
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, newUris);
        } else if (newUris.size() == 1) {
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, newUris.get(0));
            if (newUris.get(0)!=null && newUris.get(0).getPath()!=null) {
                intent.putExtra(Intent.EXTRA_TITLE, newFilenames.get(0));
            }
        }

        // THE FIX: Explicitly grant permissions to every URI via ClipData
        if (!newUris.isEmpty()) {
            // Create ClipData with the first URI
            ClipData clipData = ClipData.newRawUri(shareTitle, newUris.get(0));

            // Add all other URIs in the list to the ClipData
            for (int i = 1; i < newUris.size(); i++) {
                clipData.addItem(new ClipData.Item(newUris.get(i)));
            }

            // Attach the ClipData to the intent.
            // This is the "manifest" that Google Drive needs to start its UploadActivity.
            intent.setClipData(clipData);
        }


        // This is the key for background services like Google Drive
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        mainActivityInterface.getMainHandler().post(() -> {
            // Create the Chooser
            Intent chooser = Intent.createChooser(intent, shareTitle);
            // IMPORTANT: The chooser also needs the flag to pass it down to the selected app
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(chooser);
            myView.scrim.setVisibility(View.GONE);
            myView.progressText.setVisibility(View.GONE);
            myView.shareButton.setEnabled(true);
            myView.shareButton.show();
            myView.print.setEnabled(true);
            myView.print.show();
            uris = new ArrayList<>();
        });
    }

    private File getFileFromUri(Uri uri, File exportFolder) {
        if (uri!=null) {
            String name = uri.getLastPathSegment();
            String bitToRemove = "OpenSong/Export/";
            if (name.contains(bitToRemove) && !name.endsWith(bitToRemove)) {
                name = name.substring(name.indexOf(bitToRemove) + bitToRemove.length());
            }
            bitToRemove = "OpenSong%2FExport%2F";
            if (name.contains(bitToRemove) && !name.endsWith(bitToRemove)) {
                name = name.substring(name.indexOf(bitToRemove) + bitToRemove.length());
            }
            bitToRemove = "/";
            if (name.contains(bitToRemove) && !name.endsWith(bitToRemove)) {
                name = name.substring(name.indexOf(bitToRemove) + bitToRemove.length());
            }
            return new File(exportFolder, name);
        } else {
            return null;
        }
    }

    // We can create nice views on the fly here by processing the Song, then populating the views
    private void createOnTheFly(Song thisSong, String pdfName) {
        // Make sure any current headers/sections are wiped

        tempSong = thisSong;
        mainActivityInterface.getProcessSong().setPdfPrinting(true);
        mainActivityInterface.getProcessSong().setForceSinglePagePDF(myView.forceSinglePage.getChecked());

        mainActivityInterface.getMainHandler().post(()-> {
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
        sectionViewsScreenshot = new ArrayList<>();
        sectionViewWidthsScreenshot = new ArrayList<>();
        sectionViewHeightsScreenshot = new ArrayList<>();
        if (getContext()!=null) {
            headerLayoutPDF = new LinearLayout(getContext());
        }
    }

    public void createOnTheFlyHeader(Song thisSong, String pdfName) {
        // Get the song sheet header
        // Once this has drawn, move to the next stage of the song sections
        mainActivityInterface.getProcessSong().setPdfPrinting(true);
        mainActivityInterface.getProcessSong().setForceSinglePagePDF(myView.forceSinglePage.getChecked());

        tempSong = thisSong;

        ViewTreeObserver headerVTO = myView.hiddenHeader.getViewTreeObserver();
        headerVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                myView.hiddenHeader.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                headerVTO.removeOnGlobalLayoutListener(this);
                headerLayoutWidth = myView.hiddenHeader.getMeasuredWidth();
                headerLayoutHeight = myView.hiddenHeader.getMeasuredHeight();
                if (!pdfName.equals(set_string +" " +setToExport+".pdf") && !setPNG && !png && !image) {
                    myView.hiddenHeader.removeAllViews();
                }
                createOnTheFlySections(thisSong, pdfName);
            }
        });
        // Now draw it here for measuring via the VTO
        headerLayoutPDF = mainActivityInterface.getSongSheetHeaders().getSongSheet(thisSong,
                scaleComments, mainActivityInterface.getMyThemeColors().getPdfTextColor());
        if (headerLayoutPDF!=null) {
            myView.hiddenHeader.addView(headerLayoutPDF);
        } else {
            createOnTheFlySections(thisSong, pdfName);
        }
    }

    public void createOnTheFlySections(Song thisSong, String pdfName) {
        // If we don't have any sections in the song, change the double line breaks into sections
        // Keep a reference of the song/pdfName
        this.thisSong = thisSong;
        this.pdfName = pdfName;

        mainActivityInterface.getProcessSong().setPdfPrinting(true);
        mainActivityInterface.getProcessSong().setForceSinglePagePDF(myView.forceSinglePage.getChecked());
        if (thisSong==null) {
            thisSong =  new Song();
        }
        if (thisSong.getLyrics()==null) {
            thisSong.setLyrics("");
        }
        tempSong = thisSong;
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
        // In case we have inline abc notation, reset those objects
        mainActivityInterface.getAbcNotation().resetInlineAbcObjects();
        mainActivityInterface.getAbcNotation().setAbcWebViewsDrawn(false);

        myView.hiddenSections.setBackgroundColor(Color.WHITE);
        mainActivityInterface.getMakePDF().setIsSetListPrinting(true);
        sectionViewsPDF = mainActivityInterface.getProcessSong().
                setSongInLayout(thisSong,true, false);

        // Now we have the views, add them to the temp layout and set up a view tree listener to measure
        ViewTreeObserver sectionsVTO = myView.hiddenSections.getViewTreeObserver();
        Song finalThisSong = thisSong;
        sectionsVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // The views are ready so lets measure them after clearing this listener
                // If all the views are there, we can start measuring
                if (myView.hiddenSections.getChildCount()>=sectionViewsPDF.size()) {
                    myView.hiddenSections.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    sectionsVTO.removeOnGlobalLayoutListener(this);

                    // Proceed if we don't have inlineAbcObjects, or they have been measured
                    // If there are inlineAbcObjects, we will hear back via the JSInterface that triggers abcFinished()
                    Log.d(TAG,"inlineAbcObjects:"+mainActivityInterface.getAbcNotation().countInlineAbcObjects()+"  drawn:"+mainActivityInterface.getAbcNotation().getAbcWebViewsDrawn());
                    if (mainActivityInterface.getAbcNotation().countInlineAbcObjects()==0 ||
                                mainActivityInterface.getAbcNotation().getAbcWebViewsDrawn()) {
                        measureSectionsAndContinue();
                    }
                }
            }
        });

        // Add the section views and this will trigger the VTO
        for (int x=0; x<sectionViewsPDF.size(); x++) {
            myView.hiddenSections.addView(sectionViewsPDF.get(x));
        }
    }

    public void measureSectionsAndContinue() {
        int maxWidth = 0;
        for (int x=0; x<myView.hiddenSections.getChildCount(); x++) {
            View view = myView.hiddenSections.getChildAt(x);
            int width = view.getMeasuredWidth();
            int height = view.getMeasuredHeight();
            // If we have inlineAbcObjects, then fix these sections to use the ImageView version
            if (mainActivityInterface.getAbcNotation().countInlineAbcObjects()>0) {
                // Get the extra webView height for any webview in this section
                for (InlineAbcObject inlineAbcObject:mainActivityInterface.getAbcNotation().getInlineAbcObjects()) {
                    Log.d(TAG,"inlineAbcObject("+inlineAbcObject.getAbcItem()+")  containingItem("+inlineAbcObject.getAbcContainingItem()+")  size:"+inlineAbcObject.getAbcWidth()+"x"+inlineAbcObject.getAbcHeight());
                    if (inlineAbcObject.getAbcContainingItem()==x && inlineAbcObject.getAbcHeight()>1) {
                        // The inlineAbcWebView was hidden, so had no size
                        // We are going to switch on the ImageView and need to size this to what the webView reported
                        ImageView imageView = inlineAbcObject.getInlineAbcImageView(false);
                        inlineAbcObject.drawTheImageView(imageView);
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setBackgroundColor(Color.RED);
                        // To make the scroll work properly, adjust the size
                        height = height + inlineAbcObject.getAbcHeight();
                        width = Math.max(width,inlineAbcObject.getAbcWidth());
                    }
                }
            }
            sectionViewWidthsPDF.add(width);
            sectionViewHeightsPDF.add(height);
            if (width>maxWidth) {
                maxWidth = width;
            }
        }

        boolean isSetFile = pdfName.equals(set_string +" " +setToExport+".pdf");

        // If we are exporting a setPNG and this is the set, take a bitmap!
        if (isSetFile && setPNG && !setPNGDone) {
            try {
                // The header should still be in place
                // Now take a bitmap of the layout
                // Check how many columns are used for the bitmap
                setPNGContent = Bitmap.createBitmap(maxWidth, myView.previewLayout.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(setPNGContent);
                myView.previewLayout.draw(canvas);
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
            setPNGDone = true;
        }

        // Now trigger the next step of preparing the pdf from the views created on the fly
        if (uris==null) {
            uris = new ArrayList<>();
        }
        if (mimeTypes==null) {
            mimeTypes = new ArrayList<>();
        }

        if ((png || image) && !isSetFile) {
            // Now take a bitmap of the layout for the song
            myView.previewLayout.setVisibility(View.VISIBLE);
            setPNGContent = Bitmap.createBitmap(myView.previewLayout.getWidth(), myView.previewLayout.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(setPNGContent);
            myView.previewLayout.draw(canvas);

            pngName = pdfName.replace(".pdf",".png");
            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Export", "", pngName);
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, uri, null, "Export", "", pngName);
            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
            mainActivityInterface.getStorageAccess().writeImage(outputStream, setPNGContent);
            uris.add(uri);
            if (!mimeTypes.contains("image/png")) {
                mimeTypes.add("image/png");
            }
            myView.previewLayout.setVisibility(View.INVISIBLE);
            myView.hiddenHeader.removeAllViews();
        }

        // Now detach from this view (can only be shown in one layout)
        myView.hiddenSections.removeAllViews();

        // If we wanted a pdf (rather than png), add it

        if ((setPDF && isSetFile && !setPDFDone) || (!isSetFile && pdf)) {
            // Sets are always processed first, so mark as done
            setPDFDone = true;
            mainActivityInterface.getMakePDF().setSong(tempSong);
            mainActivityInterface.getMakePDF().getColumns(sectionViewsPDF,sectionViewWidthsPDF,sectionViewHeightsPDF);
            uris.add(mainActivityInterface.getMakePDF().createTextPDF(
                    sectionViewsPDF, sectionViewWidthsPDF,
                    sectionViewHeightsPDF, headerLayoutPDF,
                    headerLayoutWidth, headerLayoutHeight,
                    pdfName, null));

            if ((!isSetFile && !pdf) || (isSetFile && !setPDF)) {
                // Remove that uri as it was only created for an image screenshot
                uris.remove(uris.size()-1);
            } else {
                if (!mimeTypes.contains("application/pdf")) {
                    mimeTypes.add("application/pdf");
                }
            }
        }
        if (screenShot) {
            mainActivityInterface.getProcessSong().setMakingImageOrScreenShot(true);
            createOnTheFlySectionsScreenshot(thisSong,pdfName);
            mainActivityInterface.getProcessSong().setMakingImageOrScreenShot(false);
        } else if (isPrint) {
            // We have exported a song as a print layout
            doPrint(false);
        } else {
            renderPDFSongs();
        }
    }

    public void createOnTheFlySectionsScreenshot(Song thisSong, String pdfName) {
        // Prepare the song sheet header if required
        mainActivityInterface.getSongSheetTitleLayout().removeAllViews();
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("songSheet",false)) {
            mainActivityInterface.getSongSheetHeaders().setForExport(false);
            mainActivityInterface.setSongSheetTitleLayout(mainActivityInterface.getSongSheetHeaders().getSongSheet(thisSong,
                    scaleComments, screenShot ? mainActivityInterface.getMyThemeColors().getLyricsTextColor() : Color.BLACK));
            mainActivityInterface.getSongSheetHeaders().setForExport(true);
        }

        // If we don't have any sections in the song, change the double line breaks into sections
        if (!thisSong.getLyrics().contains("\n[")) {
            String[] lines = thisSong.getLyrics().split("\n");
            StringBuilder stringBuilder = new StringBuilder();
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    stringBuilder.append("[]\n");
                } else {
                    stringBuilder.append(line).append("\n");
                }
            }
            thisSong.setLyrics(stringBuilder.toString());
        }

        // Create the content for the section views.
        myView.scaledSongContent.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
        myView.scaledPageHolder.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
        sectionViewsScreenshot = mainActivityInterface.getProcessSong().
                setSongInLayout(thisSong, !screenShot, false);

        // Add to the test layout to measure
        ViewTreeObserver sectionsVTO = myView.hiddenSections.getViewTreeObserver();
        sectionsVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (myView.hiddenSections.getChildCount()==sectionViewsScreenshot.size()) {
                    myView.hiddenSections.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    sectionsVTO.removeOnGlobalLayoutListener(this);
                    // Go through each item and measure them
                    int totalHeight = 0;
                    int requiredWidth = 0;
                    for (int x = 0; x < sectionViewsScreenshot.size(); x++) {
                        int width = sectionViewsScreenshot.get(x).getMeasuredWidth();
                        int height = sectionViewsScreenshot.get(x).getMeasuredHeight();
                        totalHeight = totalHeight + height;
                        if (width > requiredWidth) {
                            requiredWidth = width;
                        }
                        sectionViewWidthsScreenshot.add(width);
                        sectionViewHeightsScreenshot.add(height);
                    }
                    createOnTheFlySectionsScreenshots2(thisSong, pdfName);
                }
            }
        });
        // Add the section views and this will trigger the VTO
        for (int x=0; x<sectionViewsScreenshot.size(); x++) {
            //sectionViewsScreenshot.get(x).getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            myView.hiddenSections.addView(sectionViewsScreenshot.get(x));
        }
    }

    float[] scaleInfo;

    public void createOnTheFlySectionsScreenshots2(Song thisSong, String pdfName) {
        // Get the sizes from the performance fragment if possible (due to different paddings)
        int availableWidth;
        int availableHeight;
        int[] viewPadding = mainActivityInterface.getViewMargins();
        if (mainActivityInterface.getMode().equals(mode_performance_string)
                && mainActivityInterface.getAvailableSizes()!=null) {
            int[] sizes = mainActivityInterface.getAvailableSizes();
            availableWidth = sizes[0];
            availableHeight = sizes[1];
        } else {
            int[] screenSizes = mainActivityInterface.getDisplayMetrics();
            int screenWidth = screenSizes[0];
            int screenHeight = screenSizes[1];
            availableWidth = screenWidth - viewPadding[0] - viewPadding[1];
            availableHeight = screenHeight - viewPadding[2] - viewPadding[3];
        }

        myView.scaledSongContent.getLayoutParams().width=availableWidth;
        myView.scaledSongContent.getLayoutParams().height=availableHeight;
        // Now we have the views, add them to the temp layout and set up a view tree listener to measure
        ViewTreeObserver sectionsVTO = myView.scaledSongContent.getViewTreeObserver();
        sectionsVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // The views are ready so lets measure them after clearing this listener
                // If all the views are there, we can start measuring
                int col1Items = 0;
                int col2Items = 0;
                int col3Items = 0;
                if (myView.scaledSongContent.getCol1().getChildCount()>0) {
                    col1Items = ((LinearLayout)myView.scaledSongContent.getCol1().getChildAt(0)).getChildCount();
                }
                if (myView.scaledSongContent.getCol2().getChildCount()>0) {
                    col2Items = ((LinearLayout)myView.scaledSongContent.getCol2().getChildAt(0)).getChildCount();
                }
                if (myView.scaledSongContent.getCol3().getChildCount()>0) {
                    col3Items = ((LinearLayout)myView.scaledSongContent.getCol3().getChildAt(0)).getChildCount();
                }

                int totalItems = col1Items + col2Items + col3Items;

                if (totalItems>=sectionViewsScreenshot.size()) {
                    myView.hiddenSections.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    sectionsVTO.removeOnGlobalLayoutListener(this);
                    int maxWidth = 0;
                    for (int x=0; x<myView.hiddenSections.getChildCount(); x++) {
                        View view = myView.hiddenSections.getChildAt(x);
                        int width = view.getMeasuredWidth();
                        int height = view.getMeasuredHeight();
                        sectionViewWidthsScreenshot.add(width);
                        sectionViewHeightsScreenshot.add(height);
                        if (width>maxWidth) {
                            maxWidth = width;
                        }
                    }

                    boolean isSetFile = pdfName.equals(set_string +" " +setToExport+".pdf");

                    // Now trigger the next step of preparing the pdf from the views created on the fly
                    if (uris==null) {
                        uris = new ArrayList<>();
                    }
                    if (mimeTypes==null) {
                        mimeTypes = new ArrayList<>();
                    }

                    int thisWidth = 0;
                    int thisHeight = 0;
                    if (scaleInfo[0]==1) {
                        /*float[] {1,           // Number of columns
                    1    oneColumnScale,    // Overall best scale
                    2    col1_1Width,       // Column 1 max width
                    3    col1_1Height,      // Column 1 total height
                    4    sectionSpace}      // Section space per view except last column */
                        thisWidth = (int)(scaleInfo[2] * scaleInfo[1]);
                        thisHeight = (int)(scaleInfo[3] * scaleInfo[1]);
                    } else if (scaleInfo[0]==2) {
                        /*float[]{2,             // Number of columns
                    1    twoColumnScale,     // Overall best scale
                    2    columnBreak2,       // Break point
                    3    col1_2ScaleBest,    // Best col 1 scale
                    4    col1_2Width,        // Column 1 max width
                    5    col1_2Height,       // Column 1 total height
                    6    col2_2ScaleBest,    // Best col 2 scale
                    7    col2_2Width,        // Column 2 max width
                    8    col2_2Height,       // Column 2 total height
                    9    sectionSpace}       // Section space per view except last column */
                        thisWidth = availableWidth;
                        thisHeight = (int)Math.max(scaleInfo[3]*scaleInfo[5],scaleInfo[6]*scaleInfo[8]);
                    } else if (scaleInfo[0]==3) {
                        /*float[]{3,             // Number of columns
                    1    threeColumnScale,   // Overall best scale
                    2    columnBreak3_a,     // Break point 1
                    3    columnBreak3_b,     // Break point 2
                    4    col1_3ScaleBest,    // Best col 1 scale
                    5    col1_3Width,        // Column 1 max width
                    6    col1_3Height,       // Column 1 total height
                    7    col2_3ScaleBest,    // Best col 2 scale
                    8    col2_3Width,        // Column 2 max width
                    9    col2_3Height,       // Column 2 total height
                    10   col3_3ScaleBest,    // Best col 3 scale
                    11   col3_3Width,        // Column 3 max width
                    12   col3_3Height,       // Column 3 total height
                    13   sectionSpace};      // Section space per view except last in column */
                        thisWidth = availableWidth;
                        thisHeight = (int)Math.max(scaleInfo[4]*scaleInfo[6],Math.max(scaleInfo[7]*scaleInfo[9],scaleInfo[10]*scaleInfo[12]));

                    }

                    boolean takingScreenShot = (png && !isSetFile) || (screenShot && !isSetFile);
                    if (takingScreenShot && thisWidth>0 && thisHeight>0) {
                        // Now take a bitmap of the layout for the song
                        // Get the maximum width of the views

                        // If we are taking a screenshot, we need to set the song into columns
                        // Remove the views and set them in the layout
                        myView.hiddenSections.removeAllViews();
                        myView.scaledPageHolder.setVisibility(View.VISIBLE);

                        setPNGContent = Bitmap.createBitmap(thisWidth, thisHeight, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(setPNGContent);
                        myView.scaledPageHolder.draw(canvas);

                        pngName = pdfName.replace(".pdf","_"+screenshot_string+".png");
                        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Export", "", pngName);
                        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, uri, null, "Export", "", pngName);
                        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
                        mainActivityInterface.getStorageAccess().writeImage(outputStream, setPNGContent);
                        uris.add(uri);
                        myView.previewLayout.setVisibility(View.INVISIBLE);
                        myView.scaledPageHolder.setVisibility(View.INVISIBLE);
                        myView.hiddenHeader.removeAllViews();
                    }


                    // Now detach from this view (can only be shown in one layout)
                    myView.hiddenSections.removeAllViews();

                    // If we wanted a pdf (rather than png), add it

                    if ((setPDF && isSetFile && !setPDFDone) || (!isSetFile && pdf)) {
                        // Sets are always processed first, so mark as done
                        setPDFDone = true;
                        uris.add(mainActivityInterface.getMakePDF().createTextPDF(
                                sectionViewsPDF, sectionViewWidthsPDF,
                                sectionViewHeightsPDF, headerLayoutPDF,
                                headerLayoutWidth, headerLayoutHeight,
                                pdfName, null));

                        if ((!isSetFile && !pdf) || (isSetFile && !setPDF)) {
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
                        int delay = takingScreenShot ? 100:20;
                        // GE added this via a delayed runnable otherwise the repeated layout didn't get time to reset.
                        new Handler(Looper.getMainLooper()).postDelayed(() -> renderPDFSongs(),delay);
                    }
                }
            }
        });

        // Remove any scaled header that exists
        myView.scaledHeader.removeAllViews();
        mainActivityInterface.getProcessSong().setMakingScaledScreenShot(true);
        mainActivityInterface.getProcessSong().setMakingImageOrScreenShot(true);
        scaleInfo = mainActivityInterface.getProcessSong().addViewsToScreen(thisSong, sectionViewsScreenshot,
                sectionViewWidthsScreenshot,sectionViewHeightsScreenshot,myView.scaledPageHolder,myView.scaledSongContent,myView.scaledHeader,availableWidth,availableHeight,
                myView.scaledSongContent.getCol1(),myView.scaledSongContent.getCol2(),myView.scaledSongContent.getCol3(),false,getResources().getDisplayMetrics());
        mainActivityInterface.getProcessSong().setMakingScaledScreenShot(false);
        mainActivityInterface.getProcessSong().setMakingImageOrScreenShot(false);

    }

    private void updateProgressText(String text, int curr, int total) {
        String progressText = text + " (" + curr + "/" + total + ")";
        myView.progressText.post(() -> myView.progressText.setText(progressText));
    }

    private void doPrint(boolean isSet) {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Get a PrintManager instance
            if (getActivity() != null) {
                PrintManager printManager = (PrintManager) getActivity().getSystemService(Context.PRINT_SERVICE);

                // Set job name, which will be displayed in the print queue
                String jobName = app_name_string + " Document";

                // Start a print job, passing in a PrintDocumentAdapter implementation
                // to handle the generation of a print document

                if (isSet) {
                    // Set the variable that will remove gaps from set items on the set list page(s)
                    mainActivityInterface.getMakePDF().setIsSetListPrinting(true);
                    mainActivityInterface.getProcessSong().setPdfPrinting(true);
                    mainActivityInterface.getProcessSong().setForceSinglePagePDF(myView.forceSinglePage.getChecked());

                    // Go through the sets and create any custom slides required (variations, slides, etc).
                    ArrayList<Uri> setFiles = mainActivityInterface.getExportActions().addOpenSongSetsToUris(setNames);
                    for (Uri setFile : setFiles) {
                        mainActivityInterface.getSetActions().extractSetFile(setFile, mainActivityInterface.getCurrentSet(),true);
                    }

                    // This is sent to the MultipagePrinterAdapter class to deal with
                    MultipagePrinterAdapter multipagePrinterAdapter = new MultipagePrinterAdapter(getActivity());
                    multipagePrinterAdapter.updateSetList(this, null, setToExport, setData[0], setData[1], setData[2]);
                    mainActivityInterface.getMakePDF().setPreferedAttributes();
                    printManager.print(jobName, multipagePrinterAdapter, mainActivityInterface.getMakePDF().getPrintAttributes());


                } else {
                    // We are printing a single item (song, or song list)
                    // Set the variable that will remove gaps from set items on the set list page(s)
                    mainActivityInterface.getMakePDF().setIsSetListPrinting(false);
                    mainActivityInterface.getProcessSong().setPdfPrinting(true);
                    mainActivityInterface.getProcessSong().setForceSinglePagePDF(myView.forceSinglePage.getChecked());

                    // This is sent to the MultipagePrinterAdapter class to deal with
                    MultipagePrinterAdapter multipagePrinterAdapter = new MultipagePrinterAdapter(getActivity());
                    multipagePrinterAdapter.updateSetList(this, null,null, tempSong.getFolderNamePair(), tempSong.getTitle(), tempSong.getKey());
                    mainActivityInterface.getMakePDF().setPreferedAttributes();
                    printManager.print(jobName, multipagePrinterAdapter, mainActivityInterface.getMakePDF().getPrintAttributes());

                }
                mainActivityInterface.getSong().setFolder(currentSongFolder);
                mainActivityInterface.getSong().setFilename(currentSongFile);
                mainActivityInterface.getProcessSong().setPdfPrinting(false);
                mainActivityInterface.getProcessSong().setForceSinglePagePDF(myView.forceSinglePage.getChecked());

            }
        });
    }

    // OpenSongSet bundle export
    private void exportOpenSongSetBundle() {
        // The song files should already now be in the export folder
        // We now need to add them to a zip file
        if (getContext()!=null && myView!=null) {
            if (openSongSetBundle) {
                mainActivityInterface.getOpenSongSetBundle().zipFiles(setToExport, urisSetBundle, true);

                // Add the bundle files to the uris
                Uri setBundleUri = mainActivityInterface.getOpenSongSetBundle().getSetBundleUri();
                if (setBundleUri != null) {
                    uris.add(setBundleUri);
                }
            }

            /*if (justChordsSetBundle) {
                //mainActivityInterface.getOpenSongSetBundle().zipFiles(setToExport, urisSetBundle, false);

                Uri justChordsBundleUri = mainActivityInterface.getOpenSongSetBundle().getJustChordsBundleUri();
                if (justChordsBundleUri!=null) {
                        uris.add(justChordsBundleUri);
                }
            }*/
            mainActivityInterface.getOpenSongSetBundle().destroy();
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
        sectionViewsScreenshot = new ArrayList<>();
        sectionViewWidthsScreenshot = new ArrayList<>();
        sectionViewHeightsScreenshot = new ArrayList<>();
        mimeTypes = new ArrayList<>();
        uris = new ArrayList<>();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        tidyOnClose();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tidyOnClose();
    }
    private void tidyOnClose() {
        try {
            if (mainActivityInterface!=null) {
                mainActivityInterface.getAbcNotation().setExportFragment(null);
                mainActivityInterface.getProcessSong().setPdfPrinting(false);
                mainActivityInterface.getMakePDF().setIsSetListPrinting(false);
                mainActivityInterface.getOpenSongSetBundle().setProgressText(null);
                mainActivityInterface.getSong().setFolder(currentSongFolder);
                mainActivityInterface.getSong().setFilename(currentSongFile);
                mainActivityInterface.getLoadSong().doLoadSong(mainActivityInterface.getSong(),false);
                mainActivityInterface.setForceReload(true);
                mainActivityInterface.getStorageAccess().wipeFolder("Export","");
                mainActivityInterface.getSongSheetHeaders().setForExport(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Song getTempSong() {
        return tempSong;
    }

    public void abcFinished() {
        // We have rendered the inline ABC notation, so can continue with measuring
        Log.d(TAG,"abcFinished");
        myView.previewLayout.setVisibility(View.VISIBLE);
        myView.hiddenSections.setVisibility(View.VISIBLE);
        measureSectionsAndContinue();
    }
}
