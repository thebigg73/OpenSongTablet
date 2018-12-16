package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class PopUpFontsFragment extends DialogFragment {

    static PopUpFontsFragment newInstance() {
        PopUpFontsFragment frag;
        frag = new PopUpFontsFragment();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
        void refreshSecondaryDisplay(String which);
    }

    private MyInterface mListener;

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

    int temp_mylyricsfontnum = FullscreenActivity.mylyricsfontnum,
            temp_mychordsfontnum = FullscreenActivity.mychordsfontnum,
            temp_mypresofontnum = FullscreenActivity.mypresofontnum,
            temp_mypresoinfofontnum = FullscreenActivity.mypresoinfofontnum,
            temp_linespacing = FullscreenActivity.linespacing;
    Spinner lyricsFontSpinner, chordsFontSpinner, presoFontSpinner, presoInfoFontSpinner, customFontSpinner;
    TextView headingPreview, commentPreview, lyricsPreview1, lyricsPreview2, chordPreview1,
            chordPreview2, scaleHeading_TextView, scaleComment_TextView, scaleChords_TextView,
            lineSpacingText;
    SeekBar scaleHeading_SeekBar, scaleComment_SeekBar, scaleChords_SeekBar, lineSpacingSeekBar;
    TableLayout songPreview;
    SwitchCompat trimlines_SwitchCompat, trimsections_SwitchCompat, hideBox_SwitchCompat;
    StorageAccess storageAccess;

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_font, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.options_options_fonts));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe,getActivity());
                saveMe.setEnabled(false);
                doSave();
            }
        });

        // Initialise the views
        lyricsFontSpinner = V.findViewById(R.id.lyricsFontSpinner);
        chordsFontSpinner = V.findViewById(R.id.chordsFontSpinner);
        presoFontSpinner = V.findViewById(R.id.presoFontSpinner);
        presoInfoFontSpinner = V.findViewById(R.id.presoInfoFontSpinner);
        customFontSpinner = V.findViewById(R.id.customFontSpinner);
        headingPreview = V.findViewById(R.id.headingPreview);
        commentPreview = V.findViewById(R.id.commentPreview);
        lyricsPreview1 = V.findViewById(R.id.lyricsPreview1);
        lyricsPreview2 = V.findViewById(R.id.lyricsPreview2);
        chordPreview1 = V.findViewById(R.id.chordPreview1);
        chordPreview2 = V.findViewById(R.id.chordPreview2);
        scaleChords_TextView = V.findViewById(R.id.scaleChords_TextView);
        scaleChords_SeekBar = V.findViewById(R.id.scaleChords_SeekBar);
        scaleComment_TextView = V.findViewById(R.id.scaleComment_TextView);
        scaleComment_SeekBar = V.findViewById(R.id.scaleComment_SeekBar);
        scaleHeading_TextView = V.findViewById(R.id.scaleHeading_TextView);
        scaleHeading_SeekBar = V.findViewById(R.id.scaleHeading_SeekBar);
        lineSpacingSeekBar = V.findViewById(R.id.lineSpacingSeekBar);
        lineSpacingText = V.findViewById(R.id.lineSpacingText);
        songPreview = V.findViewById(R.id.songPreview);
        hideBox_SwitchCompat = V.findViewById(R.id.hideBox_SwitchCompat);
        trimlines_SwitchCompat = V.findViewById(R.id.trimlines_SwitchCompat);
        trimsections_SwitchCompat = V.findViewById(R.id.trimsections_SwitchCompat);

        // Set up the typefaces
        SetTypeFace.setTypeface(getActivity());

        // Set the lyrics and chord font preview to what they should look like
        headingPreview.setTextSize(12*FullscreenActivity.headingfontscalesize);
        commentPreview.setTextSize(12*FullscreenActivity.commentfontscalesize);
        lyricsPreview1.setTextSize(12);
        lyricsPreview2.setTextSize(12);
        chordPreview1.setTextSize(12);
        chordPreview2.setTextSize(12);
        lyricsPreview1.setPadding(0, -(int) ((float) temp_linespacing / 3.0f), 0, 0);
        lyricsPreview2.setPadding(0, -(int) ((float) temp_linespacing / 3.0f), 0, 0);
        chordPreview1.setPadding(0, -(int) ((float) temp_linespacing / 3.0f), 0, 0);
        chordPreview2.setPadding(0, -(int) ((float) temp_linespacing / 3.0f), 0, 0);
        trimlines_SwitchCompat.setChecked(FullscreenActivity.trimSections);
        hideBox_SwitchCompat.setChecked(FullscreenActivity.hideLyricsBox);
        trimsections_SwitchCompat.setChecked(!FullscreenActivity.trimSectionSpace);
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
        font_choices.add(getResources().getString(R.string.font_roboto_light));
        font_choices.add(getResources().getString(R.string.font_roboto_thin));
        font_choices.add(getResources().getString(R.string.font_roboto_medium));
        font_choices.add(getActivity().getString(R.string.custom));

        ArrayAdapter<String> choose_fonts = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, font_choices);
        choose_fonts.setDropDownViewResource(R.layout.my_spinner);
        lyricsFontSpinner.setAdapter(choose_fonts);
        chordsFontSpinner.setAdapter(choose_fonts);
        presoFontSpinner.setAdapter(choose_fonts);
        presoInfoFontSpinner.setAdapter(choose_fonts);
        lyricsFontSpinner.setSelection(temp_mylyricsfontnum);
        chordsFontSpinner.setSelection(temp_mychordsfontnum);
        presoFontSpinner.setSelection(temp_mypresofontnum);
        presoInfoFontSpinner.setSelection(temp_mypresoinfofontnum);
        lyricnchordsPreviewUpdate();

        // Set up the custom fonts
        storageAccess = new StorageAccess();
        ArrayList<String> customfontscontents = storageAccess.listFilesInFolder(getActivity(),"Fonts","");
        ArrayList<String> customfontsavail = new ArrayList<>();
        customfontsavail.add("");

        for (String cf : customfontscontents) {
            if (cf.endsWith(".ttf") || cf.endsWith(".otf")) {
                customfontsavail.add(cf);
            }
        }
        final ArrayAdapter<String> choose_customfonts = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, customfontsavail);
        choose_customfonts.setDropDownViewResource(R.layout.my_spinner);
        customFontSpinner.setAdapter(choose_customfonts);
        customFontSpinner.setSelection(customfontpos(FullscreenActivity.customfontname, customfontsavail));

        // Listen for font changes
        customFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                FullscreenActivity.customfontname = choose_customfonts.getItem(i);
                FullscreenActivity.customfont = SetTypeFace.setCustomFont(getActivity(), choose_customfonts.getItem(i));
                SetTypeFace.setTypeface(getActivity());
                Preferences.savePreferences();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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
        presoFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                temp_mypresofontnum = position;
                FullscreenActivity.mypresofontnum = position;
                Preferences.savePreferences();
                SetTypeFace.setTypeface(getActivity());
                if (mListener!=null) {
                    mListener.refreshSecondaryDisplay("all");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                lyricnchordsPreviewUpdate();
            }
        });
        presoInfoFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                temp_mypresoinfofontnum = position;
                FullscreenActivity.mypresoinfofontnum = position;
                Preferences.savePreferences();
                SetTypeFace.setTypeface(getActivity());
                if (mListener!=null) {
                    mListener.refreshSecondaryDisplay("all");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                lyricnchordsPreviewUpdate();
            }
        });

        // Listen for seekbar changes
        scaleHeading_SeekBar.setMax(200);
        int progress = (int) (FullscreenActivity.headingfontscalesize * 100);
        scaleHeading_SeekBar.setProgress(progress);
        String text = progress + "%";
        scaleHeading_TextView.setText(text);
        scaleHeading_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = progress + "%";
                scaleHeading_TextView.setText(text);
                float newsize = 12 * ((float) progress/100.0f);
                headingPreview.setTextSize(newsize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        scaleChords_SeekBar.setMax(200);
        progress = (int) (FullscreenActivity.chordfontscalesize * 100);
        scaleChords_SeekBar.setProgress(progress);
        text = progress + "%";
        scaleChords_TextView.setText(text);
        scaleChords_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = progress + "%";
                scaleChords_TextView.setText(text);
                float newsize = 12 * ((float) progress/100.0f);
                chordPreview1.setTextSize(newsize);
                chordPreview2.setTextSize(newsize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        scaleComment_SeekBar.setMax(200);
        progress = (int) (FullscreenActivity.commentfontscalesize * 100);
        scaleComment_SeekBar.setProgress(progress);
        text = progress + "%";
        scaleComment_TextView.setText(text);
        scaleComment_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = progress + "%";
                scaleComment_TextView.setText(text);
                float newsize = 12 * ((float) progress/100.0f);
                commentPreview.setTextSize(newsize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        lineSpacingSeekBar.setMax(30);
        lineSpacingSeekBar.setProgress(temp_linespacing);
        String spacing_text = temp_linespacing + " %";
        lineSpacingText.setText(spacing_text);
        lineSpacingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String newtext = progress + " %";
                lineSpacingText.setText(newtext);
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

        hideBox_SwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.hideLyricsBox = b;
                Preferences.savePreferences();
                if (mListener!=null) {
                    mListener.refreshAll();
                }
            }
        });
        trimlines_SwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.trimSections = b;
                Preferences.savePreferences();
                if (mListener!=null) {
                    mListener.refreshAll();
                }
            }
        });
        trimsections_SwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // Since this asks the user if they want the space, the trim value is the opposite!
                FullscreenActivity.trimSectionSpace = !b;
                Preferences.savePreferences();
                if (mListener!=null) {
                    mListener.refreshAll();
                }
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    public int customfontpos(String s, ArrayList<String> ar) {
        int i=0;
        for (int z=0; z<ar.size(); z++) {
            if (s.equals(ar.get(z))) {
                i = z;
            }
        }
        return i;
    }

    public void doSave() {
        FullscreenActivity.mylyricsfontnum = lyricsFontSpinner.getSelectedItemPosition();
        FullscreenActivity.mychordsfontnum = chordsFontSpinner.getSelectedItemPosition();
        FullscreenActivity.mypresofontnum = presoFontSpinner.getSelectedItemPosition();
        FullscreenActivity.mypresoinfofontnum = presoInfoFontSpinner.getSelectedItemPosition();
        FullscreenActivity.linespacing = lineSpacingSeekBar.getProgress();
        float num = (float) scaleHeading_SeekBar.getProgress()/100.0f;
        FullscreenActivity.headingfontscalesize = num;
        num = (float) scaleComment_SeekBar.getProgress()/100.0f;
        FullscreenActivity.commentfontscalesize = num;
        num = (float) scaleChords_SeekBar.getProgress()/100.0f;
        FullscreenActivity.chordfontscalesize = num;
        Preferences.savePreferences();
        mListener.refreshAll();
        dismiss();
    }

    public void lyricnchordsPreviewUpdate() {
        lyricsPreview1.setTextColor(FullscreenActivity.light_lyricsTextColor);
        lyricsPreview2.setTextColor(FullscreenActivity.light_lyricsTextColor);
        chordPreview1.setTextColor(FullscreenActivity.light_lyricsChordsColor);
        chordPreview2.setTextColor(FullscreenActivity.light_lyricsChordsColor);
        songPreview.setBackgroundColor(FullscreenActivity.light_lyricsBackgroundColor);

        // Decide on the font being used for the lyrics
        switch (temp_mylyricsfontnum) {
            case 1:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface1);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface1);
                headingPreview.setTypeface(FullscreenActivity.typeface1);
                commentPreview.setTypeface(FullscreenActivity.typeface1);
                break;
            case 2:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface2);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface2);
                headingPreview.setTypeface(FullscreenActivity.typeface2);
                commentPreview.setTypeface(FullscreenActivity.typeface2);
                break;
            case 3:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface3);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface3);
                headingPreview.setTypeface(FullscreenActivity.typeface3);
                commentPreview.setTypeface(FullscreenActivity.typeface3);
                break;
            case 4:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface4);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface4);
                headingPreview.setTypeface(FullscreenActivity.typeface4);
                commentPreview.setTypeface(FullscreenActivity.typeface4);
                break;
            case 5:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface5);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface5);
                headingPreview.setTypeface(FullscreenActivity.typeface5);
                commentPreview.setTypeface(FullscreenActivity.typeface5);
                break;
            case 6:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface6);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface6);
                headingPreview.setTypeface(FullscreenActivity.typeface6);
                commentPreview.setTypeface(FullscreenActivity.typeface6);
                break;
            case 7:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface7);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface7);
                headingPreview.setTypeface(FullscreenActivity.typeface7);
                commentPreview.setTypeface(FullscreenActivity.typeface7);
                break;
            case 8:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface8);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface8);
                headingPreview.setTypeface(FullscreenActivity.typeface8);
                commentPreview.setTypeface(FullscreenActivity.typeface8);
                break;
            case 9:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface9);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface9);
                headingPreview.setTypeface(FullscreenActivity.typeface9);
                commentPreview.setTypeface(FullscreenActivity.typeface9);
                break;
            case 10:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface10);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface10);
                headingPreview.setTypeface(FullscreenActivity.typeface10);
                commentPreview.setTypeface(FullscreenActivity.typeface10);
                break;
            case 11:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface11);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface11);
                headingPreview.setTypeface(FullscreenActivity.typeface11);
                commentPreview.setTypeface(FullscreenActivity.typeface11);
                break;
            case 12:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface12);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface12);
                headingPreview.setTypeface(FullscreenActivity.typeface12);
                commentPreview.setTypeface(FullscreenActivity.typeface12);
                break;
            case 13:
                lyricsPreview1.setTypeface(FullscreenActivity.customfont);
                lyricsPreview2.setTypeface(FullscreenActivity.customfont);
                headingPreview.setTypeface(FullscreenActivity.customfont);
                commentPreview.setTypeface(FullscreenActivity.customfont);
                break;
            default:
                lyricsPreview1.setTypeface(FullscreenActivity.typeface0);
                lyricsPreview2.setTypeface(FullscreenActivity.typeface0);
                headingPreview.setTypeface(FullscreenActivity.typeface0);
                commentPreview.setTypeface(FullscreenActivity.typeface0);
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
            case 10:
                chordPreview1.setTypeface(FullscreenActivity.typeface10);
                chordPreview2.setTypeface(FullscreenActivity.typeface10);
                break;
            case 11:
                chordPreview1.setTypeface(FullscreenActivity.typeface11);
                chordPreview2.setTypeface(FullscreenActivity.typeface11);
                break;
            case 12:
                chordPreview1.setTypeface(FullscreenActivity.typeface12);
                chordPreview2.setTypeface(FullscreenActivity.typeface12);
                break;
            case 13:
                chordPreview1.setTypeface(FullscreenActivity.customfont);
                chordPreview2.setTypeface(FullscreenActivity.customfont);
               break;
            default:
                chordPreview1.setTypeface(FullscreenActivity.typeface0);
                chordPreview2.setTypeface(FullscreenActivity.typeface0);
                break;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}