package com.garethevans.church.opensongtablet.utilities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.InformationBottomSheet;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetTunerBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchProcessor;

public class TunerBottomSheet extends BottomSheetDialogFragment {

    @SuppressWarnings("unused,FieldCanBeLocal")
    private final String TAG = "TunerBottomSheet";
    private final float confidence = 0.91f;
    private final ArrayList<String> tunings = new ArrayList<>(Arrays.asList("432", "434", "436",
            "437", "438", "439", "440", "441", "442", "443", "444"));
    private final ArrayList<String> cents = new ArrayList<>(Arrays.asList("+/- 0 cent", "+/- 1 cent",
            "+/- 2 cent", "+/- 3 cent", "+/- 4 cent", "+/- 5 cent"));
    private final Handler resetTunerHandler = new Handler(Looper.myLooper());
    private final Handler turnOffAudioTrack = new Handler();
    private MainActivityInterface mainActivityInterface;
    private BottomSheetTunerBinding myView;
    private ActivityResultLauncher<String> activityResultLauncher;
    private ToneGenerator toneGenerator;
    private ArrayList<Double> midiNoteFrequency;
    private float concertPitch = 440f;
    private int centsInTune = 2;
    @SuppressWarnings("FieldCanBeLocal")
    private final int centsBand1 = 10, centsBand2 = 20, centsBand3 = 30, centsBand4 = 40;
    private String tuner_string = "", website_tuner_string = "", microphone_string = "",
            permissions_refused_string = "", settings_string = "";
    private Runnable resetTunerDisplay;
    private int needleInTuneColor = Color.GREEN, needleNotInTuneColor = Color.GRAY;
    private Runnable turnOffAudioRunnable;
    private AudioDispatcher audioDispatcher;
    private AudioProcessor audioProcessor;
    @SuppressWarnings("FieldCanBeLocal")
    private Thread audioThread;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = null;
        if (getActivity() != null) {
            w = getActivity().getWindow();
        }
        if (w != null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }

        // Set up the runnable to hide tuner values after a certain time
        resetTunerDisplay = () -> {
            if (myView != null) {
                setTunerBlocks(myView.bandFlat1, false, false);
                setTunerBlocks(myView.bandFlat2, false, false);
                setTunerBlocks(myView.bandFlat3, false, false);
                setTunerBlocks(myView.bandFlat4, false, false);
                setTunerBlocks(myView.bandSharp1, false, false);
                setTunerBlocks(myView.bandSharp2, false, false);
                setTunerBlocks(myView.bandSharp3, false, false);
                setTunerBlocks(myView.bandSharp4, false, false);
                setTunerBlocks(myView.bandInTune, false, false);
                if (myView != null) {
                    myView.tunerNote.setText("-");
                    myView.desiredFreq.setText("");
                    myView.tunerFreq.setText("");
                    myView.needle.animate()
                            .rotation(0)
                            .setDuration(500)
                            .setInterpolator(new DecelerateInterpolator()).start();
                    //myView.needle.setRotation(0);
                    myView.needle.setColorFilter(needleNotInTuneColor, PorterDuff.Mode.SRC_IN);
                }
            }
        };

        turnOffAudioRunnable = () -> {
            if (toneGenerator != null) {
                toneGenerator.stopTone();
            }
        };
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetTunerBinding.inflate(inflater, container, false);

        prepareStrings();


        myView.dialogHeader.setText(tuner_string);
        myView.dialogHeader.setClose(this);
        myView.dialogHeader.setWebHelp(mainActivityInterface, website_tuner_string);

        // Set the values
        setValues();

        // Set the listeners
        setListeners();

        // Initialise audio (and permisisons)
        checkPermissions();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext() != null) {
            tuner_string = getString(R.string.tuner);
            website_tuner_string = getString(R.string.website_tuner);
            microphone_string = getString(R.string.microphone);
            permissions_refused_string = getString(R.string.permissions_refused);
            settings_string = getString(R.string.settings);
            needleInTuneColor = ContextCompat.getColor(getContext(), R.color.green);
            needleNotInTuneColor = ContextCompat.getColor(getContext(), R.color.colorSecondary);
            toneGenerator = new ToneGenerator(getContext());
        }
    }

    private void setValues() {
        if (getContext() != null) {
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter1 = new ExposedDropDownArrayAdapter(getContext(),
                    myView.instrument, R.layout.view_exposed_dropdown_item, mainActivityInterface.getChordDisplayProcessing().getInstruments());
            myView.instrument.setAdapter(exposedDropDownArrayAdapter1);
        }
        myView.instrument.setText(instrumentPrefToText());
        myView.instrument.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                instrumentTextToPref();
                setUpTuningButtons();
            }
        });

        if (getContext() != null) {
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter2 = new ExposedDropDownArrayAdapter(getContext(),
                    myView.aHz, R.layout.view_exposed_dropdown_item, tunings);
            myView.aHz.setAdapter(exposedDropDownArrayAdapter2);
        }
        concertPitch = (float) mainActivityInterface.getPreferences().getMyPreferenceInt("refAHz", 440);
        myView.aHz.setText(String.valueOf((int) concertPitch));

        checkMidiButtons();

        myView.aHz.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Get the int value
                int value = Integer.parseInt(s.toString());
                mainActivityInterface.getPreferences().setMyPreferenceInt("refAHz", value);
                concertPitch = (float) value;
                initialiseTuner();
                checkMidiButtons();
            }
        });

        if (getContext() != null) {
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter3 = new ExposedDropDownArrayAdapter(getContext(),
                    myView.accuracy, R.layout.view_exposed_dropdown_item, cents);
            myView.accuracy.setAdapter(exposedDropDownArrayAdapter3);
        }

        int tunerCents = mainActivityInterface.getPreferences().getMyPreferenceInt("tunerCents", 2);
        myView.accuracy.setText("+/- " + tunerCents + " cent");
        getTunerCents(tunerCents);
        myView.accuracy.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Get the int value
                // Get rid of the text
                String text = s.toString().replaceAll("[^0-9]", "");
                int value = Integer.parseInt(text);
                mainActivityInterface.getPreferences().setMyPreferenceInt("tunerCents", value);
                getTunerCents(value);
            }
        });

        setUpTuningButtons();

        myView.needle.setColorFilter(needleNotInTuneColor, PorterDuff.Mode.SRC_IN);
    }

    private void checkMidiButtons() {
        // If we are set to 440Hz, the piano midi notes are fine, if not, disable them
        if (concertPitch == 440) {
            myView.pianoHolder.setVisibility(View.VISIBLE);
        } else {
            myView.pianoHolder.setVisibility(View.GONE);
        }
    }

    private void getTunerCents(int tunerCents) {
        switch (tunerCents) {
            case 0:
                centsInTune = 0;
                break;
            case 1:
                centsInTune = 1;
                break;
            case 2:
            default:
                centsInTune = 2;
                break;
            case 3:
                centsInTune = 3;
                break;
            case 4:
                centsInTune = 4;
                break;
            case 5:
                centsInTune = 5;
                break;
        }
    }

    private String instrumentPrefToText() {
        String pref = mainActivityInterface.getPreferences().getMyPreferenceString(
                "chordInstrument", "g");
        mainActivityInterface.getMidi().setMidiInstrument(pref);
        return mainActivityInterface.getChordDisplayProcessing().getInstrumentFromPref(pref);
    }

    private void instrumentTextToPref() {
        String pref = mainActivityInterface.getChordDisplayProcessing().getPrefFromInstrument(myView.instrument.getText().toString());
        mainActivityInterface.getMidi().setMidiInstrument(pref);
        mainActivityInterface.getPreferences().setMyPreferenceString(
                "chordInstrument", pref);
    }

    private void setUpTuningButtons() {
        List<String> notesArray = mainActivityInterface.getMidi().getStartNotes("standard");
        String instrument = mainActivityInterface.getPreferences().getMyPreferenceString("chordInstrument", "g");
        mainActivityInterface.getMidi().setMidiInstrument(instrument);
        switch (instrument) {
            // Notes range from 0-5 (for max 6 strings)
            case "g":
                // Used to include chord code for midi purposes, but not now:
                // setUpTuningButton(myView.note0, true, notesArray.get(0), "0xxxxx");
                setUpTuningButton(myView.note0, true, notesArray.get(0));
                setUpTuningButton(myView.note1, true, notesArray.get(1));
                setUpTuningButton(myView.note2, true, notesArray.get(2));
                setUpTuningButton(myView.note3, true, notesArray.get(3));
                setUpTuningButton(myView.note4, true, notesArray.get(4));
                setUpTuningButton(myView.note5, true, notesArray.get(5));
                break;

            case "u":
            case "b":
            case "c":
            case "m":
                setUpTuningButton(myView.note0, true, notesArray.get(0));
                setUpTuningButton(myView.note1, true, notesArray.get(1));
                setUpTuningButton(myView.note2, true, notesArray.get(2));
                setUpTuningButton(myView.note3, true, notesArray.get(3));
                setUpTuningButton(myView.note4, false, "");
                setUpTuningButton(myView.note5, false, "");
                break;

            case "B":
                setUpTuningButton(myView.note0, true, notesArray.get(0));
                setUpTuningButton(myView.note1, true, notesArray.get(1));
                setUpTuningButton(myView.note2, true, notesArray.get(2));
                setUpTuningButton(myView.note3, true, notesArray.get(3));
                setUpTuningButton(myView.note4, true, notesArray.get(4));
                setUpTuningButton(myView.note5, false, "");
                break;

            case "p":
                setUpTuningButton(myView.note0, false, "");
                setUpTuningButton(myView.note1, false, "");
                setUpTuningButton(myView.note2, false, "");
                setUpTuningButton(myView.note3, false, "");
                setUpTuningButton(myView.note4, false, "");
                setUpTuningButton(myView.note5, false, "");

        }
    }

    private void setUpTuningButton(MaterialButton button, boolean visible, String note) {
        if (myView != null) {
            if (visible) {
                button.setVisibility(View.VISIBLE);
                // Set the text only version of the note (without the octave number)
                String text = note.replaceAll("[^a-zA-Z]", "");
                button.setText(text);

                // Set the listener
                button.setOnClickListener(v -> {
                    double frequencyChosen = midiNoteFrequency.get(mainActivityInterface.getMidi().getNotes().indexOf(note) + 12);
                    prepareSineWave(frequencyChosen);
                });
            } else {
                button.setVisibility(View.GONE);
            }
        }
    }

    private void setListeners() {
        myView.piano.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // If the view has drawn, get the width and scale it to fit the bottomsheet width
                int width = myView.piano.getRoot().getWidth();
                int height = myView.piano.getRoot().getHeight();
                if (width > 0) {
                    float scale = ((float) mainActivityInterface.getDisplayMetrics()[0] - (mainActivityInterface.getDisplayDensity() * 32)) / (float) width;
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) myView.piano.getRoot().getLayoutParams();
                    layoutParams.width = (int) (width * scale);
                    layoutParams.height = (int) (height * scale);
                    myView.piano.getRoot().setLayoutParams(layoutParams);
                    myView.piano.getRoot().setPivotX(0);
                    myView.piano.getRoot().setPivotY(0);
                    myView.piano.getRoot().setScaleX(scale);
                    myView.piano.getRoot().setScaleY(scale);
                    myView.piano.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    myView.piano.getRoot().requestLayout();
                }
            }
        });
        myView.piano.c0.setOnClickListener(new PianoButton("C3"));
        myView.piano.csharp0.setOnClickListener(new PianoButton("C#3"));
        myView.piano.d0.setOnClickListener(new PianoButton("D3"));
        myView.piano.dsharp0.setOnClickListener(new PianoButton("D#3"));
        myView.piano.e0.setOnClickListener(new PianoButton("E3"));
        myView.piano.f0.setOnClickListener(new PianoButton("F3"));
        myView.piano.fsharp0.setOnClickListener(new PianoButton("F#3"));
        myView.piano.g0.setOnClickListener(new PianoButton("G3"));
        myView.piano.gsharp0.setOnClickListener(new PianoButton("G#3"));
        myView.piano.a0.setOnClickListener(new PianoButton("A3"));
        myView.piano.asharp0.setOnClickListener(new PianoButton("A#3"));
        myView.piano.b0.setOnClickListener(new PianoButton("B3"));
        myView.piano.c1.setOnClickListener(new PianoButton("C4"));
        myView.piano.csharp1.setOnClickListener(new PianoButton("C#4"));
        myView.piano.d1.setOnClickListener(new PianoButton("D4"));
        myView.piano.dsharp1.setOnClickListener(new PianoButton("D#4"));
        myView.piano.e1.setOnClickListener(new PianoButton("E4"));
        myView.piano.f1.setOnClickListener(new PianoButton("F4"));
        myView.piano.fsharp1.setOnClickListener(new PianoButton("F#4"));
        myView.piano.g1.setOnClickListener(new PianoButton("G4"));
        myView.piano.gsharp1.setOnClickListener(new PianoButton("G#4"));
        myView.piano.a1.setOnClickListener(new PianoButton("A4"));
        myView.piano.asharp1.setOnClickListener(new PianoButton("A#4"));
        myView.piano.b1.setOnClickListener(new PianoButton("B4"));
        myView.piano.c2.setOnClickListener(new PianoButton("C5"));
        myView.piano.csharp2.setOnClickListener(new PianoButton("C#5"));
        myView.piano.d2.setOnClickListener(new PianoButton("D5"));
        myView.piano.dsharp2.setOnClickListener(new PianoButton("D#5"));
        myView.piano.e2.setOnClickListener(new PianoButton("E5"));

        myView.tuner.setOnClickListener(v -> {
            if (!mainActivityInterface.getAppPermissions().hasAudioPermissions()) {
                activityResultLauncher.launch(mainActivityInterface.getAppPermissions().getAudioPermissions());
            }
        });
    }

    @SuppressLint("MissingPermission") // Checked in getAppPermissions
    private void checkPermissions() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                initialiseTuner();

            } else {
                // notify user
                InformationBottomSheet informationBottomSheet = new InformationBottomSheet(microphone_string,
                        permissions_refused_string, settings_string, "appPrefs");
                informationBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "InformationBottomSheet");

            }
        });
        activityResultLauncher.launch(mainActivityInterface.getAppPermissions().getAudioPermissions());
    }

    private void initialiseTuner() {
        myView.tunerNote.setText("-");

        midiNoteFrequency = new ArrayList<>();

        // Go through each entry from 0 to 127 and calculate the frequency for the note
        for (int i = 0; i < 127; i++) {
            double freq = (float) Math.pow(2, ((i - 69) / 12f)) * concertPitch;
            midiNoteFrequency.add(freq);
        }

        int SAMPLE_RATE = 44100;
        int BUFFER_SIZE = 1024 * 16;
        int OVERLAP = 1024 * 2;
        audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE, BUFFER_SIZE, OVERLAP);

        audioProcessor = getAudioProcessor(SAMPLE_RATE, BUFFER_SIZE);
        audioDispatcher.addAudioProcessor(audioProcessor);

        audioThread = new Thread(audioDispatcher, "Audio Thread");
        audioThread.start();
    }

    private AudioProcessor getAudioProcessor(int SAMPLE_RATE, int BUFFER_SIZE) {
        Log.d(TAG,"set pitchDetectionHandler");
        PitchDetectionHandler pitchDetectionHandler = (pitchDetectionResult, audioEvent) -> {
            float pitchHz = pitchDetectionResult.getPitch();
            float probability = pitchDetectionResult.getProbability();
            boolean isPitched = pitchDetectionResult.isPitched();
            myView.note0.post(() -> {
                if (probability > confidence && pitchHz > 30 && pitchHz < 2000 && isPitched) {
                    checkTheTuning(pitchHz);
                }
            });
        };
        return new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, SAMPLE_RATE, BUFFER_SIZE, pitchDetectionHandler);
    }

    private void checkTheTuning(float pitchHz) {
        resetTunerHandler.removeCallbacks(resetTunerDisplay);
        resetTunerHandler.postDelayed(resetTunerDisplay, 1000);

        int foundNote = -1;

        String freqText = (float) Math.round(pitchHz * 10f) / 10f + "Hz";

        // Get the current note cents.  Less than 50 means we know the closest note
        int currentCents;
        int foundCents = 0;
        for (int i = 0; i < midiNoteFrequency.size(); i++) {
            currentCents = (int) Math.round(1200f * (Math.log((double)pitchHz / midiNoteFrequency.get(i))));
            if (Math.abs(currentCents) < 50) {
                // Note detected, so no need to continue the loop
                // Decide if this or the next in the array is closer
                int nextCents = currentCents;
                if (i < midiNoteFrequency.size() - 1) {
                    nextCents = (int) Math.round(1200f * (Math.log((double)pitchHz / midiNoteFrequency.get(i + 1))));
                }

                if (Math.abs(currentCents) < Math.abs(nextCents)) {
                    foundCents = currentCents;
                    foundNote = i;
                } else {
                    foundCents = nextCents;
                    foundNote = i + 1;
                }
                // 50 cents should be possible, but Tarsos gives value of 35 for what should be 50
                // Multiply by 50/35.  Make sure it is always less than 50 though just in case!
                foundCents = Math.min(50,Math.round((float)foundCents * (50f/35f)));
                break;
            }
        }

        if (foundNote >= 0 && Math.abs(foundCents) < 50) {
            // We can now update the display
            boolean isSharp = false, isFlat = false, inTune = false, closeInTune = false;
            int band;
            if (Math.abs(foundCents) >= centsBand4) {
                band = 4;
            } else if (Math.abs(foundCents) >= centsBand3) {
                band = 3;
            } else if (Math.abs(foundCents) >= centsBand2) {
                band = 2;
            } else if (Math.abs(foundCents) >= centsBand1) {
                band = 1;
            } else if (Math.abs(foundCents) > centsInTune) {
                band = 1;
                closeInTune = true;
            } else {
                band = 0;
            }

            if (foundCents > centsInTune) {
                isSharp = true;
            } else if (foundCents < -centsInTune) {
                isFlat = true;
            } else {
                inTune = true;
            }

            myView.needle.animate()
                    // We want 60 degree rotation for 50 cents on the dial. (60/50) = 1.2
                    .rotation(foundCents * 1.2f)
                    .setDuration(500)
                    .setInterpolator(new DecelerateInterpolator()).start();

            myView.needle.setColorFilter(inTune ? needleInTuneColor : needleNotInTuneColor, PorterDuff.Mode.SRC_IN);
            String noteText = mainActivityInterface.getMidi().getNoteFromInt(foundNote - 12).replaceAll("", "");
            String freqReq = (Math.round((midiNoteFrequency.get(foundNote) * 10)) / 10f) + "Hz";
            myView.tunerNote.setText(noteText);
            myView.tunerFreq.setText(freqText);
            myView.desiredFreq.setText(freqReq);
            setTunerBlocks(myView.bandFlat4, isFlat && band == 4, false);
            setTunerBlocks(myView.bandFlat3, isFlat && band >= 3, false);
            setTunerBlocks(myView.bandFlat2, isFlat && band >= 2, false);
            setTunerBlocks(myView.bandFlat1, isFlat && band >= 1, false);
            setTunerBlocks(myView.bandInTune, (inTune && band == 0) || closeInTune, true);
            setTunerBlocks(myView.bandSharp1, isSharp && band >= 1, false);
            setTunerBlocks(myView.bandSharp2, isSharp && band >= 2, false);
            setTunerBlocks(myView.bandSharp3, isSharp && band >= 3, false);
            setTunerBlocks(myView.bandSharp4, isSharp && band == 4, false);

        } else {
            myView.tunerNote.setText("-");
            setTunerBlocks(myView.bandFlat4, false, false);
            setTunerBlocks(myView.bandFlat3, false, false);
            setTunerBlocks(myView.bandFlat2, false, false);
            setTunerBlocks(myView.bandFlat1, false, false);
            setTunerBlocks(myView.bandInTune, false, false);
            setTunerBlocks(myView.bandSharp1, false, false);
            setTunerBlocks(myView.bandSharp2, false, false);
            setTunerBlocks(myView.bandSharp3, false, false);
            setTunerBlocks(myView.bandSharp4, false, false);
        }
    }

    private void setTunerBlocks(ImageView view, boolean isOn, boolean green) {
        if (getContext() != null) {
            if (green && isOn) {
                view.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.tuner_in_tune, null));
            } else if (isOn) {
                view.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.tuner_block_on, null));
            } else {
                view.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.tuner_block_off, null));
            }
        }
    }

    private void prepareSineWave(double freqOfTone) {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            if (toneGenerator != null && !toneGenerator.getIsPlaying()) {
                turnOffAudioTrack.removeCallbacks(turnOffAudioRunnable);
                try {
                    turnOffAudioTrack.postDelayed(turnOffAudioRunnable, 2000);
                    toneGenerator.startTone(freqOfTone);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (toneGenerator != null) {
            toneGenerator.stopTone();
            toneGenerator.nullAudioTrack();
            toneGenerator = null;
        }
        if (audioDispatcher != null) {
            try {
                audioDispatcher.stop();
                audioDispatcher = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        audioProcessor = null;
    }

    private class PianoButton implements View.OnClickListener {

        String note;

        PianoButton(String note) {
            this.note = note;
        }

        @Override
        public void onClick(View v) {
            if (note.contains("#")) {
                ((ImageView) v).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.piano_note_black_on, null));
                v.postDelayed(() -> ((ImageView) v).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.piano_note_black, null)), 300);
            } else {
                ((ImageView) v).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.piano_note_white_on, null));
                v.postDelayed(() -> ((ImageView) v).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.piano_note_white, null)), 300);
            }
            mainActivityInterface.getMidi().setUsePianoNotes(true);
            mainActivityInterface.getMidi().playMidiNotes(note, "standard", 100, 0);
        }
    }
}
