package com.garethevans.church.opensongtablet.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;

public class OCR {

    private ArrayList<String> pdfPages;
    private int pageCount;
    private String filename = null;
    private final MainActivityInterface mainActivityInterface;
    private final String TAG = "OCR", blockSplitStart = "__BLOCKSTART_", blockSplitEnd = "_BLOCKEND__";
    private int pageHeightRunning = 0, maxTop = 0, totalLineHeight = 0, totalLineCount = 0;

    public OCR(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
    }
    public void getTextFromPDF(String folder, String filename, boolean useCropped) {
        // This uses most bits of the ProcessSong methods used to display the pdf as an image
        // However we will iterate through each page and send the bitmap off for ocr recognition
        // It also processes images (png, jpg, gif) and camera intents using the same logic with 1 page

        this.filename = filename;

        pdfPages = new ArrayList<>();

        // Get the pdf uri
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",folder,filename);

        // Get the parcel file descriptor
        ParcelFileDescriptor parcelFileDescriptor = mainActivityInterface.getProcessSong().getPDFParcelFileDescriptor(uri);

        // Get the pdf renderer
        PdfRenderer pdfRenderer = mainActivityInterface.getProcessSong().getPDFRenderer(parcelFileDescriptor);

        // Get the page count
        pageCount = mainActivityInterface.getProcessSong().getPDFPageCount(pdfRenderer);

        Log.d(TAG,"uri"+uri);
        Log.d(TAG,"parcelFileDescriptor="+parcelFileDescriptor);
        Log.d(TAG,"pdfRenderer="+pdfRenderer);
        Log.d(TAG,"pageCount="+pageCount);

        if (parcelFileDescriptor!=null && pdfRenderer!=null && pageCount>0) {
            // Good to continue!

            Log.d(TAG,"not null!");
            PdfRenderer.Page currentPage;
            Bitmap bmp;

            for (int i=0; i<pageCount; i++) {
                // Get the currentPDF page
                currentPage = mainActivityInterface.getProcessSong().getPDFPage(pdfRenderer,i);

                // Get the currentPDF size
                ArrayList<Integer> pdfSize = mainActivityInterface.getProcessSong().getPDFPageSize(currentPage, useCropped);

                // Get a scaled Bitmap size
                ArrayList<Integer> bmpSize = mainActivityInterface.getProcessSong().getBitmapScaledSize(pdfSize,1200,1600,"Y");

                Log.d(TAG,"bmpWidth="+bmpSize.get(0));
                Log.d(TAG,"bmpHeight="+bmpSize.get(1));

                // Get a scaled bitmap for these sizes
                bmp = mainActivityInterface.getProcessSong().createBitmapFromPage(bmpSize,currentPage,false, useCropped);

                // Send this page off for processing.  The onSuccessListener knows when it is done
                extractTextFromBitmap(bmp, i);

                currentPage.close();
            }
            try {
                pdfRenderer.close();
                parcelFileDescriptor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void getTextFromImageFile(String folder, String filename) {
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",folder,filename);
        Bitmap bitmap = mainActivityInterface.getProcessSong().getBitmapFromUri(uri,0,0);
        getTextFromImage(bitmap);
    }

    public void getTextFromImage(Bitmap bmp) {
        // Just a plain jpg, png or gif converted to a bitmap
        // Pretending it is from a 1 page pdf
        pdfPages = new ArrayList<>();
        pageCount = 1;
        if (bmp!=null) {
            extractTextFromBitmap(bmp, 0);
        }
    }

    public void getTextFromCamera(Uri uri) {
        // The camera saves to Backgrounds/camera_capture.png (_cache) received in the call
        // Pretending it is from a 1 page pdf
        pdfPages = new ArrayList<>();
        pageCount = 1;
        Bitmap bmp = mainActivityInterface.getProcessSong().getBitmapFromUri(uri,0,0);
        Log.d(TAG,"bmp="+bmp);
        if (bmp!=null) {
            extractTextFromBitmap(bmp, 0);
        }
    }

    private void extractTextFromBitmap(Bitmap bmp, int page) {
        int pageHeightToAdd = (page) * bmp.getHeight();
        maxTop = pageHeightToAdd; // 0 for first page
        final int currpage = page;
        InputImage image = InputImage.fromBitmap(bmp, 0);
        TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> task = textRecognizer.process(image);
        task.addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                // Use an array where the index is the top position
                SparseArray<String> stringSparseArray = new SparseArray<>();

                for (Text.TextBlock block : text.getTextBlocks()) {
                    for (Text.Line line : block.getLines()) {
                        String lineText = line.getText();
                        Rect lineFrame = line.getBoundingBox();
                        int top = pageHeightToAdd;
                        int left = 0;
                        if (lineFrame!=null) {
                            top = lineFrame.top + pageHeightToAdd;
                            left = lineFrame.left;
                            totalLineCount++;
                            totalLineHeight += lineFrame.bottom - lineFrame.top;
                        }

                        String currLine = stringSparseArray.get(top,"");
                        String newLine = (currLine+blockSplitStart+left+blockSplitEnd+lineText).trim();
                        stringSparseArray.put(top,newLine);
                        maxTop = Math.max(maxTop,top);
                    }
                }

                // Now we have the lines in the correct order (by top position)
                // Try to merge lines that have similar tops (within fudge)
                int lastTop = 0;
                // Work out the fudge factor based on average line height
                int fudge = 10;
                if (totalLineCount!=0) {
                    float avHeight = (float)totalLineHeight/(float)totalLineCount;
                    fudge = Math.round((0.5f*avHeight));
                }


                SparseArray<String> tidiedLines = new SparseArray<>();
                for (int x=0; x<maxTop+1; x++) {
                    if (stringSparseArray.get(x,null)!=null) {
                        if (x<lastTop+fudge) {
                            // Merge to lastTop
                            String prevVal = tidiedLines.get(lastTop,"");
                            tidiedLines.put(lastTop,(prevVal+" "+stringSparseArray.get(x)).trim());
                        } else {
                            // New line
                            tidiedLines.put(x,stringSparseArray.get(x));
                            lastTop = x;
                        }
                    }
                }

                StringBuilder textFromLinesArray = new StringBuilder();
                for (int x=0; x<maxTop; x++) {
                    StringBuilder thisLine = new StringBuilder();
                    if (tidiedLines.get(x,null)!=null) {
                        // This line has each section with the left position inside
                        // We need to do this to add them back in the correct order
                        String[] blocks = tidiedLines.get(x).split(blockSplitStart);
                        SparseArray<String> horizontalArray = new SparseArray<>();
                        int maxHPos = 0;
                        for (int y = 0; y < blocks.length; y++) {
                            // Get the horizontal pos
                            if (blocks[y].contains(blockSplitEnd)) {
                                int hpos = Integer.parseInt(blocks[y].substring(0, blocks[y].indexOf(blockSplitEnd)).replaceAll("\\D", ""));
                                String blockText = blocks[y].substring(blocks[y].indexOf(blockSplitEnd) + blockSplitEnd.length());
                                horizontalArray.put(hpos, blockText);
                                maxHPos = Math.max(hpos, maxHPos);
                            }
                        }
                        for (int z = 0; z < maxHPos+1; z++) {
                            if (horizontalArray.get(z, null) != null) {
                                thisLine.append(horizontalArray.get(z)).append(" ");
                            }
                        }
                        textFromLinesArray.append(thisLine).append("\n");
                    }
                }

                pdfPages.add(currpage,textFromLinesArray.toString());
                pageHeightRunning = maxTop;
                if (pdfPages.size()==pageCount) {
                    // We're done
                    runCompleteTask();
                }
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pdfPages.add(currpage,"");
                Log.d(TAG,"Error on page "+currpage);
                if (pdfPages.size()==pageCount) {
                    // We're done
                    runCompleteTask();
                }
            }
        });


        /*recognizer.process(image).addOnSuccessListener(visionText -> {

            String resultText = visionText.getText();
            for (Text.TextBlock block : visionText.getTextBlocks()) {
                String blockText = block.getText();
                Log.d(TAG,"blockText:"+blockText);
                Point[] blockCornerPoints = block.getCornerPoints();
                Rect blockFrame = block.getBoundingBox();
                for (Text.Line line : block.getLines()) {
                    String lineText = line.getText();
                    Log.d(TAG,"lineText:"+lineText);
                    Point[] lineCornerPoints = line.getCornerPoints();
                    Rect lineFrame = line.getBoundingBox();
                    for (Text.Element element : line.getElements()) {
                        String elementText = element.getText();
                        Log.d(TAG,"elementText:"+elementText);
                        Point[] elementCornerPoints = element.getCornerPoints();
                        Rect elementFrame = element.getBoundingBox();
                        for (Text.Symbol symbol : element.getSymbols()) {
                            String symbolText = symbol.getText();
                            Point[] symbolCornerPoints = symbol.getCornerPoints();
                            Rect symbolFrame = symbol.getBoundingBox();
                        }
                    }
                }
            }
                            pdfPages.add(currpage,visionText.getText());
                            if (pdfPages.size()==pageCount) {
                                // We're done
                                runCompleteTask();
                            }
                        }).addOnFailureListener(e -> {
                            pdfPages.add(currpage,"");
                            Log.d(TAG,"Error on page "+currpage);
                            if (pdfPages.size()==pageCount) {
                                // We're done
                                runCompleteTask();
                            }
                        });*/
    }

    private void runCompleteTask() {
        // Get a filename
        if (filename==null) {
            // Send the song filename as it currently exists - camera pics set this separately
            filename = mainActivityInterface.getSong().getFilename();
        }
        // Send the array of pages to the Bottom sheet for the user to decide what to do
        PDFExtractBottomSheet pdfExtractBottomSheet = new PDFExtractBottomSheet(pdfPages,filename);
        pdfExtractBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"PDFExtractBottomSheet");
        filename = null;
    }
}
