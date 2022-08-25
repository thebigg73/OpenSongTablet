package com.garethevans.church.opensongtablet.export;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.view.View;
import android.widget.LinearLayout;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class PrinterAdapter extends PrintDocumentAdapter {

    private Activity activity;
    private MainActivityInterface mainActivityInterface;
    private PdfDocument pdfDocument;
    private String name;
    private Uri uri;
    private ArrayList<View> sectionViewsPDF;
    private ArrayList<Integer> sectionViewWidthsPDF, sectionViewHeightsPDF;
    private LinearLayout headerLayoutPDF;
    private int headerLayoutWidth, headerLayoutHeight, docWidth, docHeight;
    private int totalPages;
    private PrintedPdfDocument printedPdfDocument;


    public PrinterAdapter(Activity activity) {
        this.activity = activity;
        mainActivityInterface = (MainActivityInterface) activity;
    }

    public void updateSections(ArrayList<View> sectionViewsPDF, ArrayList<Integer> sectionViewWidthsPDF,
                               ArrayList<Integer> sectionViewHeightsPDF, LinearLayout headerLayoutPDF,
                               int headerLayoutWidth, int headerLayoutHeight, String name) {
        this.sectionViewsPDF = sectionViewsPDF;
        this.sectionViewWidthsPDF = sectionViewWidthsPDF;
        this.sectionViewHeightsPDF = sectionViewHeightsPDF;
        this.headerLayoutPDF = headerLayoutPDF;
        this.headerLayoutWidth = headerLayoutWidth;
        this.headerLayoutHeight = headerLayoutHeight;
        this.name = name;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                         CancellationSignal cancellationSignal, LayoutResultCallback callback,
                         Bundle extras) {
        // The user has chosen a printer, orientation, etc that decides a layout

        // Respond to cancellation request
        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        uri = mainActivityInterface.getMakePDF().createTextPDF(
                sectionViewsPDF, sectionViewWidthsPDF, sectionViewHeightsPDF, headerLayoutPDF,
                headerLayoutWidth, headerLayoutHeight, name, newAttributes);

        // Actual PDF document
        pdfDocument = mainActivityInterface.getMakePDF().getPdfDocument();
        printedPdfDocument = new PrintedPdfDocument(activity, newAttributes);

        // Compute the expected number of printed pages
        totalPages = pdfDocument.getPages().size();

        docWidth = mainActivityInterface.getMakePDF().getDocWidth();
        docHeight = mainActivityInterface.getMakePDF().getDocHeight();

        if (totalPages > 0) {
            // Return print information to print framework
            PrintDocumentInfo info = new PrintDocumentInfo
                    .Builder(name + ".pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(totalPages)
                    .build();
            // Content layout reflow is complete
            callback.onLayoutFinished(info, true);
        } else {
            // Otherwise report an error to the print framework
            callback.onLayoutFailed("Page count calculation failed.");
        }
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                        CancellationSignal cancellationSignal, WriteResultCallback callback) {
        // The user has chosen the pages, format and clicked the print button

        try {
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
            OutputStream outputStream = new FileOutputStream(destination.getFileDescriptor());

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

    private boolean pageInRange(PageRange[] pageRanges, int page) {
        for (PageRange pageRange : pageRanges) {
            if ((page >= pageRange.getStart()) &&
                    (page <= pageRange.getEnd()))
                return true;
        }
        return false;
    }

    private void drawPage(PdfDocument.Page page, int i) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        Canvas canvas = page.getCanvas();
        canvas.drawRect(pdfDocument.getPages().get(i).getContentRect(),paint);
    }
}
