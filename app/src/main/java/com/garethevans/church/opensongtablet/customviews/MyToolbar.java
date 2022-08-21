package com.garethevans.church.opensongtablet.customviews;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.SongDetailsBottomSheet;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textview.MaterialTextView;


public class MyToolbar extends MaterialToolbar {

    // This holds references to the items in the Toolbar (except the battery)
    // Battery changes get sent via the mainactivityInterface
    // The toolbar is set as the supportActionbar received as actionBar, so can called via that as well

    private Activity activity;
    private Context c;
    private MainActivityInterface mainActivityInterface;
    private ActionBar actionBar;
    private final RelativeLayout batteryholder;
    private final TextView title;
    private final TextView author;
    private final TextView key;
    private final TextView capo;
    private final TextClock clock;
    private final ImageView setIcon, batteryimage;
    private final ImageView webHelp;
    private final com.google.android.material.textview.MaterialTextView batterycharge;
    private Handler delayActionBarHide;
    private Runnable hideActionBarRunnable;
    private final int autoHideTime = 1200;
    private float clockTextSize;
    private boolean clock24hFormat, clockOn, hideActionBar, clockSeconds, performanceMode;
    private final String TAG = "MyToolbar";

    // Set up the view and view items
    public void initialiseToolbar(Activity activity, Context c, ActionBar actionBar) {
        this.activity = activity;
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        this.actionBar = actionBar;
        delayActionBarHide = new Handler();
        hideActionBarRunnable = () -> {
            if (actionBar.isShowing()) {
                actionBar.hide();
            }
        };
        updateActionBarPrefs();
    }
    public MyToolbar(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        View v = inflate(context, R.layout.view_toolbar, this);
        setIcon = v.findViewById(R.id.setIcon);
        title = v.findViewById(R.id.songtitle_ab);
        key = v.findViewById(R.id.songkey_ab);
        capo = v.findViewById(R.id.songcapo_ab);
        author = v.findViewById(R.id.songauthor_ab);
        batteryholder = v.findViewById(R.id.batteryholder);
        batteryimage = v.findViewById(R.id.batteryimage);
        batterycharge = v.findViewById(R.id.batterycharge);
        clock = v.findViewById(R.id.digitalclock);
        webHelp = v.findViewById(R.id.webHelp);

        batteryholder.setOnClickListener(v1 -> mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_actionbar), 0));
    }

    // Deal with the preferences used for the actionbar
    private void updateActionBarPrefs() {
        clockTextSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("clockTextSize",9.0f);
        clock24hFormat = mainActivityInterface.getPreferences().getMyPreferenceBoolean("clock24hFormat",true);
        clockOn = mainActivityInterface.getPreferences().getMyPreferenceBoolean("clockOn",true);
        clockSeconds = mainActivityInterface.getPreferences().getMyPreferenceBoolean("clockSeconds",false);
        hideActionBar = mainActivityInterface.getPreferences().getMyPreferenceBoolean("hideActionBar",false);
        updateClock();
    }
    public void updateActionBarSettings(String prefName, float value, boolean isvisible) {

        switch (prefName) {
            case "batteryDialOn":
                mainActivityInterface.getBatteryStatus().setBatteryDialOn(isvisible);
                break;
            case "batteryDialThickness":
                mainActivityInterface.getBatteryStatus().setBatteryDialThickness((int)value);
                mainActivityInterface.getBatteryStatus().setBatteryImage();
                break;
            case "batteryTextOn":
                mainActivityInterface.getBatteryStatus().setBatteryTextOn(isvisible);
                break;
            case "batteryTextSize":
                mainActivityInterface.getBatteryStatus().setBatteryTextSize(value);
                break;
            case "clockOn":
                hideView(clock,!isvisible);
                break;
            case "clock24hFormat":
                clock24hFormat = isvisible;
                updateClock();
                break;
            case "clockSeconds":
                clockSeconds = isvisible;
                updateClock();
                break;
            case "clockTextSize":
                clock.setTextSize(value);
                break;
            case "songTitleSize":
                title.setTextSize(value);
                key.setTextSize(value);
                capo.setTextSize(value);
                break;
            case "songAuthorSize":
                author.setTextSize(value);
                break;
            case "hideActionBar":
                setHideActionBar(!isvisible);
                break;
        }
    }

    // If we have chosen to autohide the actionbar
    public boolean contentBehind(boolean menuOpen) {
        return (hideActionBar && performanceMode && !menuOpen);
    }

    public void setHideActionBar(boolean hideActionBar) {
        this.hideActionBar = hideActionBar;
    }
    public boolean getHideActionBar() {
        return hideActionBar;
    }

    // Update the text in the actionbar to either be a song info, or menu title
    public void setActionBar(String newtitle) {
        // By default hide the webHelp (can be shown later)
        updateToolbarHelp(null);

        if (newtitle == null) {
            // We are in the Performance/Stage mode
            float mainsize = mainActivityInterface.getPreferences().getMyPreferenceFloat("songTitleSize",13.0f);

            // If we are in a set, show the icon
            int positionInSet = mainActivityInterface.getSetActions().indexSongInSet(mainActivityInterface.getSong());
            if (positionInSet>-1) {
                // Check the set menu fragment to see if we need to highlight
                // This happens if we just loaded a song (not clicking on the set item in the menu)
                mainActivityInterface.checkSetMenuItemHighlighted(positionInSet);
                setIcon.setVisibility(View.VISIBLE);
                mainActivityInterface.getCurrentSet().setIndexSongInSet(positionInSet);
            } else {
                setIcon.setVisibility(View.GONE);
                mainActivityInterface.getCurrentSet().setIndexSongInSet(-1);
            }

            if (title != null && mainActivityInterface.getSong().getTitle() != null) {
                title.setTextSize(mainsize);
                String text = mainActivityInterface.getSong().getTitle();
                if (mainActivityInterface.getSong().getFolder().startsWith("*")) {
                    text = "*" + text;
                }
                title.setText(text);
            }
            if (author != null && mainActivityInterface.getSong().getAuthor() != null &&
                    !mainActivityInterface.getSong().getAuthor().isEmpty()) {
                author.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat("songAuthorSize",11.0f));
                author.setText(mainActivityInterface.getSong().getAuthor());
                hideView(author, false);
            } else {
                hideView(author, true);
            }
            if (key != null && mainActivityInterface.getSong().getKey() != null &&
                    !mainActivityInterface.getSong().getKey().isEmpty()) {
                String k = " (" + mainActivityInterface.getSong().getKey() + ")";
                key.setTextSize(mainsize);
                capo.setTextSize(mainsize);
                key.setText(k);
                hideView(key, false);
            } else {
                hideView(key, true);
            }
            if (title!=null) {
                title.setOnClickListener(v -> openDetails());
                title.setOnLongClickListener(view -> {
                    editSong();
                    return true;
                });
            }
            if (author!=null) {
                author.setOnClickListener(v -> openDetails());
                author.setOnLongClickListener(view -> {
                    editSong();
                    return true;
                });
            }
            if (key!=null) {
                key.setOnClickListener(v -> openDetails());
                key.setOnLongClickListener(view -> {
                    editSong();
                    return true;
                });
            }

        } else {
            // We are in a different fragment, so hide the song info stuff
            setIcon.setVisibility(View.GONE);
            if (title != null) {
                title.setTextSize(18.0f);
                title.setText(newtitle);
                hideView(author, true);
                hideView(key, true);
            }
        }
    }

    // Update the web help icon in the toolbar
    public void updateToolbarHelp(String webAddress) {
        // This allows a help button to be shown in the action bar
        // This links to the specific page in the user manul (if webAddress isn't null)
        if (webAddress==null || webAddress.isEmpty()) {
            webHelp.setVisibility(View.GONE);
        } else {
            webHelp.setVisibility(View.VISIBLE);
            webHelp.setOnClickListener(v->mainActivityInterface.openDocument(webAddress));
            // For the first run, show the showcase as well
            mainActivityInterface.getShowCase().singleShowCase(activity, webHelp,null,
                    c.getString(R.string.help),false,"webHelp");
        }
    }

    // Clicking on the song title/author/etc. opens up the song details bottom sheet
    private void openDetails() {
        if (!mainActivityInterface.getSong().getTitle().equals("Welcome to OpenSongApp")) {
            SongDetailsBottomSheet songDetailsBottomSheet = new SongDetailsBottomSheet();
            songDetailsBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "songDetailsBottomSheet");
        }
    }

    // Allow editing the song by long pressing on the title/author/etc. if not null
    private void editSong() {
        if (!mainActivityInterface.getSong().getTitle().equals("Welcome to OpenSongApp")) {
            mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_edit), 0);
        }
    }

    // Set the capo string
    public void setActionBarCapo(TextView capo, String string) {
        capo.setText(string);
    }

    // This is used to show/hide various parts of the toolbar text (author, copyright, etc)
    private void hideView(View v, boolean hide) {
        if (hide) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }

    // Set when entering/exiting performance mode as this is used to determine if we can autohide actionbar
    public void setPerformanceMode(boolean inPerformanceMode) {
        performanceMode = inPerformanceMode;
    }

    // Show/hide the actionbar
    public void showActionBar(boolean menuOpen) {
        // Remove any existing callbacks to hide the actionbar
        try {
            delayActionBarHide.removeCallbacks(hideActionBarRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Now show the action bar
        actionBar.show();

        // If we need to hide the actionbar again, set a runnable, as long as the menu isn't open
        if (hideActionBar && performanceMode && !menuOpen) {
            try {
                delayActionBarHide.postDelayed(hideActionBarRunnable, autoHideTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeCallBacks() {
        delayActionBarHide.removeCallbacks(hideActionBarRunnable);
    }

    // Flash on/off for metronome
    public void doFlash(int colorBar) {
        setBackground(new ColorDrawable(colorBar));
        //toolbar.setBackgroundDrawable(new ColorDrawable(colorBar));
        //actionBar.setBackgroundDrawable(new ColorDrawable(colorBar));
    }

    // Get the actionbar height - fakes a height of 0 if autohiding
    public int getActionBarHeight(boolean menuOpen) {
        if (hideActionBar && performanceMode && !menuOpen) {
            return 0;
        } else {
            if (getHeight()==0) {
                // Just in case there was a call before drawn
                return 168;
            } else {
                return getHeight();
            }
        }
    }

    public void updateClock() {
        mainActivityInterface.getTimeTools().setFormat(clock,clockTextSize,
                clockOn, clock24hFormat, clockSeconds);
    }

    public void batteryholderVisibility(int visibility) {
        batteryholder.setVisibility(visibility);
    }

    public ImageView getBatteryimage() {
        return batteryimage;
    }

    public MaterialTextView getBatterycharge() {
        return batterycharge;
    }

    public void showClock(boolean show) {
        if (show && clockOn) {
            // Only show if that is our preference
            clock.setVisibility(View.VISIBLE);
        } else {
            clock.setVisibility(View.GONE);
        }
    }

    @Override
    public void setNavigationOnClickListener(OnClickListener listener) {
        super.setNavigationOnClickListener(listener);
    }

    @Override
    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        super.setOnMenuItemClickListener(listener);
    }
}
