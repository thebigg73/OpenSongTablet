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

import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.sqlite.SQLite;

import java.io.OutputStream;


public class MakePDF {

    int margin;
    int paintSize;
    int headerHeight;
    int footerHeight;
    int docWidth  = 595;  // Based on 1/72 of an inch  595/72 * 2.54 = 21.0cm
    int docHeight = 842;  // Based on 1/72 of an inch  842/72 * 2.54 = 29.7cm
    int lyricheight;
    int lyricwidth;
    int chordColor;
    int lyricColor;
    int dpi;
    Paint paint;

    public Uri createPDF(Context c, Preferences preferences, StorageAccess storageAccess, ProcessSong processSong, SQLite thisSongSQL) {
        String newFilename = thisSongSQL.getFolder().replace("/","_");
        if (!newFilename.endsWith("_")) {
            newFilename = newFilename + "_";
        }
        newFilename = newFilename + thisSongSQL.getFilename() + ".pdf";
        Uri uri = storageAccess.getUriForItem(c,preferences,"Export","",newFilename);
        storageAccess.lollipopCreateFileForOutputStream(c,preferences,uri, "application/pdf","Export","",newFilename);

        // Set the paint values
        setPaintDefaults(c,preferences);

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

        writeTheHeader(thisSongSQL,chead,0);
        Log.d("d","headerHeight="+headerHeight);
        // Wipe it for now
        clearPage(page);

        // Test write the footer to the page to get its height
        writeTheFooter(cfoot,0);
        Log.d("d","footerHeight="+footerHeight);
        // Wipe it for now
        clearPage(page);

        // Test write the footer to the page to get its height
        writePDFContent(c,processSong,thisSongSQL,clyrics,0,1.0f);
        Log.d("d","lyricWidth="+lyricwidth);
        Log.d("d","lyricHeight="+lyricheight);
        // Wipe it for now
        clearPage(page);

        // Determine the maxscaling for the lyrics part
        float scaling = getScaling();

        // Add the header back in
        writeTheHeader(thisSongSQL,canvas,margin);
        // Add the lyrics back in
        writePDFContent(c,processSong,thisSongSQL,canvas,headerHeight+margin,scaling);
        // Add the footer back in
        writeTheFooter(canvas,(docHeight-margin));

        // write the PDF document
        pdfDocument.finishPage(page);
        saveThePDF(c,storageAccess,uri,pdfDocument);

        return uri;
    }

    private void clearPage(PdfDocument.Page page) {
        Canvas canvas = page.getCanvas();
        Paint clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRect(0, 0, 0, 0, clearPaint);
    }

    private void setPaintDefaults(Context c, Preferences preferences) {
        paint = new Paint();
        paintSize = 16;
        margin = 54;

        paint.setColor(Color.BLACK);
        paint.setTextSize(paintSize);
        chordColor = preferences.getMyPreferenceInt(c,"custom2_lyricsChordsColor",0xff0000aa);
        lyricColor = preferences.getMyPreferenceInt(c, "custom2_lyricsTextColor", 0xff000000);
    }
    private void saveThePDF(Context c, StorageAccess storageAccess, Uri uri, PdfDocument pdfDocument) {
        OutputStream outputStream = storageAccess.getOutputStream(c, uri);
        try {
            pdfDocument.writeTo(outputStream);
            pdfDocument.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        pdfDocument.close();
    }

    private void writeTheHeader(SQLite thisSongSQL, Canvas canvas, int ypos) {
        int height = 0;
        Rect bounds = new Rect();
        paintSize = 20;
        paint.setTextSize(paintSize);
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        String string = thisSongSQL.getTitle();
        // Add the title and the key (if it isn't blank)
        if (thisSongSQL.getKey()!=null && !thisSongSQL.getKey().isEmpty()) {
            string = string + " (" + thisSongSQL.getKey() + ")";
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
        if (thisSongSQL.getAuthor()!=null && !thisSongSQL.getAuthor().isEmpty()) {
            string = thisSongSQL.getAuthor();
            paint.getTextBounds(string,0,string.length(),bounds);
            height = height + bounds.height();
            canvas.drawText(string,margin,ypos,paint);
            ypos = ypos + paintSize;
        }

        // If the copyright isn't blank
        if (thisSongSQL.getCopyright()!=null && !thisSongSQL.getCopyright().isEmpty()) {
            string = thisSongSQL.getCopyright();
            paint.getTextBounds(string,0,string.length(),bounds);
            height = height + bounds.height();
            canvas.drawText(string,margin,ypos,paint);
            ypos = ypos + paintSize;
        }
        paint.setStrokeWidth(2);
        paint.setColor(0xff0000aa);
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
    private void writePDFContent(Context c, ProcessSong processSong, SQLite thisSongSQL, Canvas canvas, int ypos, float scaling) {
        // Now go though the lyrics
        int height = 0;
        int width = 0;
        float scaledPaintSize;
        float lineSpacing = (9+1)*scaling;
        paint.setTypeface(Typeface.MONOSPACE);
        Rect bounds = new Rect();
        String[] lines = thisSongSQL.getLyrics().split("\n");
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
                line = processSong.fixHeading(c,line);

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
