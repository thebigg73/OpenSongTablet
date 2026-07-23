package com.garethevans.church.opensongtablet.beatbuddy;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.DialogHeader;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDown;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.customviews.FloatWindow;
import com.garethevans.church.opensongtablet.customviews.MyExtendedFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.MyRotaryDialView;
import com.garethevans.church.opensongtablet.drummer.DrumCalculations;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.screensetup.Palette;

import java.util.ArrayList;

public class BeatBuddyControlPopUp {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "BeatBuddyControlPopUp";

    private final MainActivityInterface mainActivityInterface;
    private final Context c;
    private MyRotaryDialView beatBuddyVolumeControl, beatBuddyHeadphoneVolumeControl, beatBuddyTempoControl, beatBuddyDrumKitControl;
    private ExposedDropDown songExposedDropDown;
    private MyFloatingActionButton closeButton, minimiseButton, play, stop, pause;
    private MyExtendedFloatingActionButton saveToSongButton;
    private DialogHeader dialogHeader;
    private LinearLayout minimiseLayout;
    private PopupWindow popupWindow;
    private FloatWindow floatWindow;
    private BBSQLite bbsqLite;

    private int posX, posY;
    private float pageButtonAlpha;
    private boolean minimised = false;
    private boolean settingInitialValues = true;

    private ArrayList<String> songs = new ArrayList<>();
    private ArrayList<String> kits = new ArrayList<>();
    private Palette palette;

    // Initialise the popup class
    public BeatBuddyControlPopUp(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        bbsqLite = new BBSQLite(c);
        posX = 0;
        posY = (int) ((float) mainActivityInterface.getToolbar().getActionBarHeight(mainActivityInterface.needActionBar()) * 1.2f);
        pageButtonAlpha = mainActivityInterface.getMyThemeColors().getPageButtonAlpha();
        palette = new Palette(c);
        // Because we are on a floating window, we need to tweak the theme
        palette.secondary = palette.secondaryVariant;
        palette.primary = palette.primaryVariant;
    }

    // The views and listeners for the popup
    public void floatPlayer(View viewHolder) {
        // If the popup is showing already, dismiss it
        if (popupWindow != null && popupWindow.isShowing()) {
            try {
                popupWindow.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Set up the views
            setupViews();
            setListeners();

            popupWindow.showAtLocation(viewHolder, Gravity.TOP | Gravity.START, posX, posY);

            // Deal with the moveable element (from the top bar)
            setupDrag();

            // Now get the BeatBuddy values
            getBeatBuddyValues(c);
        }
    }

    private void setupViews() {
        settingInitialValues = true;

        // The popup
        popupWindow = new PopupWindow(c);
        popupWindow.setBackgroundDrawable(null);

        // The main layout (FloatWindow is just a custom linearlayout where I've overridden the performclick
        floatWindow = new FloatWindow(c);
        // TODO reinstate
        //floatWindow.setAlpha(pageButtonAlpha);

        View myView = View.inflate(c, R.layout.view_beatbuddy_control_popup, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myView.findViewById(R.id.layout).setBackgroundTintList(ColorStateList.valueOf(mainActivityInterface.getPalette().secondary));
        } else {
            myView.findViewById(R.id.layout).setBackgroundColor(mainActivityInterface.getPalette().secondary);
        }
        floatWindow.addView(myView);

        // Initialise the views
        minimiseLayout = myView.findViewById(R.id.minimiseLayout);
        dialogHeader = myView.findViewById(R.id.dialogHeader);
        beatBuddyVolumeControl = myView.findViewById(R.id.beatBuddyVolume);
        beatBuddyHeadphoneVolumeControl = myView.findViewById(R.id.beatBuddyHeadphoneVolume);
        beatBuddyTempoControl = myView.findViewById(R.id.beatBuddyTempo);
        beatBuddyDrumKitControl = myView.findViewById(R.id.beatBuddyDrumKit);
        songExposedDropDown = myView.findViewById(R.id.songExposedDropDown);
        saveToSongButton = myView.findViewById(R.id.saveToSong);
        play = myView.findViewById(R.id.play);
        pause = myView.findViewById(R.id.pause);
        stop = myView.findViewById(R.id.stop);

        // Update the themes on these views
        beatBuddyVolumeControl.setPalette(palette);
        beatBuddyHeadphoneVolumeControl.setPalette(palette);
        beatBuddyTempoControl.setPalette(palette);
        beatBuddyDrumKitControl.setPalette(palette);
        play.setPalette(palette);
        stop.setPalette(palette);
        pause.setPalette(palette);
        saveToSongButton.setPalette(palette);

        dialogHeader.setText(c.getString(R.string.beat_buddy));
        dialogHeader.setWebHelp(mainActivityInterface, c.getString(R.string.website_beatbuddy_controller));
        dialogHeader.showMinimiseButton(true);
        minimiseButton = dialogHeader.getMinimiseButton();
        closeButton = dialogHeader.getCloseButton();

        try {
            dialogHeader.findViewById(R.id.headerLayout).setPadding(8,0,8,0);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Disable everything until the beatBuddy values are prepared
        setEnabled(false);

        popupWindow.setContentView(floatWindow);
    }

    private void setListeners() {
        dialogHeader.getCloseButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivityInterface.toggleBeatBuddyControlPopUp();
            }
        });
        dialogHeader.getMinimiseButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMinimise();
            }
        });
        // Fires whenever the dial/value is tapped or adjusted
        beatBuddyTempoControl.setTapTempoEnabled(true);
        beatBuddyTempoControl.setOnDialClickListener(currentValue -> {
            // Handle action with the current value
            String tempoHexSequence = mainActivityInterface.getBeatBuddy().getTempoCode(currentValue);
            mainActivityInterface.getMidi().sendMidiHexSequence(tempoHexSequence);
        });
        beatBuddyVolumeControl.setOnDialClickListener(currentValue -> {
            // Handle action with the current value
            mainActivityInterface.getBeatBuddy().setBeatBuddyVolume(currentValue);
            String volumeHexSequence = mainActivityInterface.getBeatBuddy().getVolumeCode();
            mainActivityInterface.getMidi().sendMidiHexSequence(volumeHexSequence);
        });
        beatBuddyHeadphoneVolumeControl.setOnDialClickListener(currentValue -> {
            // Handle action with the current value
            mainActivityInterface.getBeatBuddy().setBeatBuddyHPVolume(currentValue);
            String volumeHPHexSequence = mainActivityInterface.getBeatBuddy().getVolumeHPCode();
            mainActivityInterface.getMidi().sendMidiHexSequence(volumeHPHexSequence);
        });
        beatBuddyDrumKitControl.setOnDialClickListener(currentValue -> {
            // Handle action with the current value
            // The dial uses the drum kits in alphabetical order, so we need to look up the actual kit number
            String currentKit = beatBuddyDrumKitControl.getCurrentTextValue();
            int kitNum = bbsqLite.getNumberFromKit(currentKit);
            String drumKitHexCode = mainActivityInterface.getBeatBuddy().getDrumKitCode(kitNum);
            mainActivityInterface.getMidi().sendMidiHexSequence(drumKitHexCode);
        });
        songExposedDropDown.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (!settingInitialValues) {
                    if (editable != null) {
                        bbsqLite.sendSongMidiCodeFromName(c, editable.toString());
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        });
        play.setOnClickListener(view -> {
            Log.d(TAG,"start");
            mainActivityInterface.getBeatBuddy().beatBuddyStart();
        });
        stop.setOnClickListener(view -> mainActivityInterface.getBeatBuddy().beatBuddyStop());
        pause.setOnClickListener(view -> mainActivityInterface.getBeatBuddy().beatBuddyPause());
        saveToSongButton.setOnClickListener(view -> {
            // Save the fields
            mainActivityInterface.getSong().setBeatbuddykit(beatBuddyDrumKitControl.getCurrentTextValue());
            mainActivityInterface.getSong().setTempo(String.valueOf(beatBuddyTempoControl.getCurrentValue()));

            // Now we save the volume, headphone volume as static messages
            String volumeHexCode = mainActivityInterface.getBeatBuddy().getVolumeCode();
            String volumeHPHexCode = mainActivityInterface.getBeatBuddy().getVolumeHPCode();

            String currentMidiMessages = mainActivityInterface.getSong().getMidi();
            String newMidiMessages;
            if (currentMidiMessages==null) {
                newMidiMessages = volumeHexCode + "\n" + volumeHPHexCode;
            } else {
                newMidiMessages = currentMidiMessages.trim() + "\n" + volumeHexCode + "\n" + volumeHPHexCode;
            }
            mainActivityInterface.getSong().setMidi(newMidiMessages.trim());

            mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
            mainActivityInterface.getShowToast().success();
        });
    }

    private void setupDrag() {
        floatWindow.setOnTouchListener(new View.OnTouchListener() {
            int orgX, orgY;
            int offsetX, offsetY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        orgX = (int) event.getX();
                        orgY = (int) event.getY();
                        floatWindow.performClick();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        offsetX = (int) event.getRawX() - orgX;
                        offsetY = (int) event.getRawY() - orgY;
                        popupWindow.update(offsetX, offsetY, -1, -1, true);
                        break;
                    case MotionEvent.ACTION_UP:
                }
                return true;
            }
        });
    }

    private void setEnabled(boolean enabled) {
        beatBuddyVolumeControl.setEnabled(enabled);
        beatBuddyHeadphoneVolumeControl.setEnabled(enabled);
        beatBuddyTempoControl.setEnabled(enabled);
        beatBuddyDrumKitControl.setEnabled(enabled);
        songExposedDropDown.setEnabled(enabled);
        play.setEnabled(enabled);
        pause.setEnabled(enabled);
        stop.setEnabled(enabled);
        settingInitialValues = !enabled;
    }


    private void getBeatBuddyValues(Context c) {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Decide which songs and kits to use
            if (c!=null) {
                try (BBSQLite bbsqLite = new BBSQLite(c)) {
                    String tableSongs = bbsqLite.TABLE_NAME_DEFAULT_SONGS;
                    String tableKits = bbsqLite.TABLE_NAME_DEFAULT_DRUMS;
                    if (mainActivityInterface.getBeatBuddy().getBeatBuddyUseImported()) {
                        tableSongs = bbsqLite.TABLE_NAME_MY_SONGS;
                        tableKits = bbsqLite.TABLE_NAME_MY_DRUMS;
                    }
                    songs = bbsqLite.getUnique(bbsqLite.COLUMN_SONG_NAME, tableSongs);
                    kits = bbsqLite.getUnique(bbsqLite.COLUMN_KIT_NAME, tableKits);

                    // Now we can populate the controls and enable them
                    setupControls();
                } catch (Exception e) {
                    e.printStackTrace();
                    mainActivityInterface.getStorageAccess().updateCrashLog(e.toString());
                }
            }
        });
    }

    private void setupControls() {
        // This is done on the main thread
        mainActivityInterface.getMainHandler().post(() -> {
            // Set the drum kit values based on the kits we have
            beatBuddyDrumKitControl.setShowValuesAsText(true);
            beatBuddyDrumKitControl.setTextValues(kits);
            beatBuddyDrumKitControl.setValueFrom(1);
            beatBuddyDrumKitControl.setValueTo(kits.size()-1);
            int songKit = kits.indexOf(mainActivityInterface.getSong().getBeatbuddykit());
            beatBuddyDrumKitControl.setCurrentValue(Math.max(songKit, 1));

            beatBuddyTempoControl.setCurrentValue(DrumCalculations.getFixedTempo(mainActivityInterface.getSong().getTempo(),true));

            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(c, songExposedDropDown, R.layout.view_exposed_dropdown_item, songs);
            songExposedDropDown.setAdapter(exposedDropDownArrayAdapter);
            songExposedDropDown.setText(mainActivityInterface.getSong().getBeatbuddysong());

            setEnabled(true);
        });

    }

    private void toggleMinimise() {
        minimised = !minimised;
        minimiseLayout.setVisibility(minimised ? View.GONE : View.VISIBLE);
    }

    // Close down the popup and completely stop and release all resources
    public void destroyPopup() {
        try {
            closeButton = null;
            floatWindow = null;
            minimiseButton = null;
            if (popupWindow != null) {
                popupWindow.dismiss();
                popupWindow = null;
            }
            mainActivityInterface.removeBeatBuddyControlPopUp();
            bbsqLite = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
