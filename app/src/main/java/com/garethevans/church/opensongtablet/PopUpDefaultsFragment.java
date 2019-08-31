package com.garethevans.church.opensongtablet;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Objects;

public class PopUpDefaultsFragment extends DialogFragment {

    private Button tl_button;
    private Button tc_button;
    private Button tr_button;
    private Button l_button;
    private Button c_button;
    private Button r_button;
    private Button bl_button;
    private Button bc_button;
    private Button br_button;

    private Preferences preferences;

    static PopUpDefaultsFragment newInstance() {
        PopUpDefaultsFragment frag;
        frag = new PopUpDefaultsFragment();
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.popup_popupdefaults, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.display_popups));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        preferences = new Preferences();

        // Initialise the views
        SeekBar popupAlpha_seekBar = V.findViewById(R.id.popupAlpha_seekBar);
        SeekBar popupScale_seekBar = V.findViewById(R.id.popupScale_seekBar);
        SeekBar popupDim_seekBar = V.findViewById(R.id.popupDim_seekbar);
        tl_button = V.findViewById(R.id.tl_button);
        tc_button = V.findViewById(R.id.tc_button);
        tr_button = V.findViewById(R.id.tr_button);
        l_button = V.findViewById(R.id.l_button);
        c_button = V.findViewById(R.id.c_button);
        r_button = V.findViewById(R.id.r_button);
        bl_button = V.findViewById(R.id.bl_button);
        bc_button = V.findViewById(R.id.bc_button);
        br_button = V.findViewById(R.id.br_button);

        // Set the seekBars to their current positions
        popupAlpha_seekBar.setProgress((int) (preferences.getMyPreferenceFloat(getActivity(),"popupAlpha",0.8f)*10)-6);
        popupDim_seekBar.setProgress((int) (preferences.getMyPreferenceFloat(getActivity(),"popupDim",0.8f)*10));
        popupScale_seekBar.setProgress((int) (preferences.getMyPreferenceFloat(getActivity(),"popupScale",0.7f)*10)-3);

        fixbuttons();

        // Listen for changes
        popupAlpha_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                preferences.setMyPreferenceFloat(getActivity(),"popupAlpha",((i+6.0f) / 10.0f));
                PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(),preferences);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        popupDim_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                preferences.setMyPreferenceFloat(getActivity(),"popupDim", (float) i / 10.0f);
                PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(),preferences);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        popupScale_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                preferences.setMyPreferenceFloat(getActivity(),"popupScale", (i+3.0f)/10.0f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(),preferences);
            }
        });

        tl_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setMyPreferenceString(getActivity(),"popupPosition","tl");
                fixbuttons();
            }
        });
        tc_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setMyPreferenceString(getActivity(),"popupPosition","tc");
                fixbuttons();
            }
        });
        tr_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setMyPreferenceString(getActivity(),"popupPosition","tr");
                fixbuttons();
            }
        });
        l_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setMyPreferenceString(getActivity(),"popupPosition","l");
                fixbuttons();
            }
        });
        c_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setMyPreferenceString(getActivity(),"popupPosition","c");
                fixbuttons();
            }
        });
        r_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setMyPreferenceString(getActivity(),"popupPosition","r");
                fixbuttons();
            }
        });
        bl_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setMyPreferenceString(getActivity(),"popupPosition","bl");
                fixbuttons();
            }
        });
        bc_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setMyPreferenceString(getActivity(),"popupPosition","bc");
                fixbuttons();
            }
        });
        br_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setMyPreferenceString(getActivity(),"popupPosition","br");
                fixbuttons();
            }
        });
        return V;
    }

    //@SuppressWarnings("deprecation")
    private void fixbuttons() {

            tl_button.setBackgroundResource(R.drawable.grey_button);
            tc_button.setBackgroundResource(R.drawable.grey_button);
            tr_button.setBackgroundResource(R.drawable.grey_button);
            l_button.setBackgroundResource(R.drawable.grey_button);
            c_button.setBackgroundResource(R.drawable.grey_button);
            r_button.setBackgroundResource(R.drawable.grey_button);
            bl_button.setBackgroundResource(R.drawable.grey_button);
            bc_button.setBackgroundResource(R.drawable.grey_button);
            br_button.setBackgroundResource(R.drawable.grey_button);

            switch (preferences.getMyPreferenceString(getActivity(),"popupPosition","c")) {
                case "tl":
                    tl_button.setBackgroundResource(R.drawable.blue_button);
                    break;
                case "tc":
                    tc_button.setBackgroundResource(R.drawable.blue_button);
                    break;
                case "tr":
                    tr_button.setBackgroundResource(R.drawable.blue_button);
                    break;
                case "l":
                    l_button.setBackgroundResource(R.drawable.blue_button);
                    break;
                default:
                case "c":
                    c_button.setBackgroundResource(R.drawable.blue_button);
                    preferences.setMyPreferenceString(getActivity(),"popupPosition","c");
                    break;
                case "r":
                    r_button.setBackgroundResource(R.drawable.blue_button);
                    break;
                case "bl":
                    bl_button.setBackgroundResource(R.drawable.blue_button);
                    break;
                case "bc":
                    bc_button.setBackgroundResource(R.drawable.blue_button);
                    break;
                case "br":
                    br_button.setBackgroundResource(R.drawable.blue_button);
                    break;
            }
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(),preferences);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
