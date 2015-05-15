package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

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
    Button pedalToggleScrollBeforeSwipeButton;
    Button pedalPadButton;
    Button pedalAutoScrollButton;
    Button pedalMetronomeButton;
    Button closePedalPopup;
    RelativeLayout mainpopupPage;

    String assignWhich = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.options_options_pedal));
        View V = inflater.inflate(R.layout.popup_pedals, container, false);

        // Initialise the views
        mainpopupPage = (RelativeLayout) V.findViewById(R.id.mainpopupPage);
        pedalPreviousButton = (Button) V.findViewById(R.id.pedalPreviousButton);
        pedalNextButton = (Button) V.findViewById(R.id.pedalNextButton);
        pedalDownButton = (Button) V.findViewById(R.id.pedalDownButton);
        pedalUpButton = (Button) V.findViewById(R.id.pedalUpButton);
        pedalToggleScrollBeforeSwipeButton = (Button) V.findViewById(R.id.pedalToggleScrollBeforeSwipeButton);
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
                    pedalToggleScrollBeforeSwipeButton.setText(getResources().getString(R.string.toggleScrollBeforeSwipe) + "\n" + getResources().getString(R.string.currently) + "=" + getResources().getString(R.string.no));
                    FullscreenActivity.toggleScrollBeforeSwipe = "N";
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.toggleScrollBeforeSwipeToggle) + " " + getResources().getString(R.string.off);
                    ShowToast.showToast(getActivity());
                } else {
                    FullscreenActivity.toggleScrollBeforeSwipe = "Y";
                    pedalToggleScrollBeforeSwipeButton.setText(getResources().getString(R.string.toggleScrollBeforeSwipe) + "\n" + getResources().getString(R.string.currently) + "=" + getResources().getString(R.string.yes));
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.toggleScrollBeforeSwipeToggle) + " " + getResources().getString(R.string.on);
                    ShowToast.showToast(getActivity());
                }
                Preferences.savePreferences();
            }
        });

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
            pedalPreviousButton.setText(getResources().getString(R.string.pageturn_previous) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset));
        } else {
            pedalPreviousButton.setText(getResources().getString(R.string.pageturn_previous) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + FullscreenActivity.pageturner_PREVIOUS);
        }

        if (FullscreenActivity.pageturner_NEXT==-1) {
            pedalNextButton.setText(getResources().getString(R.string.pageturn_next) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset));
        } else {
            pedalNextButton.setText(getResources().getString(R.string.pageturn_next) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + FullscreenActivity.pageturner_NEXT);
        }

        if (FullscreenActivity.pageturner_UP==-1) {
            pedalUpButton.setText(getResources().getString(R.string.pageturn_up) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset));
        } else {
            pedalUpButton.setText(getResources().getString(R.string.pageturn_up) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + FullscreenActivity.pageturner_UP);
        }

        if (FullscreenActivity.pageturner_DOWN==-1) {
            pedalDownButton.setText(getResources().getString(R.string.pageturn_down) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset));
        } else {
            pedalDownButton.setText(getResources().getString(R.string.pageturn_down) + "\n" + getResources().getString(R.string.currentkeycode) + "=" +  FullscreenActivity.pageturner_DOWN);
        }

        if (FullscreenActivity.pageturner_PAD==-1) {
            pedalPadButton.setText(getResources().getString(R.string.padPedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset));
        } else {
            pedalPadButton.setText(getResources().getString(R.string.padPedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" +  FullscreenActivity.pageturner_PAD);
        }

        if (FullscreenActivity.pageturner_AUTOSCROLL==-1) {
            pedalAutoScrollButton.setText(getResources().getString(R.string.autoscrollPedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset));
        } else {
            pedalAutoScrollButton.setText(getResources().getString(R.string.autoscrollPedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" +  FullscreenActivity.pageturner_AUTOSCROLL);
        }

        if (FullscreenActivity.pageturner_METRONOME==-1) {
            pedalMetronomeButton.setText(getResources().getString(R.string.metronomePedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset));
        } else {
            pedalMetronomeButton.setText(getResources().getString(R.string.metronomePedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" +  FullscreenActivity.pageturner_METRONOME);
        }

        if (FullscreenActivity.toggleScrollBeforeSwipe.equals("Y")) {
            pedalToggleScrollBeforeSwipeButton.setText(getResources().getString(R.string.toggleScrollBeforeSwipe) + "\n" + getResources().getString(R.string.currently) + "=" + getResources().getString(R.string.yes));
        } else {
            pedalToggleScrollBeforeSwipeButton.setText(getResources().getString(R.string.toggleScrollBeforeSwipe) + "\n" + getResources().getString(R.string.currently) + "=" + getResources().getString(R.string.no));
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

}