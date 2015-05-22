package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class PopUpFontsFragment extends DialogFragment {

    static PopUpFontsFragment newInstance() {
        PopUpFontsFragment frag;
        frag = new PopUpFontsFragment();
        return frag;
    }

    int temp_mylyricsfontnum = FullscreenActivity.mylyricsfontnum;
    int temp_mychordsfontnum = FullscreenActivity.mychordsfontnum;
    int temp_mMaxFontSize = FullscreenActivity.mMaxFontSize;
    int temp_linespacing = FullscreenActivity.linespacing;
    Spinner lyricsFontSpinner;
    Spinner chordsFontSpinner;
    TextView lyricsPreview1;
    TextView lyricsPreview2;
    TextView chordPreview1;
    TextView chordPreview2;
    SeekBar lineSpacingSeekBar;
    TextView lineSpacingText;
    SeekBar maxAutoScaleSeekBar;
    TextView maxAutoScaleText;
    Button savePopupFont;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.options_options_fonts));
        View V = inflater.inflate(R.layout.popup_font, container, false);

        // Initialise the views
        lyricsFontSpinner = (Spinner) V.findViewById(R.id.lyricsFontSpinner);
        chordsFontSpinner = (Spinner) V.findViewById(R.id.chordsFontSpinner);
        lyricsPreview1 = (TextView) V.findViewById(R.id.lyricsPreview1);
        lyricsPreview2 = (TextView) V.findViewById(R.id.lyricsPreview2);
        chordPreview1 = (TextView) V.findViewById(R.id.chordPreview1);
        chordPreview2 = (TextView) V.findViewById(R.id.chordPreview2);
        lineSpacingSeekBar = (SeekBar) V.findViewById(R.id.lineSpacingSeekBar);
        lineSpacingText = (TextView) V.findViewById(R.id.lineSpacingText);
        maxAutoScaleSeekBar = (SeekBar) V.findViewById(R.id.maxAutoScaleSeekBar);
        maxAutoScaleText = (TextView) V.findViewById(R.id.maxAutoScaleText);
        savePopupFont = (Button) V.findViewById(R.id.savePopupFont);

        // Set up the typefaces
        SetTypeFace.setTypeface();

        // Set up the close button
        savePopupFont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.mylyricsfontnum = lyricsFontSpinner.getSelectedItemPosition();
                FullscreenActivity.mychordsfontnum = chordsFontSpinner.getSelectedItemPosition();
                FullscreenActivity.linespacing = lineSpacingSeekBar.getProgress();
                FullscreenActivity.mMaxFontSize = maxAutoScaleSeekBar.getProgress();
                Preferences.savePreferences();
                dismiss();
            }
        });

        // Set the lyrics and chord font preview to what they should look like
        lyricsPreview1.setTextSize(12);
        lyricsPreview2.setTextSize(12);
        chordPreview1.setTextSize(12);
        chordPreview2.setTextSize(12);
        lyricsPreview1.setPadding(0, -(int) ((float) temp_linespacing / 3.0f), 0, 0);
        lyricsPreview2.setPadding(0, -(int) ((float) temp_linespacing / 3.0f), 0, 0);
        chordPreview1.setPadding(0, -(int) ((float) temp_linespacing / 3.0f), 0, 0);
        chordPreview2.setPadding(0, -(int) ((float) temp_linespacing / 3.0f), 0, 0);

        ArrayList<String> font_choices = new ArrayList<>();
        font_choices.add(getResources().getString(R.string.font_default));
        font_choices.add(getResources().getString(R.string.font_monospace));
        font_choices.add(getResources().getString(R.string.font_sans));
        font_choices.add(getResources().getString(R.string.font_serif));
        font_choices.add(getResources().getString(R.string.font_firasanslight));
        font_choices.add(getResources().getString(R.string.font_firasansregular));
        font_choices.add(getResources().getString(R.string.font_kaushanscript));
        font_choices.add(getResources().getString(R.string.font_latolight));
        font_choices.add(getResources().getString(R.string.font_latoregular));
        font_choices.add(getResources().getString(R.string.font_leaguegothic));

        ArrayAdapter<String> choose_fonts = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, font_choices);
        choose_fonts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lyricsFontSpinner.setAdapter(choose_fonts);
        chordsFontSpinner.setAdapter(choose_fonts);
        lyricsFontSpinner.setSelection(temp_mylyricsfontnum);
        chordsFontSpinner.setSelection(temp_mychordsfontnum);
        lyricnchordsPreviewUpdate();

        // Listen for font changes
        lyricsFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                temp_mylyricsfontnum = position;
                lyricnchordsPreviewUpdate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                lyricnchordsPreviewUpdate();

            }
        });
        chordsFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                temp_mychordsfontnum = position;
                lyricnchordsPreviewUpdate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                lyricnchordsPreviewUpdate();

            }
        });

        // Listen for seekbar changes
        maxAutoScaleSeekBar.setMax(80);
        maxAutoScaleSeekBar.setProgress(temp_mMaxFontSize - 20);
        maxAutoScaleText.setText((temp_mMaxFontSize) + " sp");
        maxAutoScaleText.setTextSize((temp_mMaxFontSize));
        maxAutoScaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maxAutoScaleText.setText((progress + 20) + " sp");
                maxAutoScaleText.setTextSize(progress + 20.0f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                temp_mMaxFontSize = (maxAutoScaleSeekBar.getProgress() + 20);
            }
        });

        lineSpacingSeekBar.setMax(30);
        lineSpacingSeekBar.setProgress(temp_linespacing);
        lineSpacingText.setText(temp_linespacing + " %");
        lineSpacingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lineSpacingText.setText(progress + " %");
                lyricsPreview1.setPadding(0, -(int) ((float) progress / 3.0f), 0, 0);
                lyricsPreview2.setPadding(0, -(int) ((float) progress / 3.0f), 0, 0);
                chordPreview1.setPadding(0, -(int) ((float) progress / 3.0f), 0, 0);
                chordPreview2.setPadding(0, -(int) ((float) progress / 3.0f), 0, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        return V;
    }

    public void lyricnchordsPreviewUpdate() {
        lyricsPreview1.setTextColor(FullscreenActivity.dark_lyricsTextColor);
        lyricsPreview2.setTextColor(FullscreenActivity.dark_lyricsTextColor);
        chordPreview1.setTextColor(FullscreenActivity.dark_lyricsChordsColor);
        chordPreview2.setTextColor(FullscreenActivity.dark_lyricsChordsColor);

        // Decide on the font being used for the lyrics
        switch (temp_mylyricsfontnum) {
            case 1:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface1);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface1);
                break;
            case 2:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface2);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface2);
                break;
            case 3:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface3);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface3);
                break;
            case 4:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface4);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface4);
                break;
            case 5:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface5);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface5);
                break;
            case 6:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface6);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface6);
                break;
            case 7:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface7);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface7);
                break;
            case 8:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface8);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface8);
                break;
            case 9:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface9);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface9);
                break;
            default:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface0);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface0);
                break;
        }

        // Decide on the font being used for chords
        switch (temp_mychordsfontnum) {
            case 1:
                chordPreview1.setTypeface(FullscreenActivity.typeface1);
                chordPreview2.setTypeface(FullscreenActivity.typeface1);
                break;
            case 2:
                chordPreview1.setTypeface(FullscreenActivity.typeface2);
                chordPreview2.setTypeface(FullscreenActivity.typeface2);
                break;
            case 3:
                chordPreview1.setTypeface(FullscreenActivity.typeface3);
                chordPreview2.setTypeface(FullscreenActivity.typeface3);
                break;
            case 4:
                chordPreview1.setTypeface(FullscreenActivity.typeface4);
                chordPreview2.setTypeface(FullscreenActivity.typeface4);
                break;
            case 5:
                chordPreview1.setTypeface(FullscreenActivity.typeface5);
                chordPreview2.setTypeface(FullscreenActivity.typeface5);
                break;
            case 6:
                chordPreview1.setTypeface(FullscreenActivity.typeface6);
                chordPreview2.setTypeface(FullscreenActivity.typeface6);
                break;
            case 7:
                chordPreview1.setTypeface(FullscreenActivity.typeface7);
                chordPreview2.setTypeface(FullscreenActivity.typeface7);
                break;
            case 8:
                chordPreview1.setTypeface(FullscreenActivity.typeface8);
                chordPreview2.setTypeface(FullscreenActivity.typeface8);
                break;
            case 9:
                chordPreview1.setTypeface(FullscreenActivity.typeface9);
                chordPreview2.setTypeface(FullscreenActivity.typeface9);
                break;
            default:
                chordPreview1.setTypeface(FullscreenActivity.typeface0);
                chordPreview2.setTypeface(FullscreenActivity.typeface0);
                break;
        }
    }

}