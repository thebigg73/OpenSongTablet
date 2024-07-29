package com.garethevans.church.opensongtablet.midi;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetMidiBoardBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;

public class MidiBoardBottomSheet extends BottomSheetDialogFragment {

    private MainActivityInterface mainActivityInterface;
    private BottomSheetMidiBoardBinding myView;
    private final String TAG = "MidiBoardBottomSheet";
    private int boardNum = 1, colorActive=-1, colorInactive=-1, whichButtonToEdit=-1, whichSliderToEdit=-1;
    private String boardTitle = "";
    private ArrayList<MaterialButton> boardButtonView = new ArrayList<>();
    private ArrayList<String> boardButtonName, boardButtonMIDI, boardSliderName;
    private ArrayList<Integer> boardSliderChannel, boardSliderCC, boardSliderVal;
    private String string_midi_board, string_midi_board_website, string_title;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        getValues(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialog1 -> {
            try {
                BottomSheetDialog d = (BottomSheetDialog) dialog1;
                FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                    BottomSheetBehavior.from(bottomSheet).setDraggable(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = BottomSheetMidiBoardBinding.inflate(inflater,container,false);
        myView.dialogHeading.setText(string_midi_board);
        myView.dialogHeading.setWebHelp(mainActivityInterface,string_midi_board_website);
        myView.dialogHeading.setClose(this);

        // Decide which midi board we are viewing and set up the views
        setupViews(mainActivityInterface.getPreferences().getMyPreferenceInt("midiBoard",1));

        // Set the listeners
        setupListeners();

        return myView.getRoot();
    }

    private void getValues(Context c) {
        if (c!=null) {
            string_midi_board = c.getString(R.string.midi_board);
            string_midi_board_website = c.getString(R.string.website_midi_board);
            string_title = c.getString(R.string.title);
            colorActive = ContextCompat.getColor(c, R.color.colorSecondary);
            colorInactive = ContextCompat.getColor(c, R.color.colorAltPrimary);
        }
    }

    private void setupViews(int boardNum) {
        this.boardNum = boardNum;
        boardTitle = mainActivityInterface.getPreferences().getMyPreferenceString("midiBoard"+boardNum+"Title",string_title+" "+boardNum);

        myView.boardTitle.setText(boardTitle);
        // Adjust the board number switch colours at the top
        myView.boardPage1.setBackgroundColor(boardNum==1 ? colorActive:colorInactive);
        myView.boardPage2.setBackgroundColor(boardNum==2 ? colorActive:colorInactive);
        myView.boardPage3.setBackgroundColor(boardNum==3 ? colorActive:colorInactive);

        // Now get the button names and MIDI codes
        boardButtonView = new ArrayList<>();
        boardButtonName = new ArrayList<>();
        boardButtonMIDI = new ArrayList<>();
        boardButtonName.add("");
        boardButtonMIDI.add("");
        boardButtonView.add(null);
        boardButtonView.add(myView.boardButton1);
        boardButtonView.add(myView.boardButton2);
        boardButtonView.add(myView.boardButton3);
        boardButtonView.add(myView.boardButton4);
        boardButtonView.add(myView.boardButton5);
        boardButtonView.add(myView.boardButton6);
        boardButtonView.add(myView.boardButton7);
        boardButtonView.add(myView.boardButton8);
        for (int x=1; x<=8; x++) {
            boardButtonName.add(mainActivityInterface.getPreferences().getMyPreferenceString("midiBoard"+boardNum+"Button"+x+"Name",String.valueOf(x)));
            boardButtonMIDI.add(mainActivityInterface.getPreferences().getMyPreferenceString("midiBoard"+boardNum+"Button"+x+"MIDI",""));
            setUpButton(x);
        }

        // Now get the sliders
        myView.boardSlider1.setLabelFormatter(value -> String.valueOf((int)value));
        myView.boardSlider2.setLabelFormatter(value -> String.valueOf((int)value));
        boardSliderName = new ArrayList<>();
        boardSliderChannel = new ArrayList<>();
        boardSliderCC = new ArrayList<>();
        boardSliderVal = new ArrayList<>();
        boardSliderName.add("");
        boardSliderChannel.add(1);
        boardSliderCC.add(0);
        boardSliderVal.add(0);
        for (int y=1; y<=2; y++) {
            boardSliderName.add(mainActivityInterface.getPreferences().getMyPreferenceString("midiBoard"+boardNum+"Slider"+y+"Name",""));
            boardSliderChannel.add(mainActivityInterface.getPreferences().getMyPreferenceInt("midiBoard"+boardNum+"Slider"+y+"Channel",1));
            boardSliderCC.add(mainActivityInterface.getPreferences().getMyPreferenceInt("midiBoard"+boardNum+"Slider"+y+"CC",0));
            boardSliderVal.add(mainActivityInterface.getPreferences().getMyPreferenceInt("midiBoard"+boardNum+"Slider"+y+"Value",0));
        }
        myView.boardSlider1.initialiseSliderValues(boardSliderName.get(1),boardSliderChannel.get(1),boardSliderCC.get(1),boardSliderVal.get(1));
        myView.boardSlider2.initialiseSliderValues(boardSliderName.get(2),boardSliderChannel.get(2),boardSliderCC.get(2),boardSliderVal.get(2));
    }

    private void setUpButton(int whichButton) {
        String name = boardButtonName.get(whichButton);
        String midi = boardButtonMIDI.get(whichButton);
        String text = "";
        if (name!=null && !name.trim().isEmpty()) {
            text += "<h2>" + name.trim() + "</h2>";
        }
        if (midi!=null && !midi.trim().isEmpty()) {
            text += "<p>" + midi.trim() + "<p>";
        }
        boardButtonView.get(whichButton).setText(Html.fromHtml(text));
        boardButtonView.get(whichButton).setOnClickListener(view -> {
            Log.d(TAG,"Short press button:"+whichButton+"  midi:"+midi);
            whichButtonToEdit = whichButton;
            mainActivityInterface.getMidi().sendMidiHexSequence(mainActivityInterface.getMidi().checkForShortHandMIDI(midi));
        });
        boardButtonView.get(whichButton).setOnLongClickListener(view -> {
            Log.d(TAG,"Long press button:"+whichButton+"  midi:"+midi);
            whichButtonToEdit = whichButton;
            MidiShortHandBottomSheet midiShortHandBottomSheet = new MidiShortHandBottomSheet(null,MidiBoardBottomSheet.this,"MidiBoardBottomSheet",name,midi);
            midiShortHandBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"MidiShortHandBottomSheet");
            return true;
        });
    }

    private void setupListeners() {
        myView.boardPage1.setOnClickListener(view -> {
            mainActivityInterface.getPreferences().setMyPreferenceInt("midiBoard",1);
            setupViews(1);
        });
        myView.boardPage2.setOnClickListener(view -> {
            mainActivityInterface.getPreferences().setMyPreferenceInt("midiBoard",2);
            setupViews(2);
        });
        myView.boardPage3.setOnClickListener(view -> {
            mainActivityInterface.getPreferences().setMyPreferenceInt("midiBoard",3);
            setupViews(3);
        });
        myView.boardSlider1.getSliderValues().setOnClickListener(view -> {
            String midiCode = myView.boardSlider1.getMidiCode();
            mainActivityInterface.getMidi().sendMidiHexSequence(mainActivityInterface.getMidi().checkForShortHandMIDI(midiCode));
        });
        myView.boardSlider2.getSliderValues().setOnClickListener(view -> {
            String midiCode = myView.boardSlider2.getMidiCode();
            mainActivityInterface.getMidi().sendMidiHexSequence(mainActivityInterface.getMidi().checkForShortHandMIDI(midiCode));
        });
        myView.boardSlider1.setOnLongClickListener(view -> {
            Log.d(TAG,"edit board"+boardNum+"Slider1");
            whichSliderToEdit = 1;
            MidiControllerBottomSheet midiControllerBottomSheet = new MidiControllerBottomSheet(MidiBoardBottomSheet.this,boardSliderName.get(1),boardSliderChannel.get(1),boardSliderCC.get(1));
            midiControllerBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"MidiControllerBottomSheet");
            return true;
        });
        myView.boardSlider2.setOnLongClickListener(view -> {
            Log.d(TAG,"edit board"+boardNum+"Slider2");
            whichSliderToEdit = 2;
            MidiControllerBottomSheet midiControllerBottomSheet = new MidiControllerBottomSheet(MidiBoardBottomSheet.this,boardSliderName.get(2),boardSliderChannel.get(2),boardSliderCC.get(2));
            midiControllerBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"MidiControllerBottomSheet");
            return true;
        });
        myView.boardSlider1.addOnChangeListener((slider, value, fromUser) -> myView.boardSlider1.updateSliderVal((int)value));
        myView.boardSlider1.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                String midiCode = myView.boardSlider1.getMidiCode();
                int value = myView.boardSlider1.getSliderVal();
                boardSliderVal.set(1,value);
                mainActivityInterface.getPreferences().setMyPreferenceInt("midiBoard"+boardNum+"Slider1Value",value);
                mainActivityInterface.getMidi().sendMidiHexSequence(mainActivityInterface.getMidi().checkForShortHandMIDI(midiCode));
            }
        });
        myView.boardSlider2.addOnChangeListener((slider, value, fromUser) -> myView.boardSlider2.updateSliderVal((int)value));
        myView.boardSlider2.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                String midiCode = myView.boardSlider2.getMidiCode();
                int value = myView.boardSlider2.getSliderVal();
                boardSliderVal.set(2,value);
                mainActivityInterface.getPreferences().setMyPreferenceInt("midiBoard"+boardNum+"Slider2Value",value);
                mainActivityInterface.getMidi().sendMidiHexSequence(mainActivityInterface.getMidi().checkForShortHandMIDI(midiCode));
            }
        });
        myView.boardTitle.setOnClickListener(view -> {
            whichSliderToEdit = -1;
            MidiControllerBottomSheet midiControllerBottomSheet = new MidiControllerBottomSheet(MidiBoardBottomSheet.this,boardTitle,-1,-1);
            midiControllerBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"MidiControllerBottomSheet");
        });
        myView.boardTitle.setOnLongClickListener(view -> {
            whichSliderToEdit = -1;
            MidiControllerBottomSheet midiControllerBottomSheet = new MidiControllerBottomSheet(MidiBoardBottomSheet.this,boardTitle,-1,-1);
            midiControllerBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"MidiControllerBottomSheet");
            return true;
        });

    }

    public void addCommand(String buttonName, String newCommand) {
        // We know the button as we clicked on it!
        if (whichButtonToEdit!=-1) {
            mainActivityInterface.getPreferences().setMyPreferenceString("midiBoard"+boardNum+"Button"+whichButtonToEdit+"MIDI",newCommand);
            mainActivityInterface.getPreferences().setMyPreferenceString("midiBoard"+boardNum+"Button"+whichButtonToEdit+"Name",buttonName);
            String text = "";
            if (buttonName!=null && !buttonName.trim().isEmpty()) {
                text += "<h2>" + buttonName.trim() + "</h2>";
            }
            if (newCommand!=null && !newCommand.trim().isEmpty()) {
                text += "<p>" + newCommand.trim() + "</p>";
            }
            boardButtonView.get(whichButtonToEdit).setText(Html.fromHtml(text));
            whichButtonToEdit = -1;
        }
        // Update the board buttons
        setupViews(boardNum);

        Log.d(TAG,"newCommand:"+newCommand);
    }

    public void editTitle(String boardTitle) {
        this.boardTitle = boardTitle;
        mainActivityInterface.getPreferences().setMyPreferenceString("midiBoard"+boardNum+"Title",boardTitle);
        myView.boardTitle.setText(boardTitle);
    }

    public void editSlider(String sliderName, String midiChannel, String controllerNumber) {
        if (whichSliderToEdit==1 || whichSliderToEdit==2) {
            // Set the new name
            sliderName = sliderName.trim();
            mainActivityInterface.getPreferences().setMyPreferenceString("midiBoard" + boardNum + "Slider" + whichSliderToEdit + "Name", sliderName);
            boardSliderName.set(whichSliderToEdit,sliderName);

            // Get the channel
            midiChannel = midiChannel.replaceAll("\\D","");
            int channel = 1;
            if (!midiChannel.isEmpty()) {
                channel = Integer.parseInt(midiChannel);
            }

            // Get the controller
            controllerNumber = controllerNumber.replaceAll("\\D","");
            int controller = 0;
            if (!controllerNumber.isEmpty()) {
                controller = Integer.parseInt(controllerNumber);
            }

            mainActivityInterface.getPreferences().setMyPreferenceInt("midiBoard" + boardNum + "Slider" + whichSliderToEdit + "Channel", channel);
            boardSliderChannel.set(whichSliderToEdit,channel);
            mainActivityInterface.getPreferences().setMyPreferenceInt("midiBoard" + boardNum + "Slider" + whichSliderToEdit + "CC", controller);
            boardSliderCC.set(whichSliderToEdit,controller);

            if (whichSliderToEdit==1) {
                myView.boardSlider1.initialiseSliderValues(sliderName,channel,controller,myView.boardSlider1.getSliderVal());
            } else if (whichSliderToEdit==2) {
                myView.boardSlider2.initialiseSliderValues(sliderName,channel,controller,myView.boardSlider2.getSliderVal());
            }
            whichSliderToEdit = -1;
        }
    }
}
