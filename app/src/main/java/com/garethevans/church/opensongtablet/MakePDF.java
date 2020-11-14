package com.garethevans.church.opensongtablet;

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

import java.io.OutputStream;

public class MakePDF {

    int margin;
    int paintSize;
    int headerHeight;
    int footerHeight;
    int docWidth  = 595;  // Based on 1/72 of an inch  595/72 * 2.54 = 21.0cm
    int docHeight = 842;  // Based on 1/72 of an inch  842/72 * 2.54 = 29.7cm
    float headerScaling;
    float footerScaling;
    float lyricScaling;
    int chordColor;
    int lyricColor;
    int dpi;
    Paint paint;

    public Uri createPDF(Context c, Preferences preferences, StorageAccess storageAccess, ProcessSong processSong, SQLite thisSong) {
        String newFilename = thisSong.getFolder().replace("/","_");
        if (!newFilename.endsWith("_")) {
            newFilename = newFilename + "_";
        }
        newFilename = newFilename + thisSong.getFilename() + ".pdf";
        Uri uri = storageAccess.getUriForItem(c,preferences,"Export","",newFilename);
        storageAccess.lollipopCreateFileForOutputStream(c,preferences,uri, "application/pdf","Export","",newFilename);

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

        writeTheHeader(c,thisSong,chead,0,1.0f);
        // Wipe it for now
        clearPage(page);

        // Test write the footer to the page to get its height
        writeTheFooter(thisSong,cfoot,0,1.0f);
        // Wipe it for now
        clearPage(page);

        // Test write content to the page to get its height (uses the header and footer heights from test writes)
        writePDFContent(c,processSong,thisSong,clyrics,0,1.0f);
        // Wipe it for now
        clearPage(page);

        // Scaling values are updated by the test writes

        // Add the header back in
        writeTheHeader(c,thisSong,canvas,margin,headerScaling);
        // Add the lyrics back in
        writePDFContent(c,processSong,thisSong,canvas,headerHeight+margin,lyricScaling);
        // Add the footer back in
        writeTheFooter(thisSong,canvas,(docHeight-margin),footerScaling);

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

    private void setPaintDefaults() {
        paint = new Paint();
        margin = 54;
        paint.setColor(Color.BLACK);
        // Keep it black and white for now
        chordColor = Color.BLACK;
        lyricColor = Color.BLACK;
        // IV - Needed to get text to space properly in footer!
        paint.setSubpixelText(true);
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

    private void writeTheHeader(Context c, SQLite thisSong, Canvas canvas, int ypos, float scaling) {
        int height;
        int width = 0;
        Rect bounds = new Rect();
        paintSize = (int) (20 * scaling);
        paint.setTextSize(paintSize);
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        String string = thisSong.getTitle();
        paint.getTextBounds(string,0,string.length(),bounds);
        height = bounds.height();
        width = Math.max(width,bounds.width());
        canvas.drawText(string,margin,ypos,paint);
        ypos = ypos + paintSize;

        // Get ready for the subheadings
        paintSize = (int) (12 * scaling);
        paint.setTextSize(paintSize);
        paint.setTypeface(Typeface.DEFAULT);
        // If the author isn't blank
        if (thisSong.getAuthor()!=null && !thisSong.getAuthor().isEmpty()) {
            string = thisSong.getAuthor();
            paint.getTextBounds(string,0,string.length(),bounds);
            height = height + bounds.height();
            width = Math.max(width,bounds.width());
            // -ve ypos tweak to line position
            ypos = ypos - 4;
            canvas.drawText(string,margin,ypos,paint);
            ypos = ypos + paintSize;
        }

        // If the key/tempo/timesig isn't blank
        String extra = "";
        if (thisSong.getKey()!=null && !thisSong.getKey().isEmpty()) {
            extra += c.getResources().getString(R.string.edit_song_key) + ": " + thisSong.getKey();
        }
        if (thisSong.getTempo()!=null && !thisSong.getTempo().isEmpty()) {
            if (!extra.equals("")) extra += " | ";
            extra += c.getResources().getString(R.string.edit_song_tempo) + ": " + thisSong.getTempo();
        }
        if (thisSong.getTimesig()!=null && !thisSong.getTimesig().isEmpty()) {
            if (!extra.equals("")) extra += " | ";
            extra += c.getResources().getString(R.string.edit_song_timesig) + ": " + thisSong.getTimesig();
        }

        if (!extra.isEmpty()) {
            paint.getTextBounds(extra,0,extra.length(),bounds);
            height = height + bounds.height();
            width = Math.max(width,bounds.width());
            ypos = ypos + 2; // Position tweak
            canvas.drawText(extra,margin,ypos,paint);
            ypos = ypos + paintSize;
        }

        paint.setStrokeWidth(1);
        paint.setColor(Color.BLACK);
        ypos = ypos - 4; // Position tweak
        canvas.drawLine(margin,ypos,docWidth-margin,ypos,paint);
        headerHeight = height + 22;
        headerScaling = getScaling(width, 0);
    }

    private void writeTheFooter(SQLite thisSong, Canvas canvas, int ypos, float scaling) {
        int height = 0;
        int width = 0;
        paint.setTypeface(Typeface.DEFAULT);
        ypos = ypos - 10;

        // Copyright in footer if isn't blank
        if (thisSong.getCopyright() != null && !thisSong.getCopyright().isEmpty()) {
            paint.setTextSize(8 * scaling);
            Rect bounds = new Rect();
            String string = thisSong.getCopyright();
            // Use copyright character - we want short text (no localisation needed)
            if (!string.contains("©")) {
                string = "© " + string;
            }
            paint.getTextBounds(string, 0, string.length(), bounds);
            height = bounds.height() + 4;
            width = Math.max(width,bounds.width());
            ypos = ypos - 12;
            canvas.drawText(string, margin, ypos, paint);
            ypos = ypos + 12;
        }

        Rect bounds = new Rect();
        paint.setTextSize(10 * scaling);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setColor(0xFFAAAAAA);
        String string = "Prepared by OpenSongApp (https://www.opensongapp.com)";
        paint.getTextBounds(string,0,string.length(),bounds);
        height = height + bounds.height();
        width = Math.max(width,bounds.width());
        canvas.drawText(string,margin,ypos,paint);
        footerHeight = height + 16;
        footerScaling = getScaling(width, 0);
    }

    private void writePDFContent(Context c, ProcessSong processSong, SQLite thisSong, Canvas canvas, int ypos, float scaling) {
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
                line = processSong.beautifyHeadings(line,c)[0];

            } else if (line.startsWith(";")) {
                // Comment or tab line
                line = line.replaceFirst(";"," ");

            } else {
                // If none of the above, it's a lyrics line
                line = line.replace("_"," ");
                line = line.replace("|"," ");
            }

            paint.getTextBounds(line,0,line.length(),bounds);
            height = ypos + bounds.height();
            width = Math.max(width,bounds.width());
            canvas.drawText(line,margin,ypos,paint);
            ypos = ypos + (int)(lineSpacing+0.6f);   // Add on 0.6 to ensure it is always rounded up
        }
        // Make sure void exits with underline off
        paint.setUnderlineText(false);

        lyricScaling = getScaling(width, height);
    }

    private float getScaling(float width, float height) {
        float scaleVal;
        int availWidth = (docWidth-(3*margin));
        float xscale = (float)availWidth/ width;
        if (height == 0) {
            // For header and footer, consider width. With a max scale of 1.0f, so never larger.
            scaleVal = Math.min(xscale, 1.0f);
        } else {
            int availHeight = (docHeight-(2*margin)-headerHeight-footerHeight);
            float yscale = (float) availHeight / height;
            // For lyric, consider width and height. With a max scale of 1.7f otherwise monospace is bigger than the title!
            scaleVal = Math.min(Math.min(xscale,yscale),1.7f);
        }
        // Round down to 1 decimal place
        scaleVal = (float)Math.floor(scaleVal*10);
        scaleVal = scaleVal/10.0f;
        return scaleVal;
    }
}