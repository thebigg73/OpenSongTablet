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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class PopUpPageButtonsFragment extends DialogFragment {

    static PopUpPageButtonsFragment newInstance() {
        PopUpPageButtonsFragment frag;
        frag = new PopUpPageButtonsFragment();
        return frag;
    }

    public interface MyInterface {
        void pageButtonAlpha(String s);
        void setupPageButtons(String s);
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

    SwitchCompat pageButtonSize_Switch;
    SwitchCompat pageButtonGroup_Switch;
    SeekBar pageButtonTransparency_seekBar;
    TextView transparency_TextView;
    SwitchCompat extraButtonGroup_Switch;
    SwitchCompat customButtonGroup_Switch;
    SwitchCompat setVisible_Switch;
    SwitchCompat padVisible_Switch;
    SwitchCompat autoscrollVisible_Switch;
    SwitchCompat metronomeVisible_Switch;
    SwitchCompat chordsVisible_Switch;
    SwitchCompat linksVisible_Switch;
    SwitchCompat stickyVisible_Switch;
    SwitchCompat highlightVisible_Switch;
    SwitchCompat pageselectVisible_Switch;
    SwitchCompat custom1Visible_Switch;
    SwitchCompat custom2Visible_Switch;
    SwitchCompat custom3Visible_Switch;
    SwitchCompat custom4Visible_Switch;
    Button showPageButtons;

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

        View V = inflater.inflate(R.layout.popup_pagebuttons, container, false);

        TextView title = (TextView) V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.pagebuttons));
        final FloatingActionButton closeMe = (FloatingActionButton) V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = (FloatingActionButton) V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        // Initialise the views
        pageButtonSize_Switch = (SwitchCompat) V.findViewById(R.id.pageButtonSize_Switch);
        pageButtonGroup_Switch = (SwitchCompat) V.findViewById(R.id.pageButtonGroup_Switch);
        pageButtonTransparency_seekBar = (SeekBar) V.findViewById(R.id.pageButtonTransparency_seekBar);
        transparency_TextView = (TextView) V.findViewById(R.id.transparency_TextView);
        extraButtonGroup_Switch = (SwitchCompat) V.findViewById(R.id.extraButtonGroup_Switch);
        customButtonGroup_Switch = (SwitchCompat) V.findViewById(R.id.customButtonGroup_Switch);
        setVisible_Switch = (SwitchCompat) V.findViewById(R.id.setVisible_Switch);
        padVisible_Switch = (SwitchCompat) V.findViewById(R.id.padVisible_Switch);
        autoscrollVisible_Switch = (SwitchCompat) V.findViewById(R.id.autoscrollVisible_Switch);
        metronomeVisible_Switch = (SwitchCompat) V.findViewById(R.id.metronomeVisible_Switch);
        chordsVisible_Switch = (SwitchCompat) V.findViewById(R.id.chordsVisible_Switch);
        linksVisible_Switch = (SwitchCompat) V.findViewById(R.id.linksVisible_Switch);
        stickyVisible_Switch = (SwitchCompat) V.findViewById(R.id.stickyVisible_Switch);
        highlightVisible_Switch = (SwitchCompat) V.findViewById(R.id.highlightVisible_Switch);
        pageselectVisible_Switch = (SwitchCompat) V.findViewById(R.id.pageselectVisible_Switch);
        custom1Visible_Switch = (SwitchCompat) V.findViewById(R.id.custom1Visible_Switch);
        custom2Visible_Switch = (SwitchCompat) V.findViewById(R.id.custom2Visible_Switch);
        custom3Visible_Switch = (SwitchCompat) V.findViewById(R.id.custom3Visible_Switch);
        custom4Visible_Switch = (SwitchCompat) V.findViewById(R.id.custom4Visible_Switch);
        showPageButtons = (Button) V.findViewById(R.id.showPageButtons);

        // Set the default values
        if (FullscreenActivity.fabSize == FloatingActionButton.SIZE_NORMAL) {
            pageButtonSize_Switch.setChecked(true);
        } else {
            pageButtonSize_Switch.setChecked(false);
        }
        int gettransp = (int) (FullscreenActivity.pageButtonAlpha * 100);
        String text = gettransp + "%";
        pageButtonTransparency_seekBar.setProgress(gettransp);
        transparency_TextView.setText(text);
        pageButtonGroup_Switch.setChecked(FullscreenActivity.grouppagebuttons);

        extraButtonGroup_Switch.setChecked(FullscreenActivity.page_extra_grouped);
        customButtonGroup_Switch.setChecked(FullscreenActivity.page_custom_grouped);
        setVisible_Switch.setChecked(FullscreenActivity.page_set_visible);
        padVisible_Switch.setChecked(FullscreenActivity.page_pad_visible);
        autoscrollVisible_Switch.setChecked(FullscreenActivity.page_autoscroll_visible);
        metronomeVisible_Switch.setChecked(FullscreenActivity.page_metronome_visible);
        chordsVisible_Switch.setChecked(FullscreenActivity.page_chord_visible);
        linksVisible_Switch.setChecked(FullscreenActivity.page_links_visible);
        stickyVisible_Switch.setChecked(FullscreenActivity.page_sticky_visible);
        highlightVisible_Switch.setChecked(FullscreenActivity.page_highlight_visible);
        pageselectVisible_Switch.setChecked(FullscreenActivity.page_pages_visible);
        custom1Visible_Switch.setChecked(FullscreenActivity.page_custom1_visible);
        custom2Visible_Switch.setChecked(FullscreenActivity.page_custom2_visible);
        custom3Visible_Switch.setChecked(FullscreenActivity.page_custom3_visible);
        custom4Visible_Switch.setChecked(FullscreenActivity.page_custom4_visible);
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
                    FullscreenActivity.fabSize = FloatingActionButton.SIZE_NORMAL;
                } else {
                    FullscreenActivity.fabSize = FloatingActionButton.SIZE_MINI;
                }
                quickSave();
            }
        });
        pageButtonGroup_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.grouppagebuttons = b;
                enableordisablegrouping();
                quickSave();
            }
        });
        pageButtonTransparency_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Transparency runs from 0% to 100%
                FullscreenActivity.pageButtonAlpha = progress / 100.0f;
                String text = progress + "%";
                transparency_TextView.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                quickSave();
            }
        });

        extraButtonGroup_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.page_extra_grouped = b;
                quickSave();
            }
        });
        customButtonGroup_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.page_custom_grouped = b;
                quickSave();
            }
        });
        setVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.page_set_visible = b;
                quickSave();
            }
        });
        padVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.page_pad_visible = b;
                quickSave();
            }
        });
        metronomeVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.page_metronome_visible = b;
                quickSave();
            }
        });
        autoscrollVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.page_autoscroll_visible = b;
                quickSave();
            }
        });
        chordsVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.page_chord_visible = b;
                quickSave();
            }
        });
        linksVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.page_links_visible = b;
                quickSave();
            }
        });
        stickyVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.page_sticky_visible = b;
                quickSave();
            }
        });
        highlightVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.page_highlight_visible = b;
                quickSave();
            }
        });
        pageselectVisible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.page_pages_visible = b;
                quickSave();
            }
        });
        custom1Visible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.page_custom1_visible = b;
                quickSave();
            }
        });
        custom2Visible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.page_custom2_visible = b;
                quickSave();
            }
        });
        custom3Visible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.page_custom3_visible = b;
                quickSave();
            }
        });
        custom4Visible_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.page_custom4_visible = b;
                quickSave();
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
        return V;
    }

    public void enableordisablegrouping() {
        extraButtonGroup_Switch.setEnabled(!FullscreenActivity.grouppagebuttons);
        customButtonGroup_Switch.setEnabled(!FullscreenActivity.grouppagebuttons);
    }

    public void quickSave() {
        Preferences.savePreferences();
        if (mListener!=null) {
            mListener.setupPageButtons("");
        }
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if (mListener!=null) {
            mListener.pageButtonAlpha("");
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
