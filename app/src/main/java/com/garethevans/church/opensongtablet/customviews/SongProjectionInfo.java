package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.graphics.Color;
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
    private final LinearLayout contentLayout;
    private final LinearLayout castSongInfo;
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

    // Adjust the layout depending on what is needed
    public void setupLayout(Context c, MainActivityInterface mainActivityInterface, boolean miniInfo) {
        // Set up the text info bar fonts
        setupFonts(c, mainActivityInterface);

        // Set the background color, logo and alignment based on mode
        if (miniInfo) {
            // We just want the text
            showMiniLogo(false);
            castSongInfo.setBackgroundColor(Color.TRANSPARENT);
        } else if (mainActivityInterface.getMode().equals("Presenter")) {
            // The bottom bar for the secondary display in Presenter mode
            showMiniLogo(false);
            castSongInfo.setBackgroundColor(mainActivityInterface.getMyThemeColors().getPresoShadowColor());
        } else {
            // Performance / Stage mode
            showMiniLogo(true);
            castSongInfo.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    // Updating the text in the view
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
        Log.d(TAG,"text:"+text+"  view:"+textView);
    }


    private void showMiniLogo(boolean show) {
        if (show) {
            miniLogo.setVisibility(View.VISIBLE);
        } else {
            miniLogo.setVisibility(View.GONE);
        }
    }
    private void setupFonts(Context c, MainActivityInterface mainActivityInterface) {
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

}
