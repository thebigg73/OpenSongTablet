package com.garethevans.church.opensongtablet.pdf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.print.PrintAttributes;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.OutputStream;
import java.util.ArrayList;

public class MakePDF {

    private final int margin = 36;            // Based on 1/72 of an inch:  54/72  * 2.54 = 1cm
    private final int footerHeight = 12;      // Based on 1/72 of an inch:  90/72  * 2.54 = 2.0cm
    private int headerHeight, docWidth, docHeight, availableHeight, pageNum=1, totalPages=1;
    private Paint linePaint, footerPaint;
    private PdfDocument pdfDocument;
    private PdfDocument.Page page;
    private float sectionScaling;
    private Canvas pageCanvas;

    public Uri createTextPDF(Context c, MainActivityInterface mainActivityInterface, Song thisSong,
                         ArrayList<View> sectionViews, ArrayList<Integer> sectionWidths,
                         ArrayList<Integer> sectionHeights, LinearLayout headerLayout,
                         int headerLayoutWidth, int headerLayoutHeight, String exportFilename) {

        Log.d("d","exportFilename="+exportFilename);

        // Set the paint values
        setPaintDefaults(mainActivityInterface);

        // Create the document
        pdfDocument = new PdfDocument();

        initialiseSizes(c,mainActivityInterface);

        // Start for page 1
        startPage();

        // The PDF will be created using the views created by the mode for the current song,
        // or on the fly for a non-current song when selecting export as PDF

        // Add or create the header
        createHeader(headerLayout, headerLayoutWidth, headerLayoutHeight);

        // Get the space available to the song sections and pages needed to fit them all in
        determineSpaceAndPages(mainActivityInterface, sectionWidths, sectionHeights);


        // Add in the song sections and the footer at the bottom of each page.
        addSectionViews(sectionViews, sectionWidths, sectionHeights);

        // Save the PDF document ready for sharing
        Uri uri = getPDFUri(c,mainActivityInterface,exportFilename);
        //pdfDocument.finishPage(page);
        saveThePDF(c, mainActivityInterface, uri);

        return uri;
    }

    // Initialise the PDF and Paint stuff
    private void setPaintDefaults(MainActivityInterface mainActivityInterface) {
        // For drawing the horizontal lines
        linePaint = new Paint();
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStrokeWidth(2);
        linePaint.setAntiAlias(true);

        // For writing the footer
        footerPaint = new Paint();
        footerPaint.setColor(Color.DKGRAY);
        footerPaint.setTypeface(mainActivityInterface.getMyFonts().getLyricFont());
        footerPaint.setTextSize(10);
        footerPaint.setAntiAlias(true);
    }

    // Initialise the sizes and page numbers
    private void initialiseSizes(Context c, MainActivityInterface mainActivityInterface) {
        String pdfSize = mainActivityInterface.getPreferences().getMyPreferenceString(c,"pdfSize","A4");
        PrintAttributes.MediaSize mediaSize;
        switch (pdfSize) {
            case "A4":
            default:
                mediaSize = PrintAttributes.MediaSize.ISO_A4;
                break;
            case "Letter":
                mediaSize = PrintAttributes.MediaSize.NA_LETTER;
                break;
        }
        docWidth =  (int) (((float)mediaSize.getWidthMils()/1000.0f)*72.0f);
        docHeight = (int) (((float)mediaSize.getHeightMils()/1000.0f)*72.0f);
        pageNum = 1;
        totalPages = 1;
    }

    // Create and start the new page based on the current page number
    private void startPage() {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(docWidth, docHeight, pageNum).create();
        page = pdfDocument.startPage(pageInfo);
        pageCanvas = page.getCanvas();

        // Set the canvas density to 72dpi.  This way pixels match points!
        pageCanvas.setDensity(72);
    }

    // Headers and footers
    private void createHeader(LinearLayout headerLayout, int headerLayoutWidth,
                              int headerLayoutHeight) {
        int headerWidth = headerLayoutWidth;
        headerHeight = headerLayoutHeight;
        float headerScaling;
        if (headerWidth < (docWidth - (margin * 2))) {
            // Don't make it any bigger!
            headerScaling = 1.0f;
        } else {
            headerScaling = ((float) (docWidth - (margin * 2))) / (float) headerWidth;
            headerWidth = docWidth - (margin * 2);
            headerHeight = (int) ((float) headerHeight * headerScaling);
        }

        // Do any scaling
        scaleThisView(headerLayout, headerWidth, headerHeight, headerScaling);

        // Save the canvas, translate for correct write positioning, then restore the canvas state/position
        pageCanvas.save();
        pageCanvas.translate(margin, margin);
        pageCanvas.scale(headerScaling,headerScaling);
        headerLayout.draw(pageCanvas);
        pageCanvas.restore();

        // Draw a horizontal line under the heading
        drawHorizontalLine(headerHeight + margin);
        headerHeight = headerHeight + 4;
    }

    // Decide on sizes required
    private void determineSpaceAndPages(MainActivityInterface mainActivityInterface,
                                        ArrayList<Integer> sectionWidths,
                                        ArrayList<Integer> sectionHeights) {
        // The max scaling is preferrably scaling the sections to the width of the screen
        // However, if this means that any section is too tall to fit the space between ther
        // header and footer, we will have to scale it down, otherwise it will never fit.

        // Decide on our scaling
        int maxWidth = mainActivityInterface.getProcessSong().getMaxValue(sectionWidths,0,sectionWidths.size());
        int maxHeight = mainActivityInterface.getProcessSong().getMaxValue(sectionHeights,0,sectionHeights.size());

        // Determine the size we have available based on the document sizes
        // The width is the document width minus 2x margin (left and right)
        // The height is the document height minus 2x margin (top and bottom) and header and footer
        // The header can vary depending on the content, but the footer is always the same

        int availableWidth = docWidth - (margin * 2);
        availableHeight = docHeight - (margin * 2)  - headerHeight - footerHeight;

        // We need to make sure that at least all sections can fit on the available page
        // If any are bigger, we need to scale down
        // Firstly scale to the max width available
        sectionScaling = (float)availableWidth /(float)maxWidth;

        // If it is bigger than 1.2, it will look silly, so set this as max
        if (sectionScaling > 1.2f) {
            sectionScaling = 1.2f;
        }

        // Now check how this affects the height of the views
        // We only reduce the scaling if the section heights are too big
        if ((maxHeight*sectionScaling) > availableHeight) {
            sectionScaling = (float)availableHeight/(float)maxHeight;
        }

        // Now plan out how many pages we will need
        // We need to do this before being able to write the footer
        int spaceStillAvailable = availableHeight;
        for (int sectionHeight:sectionHeights) {
            if (sectionHeight*sectionScaling > spaceStillAvailable) {
                totalPages++;
                spaceStillAvailable = availableHeight + headerHeight - (int)(sectionHeight*sectionScaling);
            } else {
                spaceStillAvailable = spaceStillAvailable - (int)(sectionHeight*sectionScaling);
            }
        }
    }

    // The footer creation with OpenSongApp credit and page numbering if required
    private void createFooter() {
        // The OpenSongApp credit.  This is drawn after the rest of the page content has finished
        Rect bounds = new Rect();
        String string = "Prepared by OpenSongApp (https://www.opensongapp.com)";
        footerPaint.getTextBounds(string,0,string.length(),bounds);
        pageCanvas.drawText(string,margin,docHeight-margin-footerHeight,footerPaint);

        // The page numbering if there is more than 1 page needed
        if (totalPages>1) {
            bounds = new Rect();
            String pageString = "Page " + pageNum + "/" + totalPages;
            footerPaint.getTextBounds(pageString,0,pageString.length(),bounds);
            pageCanvas.drawText(pageString,docWidth-margin-bounds.width(),docHeight-margin-footerHeight,footerPaint);
        }

        // Draw a line
        drawHorizontalLine(docHeight-margin-footerHeight-12);
    }
    private void drawHorizontalLine(int y) {
        pageCanvas.drawLine(margin, y, docWidth - margin, y, linePaint);
    }

    // Add the song either from the section views or creating them manually
    private void addSectionViews(ArrayList<View> sectionViews,
                                 ArrayList<Integer> sectionWidths,
                                 ArrayList<Integer> sectionHeights) {
        // Now add the views one at a time.  If necessary, we create a new page as we go

        // Set our starting positions and sizes
        float ypos = headerHeight + margin;
        int spaceStillAvailable = availableHeight;

        // Go through views one at a time
        for (int x=0; x<sectionViews.size(); x++) {
            View view = sectionViews.get(x);
            int newWidth = (int) ((float)sectionWidths.get(x)*sectionScaling);
            int newHeight = (int) ((float)sectionHeights.get(x)*sectionScaling);

            // Check we have available height remaining for this view.  If not, create a new page

            if (newHeight > spaceStillAvailable) {
                // Add the footer and finish the page
                createFooter();
                pdfDocument.finishPage(page);
                pageNum++;

                // Start a new page, but we no longer need a header, so add that back to the size
                startPage();
                spaceStillAvailable = availableHeight + headerHeight - newHeight;
                ypos = margin;
            } else {
                spaceStillAvailable = spaceStillAvailable - newHeight;
            }

            // Scale the view
            scaleThisView(view,newWidth,newHeight,sectionScaling);

            // Save, translate to account for new position, write, then restore the page canvas
            pageCanvas.save();
            pageCanvas.translate(margin, ypos);
            pageCanvas.scale(sectionScaling,sectionScaling);
            view.draw(pageCanvas);
            pageCanvas.restore();

            // Set the position to draw the next view
            ypos = ypos + newHeight;
        }

        // Add the footer to the last page and finish it
        createFooter();
        pdfDocument.finishPage(page);
    }

    // Do any scaling
    private void scaleThisView(View view, int width, int height, float scaleValue) {
        if (view!=null) {
            view.setPivotX(0.0f);
            view.setPivotY(0.0f);
            view.setScaleX(scaleValue);
            view.setScaleY(scaleValue);
            view.layout(0, 0, width, height);

        } else {
            Log.d("MakePDF","View was null for scaling");
        }
    }

    // Deal with saving the PDF so we can share it
    private void saveThePDF(Context c, MainActivityInterface mainActivityInterface, Uri uri) {
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c, uri);
        try {
            pdfDocument.writeTo(outputStream);
            outputStream.close();
            pdfDocument.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Uri getPDFUri(Context c, MainActivityInterface mainActivityInterface, String exportFilename) {
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface, "Export", "", exportFilename);
        if (mainActivityInterface.getStorageAccess().uriExists(c,uri)) {
            // Remove it as we want to create a new version!
            mainActivityInterface.getStorageAccess().deleteFile(c,uri);
        }
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(c, mainActivityInterface, uri, null, "Export", "", exportFilename);
        return uri;
    }
}
