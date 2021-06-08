package com.garethevans.church.opensongtablet.export;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.OutputStream;

public class MakePDF {

    private int margin;
    private int paintSize;
    private int headerHeight;
    private int footerHeight;
    private final int docWidth  = 595;  // Based on 1/72 of an inch  595/72 * 2.54 = 21.0cm
    private final int docHeight = 842;  // Based on 1/72 of an inch  842/72 * 2.54 = 29.7cm
    private int lyricheight;
    private int lyricwidth;
    private int chordColor;
    private int lyricColor;
    private int dpi;
    private Paint paint;

    public Uri createPDF(Context c, MainActivityInterface mainActivityInterface, Song thisSong) {
        String newFilename = thisSong.getFolder().replace("/","_");
        if (!newFilename.endsWith("_")) {
            newFilename = newFilename + "_";
        }
        newFilename = newFilename + thisSong.getFilename() + ".pdf";
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(c,mainActivityInterface.getPreferences(),"Export","",newFilename);
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(c,mainActivityInterface.getPreferences(),uri, "application/pdf","Export","",newFilename);

        // Set the paint values
        setPaintDefaults();

        // create a new document
        PdfDocument pdfDocument = new PdfDocument();

        // create a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(docWidth,docHeight,1).create();  // Sizes are A4
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        // Test write the header to the page to get its height
        Canvas canvas = page.getCanvas();
        dpi = 72;
        canvas.setDensity(dpi);  // This way pixels match points!
        Canvas chead = new Canvas();
        Canvas cfoot = new Canvas();
        Canvas clyrics = new Canvas();

        writeTheHeader(c,thisSong,chead,0);
        Log.d("d","headerHeight="+headerHeight);
        // Wipe it for now
        clearPage(page);

        // Test write the footer to the page to get its height
        writeTheFooter(cfoot,0);
        Log.d("d","footerHeight="+footerHeight);
        // Wipe it for now
        clearPage(page);

        // Test write the footer to the page to get its height
        writePDFContent(c,mainActivityInterface,thisSong,clyrics,0,1.0f);
        Log.d("d","lyricWidth="+lyricwidth);
        Log.d("d","lyricHeight="+lyricheight);
        // Wipe it for now
        clearPage(page);

        // Determine the maxscaling for the lyrics part
        float scaling = getScaling();

        // Add the header back in
        writeTheHeader(c,thisSong,canvas,margin);
        // Add the lyrics back in
        writePDFContent(c,mainActivityInterface,thisSong,canvas,headerHeight+margin,scaling);
        // Add the footer back in
        writeTheFooter(canvas,(docHeight-margin));

        // write the PDF document
        pdfDocument.finishPage(page);
        saveThePDF(c,mainActivityInterface,uri,pdfDocument);

        return uri;
    }

    private void clearPage(PdfDocument.Page page) {
        Canvas canvas = page.getCanvas();
        Paint clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRect(0, 0, 0, 0, clearPaint);
    }

    private void setPaintDefaults() {
        paint = new Paint();
        paintSize = 16;
        margin = 54;

        paint.setColor(Color.BLACK);
        paint.setTextSize(paintSize);
        // Keep the colours black and white for now
        chordColor = Color.BLACK;
        lyricColor = Color.BLACK;
    }
    private void saveThePDF(Context c, MainActivityInterface mainActivityInterface, Uri uri, PdfDocument pdfDocument) {
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c, uri);
        try {
            pdfDocument.writeTo(outputStream);
            pdfDocument.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        pdfDocument.close();
    }

    private void writeTheHeader(Context c, Song thisSong, Canvas canvas, int ypos) {
        int height = 0;
        Rect bounds = new Rect();
        paintSize = 20;
        paint.setTextSize(paintSize);
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        String string = thisSong.getTitle();
        // Add the title and the key (if it isn't blank)
        if (thisSong.getKey()!=null && !thisSong.getKey().isEmpty()) {
            string = string + " (" + thisSong.getKey() + ")";
        }
        paint.getTextBounds(string,0,string.length(),bounds);
        height = height + bounds.height();
        canvas.drawText(string,margin,ypos,paint);
        // Get ready for the subheadings
        ypos = ypos + paintSize;
        paintSize = 12;
        paint.setTextSize(paintSize);

        paint.setTypeface(Typeface.DEFAULT);
        // If the author isn't blank
        if (thisSong.getAuthor()!=null && !thisSong.getAuthor().isEmpty()) {
            string = thisSong.getAuthor();
            paint.getTextBounds(string,0,string.length(),bounds);
            height = height + bounds.height();
            canvas.drawText(string,margin,ypos,paint);
            ypos = ypos + paintSize;
        }

        // If the copyright isn't blank
        if (thisSong.getCopyright()!=null && !thisSong.getCopyright().isEmpty()) {
            string = thisSong.getCopyright();
            if (!string.toLowerCase().contains(c.getString(R.string.copyright).toLowerCase())) {
                string = c.getString(R.string.copyright) + " " + string;
            }
            paint.getTextBounds(string,0,string.length(),bounds);
            height = height + bounds.height();
            canvas.drawText(string,margin,ypos,paint);
            ypos = ypos + paintSize;
        }
        paint.setStrokeWidth(2);
        paint.setColor(Color.BLACK);
        canvas.drawLine(margin,ypos,docWidth-margin,ypos,paint);
        headerHeight = height + 22;
    }

    private void writeTheFooter(Canvas canvas, int ypos) {
        Rect bounds = new Rect();
        paint.setTextSize(10);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setColor(Color.DKGRAY);
        String string = "Prepared by OpenSongApp (https://www.opensongapp.com)";
        paint.getTextBounds(string,0,string.length(),bounds);
        footerHeight = bounds.height();
        canvas.drawText(string,margin,(ypos-10),paint);
    }
    private void writePDFContent(Context c, MainActivityInterface mainActivityInterface, Song thisSong, Canvas canvas, int ypos, float scaling) {
        // Now go though the lyrics
        int height = 0;
        int width = 0;
        float scaledPaintSize;
        float lineSpacing = (9+1)*scaling;
        paint.setTypeface(Typeface.MONOSPACE);
        Rect bounds = new Rect();
        String[] lines = thisSong.getLyrics().split("\n");
        for (String line:lines) {
            // Set the defaults;
            scaledPaintSize = 9*scaling;
            paint.setTextSize(scaledPaintSize);
            paint.setColor(lyricColor);
            paint.setUnderlineText(false);

            if (line.startsWith(".")) {
                // Chord line
                paint.setColor(chordColor);
                line = line.replaceFirst("."," ");

            } else if (line.startsWith("[") || line.startsWith(" [")) {
                // Heading line
                scaledPaintSize = 8*scaling;
                paint.setTextSize(scaledPaintSize);
                paint.setUnderlineText(true);
                paint.setColor(lyricColor);
                line = mainActivityInterface.getProcessSong().beautifyHeading(c,mainActivityInterface,line);

            } else if (line.startsWith(";")) {
                // Comment or tab line
                line = line.replaceFirst(";"," ");

            } else {
                // If none of the above, it's a lyrics line
                line = line.replace("_","-");
                line = line.replace("|"," ");
            }

            paint.getTextBounds(line,0,line.length(),bounds);
            height = height + bounds.height();
            width = Math.max(width,bounds.width());
            canvas.drawText(line,margin,ypos,paint);
            ypos = ypos + (int)(lineSpacing+0.6f);   // Add on 0.6 to ensure it is always rounded up
        }
        lyricwidth = width;
        lyricheight = height;
        lyricheight = ypos;

    }

    private float getScaling() {
        int availWidth = (docWidth-(3*margin));
        int availHeight = (docHeight-(2*margin)-headerHeight-footerHeight);
        float xscale = (float)availWidth/(float)lyricwidth;
        float yscale = (float)availHeight/(float)lyricheight;
        Log.d("d","canvas size: "+lyricwidth+"x"+lyricheight);
        Log.d("d","available size: "+availWidth+"x"+availHeight);
        Log.d("d","scale values: "+xscale+"x"+yscale);
        float scaleVal = Math.min(xscale,yscale);
        // Round down to 1 decimal places
        scaleVal = (float)Math.floor(scaleVal*10);
        scaleVal = scaleVal/10.0f;
        Log.d("d","scale value: "+scaleVal);
        if (scaleVal>2.0f) {
            // This is the max scaling, otherwise bigger than the title!
            scaleVal = 2.0f;
        }
        return scaleVal;
    }

}
