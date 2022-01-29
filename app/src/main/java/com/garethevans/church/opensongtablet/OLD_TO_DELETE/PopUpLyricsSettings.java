/*
package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.garethevans.church.opensongtablet.OLD_TO_DELETE._CustomAnimations;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class PopUpLyricsSettings extends DialogFragment {

    static PopUpLyricsSettings newInstance() {
        PopUpLyricsSettings frag;
        frag = new PopUpLyricsSettings();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
    }

    private MyInterface mListener;
    private _Preferences preferences;
    private StorageAccess storageAccess;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    private SwitchCompat songLyricsToggleSwitch, songPresentationOrderSwitch, songLyricsBoxSwitch,
            songTrimSwitch, songSectionSpaceSwitch, trimlinespacing_SwitchCompat;
    private TextView scaleHeading_TextView, scaleComment_TextView, scaleChords_TextView, scaleHeadingTitle_TextView,
            scaleCommentTitle_TextView, lineSpacing_TextView, title;
    private SeekBar scaleHeading_SeekBar, scaleComment_SeekBar, lineSpacing_SeekBar;
    private FloatingActionButton closeMe, saveMe;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_lyricssettings, container, false);

        // Initialise the helper classes
        preferences = new _Preferences();
        storageAccess = new StorageAccess();

        // Initialise the views
        initialiseViews(V);

        // Set the values to defaults
        setDefaults();

        // Update the font sizes
        updateFontSizes();

        // Set up the listeners
        setUpListeners();
        return V;
    }

    private void initialiseViews(View v) {
        closeMe = v.findViewById(R.id.closeMe);
        saveMe = v.findViewById(R.id.saveMe);
        title = v.findViewById(R.id.dialogtitle);
        songLyricsToggleSwitch = v.findViewById(R.id.songLyricsToggleSwitch);
        songLyricsBoxSwitch = v.findViewById(R.id.songLyricsBoxSwitch);
        songTrimSwitch = v.findViewById(R.id.songTrimSwitch);
        songSectionSpaceSwitch = v.findViewById(R.id.songSectionSpaceSwitch);
        scaleHeadingTitle_TextView = v.findViewById(R.id.scaleHeadingTitle_TextView);
        scaleHeading_SeekBar = v.findViewById(R.id.scaleHeading_SeekBar);
        scaleHeading_TextView = v.findViewById(R.id.scaleHeading_TextView);
        scaleCommentTitle_TextView = v.findViewById(R.id.scaleCommentTitle_TextView);
        scaleComment_SeekBar = v.findViewById(R.id.scaleComment_SeekBar);
        scaleComment_TextView = v.findViewById(R.id.scaleComment_TextView);
        trimlinespacing_SwitchCompat = v.findViewById(R.id.trimlinespacing_SwitchCompat);
        lineSpacing_SeekBar = v.findViewById(R.id.lineSpacing_SeekBar);
        lineSpacing_TextView = v.findViewById(R.id.lineSpacing_TextView);
        songPresentationOrderSwitch = v.findViewById(R.id.songPresentationOrderSwitch);
    }

    private void setDefaults() {
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.choose_fonts));
        saveMe.hide();
        songLyricsToggleSwitch.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"displayLyrics",true));
        songLyricsBoxSwitch.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"hideLyricsBox",false));
        songTrimSwitch.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"trimSections",true));
        songSectionSpaceSwitch.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"addSectionSpace",false));
        trimlinespacing_SwitchCompat.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"trimLines",true));
        songPresentationOrderSwitch.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"usePresentationOrder",false));
        scaleHeading_SeekBar.setMax(200);
        int progress = (int) (preferences.getMyPreferenceFloat(getActivity(),"scaleHeadings", 0.6f) * 100);
        scaleHeading_SeekBar.setProgress(progress);
        String text = progress + "%";
        scaleHeading_TextView.setText(text);
        scaleComment_SeekBar.setMax(200);
        progress = (int) (preferences.getMyPreferenceFloat(getActivity(),"scaleComments", 0.8f) * 100);
        scaleComment_SeekBar.setProgress(progress);
        text = progress + "%";
        scaleComment_TextView.setText(text);
        lineSpacing_SeekBar.setMax(100);
        progress = (int) (preferences.getMyPreferenceFloat(getActivity(),"lineSpacing",0.1f) * 100);
        lineSpacing_SeekBar.setProgress(progress);
        text = progress + "%";
        lineSpacing_TextView.setText(text);
        // If we are running kitkat, hide the trim options
        if (!storageAccess.lollipopOrLater()) {
            lineSpacing_SeekBar.setVisibility(View.GONE);
            lineSpacing_TextView.setVisibility(View.GONE);
            trimlinespacing_SwitchCompat.setVisibility(View.GONE);
        }
    }

    private void updateFontSizes() {
        float menuFontSize = preferences.getMyPreferenceFloat(getActivity(), "songMenuAlphaIndexSize", 14.0f);
        ResizeMenuItems resizeMenuItems = new ResizeMenuItems();
        resizeMenuItems.updateTextViewSize(title, menuFontSize, "L", false);
        resizeMenuItems.updateTextViewSize(songLyricsToggleSwitch, menuFontSize, "", false);
        resizeMenuItems.updateTextViewSize(songLyricsBoxSwitch, menuFontSize, "", false);
        resizeMenuItems.updateTextViewSize(songTrimSwitch, menuFontSize, "", false);
        resizeMenuItems.updateTextViewSize(songSectionSpaceSwitch, menuFontSize, "", false);
        resizeMenuItems.updateTextViewSize(scaleHeadingTitle_TextView, menuFontSize, "", false);
        resizeMenuItems.updateTextViewSize(scaleHeading_TextView, menuFontSize, "S", false);
        resizeMenuItems.updateTextViewSize(scaleCommentTitle_TextView, menuFontSize, "", false);
        resizeMenuItems.updateTextViewSize(scaleComment_TextView, menuFontSize, "S", false);
        resizeMenuItems.updateTextViewSize(trimlinespacing_SwitchCompat, menuFontSize, "", false);
        resizeMenuItems.updateTextViewSize(lineSpacing_TextView, menuFontSize, "S", false);
        resizeMenuItems.updateTextViewSize(songPresentationOrderSwitch, menuFontSize, "", false);
    }

    private void setUpListeners() {
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _CustomAnimations.animateFAB(closeMe,getActivity());
                try {
                    if (mListener!=null) {
                        mListener.refreshAll();
                    }
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        songLyricsToggleSwitch.setOnCheckedChangeListener(new SaveCheckedState("displayLyrics"));
        songLyricsBoxSwitch.setOnCheckedChangeListener(new SaveCheckedState("hideLyricsBox"));
        songTrimSwitch.setOnCheckedChangeListener(new SaveCheckedState("trimSections"));
        songSectionSpaceSwitch.setOnCheckedChangeListener(new SaveCheckedState("addSectionSpace"));
        trimlinespacing_SwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean b) {
                // Disable the linespacing seekbar if required
                if (b) {
                    lineSpacing_SeekBar.setVisibility(View.VISIBLE);
                    lineSpacing_TextView.setVisibility(View.VISIBLE);
                } else {
                    lineSpacing_SeekBar.setVisibility(View.GONE);
                    lineSpacing_TextView.setVisibility(View.GONE);
                }
                lineSpacing_SeekBar.setEnabled(b);
                preferences.setMyPreferenceBoolean(getActivity(),"trimLines",b);
            }
        });
        songPresentationOrderSwitch.setOnCheckedChangeListener(new SaveCheckedState("usePresentationOrder"));
        songLyricsBoxSwitch.setOnCheckedChangeListener(new SaveCheckedState("hideLyricsBox"));
        songLyricsBoxSwitch.setOnCheckedChangeListener(new SaveCheckedState("hideLyricsBox"));
        songLyricsBoxSwitch.setOnCheckedChangeListener(new SaveCheckedState("hideLyricsBox"));
        songLyricsBoxSwitch.setOnCheckedChangeListener(new SaveCheckedState("hideLyricsBox"));
        songLyricsBoxSwitch.setOnCheckedChangeListener(new SaveCheckedState("hideLyricsBox"));
        songLyricsBoxSwitch.setOnCheckedChangeListener(new SaveCheckedState("hideLyricsBox"));
        songLyricsBoxSwitch.setOnCheckedChangeListener(new SaveCheckedState("hideLyricsBox"));
        songLyricsBoxSwitch.setOnCheckedChangeListener(new SaveCheckedState("hideLyricsBox"));

        scaleHeading_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = progress + "%";
                scaleHeading_TextView.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float num = (float) scaleHeading_SeekBar.getProgress() / 100.0f;
                preferences.setMyPreferenceFloat(getActivity(), "scaleHeadings", num);
            }
        });
        scaleComment_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = progress + "%";
                scaleComment_TextView.setText(text);
                //float newsize = 12 * ((float) progress/100.0f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float num = (float) scaleComment_SeekBar.getProgress() / 100.0f;
                preferences.setMyPreferenceFloat(getActivity(), "scaleComments", num);
            }
        });
        lineSpacing_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = progress + "%";
                lineSpacing_TextView.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float num = (float) lineSpacing_SeekBar.getProgress() / 100.0f;
                preferences.setMyPreferenceFloat(getActivity(), "lineSpacing", num);
            }
        });
    }

    class SaveCheckedState implements CompoundButton.OnCheckedChangeListener {
        String value;
        SaveCheckedState(String s) {
            this.value = s;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            preferences.setMyPreferenceBoolean(getActivity(), value, isChecked);
        }
    }
}
*/
