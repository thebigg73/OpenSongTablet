package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PopUpPageButtonsFragment extends DialogFragment {

    static PopUpPageButtonsFragment newInstance() {
        PopUpPageButtonsFragment frag;
        frag = new PopUpPageButtonsFragment();
        return frag;
    }

    public interface MyInterface {
        void openFragment();
        // Used for setup and display of all buttons
        void setupPageButtons();
        void onScrollAction();
    }

    private MyInterface mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        mListener = (MyInterface) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    private TextView transparency_TextView;

    private Preferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }

        View V = inflater.inflate(R.layout.popup_pagebuttons, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.pagebuttons));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getContext());
            closeMe.setEnabled(false);
            dismiss();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        preferences = new Preferences();

        // Initialise the views
        SwitchCompat pageButtonSize_Switch = V.findViewById(R.id.pageButtonSize_Switch);
        SwitchCompat pageButtonGroup_Switch = V.findViewById(R.id.pageButtonGroup_Switch);
        SeekBar pageButtonTransparency_seekBar = V.findViewById(R.id.pageButtonTransparency_seekBar);
        transparency_TextView = V.findViewById(R.id.transparency_TextView);
        SwitchCompat extraButtonGroup_Switch = V.findViewById(R.id.extraButtonGroup_Switch);
        SwitchCompat customButtonGroup_Switch = V.findViewById(R.id.customButtonGroup_Switch);
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
        Button customiseQuickLaunchButtons = V.findViewById(R.id.customiseQuickLaunchButtons);
        SwitchCompat scrollVisible_Switch = V.findViewById(R.id.scrollVisible_Switch);
        SwitchCompat setMoveVisible_Switch = V.findViewById(R.id.setMoveVisible_Switch);

        // Set the default values
        pageButtonSize_Switch.setChecked(preferences.getMyPreferenceInt(getContext(), "pageButtonSize", FloatingActionButton.SIZE_NORMAL) ==
                FloatingActionButton.SIZE_NORMAL);
        int gettransp = (int) (preferences.getMyPreferenceFloat(getContext(), "pageButtonAlpha", 0.5f) * 100);
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
        String c1 = getString(R.string.custom) + " (1)";
        String c2 = getString(R.string.custom) + " (2)";
        String c3 = getString(R.string.custom) + " (3)";
        String c4 = getString(R.string.custom) + " (4)";
        custom1Visible_Switch.setText(c1);
        custom2Visible_Switch.setText(c2);
        custom3Visible_Switch.setText(c3);
        custom4Visible_Switch.setText(c4);

        // IV - collapsing and expanding groups means a block is no longer needed
        //enableordisablegrouping();

        // Set the listeners
        pageButtonSize_Switch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                preferences.setMyPreferenceInt(getContext(),"pageButtonSize",FloatingActionButton.SIZE_NORMAL);
            } else {
                preferences.setMyPreferenceInt(getContext(),"pageButtonSize",FloatingActionButton.SIZE_MINI);
            }
            updateDisplay();
        });
        pageButtonGroup_Switch.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(getContext(),"pageButtonGroupMain", b);
            updateDisplay();
        });
        pageButtonTransparency_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Transparency runs from 0% to 100%
                preferences.setMyPreferenceFloat(getContext(),"pageButtonAlpha",(float)progress / 100.0f);
                String text = progress + "%";
                transparency_TextView.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {updateDisplay();}
        });

        extraButtonGroup_Switch.setOnCheckedChangeListener((compoundButton, b) -> {
            saveValue("pageButtonGroupExtra",b);
            updateDisplay();
        });
        customButtonGroup_Switch.setOnCheckedChangeListener((compoundButton, b) -> {
            saveValue("pageButtonGroupCustom",b);
            updateDisplay();
        });
        setVisible_Switch.setOnCheckedChangeListener((compoundButton, b) -> saveValue("pageButtonShowSet",b));
        padVisible_Switch.setOnCheckedChangeListener((compoundButton, b) -> saveValue("pageButtonShowPad",b));
        metronomeVisible_Switch.setOnCheckedChangeListener((compoundButton, b) -> saveValue("pageButtonShowMetronome",b));
        autoscrollVisible_Switch.setOnCheckedChangeListener((compoundButton, b) -> saveValue("pageButtonShowAutoscroll",b));
        chordsVisible_Switch.setOnCheckedChangeListener((compoundButton, b) -> saveValue("pageButtonShowChords",b));
        linksVisible_Switch.setOnCheckedChangeListener((compoundButton, b) -> saveValue("pageButtonShowLinks",b));
        stickyVisible_Switch.setOnCheckedChangeListener((compoundButton, b) -> saveValue("pageButtonShowSticky",b));
        notationVisible_Switch.setOnCheckedChangeListener((compoundButton, b) -> saveValue("pageButtonShowNotation",b));
        highlightVisible_Switch.setOnCheckedChangeListener((compoundButton, b) -> saveValue("pageButtonShowHighlighter",b));
        pageselectVisible_Switch.setOnCheckedChangeListener((compoundButton, b) -> saveValue("pageButtonShowPageSelect",b));
        custom1Visible_Switch.setOnCheckedChangeListener((compoundButton, b) -> saveValue("pageButtonShowCustom1",b));
        custom2Visible_Switch.setOnCheckedChangeListener((compoundButton, b) -> saveValue("pageButtonShowCustom2",b));
        custom3Visible_Switch.setOnCheckedChangeListener((compoundButton, b) -> saveValue("pageButtonShowCustom3",b));
        custom4Visible_Switch.setOnCheckedChangeListener((compoundButton, b) -> saveValue("pageButtonShowCustom4",b));
        scrollVisible_Switch.setOnCheckedChangeListener((compoundButton, b) -> saveValue("pageButtonShowScroll",b));
        setMoveVisible_Switch.setOnCheckedChangeListener((compoundButton, b) -> saveValue("pageButtonShowSetMove",b));
        showPageButtons.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "groupedpagebuttons";
            dismiss();
            if (mListener!=null) {
                mListener.openFragment();
            }
        });
        customiseQuickLaunchButtons.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "quicklaunch";
            dismiss();
            if (mListener!=null) {
                mListener.openFragment();
            }
        });
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void saveValue(String value, boolean trueorfalse) {
        preferences.setMyPreferenceBoolean(getContext(),value,trueorfalse);
        updateDisplay();
    }
    private boolean getValue(String value, boolean fallback) {
        return preferences.getMyPreferenceBoolean(getContext(),value,fallback);
    }

    // IV - enableordisablegrouping() no longer needed with expanding and contracting button groups
    private void updateDisplay() {
        if (mListener!=null) {
            // IV - setup is needed as the all group button needs to change function
            mListener.setupPageButtons();
            mListener.onScrollAction();
        }
    }

    @Override
    public void onDismiss(@NonNull final DialogInterface dialog) {
       updateDisplay();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

}
