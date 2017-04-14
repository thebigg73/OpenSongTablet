package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class PopUpAutoScrollDefaultsFragment extends DialogFragment {

    static PopUpAutoScrollDefaultsFragment newInstance() {
        PopUpAutoScrollDefaultsFragment frag;
        frag = new PopUpAutoScrollDefaultsFragment();
        return frag;
    }

    TextView default_delaytime_TextView;
    SeekBar default_delaytime_SeekBar;
    EditText default_duration_EditText;
    RadioGroup autoscroll_defaults_RadioGroup;
    RadioButton autoscroll_default_RadioButton;
    RadioButton autoscroll_prompt_RadioButton;

    public void onStart() {
        super.onStart();

        // safety check
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
        if (getDialog().getWindow()!=null) {
            getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.popup_dialogtitle);
            TextView title = (TextView) getDialog().getWindow().findViewById(R.id.dialogtitle);
            title.setText(getActivity().getResources().getString(R.string.default_autoscroll));
            FloatingActionButton closeMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.closeMe);
            closeMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            FloatingActionButton saveMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.saveMe);
            saveMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    doSave();
                }
            });
        } else {
            getDialog().setTitle(getActivity().getResources().getString(R.string.default_autoscroll));
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
        getDialog().requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_autoscrolldefaults, container, false);

        // Initialise the views
        default_delaytime_TextView = (TextView) V.findViewById(R.id.default_delaytime_TextView);
        default_delaytime_SeekBar = (SeekBar) V.findViewById(R.id.default_delaytime_SeekBar);
        default_duration_EditText = (EditText) V.findViewById(R.id.default_duration_EditText);
        autoscroll_defaults_RadioGroup = (RadioGroup) V.findViewById(R.id.autoscroll_defaults_RadioGroup);
        autoscroll_default_RadioButton = (RadioButton) V.findViewById(R.id.autoscroll_default_RadioButton);
        autoscroll_prompt_RadioButton = (RadioButton) V.findViewById(R.id.autoscroll_prompt_RadioButton);

        // Set them to the default values
        default_delaytime_SeekBar.setMax(30);
        default_delaytime_SeekBar.setProgress(FullscreenActivity.default_autoscroll_predelay);
        String text = FullscreenActivity.default_autoscroll_predelay + " s";
        default_delaytime_TextView.setText(text);
        if (FullscreenActivity.autoscroll_default_or_prompt.equals("prompt")) {
            autoscroll_prompt_RadioButton.setChecked(true);
            autoscroll_default_RadioButton.setChecked(false);
        } else {
            autoscroll_prompt_RadioButton.setChecked(false);
            autoscroll_default_RadioButton.setChecked(true);
        }

        text = "" + FullscreenActivity.default_autoscroll_songlength;
        default_duration_EditText.setText(text);

        // Set listeners for changes and clicks
        default_delaytime_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = progress + " s";
                default_delaytime_TextView.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        return V;
    }

    public void doSave(){
        FullscreenActivity.default_autoscroll_predelay = default_delaytime_SeekBar.getProgress();
        String length = default_duration_EditText.getText().toString();
        FullscreenActivity.default_autoscroll_songlength = Integer.parseInt(length);
        if (autoscroll_prompt_RadioButton.isChecked()) {
            FullscreenActivity.autoscroll_default_or_prompt = "prompt";
        } else {
            FullscreenActivity.autoscroll_default_or_prompt = "default";
        }
        Preferences.savePreferences();
        dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
