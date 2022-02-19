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

    private final LinearLayout castSongInfo;
    private final TextView songTitle, songAuthor, songCopyright, songCCLI;
    private final ImageView miniLogo;
    private final String TAG = "SongProjectionInfo";
    private int viewHeight = 0;

    public SongProjectionInfo(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_song_info, this);

        castSongInfo = findViewById(R.id.castSongInfo);
        LinearLayout contentLayout = findViewById(R.id.contentLayout);
        songTitle = findViewById(R.id.songTitle);
        songAuthor = findViewById(R.id.songAuthor);
        songCopyright = findViewById(R.id.songCopyright);
        songCCLI = findViewById(R.id.songCCLI);
        miniLogo = findViewById(R.id.miniLogo);

        castSongInfo.setId(View.generateViewId());
        contentLayout.setId(View.generateViewId());
        songTitle.setId(View.generateViewId());
        songAuthor.setId(View.generateViewId());
        songCopyright.setId(View.generateViewId());
        songCCLI.setId(View.generateViewId());
        miniLogo.setId(View.generateViewId());
    }

    // Adjust the layout depending on what is needed
    public void setupLayout(MainActivityInterface mainActivityInterface, boolean miniInfo) {
        // Set up the text info bar fonts
        setupFonts(mainActivityInterface);

        // Set the background color, logo and alignment based on mode
        if (miniInfo) {
            // We just want the text
            showMiniLogo(false);
            smallText(mainActivityInterface, true);
            castSongInfo.setBackgroundColor(Color.TRANSPARENT);
        } else if (mainActivityInterface.getMode().equals("Presenter")) {
            // The bottom bar for the secondary display in Presenter mode
            showMiniLogo(false);
            smallText(mainActivityInterface, false);
            castSongInfo.setBackgroundColor(mainActivityInterface.getMyThemeColors().getPresoShadowColor());
        } else {
            // Performance / Stage mode
            showMiniLogo(true);
            smallText(mainActivityInterface, true);
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
    public void setSongCCLI(String ccli) {
        setText(songCCLI,ccli);
    }
    private void setText(TextView textView, String text) {
        if (text==null || text.isEmpty()) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
        }
        textView.setText(text);
    }


    private void showMiniLogo(boolean show) {
        if (show) {
            miniLogo.setVisibility(View.VISIBLE);
        } else {
            miniLogo.setVisibility(View.GONE);
        }
    }
    private void setupFonts(MainActivityInterface mainActivityInterface) {
        songTitle.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
        songAuthor.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
        songCopyright.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
        songCCLI.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());

        songTitle.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
        songAuthor.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
        songCopyright.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
        songCCLI.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
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
        songCCLI.setGravity(align);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        viewHeight = yNew;
    }

    public int getViewHeight() {
        if (viewHeight==0) {
            viewHeight = getMeasuredHeight();
        }
        return viewHeight;
    }

    public void setViewHeight(int viewHeight) {
        Log.d(TAG,"set height to "+viewHeight);
        if (viewHeight!=0) {
            this.viewHeight = viewHeight;
        }
    }

    private void smallText(MainActivityInterface mainActivityInterface, boolean smallText) {
        if (smallText) {
            songTitle.setTextSize(14f);
            songAuthor.setTextSize(12f);
            songCopyright.setTextSize(12f);
            songCCLI.setTextSize(12f);
        } else {
            songTitle.setTextSize(mainActivityInterface.getPresenterSettings().getPresoTitleTextSize());
            songAuthor.setTextSize(mainActivityInterface.getPresenterSettings().getPresoAuthorTextSize());
            songCopyright.setTextSize(mainActivityInterface.getPresenterSettings().getPresoCopyrightTextSize());
            songCCLI.setTextSize(mainActivityInterface.getPresenterSettings().getPresoCopyrightTextSize());
        }
    }
}
