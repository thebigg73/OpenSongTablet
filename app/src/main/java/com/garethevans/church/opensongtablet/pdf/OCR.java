package com.garethevans.church.opensongtablet.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;

public class OCR {

    private ArrayList<String> pdfPages;
    private int pageCount;
    private String filename = null;
    private MainActivityInterface mainActivityInterface;
    private final String TAG = "OCR";

    public void getTextFromPDF(Context c, MainActivityInterface mainActivityInterface,
                               String folder, String filename) {

        this.mainActivityInterface = mainActivityInterface;

        // This uses most bits of the ProcessSong methods used to display the pdf as an image
        // However we will iterate through each page and send the bitmap off for ocr recognition
        // It also processes images (png, jpg, gif) and camera intents using the same logic with 1 page

        pdfPages = new ArrayList<>();

        // Get the pdf uri
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(c,mainActivityInterface,"Songs",folder,filename);

        // Get the parcel file descriptor
        ParcelFileDescriptor parcelFileDescriptor = mainActivityInterface.getProcessSong().getPDFParcelFileDescriptor(c,uri);

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
                ArrayList<Integer> pdfSize = mainActivityInterface.getProcessSong().getPDFPageSize(currentPage);

                // Get a scaled Bitmap size
                ArrayList<Integer> bmpSize = mainActivityInterface.getProcessSong().getBitmapScaledSize(pdfSize,1200,1600,"Y");

                Log.d(TAG,"bmpWidth="+bmpSize.get(0));
                Log.d(TAG,"bmpHeight="+bmpSize.get(1));

                // Get a scaled bitmap for these sizes
                bmp = mainActivityInterface.getProcessSong().createBitmapFromPage(bmpSize,currentPage,false);

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

    public void getTextFromImage(MainActivityInterface mainActivityInterface, Bitmap bmp) {

        this.mainActivityInterface = mainActivityInterface;
        // Just a plain jpg, png or gif converted to a bitmap
        // Pretending it is from a 1 page pdf
        pdfPages = new ArrayList<>();
        pageCount = 1;
        if (bmp!=null) {
            extractTextFromBitmap(bmp, 0);
        }
    }

    public void getTextFromCamera(Context c, MainActivityInterface mainActivityInterface, Uri uri) {
        this.mainActivityInterface = mainActivityInterface;
        // The camera saves to Backgrounds/camera_capture.png (_cache) received in the call
        // Pretending it is from a 1 page pdf
        pdfPages = new ArrayList<>();
        pageCount = 1;
        Bitmap bmp = mainActivityInterface.getProcessSong().getBitmapFromUri(c,mainActivityInterface,uri,0,0);
        Log.d(TAG,"bmp="+bmp);
        if (bmp!=null) {
            extractTextFromBitmap(bmp, 0);
        }
    }

    private void extractTextFromBitmap(Bitmap bmp, int page) {
        InputImage image = InputImage.fromBitmap(bmp, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        final int currpage = page;
        recognizer.process(image).addOnSuccessListener(visionText -> {
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
                        });
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
    }
}
