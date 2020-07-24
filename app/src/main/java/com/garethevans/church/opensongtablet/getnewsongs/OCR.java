package com.garethevans.church.opensongtablet.getnewsongs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.util.ArrayList;

public class OCR {

    ArrayList<String> pdfPages;
    int pageCount;
    MainActivityInterface mainActivityInterface;

    public void getTextFromPDF(Context c, Preferences preferences, StorageAccess storageAccess,
                                 ProcessSong processSong, MainActivityInterface mainActivityInterface,
                               String folder, String filename) {

        this.mainActivityInterface = mainActivityInterface;

        // This uses most bits of the ProcessSong methods used to display the pdf as an image
        // However we will iterate through each page and send the bitmap off for ocr recognition

        pdfPages = new ArrayList<>();

        // Get the pdf uri
        Uri uri = storageAccess.getUriForItem(c,preferences,"Songs",folder,filename);

        // Get the parcel file descriptor
        ParcelFileDescriptor parcelFileDescriptor = processSong.getPDFParcelFileDescriptor(c,uri);

        // Get the pdf renderer
        PdfRenderer pdfRenderer = processSong.getPDFRenderer(parcelFileDescriptor);

        // Get the page count
        pageCount = processSong.getPDFPageCount(pdfRenderer);

        Log.d("d","uri"+uri);
        Log.d("d","parcelFileDescriptor="+parcelFileDescriptor);
        Log.d("d","pdfRenderer="+pdfRenderer);
        Log.d("d","pageCount="+pageCount);

        if (parcelFileDescriptor!=null && pdfRenderer!=null && pageCount>0) {
            // Good to continue!

            Log.d("d","not null!");
            PdfRenderer.Page currentPage;
            Bitmap bmp;

            for (int i=0; i<pageCount; i++) {
                // Get the currentPDF page
                currentPage = processSong.getPDFPage(pdfRenderer,i);

                // Get the currentPDF size
                ArrayList<Integer> pdfSize = processSong.getPDFPageSize(currentPage);

                // Get a scaled Bitmap size
                ArrayList<Integer> bmpSize = processSong.getBitmapScaledSize(pdfSize,1200,1600,"Y");

                Log.d("d","bmpWidth="+bmpSize.get(0));
                Log.d("d","bmpHeight="+bmpSize.get(1));

                // Get a scaled bitmap for these sizes
                bmp = processSong.createBitmapFromPage(bmpSize,currentPage,false);

                // Send this page off for processing.  The onSuccessListener knows when it is done
                extractTextFromBitmap(bmp,0,i);

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

    private void extractTextFromBitmap(Bitmap bmp,int rotation,int page) {
        String s = "";
        InputImage image = InputImage.fromBitmap(bmp, rotation);
        TextRecognizer recognizer = TextRecognition.getClient();

        final int currpage = page;
        final Task<Text> result = recognizer.process(image)
                        .addOnSuccessListener(visionText -> {
                            pdfPages.add(currpage,visionText.getText());
                            if (pdfPages.size()==pageCount) {
                                // We're done
                                runCompleteTask();
                            }

                        })
                        .addOnFailureListener(e -> {
                            pdfPages.add(currpage,"");
                            Log.d("d","Error on page "+currpage);
                            if (pdfPages.size()==pageCount) {
                                // We're done
                                runCompleteTask();
                            }
                        });
    }


    private void runCompleteTask() {

        StringBuilder sb = new StringBuilder();
        for (String s:pdfPages) {
            sb.append(s).append("\n");
        }
        String s = sb.toString();
        Log.d("d","Found text:\n"+s);
        if (mainActivityInterface!=null) {
            // TODO do something with it
        }
    }
}
