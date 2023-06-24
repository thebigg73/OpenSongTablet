package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
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

    private final LinearLayout castSongInfo, contentLayout;
    private final TextView songTitle, songAuthor, songCopyright, songCCLI, capoIcon;
    private final TextClock textClock;
    private final ImageView miniLogo;
    private int viewHeight = 0;
    private boolean smallText, isDisplaying=false, presenterPrimaryScreen;
    private float clockTextSize;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SongProjectionInfo";
    private final String performance_string;

    public SongProjectionInfo(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_song_info, this);

        castSongInfo = findViewById(R.id.castSongInfo);
        contentLayout = findViewById(R.id.contentLayout);
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

        performance_string = context.getString(R.string.mode_performance);
    }


    // Adjust the layout depending on what is needed
    public void setPresenterPrimaryScreen(Context context, MainActivityInterface mainActivityInterface, boolean presenterPrimaryScreen) {
        // Used for the PresenterMode device screen - stick to Lato!
        this.presenterPrimaryScreen = presenterPrimaryScreen;
        // Run the updates
        setupLayout(context,mainActivityInterface,true);
        textClock.setVisibility(View.GONE);
    }
    public void setupLayout(Context c, MainActivityInterface mainActivityInterface, boolean miniInfo) {
        // Set the background color, logo and alignment based on mode
        if (miniInfo || presenterPrimaryScreen) {
            // We just want the text
            showMiniLogo(false);
            smallText(mainActivityInterface, true);
            castSongInfo.setBackgroundColor(Color.TRANSPARENT);
        } else if (!mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))) {
            // The bottom bar for the secondary display in Stage and Presenter mode
            showMiniLogo(false);
            smallText(mainActivityInterface, false);
            castSongInfo.setBackgroundColor(mainActivityInterface.getMyThemeColors().getPresoShadowColor());
        } else {
            // Performance
            showMiniLogo(true);
            smallText(mainActivityInterface, true);
            castSongInfo.setBackgroundColor(Color.TRANSPARENT);
        }
        // Set up the text info bar fonts
        setupFonts(mainActivityInterface);
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
        if (capo==null || capo.isEmpty()) {
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
        Log.d(TAG,"showMiniLogo("+show+")");
        if (show) {
            miniLogo.setVisibility(View.VISIBLE);
        } else {
            miniLogo.setVisibility(View.GONE);
        }
    }
    public void setupFonts(MainActivityInterface mainActivityInterface) {
        if (!presenterPrimaryScreen) {
            songTitle.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
            songAuthor.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
            songCopyright.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
            songCCLI.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
            textClock.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());

            ColorStateList colorList;
            int color;
            if (mainActivityInterface.getMode().equals(performance_string)) {
                color = mainActivityInterface.getMyThemeColors().getLyricsTextColor();
            } else {
                color = mainActivityInterface.getMyThemeColors().getPresoInfoFontColor();
            }
            Log.d(TAG,"compare color: black:"+Color.BLACK+"  white:"+Color.WHITE+"  color:"+color);
            colorList = ColorStateList.valueOf(color);
            capoIcon.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
            TextViewCompat.setCompoundDrawableTintList(capoIcon, colorList);

            songTitle.setTextColor(color);
            songAuthor.setTextColor(color);
            songCopyright.setTextColor(color);
            songCCLI.setTextColor(color);
            textClock.setTextColor(color);

            songTitle.setTextSize(mainActivityInterface.getPresenterSettings().getPresoTitleTextSize());
            songAuthor.setTextSize(mainActivityInterface.getPresenterSettings().getPresoAuthorTextSize());
            songCopyright.setTextSize(mainActivityInterface.getPresenterSettings().getPresoCopyrightTextSize());
            songCCLI.setTextSize(mainActivityInterface.getPresenterSettings().getPresoCopyrightTextSize());
            textClock.setTextSize(mainActivityInterface.getPresenterSettings().getPresoClockSize());

            // IV - Ensure Standard line wrap for extra details
            songAuthor.setMaxLines(Integer.MAX_VALUE);
            songAuthor.setEllipsize(null);
            songCopyright.setMaxLines(Integer.MAX_VALUE);
            songCopyright.setEllipsize(null);
            songCCLI.setMaxLines(Integer.MAX_VALUE);
            songCCLI.setEllipsize(null);
        } else {
            Typeface typeface = mainActivityInterface.getMyFonts().getAppDefault();
            songTitle.setTypeface(typeface);
            songAuthor.setTypeface(typeface);
            songCopyright.setTypeface(typeface);
            songCCLI.setTypeface(typeface);
            textClock.setTypeface(typeface);

            // IV - A dialog uses white text
            int color = android.R.color.white;
            songTitle.setTextColor(getResources().getColor(color));
            songAuthor.setTextColor(getResources().getColor(color));
            songCopyright.setTextColor(getResources().getColor(color));
            songCCLI.setTextColor(getResources().getColor(color));

            songTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
            songAuthor.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            songCopyright.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            songCCLI.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            castSongInfo.setBackgroundColor(Color.TRANSPARENT);
            contentLayout.setPadding(0,0,0,0);
            miniLogo.setVisibility(View.GONE);
            capoIcon.setVisibility(View.GONE);
            textClock.setVisibility(View.GONE);

            // IV - Restrict extra detail to 1 line
            songAuthor.setLines(1);
            songAuthor.setEllipsize(TextUtils.TruncateAt.END);
            songCopyright.setLines(1);
            songCopyright.setEllipsize(TextUtils.TruncateAt.END);
        }
        updateClockSettings(mainActivityInterface);
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
        if (!presenterPrimaryScreen) {
            mainActivityInterface.getTimeTools().setFormat(textClock,
                    false, clockTextSize,
                    mainActivityInterface.getPresenterSettings().getPresoShowClock(),
                    mainActivityInterface.getPresenterSettings().getPresoClock24h(),
                    mainActivityInterface.getPresenterSettings().getPresoClockSeconds());
            // Change the visibility based on the preference (settings open is used for the toolbar clock)
            Log.d(TAG, "updateClockSettings() wanted:" + mainActivityInterface.getPresenterSettings().getPresoShowClock());
            textClock.postDelayed(() ->
                    textClock.setVisibility(mainActivityInterface.getPresenterSettings().getPresoShowClock() ? View.VISIBLE : View.GONE), 500);
        } else {
            textClock.postDelayed(() ->
                    textClock.setVisibility(View.GONE), 500);
        }
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

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        castSongInfo.setPadding(left,top,right,bottom);
    }
}
