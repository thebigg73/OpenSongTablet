package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class SongSheetHeaders {

    private final Context c;
    @SuppressWarnings("canbelocal")
    private final String TAG = "SongSheetHeaders";
    private final MainActivityInterface mainActivityInterface;
    private LinearLayout linearLayout;
    private String songSheetHTML = "";
    private boolean forExport;

    public SongSheetHeaders(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }

    public LinearLayout getSongSheet(Song thisSong, float commentScaling,  int textColor) {
        // Rather than assuming it is the current song, we get passed the song current or otherwise
        // This allows on the fly processing of other songs not processed (e.g. as part of a set)
        // IV - Uses SongSelect tricks of bold, simple separators and field names - to enhance readability
        linearLayout = new LinearLayout(c);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        Typeface typeface = mainActivityInterface.getMyFonts().getLyricFont();

        // This will generate a separate LinearLayout containing the songsheet info
        String title = thisSong.getTitle();
        String author = thisSong.getAuthor();
        String copyright = thisSong.getCopyright();

        float defFontSize;
        if (forExport) {
            defFontSize = mainActivityInterface.getProcessSong().getDefFontSize();
        } else {
            defFontSize = 12f;
        }

        songSheetHTML = "";

        if (title!=null && !title.isEmpty()) {
            TextView textView = getSongSheetTexts(title,typeface,textColor,defFontSize);
            textView.setPaintFlags(textView.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            textView.setTypeface(textView.getTypeface(),Typeface.BOLD);
            linearLayout.addView(textView);
        }
        if (author!=null && !author.isEmpty()) {
            linearLayout.addView(getSongSheetTexts(author,typeface,textColor,defFontSize*commentScaling));
            songSheetHTML += author+ "<br>\n";
        }
        if (copyright!=null && !copyright.isEmpty()) {
            if (!copyright.contains("©") && !copyright.contains(c.getString(R.string.copyright))) {
                copyright = "© "+copyright;
            }
            linearLayout.addView(getSongSheetTexts(copyright,typeface,textColor,defFontSize*commentScaling));
            songSheetHTML += copyright + "<br>\n";
        }

        String keyCapoTempo = getKeyCapoTempo(thisSong);

        if (!keyCapoTempo.isEmpty()) {
            TextView textView = getSongSheetTexts(keyCapoTempo.trim(),typeface,textColor,defFontSize*commentScaling);
            textView.setPaintFlags(textView.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            textView.setTypeface(textView.getTypeface(),Typeface.BOLD);
            linearLayout.addView(textView);
            songSheetHTML += keyCapoTempo + "<br>\n";
        }

        // Add a section space to the bottom of the songSheet
        if (linearLayout.getChildCount()>0) {
            linearLayout.addView(getSongSheetTexts("",typeface,textColor,defFontSize*commentScaling*0.5f));
        }

        if (linearLayout!=null && linearLayout.getChildCount()==0) {
            linearLayout = null;
        }

        return linearLayout;
    }

    public TextView getSongSheetTexts(String content, Typeface typeface, int color, float size) {
        TextView textView = new TextView(c);
        textView.setTypeface(typeface);
        textView.setTextColor(color);
        textView.setTextSize(size);
        textView.setText(content);
        return textView;
    }

    public String getKeyCapoTempo(Song thisSong) {
        String key = thisSong.getKey();
        String capo = thisSong.getCapo();
        String tempo = thisSong.getTempo();
        String timesig = thisSong.getTimesig();

        String keyCapoTempo = "";

        if (capo!=null && !capo.isEmpty()) {
            keyCapoTempo += "| " + c.getString(R.string.capo) + " - " + capo + " ";
            keyCapoTempo += ("(" + mainActivityInterface.getTranspose().capoKeyTranspose(thisSong) + ") ").replace(" ()","");
        }

        if (key!=null && !key.isEmpty()) {
            keyCapoTempo += "| " + c.getString(R.string.key) + " - " + key + " ";
        }

        if (tempo!=null && !tempo.isEmpty()) {
            keyCapoTempo += "| " + c.getString(R.string.tempo) + " - " + tempo + " ";
        }

        if (timesig!=null && !timesig.isEmpty()) {
            keyCapoTempo += "| " + c.getString(R.string.time) + " - " + timesig + " ";
        }

        keyCapoTempo = keyCapoTempo.trim().replaceFirst("\\| ","");

        return keyCapoTempo;
    }

    public void setForExport(boolean forExport) {
        this.forExport = forExport;
    }

    public String getSongSheetTitleMainHTML(Song thisSong) {
        // This will generate a separate LinearLayout containing the songsheet info
        String title = thisSong.getTitle();

        if (title!=null && !title.isEmpty()) {
            return "<div class=\"titlemain\">" + title + "</div>\n";
        } else {
            return "";
        }
    }
    public String getSongSheetTitleExtrasHTML(Song thisSong) {
        // This will generate a separate LinearLayout containing the songsheet info
        String author = thisSong.getAuthor();
        String copyright = thisSong.getCopyright();
        String keyCapoTempo = getKeyCapoTempo(thisSong);
        String extras = "";
        if (author!=null && !author.isEmpty()) {
            extras += "<div class=\"titleextras\">" + author + "</div>\n";
        }
        if (copyright!=null && !copyright.isEmpty()) {
            extras += "<div class=\"titleextras\">" + copyright + "</div>\n";
        }
        if (!keyCapoTempo.isEmpty()) {
            extras += "<div class=\"titleextras\">" + keyCapoTempo + "</div>\n";
        }

        return extras;
    }
}
