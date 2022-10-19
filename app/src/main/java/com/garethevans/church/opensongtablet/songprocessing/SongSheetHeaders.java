package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class SongSheetHeaders {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;

    public SongSheetHeaders(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }

    public LinearLayout getSongSheet(Song thisSong, float commentScaling, boolean forPDF) {

        // Rather than assuming it is the current song, we get passed the song current or otherwise
        // This allows on the fly processing of other songs not processed (e.g. as part of a set)

        LinearLayout linearLayout = null;

        if (forPDF || (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                mainActivityInterface.getPreferences().getMyPreferenceBoolean("songSheet",false))) {

            linearLayout = new LinearLayout(c);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            Typeface typeface = mainActivityInterface.getMyFonts().getLyricFont();

            // This will generate a separate LinearLayout containing the songsheet info
            int textColor;
            if (forPDF) {
                textColor = Color.BLACK;
            } else {
                textColor = mainActivityInterface.getMyThemeColors().getLyricsTextColor();
            }
            String title = thisSong.getTitle();
            String author = thisSong.getAuthor();
            String copyright = thisSong.getCopyright();

            float defFontSize = 12.0f;

            if (title!=null && !title.isEmpty()) {
                linearLayout.addView(getSongSheetTexts(title,typeface,textColor,defFontSize));
            }
            if (author!=null && !author.isEmpty()) {
                linearLayout.addView(getSongSheetTexts(author,typeface,textColor,defFontSize*commentScaling));
            }
            if (copyright!=null && !copyright.isEmpty()) {
                if (!copyright.contains("©") && !copyright.contains(c.getString(R.string.copyright))) {
                    copyright = "©"+copyright;
                }
                linearLayout.addView(getSongSheetTexts(copyright,typeface,textColor,defFontSize*commentScaling));
            }

            String keyCapoTempo = getKeyCapoTempo(thisSong);

            if (!keyCapoTempo.isEmpty()) {
                linearLayout.addView(getSongSheetTexts(keyCapoTempo.trim(),typeface,textColor,defFontSize*commentScaling));
            }

            // Add a section space to the bottom of the songSheet
            if (linearLayout.getChildCount()>0) {
                linearLayout.addView(getSongSheetTexts("",typeface,textColor,defFontSize*commentScaling*0.5f));
            }
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
            keyCapoTempo += c.getString(R.string.capo) + ": " + capo + " ";
            if (key!=null && !key.isEmpty()) {
                keyCapoTempo += "(" + mainActivityInterface.getTranspose().getKeyBeforeCapo(Integer.parseInt(capo), key) + ") |";
            }
        } else if (key!=null && !key.isEmpty()) {
            keyCapoTempo += "| " + c.getString(R.string.key) + ": " + key + " |";
        }

        if (tempo!=null && !tempo.isEmpty()) {
            keyCapoTempo += "| " + c.getString(R.string.tempo) + ": " + tempo + "bpm |";
        }

        if (timesig!=null && !timesig.isEmpty()) {
            keyCapoTempo += "| " + c.getString(R.string.time_signature) + ": " + timesig + " |";
        }

        // Remove double || and the ones at the start and the end
        keyCapoTempo = keyCapoTempo.replace("||","|");
        if (keyCapoTempo.startsWith("|")) {
            keyCapoTempo = keyCapoTempo.replaceFirst("\\|","");
        }
        if (keyCapoTempo.endsWith("|")) {
            keyCapoTempo = keyCapoTempo.substring(0,keyCapoTempo.lastIndexOf("|"));
        }
        return keyCapoTempo.trim();
    }
}
