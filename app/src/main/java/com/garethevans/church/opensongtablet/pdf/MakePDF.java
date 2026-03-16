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
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.garethevans.church.opensongtablet.abcnotation.InlineAbcObject;
import com.garethevans.church.opensongtablet.abcnotation.InlineAbcWebViewTagObject;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.OutputStream;
import java.util.ArrayList;

public class MakePDF {

    private final MainActivityInterface mainActivityInterface;
    private final float margin_cm = 1.5f;           // 1.5cm
    private final float footerHeight_cm = 0.6f;     // 0.
    private final int linePos = 12;
    private float maxScaling; // 14sp
    private float headerScaling = 1f;
    private boolean forceSinglePage;
    private int headerHeight, headerWidth, scaledHeaderHeight, docWidth, docHeight, availableWidth, availableHeight, pageNum=1, totalPages=1;
    private Paint linePaint, footerPaint;
    private int lineWidth;
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
    private float[] columnInfo;
    private Song thisSong;

    public MakePDF(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        maxScaling = mainActivityInterface.getPreferences().getMyPreferenceFloat("maxPDFScaling",0.75f);
        forceSinglePage = mainActivityInterface.getPreferences().getMyPreferenceBoolean("forcePDFSinglePage",false);
    }

    public void setSong(Song thisSong) {
        this.thisSong = thisSong;
    }

    public float getMaxPDFScaling() {
        return maxScaling;
    }

    public void setMaxPDFScaling(float maxScaling) {
        this.maxScaling = maxScaling;
        mainActivityInterface.getPreferences().setMyPreferenceFloat("maxPDFScaling",maxScaling);
    }

    public boolean getForceSinglePage() {
        return forceSinglePage;
    }

    public void setForceSinglePage(boolean forceSinglePage) {
        this.forceSinglePage = forceSinglePage;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("forcePDFSinglePage",forceSinglePage);
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
        lineWidth = 1;
        linePaint.setStrokeWidth(lineWidth);
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
            docWidth = (int) (((float) printAttributes.getMediaSize().getWidthMils() / 1000f) * 72f);
            docHeight = (int) (((float) printAttributes.getMediaSize().getHeightMils() / 1000f) * 72f);
        }

        pageNum = 1;
        totalPages = 1;
    }

    public void setPreferedAttributes() {
        String pdfSize = mainActivityInterface.getPreferences().getMyPreferenceString("pdfSize", "A4");

        PrintAttributes.MediaSize mediaSize;
        switch (pdfSize) {
            case "Letter":
                mediaSize = PrintAttributes.MediaSize.NA_LETTER;
                break;
            case "A4":
            default:
                mediaSize = PrintAttributes.MediaSize.ISO_A4;
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
        if (headerLayout!=null && headerHeight==0) {
            headerHeight = headerLayout.getMeasuredHeight();
        }
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
        scaledHeaderHeight = (int) ((float)headerHeight * headerScaling);

        // Do any scaling
        scaleThisView(headerLayout, headerWidth, headerHeight, headerScaling);

        pageCanvas = page.getCanvas();
        // Save the canvas, translate for correct write positioning, then restore the canvas state/position
        try {
            if (headerLayout!=null) {
                pageCanvas.save();
                pageCanvas.translate(cmToPx(margin_cm), cmToPx(margin_cm));
                pageCanvas.scale(headerScaling, headerScaling);
                headerLayout.draw(pageCanvas);
                pageCanvas.restore();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Draw a horizontal line under the heading
        drawHorizontalLine(scaledHeaderHeight + cmToPx(margin_cm) - lineWidth);
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
        int totalHeight = mainActivityInterface.getProcessSong().getTotal(sectionHeights,0,sectionHeights.size());

        // If we are adding section spaces, add this on to the heights for each section except the last
        sectionSpace = 0;
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("addSectionSpace",true) &&
                sectionHeights.size()>1) {
            sectionSpace = (int) (0.75 * maxScaling * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    mainActivityInterface.getProcessSong().getDefFontSize(), c.getResources().getDisplayMetrics()));
            maxHeight += sectionSpace*(sectionHeights.size()-1);
            totalHeight += sectionSpace*(sectionHeights.size()-1);
        }

        // Determine the size we have available based on the document sizes
        // The width is the document width minus 2x margin (left and right)
        // The height is the document height minus 2x margin (top and bottom) and header and footer
        // The header can vary depending on the content, but the footer is always the same
        setAvailableWidth();
        setAvailableHeight();

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
            sectionScaling = Math.max(sectionScaling, maxScaling);
        }

        // If we are forcing single page PDF, scale to the height
        float maxVerticalScaling = availableHeight/ (float) totalHeight;
        maxVerticalScaling = Math.min(sectionScaling, maxVerticalScaling);
        if (forceSinglePage && (thisSong==null || thisSong.getCapoprint()==null || !thisSong.getCapoprint().equals("ISTHESET"))) {
            sectionScaling = maxVerticalScaling;
        }

        // Now plan out how many pages we will need
        // We need to do this before being able to write the footer
        int spaceStillAvailable = availableHeight;
        for (int sectionHeight:sectionHeights) {
            if (sectionHeight*sectionScaling > spaceStillAvailable) {
                totalPages++;
                spaceStillAvailable = availableHeight - scaledHeaderHeight - (int)(sectionHeight*sectionScaling);
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

    // Decide on columns
    public void getColumns(ArrayList<View> sectionViews, ArrayList<Integer> sectionWidths, ArrayList<Integer> sectionHeights) {
        columnInfo = mainActivityInterface.getProcessSong().getPDFColumnInfo(thisSong, sectionViews, sectionWidths, sectionHeights, availableWidth, availableHeight, false, c.getResources().getDisplayMetrics());
    }

    // This helps check the sizes if the PDF printer changes formats, etc.
    private void checkSizes(ArrayList<Integer> sectionWidths, ArrayList<Integer> sectionHeights) {
        setAvailableWidth();
        setAvailableHeight();
        determineSpaceAndPages(sectionWidths,sectionHeights);
    }

    private void setAvailableWidth() {
        if (docWidth>0) {
            availableWidth = docWidth - (cmToPx(margin_cm) * 2);
        }
    }

    private void setAvailableHeight() {
        if (docHeight>0) {
            availableHeight = docHeight - (cmToPx(margin_cm) * 2) - scaledHeaderHeight - cmToPx(footerHeight_cm) - (2*lineWidth) - linePos;
        }
    }

    public void setHeaderHeight(int headerHeight) {
        this.headerHeight = headerHeight;
        scaledHeaderHeight = Math.round(headerHeight * headerScaling);
        setAvailableHeight();
    }

    // Add the song either from the section views or creating them manually
    private void addSectionViews(ArrayList<View> sectionViews,
                                 ArrayList<Integer> sectionWidths,
                                 ArrayList<Integer> sectionHeights) {

        // For non-XML songs, we always use 1 column
        if (columnInfo==null) {
            columnInfo = new float[13];
        }
        if ((thisSong!=null && thisSong.getFilename()!=null && mainActivityInterface.getStorageAccess().isIMGorPDF(thisSong.getFilename())) ||
                !mainActivityInterface.getPreferences().getMyPreferenceBoolean("forcePDFSinglePage",false)) {
            columnInfo[0] = 1;
        }

        if (columnInfo!=null && columnInfo.length>0) {
            switch ((int)columnInfo[0]) {
                case 2:
                    doubleColumn(sectionViews, sectionWidths, sectionHeights);
                    break;
                case 3:
                    tripleColumn(sectionViews, sectionWidths, sectionHeights);
                    break;
                case 1:
                default:
                    singleColumn(sectionViews, sectionWidths, sectionHeights);
                    break;
            }
        }
    }

    private int getTotalColumnSectionSpaces(ArrayList<Integer> sectionHeights, int sectionSpace) {
        // This goes through the sections and for each one that is greater than 0, adds a space
        // This isn't done for the last section
        int totalSpace = 0;
        if (sectionHeights.size()>1 && mainActivityInterface.getPreferences().getMyPreferenceBoolean("addSectionSpace",true)) {
            for (int x=0; x<sectionHeights.size()-1; x++) {
                totalSpace += sectionHeights.get(x)>0 ? sectionSpace:0;
            }
        }
        return totalSpace;
    }

    private void singleColumn(ArrayList<View> sectionViews,
                              ArrayList<Integer> sectionWidths,
                              ArrayList<Integer> sectionHeights) {
        // Now add the views one at a time.  If necessary, we create a new page as we go
        // Set our starting positions and sizes
        Log.d(TAG,"single column");
        checkSizes(sectionWidths,sectionHeights);

        /*
            columnInfo[0] = 1  Number of columns
            columnInfo[1]      Overall best scale
            columnInfo[2]      Column 1 max width
            columnInfo[3]      Column 1 total height (including section spaces)
            columnInfo[4]      Section space per view except last in column
         */
        // We may have changed the orientation since first measuring, so check again

        boolean forcePDFSinglePage = mainActivityInterface.getPreferences().getMyPreferenceBoolean("forcePDFSinglePage",false);
        boolean isTheSet = thisSong!=null && thisSong.getCapoprint()!=null && thisSong.getCapoprint().equals("ISTHESET");
        sectionSpace = (int)columnInfo[4];

        // If this is the set list, reduce the spacing
        float tempSpacing = isTheSet ? (int) (0.25f * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                mainActivityInterface.getProcessSong().getDefFontSize(), c.getResources().getDisplayMetrics())):sectionSpace;

        float columnScale = sectionScaling;

        // If we are processing a song (not the set list), but aren't adding section spaces, set that to 0
        if (!isTheSet && !mainActivityInterface.getPreferences().getMyPreferenceBoolean("addSectionSpace",true)) {
            tempSpacing = 0;
        }

        // If we want full scale (not for the setlist or song list though)
        if (forcePDFSinglePage && !isTheSet) {
            float widthScaling = availableWidth / columnInfo[2];
            float heightScaling = availableHeight / columnInfo[3];
            columnScale = Math.min(widthScaling, heightScaling);
            if (columnInfo[2]==0 && columnInfo[3]==0) {
                // Empty song, don't try for infinite scale
                columnScale = 1;
            }
        }

        if (thisSong!=null && thisSong.getUser1()!=null && thisSong.getUser1().equals("PRINT_SONG_LIST")) {
            float widthScaling = availableWidth / columnInfo[2];
            float heightScaling = 0.5f;
            columnScale = Math.min(widthScaling, heightScaling);
        }

        // If this is a PDF or image, we want full scale
        boolean pdfFile = mainActivityInterface.getStorageAccess().isSpecificFileExtension("pdf",thisSong.getFilename());
        boolean imgFile = mainActivityInterface.getStorageAccess().isSpecificFileExtension("image",thisSong.getFilename());
        if ((pdfFile || imgFile) && !sectionViews.isEmpty()) {
            float widthScaling = (float) availableWidth / sectionWidths.get(0);
            float heightScaling = (float) availableHeight / sectionHeights.get(0);
            columnScale = Math.min(widthScaling, heightScaling);
            forcePDFSinglePage = false;
        }

        // Set the starting y position for the content
        float ypos = scaledHeaderHeight + cmToPx(margin_cm) + lineWidth;
        int spaceStillAvailable = availableHeight;

        // Go through views one at a time
        for (int x=0; x<sectionViews.size(); x++) {
            View view = sectionViews.get(x);
            int newWidth = (int) ((float)sectionWidths.get(x)*columnScale);
            int newHeight = (int) ((float)sectionHeights.get(x)*columnScale);

            // Check we have available height remaining for this view.  If not, create a new page
            if ((isTheSet || !forcePDFSinglePage) && newHeight > spaceStillAvailable) {
                // Add the footer and finish the page
                createFooter();
                pdfDocument.finishPage(page);
                pageNum++;

                // Start a new page, but we no longer need a header
                startPage();
                spaceStillAvailable = availableHeight + scaledHeaderHeight - newHeight - Math.round(columnScale*tempSpacing);
                ypos = cmToPx(margin_cm);
            } else if (newHeight>0) {
                    spaceStillAvailable = spaceStillAvailable - newHeight - Math.round(columnScale * tempSpacing);
            }

            // Scale the view if not an image or pdf as we have already done this
            // PDF and images are only ever single column output
            if (!imgFile && !pdfFile) {
                scaleThisView(view, sectionWidths.get(x), sectionHeights.get(x), columnScale);
            }

            // Save, translate to account for new position, write, then restore the page canvas
            pageCanvas.save();
            pageCanvas.translate(cmToPx(margin_cm),ypos);
            pageCanvas.scale(columnScale, columnScale);

            Log.d(TAG,"try to check the children");
            Log.d(TAG,"inlineAbcObjects.count():"+mainActivityInterface.getAbcNotation().countInlineAbcObjects());
            if (mainActivityInterface.getAbcNotation().countInlineAbcObjects()>0) {
                for (int i = 0; i < ((LinearLayout) view).getChildCount(); i++) {
                    View thisView = ((LinearLayout) view).getChildAt(i);
                    Log.d(TAG, "child:" + thisView.getClass().getName());
                    if (thisView.getClass().getName().equals("android.widget.ImageView")) {
                        // Get the tag and therefore the abcObject
                        InlineAbcWebViewTagObject tagObject = (InlineAbcWebViewTagObject) thisView.getTag();
                        int item = tagObject.getObjectNumber();
                        Log.d(TAG, "tagObject item:" + item);
                        InlineAbcObject inlineAbcObject = mainActivityInterface.getAbcNotation().getInlineAbcObjects().get(item);
                        Log.d(TAG, "size of object:" + inlineAbcObject.getAbcWidth() + "x" + inlineAbcObject.getAbcHeight());
                        Log.d(TAG, "size of ImageView:" + thisView.getMeasuredWidth() + "x" + thisView.getMeasuredHeight());
                        float resize = (float) inlineAbcObject.getAbcWidth() / (float) thisView.getMeasuredWidth();
                        int newImgHeight = (int) (resize * inlineAbcObject.getAbcHeight());
                        ViewGroup.LayoutParams lp = thisView.getLayoutParams();
                        lp.height = newImgHeight;
                        //thisView.setLayoutParams(llp);
                        //thisView.invalidate();
                        inlineAbcObject.setIsPDF(true);
                        mainActivityInterface.getAbcNotation().getInlineAbcObjects().get(item).drawTheImageView((ImageView) thisView);
                        Log.d(TAG, "size of ImageView:" + thisView.getMeasuredWidth() + "x" + thisView.getMeasuredHeight());
                        inlineAbcObject.setIsPDF(false);
                        thisView.setVisibility(View.VISIBLE);
                    }
                }
            }

            //Log.d(TAG,"view:"+view.getClass()+"  width:"+view.getMeasuredWidth()+"  height:"+view.getMeasuredHeight());
            //TextView logo = new TextView(c);
            //logo.setImageDrawable(ResourcesCompat.getDrawable(c.getResources(), R.drawable.splash_logo,null));
            //logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            //logo.setText("HHELLLOO");
            //logo.setLayoutParams(new LinearLayout.LayoutParams(200,20));
            //logo.setBackgroundColor(Color.RED);
            //logo.draw(pageCanvas);
            view.draw(pageCanvas);
            pageCanvas.restore();

            // Set the position to draw the next view, but add the section space if required
            ypos = ypos + newHeight + (newHeight>0? Math.round(columnScale*tempSpacing):0);
        }

        // Add the footer to the last page and finish it
        createFooter();
        pdfDocument.finishPage(page);

        // Because there may be additional pages added (multiple files combined)
        pageNum++;
        totalPages++;
    }

    private void doubleColumn(ArrayList<View> sectionViews,
                              ArrayList<Integer> sectionWidths,
                              ArrayList<Integer> sectionHeights) {
        Log.d(TAG,"Two columns");
        /* columnInfo[0] = 2  - Number of columns
                *columnInfo[1] - Overall best scale
                columnInfo[2] - Break point
                *columnInfo[3] - Best col 1 scale
                columnInfo[4] - Column 1 max width
                columnInfo[5] - Column 1 total height
                *columnInfo[6] - Best col 2 scale
                columnInfo[7] - Column 2 max width
                columnInfo[8] - Column 2 total height
                columnInfo[9] - Section space per view except last in column
         */

        /*
        availableWidth = 2*columnWidth + padding
        2*columnWidth = availableWidth - padding
        columnWidth = (availableWidth - padding)/2
        */

        checkSizes(sectionWidths,sectionHeights);
        float columnPadding = cmToPx(margin_cm)/2f;
        float columnWidth = (availableWidth-columnPadding)/2f;
        sectionSpace = (int)columnInfo[9];

        boolean useSmallestScale = !mainActivityInterface.getPreferences().getMyPreferenceBoolean("songAutoScaleColumnMaximise",true);
        // We may have changed the orientation since first measuring, so check again
        int sectionsInCol1 = (int)columnInfo[2];
        int sectionsInCol2 = sectionViews.size() -sectionsInCol1;

        float columnScale1 = Math.min(columnWidth/columnInfo[4],availableHeight/columnInfo[5]);
        float columnScale2 = Math.min(columnWidth/columnInfo[7],availableHeight/columnInfo[8]);

        if (useSmallestScale) {
            float smallestScale = Math.min(columnScale1,columnScale2);
            columnScale1 = smallestScale;
            columnScale2 = smallestScale;
        }

        // Now add the views one at a time until the first column is completed
        // We will only be using a single page, so no need to keep track of the sizes
        // Set our starting positions and sizes
        float ypos = scaledHeaderHeight + cmToPx(margin_cm) + lineWidth;

        // Go through views one at a time for column 1
        for (int x=0; x<columnInfo[2]; x++) {
            View view = sectionViews.get(x);
            int newWidth = (int) ((float)sectionWidths.get(x)*columnScale1);
            int newHeight = (int) ((float)sectionHeights.get(x)*columnScale1);

            // Scale the view
            scaleThisView(view,sectionWidths.get(x),sectionHeights.get(x),columnScale1);

            // Save, translate to account for new position, write, then restore the page canvas
            // Column 1 is indented by the page margin only
            pageCanvas.save();
            pageCanvas.translate(cmToPx(margin_cm),ypos);
            pageCanvas.scale(columnScale1,columnScale1);
            view.draw(pageCanvas);
            pageCanvas.restore();

            // Set the position to draw the next view
            ypos = ypos + newHeight + (columnScale1*sectionSpace);
        }

        // Set our starting positions and sizes
        ypos = scaledHeaderHeight + cmToPx(margin_cm);

        // Go through views one at a time for column 2
        for (int x=(int)columnInfo[2]; x<sectionViews.size(); x++) {
            View view = sectionViews.get(x);
            int newWidth = (int) ((float)sectionWidths.get(x)*columnScale2);
            int newHeight = (int) ((float)sectionHeights.get(x)*columnScale2);

            // Scale the view
            scaleThisView(view,sectionWidths.get(x),sectionHeights.get(x),columnScale2);

            // Save, translate to account for new position, write, then restore the page canvas
            // Column 2 is indented by the page margin + colum 1 + padding
            pageCanvas.save();
            pageCanvas.translate(cmToPx(margin_cm) + columnWidth + columnPadding,ypos);
            pageCanvas.scale(columnScale2,columnScale2);
            view.draw(pageCanvas);
            pageCanvas.restore();

            // Set the position to draw the next view
            ypos = ypos + newHeight + (columnScale2*sectionSpace);
        }

        // Add the footer to the last page and finish it
        createFooter();
        pdfDocument.finishPage(page);

        // Because there may be additional pages added (multiple files combined)
        pageNum++;
        totalPages++;
    }

    private void tripleColumn(ArrayList<View> sectionViews,
                              ArrayList<Integer> sectionWidths,
                              ArrayList<Integer> sectionHeights) {
        /* columnInfo[0] = 3  - Number of columns
                columnInfo[1]Overall best scale
                        columnInfo[2]Break point 1
                        columnInfo[3]Break point 2
                        columnInfo[4]Best col 1 scale
                        columnInfo[5]Column 1 max width
                        columnInfo[6]Column 1 total height
                        columnInfo[7]Best col 2 scale
                        columnInfo[8]Column 2 max width
                        columnInfo[9]Column 2 total height
                        columnInfo[10]Best col 3 scale
                        columnInfo[11]Column 3 max width
                        columnInfo[12]Column 3 total height
                        columnInfo[13]Section space per view except last in column

         */
        Log.d(TAG,"Three columns");

        float padding = cmToPx(margin_cm)/2f;
        float columnWidth = (availableWidth-(2*padding))/3f;
        sectionSpace = (int)columnInfo[13];
        boolean useSmallestScale = !mainActivityInterface.getPreferences().getMyPreferenceBoolean("songAutoScaleColumnMaximise",true);
        int sectionsInCol1 = (int)columnInfo[2];
        int sectionsInCol2 = (int)columnInfo[3] - (int)columnInfo[2];
        int sectionsInCol3 = sectionViews.size() - sectionsInCol2 - sectionsInCol1;

        // We may have changed the orientation since first measuring, so check again
        float columnScale1 = Math.min(columnWidth/columnInfo[5],(availableHeight - ((sectionsInCol1-1)*sectionSpace))/columnInfo[6]);
        float columnScale2 = Math.min(columnWidth/columnInfo[8],(availableHeight - ((sectionsInCol2-1)*sectionSpace))/columnInfo[9]);
        float columnScale3 = Math.min(columnWidth/columnInfo[11],(availableHeight - ((sectionsInCol3-1)*sectionSpace))/columnInfo[12]);
        if (useSmallestScale) {
            float smallestScale = Math.min(Math.min(columnScale1,columnScale2),columnScale3);
            columnScale1 = smallestScale;
            columnScale2 = smallestScale;
            columnScale3 = smallestScale;
        }

        // Now add the views one at a time until the first column is completed
        // We will only be using a single page, so no need to keep track of the sizes
        // Set our starting positions and sizes
        float ypos = scaledHeaderHeight + cmToPx(margin_cm);

        // Go through views one at a time for column 1
        for (int x=0; x<columnInfo[2]; x++) {
            View view = sectionViews.get(x);
            int newWidth = (int) ((float)sectionWidths.get(x)*columnScale1);
            int newHeight = (int) ((float)sectionHeights.get(x)*columnScale1);

            // Scale the view
            scaleThisView(view,sectionWidths.get(x),sectionHeights.get(x),columnScale1);

            // Save, translate to account for new position, write, then restore the page canvas
            // Column 1 should be indented by the page margin only
            pageCanvas.save();
            pageCanvas.translate(cmToPx(margin_cm),ypos);
            pageCanvas.scale(columnScale1,columnScale1);
            view.draw(pageCanvas);
            pageCanvas.restore();

            // Set the position to draw the next view
            ypos = ypos + newHeight + (sectionSpace*columnScale1);
        }

        // Set our starting positions and sizes
        ypos = scaledHeaderHeight + cmToPx(margin_cm);

        // Go through views one at a time for column 2
        for (int x=(int)columnInfo[2]; x<columnInfo[3]; x++) {
            View view = sectionViews.get(x);
            int newWidth = (int) ((float)sectionWidths.get(x)*columnScale2);
            int newHeight = (int) ((float)sectionHeights.get(x)*columnScale2);

            // Scale the view
            scaleThisView(view,sectionWidths.get(x),sectionHeights.get(x),columnScale2);

            // Save, translate to account for new position, write, then restore the page canvas
            // Column 2 should be indented by the page margin + first column width + padding
            pageCanvas.save();
            pageCanvas.translate(cmToPx(margin_cm)+columnWidth+padding,ypos);
            pageCanvas.scale(columnScale2,columnScale2);
            view.draw(pageCanvas);
            pageCanvas.restore();

            // Set the position to draw the next view
            ypos = ypos + newHeight + (sectionSpace*columnScale2);
        }

        // Set our starting positions and sizes
        ypos = scaledHeaderHeight + cmToPx(margin_cm);

        // Go through views one at a time for column 3
        for (int x=(int)columnInfo[3]; x<sectionViews.size(); x++) {
            View view = sectionViews.get(x);
            int newWidth = (int) ((float)sectionWidths.get(x)*columnScale3);
            int newHeight = (int) ((float)sectionHeights.get(x)*columnScale3);

            // Scale the view
            scaleThisView(view,sectionWidths.get(x),sectionHeights.get(x),columnScale3);

            // Save, translate to account for new position, write, then restore the page canvas
            // Column 3 should be indented by the page margin + first/second column widths + 2*padding
            pageCanvas.save();
            pageCanvas.translate(cmToPx(margin_cm)+(columnWidth*2)+(padding*2),ypos);
            pageCanvas.scale(columnScale3,columnScale3);
            view.draw(pageCanvas);
            pageCanvas.restore();

            // Set the position to draw the next view
            ypos = ypos + newHeight + (sectionSpace*columnScale3);
        }

        // Add the footer to the last page and finish it
        createFooter();
        pdfDocument.finishPage(page);

        // Because there may be additional pages added (multiple files combined)
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
            //view.layout(0, 0, (int)((docWidth - (cmToPx(margin_cm) * 2))/scaleValue), (int)(height/scaleValue));
            view.layout(0, 0, (int)(width*scaleValue), (int)(height*scaleValue));

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
        mainActivityInterface.getStorageAccess().makeSureFileIsRegistered("Export", "", exportFilename,true);
        return uri;
    }

    public PdfDocument getPdfDocument() {
        return pdfDocument;
    }
    public PrintAttributes getPrintAttributes() {
        return printAttributes;
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
