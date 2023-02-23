package com.garethevans.church.opensongtablet.controls;

// This is used to set up the correct page button icons based on what the user wants them to be
// It supports the main page buttons on the song window, but also the edit buttons fragment

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.ActionInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class PageButtons {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "PageButtons";

    // For the actions
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final ActionInterface actionInterface;

    // Everything available for the buttons
    private ArrayList<String> actions, text, shortText, longText;
    private ArrayList<Integer> drawableIds;
    private int pageButtonColor;
    private float pageButtonAlpha;
    private int pageButtonIconColor;

    // My buttons in the main activity
    private LinearLayout pageButtonsLayout;
    private ArrayList<FloatingActionButton> fabs;

    // My chosen buttons in the edit fragment
    private final int pageButtonNum = 8;
    private final int animationTime = 200;
    private ArrayList<String> pageButtonAction, pageButtonText, pageButtonShortText, pageButtonLongText;
    private ArrayList<Drawable> pageButtonDrawable;
    private ArrayList<Boolean> pageButtonVisibility;
    private FloatingActionButton actionButton;

    public PageButtons(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        // Set up the return interface for sending instructions back to the main activity
        actionInterface = (ActionInterface) c;
        // Prepare the arrays of available actions with matching short and long text
        prepareAvailableActions();
        prepareAvailableButtonText();
        prepareShortActionText();
        prepareLongActionText();
        prepareDrawableIds();

        // Now get our button preferences
        setPreferences();
    }

    public void setMainFABS(FloatingActionButton actionButton, FloatingActionButton custom1,
                       FloatingActionButton custom2, FloatingActionButton custom3,
                       FloatingActionButton custom4, FloatingActionButton custom5,
                       FloatingActionButton custom6, FloatingActionButton custom7,
                            FloatingActionButton custom8, LinearLayout pageButtonsLayout) {
        this.actionButton = actionButton;
        fabs = new ArrayList<>();
        updateColors();
        custom1.hide();
        custom2.hide();
        custom3.hide();
        custom4.hide();
        custom5.hide();
        custom6.hide();
        custom7.hide();
        custom8.hide();
        custom1.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        custom2.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        custom3.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        custom4.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        custom5.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        custom6.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        custom7.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        custom8.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        fabs.add(custom1);
        fabs.add(custom2);
        fabs.add(custom3);
        fabs.add(custom4);
        fabs.add(custom5);
        fabs.add(custom6);
        fabs.add(custom7);
        fabs.add(custom8);
        pageButtonsLayout.setAlpha(pageButtonAlpha);
        this.pageButtonsLayout = pageButtonsLayout;
    }

    public void updateColors() {
        pageButtonColor = mainActivityInterface.getMyThemeColors().getPageButtonsSplitColor();
        pageButtonAlpha = mainActivityInterface.getMyThemeColors().getPageButtonsSplitAlpha();
        pageButtonIconColor = mainActivityInterface.getMyThemeColors().getExtraInfoTextColor();
        if (actionButton!=null && actionButton.getRotation()==0) {
            actionButton.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        }
        for (FloatingActionButton fab:fabs) {
            fab.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        }
        if (pageButtonsLayout!=null) {
            pageButtonsLayout.setAlpha(pageButtonAlpha);
        }
    }

    public FloatingActionButton getFAB(int x) {
        return fabs.get(x);
    }
    private final AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();

    public void animatePageButton(boolean open) {
        if (open) {
            ViewCompat.animate(actionButton).rotation(45f).withLayer().setDuration(animationTime).
                    setInterpolator(interpolator).start();
            int redAlpha = ColorUtils.setAlphaComponent(c.getResources().getColor(R.color.red), (int)(pageButtonAlpha*255));
            actionButton.setBackgroundTintList(ColorStateList.valueOf(redAlpha));
        } else {
            ViewCompat.animate(actionButton).rotation(0f).withLayer().setDuration(animationTime).
                    setInterpolator(interpolator).start();
            actionButton.setBackgroundTintList(ColorStateList.valueOf(pageButtonColor));
        }
        for (int x=0; x<pageButtonNum; x++) {
            if (pageButtonVisibility.get(x) && open) {
                getFAB(x).show();
            } else {
                getFAB(x).hide();
            }
        }
    }

    // This stuff below is mostly for the edit fragment

    // Everything available for the buttons
    private void prepareAvailableActions() {
        // Build the arrays with the available actions, short text and long text descriptions
        // These are the options available for the exposed dropdowns in the PageButtonsFragment
        actions = new ArrayList<>();
        actions.add("");
        actions.add("set");
        actions.add("inlineset");
        actions.add("transpose");
        actions.add("pad");
        actions.add("metronome");
        actions.add("autoscroll");
        actions.add("link");
        actions.add("nearby");
        actions.add("chordfingerings");
        actions.add("tuner");
        actions.add("stickynotes");
        actions.add("pdfpage");
        actions.add("highlight");
        actions.add("editsong");
        actions.add("addtoset");
        actions.add("togglescale");
        actions.add("scrolldown");
        actions.add("scrollup");
        actions.add("next");
        actions.add("previous");
        actions.add("theme");
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
        actions.add("import");
        actions.add("invertpdf");
        actions.add("exit");
    }
    private void prepareAvailableButtonText() {
        text = new ArrayList<>();
        text.add("");
        text.add(c.getString(R.string.set_current));
        text.add(c.getString(R.string.set_inline));
        text.add(c.getString(R.string.transpose));
        text.add(c.getString(R.string.pad));
        text.add(c.getString(R.string.metronome));
        text.add(c.getString(R.string.autoscroll));
        text.add(c.getString(R.string.link));
        text.add(c.getString(R.string.connections_connect));
        text.add(c.getString(R.string.chord_fingering));
        text.add("Tuner");
        text.add(c.getString(R.string.song_notes));
        text.add(c.getString(R.string.select_page));
        text.add(c.getString(R.string.highlight));
        text.add(c.getString(R.string.edit) + " " + c.getString(R.string.song));
        text.add(c.getString(R.string.add_song_to_set));
        text.add(c.getString(R.string.scale_auto));
        text.add(c.getString(R.string.scroll_down));
        text.add(c.getString(R.string.scroll_up));
        text.add(c.getString(R.string.next));
        text.add(c.getString(R.string.previous));
        text.add(c.getString(R.string.theme_choose));
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
        text.add(c.getString(R.string.sound_level_meter));
        text.add(c.getString(R.string.import_basic));
        text.add(c.getString(R.string.invert_PDF));
        text.add(c.getString(R.string.exit));
    }
    private void prepareShortActionText() {
        shortText = new ArrayList<>();
        shortText.add("");
        shortText.add(c.getString(R.string.show));
        shortText.add(c.getString(R.string.show) + " / " + c.getString(R.string.hide));
        shortText.add(c.getString(R.string.open));
        shortText.add(c.getString(R.string.start) + " / " + c.getString(R.string.stop));
        shortText.add(c.getString(R.string.start) + " / " + c.getString(R.string.stop));
        shortText.add(c.getString(R.string.start) + " / " + c.getString(R.string.stop));
        shortText.add(c.getString(R.string.open));
        shortText.add(c.getString(R.string.connections_discover));
        shortText.add(c.getString(R.string.show) + " / " + c.getString(R.string.hide));
        shortText.add(c.getString(R.string.select));
        shortText.add(c.getString(R.string.show) + " / " + c.getString(R.string.hide));
        shortText.add(c.getString(R.string.select));
        shortText.add(c.getString(R.string.show) + " / " + c.getString(R.string.hide));
        shortText.add(c.getString(R.string.open));
        shortText.add(c.getString(R.string.set_add));
        shortText.add(c.getString(R.string.scale_style));
        shortText.add(c.getString(R.string.select));
        shortText.add(c.getString(R.string.select));
        shortText.add(c.getString(R.string.select));
        shortText.add(c.getString(R.string.select));
        shortText.add(c.getString(R.string.select));
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
        shortText.add(c.getString(R.string.midi_send));
        shortText.add(c.getString(R.string.search));
        shortText.add(c.getString(R.string.show) + " / " + c.getString(R.string.hide));
        shortText.add(c.getString(R.string.online_services));
        shortText.add(c.getString(R.string.select));
        shortText.add(c.getString(R.string.exit) + " " + c.getString(R.string.app_name));
    }
    private void prepareLongActionText() {
        longText = new ArrayList<>();
        longText.add("");
        longText.add("");
        longText.add(c.getString(R.string.settings));
        longText.add(c.getString(R.string.settings));
        longText.add(c.getString(R.string.settings));
        longText.add(c.getString(R.string.settings));
        longText.add(c.getString(R.string.settings));
        longText.add("");
        longText.add(c.getString(R.string.settings));
        longText.add(c.getString(R.string.edit));
        longText.add("");
        longText.add(c.getString(R.string.edit));
        longText.add("");
        longText.add(c.getString(R.string.edit));
        longText.add("");
        longText.add(c.getString(R.string.variation_make));
        longText.add(c.getString(R.string.scaling_info));
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
        longText.add("");
        longText.add("");
        longText.add(c.getString(R.string.settings));
        longText.add(c.getString(R.string.edit));
        longText.add("");
        longText.add("");
        longText.add("");
        longText.add(c.getString(R.string.settings));
        longText.add("");
        longText.add("");
        longText.add(c.getString(R.string.import_main));
        longText.add("");
        longText.add("");
    }
    private void prepareDrawableIds() {
        drawableIds = new ArrayList<>();
        drawableIds.add(R.drawable.help);
        drawableIds.add(R.drawable.list_number);
        drawableIds.add(R.drawable.inline_set);
        drawableIds.add(R.drawable.transpose);
        drawableIds.add(R.drawable.amplifier);
        drawableIds.add(R.drawable.metronome);
        drawableIds.add(R.drawable.autoscroll);
        drawableIds.add(R.drawable.link);
        drawableIds.add(R.drawable.nearby);
        drawableIds.add(R.drawable.guitar);
        drawableIds.add(R.drawable.tuner);
        drawableIds.add(R.drawable.note_text);
        drawableIds.add(R.drawable.book);
        drawableIds.add(R.drawable.highlighter);
        drawableIds.add(R.drawable.set_edit);
        drawableIds.add(R.drawable.set_add);
        drawableIds.add(R.drawable.stretch);
        drawableIds.add(R.drawable.arrow_down);
        drawableIds.add(R.drawable.arrow_up);
        drawableIds.add(R.drawable.arrow_right);
        drawableIds.add(R.drawable.arrow_left);
        drawableIds.add(R.drawable.theme);
        drawableIds.add(R.drawable.text);
        drawableIds.add(R.drawable.account);
        drawableIds.add(R.drawable.fingerprint);
        drawableIds.add(R.drawable.pedal);
        drawableIds.add(R.drawable.guitar);
        drawableIds.add(R.drawable.capo);
        drawableIds.add(R.drawable.voice);
        drawableIds.add(R.drawable.search);
        drawableIds.add(R.drawable.shuffle);
        drawableIds.add(R.drawable.clef);
        drawableIds.add(R.drawable.timer_plus);
        drawableIds.add(R.drawable.timer_minus);
        drawableIds.add(R.drawable.timer_pause);
        drawableIds.add(R.drawable.midi);
        drawableIds.add(R.drawable.bible);
        drawableIds.add(R.drawable.sound_level);
        drawableIds.add(R.drawable.database_import);
        drawableIds.add(R.drawable.invert_colors);
        drawableIds.add(R.drawable.exit);
    }

    // Decide which button we want to grab
    public int getButtonInArray(String action) {
        return actions.indexOf(action);
    }

    // Build the arrays describing the buttons (action, short description, long description, drawable, etc) based on user preferences
    private void setPreferences() {
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
            } else if (x==5) {
                fallback = "tuner";
            }
            String action = actionInterface.getPreferences().getMyPreferenceString("pageButton"+(x+1),fallback);
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

            // Set the visibility.  By default the first 7 are visible
            if (x<7) {
                pageButtonVisibility.add(actionInterface.getPreferences().getMyPreferenceBoolean("pageButtonShow" + (x + 1), true));
            } else {
                pageButtonVisibility.add(actionInterface.getPreferences().getMyPreferenceBoolean("pageButtonShow" + (x + 1), false));
            }
        }
    }

    // This will redesign the button for the page
    public void setPageButton(FloatingActionButton fab, int buttonNum, boolean editing) {
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
            if (pageButtonVisibility.get(buttonNum) && actionButton.getRotation()!=0) {
                fab.show();
            } else if (!editing) {
                // Don't hide buttons on the pagebuttonsfragment (editing page)
                fab.hide();
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
            fab.show();
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
    // They are all sent to the PerformanceGestures class for processing
    public void sendPageAction(int x, boolean isLongPress) {
        // Get the action we are trying to run
        switch(actions.get(x)) {
            case "":
                actionInterface.getPerformanceGestures().editPageButtons();
                break;
            case "set":
                actionInterface.getPerformanceGestures().setMenu();
                break;
            case "inlineset":
                if (isLongPress) {
                    actionInterface.getPerformanceGestures().inlineSetSettings();
                } else {
                    actionInterface.getPerformanceGestures().inlineSet();
                }
                break;
            case "transpose":
                actionInterface.getPerformanceGestures().transpose();
                break;
            case "pad":
                if (isLongPress) {
                    actionInterface.getPerformanceGestures().padSettings();
                } else {
                    actionInterface.getPerformanceGestures().togglePad();
                }
                break;
            case "metronome":
                if (isLongPress) {
                    actionInterface.getPerformanceGestures().metronomeSettings();
                } else {
                    actionInterface.getPerformanceGestures().toggleMetronome();
                }
                break;
            case "autoscroll":
                if (isLongPress) {
                    actionInterface.getPerformanceGestures().autoscrollSettings();
                } else {
                    actionInterface.getPerformanceGestures().toggleAutoscroll();
                }
                break;
            case "link":
                actionInterface.getPerformanceGestures().openLinks();
                break;
            case "nearby":
                if (isLongPress) {
                    actionInterface.getPerformanceGestures().nearbySettings();
                } else {
                    actionInterface.getPerformanceGestures().nearbyDiscover();
                }
                break;
            case "chordfingerings":
                if (isLongPress) {
                    actionInterface.getPerformanceGestures().chordSettings();
                } else {
                    actionInterface.getPerformanceGestures().showChordFingerings();
                }
                break;
            case "tuner":
                actionInterface.getPerformanceGestures().showTuner();
                break;
            case "stickynotes":
                if (isLongPress) {
                    actionInterface.getPerformanceGestures().stickySettings();
                } else {
                    actionInterface.getPerformanceGestures().showSticky();
                }
                break;
            case "pdfpage":
                actionInterface.getPerformanceGestures().pdfPage();
                break;
            case "highlight":
                if (isLongPress) {
                    actionInterface.getPerformanceGestures().highlighterEdit();
                } else {
                    actionInterface.getPerformanceGestures().showHighlight();
                }
                break;
            case "editsong":
                actionInterface.getPerformanceGestures().editSong();
                break;
            case "addtoset":
                if (isLongPress) {
                    actionInterface.getPerformanceGestures().addToSetAsVariation();
                } else {
                    actionInterface.getPerformanceGestures().addToSet();
                }
                break;
            case "togglescale":
                if (isLongPress) {
                    actionInterface.getPerformanceGestures().editAutoscale();
                } else {
                    actionInterface.getPerformanceGestures().toggleScale();
                }
                break;
            case "scrolldown":
                actionInterface.getPerformanceGestures().scroll(true);
                break;
            case "scrollup":
                actionInterface.getPerformanceGestures().scroll(false);
                break;
            case "next":
                actionInterface.getPerformanceGestures().nextSong();
                break;
            case "previous":
                actionInterface.getPerformanceGestures().prevSong();
                break;
            case "theme":
                actionInterface.getPerformanceGestures().editTheme();
                break;
            case "autoscale":
                actionInterface.getPerformanceGestures().editAutoscale();
                break;
            case "fonts":
                actionInterface.getPerformanceGestures().editFonts();
                break;
            case "profiles":
                actionInterface.getPerformanceGestures().editProfiles();
                break;
            case "gestures":
                actionInterface.getPerformanceGestures().editGestures();
                break;
            case "pedals":
                actionInterface.getPerformanceGestures().editPedals();
                break;
            case "showchords":
                actionInterface.getPerformanceGestures().showChords();
                break;
            case "showcapo":
                actionInterface.getPerformanceGestures().showCapo();
                break;
            case "showlyrics":
                actionInterface.getPerformanceGestures().showLyrics();
                break;
            case "search":
                actionInterface.getPerformanceGestures().songMenu();
                break;
            case "randomsong":
                actionInterface.getPerformanceGestures().randomSong();
                break;
            case "abc":
                if (isLongPress) {
                    actionInterface.getPerformanceGestures().abcEdit();
                } else {
                    actionInterface.getPerformanceGestures().showABCNotation();
                }
                break;
            case "inc_autoscroll_speed":
                actionInterface.getPerformanceGestures().speedUpAutoscroll();
                break;
            case "dec_autoscroll_speed":
                actionInterface.getPerformanceGestures().slowDownAutoscroll();
                break;
            case "toggle_autoscroll_pause":
                actionInterface.getPerformanceGestures().pauseAutoscroll();
                break;
            case "midi":
                if (isLongPress) {
                    actionInterface.getPerformanceGestures().editMidi();
                } else {
                    actionInterface.getPerformanceGestures().songMidi();
                }
                break;
            case "bible":
                actionInterface.getPerformanceGestures().bibleSettings();
                break;
            case "soundlevel":
                actionInterface.getPerformanceGestures().soundLevel();
                break;
            case "import":
                if (isLongPress) {
                    actionInterface.getPerformanceGestures().addSongs();
                } else {
                    actionInterface.getPerformanceGestures().onlineImport();
                }
                break;
            case "invertpdf":
                actionInterface.getPerformanceGestures().invertPDF();
                break;
            case "exit":
                actionInterface.getPerformanceGestures().onBackPressed();
                break;
        }
    }


    // For orientation change, close the page buttons if they are open
    // Also set the translation to the new height
    public void requestLayout() {
        if (actionButton.getRotation()!=0) {
            animatePageButton(false);
        }
    }

    public int getPageButtonNum() {
        return pageButtonNum;
    }
    public int getAnimationTime() {
        return animationTime;
    }
}
