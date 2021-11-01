package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.textview.MaterialTextView;

public class SongProjectionInfo extends LinearLayout {

    private final Context c;
    private final LinearLayout castSongInfo, contentLayout;
    private final MaterialTextView songTitle, songAuthor, songCopyright, songAlert;
    private final ImageView miniLogo;

    public SongProjectionInfo(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        c = context;
        inflate(context, R.layout.cast_song_info, this);

        castSongInfo = findViewById(R.id.castSongInfo);
        contentLayout = findViewById(R.id.contentLayout);
        songTitle = findViewById(R.id.songTitle);
        songAuthor = findViewById(R.id.songAuthor);
        songCopyright = findViewById(R.id.songCopyright);
        songAlert = findViewById(R.id.songAlert);
        miniLogo = findViewById(R.id.miniLogo);

        castSongInfo.setId(View.generateViewId());
        contentLayout.setId(View.generateViewId());
        songTitle.setId(View.generateViewId());
        songAuthor.setId(View.generateViewId());
        songCopyright.setId(View.generateViewId());
        songAlert.setId(View.generateViewId());
        miniLogo.setId(View.generateViewId());
    }

    public void setSongTitle(String title) {
        setText(songTitle,title);
    }
    public void setSongAuthor(String author) {
        setText(songAuthor,author);
    }
    public void setSongCopyright(String copyright) {
        setText(songCopyright,copyright);
    }
    public void setSongAlert(String alert) {
        setText(songAlert,alert);
    }
    private void setText(MaterialTextView textView, String text) {
        if (text==null || text.isEmpty()) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
        }
        textView.setText(text);
    }
    public void showMiniLogo(boolean show) {
        if (show) {
            miniLogo.setVisibility(View.VISIBLE);
        } else {
            miniLogo.setVisibility(View.GONE);
        }
    }
    public void setupFonts(Context c, MainActivityInterface mainActivityInterface) {
        songTitle.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
        songAuthor.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
        songCopyright.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
        songAlert.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());

        songTitle.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoTitleTextSize", 14f));
        songAuthor.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoAuthorTextSize", 12f));
        songCopyright.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoCopyrightTextSize", 12f));
        songAlert.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoAlertTextSize", 12f));

        songTitle.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
        songAuthor.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
        songCopyright.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
        songAlert.setTextColor(mainActivityInterface.getMyThemeColors().getPresoAlertColor());
    }

    // Used in presenter mode to display song info on device
    public void minifyLayout() {
        songTitle.setTextSize(18);
        songAuthor.setTextSize(16);
        songCopyright.setTextSize(16);
        songAuthor.setTextColor(c.getResources().getColor(R.color.vlightgrey));
        songCopyright.setTextColor(c.getResources().getColor(R.color.vlightgrey));
        songAlert.setText(null);
        songAlert.setVisibility(View.GONE);
        contentLayout.setPadding(0,0,0,0);
        showMiniLogo(false);
    }
}
