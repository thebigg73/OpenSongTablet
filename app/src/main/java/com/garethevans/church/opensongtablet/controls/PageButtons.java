package com.garethevans.church.opensongtablet.controls;

// This is used to set up the correct page button icons based on what the user wants them to be
// It supports the main page buttons on the song window, but also the edit buttons fragment

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.autoscroll.AutoscrollBottomSheet;
import com.garethevans.church.opensongtablet.chords.ChordFingeringBottomSheet;
import com.garethevans.church.opensongtablet.chords.TransposeBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.ActionInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.pads.PadsBottomSheet;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.songsandsetsmenu.RandomSongBottomSheet;
import com.garethevans.church.opensongtablet.tools.SoundLevelBottomSheet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class PageButtons {

    private final String TAG = "PageButtons";

    // For the actions
    private final ActionInterface actionInterface;

    // Everything available for the buttons
    private ArrayList<String> actions, text, shortText, longText;
    private ArrayList<Integer> drawableIds;
    private int pageButtonColor;
    private float pageButtonAlpha;
    private int pageButtonIconColor;

    // My buttons in the main activity
    private FloatingActionButton actionButton;
    private LinearLayout pageButtonsLayout;
    private ArrayList<FloatingActionButton> fabs;

    // My chosen buttons in the edit fragment
    private final int pageButtonNum = 6;
    private ArrayList<String> pageButtonAction, pageButtonText, pageButtonShortText, pageButtonLongText;
    private ArrayList<Drawable> pageButtonDrawable;
    private ArrayList<Boolean> pageButtonVisibility;

    public PageButtons(Context c, Preferences preferences) {
        // Set up the return interface for sending instructions back to the main activity
        actionInterface = (ActionInterface) c;
        // Prepare the arrays of available actions with matching short and long text
        prepareAvailableActions();
        prepareAvailableButtonText(c);
        prepareShortActionText(c);
        prepareLongActionText(c);
        prepareDrawableIds();

        // Now get our button preferences
        setPreferences(c,preferences);
    }

    public void setMainFABS(MainActivityInterface mainActivityInterface,
                            FloatingActionButton actionButton, FloatingActionButton custom1,
                       FloatingActionButton custom2, FloatingActionButton custom3,
                       FloatingActionButton custom4, FloatingActionButton custom5,
                       FloatingActionButton custom6, LinearLayout pageButtonsLayout) {
        this.actionButton = actionButton;
        fabs = new ArrayList<>();
        updateColors(mainActivityInterface);
        custom1.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        custom2.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        custom3.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        custom4.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        custom5.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        custom6.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        Drawable drawable1 = custom1.getDrawable();
        DrawableCompat.setTint(drawable1, pageButtonIconColor);
        custom1.setImageDrawable(drawable1);
        fabs.add(custom1);
        fabs.add(custom2);
        fabs.add(custom3);
        fabs.add(custom4);
        fabs.add(custom5);
        fabs.add(custom6);
        pageButtonsLayout.setAlpha(pageButtonAlpha);
        this.pageButtonsLayout = pageButtonsLayout;
    }

    public void updateColors(MainActivityInterface mainActivityInterface) {
        pageButtonColor = mainActivityInterface.getMyThemeColors().getPageButtonsSplitColor();
        pageButtonAlpha = mainActivityInterface.getMyThemeColors().getPageButtonsSplitAlpha();
        pageButtonIconColor = mainActivityInterface.getMyThemeColors().getExtraInfoTextColor();
    }

    public FloatingActionButton getFAB(int x) {
        return fabs.get(x);
    }
    private final OvershootInterpolator interpolator = new OvershootInterpolator(1.0f);

    public void animatePageButton(Context c, boolean open) {
        if (open) {
            ViewCompat.animate(actionButton).rotation(45f).withLayer().setDuration(500).
                    setInterpolator(interpolator).start();
            actionButton.setBackgroundTintList(ColorStateList.valueOf(c.getResources().getColor(R.color.red)));
        } else {
            ViewCompat.animate(actionButton).rotation(0f).withLayer().setDuration(500).
                    setInterpolator(interpolator).start();
            actionButton.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        }
        Log.d(TAG,"pageButtonAlpha="+pageButtonAlpha);
        actionButton.setAlpha(pageButtonAlpha);
        for (int x=0; x<6; x++) {
            if (pageButtonVisibility.get(x)) {
                getFAB(x).setVisibility(View.VISIBLE);
            } else {
                getFAB(x).setVisibility(View.GONE);
            }
        }
        animateView(pageButtonsLayout,open);
    }

    private void animateView(View view, boolean animateIn) {
        float alpha = 0f;
        int translationBy = 500;
        Runnable endRunnable = hideView(view, animateIn);
        Runnable startRunnable = hideView(view, animateIn);

        if (animateIn) {
            translationBy = -500;
            alpha = pageButtonAlpha;
            endRunnable = () -> {
                view.setAlpha(pageButtonAlpha);
            };
        } else {
            startRunnable = () -> {
                view.setAlpha(pageButtonAlpha);
            };
        }
        ViewCompat.animate(view).alpha(alpha).translationYBy(translationBy).setDuration(500).
                setInterpolator(interpolator).withStartAction(startRunnable).withEndAction(endRunnable).start();
    }
    private Runnable hideView(View view,boolean show) {
        return () -> {
            if (show) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        };
    }

    // This stuff below is mostly for the edit fragment

    // Everything available for the buttons
    private void prepareAvailableActions() {
        // Build the arrays with the available actions, short text and long text descriptions
        // These are the options available for the exposed dropdowns in the PageButtonsFragment
        actions = new ArrayList<>();
        actions.add("");
        actions.add("set");
        actions.add("transpose");
        actions.add("pad");
        actions.add("metronome");
        actions.add("autoscroll");
        actions.add("link");
        actions.add("chordfingerings");
        actions.add("stickynotes");
        actions.add("pdfpage");
        actions.add("highlight");
        actions.add("editsong");
        actions.add("theme");
        actions.add("autoscale");
        actions.add("fonts");
        actions.add("profiles");
        actions.add("gestures");
        actions.add("pedals");
        actions.add("showchords");
        actions.add("showcapo");
        actions.add("showlyrics");
        actions.add("search");
        actions.add("randomsong");
        actions.add("abc");
        actions.add("inc_autoscroll_speed");
        actions.add("dec_autoscroll_speed");
        actions.add("toggle_autoscroll_pause");
        actions.add("midi");
        actions.add("bible");
        actions.add("soundlevel");
        actions.add("exit");
    }
    private void prepareAvailableButtonText(Context c) {
        text = new ArrayList<>();
        text.add("");
        text.add(c.getString(R.string.set_current));
        text.add(c.getString(R.string.transpose));
        text.add(c.getString(R.string.pad));
        text.add(c.getString(R.string.metronome));
        text.add(c.getString(R.string.autoscroll));
        text.add(c.getString(R.string.link));
        text.add(c.getString(R.string.chord_fingering));
        text.add(c.getString(R.string.song_notes));
        text.add(c.getString(R.string.select_page));
        text.add(c.getString(R.string.highlight));
        text.add(c.getString(R.string.edit) + " " + c.getString(R.string.song));
        text.add(c.getString(R.string.theme_choose));
        text.add(c.getString(R.string.autoscale));
        text.add(c.getString(R.string.font_choose));
        text.add(c.getString(R.string.profile));
        text.add(c.getString(R.string.custom_gestures));
        text.add(c.getString(R.string.pedal));
        text.add(c.getString(R.string.show_chords));
        text.add(c.getString(R.string.show_capo));
        text.add(c.getString(R.string.show_lyrics));
        text.add(c.getString(R.string.show_songs));
        text.add(c.getString(R.string.random_song));
        text.add(c.getString(R.string.music_score));
        text.add(c.getString(R.string.inc_autoscroll_speed));
        text.add(c.getString(R.string.dec_autoscroll_speed));
        text.add(c.getString(R.string.autoscroll_pause));
        text.add(c.getString(R.string.midi));
        text.add(c.getString(R.string.bible_verse));
        // TODO string resource
        text.add("Sound meter");
        text.add(c.getString(R.string.exit));
    }
    private void prepareShortActionText(Context c) {
        shortText = new ArrayList<>();
        shortText.add("");
        shortText.add(c.getString(R.string.show));
        shortText.add(c.getString(R.string.open));
        shortText.add(c.getString(R.string.start) + " / " + c.getString(R.string.stop));
        shortText.add(c.getString(R.string.start) + " / " + c.getString(R.string.stop));
        shortText.add(c.getString(R.string.start) + " / " + c.getString(R.string.stop));
        shortText.add(c.getString(R.string.open));
        shortText.add(c.getString(R.string.show) + " / " + c.getString(R.string.hide));
        shortText.add(c.getString(R.string.show) + " / " + c.getString(R.string.hide));
        shortText.add(c.getString(R.string.select));
        shortText.add(c.getString(R.string.show) + " / " + c.getString(R.string.hide));
        shortText.add(c.getString(R.string.open));
        shortText.add(c.getString(R.string.select));
        shortText.add(c.getString(R.string.scale_style));
        shortText.add(c.getString(R.string.select));
        shortText.add(c.getString(R.string.settings));
        shortText.add(c.getString(R.string.settings));
        shortText.add(c.getString(R.string.settings));
        shortText.add(c.getString(R.string.show) + " / " + c.getString(R.string.hide));
        shortText.add(c.getString(R.string.show) + " / " + c.getString(R.string.hide));
        shortText.add(c.getString(R.string.show) + " / " + c.getString(R.string.hide));
        shortText.add(c.getString(R.string.open) + " / " + c.getString(R.string.close));
        shortText.add(c.getString(R.string.random_song));
        shortText.add(c.getString(R.string.show) + " / " + c.getString(R.string.hide));
        shortText.add(c.getString(R.string.inc_autoscroll_speed));
        shortText.add(c.getString(R.string.dec_autoscroll_speed));
        shortText.add(c.getString(R.string.pause) + " / " + c.getString(R.string.resume));
        shortText.add(c.getString(R.string.settings));
        shortText.add(c.getString(R.string.search));
        shortText.add(c.getString(R.string.show) + " / " + c.getString(R.string.hide));
        shortText.add(c.getString(R.string.exit) + " " + c.getString(R.string.app_name));
    }
    private void prepareLongActionText(Context c) {
        longText = new ArrayList<>();
        longText.add("");
        longText.add("");
        longText.add(c.getString(R.string.settings));
        longText.add(c.getString(R.string.settings));
        longText.add(c.getString(R.string.settings));
        longText.add(c.getString(R.string.settings));
        longText.add("");
        longText.add(c.getString(R.string.edit));
        longText.add(c.getString(R.string.edit));
        longText.add("");
        longText.add(c.getString(R.string.edit));
        longText.add("");
        longText.add("");
        longText.add("");
        longText.add("");
        longText.add("");
        longText.add("");
        longText.add("");
        longText.add("");
        longText.add("");
        longText.add("");
        longText.add("");
        longText.add(c.getString(R.string.settings));
        longText.add(c.getString(R.string.edit));
        longText.add("");
        longText.add("");
        longText.add("");
        longText.add("");
        longText.add("");
        longText.add("");
        longText.add("");
    }
    private void prepareDrawableIds() {
        drawableIds = new ArrayList<>();
        drawableIds.add(R.drawable.ic_help_outline_white_36dp);
        drawableIds.add(R.drawable.ic_format_list_numbers_white_36dp);
        drawableIds.add(R.drawable.ic_transpose_white_36dp);
        drawableIds.add(R.drawable.ic_amplifier_white_36dp);
        drawableIds.add(R.drawable.ic_pulse_white_36dp);
        drawableIds.add(R.drawable.ic_rotate_right_white_36dp);
        drawableIds.add(R.drawable.ic_link_white_36dp);
        drawableIds.add(R.drawable.ic_guitar_electric_white_36dp);
        drawableIds.add(R.drawable.ic_note_text_white_36dp);
        drawableIds.add(R.drawable.ic_book_white_36dp);
        drawableIds.add(R.drawable.ic_highlighter_white_36dp);
        drawableIds.add(R.drawable.ic_table_edit_white_36dp);
        drawableIds.add(R.drawable.ic_theme_light_dark_white_36dp);
        drawableIds.add(R.drawable.ic_arrow_expand_white_36dp);
        drawableIds.add(R.drawable.ic_format_text_white_36dp);
        drawableIds.add(R.drawable.ic_account_white_36dp);
        drawableIds.add(R.drawable.ic_fingerprint_white_36dp);
        drawableIds.add(R.drawable.ic_pedal_white_36dp);
        drawableIds.add(R.drawable.ic_guitar_electric_white_36dp);
        drawableIds.add(R.drawable.ic_capo_white_36dp);
        drawableIds.add(R.drawable.ic_voice_white_36dp);
        drawableIds.add(R.drawable.ic_magnify_white_36dp);
        drawableIds.add(R.drawable.ic_shuffle_white_36dp);
        drawableIds.add(R.drawable.ic_clef_white_36dp);
        drawableIds.add(R.drawable.ic_autoscroll_plus_white_36dp);
        drawableIds.add(R.drawable.ic_autoscroll_minus_white_36dp);
        drawableIds.add(R.drawable.ic_autoscroll_pause_white_36dp);
        drawableIds.add(R.drawable.ic_midi_white_36dp);
        drawableIds.add(R.drawable.ic_bible_white_36dp);
        drawableIds.add(R.drawable.ic_volume_high_white_36dp);
        drawableIds.add(R.drawable.ic_exit_to_app_white_36dp);
    }

    // Decide which button we want to grab
    public int getButtonInArray(String action) {
        return actions.indexOf(action);
    }

    // Build the arrays describing the buttons (action, short description, long description, drawable, etc) based on user preferences
    private void setPreferences(Context c, Preferences preferences) {
        // Initialise the arrays
        pageButtonAction = new ArrayList<>();
        pageButtonDrawable = new ArrayList<>();
        pageButtonText = new ArrayList<>();
        pageButtonShortText = new ArrayList<>();
        pageButtonLongText = new ArrayList<>();
        pageButtonVisibility = new ArrayList<>();

        // Go through each button and build references to the actions, drawables, etc.
        for (int x=0;x<pageButtonNum;x++) {
            // If the preference isn't set, use the default.  Button 1 and 2 are set as follows
            String fallback = "";
            if (x==0) {
                fallback = "set";
            } else if (x==1) {
                fallback = "transpose";
            } else if (x==2) {
                fallback = "editsong";
            } else if (x==3) {
                fallback = "autoscroll";
            } else if (x==4) {
                fallback = "metronome";
            }
            String action = preferences.getMyPreferenceString(c,"pageButton"+(x+1),fallback);
            Log.d(TAG, "x: "+x+"  action:"+action);
            pageButtonAction.add(action);
            int pos = getButtonInArray(action);
            if (pos>=0) {
                pageButtonText.add(text.get(pos));
                pageButtonShortText.add(shortText.get(pos));
                pageButtonLongText.add(longText.get(pos));
                pageButtonDrawable.add(ResourcesCompat.getDrawable(c.getResources(),drawableIds.get(pos),null));
            } else {
                pageButtonText.add("");
                pageButtonShortText.add("");
                pageButtonLongText.add("");
                pageButtonDrawable.add(ResourcesCompat.getDrawable(c.getResources(),drawableIds.get(0),null));
            }

            // Set the visibility
            pageButtonVisibility.add(preferences.getMyPreferenceBoolean(c,"pageButtonShow"+(x+1),true));
        }
    }

    // This will redesign the button for the page
    public void setPageButton(Context c, FloatingActionButton fab, int buttonNum, boolean editing) {
        // The alpha is set on the linear layout, not the individual buttons
        pageButtonsLayout.setAlpha(pageButtonAlpha);
        fab.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        Drawable drawable = fab.getDrawable();
        DrawableCompat.setTint(drawable, pageButtonIconColor);
        fab.setImageDrawable(drawable);

        if (buttonNum>=0 && buttonNum<=pageButtonNum) {
            Drawable buttonDrawable = pageButtonDrawable.get(buttonNum);
            buttonDrawable.mutate();
            DrawableCompat.setTint(buttonDrawable, pageButtonIconColor);
            fab.setImageDrawable(buttonDrawable);
            fab.setTag(pageButtonAction.get(buttonNum));
            if (pageButtonVisibility.get(buttonNum)) {
                fab.setVisibility(View.VISIBLE);
            } else {
                fab.setVisibility(View.GONE);
            }
            if (!editing) {
                fab.setOnClickListener(v -> {
                    int pos = actions.indexOf(pageButtonAction.get(buttonNum));
                    sendPageAction(pos,false);
                });
                fab.setOnLongClickListener(v -> {
                    int pos = actions.indexOf(pageButtonAction.get(buttonNum));
                    sendPageAction(pos,true);
                    return true;
                });
            } else {
                fab.setOnClickListener(null);
            }
        } else if (buttonNum>=0) {
            fab.setImageDrawable(ResourcesCompat.getDrawable(c.getResources(),drawableIds.get(0),null));
            fab.setTag("");
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(null);
        }
    }

    // These are the getters and setters for the PageButtonFragment
    public int getPositionFromText(String selectedtext) {
        return text.indexOf(selectedtext);
    }
    public String getPageButtonAction(int buttonNum) {
        return pageButtonAction.get(buttonNum);
    }
    public String getPageButtonText(int buttonNum) {
        return pageButtonText.get(buttonNum);
    }
    public String getPageButtonShortText(int buttonNum) {
        return pageButtonShortText.get(buttonNum);
    }
    public String getPageButtonLongText(int buttonNum) {
        return pageButtonLongText.get(buttonNum);
    }
    public boolean getPageButtonVisibility(int buttonNum) {
        return pageButtonVisibility.get(buttonNum);
    }
    public ArrayList<String> getPageButtonAvailableText() {
        return text;
    }

    public void setPageButtonAction(int button, int pos) {
        pageButtonAction.set(button,actions.get(pos));
    }
    public void setPageButtonText(int button, int pos) {
        pageButtonText.set(button,text.get(pos));
    }
    public void setPageButtonShortText(int button, int pos) {
        pageButtonShortText.set(button,shortText.get(pos));
    }
    public void setPageButtonLongText(int button, int pos) {
        pageButtonLongText.set(button,longText.get(pos));
    }
    public void setPageButtonDrawable(Context c, int button, int pos) {
        pageButtonDrawable.set(button,ResourcesCompat.getDrawable(c.getResources(),drawableIds.get(pos),null));
    }
    public void setPageButtonVisibility(int button, boolean visible) {
        pageButtonVisibility.set(button,visible);
    }

    // This deals with the actions from the page buttons
    public void sendPageAction(int x, boolean isLongPress) {
        Log.d(TAG,"x="+x+"  isLongPress="+isLongPress);
        // Get the action we are trying to run
        switch(actions.get(x)) {
            case "":
                actionInterface.navigateToFragment("opensongapp://settings/controls/pagebuttons",0);
                break;
            case "set":
                Log.d(TAG, "Show set");
                actionInterface.chooseMenu(true);
                break;
            case "transpose":
                TransposeBottomSheet transposeBottomSheet = new TransposeBottomSheet();
                transposeBottomSheet.show(actionInterface.getMyFragmentManager(),"TransposeBottomSheet");
                break;
            case "pad":
                if (isLongPress) {
                    PadsBottomSheet padsBottomSheet = new PadsBottomSheet();
                    padsBottomSheet.show(actionInterface.getMyFragmentManager(),"padsBottomSheet");
                } else {
                    actionInterface.playPad();
                }
                break;
            case "metronome":
                if (isLongPress) {
                    actionInterface.navigateToFragment("opensongapp://settings/actions/metronome",0);
                } else {
                    actionInterface.metronomeToggle();
                }
                break;
            case "autoscroll":
                if (isLongPress) {
                    AutoscrollBottomSheet autoscrollBottomSheet = new AutoscrollBottomSheet();
                    autoscrollBottomSheet.show(actionInterface.getMyFragmentManager(),"AutoscrollBottomSheet");
                } else {
                    if (actionInterface.getAutoscroll().getIsAutoscrolling()) {
                        actionInterface.getAutoscroll().stopAutoscroll();
                    } else {
                        actionInterface.getAutoscroll().startAutoscroll();
                    }
                }
                break;
            case "link":
                actionInterface.navigateToFragment("opensongapp://settings/actions/links",0);
                break;
            case "chordfingerings":
                ChordFingeringBottomSheet chordFingeringBottomSheet = new ChordFingeringBottomSheet();
                chordFingeringBottomSheet.show(actionInterface.getMyFragmentManager(),"ChordFingeringBottomSheet");
                break;
            case "stickynotes":
                Log.d(TAG,"Stickynotes");
                if (isLongPress) {
                    actionInterface.navigateToFragment("opensongapp://settings/actions/stickynotes",0);
                } else {
                    // Toggle the force show (hide is for moving away from performace/stage mode)
                    actionInterface.showSticky(true,false);
                }
                break;
            case "pdfpage":
                //TODO
                break;
            case "highlight":
                //TODO
                if (isLongPress) {
                    actionInterface.navigateToFragment("opensongapp://songactions/highlighter/edit",0);
                } else {

                }
                break;
            case "editsong":
                actionInterface.navigateToFragment("opensongapp://settings/edit",0);
                break;
            case "theme":
                actionInterface.navigateToFragment("opensongapp://settings/display/theme",0);
                break;
            case "autoscale":
                //TODO
                break;
            case "fonts":
                actionInterface.navigateToFragment("opensongapp://settings/display/fonts",0);
                break;
            case "profiles":
                actionInterface.navigateToFragment("opensongapp://settings/profiles",0);
                break;
            case "gestures":
                //TODO
                break;
            case "pedals":
                actionInterface.navigateToFragment("opensongapp://settings/controls/pedals",0);
                break;
            case "showchords":
                //TODO
                break;
            case "showcapo":
                //TODO
                break;
            case "showlyrics":
                //TODO
                break;
            case "search":
                actionInterface.chooseMenu(false);
                break;
            case "randomsong":
                String whichMenu;
                if (actionInterface.getCurrentSet().getInSet(actionInterface.getSong())) {
                    whichMenu = "set";
                } else {
                    whichMenu = "song";
                }
                RandomSongBottomSheet randomSongBottomSheet = new RandomSongBottomSheet(whichMenu);
                randomSongBottomSheet.show(actionInterface.getMyFragmentManager(),"RandomSongBottomSheet");
                break;
            case "abc":
                //TODO
                break;
            case "inc_autoscroll_speed":
                actionInterface.getAutoscroll().speedUpAutoscroll();
                break;
            case "dec_autoscroll_speed":
                actionInterface.getAutoscroll().slowDownAutoscroll();
                break;
            case "toggle_autoscroll_pause":
                actionInterface.getAutoscroll().pauseAutoscroll();
                break;
            case "midi":
                actionInterface.navigateToFragment("opensongapp://settings/midi",0);
                break;
            case "bible":
                //TODO
                break;
            case "soundlevel":
                SoundLevelBottomSheet soundLevelBottomSheet = new SoundLevelBottomSheet();
                soundLevelBottomSheet.show(actionInterface.getMyFragmentManager(),"SoundLevelBottomSheet");
                break;
            case "exit":
                actionInterface.onBackPressed();
                break;
        }
    }

}
