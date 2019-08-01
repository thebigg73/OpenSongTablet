package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Objects;

public class PopUpPageButtonsFragment extends DialogFragment {

    static PopUpPageButtonsFragment newInstance() {
        PopUpPageButtonsFragment frag;
        frag = new PopUpPageButtonsFragment();
        return frag;
    }

    public interface MyInterface {
        void pageButtonAlpha(String s);
        void groupPageButtons();
        void openFragment();
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

    private TextView transparency_TextView;
    private SwitchCompat extraButtonGroup_Switch;
    private SwitchCompat customButtonGroup_Switch;

    Preferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View V = inflater.inflate(R.layout.popup_pagebuttons, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.pagebuttons));
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
        SwitchCompat pageButtonSize_Switch = V.findViewById(R.id.pageButtonSize_Switch);
        SwitchCompat pageButtonGroup_Switch = V.findViewById(R.id.pageButtonGroup_Switch);
        SeekBar pageButtonTransparency_seekBar = V.findViewById(R.id.pageButtonTransparency_seekBar);
        transparency_TextView = V.findViewById(R.id.transparency_TextView);
        extraButtonGroup_Switch = V.findViewById(R.id.extraButtonGroup_Switch);
        customButtonGroup_Switch = V.findViewById(R.id.customButtonGroup_Switch);
        SwitchCompat setVisible_Switch = V.findViewById(R.id.setVisible_Switch);
        SwitchCompat padVisible_Switch = V.findViewById(R.id.padVisible_Switch);
        SwitchCompat autoscrollVisible_Switch = V.findViewById(R.id.autoscrollVisible_Switch);
        SwitchCompat metronomeVisible_Switch = V.findViewById(R.id.metronomeVisible_Switch);
        SwitchCompat chordsVisible_Switch = V.findViewById(R.id.chordsVisible_Switch);
        SwitchCompat linksVisible_Switch = V.findViewById(R.id.linksVisible_Switch);
        SwitchCompat stickyVisible_Switch = V.findViewById(R.id.stickyVisible_Switch);
        SwitchCompat notationVisible_Switch = V.findViewById(R.id.notationVisible_Switch);
        SwitchCompat highlightVisible_Switch = V.findViewById(R.id.highlightVisible_Switch);
        SwitchCompat pageselectVisible_Switch = V.findViewById(R.id.pageselectVisible_Switch);
        SwitchCompat custom1Visible_Switch = V.findViewById(R.id.custom1Visible_Switch);
        SwitchCompat custom2Visible_Switch = V.findViewById(R.id.custom2Visible_Switch);
        SwitchCompat custom3Visible_Switch = V.findViewById(R.id.custom3Visible_Switch);
        SwitchCompat custom4Visible_Switch = V.findViewById(R.id.custom4Visible_Switch);
        Button showPageButtons = V.findViewById(R.id.showPageButtons);
        SwitchCompat scrollVisible_Switch = V.findViewById(R.id.scrollVisible_Switch);
        SwitchCompat setMoveVisible_Switch = V.findViewById(R.id.setMoveVisible_Switch);

        // Set the default values
        if (preferences.getMyPreferenceInt(getActivity(),"pageButtonSize",FloatingActionButton.SIZE_NORMAL) ==
                FloatingActionButton.SIZE_NORMAL) {
            pageButtonSize_Switch.setChecked(true);
        } else {
            pageButtonSize_Switch.setChecked(false);
        }
        int gettransp = (int) (preferences.getMyPreferenceFloat(getActivity(), "pageButtonAlpha", 0.5f) * 100);
        String text = gettransp + "%";
        pageButtonTransparency_seekBar.setProgress(gettransp);
        transparency_TextView.setText(text);
        pageButtonGroup_Switch.setChecked(getValue("pageButtonGroupMain", false));
        extraButtonGroup_Switch.setChecked(getValue("pageButtonGroupExtra", false));
        customButtonGroup_Switch.setChecked(getValue("pageButtonGroupCustom", false));
        setVisible_Switch.setChecked(getValue("pageButtonShowSet", true));
        padVisible_Switch.setChecked(getValue("pageButtonShowPad", true));
        autoscrollVisible_Switch.setChecked(getValue("pageButtonShowAutoscroll", true));
        metronomeVisible_Switch.setChecked(getValue("pageButtonShowMetronome", false));
        chordsVisible_Switch.setChecked(getValue("pageButtonShowChords", false));
        linksVisible_Switch.setChecked(getValue("pageButtonShowLinks", false));
        stickyVisible_Switch.setChecked(getValue("pageButtonShowSticky", false));
        notationVisible_Switch.setChecked(getValue("pageButtonShowNotation", false));
        highlightVisible_Switch.setChecked(getValue("pageButtonShowHighlighter", false));
        pageselectVisible_Switch.setChecked(getValue("pageButtonShowPageSelect", false));
        custom1Visible_Switch.setChecked(getValue("pageButtonShowCustom1", true));
        custom2Visible_Switch.setChecked(getValue("pageButtonShowCustom2", true));
        custom3Visible_Switch.setChecked(getValue("pageButtonShowCustom3", true));
        custom4Visible_Switch.setChecked(getValue("pageButtonShowCustom4", true));
        scrollVisible_Switch.setChecked(getValue("pageButtonShowScroll", true));
        setMoveVisible_Switch.setChecked(getValue("pageButtonShowSetMove", true));
        String c1 = getActivity().getString(R.string.custom) + " (1)";
        String c2 = getActivity().getString(R.string.custom) + " (2)";
        String c3 = getActivity().getString(R.string.custom) + " (3)";
        String c4 = getActivity().getString(R.string.custom) + " (4)";
        custom1Visible_Switch.setText(c1);
        custom2Visible_Switch.setText(c2);
        custom3Visible_Switch.setText(c3);
        custom4Visible_Switch.setText(c4);

        enableordisablegrouping();

        // Set the listeners
        pageButtonSize_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    preferences.setMyPreferenceInt(getActivity(),"pageButtonSize",FloatingActionButton.SIZE_NORMAL);
                } else {
                    preferences.setMyPreferenceInt(getActivity(),"pageButtonSize",FloatingActionButton.SIZE_MINI);
                }
                updateDisplay();
            }
        });
        pageButtonGroup_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.setMyPreferenceBoolean(getActivity(),"pageButtonGroupMain", b);
                enableordisablegrouping();
                updateDisplay();
            }
        });
        pageButtonTransparency_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Transparency runs from 0% to 100%
                preferences.setMyPreferenceFloat(getActivity(),"pageButtonAlpha",(float)progress / 100.0f);
                String text = progress + "%";
                transparency_TextView.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {updateDisplay();}
        });

        extraButtonGroup_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonGroupExtra",b);
            }
        });
        customButtonGroup_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonGroupCustom",b);
            }
        });
        setVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonShowSet",b);
            }
        });
        padVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonShowPad",b);
            }
        });
        metronomeVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonShowMetronome",b);
            }
        });
        autoscrollVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonShowAutoscroll",b);
            }
        });
        chordsVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonShowChords",b);
            }
        });
        linksVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonShowLinks",b);
            }
        });
        stickyVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonShowSticky",b);
            }
        });
        notationVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonShowNotation",b);
            }
        });
        highlightVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonShowHighlighter",b);
            }
        });
        pageselectVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonShowPageSelect",b);
            }
        });
        custom1Visible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonShowCustom1",b);
            }
        });
        custom2Visible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonShowCustom2",b);
            }
        });
        custom3Visible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonShowCustom3",b);
            }
        });
        custom4Visible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonShowCustom4",b);
            }
        });
        scrollVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonShowScroll",b);
            }
        });
        setMoveVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveValue("pageButtonShowSetMove",b);
            }
        });
        showPageButtons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "groupedpagebuttons";
                dismiss();
                if (mListener!=null) {
                    mListener.openFragment();
                }
            }
        });
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void saveValue(String value, boolean trueorfalse) {
        preferences.setMyPreferenceBoolean(getActivity(),value,trueorfalse);
        updateDisplay();
    }
    private boolean getValue(String value, boolean fallback) {
        return preferences.getMyPreferenceBoolean(getActivity(),value,fallback);
    }

    private void enableordisablegrouping() {
        extraButtonGroup_Switch.setEnabled(!getValue("pageButtonGroupMain",false));
        customButtonGroup_Switch.setEnabled(!getValue("pageButtonGroupMain",false));
    }

    private void updateDisplay() {
        if (mListener!=null) {
            mListener.groupPageButtons();
            mListener.pageButtonAlpha("");
        }
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
       updateDisplay();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
