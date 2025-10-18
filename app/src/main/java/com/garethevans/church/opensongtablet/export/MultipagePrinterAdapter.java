package com.garethevans.church.opensongtablet.export;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.lifecycle.MutableLiveData;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;
import com.garethevans.church.opensongtablet.filemanagement.ExportSongListBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class MultipagePrinterAdapter extends PrintDocumentAdapter {

    private final MutableLiveData<Boolean> listen = new MutableLiveData<>();
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MutliPagePrint";
    private final MainActivityInterface mainActivityInterface;
    private String setName;
    private Uri uri;
    private ArrayList<View> sectionViewsPDF;
    private ArrayList<String> setItemLocations, setItemEntries, setItemKeys;
    private int headerLayoutWidth;
    private int headerLayoutHeight;
    private ExportFragment exportFragment;
    private ExportSongListBottomSheet exportSongListBottomSheet;
    private LayoutResultCallback layoutResultCallback;
    private int currentSetItem;
    private final Context c;
    private Song thisSong;

    // THIS IS USED TO MAKE MULTIPAGE PDF FILES FROM SETS WITH THE SONGS IN ONE PDF

    public MultipagePrinterAdapter(Activity activity) {
        mainActivityInterface = (MainActivityInterface) activity;
        sectionViewsPDF = new ArrayList<>();
        c = activity;
    }

    public void updateSetList(ExportFragment exportFragment, ExportSongListBottomSheet exportSongListBottomSheet, String setName, String setList, String setEntries, String setKeys) {
        setItemLocations = new ArrayList<>();
        setItemEntries = new ArrayList<>();
        setItemKeys = new ArrayList<>();
        if (setList!=null) {
            String[] sil = setList.split("\n");
            Collections.addAll(setItemLocations, sil);
        }
        if (setEntries!=null) {
            String[] sie = setEntries.split("\n");
            Collections.addAll(setItemEntries, sie);
        }
        if (setKeys!=null) {
            String[] sik = setKeys.split("\n");
            Collections.addAll(setItemKeys, sik);
        }

        this.setName = setName;
        this.exportFragment = exportFragment;
        this.exportSongListBottomSheet = exportSongListBottomSheet;
        thisSong = null;
    }

    public void setThisSong(Song thisSong) {
        this.thisSong = thisSong;
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

        boolean singleItem = false;
        if (setName==null || setName.isEmpty()) {
            // This is a single item, not a set
            setName = setItemEntries.get(0);
            singleItem = true;
        }

        mainActivityInterface.getMakePDF().createBlankPDFDoc(setName+".pdf",printAttributes);

        // Create the first section of the PDF - the set list
        if (!singleItem && (thisSong==null || (thisSong.getUser1()!=null && !thisSong.getUser1().equals("PRINT_SONG_LIST")))) {
            mainActivityInterface.getMakePDF().setIsSetListPrinting(true);
            thisSong = new Song();
            thisSong.setTitle(setName);
            thisSong.setCapoprint("ISTHESET");
            StringBuilder setItems = new StringBuilder();
            for (String setItemEntry : setItemEntries) {
                setItems.append(setItemEntry).append("\n[]\n");
            }
            thisSong.setLyrics(setItems.toString());

            listen.setValue(false);
            mainActivityInterface.getProcessSong().updateProcessingPreferences();
            mainActivityInterface.getMakePDF().setSong(thisSong);
            createOnTheFlyHeader(thisSong, true);
        } else {
            currentSetItem = 0;
            getSongOrPrintIfDone();
        }
    }

    public void createOnTheFlyHeader(Song thisSong,boolean theSetList) {
        // Get the song sheet header
        // Once this has drawn, move to the next stage of the song sections
        this.thisSong = thisSong;
        float scaleComments = mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleComments",0.8f);

        ViewTreeObserver headerVTO = null;
        if (exportFragment!=null) {
            headerVTO = exportFragment.getHiddenHeader().getViewTreeObserver();
        } else if (exportSongListBottomSheet!=null) {
            headerVTO = exportSongListBottomSheet.getHeaderLayout().getViewTreeObserver();
        }
        if (headerVTO!=null) {
            headerVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (exportFragment != null) {
                        exportFragment.getHiddenHeader().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        headerLayoutWidth = exportFragment.getHiddenHeader().getWidth();
                        headerLayoutHeight = exportFragment.getHiddenHeader().getHeight();
                        exportFragment.getHiddenHeader().removeAllViews();
                    } else if (exportSongListBottomSheet != null) {
                        exportSongListBottomSheet.getHeaderLayout().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        headerLayoutWidth = exportSongListBottomSheet.getHeaderLayout().getWidth();
                        headerLayoutHeight = exportSongListBottomSheet.getHeaderLayout().getHeight();
                    }
                    createOnTheFlySections(thisSong, theSetList);
                }
            });
        }

        // Now draw it here for measuring via the VTO
        if (exportFragment!=null) {
            exportFragment.setHeaderLayoutPDF(mainActivityInterface.getSongSheetHeaders().getSongSheet(thisSong,
                    scaleComments, mainActivityInterface.getMyThemeColors().getPdfTextColor()));
            if (exportFragment.getHeaderLayout() == null) {
                exportFragment.setHeaderLayoutPDF(new LinearLayout(exportFragment.getHiddenHeader().getContext()));
            }
            exportFragment.getHiddenHeader().addView(exportFragment.getHeaderLayout());
        } else if (exportSongListBottomSheet!=null) {
            exportSongListBottomSheet.setHeaderLayoutPDF(mainActivityInterface.getSongSheetHeaders().getSongSheet(thisSong,
                    scaleComments, mainActivityInterface.getMyThemeColors().getPdfTextColor()));
            if (exportSongListBottomSheet.getHeaderLayoutPDF() == null) {
                exportSongListBottomSheet.setHeaderLayoutPDF(new LinearLayout(exportSongListBottomSheet.getHeaderLayout().getContext()));
            }
            if (exportSongListBottomSheet.getHeaderLayoutPDF()!=null) {
                exportSongListBottomSheet.getHeaderLayout().addView(exportSongListBottomSheet.getHeaderLayoutPDF());
            }
        }
    }

    public void createOnTheFlySections(Song thisSong, boolean theSetList) {
        this.thisSong = thisSong;
        if (thisSong!=null) {
            if (mainActivityInterface.getStorageAccess().isIMGorPDF(thisSong)) {
                sectionViewsPDF = new ArrayList<>();
                if (exportFragment!=null) {
                    exportFragment.resetSectionViews();
                }
                Uri thisUri = mainActivityInterface.getStorageAccess().
                        getUriForItem("Songs",thisSong.getFolder(),thisSong.getFilename());

                if (mainActivityInterface.getStorageAccess().filenameIsImage(thisSong.getFilename())) {
                    // If this is an image file, add an image view
                    ImageView imageView = new ImageView(c);
                    imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    Bitmap bitmap = mainActivityInterface.getProcessSong().getSongBitmap(thisSong.getFolder(),thisSong.getFilename());
                    imageView.setImageBitmap(bitmap);
                    sectionViewsPDF.add(imageView);
                    prepareLayoutListenerForPDFViews(theSetList);
                    // Add the image and this will trigger the VTO
                    if (exportFragment!=null) {
                        exportFragment.getHiddenSections().addView(sectionViewsPDF.get(0));
                    }
                } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ArrayList<ImageView> pdfImages = mainActivityInterface.getProcessSong().getPDFAsImageViews(c,thisUri);
                    sectionViewsPDF.addAll(pdfImages);
                    prepareLayoutListenerForPDFViews(theSetList);
                    // Now add the pages and trigger the VTO
                    if (exportFragment!=null) {
                        for (View view : pdfImages) {
                            exportFragment.getHiddenSections().addView(view);
                        }
                    }
                } else {
                    // Not allowed PDFs
                    MyMaterialSimpleTextView textView = new MyMaterialSimpleTextView(c);
                    textView.setText(c.getString(R.string.not_allowed));
                    if (exportFragment!=null) {
                        exportFragment.getHiddenSections().addView(new MyMaterialSimpleTextView(c));
                    }
                }
            } else {
                if (thisSong.getLyrics() == null) {
                    thisSong.setLyrics("");
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
                mainActivityInterface.getProcessSong().setPdfPrinting(true);
                thisSong.setLyrics(thisSong.getLyrics().trim());

                mainActivityInterface.getAbcNotation().resetInlineAbcObjects();

                sectionViewsPDF = mainActivityInterface.getProcessSong().
                        setSongInLayout(thisSong, true, false);

                mainActivityInterface.getMakePDF().setIsSetListPrinting(false);
                mainActivityInterface.getProcessSong().setPdfPrinting(true);

                if (exportFragment!=null) {
                    exportFragment.resetSectionViews();
                }
                prepareLayoutListenerForPDFViews(theSetList);

                // Add the section views and this will trigger the VTO
                for (int x = 0; x < sectionViewsPDF.size(); x++) {
                    if (exportFragment!=null) {
                        exportFragment.getHiddenSections().addView(sectionViewsPDF.get(x));
                    } else if (exportSongListBottomSheet!=null) {
                        exportSongListBottomSheet.getSectionLayout().addView(sectionViewsPDF.get(x));
                    }
                }
            }
        }
    }

    private void prepareLayoutListenerForPDFViews(boolean theSetList) {
        // Prepare the view listener for after the views have been drawn
        mainActivityInterface.getMakePDF().setSong(thisSong);
        ViewTreeObserver sectionsVTO = null;
        if (exportFragment!=null) {
            sectionsVTO = exportFragment.getHiddenSections().getViewTreeObserver();
        } else if (exportSongListBottomSheet!=null) {
            sectionsVTO = exportSongListBottomSheet.getSectionLayout().getViewTreeObserver();
        }
        if (sectionsVTO!=null) {
            sectionsVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // The views are ready so lets measure them after clearing this listener

                    // If all the views are there, we can start measuring
                    if ((exportFragment!=null && exportFragment.getHiddenSections().getChildCount() == sectionViewsPDF.size()) ||
                            (exportSongListBottomSheet!=null && exportSongListBottomSheet.getSectionLayout().getChildCount() == sectionViewsPDF.size())) {
                        if (exportFragment!=null) {
                            exportFragment.getHiddenSections().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else if (exportSongListBottomSheet!=null) {
                            exportSongListBottomSheet.getSectionLayout().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        int size = 0;
                        if (exportFragment!=null) {
                            size = exportFragment.getHiddenSections().getChildCount();
                        } else if (exportSongListBottomSheet!=null) {
                            size = exportSongListBottomSheet.getSectionLayout().getChildCount();
                        }
                        for (int x = 0; x < size; x++) {
                            View view = null;
                            if (exportFragment!=null) {
                                view = exportFragment.getHiddenSections().getChildAt(x);
                            } else if (exportSongListBottomSheet!=null) {
                                view = exportSongListBottomSheet.getSectionLayout().getChildAt(x);
                            }
                            if (view!=null) {
                                int width = view.getMeasuredWidth();
                                int height = view.getMeasuredHeight();
                                if (exportFragment!=null) {
                                    exportFragment.getSectionViews().add(view);
                                    exportFragment.getSectionWidths().add(width);
                                    exportFragment.getSectionHeights().add(height);
                                } else if (exportSongListBottomSheet!=null) {
                                    exportSongListBottomSheet.getSectionViews().add(view);
                                    exportSongListBottomSheet.getSectionWidths().add(width);
                                    exportSongListBottomSheet.getSectionHeights().add(height);
                                }
                            }
                        }

                        if (exportFragment!=null && exportFragment.getHeaderLayout() != null) {
                            mainActivityInterface.getMakePDF().setHeaderHeight(exportFragment.getHeaderLayout().getMeasuredHeight());
                            mainActivityInterface.getMakePDF().getColumns(exportFragment.getSectionViews(), exportFragment.getSectionWidths(), exportFragment.getSectionHeights());
                            // Now detach from this view (can only be shown in one layout)
                            exportFragment.getHiddenSections().removeAllViews();
                        } else if (exportSongListBottomSheet!=null && exportSongListBottomSheet.getHeaderLayout()!=null) {
                            mainActivityInterface.getMakePDF().setHeaderHeight(exportSongListBottomSheet.getHeaderLayout().getMeasuredHeight());
                            mainActivityInterface.getMakePDF().getColumns(exportSongListBottomSheet.getSectionViews(), exportSongListBottomSheet.getSectionWidths(), exportSongListBottomSheet.getSectionHeights());
                            exportSongListBottomSheet.getSectionLayout().removeAllViews();
                        }

                        // Now trigger the next step of preparing the pdf from the views created on the fly
                        listen.setValue(true);

                        if (exportFragment!=null) {
                            mainActivityInterface.getMakePDF().addCurrentItemToPDF(exportFragment.getSectionViews(),
                                    exportFragment.getSectionWidths(), exportFragment.getSectionHeights(),
                                    exportFragment.getHeaderLayout(), headerLayoutWidth,
                                    headerLayoutHeight);
                        } else if (exportSongListBottomSheet!=null) {
                            mainActivityInterface.getMakePDF().addCurrentItemToPDF(exportSongListBottomSheet.getSectionViews(),
                                    exportSongListBottomSheet.getSectionWidths(), exportSongListBottomSheet.getSectionHeights(),
                                    exportSongListBottomSheet.getHeaderLayout(), headerLayoutWidth,
                                    headerLayoutHeight);
                        }

                        if (theSetList) {
                            // Now we have finished the set list, deal with the content/songs
                            currentSetItem = 0;
                            getSongOrPrintIfDone();
                        } else if (thisSong!=null && thisSong.getUser1()!=null && thisSong.getUser1().equals("PRINT_SONG_LIST")) {
                            callPrint();
                        } else {
                            // Move to the next song
                            currentSetItem++;
                            getSongOrPrintIfDone();
                        }
                    }
                }
            });
        }
    }
    private void getSongOrPrintIfDone() {
        if (thisSong!=null && thisSong.getUser1()!=null && thisSong.getUser1().equals("PRINT_SONG_LIST")) {
            // Must be the song list
            createOnTheFlyHeader(thisSong,false);

        } else if (!mainActivityInterface.getPreferences().getMyPreferenceBoolean("exportSetSongs",true) ||
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
                currentSetSong = mainActivityInterface.getLoadSong().doLoadSongFile(currentSetSong,false);
            } else {
                if (setItemLocations.get(currentSetItem).contains("/")) {
                    String folder = setItemLocations.get(currentSetItem).substring(0,setItemLocations.get(currentSetItem).lastIndexOf("/"));
                    String filename = setItemLocations.get(currentSetItem).replace(folder+"/","");
                    currentSetSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(folder, filename);
                } else {
                    currentSetSong = mainActivityInterface.getSQLiteHelper().getSpecificSong("", setItemLocations.get(currentSetItem));
                }
            }

            // If we have transposed this song in the set on the fly, match the key here
            if (setItemKeys!=null && setItemKeys.size()>currentSetItem && !setItemKeys.get(currentSetItem).equals("ignore") && !setItemKeys.get(currentSetItem).trim().isEmpty() && currentSetSong.getKey()!=null && !currentSetSong.getKey().isEmpty() &&
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
