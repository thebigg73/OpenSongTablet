package com.garethevans.church.opensongtablet.stickynotes;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsStickynotesBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.slider.Slider;

public class StickyNotesFragment extends Fragment {

    SettingsStickynotesBinding myView;
    MainActivityInterface mainActivityInterface;
    private String song_notes_string="", on_string="", success_string="", error_string="",
            error_song_not_saved_string="", settings_text="", global_text="", song_specific_text="";
    private final String TAG = "StickyNotesFragment";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(song_notes_string);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsStickynotesBinding.inflate(inflater,container,false);

        prepareStrings();

        mainActivityInterface.setTempSong(new Song(mainActivityInterface.getSong()));
        // Parse current list from tempSong
        mainActivityInterface.getStickyNoteUtils().parseAndMigrate(mainActivityInterface.getTempSong());

        // Set up the views
        setupViews();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            song_notes_string = getString(R.string.song_notes);
            on_string = getString(R.string.on);
            success_string = getString(R.string.success);
            error_song_not_saved_string = getString(R.string.error_song_not_saved);
            error_string = getString(R.string.error);
            settings_text = getString(R.string.settings);
            global_text = getString(R.string.global);
            song_specific_text = getString(R.string.song_specific);
        }
    }

    private void setupViews() {
        mainActivityInterface.getStickyNoteUtils().populateStickyNoteEditorViews(getContext(),myView.stickyNotesContainer);
        myView.autoShowSticky.setChecked(mainActivityInterface.getPreferences().
                getMyPreferenceBoolean("stickyAuto",true));

        myView.stickySongPos.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("stickySongPos",true));
        hideTimeVisibility(myView.autoShowSticky.isChecked());
        int time = mainActivityInterface.getPreferences().
                getMyPreferenceInt("timeToDisplaySticky",0);
        myView.timeSlider.setValue((float)time);
        myView.timeSlider.setLabelFormatter(value -> ((int)value)+"s");
        setTimeHint(time);
        int stickyBgColor = mainActivityInterface.getMyThemeColors().getStickyBackgroundColor();
        int alpha = Color.alpha(stickyBgColor);
        int rgb   = stickyBgColor & 0x00FFFFFF;

        //int alpha = Math.round(mainActivityInterface.getMyThemeColors().getStickyBackgroundSplitAlpha()*100.0f);
        if (alpha<50) {
            alpha = 50;
        }
        // Alpha will be 50 - 255.  We need to change to a rounded percentage
        alpha = Math.round(alpha/2.55f);
        myView.alphaSlider.setValue(alpha);
        setAlphaHint(alpha);
        myView.alphaSlider.setLabelFormatter(value -> ((int)value)+"%");
        float stickyTextSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("stickyTextSize",14f);

        myView.stickyTextSize.setLabelFormatter(value -> ((int)value)+"sp");
        myView.stickyTextSize.setValue(stickyTextSize);
        myView.stickyTextSize.setHintTextSize(stickyTextSize);
        myView.stickyTextSize.setHint((int)stickyTextSize+"sp");

        int stickyWidth = mainActivityInterface.getPreferences().getMyPreferenceInt("stickyWidth",400);
        myView.stickyWidth.setLabelFormatter(value -> ((int)value)+"px");
        myView.stickyWidth.setValue(stickyWidth);
        myView.stickyWidth.setHint(stickyWidth +"px");

        String global = settings_text + " (" + global_text + ")";
        String local = settings_text + " (" + song_specific_text + ")";
        myView.settingsGlobal.setText(global);
        myView.settingsLocal.setText(local);

        // Check for overrides
        if (mainActivityInterface.getStickyNoteUtils().hasStickyOnOverride()) {
            myView.settingsOverrideStickySlider.setSliderPos(1);
        } else if (mainActivityInterface.getStickyNoteUtils().hasStickyOffOverride()) {
            myView.settingsOverrideStickySlider.setSliderPos(2);
        } else {
            myView.settingsOverrideStickySlider.setSliderPos(0);
        }
    }

    /*private void populateStickyNoteEditorViews() {
        if (myView == null || getContext() == null) return;

        myView.stickyNotesContainer.removeAllViews();

        // Resolve color values
        int color1 = mainActivityInterface.getMyThemeColors().getColorOnly(
                mainActivityInterface.getMyThemeColors().getStickyBackgroundColor());
        int color2 = mainActivityInterface.getMyThemeColors().getColorOnly(
                mainActivityInterface.getMyThemeColors().getStickyBackgroundColor2());
        int color3 = mainActivityInterface.getMyThemeColors().getColorOnly(
                mainActivityInterface.getMyThemeColors().getStickyBackgroundColor3());
        int color4 = mainActivityInterface.getMyThemeColors().getColorOnly(
                mainActivityInterface.getMyThemeColors().getStickyBackgroundColor4());

        Log.d(TAG, "colors:"+color1+", "+color2+", "+color3+", "+color4);

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (int i = 0; i < mainActivityInterface.getStickyNoteUtils().getStickyNotes().size(); i++) {
            final StickyNote note = mainActivityInterface.getStickyNoteUtils().getStickyNotes().get(i);
            View rowView = inflater.inflate(R.layout.view_sticky_note_item, myView.stickyNotesContainer, false);

            MyMaterialEditText editContent = rowView.findViewById(R.id.editNoteContent);
            mainActivityInterface.getProcessSong().editBoxToMultiline(editContent);
            mainActivityInterface.getProcessSong().stretchEditBoxToLines(editContent,3);

            MyFloatingActionButton deleteButton = rowView.findViewById(R.id.deleteBtn);

            MyFloatingActionButton btn1 = rowView.findViewById(R.id.btnColor1);
            MyFloatingActionButton btn2 = rowView.findViewById(R.id.btnColor2);
            MyFloatingActionButton btn3 = rowView.findViewById(R.id.btnColor3);
            MyFloatingActionButton btn4 = rowView.findViewById(R.id.btnColor4);

            editContent.setText(note.getText());

            // Set background tint for each color button
            btn1.setFABButtonColor(color1);
            btn2.setFABButtonColor(color2);
            btn3.setFABButtonColor(color3);
            btn4.setFABButtonColor(color4);

            // Helper lambda to refresh check icons based on the selected color
            Runnable updateColorSelectionUI = () -> {
                int selectedColor = note.getColor();
                btn1.setImageResource(selectedColor == 1 ? R.drawable.check : 0);
                btn2.setImageResource(selectedColor == 2 ? R.drawable.check : 0);
                btn3.setImageResource(selectedColor == 3 ? R.drawable.check : 0);
                btn4.setImageResource(selectedColor == 4 ? R.drawable.check : 0);

                // Ensure check icon uses a clear dark tint for high visibility
                int tickColor = mainActivityInterface.getMyThemeColors().getColorOnly(
                        mainActivityInterface.getMyThemeColors().getStickyTextColor());

                btn1.setFABIconColor(tickColor);
                btn2.setFABIconColor(tickColor);
                btn3.setFABIconColor(tickColor);
                btn4.setFABIconColor(tickColor);
            };

            // Initialize state
            updateColorSelectionUI.run();

            // Click listeners for color selection
            btn1.setOnClickListener(v -> { note.setColor(1); updateColorSelectionUI.run(); mainActivityInterface.getStickyNoteUtils().syncStickyNotesToTempSong(); });
            btn2.setOnClickListener(v -> { note.setColor(2); updateColorSelectionUI.run(); mainActivityInterface.getStickyNoteUtils().syncStickyNotesToTempSong(); });
            btn3.setOnClickListener(v -> { note.setColor(3); updateColorSelectionUI.run(); mainActivityInterface.getStickyNoteUtils().syncStickyNotesToTempSong(); });
            btn4.setOnClickListener(v -> { note.setColor(4); updateColorSelectionUI.run(); mainActivityInterface.getStickyNoteUtils().syncStickyNotesToTempSong(); });

            // Text change listener
            editContent.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    note.setText(s != null ? s.toString() : "");
                    mainActivityInterface.getStickyNoteUtils().syncStickyNotesToTempSong();
                }
            });

            // Delete note listener (safely remove by index and re-render)
            deleteButton.setOnClickListener(v -> {
                Log.d(TAG, "Current serialized: " + mainActivityInterface.getStickyNoteUtils().serializeStickyNotes());

                Log.d(TAG, "Deleting note at index: " + note.getIndex());
                List<StickyNote> currentNotes = mainActivityInterface.getStickyNoteUtils().getStickyNotes();
                if (note.getIndex() >= 0 && note.getIndex() < currentNotes.size()) {
                    currentNotes.remove(note.getIndex());
                }
                mainActivityInterface.getStickyNoteUtils().syncStickyNotesToTempSong();
                populateStickyNoteEditorViews();
            });

            myView.stickyNotesContainer.addView(rowView);
        }
    }*/

    private void setTimeHint(int time) {
        String val = "s";
        if (time==0) {
            val = on_string;
        } else {
            val = time + val;
        }
        myView.timeSlider.setHint(val);
    }

    private void setAlphaHint(float alpha) {
        String s = (int)alpha + "%";
        myView.alphaSlider.setHint(s);
    }

    private void setupListeners() {
        myView.addStickyNoteButton.setOnClickListener(v -> {
            Log.d(TAG,"about to add new sticky note.  Original size:"+mainActivityInterface.getStickyNoteUtils().getStickyNotes().size());
            // Add a new default sticky note (default position top-right or offset, color 1, empty text)
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int defaultX = Math.max(0, screenWidth - (mainActivityInterface.getPreferences().getMyPreferenceInt("stickyWidth",400)) - 32);
            int defaultY = (int)(mainActivityInterface.getToolbar().getActionBarHeight(false)*1.2f);
            mainActivityInterface.getStickyNoteUtils().getStickyNotes().add(new StickyNote(
                    mainActivityInterface.getStickyNoteUtils().getStickyNotes().size(), defaultX, defaultY, null, 1, " "));

            // Re-serialize and refresh UI container
            mainActivityInterface.getStickyNoteUtils().syncStickyNotesToTempSong();
            mainActivityInterface.getStickyNoteUtils().populateStickyNoteEditorViews(getContext(),myView.stickyNotesContainer);
            Log.d(TAG,"added new sticky note.  New size:"+mainActivityInterface.getStickyNoteUtils().getStickyNotes().size());
        });
        myView.autoShowSticky.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("stickyAuto",isChecked);
            hideTimeVisibility(isChecked);
        });
        myView.stickySongPos.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("stickySongPos",isChecked);
        });
        myView.saveButton.setOnClickListener(v -> {
            mainActivityInterface.getSong().setNotes(mainActivityInterface.getTempSong().getNotes());
            if (mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false)) {
                mainActivityInterface.getShowToast().doIt(success_string);
            } else {
                mainActivityInterface.getShowToast().doIt(error_song_not_saved_string);
            }
        });
        myView.alphaSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) { }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                float val = slider.getValue() / 100f;
                mainActivityInterface.getStickyNoteUtils().updateStickyAlphas(val);
            }
        });
        myView.alphaSlider.addOnChangeListener((slider, value, fromUser) -> setAlphaHint(value));
        myView.timeSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {

            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                mainActivityInterface.getPreferences().setMyPreferenceInt(
                        "timeToDisplaySticky", Math.round(slider.getValue()));
            }
        });
        myView.timeSlider.addOnChangeListener((slider, value, fromUser) -> setTimeHint(Math.round(value)));

        myView.stickyTextSize.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                mainActivityInterface.getPreferences().setMyPreferenceFloat("stickyTextSize",myView.stickyTextSize.getValue());
            }
        });
        myView.stickyTextSize.addOnChangeListener((slider, value, fromUser) -> {
            myView.stickyTextSize.setHintTextSize(value);
            myView.stickyTextSize.setHint(((int)value)+"sp");
        });

        myView.stickyWidth.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                mainActivityInterface.getPreferences().setMyPreferenceInt("stickyWidth",(int)myView.stickyWidth.getValue());
            }
        });
        myView.stickyWidth.addOnChangeListener((slider, value, fromUser) -> myView.stickyWidth.setHint(((int)value)+"px"));

        // The sticky notes override
        myView.settingsOverrideStickySlider.addOnChangeListener((slider, value, fromUser) -> {
            if (value==0) {
                mainActivityInterface.getStickyNoteUtils().updateOverrides(null);
            } else if (value==1) {
                // Add the sticky_on override
                mainActivityInterface.getStickyNoteUtils().updateOverrides("sticky_on");
            } else if (value==2) {
                // Add the sticky_off override
                mainActivityInterface.getStickyNoteUtils().updateOverrides("sticky_off");
            }
            mainActivityInterface.getStickyNoteUtils().syncStickyNotesToTempSong();
            myView.settingsOverrideStickySlider.updateAlphas();
        });
    }

    private void hideTimeVisibility(boolean visible) {
        if (visible) {
            myView.timeSlider.setVisibility(View.VISIBLE);
        } else {
            myView.timeSlider.setVisibility(View.GONE);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
