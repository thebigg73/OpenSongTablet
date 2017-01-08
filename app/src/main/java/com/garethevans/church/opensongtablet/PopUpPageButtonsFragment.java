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
        void setupPageButtons(String s);
        void showpagebuttons();
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

    Button closeButton;
    // These are old and can be deleted on v4
    SeekBar pageButtons_seekBar;
    SeekBar pageButtonScale_seekBar;
    SeekBar scrollArrows_seekbar;
    //

    SwitchCompat pageButtonSize_Switch;
    SwitchCompat scrollArrows_Switch;
    SeekBar pageButtonTransparency_seekBar;
    TextView transparency_TextView;

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
        getDialog().setTitle(getActivity().getResources().getString(R.string.pagebuttons));
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_pagebuttons, container, false);

        // Initialise the views
        closeButton = (Button) V.findViewById(R.id.closebutton);
        pageButtons_seekBar = (SeekBar) V.findViewById(R.id.pageButtons_seekBar);
        pageButtonScale_seekBar = (SeekBar) V.findViewById(R.id.pageButtonScale_seekBar);
        scrollArrows_seekbar = (SeekBar) V.findViewById(R.id.scrollArrows_seekbar);

        pageButtonSize_Switch = (SwitchCompat) V.findViewById(R.id.pageButtonSize_Switch);
        scrollArrows_Switch = (SwitchCompat) V.findViewById(R.id.scrollArrows_Switch);
        pageButtonTransparency_seekBar = (SeekBar) V.findViewById(R.id.pageButtonTransparency_seekBar);
        transparency_TextView = (TextView) V.findViewById(R.id.transparency_TextView);

        // Set the listeners
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        pageButtons_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        FullscreenActivity.pagebutton_position = "off";
                        FullscreenActivity.togglePageButtons = "N";
                        break;

                    case 1:
                        FullscreenActivity.pagebutton_position = "bottom";
                        FullscreenActivity.togglePageButtons = "Y";
                        break;

                    case 2:
                        FullscreenActivity.pagebutton_position = "right";
                        FullscreenActivity.togglePageButtons = "Y";
                        break;
                }
                Preferences.savePreferences();
                mListener.setupPageButtons("");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        pageButtonScale_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        FullscreenActivity.pagebutton_scale = "XS";
                        break;

                    case 1:
                        FullscreenActivity.pagebutton_scale = "S";
                        break;

                    case 2:
                        FullscreenActivity.pagebutton_scale = "M";
                        break;

                    case 3:
                        FullscreenActivity.pagebutton_scale = "L";
                        break;

                    case 4:
                        FullscreenActivity.pagebutton_scale = "XL";
                        break;

                    case 5:
                        FullscreenActivity.pagebutton_scale = "XXL";
                        break;
                }
                Preferences.savePreferences();
                mListener.setupPageButtons("");
                mListener.showpagebuttons();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        scrollArrows_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        FullscreenActivity.toggleScrollArrows = "S";
                        break;

                    case 1:
                        FullscreenActivity.pagebutton_position = "D";
                        break;
                }
                Preferences.savePreferences();
                mListener.setupPageButtons("");
                mListener.showpagebuttons();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        if (FullscreenActivity.fabSize == FloatingActionButton.SIZE_NORMAL) {
            pageButtonSize_Switch.setChecked(true);
        } else {
            pageButtonSize_Switch.setChecked(false);
        }

        if (FullscreenActivity.toggleScrollArrows.equals("D")) {
            scrollArrows_Switch.setChecked(true);
        } else {
            scrollArrows_Switch.setChecked(false);
        }

        pageButtonSize_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    FullscreenActivity.fabSize = FloatingActionButton.SIZE_NORMAL;
                } else {
                    FullscreenActivity.fabSize = FloatingActionButton.SIZE_MINI;
                }
                Preferences.savePreferences();
                mListener.setupPageButtons("");
                mListener.showpagebuttons();
            }
        });
        scrollArrows_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    FullscreenActivity.toggleScrollArrows = "D";
                } else {
                    FullscreenActivity.toggleScrollArrows = "S";
                }
                Preferences.savePreferences();
                mListener.setupPageButtons("");
                mListener.showpagebuttons();
            }
        });
        int gettransp = (int) (FullscreenActivity.pageButtonAlpha * 100);
        String text = gettransp + "%";
        pageButtonTransparency_seekBar.setProgress(gettransp);
        transparency_TextView.setText(text);

        pageButtonTransparency_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Transparency runs from 0% to 100%
                FullscreenActivity.pageButtonAlpha = progress / 100.0f;
                String text = progress + "%";
                transparency_TextView.setText(text);
                Preferences.savePreferences();
                mListener.setupPageButtons("");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        // Set the intial positions of the seekbars
        switch (FullscreenActivity.pagebutton_position) {
            case "off":
                pageButtons_seekBar.setProgress(0);
                break;
            case "bottom":
                pageButtons_seekBar.setProgress(1);
                break;
            case "right":
                pageButtons_seekBar.setProgress(2);
                break;
        }
        switch (FullscreenActivity.pagebutton_scale) {
            case "XS":
                pageButtonScale_seekBar.setProgress(0);
                break;
            case "S":
                pageButtonScale_seekBar.setProgress(1);
                break;
            case "M":
                pageButtonScale_seekBar.setProgress(2);
                break;
            case "L":
                pageButtonScale_seekBar.setProgress(3);
                break;
            case "XL":
                pageButtonScale_seekBar.setProgress(4);
                break;
            case "XXL":
                pageButtonScale_seekBar.setProgress(5);
                break;
        }
        switch (FullscreenActivity.toggleScrollArrows) {
            case "S":
                scrollArrows_seekbar.setProgress(0);
                break;
            case "D":
                scrollArrows_seekbar.setProgress(1);
                break;
        }
            return V;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
