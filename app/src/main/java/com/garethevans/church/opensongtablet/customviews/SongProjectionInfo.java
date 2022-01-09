package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class SongProjectionInfo extends LinearLayout {

    private final Context c;
    private final LinearLayout castSongInfo, contentLayout;
    private final TextView songTitle, songAuthor, songCopyright;
    private final ImageView miniLogo;
    private final String TAG = "SongProjectionInfo";

    public SongProjectionInfo(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        c = context;
        inflate(context, R.layout.view_song_info, this);

        castSongInfo = findViewById(R.id.castSongInfo);
        contentLayout = findViewById(R.id.contentLayout);
        songTitle = findViewById(R.id.songTitle);
        songAuthor = findViewById(R.id.songAuthor);
        songCopyright = findViewById(R.id.songCopyright);
        miniLogo = findViewById(R.id.miniLogo);

        castSongInfo.setId(View.generateViewId());
        contentLayout.setId(View.generateViewId());
        songTitle.setId(View.generateViewId());
        songAuthor.setId(View.generateViewId());
        songCopyright.setId(View.generateViewId());
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
    private void setText(TextView textView, String text) {
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

        songTitle.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoTitleTextSize", 14f));
        songAuthor.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoAuthorTextSize", 12f));
        songCopyright.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoCopyrightTextSize", 12f));

        songTitle.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
        songAuthor.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
        songCopyright.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
    }

    public String getSongTitle() {
        if (songTitle.getText()!=null) {
            return songTitle.getText().toString();
        } else {
            return "";
        }
    }

    public void setAlign(int align) {
        songTitle.setGravity(align);
        songAuthor.setGravity(align);
        songCopyright.setGravity(align);
    }

    // Used in presenter mode to display song info on device
    public void minifyLayout(boolean miniLogoOn) {
        showMiniLogo(miniLogoOn);
        songTitle.setTextSize(18);
        songAuthor.setTextSize(16);
        songCopyright.setTextSize(16);
        songAuthor.setTextColor(c.getResources().getColor(R.color.vlightgrey));
        songCopyright.setTextColor(c.getResources().getColor(R.color.vlightgrey));
        if (miniLogoOn) {
            contentLayout.setPadding(8,8,8,8);
        } else {
            contentLayout.setPadding(0,0,0,0);
        }
    }

    // Decide if the full info is still required in presenter mode
    public void setInfoBarRequired(boolean infoBarRequired, boolean alertRequired) {
        if (infoBarRequired) {
            if (songTitle.getText()!=null && !songTitle.getText().toString().isEmpty()) {
                songTitle.setVisibility(View.VISIBLE);
            } else {
                songTitle.setVisibility(View.GONE);
            }
            if (songAuthor.getText()!=null && !songAuthor.getText().toString().isEmpty()) {
                songAuthor.setVisibility(View.VISIBLE);
            } else {
                songAuthor.setVisibility(View.GONE);
            }
            if (songCopyright.getText()!=null && !songCopyright.getText().toString().isEmpty()) {
                songCopyright.setVisibility(View.VISIBLE);
            } else {
                songCopyright.setVisibility(View.GONE);
            }
        } else {
            songTitle.setVisibility(View.GONE);
            songAuthor.setVisibility(View.GONE);
            songCopyright.setVisibility(View.GONE);
        }

    }
}
