package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

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
    Button pedalAutoScrollPadButton;
    Button pedalAutoScrollMetronomeButton;
    Button pedalPadMetronomeButton;
    Button pedalAutoScrollPadMetronomeButton;

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
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_pedals, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.options_options_pedal));
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
        saveMe.setVisibility(View.GONE);

        // Initialise the views
        pedalPreviousButton = V.findViewById(R.id.pedalPreviousButton);
        pedalNextButton = V.findViewById(R.id.pedalNextButton);
        pedalDownButton = V.findViewById(R.id.pedalDownButton);
        pedalUpButton = V.findViewById(R.id.pedalUpButton);
        pedalToggleScrollBeforeSwipeButton = V.findViewById(R.id.pedalToggleScrollBeforeSwipeButton);
        pedalPadButton = V.findViewById(R.id.pedalPadButton);
        pedalAutoScrollButton = V.findViewById(R.id.pedalAutoScrollButton);
        pedalMetronomeButton = V.findViewById(R.id.pedalMetronomeButton);
        pedalAutoScrollPadButton = V.findViewById(R.id.pedalAutoScrollPadButton);
        pedalAutoScrollMetronomeButton = V.findViewById(R.id.pedalAutoScrollMetronomeButton);
        pedalPadMetronomeButton = V.findViewById(R.id.pedalPadMetronomeButton);
        pedalAutoScrollPadMetronomeButton = V.findViewById(R.id.pedalAutoScrollPadMetronomeButton);

        resetButtons();

        // Set up button listeners
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

        pedalAutoScrollPadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtons();
                pedalAutoScrollPadButton.setEnabled(false);
                pedalAutoScrollPadButton.setText(getResources().getString(
                        R.string.pageturn_waiting));
                assignWhich="autoscrollpad";
            }
        });

        pedalAutoScrollMetronomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtons();
                pedalAutoScrollMetronomeButton.setEnabled(false);
                pedalAutoScrollMetronomeButton.setText(getResources().getString(
                        R.string.pageturn_waiting));
                assignWhich="autoscrollmetronome";
            }
        });

        pedalPadMetronomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtons();
                pedalPadMetronomeButton.setEnabled(false);
                pedalPadMetronomeButton.setText(getResources().getString(
                        R.string.pageturn_waiting));
                assignWhich="padmetronome";
            }
        });
        pedalAutoScrollPadMetronomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtons();
                pedalAutoScrollPadMetronomeButton.setEnabled(false);
                pedalAutoScrollPadMetronomeButton.setText(getResources().getString(
                        R.string.pageturn_waiting));
                assignWhich="autoscrollpadmetronome";
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
        pedalAutoScrollPadButton.setEnabled(true);
        pedalAutoScrollMetronomeButton.setEnabled(true);
        pedalPadMetronomeButton.setEnabled(true);
        pedalAutoScrollPadMetronomeButton.setEnabled(true);
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

        if (FullscreenActivity.pageturner_AUTOSCROLLPAD==-1) {
            String text = getResources().getString(R.string.autoscrollPedalText) + " +\n " +
                    getResources().getString(R.string.padPedalText) + "\n" +
                    getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset);
            pedalAutoScrollPadButton.setText(text);
        } else {
            String text = getResources().getString(R.string.autoscrollPedalText) + " +\n " +
                    getResources().getString(R.string.padPedalText) + "\n" +
                    getResources().getString(R.string.currentkeycode) + "=" +  FullscreenActivity.pageturner_AUTOSCROLLPAD;
            pedalAutoScrollPadButton.setText(text);
        }

        if (FullscreenActivity.pageturner_AUTOSCROLLMETRONOME==-1) {
            String text = getResources().getString(R.string.autoscrollPedalText) + " +\n " +
                    getResources().getString(R.string.metronomePedalText) + "\n" +
                    getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset);
            pedalAutoScrollMetronomeButton.setText(text);
        } else {
            String text = getResources().getString(R.string.autoscrollPedalText) + " +\n " +
                    getResources().getString(R.string.metronomePedalText) + "\n" +
                    getResources().getString(R.string.currentkeycode) + "=" +  FullscreenActivity.pageturner_AUTOSCROLLMETRONOME;
            pedalAutoScrollMetronomeButton.setText(text);
        }

        if (FullscreenActivity.pageturner_PADMETRONOME==-1) {
            String text = getResources().getString(R.string.padPedalText) + " +\n " +
                    getResources().getString(R.string.metronomePedalText) + "\n" +
                    getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset);
            pedalPadMetronomeButton.setText(text);
        } else {
            String text = getResources().getString(R.string.padPedalText) + " +\n " +
                    getResources().getString(R.string.metronomePedalText) + "\n" +
                    getResources().getString(R.string.currentkeycode) + "=" +  FullscreenActivity.pageturner_PADMETRONOME;
            pedalPadMetronomeButton.setText(text);
        }

        if (FullscreenActivity.pageturner_AUTOSCROLLPADMETRONOME==-1) {
            String text = getResources().getString(R.string.autoscrollPedalText) + " +\n " +
                    getResources().getString(R.string.padPedalText) + " +\n" +
                    getResources().getString(R.string.metronomePedalText) + "\n" +
                    getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset);
            pedalAutoScrollPadMetronomeButton.setText(text);
        } else {
            String text = getResources().getString(R.string.autoscrollPedalText) + " +\n " +
                    getResources().getString(R.string.padPedalText) + " +\n" +
                    getResources().getString(R.string.metronomePedalText) + "\n" +
                    getResources().getString(R.string.currentkeycode) + "=" +  FullscreenActivity.pageturner_AUTOSCROLLPADMETRONOME;
            pedalAutoScrollPadMetronomeButton.setText(text);
        }



        if (FullscreenActivity.toggleScrollBeforeSwipe.equals("Y")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                pedalToggleScrollBeforeSwipeButton.setChecked(true);
            }

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                pedalToggleScrollBeforeSwipeButton.setChecked(false);
            }
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
                    } else if (FullscreenActivity.pageturner_AUTOSCROLLPAD == keyCode) {
                        FullscreenActivity.pageturner_AUTOSCROLLPAD = -1;
                    } else if (FullscreenActivity.pageturner_AUTOSCROLLMETRONOME == keyCode) {
                        FullscreenActivity.pageturner_AUTOSCROLLMETRONOME = -1;
                    } else if (FullscreenActivity.pageturner_PADMETRONOME == keyCode) {
                        FullscreenActivity.pageturner_PADMETRONOME = -1;
                    } else if (FullscreenActivity.pageturner_AUTOSCROLLPADMETRONOME == keyCode) {
                        FullscreenActivity.pageturner_AUTOSCROLLPADMETRONOME = -1;
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
                    } else if (assignWhich.equals("autoscrollpad")) {
                        FullscreenActivity.pageturner_AUTOSCROLLPAD = keyCode;
                        Preferences.savePreferences();
                    } else if (assignWhich.equals("autoscrollmetronome")) {
                        FullscreenActivity.pageturner_AUTOSCROLLMETRONOME = keyCode;
                        Preferences.savePreferences();
                    } else if (assignWhich.equals("padmetronome")) {
                        FullscreenActivity.pageturner_PADMETRONOME = keyCode;
                        Preferences.savePreferences();
                    } else if (assignWhich.equals("autoscrollpadmetronome")) {
                        FullscreenActivity.pageturner_AUTOSCROLLPADMETRONOME = keyCode;
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