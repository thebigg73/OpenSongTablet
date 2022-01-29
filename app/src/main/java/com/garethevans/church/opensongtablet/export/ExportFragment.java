package com.garethevans.church.opensongtablet.export;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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

public class ExportFragment extends Fragment {

    private SettingsExportBinding myView;
    private MainActivityInterface mainActivityInterface;
    private ArrayList<Uri> uris;
    private Uri uri;
    private final String TAG = "ExportFragment";
    private final MutableLiveData<Boolean> listen = new MutableLiveData<>();
    private ArrayList<View> sectionViewsPDF;
    private ArrayList<Integer> sectionViewWidthsPDF, sectionViewHeightsPDF;
    private LinearLayout headerLayoutPDF;
    private int headerLayoutWidth, headerLayoutHeight;
    private String setToExport = null;
    private int songsToAdd, songsProcessed;
    boolean openSong = false;
    boolean openSongApp = false;
    boolean pdf = false;
    boolean onsong = false;
    boolean openSongSet = false;
    boolean openSongAppSet = false;
    boolean includeSongs = false;
    boolean textSet = false;
    volatile boolean processingSetPDFs = false;

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

        // Some options are hidden by default and only visible if we have a proper OpenSong song
        // If exporting a set, some options aren't allowed
        showUsable();

        myView.shareButton.setOnClickListener(v -> prepareExport());

        myView.nestedScrollView.setFabToAnimate(myView.shareButton);

        return myView.getRoot();
    }

    private void showUsable() {

        // By default everything is hidden.  Only make the correct ones visible

        // Check if we are exporting a set
        if (mainActivityInterface.getWhattodo().startsWith("exportset:")) {
            setToExport = mainActivityInterface.getWhattodo().replace("exportset:","");

            // Set the default
            if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"exportOpenSongAppSet",true)) {
                myView.openSongAppSet.setChecked(true);
            } else {
                myView.openSongSet.setChecked(true);
            }

            // The listener for the include songs
            myView.includeSongs.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    myView.songOptionsLayout.setVisibility(View.VISIBLE);
                } else {
                    myView.songOptionsLayout.setVisibility(View.GONE);
                }
            });

            // If we are exporting songs, hide the ones not allowed for sets
            myView.image.setVisibility(View.GONE);
            myView.text.setVisibility(View.GONE);
            myView.chordPro.setVisibility(View.GONE);

            // Now show the set view
            myView.setOptionsLayout.setVisibility(View.VISIBLE);

        } else {
            // Hide the options based on the song format
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
                    if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(), "exportOpenSongApp", true)) {
                        myView.openSongApp.setChecked(true);
                    } else {
                        myView.openSong.setChecked(true);
                    }
                    break;
            }

            // Now show the song options
            myView.songOptionsLayout.setVisibility(View.VISIBLE);
        }

        // Make sure the progress bar is hidden
        myView.progressBar.setVisibility(View.GONE);
        myView.progressText.setVisibility(View.GONE);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void prepareExport() {
        openSong = myView.openSong.isChecked();
        openSongApp = myView.openSongApp.isChecked();
        pdf = myView.pdf.isChecked();
        onsong = myView.onSong.isChecked();
        openSongSet = myView.openSongSet.isChecked();
        openSongAppSet = myView.openSongAppSet.isChecked();
        includeSongs = myView.includeSongs.isChecked();
        textSet = myView.textSet.isChecked();

        // Do this in a new Thread
        new Thread(() -> {

            // If we are exporting a song, we do this once, if not, we do it for each song in the set
            requireActivity().runOnUiThread(() -> {
                myView.progressBar.setVisibility(View.VISIBLE);
                myView.progressText.setVisibility(View.VISIBLE);
                myView.shareButton.setEnabled(false);
            });

            if (mainActivityInterface.getWhattodo().startsWith("exportset:")) {
                uris = new ArrayList<>();
                ArrayList<String> setNames = mainActivityInterface.getExportActions().getListOfSets(setToExport);

                updateProgressText(getString(R.string.set));

                // Add the set files
                if (openSongAppSet) {
                    mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(), "exportOpenSongAppSet", true);
                    // Copy the set(s) to an .osts file extensions and get the uri(s)
                    uris.addAll(mainActivityInterface.getExportActions().addOpenSongAppSetsToUris(requireContext(),
                            mainActivityInterface, setNames));
                } else if (openSongSet) {
                    mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(), "exportOpenSongAppSet", false);
                    // Just add the actual set file(s) (no extension)
                    uris.addAll(mainActivityInterface.getExportActions().addOpenSongSetsToUris(requireContext(),
                            mainActivityInterface, setNames));
                }

                String[] setData = new String[2];
                if (textSet || includeSongs) {
                    setData = mainActivityInterface.getExportActions().parseSets(requireContext(),
                            mainActivityInterface, setNames);
                    if (textSet) {
                        mainActivityInterface.getStorageAccess().doStringWriteToFile(requireContext(),
                                mainActivityInterface,"Export","","Set.txt",setData[1]);
                        uris.add(mainActivityInterface.getStorageAccess().getUriForItem(requireContext(),
                                mainActivityInterface,"Export","","Set.txt"));
                    }
                }

                // Now deal with any songs in the sets
                if (includeSongs) {
                    String[] ids = setData[0].split("\n");
                    StringBuilder songsAlreadyAdded = new StringBuilder();
                    songsToAdd = ids.length;
                    songsProcessed = 0;
                    processingSetPDFs = false;

                    for (String id : ids) {
                        // Only add if we don't already have it (as we may have multiple references to songs
                        // Especially is we have selected more than one set)
                        if (!songsAlreadyAdded.toString().contains(id)) {
                            songsAlreadyAdded.append("\n").append(id);
                            String[] location = mainActivityInterface.getExportActions().getFolderAndFile(requireContext(), id);
                            updateProgressText(location[1]);
                            boolean likelyXML = !location[1].contains(".") || location[1].toLowerCase(Locale.ROOT).endsWith(".xml");
                            boolean likelyPDF = location[1].toLowerCase(Locale.ROOT).endsWith(".pdf");

                            if ((openSong && likelyXML) || (pdf && likelyPDF)) {
                                // Just get a uri for the song
                                uris.add(mainActivityInterface.getStorageAccess().getUriForItem(requireContext(),
                                        mainActivityInterface, "Songs", location[0], location[1]));
                                songsProcessed++;


                            } else if (openSongApp && likelyXML) {
                                uris.add(mainActivityInterface.getStorageAccess().copyFromTo(requireContext(),
                                        mainActivityInterface, "Songs", location[0], location[1],
                                        "Export", "", location[1] + ".ost"));
                                songsProcessed++;

                            } else if (onsong && likelyXML) {
                                // Get the text from the file
                                Song song = mainActivityInterface.getSQLiteHelper().getSpecificSong(requireContext(),
                                        mainActivityInterface, location[0], location[1]);
                                String content = mainActivityInterface.getPrepareFormats().getSongAsOnSong(requireContext(),
                                        mainActivityInterface, song);
                                if (mainActivityInterface.getStorageAccess().doStringWriteToFile(requireContext(), mainActivityInterface, "Export", "", location[1] + ".onsong", content)) {
                                    uris.add(mainActivityInterface.getStorageAccess().getUriForItem(requireContext(), mainActivityInterface, "Export", "", location[1] + ".onsong"));
                                }
                                songsProcessed++;

                            } else if (pdf && likelyXML) {
                                // This bit takes more time as we have to parse and draw the pdf
                                // Don't up the songs processed here as we need to wait for drawing
                                while (processingSetPDFs) {
                                    // Just wait
                                }

                                // We are about to start process, so set the variable
                                // This stops the next pdf from overwriting this one if it is slow
                                processingSetPDFs = true;

                                requireActivity().runOnUiThread(() -> {
                                    listen.setValue(false); //Initilize with a value
                                    Song song = mainActivityInterface.getSQLiteHelper().getSpecificSong(requireContext(),
                                            mainActivityInterface, location[0], location[1]);
                                    createOnTheFly(requireContext(), mainActivityInterface, song);
                                    listen.observe(getViewLifecycleOwner(), isDone -> {
                                        if (isDone) {
                                            uris.add(mainActivityInterface.getMakePDF().createTextPDF(requireContext(), mainActivityInterface,
                                                    sectionViewsPDF, sectionViewWidthsPDF,
                                                    sectionViewHeightsPDF, headerLayoutPDF, headerLayoutWidth, headerLayoutHeight, location[1] + ".pdf"));
                                            listen.removeObservers(getViewLifecycleOwner());
                                            songsProcessed++;
                                            processingSetPDFs = false;
                                            if (songsProcessed == songsToAdd) {
                                                requireActivity().runOnUiThread(this::openIntent);
                                            }
                                        }
                                    });
                                });

                            } else {
                                // Do nothing
                                songsProcessed++;
                            }
                        }
                    }
                }

                // If all is well, we send the intent.
                // If we are still processing a pdf, we check again at the end of that pass
                if (songsProcessed == songsToAdd) {
                    requireActivity().runOnUiThread(this::openIntent);
                }

            } else {
                if (myView.pdf.isChecked()) {
                    if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                        // We are just copying an existing PDF
                        initiateShare("application/pdf");

                    } else {
                        // Create PDF song on the fly
                        requireActivity().runOnUiThread(() -> {
                            listen.setValue(false); //Initilize with a value
                            createOnTheFly(requireContext(), mainActivityInterface, mainActivityInterface.getSong());
                            listen.observe(getViewLifecycleOwner(), isDone -> {
                                if (isDone) {
                                    listen.removeObservers(getViewLifecycleOwner());
                                    initiateShare("application/pdf");
                                }
                            });
                        });
                    }

                } else if (myView.onSong.isChecked() || myView.chordPro.isChecked() || myView.text.isChecked()) {
                    initiateShare("text/plain");

                } else if (myView.openSong.isChecked() || myView.openSongApp.isChecked()) {
                    mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(), "exportOpenSongApp", myView.openSongApp.isChecked());
                    initiateShare("text/xml");

                } else if (myView.image.isChecked()) {
                    initiateShare("image/*");
                }
            }
        }).start();
    }

    public void openIntent() {
        Intent intent = mainActivityInterface.getExportActions().setShareIntent(setToExport, "*/*", null, uris);
        startActivity(Intent.createChooser(intent, getString(R.string.export_set).replace("(.osts)", "")));
        myView.progressBar.setVisibility(View.GONE);
        myView.progressText.setVisibility(View.GONE);
        myView.shareButton.setEnabled(true);
    }

    private void initiateShare(String type) {
        String exportFilename;
        String content = null;

        // Sharing a song should initiate the CCLI Log of printed (value 6)
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"ccliAutomaticLogging",false)) {
            mainActivityInterface.getCCLILog().addEntry(requireContext(),mainActivityInterface,mainActivityInterface.getSong(),"6");
        }

        if ((mainActivityInterface.getSong().getFiletype().equals("PDF") && type.equals("application/pdf") && myView.pdf.isChecked()) ||
                (mainActivityInterface.getSong().getFiletype().equals("IMG") && type.equals("image/*") && myView.image.isChecked())) {
            // This is for PDF or image songs  Only allow export in the same format
            uri = mainActivityInterface.getExportActions().getActualSongFile(requireContext(),mainActivityInterface,mainActivityInterface.getSong());

        } else if (mainActivityInterface.getSong().getFiletype().equals("XML")){
            // These options are for OpenSong songs
            if (type.equals("application/pdf")) {
                exportFilename = getExportFilename(mainActivityInterface.getSong(),".pdf");

                // Create a PDF on the fly
                uri = mainActivityInterface.getMakePDF().createTextPDF(requireContext(), mainActivityInterface,
                        sectionViewsPDF, sectionViewWidthsPDF, sectionViewHeightsPDF, headerLayoutPDF,
                        headerLayoutWidth, headerLayoutHeight, exportFilename);

            } else if (type.equals("text/xml") && myView.openSongApp.isChecked()) {
                // Make a copy of the file as an .ost
                //exportFilename = getExportFilename(mainActivityInterface.getSong(),".ost");
                uri = copyOpenSongApp(mainActivityInterface.getSong());

            } else if (type.equals("text/xml") && myView.openSong.isChecked()) {
                // Make a copy of the file as is
                uri = mainActivityInterface.getExportActions().getActualSongFile(requireContext(),mainActivityInterface,mainActivityInterface.getSong());
                //exportFilename = getExportFilename(mainActivityInterface.getSong(),"");

            } else if (type.equals("text/plain") && myView.onSong.isChecked()) {
                // Convert the song to OnSong format and save it
                content = mainActivityInterface.getPrepareFormats().getSongAsOnSong(requireContext(),
                        mainActivityInterface, mainActivityInterface.getSong());
                uri = getExportUri(mainActivityInterface.getSong(),".onsong");
                exportFilename = getExportFilename(mainActivityInterface.getSong(),".onsong");
                mainActivityInterface.getStorageAccess().doStringWriteToFile(requireContext(),mainActivityInterface,"Export","",exportFilename,content);


            } else if (type.equals("text/plain") && myView.chordPro.isChecked()) {
                // Convert the song to ChordPro format and save it
                content = mainActivityInterface.getPrepareFormats().getSongAsChoPro(requireContext(),
                        mainActivityInterface, mainActivityInterface.getSong());
                uri = getExportUri(mainActivityInterface.getSong(),".cho");
                exportFilename = getExportFilename(mainActivityInterface.getSong(),".cho");
                mainActivityInterface.getStorageAccess().doStringWriteToFile(requireContext(),mainActivityInterface,"Export","",exportFilename,content);


            } else if (type.equals("text/plain") && myView.text.isChecked()) {
                // Convert the song to text format and save it
                content = mainActivityInterface.getPrepareFormats().getSongAsText(mainActivityInterface.getSong());
                uri = getExportUri(mainActivityInterface.getSong(),".txt");
                exportFilename = getExportFilename(mainActivityInterface.getSong(),".txt");
                mainActivityInterface.getStorageAccess().doStringWriteToFile(requireContext(),mainActivityInterface,"Export","",exportFilename,content);
            }
        }
        Intent intent = mainActivityInterface.getExportActions().setShareIntent(content,type,uri,uris);
        requireActivity().runOnUiThread(() -> {
            startActivity(Intent.createChooser(intent,getString(R.string.export_current_song)));
            myView.progressBar.setVisibility(View.GONE);
            myView.shareButton.setEnabled(true);
        });
    }

    private Uri copyOpenSongApp(Song song) {
        uri = getExportUri(song,".ost");
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(requireContext(),
                mainActivityInterface.getExportActions().getActualSongFile(requireContext(),
                        mainActivityInterface,mainActivityInterface.getSong()));
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(requireContext(),uri);
        mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream);
        return uri;
    }
    private String getExportFilename(Song song, String extension) {
        return song.getFolder().replace("/","_") + "_" +
                song.getFilename().replace("/","_") + extension;
    }
    private Uri getExportUri(Song song, String extension) {
        String exportFilename = getExportFilename(song, extension);
        uri = mainActivityInterface.getStorageAccess().getUriForItem(requireContext(),
                mainActivityInterface,"Export","",
                exportFilename);

        // If the output file exists, delete it first (so we sort of overwrite)
        if (mainActivityInterface.getStorageAccess().uriExists(requireContext(),uri)) {
            mainActivityInterface.getStorageAccess().deleteFile(requireContext(),uri);
        }

        // Now create a blank file ready to assign an outputStream
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(requireContext(),
                mainActivityInterface,uri,null,"Export","",
                exportFilename);

        return uri;
    }


    // We can create nice views on the fly here by processing the Song, then populating the views
    private void createOnTheFly(Context c, MainActivityInterface mainActivityInterface, Song thisSong) {
        // Make sure any current headers/sections are wiped
        initialiseViews();

        // Now start preparing the views.  First up the header
        createOnTheFlyHeader(c,mainActivityInterface,thisSong);
    }
    private void initialiseViews() {
        sectionViewsPDF = new ArrayList<>();
        sectionViewWidthsPDF = new ArrayList<>();
        sectionViewHeightsPDF = new ArrayList<>();
        headerLayoutPDF = new LinearLayout(requireContext());
        myView.hiddenSections.removeAllViews();
        myView.hiddenHeader.removeAllViews();
    }
    private void createOnTheFlyHeader(Context c, MainActivityInterface mainActivityInterface, Song thisSong) {
        // Get the song sheet header
        // Once this has drawn, move to the next stage of the song sections
        float scaleComments = mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"scaleComments",0.8f);

        ViewTreeObserver headerVTO = myView.hiddenHeader.getViewTreeObserver();
        headerVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                myView.hiddenHeader.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                headerLayoutWidth = myView.hiddenHeader.getMeasuredWidth();
                headerLayoutHeight = myView.hiddenHeader.getMeasuredHeight();
                myView.hiddenHeader.removeAllViews();
                createOnTheFlySections(c,mainActivityInterface,thisSong);
            }
        });

        // Now draw it here for measuring via the VTO
        headerLayoutPDF = mainActivityInterface.getSongSheetHeaders().getSongSheet(c,mainActivityInterface,thisSong,
                scaleComments,true);
        myView.hiddenHeader.addView(headerLayoutPDF);
    }
    private void createOnTheFlySections(Context c, MainActivityInterface mainActivityInterface, Song thisSong) {

        mainActivityInterface.getProcessSong().updateProcessingPreferences(c, mainActivityInterface);

        // Create the content for the section views.
        sectionViewsPDF = mainActivityInterface.getProcessSong().
                setSongInLayout(c,mainActivityInterface,thisSong,true, false);

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
        // Add the section views and this will trigger the VTO
        for (int x=0; x<sectionViewsPDF.size(); x++) {
            myView.hiddenSections.addView(sectionViewsPDF.get(x));
        }
    }

    private void updateProgressText(String text) {
        myView.progressText.post(() -> {
            String progressText = getString(R.string.processing) + ": " + text;
            myView.progressText.setText(progressText);
        });
    }
}
