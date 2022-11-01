package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.widget.TextViewCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class SongProjectionInfo extends LinearLayoutCompat {

    private final LinearLayout castSongInfo;
    private final TextView songTitle, songAuthor, songCopyright, songCCLI, capoIcon;
    private final TextClock textClock;
    private final ImageView miniLogo;
    private int viewHeight = 0;
    private boolean smallText, isDisplaying=false;
    private float clockTextSize;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SongProjectionInfo";

    public SongProjectionInfo(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_song_info, this);

        castSongInfo = findViewById(R.id.castSongInfo);
        LinearLayout contentLayout = findViewById(R.id.contentLayout);
        songTitle = findViewById(R.id.songTitle);
        songAuthor = findViewById(R.id.songAuthor);
        songCopyright = findViewById(R.id.songCopyright);
        songCCLI = findViewById(R.id.songCCLI);
        miniLogo = findViewById(R.id.miniLogo);
        textClock = findViewById(R.id.textClock);
        capoIcon = findViewById(R.id.capoInfo);
        castSongInfo.setId(View.generateViewId());
        contentLayout.setId(View.generateViewId());
        songTitle.setId(View.generateViewId());
        songAuthor.setId(View.generateViewId());
        songCopyright.setId(View.generateViewId());
        songCCLI.setId(View.generateViewId());
        miniLogo.setId(View.generateViewId());
        textClock.setId(View.generateViewId());
        capoIcon.setId(View.generateViewId());
    }


    // Adjust the layout depending on what is needed
    public void setupLayout(Context c, MainActivityInterface mainActivityInterface, boolean miniInfo) {
        // Set up the text info bar fonts
        setupFonts(mainActivityInterface);

        // Set the background color, logo and alignment based on mode
        if (miniInfo) {
            // We just want the text
            showMiniLogo(false);
            smallText(mainActivityInterface, true);
            castSongInfo.setBackgroundColor(Color.TRANSPARENT);
        } else if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_presenter))) {
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
    public void setCapo(String capo) {
        if (capo==null) {
            capoIcon.setText("");
            capoIcon.setVisibility(View.GONE);
        } else {
            capoIcon.setText(capo);
            capoIcon.setVisibility(View.VISIBLE);
        }
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
    public void setupFonts(MainActivityInterface mainActivityInterface) {
        songTitle.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
        songAuthor.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
        songCopyright.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
        songCCLI.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
        textClock.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());

        capoIcon.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
        ColorStateList colorList = ColorStateList.valueOf(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
        TextViewCompat.setCompoundDrawableTintList(capoIcon, colorList);

        songTitle.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
        songAuthor.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
        songCopyright.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
        songCCLI.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
        textClock.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
    }

    public String getTextViewString(TextView textView) {
        if (textView.getText()!=null) {
            return textView.getText().toString();
        } else {
            return "";
        }
    }

    public void setNullValues() {
        songTitle.setText(null);
        songAuthor.setText(null);
        songCopyright.setText(null);
        songCCLI.setText(null);
    }

    public boolean getValuesNonNull() {
        return viewIsSet(songTitle) && viewIsSet(songAuthor) && viewIsSet(songCopyright) && viewIsSet(songCCLI);
    }

    private boolean viewIsSet(TextView textView) {
        return textView.getText()!=null || textView.getVisibility()==View.GONE;
    }

    public void setAlign(int align) {
        songTitle.setGravity(align);
        songAuthor.setGravity(align);
        songCopyright.setGravity(align);
        songCCLI.setGravity(align);
        textClock.setGravity(align);
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

    public void setIsDisplaying(boolean isDisplaying) {
        this.isDisplaying = isDisplaying;
    }
    public boolean getIsDisplaying() {
        return isDisplaying;
    }

    public void setViewHeight(int viewHeight) {
        if (viewHeight!=0) {
            this.viewHeight = viewHeight;
        }
    }

    private void smallText(MainActivityInterface mainActivityInterface, boolean smallText) {
        this.smallText = smallText;
        capoIcon.setTextSize(22f);
        if (smallText) {
            songTitle.setTextSize(14f);
            songAuthor.setTextSize(12f);
            songCopyright.setTextSize(12f);
            songCCLI.setTextSize(12f);
            clockTextSize = 12f;
        } else {
            songTitle.setTextSize(mainActivityInterface.getPresenterSettings().getPresoTitleTextSize());
            songAuthor.setTextSize(mainActivityInterface.getPresenterSettings().getPresoAuthorTextSize());
            songCopyright.setTextSize(mainActivityInterface.getPresenterSettings().getPresoCopyrightTextSize());
            songCCLI.setTextSize(mainActivityInterface.getPresenterSettings().getPresoCopyrightTextSize());
            clockTextSize = mainActivityInterface.getPresenterSettings().getPresoClockSize();
        }

        // Now update the clock settings
        updateClockSettings(mainActivityInterface);
    }

    public void updateClockSettings(MainActivityInterface mainActivityInterface) {
        mainActivityInterface.getTimeTools().setFormat(textClock,clockTextSize,
                mainActivityInterface.getPresenterSettings().getPresoShowClock(),
                mainActivityInterface.getPresenterSettings().getPresoClock24h(),
                mainActivityInterface.getPresenterSettings().getPresoClockSeconds());
    }

    public boolean isNewInfo(String compareString) {
        String currentString = getTextViewString(songTitle) + getTextViewString(songAuthor) +
                getTextViewString(songCopyright) + getTextViewString(songCCLI);

        if (compareString!=null) {
            return !compareString.equals(currentString);
        } else {
            return true;
        }
    }
}
