package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
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

    Spinner lyricsFontSpinner, chordsFontSpinner, presoFontSpinner, presoInfoFontSpinner;

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

    TextView scaleHeading_TextView, scaleComment_TextView, scaleChords_TextView,
            lineSpacing_TextView, googleFont_TextView, customPreviewTextView, lyricPreviewTextView,
            chordPreviewTextView, presoPreviewTextView, presoinfoPreviewTextView;
    EditText googleFont_EditText;
    Button browseMore;
    FloatingActionButton testFont_FAB;
    ArrayAdapter<String> choose_fonts;
    ProgressBar progressBar;
    SeekBar scaleHeading_SeekBar, scaleComment_SeekBar, scaleChords_SeekBar, lineSpacing_SeekBar;
    SwitchCompat trimlines_SwitchCompat, trimsections_SwitchCompat, hideBox_SwitchCompat, trimlinespacing_SwitchCompat;
    SetTypeFace setTypeFace;
    StorageAccess storageAccess;
    Preferences preferences;
    // Handlers for fonts
    Handler lyrichandler, chordhandler, presohandler, presoinfohandler, customhandler, monohandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_font, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.options_options_fonts));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setVisibility(View.GONE);
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe,getActivity());
                saveMe.setEnabled(false);
                doSave();
            }
        });

        // Initialise the helper classes
        preferences = new Preferences();
        setTypeFace = new SetTypeFace();

        // Initialise the font handlers
        lyrichandler = new Handler();
        chordhandler = new Handler();
        presohandler = new Handler();
        presoinfohandler = new Handler();
        customhandler = new Handler();
        monohandler = new Handler();

        // Initialise the views
        lyricsFontSpinner = V.findViewById(R.id.lyricsFontSpinner);
        chordsFontSpinner = V.findViewById(R.id.chordsFontSpinner);
        presoFontSpinner = V.findViewById(R.id.presoFontSpinner);
        presoInfoFontSpinner = V.findViewById(R.id.presoInfoFontSpinner);
        lyricPreviewTextView = V.findViewById(R.id.lyricPreviewTextView);
        chordPreviewTextView = V.findViewById(R.id.chordPreviewTextView);
        presoPreviewTextView = V.findViewById(R.id.presoPreviewTextView);
        presoinfoPreviewTextView = V.findViewById(R.id.presoinfoPreviewTextView);
        customPreviewTextView = V.findViewById(R.id.customPreviewTextView);
        scaleChords_TextView = V.findViewById(R.id.scaleChords_TextView);
        scaleChords_SeekBar = V.findViewById(R.id.scaleChords_SeekBar);
        scaleComment_TextView = V.findViewById(R.id.scaleComment_TextView);
        scaleComment_SeekBar = V.findViewById(R.id.scaleComment_SeekBar);
        scaleHeading_TextView = V.findViewById(R.id.scaleHeading_TextView);
        scaleHeading_SeekBar = V.findViewById(R.id.scaleHeading_SeekBar);
        lineSpacing_TextView = V.findViewById(R.id.lineSpacing_TextView);
        lineSpacing_SeekBar = V.findViewById(R.id.lineSpacing_SeekBar);
        trimlinespacing_SwitchCompat = V.findViewById(R.id.trimlinespacing_SwitchCompat);
        hideBox_SwitchCompat = V.findViewById(R.id.hideBox_SwitchCompat);
        trimlines_SwitchCompat = V.findViewById(R.id.trimlines_SwitchCompat);
        trimsections_SwitchCompat = V.findViewById(R.id.trimsections_SwitchCompat);
        googleFont_EditText = V.findViewById(R.id.googleFont_EditText);
        googleFont_TextView = V.findViewById(R.id.googleFont_TextView);
        progressBar = V.findViewById(R.id.progressBar);
        testFont_FAB = V.findViewById(R.id.testFont_FAB);
        browseMore = V.findViewById(R.id.browseMore);

        // Set up the typefaces
        setTypeFace.setUpAppFonts(getActivity(), preferences, lyrichandler, chordhandler,
                presohandler, presoinfohandler, customhandler, monohandler);

        trimlines_SwitchCompat.setChecked(FullscreenActivity.trimSections);
        hideBox_SwitchCompat.setChecked(FullscreenActivity.hideLyricsBox);
        trimsections_SwitchCompat.setChecked(!FullscreenActivity.trimSectionSpace);
        trimlinespacing_SwitchCompat.setChecked(FullscreenActivity.trimLines);
        lineSpacing_SeekBar.setEnabled(FullscreenActivity.trimLines);

        // Set up the custom fonts - use my preferred Google font lists as local files no longer work!!!
        ArrayList<String> customfontsavail = setTypeFace.googleFontsAllowed(getActivity());
        choose_fonts = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, customfontsavail);
        choose_fonts.setDropDownViewResource(R.layout.my_spinner);

        // Set the dropdown lists
        lyricsFontSpinner.setAdapter(choose_fonts);
        chordsFontSpinner.setAdapter(choose_fonts);
        presoFontSpinner.setAdapter(choose_fonts);
        presoInfoFontSpinner.setAdapter(choose_fonts);

        // Select the appropriate items in the list
        lyricsFontSpinner.setSelection(getPositionInList("fontLyric"));
        chordsFontSpinner.setSelection(getPositionInList("fontChord"));
        presoFontSpinner.setSelection(getPositionInList("fontPreso"));
        presoInfoFontSpinner.setSelection(getPositionInList("fontPresoInfo"));

        // Listen for the user changing the Google custom font
        testFont_FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fonttosearch = "Lato";
                if (googleFont_EditText.getText() != null && !googleFont_EditText.getText().toString().equals("")) {
                    fonttosearch = googleFont_EditText.getText().toString();
                }
                setTypeFace.setChosenFont(getActivity(), preferences, fonttosearch, "custom", customPreviewTextView, progressBar, customhandler);
            }
        });

        lyricsFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateItem(position, "fontLyric", "lyric", lyricPreviewTextView, lyrichandler);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        chordsFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateItem(position, "fontChord", "chord", chordPreviewTextView, chordhandler);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        presoFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateItem(position, "fontPreso", "preso", presoPreviewTextView, presohandler);
                if (mListener!=null) {
                    mListener.refreshSecondaryDisplay("all");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        presoInfoFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateItem(position, "fontPresoInfo", "presoinfo", presoinfoPreviewTextView, presoinfohandler);
                if (mListener!=null) {
                    mListener.refreshSecondaryDisplay("all");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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
                //float newsize = 12 * ((float) progress/100.0f);
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
                //float newsize = 12 * ((float) progress/100.0f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        lineSpacing_SeekBar.setMax(100);
        progress = (int) (FullscreenActivity.linespacing * 100);
        lineSpacing_SeekBar.setProgress(progress);
        text = progress + "%";
        lineSpacing_TextView.setText(text);
        lineSpacing_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = progress + "%";
                lineSpacing_TextView.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        trimlinespacing_SwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean b) {
                // Disable the linespacing seekbar if required
                lineSpacing_SeekBar.setEnabled(b);
                FullscreenActivity.trimLines = b;
                Preferences.savePreferences();
            }
        });

        hideBox_SwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.hideLyricsBox = b;
                Preferences.savePreferences();
            }
        });
        trimlines_SwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.trimSections = b;
                Preferences.savePreferences();
            }
        });
        trimsections_SwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // Since this asks the user if they want the space, the trim value is the opposite!
                FullscreenActivity.trimSectionSpace = !b;
                Preferences.savePreferences();
            }
        });
        browseMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.whattodo = "browsefonts";
                FullscreenActivity.webpage = "https://fonts.google.com";
                if (mListener!=null) {
                    mListener.openFragment();
                }
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    void updateItem(int position, String prefname, String what, TextView textView, Handler handler) {
        String fontchosen = choose_fonts.getItem(position);
        preferences.setMyPreferenceString(getActivity(), prefname, fontchosen);
        setTypeFace.setChosenFont(getActivity(), preferences, fontchosen, what,
                textView, null, handler);
    }

    int getPositionInList(String what) {
        String valToFind = preferences.getMyPreferenceString(getActivity(), what, "Lato");
        return choose_fonts.getPosition(valToFind);
    }

    public void doSave() {
        FullscreenActivity.mylyricsfontnum = lyricsFontSpinner.getSelectedItemPosition();
        FullscreenActivity.mychordsfontnum = chordsFontSpinner.getSelectedItemPosition();
        FullscreenActivity.mypresofontnum = presoFontSpinner.getSelectedItemPosition();
        FullscreenActivity.mypresoinfofontnum = presoInfoFontSpinner.getSelectedItemPosition();
        float num = (float) scaleHeading_SeekBar.getProgress()/100.0f;
        FullscreenActivity.headingfontscalesize = num;
        num = (float) scaleComment_SeekBar.getProgress()/100.0f;
        FullscreenActivity.commentfontscalesize = num;
        num = (float) scaleChords_SeekBar.getProgress()/100.0f;
        FullscreenActivity.chordfontscalesize = num;
        num = (float) lineSpacing_SeekBar.getProgress() / 100.0f;
        FullscreenActivity.linespacing = num;
        Preferences.savePreferences();
        mListener.refreshAll();
        dismiss();
    }

    public interface MyInterface {
        void refreshAll();

        void refreshSecondaryDisplay(String which);

        void openFragment();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}