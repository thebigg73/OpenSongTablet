package com.garethevans.church.opensongtablet.export;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.lifecycle.MutableLiveData;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class MultipagePrinterAdapter extends PrintDocumentAdapter {

    private final MutableLiveData<Boolean> listen = new MutableLiveData<>();
    @SuppressWarnings({"unused","variablecanbelocal"})
    private final String TAG = "MutliPagePrint";
    private final MainActivityInterface mainActivityInterface;
    private String setName;
    private Uri uri;
    private ArrayList<View> sectionViewsPDF;
    private ArrayList<String> setItemLocations, setItemEntries, setItemKeys;
    private int headerLayoutWidth;
    private int headerLayoutHeight;
    private ExportFragment exportFragment;
    private LayoutResultCallback layoutResultCallback;
    private int currentSetItem;

    // THIS IS USED TO MAKE MULTIPAGE PDF FILES FROM SETS WITH THE SONGS IN ONE PDF

    public MultipagePrinterAdapter(Activity activity) {
        mainActivityInterface = (MainActivityInterface) activity;
        sectionViewsPDF = new ArrayList<>();
    }

    public void updateSetList(ExportFragment exportFragment, String setName, String setList, String setEntries, String setKeys) {
        String[] sil = setList.split("\n");
        String[] sie = setEntries.split("\n");
        String[] sik = setKeys.split("\n");
        setItemLocations = new ArrayList<>();
        setItemEntries = new ArrayList<>();
        setItemKeys = new ArrayList<>();
        Collections.addAll(setItemLocations, sil);
        Collections.addAll(setItemEntries, sie);
        Collections.addAll(setItemKeys, sik);
        this.setName = setName;
        this.exportFragment = exportFragment;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes printAttributes,
                         CancellationSignal cancellationSignal, LayoutResultCallback layoutResultCallback,
                         Bundle extras) {
        // The user has chosen a printer, orientation, etc that decides a layout
        this.layoutResultCallback = layoutResultCallback;

        // Respond to cancellation request
        if (cancellationSignal.isCanceled()) {
            layoutResultCallback.onLayoutCancelled();
            return;
        }

        mainActivityInterface.getMakePDF().createBlankPDFDoc(setName+".pdf",printAttributes);

        // Create the first section of the PDF - the set list
        mainActivityInterface.getMakePDF().setIsSetListPrinting(true);
        Song tempSong = new Song();
        tempSong.setTitle(setName);
        StringBuilder setItems = new StringBuilder();
        for (String setItemEntry:setItemEntries) {
            setItems.append(setItemEntry).append("\n[]\n");
        }
        tempSong.setLyrics(setItems.toString());

        listen.setValue(false);
        mainActivityInterface.getProcessSong().updateProcessingPreferences();

        createOnTheFlyHeader(tempSong,true);
    }

    public void createOnTheFlyHeader(Song thisSong,boolean theSetList) {
        // Get the song sheet header
        // Once this has drawn, move to the next stage of the song sections
        float scaleComments = mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleComments",0.8f);

        ViewTreeObserver headerVTO = exportFragment.getHiddenHeader().getViewTreeObserver();
        headerVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                exportFragment.getHiddenHeader().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                headerLayoutWidth = exportFragment.getHiddenHeader().getWidth();
                headerLayoutHeight = exportFragment.getHiddenHeader().getHeight();
                exportFragment.getHiddenHeader().removeAllViews();
                createOnTheFlySections(thisSong,theSetList);
            }
        });

        // Now draw it here for measuring via the VTO
        exportFragment.setHeaderLayoutPDF(mainActivityInterface.getSongSheetHeaders().getSongSheet(thisSong,
                scaleComments, Color.BLACK));
        if (exportFragment.getHeaderLayout()==null) {
            exportFragment.setHeaderLayoutPDF(new LinearLayout(exportFragment.getHiddenHeader().getContext()));
        }

        exportFragment.getHiddenHeader().addView(exportFragment.getHeaderLayout());
    }

    public void createOnTheFlySections(Song thisSong,boolean theSetList) {
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

        exportFragment.resetSectionViews();

        // Now we have the views, add them to the temp layout and set up a view tree listener to measure
        ViewTreeObserver sectionsVTO = exportFragment.getHiddenSections().getViewTreeObserver();
        sectionsVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // The views are ready so lets measure them after clearing this listener

                // If all the views are there, we can start measuring
                if (exportFragment.getHiddenSections().getChildCount()==sectionViewsPDF.size()) {
                    exportFragment.getHiddenSections().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    for (int x=0; x<exportFragment.getHiddenSections().getChildCount(); x++) {
                        View view = exportFragment.getHiddenSections().getChildAt(x);
                        int width = view.getMeasuredWidth();
                        int height = view.getMeasuredHeight();
                        exportFragment.getSectionViews().add(view);
                        exportFragment.getSectionWidths().add(width);
                        exportFragment.getSectionHeights().add(height);
                    }
                    // Now detach from this view (can only be shown in one layout)
                    exportFragment.getHiddenSections().removeAllViews();

                    // Now trigger the next step of preparing the pdf from the views created on the fly
                    listen.setValue(true);

                    mainActivityInterface.getMakePDF().addCurrentItemToPDF(exportFragment.getSectionViews(),
                            exportFragment.getSectionWidths(),exportFragment.getSectionHeights(),
                            exportFragment.getHeaderLayout(),headerLayoutWidth,
                            headerLayoutHeight);

                    if (theSetList) {
                        // Now we have finished the set list, deal with the content/songs
                        currentSetItem = 0;
                        getSongOrPrintIfDone();
                    } else {
                        // Move to the next song
                        currentSetItem++;
                        getSongOrPrintIfDone();
                    }

                }
            }
        });

        // Add the section views and this will trigger the VTO
        for (int x=0; x<sectionViewsPDF.size(); x++) {
            exportFragment.getHiddenSections().addView(sectionViewsPDF.get(x));
        }
    }

    private void getSongOrPrintIfDone() {
        Log.d(TAG,"getSongOrPrintIfDone()  exportSetSongs:"+mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportSetSongs",false)+
                "    currentSetItem:"+currentSetItem+"    setItemEntries.size():"+setItemEntries.size());
        if (!mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportSetSongs",true) ||
                currentSetItem>=setItemEntries.size()) {
            callPrint();
        } else if (setItemLocations.size()>currentSetItem && !setItemLocations.get(currentSetItem).equals("ignore")) {
            // Initialse the song for processing
            Song currentSetSong;
            if (setItemLocations.get(currentSetItem).contains("../") ||
            setItemLocations.get(currentSetItem).contains("**")) {
                String s = setItemLocations.get(currentSetItem);
                s = s.replace("../","**");
                // This is a custom file - load it!
                String[] location = s.split("/");
                currentSetSong = new Song();
                //currentSetSong.setFolder(location[0]);
                currentSetSong.setFolder("../Export");
                currentSetSong.setFilename(location[1]);
                Log.d(TAG,"loading song at :"+currentSetSong.getFolder()+"/"+currentSetSong.getFilename());
                currentSetSong = mainActivityInterface.getLoadSong().doLoadSongFile(currentSetSong,false);
            } else {
                if (setItemLocations.get(currentSetItem).contains("/")) {
                    String[] location = setItemLocations.get(currentSetItem).split("/");
                    currentSetSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(location[0], location[1]);
                } else {
                    currentSetSong = mainActivityInterface.getSQLiteHelper().getSpecificSong("", setItemLocations.get(currentSetItem));
                }
            }

            // If we have transposed this song in the set on the fly, match the key here
            if (!setItemKeys.get(currentSetItem).equals("ignore") && !setItemKeys.get(currentSetItem).trim().isEmpty() && currentSetSong.getKey()!=null && !currentSetSong.getKey().isEmpty() &&
                    !setItemKeys.get(currentSetItem).trim().equals(currentSetSong.getKey())) {
                int transposeTimes = mainActivityInterface.getTranspose().getTransposeTimes(currentSetSong.getKey(),setItemKeys.get(currentSetItem).trim());
                mainActivityInterface.getTranspose().checkChordFormat(currentSetSong);
                currentSetSong = mainActivityInterface.getTranspose().doTranspose(currentSetSong,"+1",transposeTimes,currentSetSong.getDetectedChordFormat(),currentSetSong.getDesiredChordFormat());
            }

            // Now do the header.  Once this is done, it does the content, then moves to the next song
            createOnTheFlyHeader(currentSetSong,false);
        } else if (setItemLocations.size()>currentSetItem && setItemLocations.get(currentSetItem).equals("ignore")) {
            currentSetItem++;
            getSongOrPrintIfDone();
        } else {
            currentSetItem++;
            callPrint();
        }
    }

    private void callPrint() {
        // Actual PDF document
        if (setName==null) {
            setName = mainActivityInterface.getMakePDF().getExportFilename();
        }

        uri = mainActivityInterface.getMakePDF().getPDFFile(setName+".pdf");
        PdfDocument pdfDocument = mainActivityInterface.getMakePDF().getPdfDocument();

        // Compute the expected number of printed pages
        int totalPages = pdfDocument.getPages().size();

        if (totalPages > 0) {
            // Return print information to print framework
            PrintDocumentInfo info = new PrintDocumentInfo
                    .Builder(setName + ".pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(totalPages)
                    .build();
            // Content layout reflow is complete
            if (layoutResultCallback!=null) {
                layoutResultCallback.onLayoutFinished(info, true);
            }
        } else {
            // Otherwise report an error to the print framework
            if (layoutResultCallback!=null) {
                layoutResultCallback.onLayoutFailed("Page count calculation failed.");
            }
        }
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                        CancellationSignal cancellationSignal, WriteResultCallback callback) {
        // The user has chosen the pages, format and clicked the print button

        try (InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
             OutputStream outputStream = new FileOutputStream(destination.getFileDescriptor())) {
            byte[] buf=new byte[16384];
            int size;

            while ((size=inputStream.read(buf)) >= 0
                    && !cancellationSignal.isCanceled()) {
                outputStream.write(buf, 0, size);
            }

            if (cancellationSignal.isCanceled()) {
                callback.onWriteCancelled();
            } else {
                callback.onWriteFinished(pages);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
