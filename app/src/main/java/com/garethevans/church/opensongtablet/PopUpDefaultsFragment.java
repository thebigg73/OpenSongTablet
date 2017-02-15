package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

public class PopUpDefaultsFragment extends DialogFragment {

    SeekBar popupAlpha_seekBar;
    SeekBar popupScale_seekBar;
    SeekBar popupDim_seekBar;
    Button tl_button;
    Button tc_button;
    Button tr_button;
    Button l_button;
    Button c_button;
    Button r_button;
    Button bl_button;
    Button bc_button;
    Button br_button;

    static PopUpDefaultsFragment newInstance() {
        PopUpDefaultsFragment frag;
        frag = new PopUpDefaultsFragment();
        return frag;
    }

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.popup_popupdefaults, container, false);
        getDialog().setTitle(getActivity().getResources().getString(R.string.options_display_popups));
        getDialog().setCanceledOnTouchOutside(true);

        // Initialise the views
        popupAlpha_seekBar = (SeekBar) V.findViewById(R.id.popupAlpha_seekBar);
        popupScale_seekBar = (SeekBar) V.findViewById(R.id.popupScale_seekBar);
        popupDim_seekBar   = (SeekBar) V.findViewById(R.id.popupDim_seekbar);
        tl_button = (Button) V.findViewById(R.id.tl_button);
        tc_button = (Button) V.findViewById(R.id.tc_button);
        tr_button = (Button) V.findViewById(R.id.tr_button);
        l_button = (Button) V.findViewById(R.id.l_button);
        c_button = (Button) V.findViewById(R.id.c_button);
        r_button = (Button) V.findViewById(R.id.r_button);
        bl_button = (Button) V.findViewById(R.id.bl_button);
        bc_button = (Button) V.findViewById(R.id.bc_button);
        br_button = (Button) V.findViewById(R.id.br_button);
        Button closebutton = (Button) V.findViewById((R.id.closebutton));

        // Set the seekBars to their current positions
        if (FullscreenActivity.popupAlpha_All >= 0.6f && FullscreenActivity.popupAlpha_All <= 1.0f) {
            popupAlpha_seekBar.setProgress((int) (FullscreenActivity.popupAlpha_All*10)-6);
        } else {
            popupAlpha_seekBar.setProgress(4);
            FullscreenActivity.popupAlpha_All = 0.9f;
        }

        if (FullscreenActivity.popupDim_All >= 0.0f && FullscreenActivity.popupDim_All <= 1.0f) {
            popupDim_seekBar.setProgress((int) (FullscreenActivity.popupDim_All*10));
        } else {
            popupDim_seekBar.setProgress(7);
            FullscreenActivity.popupDim_All = 0.7f;
        }

        if (FullscreenActivity.popupScale_All >= 0.6f && FullscreenActivity.popupScale_All <= 0.9f) {
            popupScale_seekBar.setProgress((int) (FullscreenActivity.popupScale_All*10)-6);
        } else {
            popupScale_seekBar.setProgress(2);
            FullscreenActivity.popupScale_All = 0.8f;
        }

        fixbuttons();

        // Listen for changes
        popupAlpha_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Float val = (i+6.0f) / 10.0f;
                FullscreenActivity.popupAlpha_All = val;
                PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
            }
        });

        popupDim_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Float val = (float) i / 10.0f;
                FullscreenActivity.popupDim_All = val;
                PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
            }
        });

        popupScale_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Float val = (i+6.0f)/10.0f;
                FullscreenActivity.popupScale_All = val;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
                Preferences.savePreferences();
            }
        });

        tl_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.popupPosition_All = "tl";
                fixbuttons();
                Preferences.savePreferences();
            }
        });
        tc_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.popupPosition_All = "tc";
                fixbuttons();
                Preferences.savePreferences();
            }
        });
        tr_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.popupPosition_All = "tr";
                fixbuttons();
                Preferences.savePreferences();
            }
        });
        l_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.popupPosition_All = "l";
                fixbuttons();
                Preferences.savePreferences();
            }
        });
        c_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.popupPosition_All = "c";
                fixbuttons();
                Preferences.savePreferences();
            }
        });
        r_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.popupPosition_All = "r";
                fixbuttons();
                Preferences.savePreferences();
            }
        });
        bl_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.popupPosition_All = "bl";
                fixbuttons();
                Preferences.savePreferences();
            }
        });
        bc_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.popupPosition_All = "bc";
                fixbuttons();
                Preferences.savePreferences();
            }
        });
        br_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.popupPosition_All = "br";
                fixbuttons();
                Preferences.savePreferences();
            }
        });
        closebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return V;
    }

    @SuppressWarnings("deprecation")
    public void fixbuttons() {
        tl_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.grey_button));
        tc_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.grey_button));
        tr_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.grey_button));
        l_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.grey_button));
        c_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.grey_button));
        r_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.grey_button));
        bl_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.grey_button));
        bc_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.grey_button));
        br_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.grey_button));

        switch (FullscreenActivity.popupPosition_All) {
            case "tl":
                tl_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.blue_button));
                break;
            case "tc":
                tc_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.blue_button));
                break;
            case "tr":
                tr_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.blue_button));
                break;
            case "l":
                l_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.blue_button));
                break;
            default:
            case "c":
                c_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.blue_button));
                FullscreenActivity.popupPosition_All = "c";
                break;
            case "r":
                r_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.blue_button));
                break;
            case "bl":
                bl_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.blue_button));
                break;
            case "bc":
                bc_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.blue_button));
                break;
            case "br":
                br_button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.blue_button));
                break;
        }
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
