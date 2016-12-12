package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
public class PopUpPedalsFragment extends DialogFragment {

    static PopUpPedalsFragment newInstance() {
        PopUpPedalsFragment frag;
        frag = new PopUpPedalsFragment();
        return frag;
    }

    Button pedalPreviousButton;
    Button pedalNextButton;
    Button pedalDownButton;
    Button pedalUpButton;
    SwitchCompat pedalToggleScrollBeforeSwipeButton;
    Button pedalPadButton;
    Button pedalAutoScrollButton;
    Button pedalMetronomeButton;
    Button closePedalPopup;

    String assignWhich = "";

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
        getDialog().setTitle(getActivity().getResources().getString(R.string.options_options_pedal));
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_pedals, container, false);

        // Initialise the views
        pedalPreviousButton = (Button) V.findViewById(R.id.pedalPreviousButton);
        pedalNextButton = (Button) V.findViewById(R.id.pedalNextButton);
        pedalDownButton = (Button) V.findViewById(R.id.pedalDownButton);
        pedalUpButton = (Button) V.findViewById(R.id.pedalUpButton);
        pedalToggleScrollBeforeSwipeButton = (SwitchCompat) V.findViewById(R.id.pedalToggleScrollBeforeSwipeButton);
        pedalPadButton = (Button) V.findViewById(R.id.pedalPadButton);
        pedalAutoScrollButton = (Button) V.findViewById(R.id.pedalAutoScrollButton);
        pedalMetronomeButton = (Button) V.findViewById(R.id.pedalMetronomeButton);
        closePedalPopup = (Button) V.findViewById(R.id.closePedalPopup);

        // Set up button listeners
        closePedalPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        pedalPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtons();
                pedalPreviousButton.setEnabled(false);
                pedalPreviousButton.setText(getResources().getString(
                        R.string.pageturn_waiting));
                assignWhich="prev";
            }
        });

        pedalNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtons();
                pedalNextButton.setEnabled(false);
                pedalNextButton.setText(getResources().getString(
                        R.string.pageturn_waiting));
                assignWhich="next";
            }
        });

        pedalDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtons();
                pedalDownButton.setEnabled(false);
                pedalDownButton.setText(getResources().getString(
                        R.string.pageturn_waiting));
                assignWhich="down";
            }
        });

        pedalUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtons();
                pedalUpButton.setEnabled(false);
                pedalUpButton.setText(getResources().getString(
                        R.string.pageturn_waiting));
                assignWhich="up";
            }
        });

        pedalPadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtons();
                pedalPadButton.setEnabled(false);
                pedalPadButton.setText(getResources().getString(
                        R.string.pageturn_waiting));
                assignWhich="pad";
            }
        });

        pedalAutoScrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtons();
                pedalAutoScrollButton.setEnabled(false);
                pedalAutoScrollButton.setText(getResources().getString(
                        R.string.pageturn_waiting));
                assignWhich="autoscroll";
            }
        });

        pedalMetronomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtons();
                pedalMetronomeButton.setEnabled(false);
                pedalMetronomeButton.setText(getResources().getString(
                        R.string.pageturn_waiting));
                assignWhich="metronome";
            }
        });

        pedalToggleScrollBeforeSwipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FullscreenActivity.toggleScrollBeforeSwipe.equals("Y")) {
                    FullscreenActivity.toggleScrollBeforeSwipe = "N";
                } else {
                    FullscreenActivity.toggleScrollBeforeSwipe = "Y";
                }
                Preferences.savePreferences();
            }
        });

        resetButtons();

        return V;
    }

    public void resetButtons() {
        pedalPreviousButton.setEnabled(true);
        pedalNextButton.setEnabled(true);
        pedalDownButton.setEnabled(true);
        pedalUpButton.setEnabled(true);
        pedalToggleScrollBeforeSwipeButton.setEnabled(true);
        pedalPadButton.setEnabled(true);
        pedalAutoScrollButton.setEnabled(true);
        pedalMetronomeButton.setEnabled(true);
        if (FullscreenActivity.pageturner_PREVIOUS==-1) {
            String text = getResources().getString(R.string.pageturn_previous) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset);
            pedalPreviousButton.setText(text);
        } else {
            String text = getResources().getString(R.string.pageturn_previous) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + FullscreenActivity.pageturner_PREVIOUS;
            pedalPreviousButton.setText(text);
        }

        if (FullscreenActivity.pageturner_NEXT==-1) {
            String text = getResources().getString(R.string.pageturn_next) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset);
            pedalNextButton.setText(text);
        } else {
            String text = getResources().getString(R.string.pageturn_next) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + FullscreenActivity.pageturner_NEXT;
            pedalNextButton.setText(text);
        }

        if (FullscreenActivity.pageturner_UP==-1) {
            String text = getResources().getString(R.string.pageturn_up) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset);
            pedalUpButton.setText(text);
        } else {
            String text = getResources().getString(R.string.pageturn_up) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + FullscreenActivity.pageturner_UP;
            pedalUpButton.setText(text);
        }

        if (FullscreenActivity.pageturner_DOWN==-1) {
            String text = getResources().getString(R.string.pageturn_down) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset);
            pedalDownButton.setText(text);
        } else {
            String text = getResources().getString(R.string.pageturn_down) + "\n" + getResources().getString(R.string.currentkeycode) + "=" +  FullscreenActivity.pageturner_DOWN;
            pedalDownButton.setText(text);
        }

        if (FullscreenActivity.pageturner_PAD==-1) {
            String text = getResources().getString(R.string.padPedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset);
            pedalPadButton.setText(text);
        } else {
            String text = getResources().getString(R.string.padPedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" +  FullscreenActivity.pageturner_PAD;
            pedalPadButton.setText(text);
        }

        if (FullscreenActivity.pageturner_AUTOSCROLL==-1) {
            String text = getResources().getString(R.string.autoscrollPedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset);
            pedalAutoScrollButton.setText(text);
        } else {
            String text = getResources().getString(R.string.autoscrollPedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" +  FullscreenActivity.pageturner_AUTOSCROLL;
            pedalAutoScrollButton.setText(text);
        }

        if (FullscreenActivity.pageturner_METRONOME==-1) {
            String text = getResources().getString(R.string.metronomePedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset);
            pedalMetronomeButton.setText(text);
        } else {
            String text = getResources().getString(R.string.metronomePedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" +  FullscreenActivity.pageturner_METRONOME;
            pedalMetronomeButton.setText(text);
        }

        if (FullscreenActivity.toggleScrollBeforeSwipe.equals("Y")) {
            pedalToggleScrollBeforeSwipeButton.setChecked(true);

        } else {
            pedalToggleScrollBeforeSwipeButton.setChecked(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    Log.d("d", "up press=" + keyCode);
                    // Reset buttons already using this keycode
                    if (FullscreenActivity.pageturner_PREVIOUS == keyCode) {
                        FullscreenActivity.pageturner_PREVIOUS = -1;
                    } else if (FullscreenActivity.pageturner_NEXT == keyCode) {
                        FullscreenActivity.pageturner_NEXT = -1;
                    } else if (FullscreenActivity.pageturner_UP == keyCode) {
                        FullscreenActivity.pageturner_UP = -1;
                    } else if (FullscreenActivity.pageturner_DOWN == keyCode) {
                        FullscreenActivity.pageturner_DOWN = -1;
                    } else if (FullscreenActivity.pageturner_PAD == keyCode) {
                        FullscreenActivity.pageturner_PAD = -1;
                    } else if (FullscreenActivity.pageturner_AUTOSCROLL == keyCode) {
                        FullscreenActivity.pageturner_AUTOSCROLL = -1;
                    } else if (FullscreenActivity.pageturner_METRONOME == keyCode) {
                        FullscreenActivity.pageturner_METRONOME = -1;
                    }

                    if (keyCode == KeyEvent.KEYCODE_BACK && assignWhich.length()>0) {
                        //User has pressed the back key - not allowed!!!!
                        FullscreenActivity.myToastMessage = getResources().getString(R.string.no);
                        ShowToast.showToast(getActivity());
                    } else if (keyCode == KeyEvent.KEYCODE_BACK && assignWhich.length()==0) {
                        dismiss();
                        return false;
                    } else if (assignWhich.equals("prev")) {
                        FullscreenActivity.pageturner_PREVIOUS = keyCode;
                        Preferences.savePreferences();
                    } else if (assignWhich.equals("next")) {
                        FullscreenActivity.pageturner_NEXT = keyCode;
                        Preferences.savePreferences();
                    } else if (assignWhich.equals("up")) {
                        FullscreenActivity.pageturner_UP = keyCode;
                        Preferences.savePreferences();
                    } else if (assignWhich.equals("down")) {
                        FullscreenActivity.pageturner_DOWN = keyCode;
                        Preferences.savePreferences();
                    } else if (assignWhich.equals("pad")) {
                        FullscreenActivity.pageturner_PAD = keyCode;
                        Preferences.savePreferences();
                    } else if (assignWhich.equals("autoscroll")) {
                        FullscreenActivity.pageturner_AUTOSCROLL = keyCode;
                        Preferences.savePreferences();
                    } else if (assignWhich.equals("metronome")) {
                        FullscreenActivity.pageturner_METRONOME = keyCode;
                        Preferences.savePreferences();
                    }
                    assignWhich = "";
                    resetButtons();

                }
                return true; // pretend we've processed it
            }
        });

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}