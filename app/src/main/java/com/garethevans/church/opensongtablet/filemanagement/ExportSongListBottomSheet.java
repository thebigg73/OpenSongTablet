package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.BottomSheetCommon;
import com.garethevans.church.opensongtablet.customviews.MyMaterialCheckbox;
import com.garethevans.church.opensongtablet.databinding.BottomSheetExportSongListBinding;
import com.garethevans.church.opensongtablet.export.MultipagePrinterAdapter;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;

public class ExportSongListBottomSheet extends BottomSheetCommon {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ExportSongList";
    private MainActivityInterface mainActivityInterface;
    private BottomSheetExportSongListBinding myView;
    private String selectedFolders = "", contentText, contentPDF, contentCSV,
            website_export_song_list_string="", nothing_selected_string="", export_string="",
            app_name_string="", export_song_directory_string="", song_string="";
    private ArrayList<Uri> uris;
    private ArrayList<String> mimeTypes;
    private ArrayList<View> sectionViews;
    private ArrayList<Integer> sectionWidths, sectionHeights;
    private int headerWidth, headerHeight;
    private LinearLayout headerLayoutPDF;
    private boolean printing;
    private Song outputSong;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetExportSongListBinding.inflate(inflater, container, false);
        myView.dialogHeader.setClose(this);

        // Tint the progressBar as the secondary color
        mainActivityInterface.getMyThemeColors().tintProgressBar(myView.progress);

        prepareStrings();

        myView.dialogHeader.setWebHelp(mainActivityInterface,website_export_song_list_string);

        // Build the list of folders
        setupViews();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            website_export_song_list_string = getString(R.string.website_export_song_list);
            nothing_selected_string = getString(R.string.nothing_selected);
            export_string = getString(R.string.export);
            app_name_string = getString(R.string.app_name);
            export_song_directory_string = getString(R.string.export_song_directory);
            song_string = getString(R.string.song);
        }
    }
    private void setupViews() {
        // We'll get these from the database
        ArrayList<String> folders = mainActivityInterface.getSQLiteHelper().getFolders();
        if (getContext()!=null) {
            for (String folder : folders) {
                MyMaterialCheckbox checkBox = getMaterialCheckBox(folder,getContext());
                myView.songFolders.addView(checkBox);
            }
        }

        myView.export.setOnClickListener(view -> {
            resetSectionViews();
            if (!selectedFolders.isEmpty()) {
                printing = false;
                prepareExport();
            } else {
                mainActivityInterface.getShowToast().doItBottomSheet(nothing_selected_string,myView.getRoot());
            }
        });

        myView.print.setOnClickListener(view -> {
            resetSectionViews();
            if (!selectedFolders.isEmpty()) {
                printing = true;
                prepareExport();
            } else {
                mainActivityInterface.getShowToast().doItBottomSheet(nothing_selected_string, myView.getRoot());
            }
        });
    }

    private MyMaterialCheckbox getMaterialCheckBox(String folder, Context c) {
        MyMaterialCheckbox checkBox = new MyMaterialCheckbox(c);
        checkBox.setText(folder);
        checkBox.setChecked(false);
        checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                if (!selectedFolders.contains("\n" + compoundButton.getText() + "\n")) {
                    selectedFolders = selectedFolders + "\n" + compoundButton.getText() + "\n";
                }
            } else {
                selectedFolders = selectedFolders.replace("\n" + compoundButton.getText() + "\n", "");
            }
        });
        return checkBox;
    }

    private void initiateShare() {
        Uri uri;
        if (myView.exportCsv.isChecked()) {
            mimeTypes.add("text/csv");
            mainActivityInterface.getStorageAccess().writeFileFromString("Export", "", "songlist.csv", contentCSV);
            uri = mainActivityInterface.getStorageAccess().getUriForItem("Export", "", "songlist.csv");
            uris.add(uri);
        }

        if (myView.exportPDF.isChecked()) {
            mimeTypes.add("application/pdf");
            uris.add(mainActivityInterface.getMakePDF().createTextPDF(sectionViews, sectionWidths, sectionHeights, myView.headerLayout, headerWidth, headerHeight, "songlist.pdf",
                    null));
        }

        if (myView.exportText.isChecked()) {
            mimeTypes.add("text/plain");
            mainActivityInterface.getStorageAccess().writeFileFromString("Export", "", "songlist.txt", contentCSV);
            uri = mainActivityInterface.getStorageAccess().getUriForItem("Export", "", "songlist.txt");
            uris.add(uri);
        }

        String title = export_string;

        processing(false);

        Intent intent = mainActivityInterface.getExportActions().setShareIntent(contentText, "*/*", null, uris);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, app_name_string + " " +
                export_song_directory_string);
        intent.putExtra(Intent.EXTRA_TEXT, contentText);

        startActivity(Intent.createChooser(intent, title));
    }

    private void processing(boolean starting) {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                if (starting && myView!=null) {
                    myView.scrim.setVisibility(View.VISIBLE);
                    myView.progress.setVisibility(View.VISIBLE);
                    myView.export.setEnabled(false);
                    myView.export.hide();
                    myView.print.setEnabled(false);
                    myView.print.hide();
                } else if (myView!=null) {
                    myView.scrim.setVisibility(View.GONE);
                    myView.progress.setVisibility(View.GONE);
                    myView.export.setEnabled(true);
                    myView.export.show();
                    myView.print.setEnabled(true);
                    myView.print.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private void prepareExport() {
        outputSong = new Song();
        processing(true);
        boolean exportPDF = myView.exportPDF.isChecked();
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Initialise the arrays
            mimeTypes = new ArrayList<>();
            uris = new ArrayList<>();

            // Get the folders chosen
            selectedFolders = selectedFolders.replace("\n\n", "\n");
            String[] folders = selectedFolders.split("\n");
            StringBuilder songContentsTextPDF = new StringBuilder();
            StringBuilder songContentsCSV = new StringBuilder();
            songContentsCSV.append("Folder,Filename,Title,Author,Key");

            for (String folder : folders) {
                if (!folder.isEmpty()) {
                    // For each selected directory, list the songs that exist.
                    ArrayList<Song> songs = mainActivityInterface.getSQLiteHelper().
                            getSongsByFilters(true, false, false,
                                    false, false, false, folder,
                                    null, null, null, null, null,
                                    mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSortTitles",true));
                    songContentsTextPDF.append("[").append(folder).append("]\n");
                    for (Song song : songs) {
                        String songFilename = song.getFilename();
                        String songFolder = song.getFolder();
                        String songTitle = song.getTitle();
                        String songAuthor = song.getAuthor();
                        String songKey = song.getKey();

                        songContentsCSV.append("\n").append(songFolder).append(",").append(songFilename).
                                append(",").append(songTitle).append(",").append(songAuthor).append(",").
                                append(songKey);
                        songContentsTextPDF.append("[]\n");
                        songContentsTextPDF.append(song.getTitle());

                        if (songAuthor != null && !songAuthor.isEmpty()) {
                            songContentsTextPDF.append(", ").append(songAuthor);
                        }

                        if (song.getKey() != null && !song.getKey().isEmpty()) {
                            songContentsTextPDF.append(", ").append(song.getKey());
                        }
                        songContentsTextPDF.append("\n");
                    }
                    songContentsTextPDF.append("\n\n");
                }
            }

            contentCSV = songContentsCSV.toString();
            contentPDF = songContentsTextPDF.toString();
            contentText = contentPDF.replace("[]\n", "").
                    replace("[", "").replace("]", "");

            // Now, if we are expecting a PDF or printing, prepare the views on the UI
            if (exportPDF || printing) {
                outputSong.setLyrics(contentPDF);
                doPrint();
                //preparePDF(mainActivityInterface.getMainHandler());
            } else {
                // Just proceed to the share
                initiateShare();
            }
        });
    }

    private void preparePDF(Handler handler) {
        // First up prepare the header here
        handler.post(() -> {
            outputSong.setTitle(export_song_directory_string);
            myView.headerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    myView.headerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    headerWidth = myView.headerLayout.getWidth();
                    headerHeight = myView.headerLayout.getHeight();
                    // Now prepare the items/sectionviews
                    preparePDFSections(handler);
                }
            });
            myView.headerLayout.addView(mainActivityInterface.getSongSheetHeaders().getSongSheet(outputSong,
                    1f, Color.BLACK));
        });
    }

    private void preparePDFSections(Handler handler) {
        // Set the exporting scale to be min of 0.75f
        mainActivityInterface.getMakePDF().setExportingSongList(true);

        sectionViews = new ArrayList<>();
        sectionWidths = new ArrayList<>();
        sectionHeights = new ArrayList<>();

        myView.sectionViews.removeAllViews();

        outputSong.setLyrics(contentPDF);

        // Create the content for the section views.
        mainActivityInterface.getMakePDF().setIsSetListPrinting(true);
        sectionViews = mainActivityInterface.getProcessSong().
                setSongInLayout(outputSong, true, false);
        mainActivityInterface.getMakePDF().setIsSetListPrinting(false);

        handler.post(() -> {
            // Now we have the views, add them to the temp layout and set up a view tree listener to measure
            ViewTreeObserver sectionsVTO = myView.sectionViews.getViewTreeObserver();
            sectionsVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // The views are ready so lets measure them after clearing this listener

                    // If all the views are there, we can start measuring
                    if (myView.sectionViews.getChildCount() == sectionViews.size()) {
                        myView.sectionViews.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        for (int x = 0; x < myView.sectionViews.getChildCount(); x++) {
                            View view = myView.sectionViews.getChildAt(x);
                            int width = view.getWidth();
                            int height = view.getHeight();
                            sectionWidths.add(width);
                            sectionHeights.add(height);
                        }
                        // Now detach from this view (can only be shown in one layout)
                        myView.sectionViews.removeAllViews();

                        // Now trigger the next step of preparing the pdf from the views created on the fly
                        if (printing) {
                            // We have exported a song as a print layout
                            doPrint();
                        } else {
                            initiateShare();
                        }
                    }
                }
            });

            // Add the section views and this will trigger the VTO
            for (int x = 0; x < sectionViews.size(); x++) {
                myView.sectionViews.addView(sectionViews.get(x));
            }
        });
    }

    private void doPrint() {
        // Get a PrintManager instance
        if (getActivity() != null) {

            PrintManager printManager = (PrintManager) getActivity().getSystemService(Context.PRINT_SERVICE);

            // Set job name, which will be displayed in the print queue
            String jobName = app_name_string + " " + export_song_directory_string;

            // Start a print job, passing in a PrintDocumentAdapter implementation
            // to handle the generation of a print document

            processing(false);

            // Set the variable that will remove gaps from set items on the set list page(s)
            mainActivityInterface.getMakePDF().setIsSetListPrinting(true);
            mainActivityInterface.getProcessSong().setPdfPrinting(true);
            mainActivityInterface.getProcessSong().setForceSinglePagePDF(false);


            // This is sent to the MultipagePrinterAdapter class to deal with
            MultipagePrinterAdapter multipagePrinterAdapter = new MultipagePrinterAdapter(getActivity());
            outputSong.setTitle(export_song_directory_string);
            outputSong.setUser1("PRINT_SONG_LIST");
            multipagePrinterAdapter.updateSetList(null,this, outputSong.getTitle(), null,null, null);
            multipagePrinterAdapter.setThisSong(outputSong);
            mainActivityInterface.getMakePDF().setPreferedAttributes();
            mainActivityInterface.getMakePDF().setForceSinglePage(false);
            printManager.print(jobName, multipagePrinterAdapter, mainActivityInterface.getMakePDF().getPrintAttributes());


            // Set the exporting scale to be min of 0.75f

            /*
            mainActivityInterface.getMakePDF().setExportingSongList(true);

            PrinterAdapter printerAdapter = new PrinterAdapter(getActivity());
            printerAdapter.updateSections(sectionViews, sectionWidths, sectionHeights,
                    myView.headerLayout, headerWidth, headerHeight, song_string);
            mainActivityInterface.getMakePDF().setPreferedAttributes();
            printManager.print(jobName, printerAdapter, mainActivityInterface.getMakePDF().getPrintAttributes());

             */
        }
    }

    public LinearLayout getHeaderLayout() {
        return myView.headerLayout;
    }

    public RelativeLayout getSectionLayout() {
        return myView.sectionViews;
    }

    public ArrayList<View> getSectionViews() {
        return sectionViews;
    }

    public ArrayList<Integer> getSectionWidths() {
        return sectionWidths;
    }

    public ArrayList<Integer> getSectionHeights() {
        return sectionHeights;
    }


    public void setHeaderLayoutPDF(LinearLayout headerLayoutPDF) {
        this.headerLayoutPDF = headerLayoutPDF;
    }

    public LinearLayout getHeaderLayoutPDF() {
        return headerLayoutPDF;
    }

    public void resetSectionViews() {
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView != null) {
                try {
                    myView.sectionViews.removeAllViews();
                    myView.headerLayout.removeAllViews();
                }  catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        sectionViews = new ArrayList<>();
        sectionWidths = new ArrayList<>();
        sectionHeights = new ArrayList<>();
        mimeTypes = new ArrayList<>();
        uris = new ArrayList<>();
    }
}
