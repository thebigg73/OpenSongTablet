package com.garethevans.church.opensongtablet.pdf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.print.PrintAttributes;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.OutputStream;
import java.util.ArrayList;

public class MakePDF {

    private final MainActivityInterface mainActivityInterface;
    private final float margin_cm = 1.5f;           // 1.5cm
    private final float footerHeight_cm = 0.6f;     // 0.
    private final int linePos = 12;
    private final float maxScaling = 1f; // 14sp
    private int headerHeight, headerWidth, docWidth, docHeight, availableHeight, pageNum=1, totalPages=1;
    private Paint linePaint, footerPaint;
    private PdfDocument pdfDocument;
    private PdfDocument.Page page;
    private float sectionScaling;
    private Canvas pageCanvas;
    private final String TAG = "MakePDF";
    private PrintAttributes printAttributes;
    private boolean isSetListPrinting = false, exportingSongList = false;
    private boolean showTotalPage = true;
    private String exportFilename;
    private final Context c;
    private int sectionSpace;

    public MakePDF(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }

    public void createBlankPDFDoc(String exportFilename, PrintAttributes printAttributes){
        this.exportFilename = exportFilename;
        this.printAttributes = printAttributes;

        // Set the paint values
        setPaintDefaults();

        // Create the document
        pdfDocument = new PdfDocument();

        // Initialise the sizes
        initialiseSizes();
    }

    // Create the content for the current item
    public void addCurrentItemToPDF(ArrayList<View> sectionViews, ArrayList<Integer> sectionWidths,
                                    ArrayList<Integer> sectionHeights, LinearLayout headerLayout,
                                    int headerLayoutWidth, int headerLayoutHeight) {
        // Add or create the header
        headerHeight = headerLayoutHeight;
        headerWidth = headerLayoutWidth;
        startPage();

        createHeader(headerLayout);

        // Get the space available to the song sections and pages needed to fit them all in
        determineSpaceAndPages(sectionWidths, sectionHeights);

        // Add in the song sections and the footer at the bottom of each page.
        addSectionViews(sectionViews, sectionWidths, sectionHeights);
    }

    public Uri getPDFFile(String exportFilename) {
        // Save the PDF document ready for sharing
        Uri uri = getPDFUri(exportFilename);
        //pdfDocument.finishPage(page);
        saveThePDF(uri);
        return uri;
    }

    public void setExportingSongList(boolean exportingSongList) {
        this.exportingSongList = exportingSongList;
    }

    // This makes a single PDF based on one item
    public Uri createTextPDF(ArrayList<View> sectionViews, ArrayList<Integer> sectionWidths,
                             ArrayList<Integer> sectionHeights, LinearLayout headerLayout,
                             int headerLayoutWidth, int headerLayoutHeight, String exportFilename,
                             PrintAttributes printAttributes) {

        // Create the PDF doc with the default settings
        headerHeight = headerLayoutHeight;
        createBlankPDFDoc(exportFilename,printAttributes);

        // Add the currently drawn sections to the PDF document
        // These are sent from the exportFragment
        addCurrentItemToPDF(sectionViews, sectionWidths, sectionHeights,
                headerLayout, headerLayoutWidth, headerLayoutHeight);

        // Save the PDF document and return the PDF uri
        return getPDFFile(exportFilename);
    }

    // Initialise the PDF and Paint stuff
    private void setPaintDefaults() {
        // For drawing the horizontal lines
        linePaint = new Paint();
        int textColor = mainActivityInterface.getMyThemeColors().getPdfTextColor();
        linePaint.setColor(textColor);
        linePaint.setAlpha(120);
        //linePaint.setColor(Color.LTGRAY);
        linePaint.setStrokeWidth(1);
        linePaint.setAntiAlias(true);

        // For writing the footer
        footerPaint = new Paint();
        footerPaint.setColor(textColor);
        footerPaint.setAlpha(200);
        //footerPaint.setColor(Color.DKGRAY);
        footerPaint.setTypeface(mainActivityInterface.getMyFonts().getLyricFont());
        footerPaint.setTextSize(10);
        footerPaint.setAntiAlias(true);
    }

    // Initialise the sizes and page numbers
    private void initialiseSizes() {

        if (printAttributes==null) {
            setPreferedAttributes();
        } else {
            docWidth = (int) (((float)printAttributes.getMediaSize().getWidthMils()/1000f)*72f);
            docHeight = (int) (((float)printAttributes.getMediaSize().getHeightMils()/1000f)*72f);
        }

        pageNum = 1;
        totalPages = 1;

    }

    public void setPreferedAttributes() {
        String pdfSize = mainActivityInterface.getPreferences().getMyPreferenceString("pdfSize", "A4");

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
        printAttributes = new PrintAttributes.Builder().setMediaSize(mediaSize).build();
        docWidth =  (int) (((float)mediaSize.getWidthMils()/1000.0f)*72.0f);
        docHeight = (int) (((float)mediaSize.getHeightMils()/1000.0f)*72.0f);
    }

    // Create and start the new page based on the current page number
    private void startPage() {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(docWidth, docHeight, pageNum).create();
        page = pdfDocument.startPage(pageInfo);
        pageCanvas = page.getCanvas();

        // Set the canvas density to 72dpi.  This way pixels match points!
        pageCanvas.setDensity(72);

        // Set the background color
        pageCanvas.drawColor(mainActivityInterface.getMyThemeColors().getPdfBackgroundColor());
    }

    // Headers and footers
    private void createHeader(LinearLayout headerLayout) {
        float headerScaling;
        // Get the maximum scale possible by the width of the document
        float maxWidthScaling = ((float) (docWidth - (cmToPx(margin_cm) * 2))) / (float) headerWidth;
        // Get the maximum scale possible by the preferred maximum header height (2.5cm)
        float maxHeightScaling = ((float) cmToPx(2.5f) / (float) headerHeight);
        if (headerHeight==0) {
            headerScaling = 1f;
        } else {
            headerScaling = Math.min(maxWidthScaling, maxHeightScaling);
        }

        // To avoid text being too large, make sure the scaling doesn't exceed the maxScaling
        headerScaling = Math.min(headerScaling,maxScaling);

        // Check for min scaling of 0.5
        headerScaling = Math.max(headerScaling,0.5f);

        headerWidth = (int) ((float)headerWidth * headerScaling);
        headerHeight = (int) ((float)headerHeight * headerScaling);

        // Do any scaling
        scaleThisView(headerLayout, headerWidth, headerHeight, headerScaling);

        pageCanvas = page.getCanvas();
        // Save the canvas, translate for correct write positioning, then restore the canvas state/position
        try {
            pageCanvas.save();
            pageCanvas.translate(cmToPx(margin_cm), cmToPx(margin_cm));
            pageCanvas.scale(headerScaling, headerScaling);
            headerLayout.draw(pageCanvas);
            pageCanvas.restore();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Draw a horizontal line under the heading
        drawHorizontalLine(headerHeight + cmToPx(margin_cm));
        headerHeight = headerHeight + 4;
    }

    // Decide on sizes required
    private void determineSpaceAndPages(ArrayList<Integer> sectionWidths,
                                        ArrayList<Integer> sectionHeights) {
        // The max scaling is preferrably scaling the sections to the width of the screen
        // However, if this means that any section is too tall to fit the space between ther
        // header and footer, we will have to scale it down, otherwise it will never fit.

        // Decide on our scaling
        int maxWidth = mainActivityInterface.getProcessSong().getMaxValue(sectionWidths,0,sectionWidths.size());
        int maxHeight = mainActivityInterface.getProcessSong().getMaxValue(sectionHeights,0,sectionHeights.size());

        // If we are adding section spaces, add this on to the heights for each section except the last
        sectionSpace = 0;
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("addSectionSpace",true) &&
                sectionHeights.size()>1) {
            sectionSpace = (int) (0.75 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    mainActivityInterface.getProcessSong().getDefFontSize(), c.getResources().getDisplayMetrics()));
            maxHeight += sectionSpace*(sectionWidths.size()-1);
        }

        // Determine the size we have available based on the document sizes
        // The width is the document width minus 2x margin (left and right)
        // The height is the document height minus 2x margin (top and bottom) and header and footer
        // The header can vary depending on the content, but the footer is always the same

        int availableWidth = docWidth - (cmToPx(margin_cm) * 2);
        availableHeight = docHeight - (cmToPx(margin_cm) * 2) - headerHeight - cmToPx(footerHeight_cm)-linePos;

        // We need to make sure that at least all sections can fit on the available page
        // If any are bigger, we need to scale down
        // Firstly scale to the max width available
        sectionScaling = (float)availableWidth /(float)maxWidth;

        // If it is bigger than maxScaling, it will look silly, so set this as max
        sectionScaling = Math.min(sectionScaling,maxScaling);

        // Now check how this affects the height of the views
        // We only reduce the scaling if the section heights are too big
        if ((maxHeight*sectionScaling) > availableHeight) {
            sectionScaling = (float)availableHeight/(float)maxHeight;
        }

        // Check for min scaling of 0.75f if exporting song list
        if (exportingSongList) {
            sectionScaling = Math.max(sectionScaling, 0.75f);
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
        exportingSongList = false;
    }

    // The footer creation with OpenSongApp credit and page numbering if required
    private void createFooter() {
        // The OpenSongApp credit.  This is drawn after the rest of the page content has finished
        Rect bounds = new Rect();
        String string = "Prepared by OpenSongApp (<a href='https://www.opensongapp.com'>https://www.opensongapp.com</a>)";
        footerPaint.getTextBounds(string,0,string.length(),bounds);
        pageCanvas.drawText(String.valueOf(Html.fromHtml(string)),cmToPx(margin_cm),docHeight-cmToPx(margin_cm)-cmToPx(footerHeight_cm),footerPaint);

        // The page numbering if there is more than 1 page needed
        if (totalPages>1) {
            bounds = new Rect();
            String pageString;
            if (showTotalPage) {
                pageString = "Page " + pageNum + "/" + totalPages;
            } else {
                pageString = "Page " + pageNum;
            }

            footerPaint.getTextBounds(pageString,0,pageString.length(),bounds);
            pageCanvas.drawText(pageString,docWidth-cmToPx(margin_cm)-bounds.width(),docHeight-cmToPx(margin_cm)-cmToPx(footerHeight_cm),footerPaint);
        }

        // Draw a line
        drawHorizontalLine(docHeight-cmToPx(margin_cm)-cmToPx(footerHeight_cm)-linePos);
    }
    private void drawHorizontalLine(int y) {
        pageCanvas.drawLine(cmToPx(margin_cm), y, docWidth - cmToPx(margin_cm), y, linePaint);
    }

    // Add the song either from the section views or creating them manually
    private void addSectionViews(ArrayList<View> sectionViews,
                                 ArrayList<Integer> sectionWidths,
                                 ArrayList<Integer> sectionHeights) {
        // Now add the views one at a time.  If necessary, we create a new page as we go

        // Set our starting positions and sizes
        float ypos = headerHeight + cmToPx(margin_cm);
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
                ypos = cmToPx(margin_cm);
            } else {
                spaceStillAvailable = spaceStillAvailable - newHeight - sectionSpace;
            }

            // Scale the view
            scaleThisView(view,newWidth,newHeight,sectionScaling);

            // Save, translate to account for new position, write, then restore the page canvas
            pageCanvas.save();
            pageCanvas.translate(cmToPx(margin_cm),ypos);
            pageCanvas.scale(sectionScaling,sectionScaling);
            view.draw(pageCanvas);
            pageCanvas.restore();

            // Set the position to draw the next view
            ypos = ypos + newHeight + sectionSpace;
        }

        // Add the footer to the last page and finish it
        createFooter();
        pdfDocument.finishPage(page);

        // Because there may be addition pages added (multiple files combined)
        pageNum++;
        totalPages++;
    }

    // Do any scaling
    private void scaleThisView(View view, int width, int height, float scaleValue) {
        if (view!=null) {
            view.setPivotX(0.0f);
            view.setPivotY(0.0f);
            view.setScaleX(scaleValue);
            view.setScaleY(scaleValue);
            view.layout(0, 0, (int)((docWidth - (cmToPx(margin_cm) * 2))/scaleValue), (int)(height/scaleValue));

        } else {
            Log.d(TAG,"View was null for scaling");
        }
    }

    // Deal with saving the PDF so we can share it
    private void saveThePDF(Uri uri) {
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
        try {
            pdfDocument.writeTo(outputStream);
            outputStream.close();
            pdfDocument.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        showTotalPage = true;
    }
    private Uri getPDFUri(String exportFilename) {
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Export", "", exportFilename);

        // Remove it as we want to create a new version!
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" getPDFUri Create Export/"+exportFilename+" deleteOld=true");
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, uri, null, "Export", "", exportFilename);
        return uri;
    }

    public PdfDocument getPdfDocument() {
        return pdfDocument;
    }
    public PrintAttributes getPrintAttributes() {
        return printAttributes;
    }
    public int getDocWidth() {
        return docWidth;
    }
    public int getDocHeight() {
        return docHeight;
    }

    private int cmToPx(float cm) {
        // Convert cm to inches by dividing by 2.54, the to dpi by multiplying by 72 (resolution)
        return Math.round((cm/2.54f)*72);
    }

    public void setIsSetListPrinting(boolean isSetListPrinting) {
        this.isSetListPrinting = isSetListPrinting;
        showTotalPage = !isSetListPrinting;
    }
    public boolean getIsSetListPrinting() {
        return isSetListPrinting;
    }
    public String getExportFilename() {
        return exportFilename;
    }
}
