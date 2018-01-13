package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
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
    EditText default_delaymax_EditText;
    EditText default_duration_EditText;
    RadioGroup autoscroll_defaults_RadioGroup;
    RadioButton autoscroll_default_RadioButton;
    RadioButton autoscroll_prompt_RadioButton;

    @Override
    public void onStart() {
        super.onStart();

        // safety check
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

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_autoscrolldefaults, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.default_autoscroll));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, PopUpAutoScrollDefaultsFragment.this.getActivity());
                closeMe.setEnabled(false);
                PopUpAutoScrollDefaultsFragment.this.dismiss();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, PopUpAutoScrollDefaultsFragment.this.getActivity());
                saveMe.setEnabled(false);
                PopUpAutoScrollDefaultsFragment.this.doSave();
            }
        });

        // Initialise the views
        default_delaytime_TextView = V.findViewById(R.id.default_delaytime_TextView);
        default_delaytime_SeekBar = V.findViewById(R.id.default_delaytime_SeekBar);
        default_delaymax_EditText = V.findViewById(R.id.default_delaymax_EditText);
        default_duration_EditText = V.findViewById(R.id.default_duration_EditText);
        autoscroll_defaults_RadioGroup = V.findViewById(R.id.autoscroll_defaults_RadioGroup);
        autoscroll_default_RadioButton = V.findViewById(R.id.autoscroll_default_RadioButton);
        autoscroll_prompt_RadioButton = V.findViewById(R.id.autoscroll_prompt_RadioButton);

        // Set them to the default values
        default_delaytime_SeekBar.setMax(FullscreenActivity.default_autoscroll_predelay_max);
        default_delaytime_SeekBar.setProgress(FullscreenActivity.default_autoscroll_predelay);
        String text = FullscreenActivity.default_autoscroll_predelay + " s";
        default_delaymax_EditText.setText(Integer.toString(FullscreenActivity.default_autoscroll_predelay_max));
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
                seekBar.setMax(Integer.parseInt(default_delaymax_EditText.getText().toString()));
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

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    public void doSave(){
        int i;
        try {
            String s = default_delaymax_EditText.getText().toString();
            s = s.replaceAll("[^\\d.]", "");
            i = Integer.parseInt(s);
        } catch (Exception e) {
            i = 0;
        }
        FullscreenActivity.default_autoscroll_predelay_max = i;
        FullscreenActivity.default_autoscroll_predelay = default_delaytime_SeekBar.getProgress();
        String length = default_duration_EditText.getText().toString();
        try {
            FullscreenActivity.default_autoscroll_songlength = Integer.parseInt(length);
        } catch (Exception e) {
            FullscreenActivity.default_autoscroll_predelay = 0;
        }
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
