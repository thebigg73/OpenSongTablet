package com.garethevans.church.opensongtablet.export;

import android.content.Context;
import android.content.Intent;
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
import androidx.lifecycle.MutableLiveData;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsExportBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.InputStream;
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
    private ArrayList<String> mimeTypes;
    private ArrayList<String> setNames;
    private Uri uri;
    private final MutableLiveData<Boolean> listen = new MutableLiveData<>();
    private ArrayList<View> sectionViewsPDF = new ArrayList<>();
    private ArrayList<Integer> sectionViewWidthsPDF = new ArrayList<>(),
            sectionViewHeightsPDF = new ArrayList<>();
    private LinearLayout headerLayoutPDF;
    private int headerLayoutWidth, headerLayoutHeight;
    private String setToExport = null;
    private String exportType;
    private String shareTitle;
    private String exportTitle;
    private String textContent;
    private int songsToAdd, songsProcessed;
    private boolean openSong = false;
    private boolean currentFormat = false;
    private boolean openSongApp = false;
    private boolean pdf = false;
    private boolean image = false;
    private boolean chordPro = false;
    private boolean onsong = false;
    private boolean text = false;
    private boolean openSongSet = false;
    private boolean openSongAppSet = false;
    private boolean includeSongs = false;
    private boolean textSet = false;
    private volatile boolean processingSetPDFs = false;
    private String[] location;
    private String[] setData;
    private Song tempSong;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsExportBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(getString(R.string.export));

        // If we are exporting a song, we do this once, if not, we do it for each song in the set
        if (mainActivityInterface.getWhattodo().startsWith("exportset:")) {
            setToExport = mainActivityInterface.getWhattodo().replace("exportset:", "").replace("%_%","");
            setNames = mainActivityInterface.getExportActions().getListOfSets(setToExport);
            setData = mainActivityInterface.getExportActions().parseSets(setNames);
            textContent = setData[0];
        }

        // Some options are hidden by default and only visible if we have a proper OpenSong song
        // If exporting a set, some options aren't allowed
        showUsable();

        myView.shareButton.setOnClickListener(v -> prepareExport());
        myView.print.setOnClickListener(v -> {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    if (mainActivityInterface.getWhattodo().startsWith("exportset:")) {
                        doPrint(true);
                    } else {
                        if (sectionViewsPDF == null || sectionViewsPDF.size() == 0) {
                            listen.setValue(false); //Initilize with a value
                            createOnTheFly(mainActivityInterface.getSong());
                            listen.observe(getViewLifecycleOwner(), isDone -> {
                                if (isDone) {
                                    listen.removeObservers(getViewLifecycleOwner());
                                    doPrint(false);
                                }
                            });
                        } else {
                            doPrint(false);
                        }
                    }
                });
            });
        });

        myView.nestedScrollView.setExtendedFabToAnimate(myView.shareButton);
        myView.nestedScrollView.setExtendedFab2ToAnimate(myView.print);

        return myView.getRoot();
    }

    private void showUsable() {

        // By default everything is hidden.  Only make the correct ones visible

        // Set the defaults for set export
        myView.openSongAppSet.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOpenSongAppSet",false));
        myView.openSongSet.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOpenSongSet",true));
        myView.textSet.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOpenSongTextSet",false));

        // Set the defaults for song export
        myView.currentFormat.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportCurrentFormat",true));
        myView.pdf.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportPDF",false));
        myView.openSongApp.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOpenSongApp",false));
        //myView.openSong.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportDesktop",false));
        myView.onSong.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOnSong",false));
        myView.chordPro.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportChordPro",false));
        myView.text.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportText",false));

        // Set the listeners for preferences
        myView.openSongAppSet.setOnCheckedChangeListener(new MyCheckChanged("exportOpenSongAppSet"));
        myView.openSongSet.setOnCheckedChangeListener(new MyCheckChanged("exportOpenSongSet"));
        myView.textSet.setOnCheckedChangeListener(new MyCheckChanged("exportOpenSongTextSet"));

        myView.currentFormat.setOnCheckedChangeListener(new MyCheckChanged("exportCurrentFormat"));
        myView.pdf.setOnCheckedChangeListener(new MyCheckChanged("exportPDF"));
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
            mainActivityInterface.updateToolbarHelp(getString(R.string.website_export_set));

            myView.setExportInfo.setVisibility(View.VISIBLE);
            myView.currentFormat.setVisibility(View.VISIBLE);
            myView.openSong.setVisibility(View.GONE);

            // Include the songs views
            myView.includeSongs.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportSetSongs",true));
            myView.includeSongs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG,"isChecked:"+isChecked);
                    mainActivityInterface.getPreferences().setMyPreferenceBoolean("exportSetSongs",isChecked);
                    showHideSongOptions(isChecked);
                }
            });
            showHideSongOptions(myView.includeSongs.isChecked());

            // If we are exporting songs, hide the ones not allowed for sets
            myView.image.setVisibility(View.GONE);

            // Now show the set view
            myView.setOptionsLayout.setVisibility(View.VISIBLE);

        } else {
            // Hide the options based on the song format
            mainActivityInterface.updateToolbarHelp(getString(R.string.website_export_song));
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
            } else {
                myView.image.setVisibility(View.GONE);
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
                    if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportOpenSongApp", true)) {
                        myView.openSongApp.setChecked(true);
                    } else {
                        myView.openSong.setChecked(true);
                    }
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
        openSongSet = myView.openSongSet.isChecked();
        openSongAppSet = myView.openSongAppSet.isChecked();
        textSet = myView.textSet.isChecked();
        includeSongs = myView.includeSongs.isChecked();

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

        uris = new ArrayList<>();
        mimeTypes = new ArrayList<>();

        // Deal with set exporting
        if (mainActivityInterface.getWhattodo().startsWith("exportset:")) {
            exportType = getString(R.string.set);
            shareTitle = getString(R.string.export_current_set);
            exportTitle = setToExport;
            doExportSet();
        } else {
            exportType = getString(R.string.song);
            shareTitle = getString(R.string.export_current_song);
            exportTitle = mainActivityInterface.getSong().getFilename();
            doExportSong();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void doExportSet() {
        // Do this in a new Thread
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());

            // Add the .osts file
            if (openSongAppSet) {
                // Copy the set(s) to an .osts file extensions and get the uri(s)
                uris.addAll(mainActivityInterface.getExportActions().addOpenSongAppSetsToUris(setNames));
                if (!mimeTypes.contains("text/xml")) {
                    mimeTypes.add("text/xml");
                }
            }

            // Add the desktop set file (no extension)
            if (openSongSet) {
                // Just add the actual set file(s) (no extension)
                uris.addAll(mainActivityInterface.getExportActions().addOpenSongSetsToUris(setNames));
                if (!mimeTypes.contains("text/xml")) {
                    mimeTypes.add("text/xml");
                }
            }

            // Get the set contents
            Log.d(TAG,"textContent: "+textContent);

            // Add a text version of the set
            if (textSet || includeSongs) {
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

            // Now deal with any songs in the sets
            if (includeSongs) {
                String[] ids = setData[0].split("\n");
                StringBuilder songsAlreadyAdded = new StringBuilder();
                songsToAdd = ids.length;
                songsProcessed = 0;
                processingSetPDFs = false;

                Log.d(TAG,"songsToAdd = "+songsToAdd);

                for (int x=0; x< ids.length; x++) {
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


                        // Sharing a song should initiate the CCLI Log of printed (value 6)
                        Song song = mainActivityInterface.getSQLiteHelper().getSpecificSong(location[0], location[1]);
                        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("ccliAutomaticLogging",false)) {
                            mainActivityInterface.getCCLILog().addEntry(song,"6");
                        }

                        // Deal with the currentFormat option first, or PDF for PDF songs
                        if (currentFormat || (pdf && likelyPDF)) {
                            // Just get a uri for the song
                            uris.add(mainActivityInterface.getStorageAccess().getUriForItem("Songs", location[0], location[1]));
                            if (openSong && !mimeTypes.contains("text/xml")) {
                                mimeTypes.add("text/xml");
                            } else if (pdf && !mimeTypes.contains("application/pdf")) {
                                mimeTypes.add("application/pdf");
                            }
                        }

                        // Add OpenSongApp files
                        if (openSongApp && likelyXML) {
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doExportSet CopyFromTo Songs/"+location[0]+"/"+location[1]+" to Export/"+location[1]+".ost");
                            uris.add(mainActivityInterface.getStorageAccess().copyFromTo(
                                    "Songs", location[0], location[1],
                                    "Export", "", location[1] + ".ost"));
                            if (!mimeTypes.contains("text/xml")) {
                                mimeTypes.add("text/xml");
                            }
                        }

                        // Add onSong files
                        if (onsong && likelyXML) {
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

                        // Add chorPro songs
                        if (chordPro && likelyXML) {
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
                        if (text && likelyXML) {
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

                        // Convert song to pdf
                        if (pdf && likelyXML) {
                            // This bit takes more time as we have to parse and draw the pdf
                            // Don't up the songs processed here as we need to wait for drawing
                            while (processingSetPDFs) {
                                // Just wait
                            }

                            // We are about to start process, so set the variable
                            // This stops the next pdf from overwriting this one if it is slow
                            processingSetPDFs = true;

                            handler.post(() -> {
                                listen.setValue(false); //Initilize with a value
                                createOnTheFly(song);
                                listen.observe(getViewLifecycleOwner(), isDone -> {
                                    if (isDone) {
                                        uris.add(mainActivityInterface.getMakePDF().createTextPDF(
                                                sectionViewsPDF, sectionViewWidthsPDF,
                                                sectionViewHeightsPDF, headerLayoutPDF,
                                                headerLayoutWidth, headerLayoutHeight,
                                                location[1] + ".pdf", null));
                                        if (!mimeTypes.contains("application/pdf")) {
                                            mimeTypes.add("application/pdf");
                                        }
                                        listen.removeObservers(getViewLifecycleOwner());
                                        processingSetPDFs = false;
                                        if (songsProcessed == songsToAdd) {
                                            handler.post(this::initiateShare);
                                        }
                                    }
                                });
                            });
                        }
                    }
                }
            }

            Log.d(TAG,"songsProcessed: "+songsProcessed+"  songsToAdd:"+songsToAdd);
            // If all is well, we send the intent.
            // If we are still processing a pdf, we check again at the end of that pass
            if (!processingSetPDFs) {
                handler.post(this::initiateShare);
            }
        });
    }

    private void doExportSong() {
        // Do this in a new Thread
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());

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

            if (pdf && isXML) {
                // Create PDF song on the fly and only initiate share once done
                handler.post(() -> {
                    listen.setValue(false); //Initilize with a value
                    createOnTheFly(mainActivityInterface.getSong());
                    listen.observe(getViewLifecycleOwner(), isDone -> {
                        if (isDone) {
                            listen.removeObservers(getViewLifecycleOwner());
                            String exportFilename = getExportFilename(mainActivityInterface.getSong(), ".pdf");
                            uri = mainActivityInterface.getMakePDF().createTextPDF(
                                    sectionViewsPDF, sectionViewWidthsPDF, sectionViewHeightsPDF, headerLayoutPDF,
                                    headerLayoutWidth, headerLayoutHeight, exportFilename, null);
                            uris.add(uri);
                            if (!mimeTypes.contains("application/pdf")) {
                                mimeTypes.add("application/pdf");
                            }
                            initiateShare();
                        }
                    });
                });
            } else {
                // No pdf processing required, so initiate the share
                initiateShare();
            }
        });
    }

    private void initiateShare() {
        Intent intent = mainActivityInterface.getExportActions().setShareIntent(textContent,"*/*",null,uris);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " " +
                exportType + ": " + shareTitle);
        intent.putExtra(Intent.EXTRA_TEXT, textContent);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            startActivity(Intent.createChooser(intent, shareTitle));
            myView.scrim.setVisibility(View.GONE);
            myView.progressText.setVisibility(View.GONE);
            myView.shareButton.setEnabled(true);
            myView.shareButton.show();
            myView.print.setEnabled(true);
            myView.print.show();
        });
    }

    private Uri copyOpenSongApp(Song song) {
        uri = getExportUri(song,".ost");
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(
                mainActivityInterface.getExportActions().getActualSongFile(mainActivityInterface.getSong()));
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" copyOpenSongApp CopyFile Songs/"+
                mainActivityInterface.getExportActions().getActualSongFile(mainActivityInterface.getSong())+" to "+uri);
        mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream);
        return uri;
    }
    private String getExportFilename(Song song, String extension) {
        return song.getFolder().replace("/","_") + "_" +
                song.getFilename().replace("/","_") + extension;
    }
    private Uri getExportUri(Song song, String extension) {
        String exportFilename = getExportFilename(song, extension);
        uri = mainActivityInterface.getStorageAccess().getUriForItem("Export","",
                exportFilename);

        // If the output file exists, delete it first (so we sort of overwrite)
        if (mainActivityInterface.getStorageAccess().uriExists(uri)) {
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" getExportUri deleteFile "+uri);
            mainActivityInterface.getStorageAccess().deleteFile(uri);
        }

        // Now create a blank file ready to assign an outputStream
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" Create Export/"+exportFilename+"  deleteOld=true");
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,
                uri,null,"Export","", exportFilename);

        return uri;
    }


    // We can create nice views on the fly here by processing the Song, then populating the views
    private void createOnTheFly(Song thisSong) {
        // Make sure any current headers/sections are wiped
        initialiseViews();

        // Now start preparing the views.  First up the header
        createOnTheFlyHeader(thisSong);
    }
    private void initialiseViews() {
        sectionViewsPDF = new ArrayList<>();
        sectionViewWidthsPDF = new ArrayList<>();
        sectionViewHeightsPDF = new ArrayList<>();
        headerLayoutPDF = new LinearLayout(requireContext());
        myView.hiddenSections.removeAllViews();
        myView.hiddenHeader.removeAllViews();
    }
    public void createOnTheFlyHeader(Song thisSong) {
        // Get the song sheet header
        // Once this has drawn, move to the next stage of the song sections
        float scaleComments = mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleComments",0.8f);

        ViewTreeObserver headerVTO = myView.hiddenHeader.getViewTreeObserver();
        headerVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                myView.hiddenHeader.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                headerLayoutWidth = myView.hiddenHeader.getMeasuredWidth();
                headerLayoutHeight = myView.hiddenHeader.getMeasuredHeight();
                myView.hiddenHeader.removeAllViews();
                createOnTheFlySections(thisSong);
            }
        });

        // Now draw it here for measuring via the VTO
        headerLayoutPDF = mainActivityInterface.getSongSheetHeaders().getSongSheet(thisSong,
                scaleComments,true);
        myView.hiddenHeader.addView(headerLayoutPDF);
    }
    public void createOnTheFlySections(Song thisSong) {

        mainActivityInterface.getProcessSong().updateProcessingPreferences();

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
                    for (int x=0; x<myView.hiddenSections.getChildCount(); x++) {
                        View view = myView.hiddenSections.getChildAt(x);
                        int width = view.getMeasuredWidth();
                        int height = view.getMeasuredHeight();
                        sectionViewWidthsPDF.add(width);
                        sectionViewHeightsPDF.add(height);
                    }
                    // Now detach from this view (can only be shown in one layout)
                    myView.hiddenSections.removeAllViews();

                    // Now trigger the next step of preparing the pdf from the views created on the fly
                    listen.setValue(true);
                }
            }
        });

        Log.d(TAG,"sectionViewsPDF.size(): "+sectionViewsPDF.size());
        // Add the section views and this will trigger the VTO
        for (int x=0; x<sectionViewsPDF.size(); x++) {
            myView.hiddenSections.addView(sectionViewsPDF.get(x));
        }
    }

    private void updateProgressText(String text, int curr, int total) {
        myView.progressText.post(() -> {
            String progressText = text + " (" + curr + "/" + total + ")";
            myView.progressText.setText(progressText);
        });
    }

    private void doPrint(boolean isSet) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) requireActivity().getSystemService(Context.PRINT_SERVICE);

        // Set job name, which will be displayed in the print queue
        String jobName = requireActivity().getString(R.string.app_name) + " Document";

        // Start a print job, passing in a PrintDocumentAdapter implementation
        // to handle the generation of a print document

        if (isSet) {
            // Set the variable that will remove gaps from set items on the set list page(s)
            mainActivityInterface.getMakePDF().setIsSetListPrinting(true);

            // For the set, we need a text version of the set
            Log.d(TAG,"setToExport: "+setToExport);
            Log.d(TAG,"setData[0]: "+setData[0]);
            Log.d(TAG,"setData[1]: "+setData[1]);

            // This is sent to the MultipagePrinterAdapter class to deal with
            MultipagePrinterAdapter multipagePrinterAdapter = new MultipagePrinterAdapter(requireActivity());
            multipagePrinterAdapter.updateSetList(this,setToExport,setData[0],setData[1]);
            mainActivityInterface.getMakePDF().setPreferedAttributes();
            printManager.print(jobName, multipagePrinterAdapter,mainActivityInterface.getMakePDF().getPrintAttributes());

        } else {
            PrinterAdapter printerAdapter = new PrinterAdapter(requireActivity());
            printerAdapter.updateSections(sectionViewsPDF, sectionViewWidthsPDF, sectionViewHeightsPDF,
                    headerLayoutPDF, headerLayoutWidth, headerLayoutHeight, getString(R.string.song));
            mainActivityInterface.getMakePDF().setPreferedAttributes();
            printManager.print(jobName, printerAdapter, mainActivityInterface.getMakePDF().getPrintAttributes());
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
    public int getHeaderWidth() {
        return headerLayoutWidth;
    }
    public int getHeaderHeight() {
        return headerLayoutHeight;
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
