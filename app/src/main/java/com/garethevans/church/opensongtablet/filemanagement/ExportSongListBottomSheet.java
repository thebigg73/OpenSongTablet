package com.garethevans.church.opensongtablet.filemanagement;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetExportSongListBinding;
import com.garethevans.church.opensongtablet.export.PrinterAdapter;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExportSongListBottomSheet extends BottomSheetDialogFragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ExportSongList";
    private MainActivityInterface mainActivityInterface;
    private BottomSheetExportSongListBinding myView;
    private String selectedFolders = "", contentText, contentPDF, contentCSV;
    private ArrayList<Uri> uris;
    private ArrayList<String> mimeTypes;
    private ArrayList<View> sectionViews;
    private ArrayList<Integer> sectionWidths, sectionHeights;
    private int headerWidth, headerHeight;
    private boolean printing;
    private Song outputSong;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetExportSongListBinding.inflate(inflater, container, false);
        myView.dialogHeader.setClose(this);
        myView.dialogHeader.setWebHelp(mainActivityInterface,getString(R.string.website_export_song_list));

        // Build the list of folders
        setupViews();

        return myView.getRoot();
    }

    private void setupViews() {
        // We'll get these from the database
        ArrayList<String> folders = mainActivityInterface.getSQLiteHelper().getFolders();
        for (String folder : folders) {
            MaterialCheckBox checkBox = new MaterialCheckBox(requireContext());
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
            myView.songFolders.addView(checkBox);
        }

        myView.export.setOnClickListener(view -> {
            if (!selectedFolders.isEmpty()) {
                printing = false;
                prepareExport();
            } else {
                mainActivityInterface.getShowToast().doIt(getString(R.string.nothing_selected));
            }
        });

        myView.print.setOnClickListener(view -> {
            if (!selectedFolders.isEmpty()) {
                printing = true;
                prepareExport();
            } else {
                mainActivityInterface.getShowToast().doIt(getString(R.string.nothing_selected));
            }
        });
    }

    private void initiateShare() {
        Uri uri;
        if (myView.exportCsv.isChecked()) {
            mimeTypes.add("text/csv");
            uri = mainActivityInterface.getStorageAccess().getUriForItem("Export", "", "songlist.csv");
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, uri, null, "Export", "", "songlist.csv");
            mainActivityInterface.getStorageAccess().doStringWriteToFile("Export", "", "songlist.csv", contentCSV);
            uris.add(uri);
        }

        if (myView.exportPDF.isChecked()) {
            mimeTypes.add("application/pdf");
            uris.add(mainActivityInterface.getMakePDF().createTextPDF(sectionViews, sectionWidths, sectionHeights, myView.headerLayout, headerWidth, headerHeight, "songlist.pdf",
                    null));
        }

        if (myView.exportText.isChecked()) {
            mimeTypes.add("text/plain");
            uri = mainActivityInterface.getStorageAccess().getUriForItem("Export", "", "songlist.txt");
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, uri, null, "Export", "", "songlist.txt");
            mainActivityInterface.getStorageAccess().doStringWriteToFile("Export", "", "songlist.txt", contentCSV);
            uris.add(uri);
        }

        String title = getString(R.string.export);

        processing(false);

        Intent intent = mainActivityInterface.getExportActions().setShareIntent(contentText, "*/*", null, uris);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, requireActivity().getString(R.string.app_name) + " " +
                getString(R.string.export_song_directory));
        intent.putExtra(Intent.EXTRA_TEXT, contentText);

        startActivity(Intent.createChooser(intent, title));
    }

    private void processing(boolean starting) {
        if (starting) {
            myView.scrim.setVisibility(View.VISIBLE);
            myView.progress.setVisibility(View.VISIBLE);
            myView.export.setEnabled(false);
            myView.export.hide();
            myView.print.setEnabled(false);
            myView.print.hide();
        } else {
            myView.scrim.setVisibility(View.GONE);
            myView.progress.setVisibility(View.GONE);
            myView.export.setEnabled(true);
            myView.export.show();
            myView.print.setEnabled(true);
            myView.print.show();
        }
    }

    private void prepareExport() {
        outputSong = new Song();
        processing(true);
        boolean exportPDF = myView.exportPDF.isChecked();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());

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
                preparePDF(handler);
            } else {
                // Just proceed to the share
                initiateShare();
            }
        });
    }

    private void preparePDF(Handler handler) {
        // First up prepare the header here
        handler.post(() -> {
            outputSong.setTitle(getString(R.string.export_song_directory));
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
                    1f, true));
        });
    }

    private void preparePDFSections(Handler handler) {
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
        PrintManager printManager = (PrintManager) requireActivity().getSystemService(Context.PRINT_SERVICE);

        // Set job name, which will be displayed in the print queue
        String jobName = requireActivity().getString(R.string.app_name) + " " + getString(R.string.export_song_directory);

        // Start a print job, passing in a PrintDocumentAdapter implementation
        // to handle the generation of a print document

        processing(false);

        PrinterAdapter printerAdapter = new PrinterAdapter(requireActivity());
        printerAdapter.updateSections(sectionViews, sectionWidths, sectionHeights,
                myView.headerLayout, headerWidth, headerHeight, getString(R.string.song));
        mainActivityInterface.getMakePDF().setPreferedAttributes();
        printManager.print(jobName, printerAdapter, mainActivityInterface.getMakePDF().getPrintAttributes());
    }
}
