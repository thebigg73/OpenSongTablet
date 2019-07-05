package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class PopUpCustomChordsFragment extends DialogFragment {

    static PopUpCustomChordsFragment newInstance() {
        PopUpCustomChordsFragment frag;
        frag = new PopUpCustomChordsFragment();
        return frag;
    }

    public interface MyInterface {
        void openFragment();
        void pageButtonAlpha(String s);
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

    private AsyncTask<Object,Void,String> prepare_custom;
    private String string6_text = "x";
    private String string5_text = "x";
    private String string4_text = "x";
    private String string3_text = "x";
    private String string2_text = "x";
    private String string1_text = "x";
    private String fret_text = "0";
    private String instrument_text = "g";
    private String chord_text = "xxxxxx";

    private boolean string6_O = false, string5_O = false, string4_O = false, string3_O = false,
            string2_O = false, string1_O = false, string6_X = true, string5_X = true,
            string4_X = true, string3_X = true, string2_X = true, string1_X = true,
            string6_f1_on = false, string6_f2_on = false, string6_f3_on = false,
            string6_f4_on = false, string6_f5_on = false, string5_f1_on = false,
            string5_f2_on = false, string5_f3_on = false, string5_f4_on = false,
            string5_f5_on = false, string4_f1_on = false, string4_f2_on = false,
            string4_f3_on = false, string4_f4_on = false, string4_f5_on = false,
            string3_f1_on = false, string3_f2_on = false, string3_f3_on = false,
            string3_f4_on = false, string3_f5_on = false, string2_f1_on = false,
            string2_f2_on = false, string2_f3_on = false, string2_f4_on = false,
            string2_f5_on = false, string1_f1_on = false, string1_f2_on = false,
            string1_f3_on = false, string1_f4_on = false, string1_f5_on = false;
    private Drawable stringtop, stringtop_X, stringtop_O, string6, string5, string4, string3, string2,
            string1, string6_on, string5_on, string4_on, string3_on, string2_on, string1_on;
    private LinearLayout savedcustomchords;
    private Spinner customchords_fret;
    private EditText customchord_name;
    private TextView customchord_code;
    private ImageView string6_top, string5_top, string4_top, string3_top, string2_top, string1_top,
            string6_f1, string5_f1, string4_f1, string3_f1, string2_f1, string1_f1,
            string6_f2, string5_f2, string4_f2, string3_f2, string2_f2, string1_f2,
            string6_f3, string5_f3, string4_f3, string3_f3, string2_f3, string1_f3,
            string6_f4, string5_f4, string4_f4, string3_f4, string2_f4, string1_f4,
            string6_f5, string5_f5, string4_f5, string3_f5, string2_f5, string1_f5;

    StorageAccess storageAccess;
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
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (mListener!=null) {
            mListener.pageButtonAlpha("chords");
        }

        View V = inflater.inflate(R.layout.popup_customchords, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.customchords));
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

        storageAccess = new StorageAccess();
        preferences = new Preferences();

        // Initialise the views
        stringtop = getActivity().getResources().getDrawable(R.drawable.string_top);
        stringtop_X = getActivity().getResources().getDrawable(R.drawable.string_top_x);
        stringtop_O = getActivity().getResources().getDrawable(R.drawable.string_top_o);
        string6 = getActivity().getResources().getDrawable(R.drawable.string_6);
        string6_on = getActivity().getResources().getDrawable(R.drawable.string_6_on);
        string5 = getActivity().getResources().getDrawable(R.drawable.string_5);
        string5_on = getActivity().getResources().getDrawable(R.drawable.string_5_on);
        string4 = getActivity().getResources().getDrawable(R.drawable.string_4);
        string4_on = getActivity().getResources().getDrawable(R.drawable.string_4_on);
        string3 = getActivity().getResources().getDrawable(R.drawable.string_3);
        string3_on = getActivity().getResources().getDrawable(R.drawable.string_3_on);
        string2 = getActivity().getResources().getDrawable(R.drawable.string_2);
        string2_on = getActivity().getResources().getDrawable(R.drawable.string_2_on);
        string1 = getActivity().getResources().getDrawable(R.drawable.string_1);
        string1_on = getActivity().getResources().getDrawable(R.drawable.string_1_on);

        Spinner customchords_instrument = V.findViewById(R.id.customchords_instrument);
        customchords_fret = V.findViewById(R.id.customchords_fret);
        customchord_name = V.findViewById(R.id.customchord_name);
        customchord_code = V.findViewById(R.id.customchord_code);
        Button customChordSave = V.findViewById(R.id.customChordSave);
        savedcustomchords = V.findViewById(R.id.savedcustomchords);
        string6_top = V.findViewById(R.id.string6_top);
        string5_top = V.findViewById(R.id.string5_top);
        string4_top = V.findViewById(R.id.string4_top);
        string3_top = V.findViewById(R.id.string3_top);
        string2_top = V.findViewById(R.id.string2_top);
        string1_top = V.findViewById(R.id.string1_top);
        string6_f1 = V.findViewById(R.id.string6_f1);
        string5_f1 = V.findViewById(R.id.string5_f1);
        string4_f1 = V.findViewById(R.id.string4_f1);
        string3_f1 = V.findViewById(R.id.string3_f1);
        string2_f1 = V.findViewById(R.id.string2_f1);
        string1_f1 = V.findViewById(R.id.string1_f1);
        string6_f2 = V.findViewById(R.id.string6_f2);
        string5_f2 = V.findViewById(R.id.string5_f2);
        string4_f2 = V.findViewById(R.id.string4_f2);
        string3_f2 = V.findViewById(R.id.string3_f2);
        string2_f2 = V.findViewById(R.id.string2_f2);
        string1_f2 = V.findViewById(R.id.string1_f2);
        string6_f3 = V.findViewById(R.id.string6_f3);
        string5_f3 = V.findViewById(R.id.string5_f3);
        string4_f3 = V.findViewById(R.id.string4_f3);
        string3_f3 = V.findViewById(R.id.string3_f3);
        string2_f3 = V.findViewById(R.id.string2_f3);
        string1_f3 = V.findViewById(R.id.string1_f3);
        string6_f4 = V.findViewById(R.id.string6_f4);
        string5_f4 = V.findViewById(R.id.string5_f4);
        string4_f4 = V.findViewById(R.id.string4_f4);
        string3_f4 = V.findViewById(R.id.string3_f4);
        string2_f4 = V.findViewById(R.id.string2_f4);
        string1_f4 = V.findViewById(R.id.string1_f4);
        string6_f5 = V.findViewById(R.id.string6_f5);
        string5_f5 = V.findViewById(R.id.string5_f5);
        string4_f5 = V.findViewById(R.id.string4_f5);
        string3_f5 = V.findViewById(R.id.string3_f5);
        string2_f5 = V.findViewById(R.id.string2_f5);
        string1_f5 = V.findViewById(R.id.string1_f5);

        ArrayList<String> instrument_choice = new ArrayList<>();
        instrument_choice.add(getActivity().getResources().getString(R.string.guitar));
        instrument_choice.add(getActivity().getResources().getString(R.string.ukulele));
        instrument_choice.add(getActivity().getResources().getString(R.string.mandolin));
        instrument_choice.add(getActivity().getResources().getString(R.string.cavaquinho));
        instrument_choice.add(getActivity().getResources().getString(R.string.banjo4));
        instrument_choice.add(getActivity().getResources().getString(R.string.banjo5));
        ArrayAdapter<String> adapter_instrument = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, instrument_choice);
        adapter_instrument.setDropDownViewResource(R.layout.my_spinner);
        customchords_instrument.setAdapter(adapter_instrument);

        // Set the current instrument
        instrument_text = preferences.getMyPreferenceString(getActivity(),"chordInstrument","g");
        switch (instrument_text) {
            case "g":
                customchords_instrument.setSelection(0);
                set6String();
                break;
            case "u":
                customchords_instrument.setSelection(1);
                set4String();
                break;
            case "m":
                customchords_instrument.setSelection(2);
                set4String();
                break;
            case "c":
                customchords_instrument.setSelection(3);
                set4String();
                break;
            case "b":
                customchords_instrument.setSelection(4);
                set4String();
                break;
            case "B":
                customchords_instrument.setSelection(5);
                set5String();
                break;
        }

        customchords_instrument.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        instrument_text = "g";
                        set6String();
                        break;
                    case 1:
                        instrument_text = "u";
                        set4String();
                        break;
                    case 2:
                        instrument_text = "m";
                        set4String();
                        break;
                    case 3:
                        instrument_text = "c";
                        set4String();
                        break;
                    case 4:
                        instrument_text = "b";
                        set4String();
                        break;
                    case 5:
                        instrument_text = "B";
                        set5String();
                        break;
                }
                preferences.setMyPreferenceString(getActivity(),"chordInstrument",instrument_text);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Prepare the listeners
        string6_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string6_top();
            }
        });
        string6_f1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string6_f1();
            }
        });
        string6_f2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string6_f2();
            }
        });
        string6_f3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string6_f3();
            }
        });
        string6_f4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string6_f4();
            }
        });
        string6_f5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string6_f5();
            }
        });
        string5_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string5_top();
            }
        });
        string5_f1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string5_f1();
            }
        });
        string5_f2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string5_f2();
            }
        });
        string5_f3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string5_f3();
            }
        });
        string5_f4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string5_f4();
            }
        });
        string5_f5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string5_f5();
            }
        });
        string4_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string4_top();
            }
        });
        string4_f1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string4_f1();
            }
        });
        string4_f2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string4_f2();
            }
        });
        string4_f3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string4_f3();
            }
        });
        string4_f4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string4_f4();
            }
        });
        string4_f5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string4_f5();
            }
        });
        string3_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string3_top();
            }
        });
        string3_f1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string3_f1();
            }
        });
        string3_f2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string3_f2();
            }
        });
        string3_f3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string3_f3();
            }
        });
        string3_f4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string3_f4();
            }
        });
        string3_f5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string3_f5();
            }
        });
        string2_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string2_top();
            }
        });
        string2_f1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string2_f1();
            }
        });
        string2_f2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string2_f2();
            }
        });
        string2_f3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string2_f3();
            }
        });
        string2_f4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string2_f4();
            }
        });
        string2_f5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string2_f5();
            }
        });
        string1_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string1_top();
            }
        });
        string1_f1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string1_f1();
            }
        });
        string1_f2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string1_f2();
            }
        });
        string1_f3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string1_f3();
            }
        });
        string1_f4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string1_f4();
            }
        });
        string1_f5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                string1_f5();
            }
        });

        customChordSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customChordSave();
            }
        });

        String[] fret_choice = {"","1","2","3","4","5","6","7","8","9"};
        ArrayAdapter<String> adapter_fret = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, fret_choice);
        adapter_fret.setDropDownViewResource(R.layout.my_spinner);
        customchords_fret.setAdapter(adapter_fret);
        customchords_fret.setOnItemSelectedListener(new FretListener());
        prepareCustomChords();
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void prepareCustomChords() {
        prepare_custom = new PrepareCustom();
        try {
            prepare_custom.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d","Custom chord fragment error");
        }
    }
    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class PrepareCustom extends AsyncTask<Object,Void,String> {

        String[] tempCustomChords;
        int numcustomchords;

        @Override
        protected void onPreExecute() {
            savedcustomchords.removeAllViews();
        }

        @Override
        protected String doInBackground(Object... objects) {

            //Get rid of excessive spaces
            StaticVariables.mCustomChords = StaticVariables.mCustomChords.trim();
            while (StaticVariables.mCustomChords.contains("  ")) {
                StaticVariables.mCustomChords = StaticVariables.mCustomChords.replace("  ", " ");
            }
            tempCustomChords = StaticVariables.mCustomChords.split(" ");
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (tempCustomChords.length > 0) {
                numcustomchords = tempCustomChords.length;
                for (int q = 0; q < numcustomchords; q++) {
                    String workingChord = tempCustomChords[q];
                    TextView chordvalue = new TextView(getActivity());
                    Button deleteChord = new Button(getActivity());
                    String chorddetails;
                    if (tempCustomChords[q].contains("_u")) {
                        chorddetails = getResources().getString(R.string.ukulele) + "\n";
                        workingChord = workingChord.replace("_u", "");
                    } else if (tempCustomChords[q].contains("_m")) {
                        chorddetails = getResources().getString(R.string.mandolin) + "\n";
                        workingChord = workingChord.replace("_m", "");
                    } else if (tempCustomChords[q].contains("_c")) {
                        chorddetails = getResources().getString(R.string.cavaquinho) + "\n";
                        workingChord = workingChord.replace("_c", "");
                    } else if (tempCustomChords[q].contains("_b")) {
                        chorddetails = getResources().getString(R.string.banjo4) + "\n";
                        workingChord = workingChord.replace("_b", "");
                    } else if (tempCustomChords[q].contains("_B")) {
                        chorddetails = getResources().getString(R.string.banjo5) + "\n";
                        workingChord = workingChord.replace("_B", "");
                    } else {
                        chorddetails = getResources().getString(R.string.guitar) + "\n";
                        workingChord = workingChord.replace("_g", "");
                    }
                    if (tempCustomChords[q].contains("_0")) {
                        chorddetails = chorddetails + getResources().getString(R.string.customchords_fret) + " = 0\n";
                        workingChord = workingChord.replace("_0", "");
                    } else if (tempCustomChords[q].contains("_1")) {
                        chorddetails = chorddetails + getResources().getString(R.string.customchords_fret) + " = 1\n";
                        workingChord = workingChord.replace("_1", "");
                    } else if (tempCustomChords[q].contains("_2")) {
                        chorddetails = chorddetails + getResources().getString(R.string.customchords_fret) + " = 2\n";
                        workingChord = workingChord.replace("_2", "");
                    } else if (tempCustomChords[q].contains("_3")) {
                        chorddetails = chorddetails + getResources().getString(R.string.customchords_fret) + " = 3\n";
                        workingChord = workingChord.replace("_3", "");
                    } else if (tempCustomChords[q].contains("_4")) {
                        chorddetails = chorddetails + getResources().getString(R.string.customchords_fret) + " = 4\n";
                        workingChord = workingChord.replace("_4", "");
                    } else if (tempCustomChords[q].contains("_5")) {
                        chorddetails = chorddetails + getResources().getString(R.string.customchords_fret) + " = 5\n";
                        workingChord = workingChord.replace("_5", "");
                    } else if (tempCustomChords[q].contains("_6")) {
                        chorddetails = chorddetails + getResources().getString(R.string.customchords_fret) + " = 6\n";
                        workingChord = workingChord.replace("_6", "");
                    } else if (tempCustomChords[q].contains("_7")) {
                        chorddetails = chorddetails + getResources().getString(R.string.customchords_fret) + " = 7\n";
                        workingChord = workingChord.replace("_7", "");
                    } else if (tempCustomChords[q].contains("_8")) {
                        chorddetails = chorddetails + getResources().getString(R.string.customchords_fret) + " = 8\n";
                        workingChord = workingChord.replace("_8", "");
                    } else if (tempCustomChords[q].contains("_9")) {
                        chorddetails = chorddetails + getResources().getString(R.string.customchords_fret) + " = 9\n";
                        workingChord = workingChord.replace("_9", "");
                    }

                    // Try to get the chord name
                    String chordname = "";
                    int startpos = workingChord.lastIndexOf("_") + 1;
                    int endpos = workingChord.length();
                    if (startpos > 0 && endpos > startpos) {
                        chordname = workingChord.substring(startpos, endpos);
                        workingChord = workingChord.replace(chordname, "");
                        workingChord = workingChord.replace("_", "");
                    }

                    chorddetails = chorddetails + chordname + " (" + workingChord + ")";
                    String newtext = "\n\n" + chorddetails;
                    chordvalue.setText(newtext);
                    deleteChord.setTransformationMethod(null);
                    newtext = getResources().getString(R.string.options_song_delete) + "\n" + tempCustomChords[q];
                    deleteChord.setText(newtext);
                    deleteChord.setBackgroundDrawable(getResources().getDrawable(R.drawable.red_button));
                    deleteChord.setOnClickListener(new OnDelete(deleteChord));
                    if (!workingChord.isEmpty()) {
                        savedcustomchords.addView(chordvalue);
                        savedcustomchords.addView(deleteChord);
                    }
                }
            }
        }
    }

    private class FretListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            fret_text = customchords_fret.getItemAtPosition(position).toString();

            if (fret_text.equals("") || fret_text.isEmpty()) {
                fret_text = "0";
            }
            updateChordText();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.e("popupChord", "Nothing selected");
        }
    }

    //This is the code to handle the user clicking on the strings/frets
    private void string6_top () {
        resetString6Drawables();
        if (string6_O) {
            string6_O = false;
            string6_X = true;
            string6_text = "x";
            string6_top.setImageDrawable(stringtop_X);
        } else if (string6_X) {
            string6_O = true;
            string6_X = false;
            string6_text = "0";
            string6_top.setImageDrawable(stringtop_O);
        }
        resetString6Values();
        updateChordText();
    }

    private void string6_f1 () {
        resetString6Drawables();
        if (string6_f1_on) {
            string6_f1_on = false;
            string6_text="x";
            string6_f1.setImageDrawable(string6);
            string6_top.setImageDrawable(stringtop_X);
        } else {
            resetString6Values();
            string6_f1_on = true;
            string6_f1.setImageDrawable(string6_on);
            string6_top.setImageDrawable(stringtop);
            string6_text="1";
        }
        updateChordText();
    }

    private void string6_f2 () {
        resetString6Drawables();
        if (string6_f2_on) {
            string6_f2_on = false;
            string6_text="x";
            string6_f2.setImageDrawable(string6);
            string6_top.setImageDrawable(stringtop_X);
        } else {
            resetString6Values();
            string6_f2_on = true;
            string6_f2.setImageDrawable(string6_on);
            string6_top.setImageDrawable(stringtop);
            string6_text="2";
        }
        updateChordText();
    }

    private void string6_f3 () {
        resetString6Drawables();
        if (string6_f3_on) {
            string6_f3_on = false;
            string6_text="x";
            string6_f3.setImageDrawable(string6);
            string6_top.setImageDrawable(stringtop_X);
        } else {
            resetString6Values();
            string6_f3_on = true;
            string6_f3.setImageDrawable(string6_on);
            string6_top.setImageDrawable(stringtop);
            string6_text="3";
        }
        updateChordText();
    }

    private void string6_f4 () {
        resetString6Drawables();
        if (string6_f4_on) {
            string6_f4_on = false;
            string6_text="x";
            string6_f4.setImageDrawable(string6);
            string6_top.setImageDrawable(stringtop_X);
        } else {
            resetString6Values();
            string6_f4_on = true;
            string6_f4.setImageDrawable(string6_on);
            string6_top.setImageDrawable(stringtop);
            string6_text="4";
        }
        updateChordText();
    }

    private void string6_f5 () {
        resetString6Drawables();
        if (string6_f5_on) {
            string6_f5_on = false;
            string6_text="x";
            string6_f5.setImageDrawable(string6);
            string6_top.setImageDrawable(stringtop_X);
        } else {
            resetString6Values();
            string6_f5_on = true;
            string6_f5.setImageDrawable(string6_on);
            string6_top.setImageDrawable(stringtop);
            string6_text="5";
        }
        updateChordText();
    }

    private void string5_top () {
        resetString5Drawables();
        if (string5_O) {
            string5_O = false;
            string5_X = true;
            string5_text = "x";
            string5_top.setImageDrawable(stringtop_X);
        } else if (string5_X) {
            string5_O = true;
            string5_X = false;
            string5_text = "0";
            string5_top.setImageDrawable(stringtop_O);
        }
        resetString5Values();
        updateChordText();
    }

    private void string5_f1 () {
        resetString5Drawables();
        if (string5_f1_on) {
            string5_f1_on = false;
            string5_text="x";
            string5_f1.setImageDrawable(string5);
            string5_top.setImageDrawable(stringtop_X);
        } else {
            resetString5Values();
            string5_f1_on = true;
            string5_f1.setImageDrawable(string5_on);
            string5_top.setImageDrawable(stringtop);
            string5_text="1";
        }
        updateChordText();
    }

    private void string5_f2 () {
        resetString5Drawables();
        if (string5_f2_on) {
            string5_f2_on = false;
            string5_text="x";
            string5_f2.setImageDrawable(string5);
            string5_top.setImageDrawable(stringtop_X);
        } else {
            resetString5Values();
            string5_f2_on = true;
            string5_f2.setImageDrawable(string5_on);
            string5_top.setImageDrawable(stringtop);
            string5_text="2";
        }
        updateChordText();
    }

    private void string5_f3 () {
        resetString5Drawables();
        if (string5_f3_on) {
            string5_f3_on = false;
            string5_text="x";
            string5_f3.setImageDrawable(string5);
            string5_top.setImageDrawable(stringtop_X);
        } else {
            resetString5Values();
            string5_f3_on = true;
            string5_f3.setImageDrawable(string5_on);
            string5_top.setImageDrawable(stringtop);
            string5_text="3";
        }
        updateChordText();
    }

    private void string5_f4 () {
        resetString5Drawables();
        if (string5_f4_on) {
            string5_f4_on = false;
            string5_text="x";
            string5_f4.setImageDrawable(string5);
            string5_top.setImageDrawable(stringtop_X);
        } else {
            resetString5Values();
            string5_f4_on = true;
            string5_f4.setImageDrawable(string5_on);
            string5_top.setImageDrawable(stringtop);
            string5_text="4";
        }
        updateChordText();
    }

    private void string5_f5 () {
        resetString5Drawables();
        if (string5_f5_on) {
            string5_f5_on = false;
            string5_text="x";
            string5_f5.setImageDrawable(string5);
            string5_top.setImageDrawable(stringtop_X);
        } else {
            resetString5Values();
            string5_f5_on = true;
            string5_f5.setImageDrawable(string5_on);
            string5_top.setImageDrawable(stringtop);
            string5_text="5";
        }
        updateChordText();
    }

    private void string4_top () {
        resetString4Drawables();
        if (string4_O) {
            string4_O = false;
            string4_X = true;
            string4_text = "x";
            string4_top.setImageDrawable(stringtop_X);
        } else if (string4_X) {
            string4_O = true;
            string4_X = false;
            string4_text = "0";
            string4_top.setImageDrawable(stringtop_O);
        }
        resetString4Values();
        updateChordText();
    }

    private void string4_f1 () {
        resetString4Drawables();
        if (string4_f1_on) {
            string4_f1_on = false;
            string4_text="x";
            string4_f1.setImageDrawable(string4);
            string4_top.setImageDrawable(stringtop_X);
        } else {
            resetString4Values();
            string4_f1_on = true;
            string4_f1.setImageDrawable(string4_on);
            string4_top.setImageDrawable(stringtop);
            string4_text="1";
        }
        updateChordText();
    }

    private void string4_f2 () {
        resetString4Drawables();
        if (string4_f2_on) {
            string4_f2_on = false;
            string4_text="x";
            string4_f2.setImageDrawable(string4);
            string4_top.setImageDrawable(stringtop_X);
        } else {
            resetString4Values();
            string4_f2_on = true;
            string4_f2.setImageDrawable(string4_on);
            string4_top.setImageDrawable(stringtop);
            string4_text="2";
        }
        updateChordText();
    }

    private void string4_f3 () {
        resetString4Drawables();
        if (string4_f3_on) {
            string4_f3_on = false;
            string4_text="x";
            string4_f3.setImageDrawable(string4);
            string4_top.setImageDrawable(stringtop_X);
        } else {
            resetString4Values();
            string4_f3_on = true;
            string4_f3.setImageDrawable(string4_on);
            string4_top.setImageDrawable(stringtop);
            string4_text="3";
        }
        updateChordText();
    }

    private void string4_f4 () {
        resetString4Drawables();
        if (string4_f4_on) {
            string4_f4_on = false;
            string4_text="x";
            string4_f4.setImageDrawable(string4);
            string4_top.setImageDrawable(stringtop_X);
        } else {
            resetString4Values();
            string4_f4_on = true;
            string4_f4.setImageDrawable(string4_on);
            string4_top.setImageDrawable(stringtop);
            string4_text="4";
        }
        updateChordText();
    }

    private void string4_f5 () {
        resetString4Drawables();
        if (string4_f5_on) {
            string4_f5_on = false;
            string4_text="x";
            string4_f5.setImageDrawable(string4);
            string4_top.setImageDrawable(stringtop_X);
        } else {
            resetString4Values();
            string4_f5_on = true;
            string4_f5.setImageDrawable(string4_on);
            string4_top.setImageDrawable(stringtop);
            string4_text="5";
        }
        updateChordText();
    }

    private void string3_top () {
        resetString3Drawables();
        if (string3_O) {
            string3_O = false;
            string3_X = true;
            string3_text = "x";
            string3_top.setImageDrawable(stringtop_X);
        } else if (string3_X) {
            string3_O = true;
            string3_X = false;
            string3_text = "0";
            string3_top.setImageDrawable(stringtop_O);
        }
        resetString3Values();
        updateChordText();
    }

    private void string3_f1 () {
        resetString3Drawables();
        if (string3_f1_on) {
            string3_f1_on = false;
            string3_text="x";
            string3_f1.setImageDrawable(string3);
            string3_top.setImageDrawable(stringtop_X);
        } else {
            resetString3Values();
            string3_f1_on = true;
            string3_f1.setImageDrawable(string3_on);
            string3_top.setImageDrawable(stringtop);
            string3_text="1";
        }
        updateChordText();
    }

    private void string3_f2 () {
        resetString3Drawables();
        if (string3_f2_on) {
            string3_f2_on = false;
            string3_text="x";
            string3_f2.setImageDrawable(string3);
            string3_top.setImageDrawable(stringtop_X);
        } else {
            resetString3Values();
            string3_f2_on = true;
            string3_f2.setImageDrawable(string3_on);
            string3_top.setImageDrawable(stringtop);
            string3_text="2";
        }
        updateChordText();
    }

    private void string3_f3 () {
        resetString3Drawables();
        if (string3_f3_on) {
            string3_f3_on = false;
            string3_text="x";
            string3_f3.setImageDrawable(string3);
            string3_top.setImageDrawable(stringtop_X);
        } else {
            resetString3Values();
            string3_f3_on = true;
            string3_f3.setImageDrawable(string3_on);
            string3_top.setImageDrawable(stringtop);
            string3_text="3";
        }
        updateChordText();
    }

    private void string3_f4 () {
        resetString3Drawables();
        if (string3_f4_on) {
            string3_f4_on = false;
            string3_text="x";
            string3_f4.setImageDrawable(string3);
            string3_top.setImageDrawable(stringtop_X);
        } else {
            resetString3Values();
            string3_f4_on = true;
            string3_f4.setImageDrawable(string3_on);
            string3_top.setImageDrawable(stringtop);
            string3_text="4";
        }
        updateChordText();
    }

    private void string3_f5 () {
        resetString3Drawables();
        if (string3_f5_on) {
            string3_f5_on = false;
            string3_text="x";
            string3_f5.setImageDrawable(string3);
            string3_top.setImageDrawable(stringtop_X);
        } else {
            resetString3Values();
            string3_f5_on = true;
            string3_f5.setImageDrawable(string3_on);
            string3_top.setImageDrawable(stringtop);
            string3_text="5";
        }
        updateChordText();
    }

    private void string2_top () {
        resetString2Drawables();
        if (string2_O) {
            string2_O = false;
            string2_X = true;
            string2_text = "x";
            string2_top.setImageDrawable(stringtop_X);
        } else if (string2_X) {
            string2_O = true;
            string2_X = false;
            string2_text = "0";
            string2_top.setImageDrawable(stringtop_O);
        }
        resetString2Values();
        updateChordText();
    }

    private void string2_f1 () {
        resetString2Drawables();
        if (string2_f1_on) {
            string2_f1_on = false;
            string2_text="x";
            string2_f1.setImageDrawable(string2);
            string2_top.setImageDrawable(stringtop_X);
        } else {
            resetString2Values();
            string2_f1_on = true;
            string2_f1.setImageDrawable(string2_on);
            string2_top.setImageDrawable(stringtop);
            string2_text="1";
        }
        updateChordText();
    }

    private void string2_f2 () {
        resetString2Drawables();
        if (string2_f2_on) {
            string2_f2_on = false;
            string2_text="x";
            string2_f2.setImageDrawable(string2);
            string2_top.setImageDrawable(stringtop_X);
        } else {
            resetString2Values();
            string2_f2_on = true;
            string2_f2.setImageDrawable(string2_on);
            string2_top.setImageDrawable(stringtop);
            string2_text="2";
        }
        updateChordText();
    }

    private void string2_f3 () {
        resetString2Drawables();
        if (string2_f3_on) {
            string2_f3_on = false;
            string2_text="x";
            string2_f3.setImageDrawable(string2);
            string2_top.setImageDrawable(stringtop_X);
        } else {
            resetString2Values();
            string2_f3_on = true;
            string2_f3.setImageDrawable(string2_on);
            string2_top.setImageDrawable(stringtop);
            string2_text="3";
        }
        updateChordText();
    }

    private void string2_f4 () {
        resetString2Drawables();
        if (string2_f4_on) {
            string2_f4_on = false;
            string2_text="x";
            string2_f4.setImageDrawable(string2);
            string2_top.setImageDrawable(stringtop_X);
        } else {
            resetString2Values();
            string2_f4_on = true;
            string2_f4.setImageDrawable(string2_on);
            string2_top.setImageDrawable(stringtop);
            string2_text="4";
        }
        updateChordText();
    }

    private void string2_f5 () {
        resetString2Drawables();
        if (string2_f5_on) {
            string2_f5_on = false;
            string2_text="x";
            string2_f5.setImageDrawable(string2);
            string2_top.setImageDrawable(stringtop_X);
        } else {
            resetString2Values();
            string2_f5_on = true;
            string2_f5.setImageDrawable(string2_on);
            string2_top.setImageDrawable(stringtop);
            string2_text="5";
        }
        updateChordText();
    }

    private void string1_top () {
        resetString1Drawables();
        if (string1_O) {
            string1_O = false;
            string1_X = true;
            string1_text = "x";
            string1_top.setImageDrawable(stringtop_X);
        } else if (string1_X) {
            string1_O = true;
            string1_X = false;
            string1_text = "0";
            string1_top.setImageDrawable(stringtop_O);
        }
        resetString1Values();
        updateChordText();
    }

    private void string1_f1 () {
        resetString1Drawables();
        if (string1_f1_on) {
            string1_f1_on = false;
            string1_text="x";
            string1_f1.setImageDrawable(string1);
            string1_top.setImageDrawable(stringtop_X);
        } else {
            resetString1Values();
            string1_f1_on = true;
            string1_f1.setImageDrawable(string1_on);
            string1_top.setImageDrawable(stringtop);
            string1_text="1";
        }
        updateChordText();
    }

    private void string1_f2 () {
        resetString1Drawables();
        if (string1_f2_on) {
            string1_f2_on = false;
            string1_text="x";
            string1_f2.setImageDrawable(string1);
            string1_top.setImageDrawable(stringtop_X);
        } else {
            resetString1Values();
            string1_f2_on = true;
            string1_f2.setImageDrawable(string1_on);
            string1_top.setImageDrawable(stringtop);
            string1_text="2";
        }
        updateChordText();
    }

    private void string1_f3 () {
        resetString1Drawables();
        if (string1_f3_on) {
            string1_f3_on = false;
            string1_text="x";
            string1_f3.setImageDrawable(string1);
            string1_top.setImageDrawable(stringtop_X);
        } else {
            resetString1Values();
            string1_f3_on = true;
            string1_f3.setImageDrawable(string1_on);
            string1_top.setImageDrawable(stringtop);
            string1_text="3";
        }
        updateChordText();
    }

    private void string1_f4 () {
        resetString1Drawables();
        if (string1_f4_on) {
            string1_f4_on = false;
            string1_text="x";
            string1_f4.setImageDrawable(string1);
            string1_top.setImageDrawable(stringtop_X);
        } else {
            resetString1Values();
            string1_f4_on = true;
            string1_f4.setImageDrawable(string1_on);
            string1_top.setImageDrawable(stringtop);
            string1_text="4";
        }
        updateChordText();
    }

    private void string1_f5 () {
        resetString1Drawables();
        if (string1_f5_on) {
            string1_f5_on = false;
            string1_text="x";
            string1_f5.setImageDrawable(string1);
            string1_top.setImageDrawable(stringtop_X);
        } else {
            resetString1Values();
            string1_f5_on = true;
            string1_f5.setImageDrawable(string1_on);
            string1_top.setImageDrawable(stringtop);
            string1_text="5";
        }
        updateChordText();
    }

    private void resetString6Drawables() {
        string6_top.setImageDrawable(stringtop);
        string6_f1.setImageDrawable(string6);
        string6_f2.setImageDrawable(string6);
        string6_f3.setImageDrawable(string6);
        string6_f4.setImageDrawable(string6);
        string6_f5.setImageDrawable(string6);
    }

    private void resetString6Values() {
        string6_f1_on = false;
        string6_f2_on = false;
        string6_f3_on = false;
        string6_f4_on = false;
        string6_f5_on = false;
    }

    private void resetString5HDrawables() {
        string5_top.setImageDrawable(stringtop);
        string5_f1.setImageDrawable(string1);
        string5_f2.setImageDrawable(string1);
        string5_f3.setImageDrawable(string1);
        string5_f4.setImageDrawable(string1);
        string5_f5.setImageDrawable(string1);
    }

    private void resetString5Drawables() {
        string5_top.setImageDrawable(stringtop);
        string5_f1.setImageDrawable(string5);
        string5_f2.setImageDrawable(string5);
        string5_f3.setImageDrawable(string5);
        string5_f4.setImageDrawable(string5);
        string5_f5.setImageDrawable(string5);
    }

    private void resetString5Values() {
        string5_f1_on = false;
        string5_f2_on = false;
        string5_f3_on = false;
        string5_f4_on = false;
        string5_f5_on = false;
    }

    private void resetString4Drawables() {
        string4_top.setImageDrawable(stringtop);
        string4_f1.setImageDrawable(string4);
        string4_f2.setImageDrawable(string4);
        string4_f3.setImageDrawable(string4);
        string4_f4.setImageDrawable(string4);
        string4_f5.setImageDrawable(string4);
    }

    private void resetString4Values() {
        string4_f1_on = false;
        string4_f2_on = false;
        string4_f3_on = false;
        string4_f4_on = false;
        string4_f5_on = false;
    }

    private void resetString3Drawables() {
        string3_top.setImageDrawable(stringtop);
        string3_f1.setImageDrawable(string3);
        string3_f2.setImageDrawable(string3);
        string3_f3.setImageDrawable(string3);
        string3_f4.setImageDrawable(string3);
        string3_f5.setImageDrawable(string3);
    }

    private void resetString3Values() {
        string3_f1_on = false;
        string3_f2_on = false;
        string3_f3_on = false;
        string3_f4_on = false;
        string3_f5_on = false;
    }

    private void resetString2Drawables() {
        string2_top.setImageDrawable(stringtop);
        string2_f1.setImageDrawable(string2);
        string2_f2.setImageDrawable(string2);
        string2_f3.setImageDrawable(string2);
        string2_f4.setImageDrawable(string2);
        string2_f5.setImageDrawable(string2);
    }

    private void resetString2Values() {
        string2_f1_on = false;
        string2_f2_on = false;
        string2_f3_on = false;
        string2_f4_on = false;
        string2_f5_on = false;
    }

    private void resetString1Drawables() {
        string1_top.setImageDrawable(stringtop);
        string1_f1.setImageDrawable(string1);
        string1_f2.setImageDrawable(string1);
        string1_f3.setImageDrawable(string1);
        string1_f4.setImageDrawable(string1);
        string1_f5.setImageDrawable(string1);
    }

    private void resetString1Values() {
        string1_f1_on = false;
        string1_f2_on = false;
        string1_f3_on = false;
        string1_f4_on = false;
        string1_f5_on = false;
    }

    private void updateChordText() {
        String chord_name = customchord_name.getText().toString();
        switch (instrument_text) {
            case "u":
            case "m":
            case "c":
            case "b":
                chord_text = string4_text + string3_text + string2_text + string1_text;
                break;
            case "B":
                chord_text = string5_text + string4_text + string3_text + string2_text + string1_text;
                break;
            case "g":
                chord_text = string6_text + string5_text + string4_text + string3_text + string2_text + string1_text;
                break;
            default:
                chord_text = "xxxxxx";
                break;
        }
        String texttowrite = chord_text + "_" + fret_text + "_" + instrument_text + "_" + chord_name;
        customchord_code.setText(texttowrite);
    }

    private void set6String() {
        string6_top.setVisibility(View.VISIBLE);
        string6_f1.setVisibility(View.VISIBLE);
        string6_f2.setVisibility(View.VISIBLE);
        string6_f3.setVisibility(View.VISIBLE);
        string6_f4.setVisibility(View.VISIBLE);
        string6_f5.setVisibility(View.VISIBLE);
        string5_top.setVisibility(View.VISIBLE);
        string5_f1.setVisibility(View.VISIBLE);
        string5_f2.setVisibility(View.VISIBLE);
        string5_f3.setVisibility(View.VISIBLE);
        string5_f4.setVisibility(View.VISIBLE);
        string5_f5.setVisibility(View.VISIBLE);
        string4_top.setVisibility(View.VISIBLE);
        string4_f1.setVisibility(View.VISIBLE);
        string4_f2.setVisibility(View.VISIBLE);
        string4_f3.setVisibility(View.VISIBLE);
        string4_f4.setVisibility(View.VISIBLE);
        string4_f5.setVisibility(View.VISIBLE);
        string3_top.setVisibility(View.VISIBLE);
        string3_f1.setVisibility(View.VISIBLE);
        string3_f2.setVisibility(View.VISIBLE);
        string3_f3.setVisibility(View.VISIBLE);
        string3_f4.setVisibility(View.VISIBLE);
        string3_f5.setVisibility(View.VISIBLE);
        string2_top.setVisibility(View.VISIBLE);
        string2_f1.setVisibility(View.VISIBLE);
        string2_f2.setVisibility(View.VISIBLE);
        string2_f3.setVisibility(View.VISIBLE);
        string2_f4.setVisibility(View.VISIBLE);
        string2_f5.setVisibility(View.VISIBLE);
        string2_top.setVisibility(View.VISIBLE);
        string1_f1.setVisibility(View.VISIBLE);
        string1_f2.setVisibility(View.VISIBLE);
        string1_f3.setVisibility(View.VISIBLE);
        string1_f4.setVisibility(View.VISIBLE);
        string1_f5.setVisibility(View.VISIBLE);
        resetString6Drawables();
        resetString6Values();
        resetString5Drawables();
        resetString5Values();
        resetString4Drawables();
        resetString4Values();
        resetString3Drawables();
        resetString3Values();
        resetString2Drawables();
        resetString2Values();
        resetString1Drawables();
        resetString1Values();
        resetStringTops();
        resetStringNotes();
        updateChordText();
    }

    private void set5String() {
        string6_top.setVisibility(View.GONE);
        string6_f1.setVisibility(View.GONE);
        string6_f2.setVisibility(View.GONE);
        string6_f3.setVisibility(View.GONE);
        string6_f4.setVisibility(View.GONE);
        string6_f5.setVisibility(View.GONE);
        string5_top.setVisibility(View.VISIBLE);
        string5_f1.setVisibility(View.VISIBLE);
        string5_f2.setVisibility(View.VISIBLE);
        string5_f3.setVisibility(View.VISIBLE);
        string5_f4.setVisibility(View.VISIBLE);
        string5_f5.setVisibility(View.VISIBLE);
        string4_top.setVisibility(View.VISIBLE);
        string4_f1.setVisibility(View.VISIBLE);
        string4_f2.setVisibility(View.VISIBLE);
        string4_f3.setVisibility(View.VISIBLE);
        string4_f4.setVisibility(View.VISIBLE);
        string4_f5.setVisibility(View.VISIBLE);
        string3_top.setVisibility(View.VISIBLE);
        string3_f1.setVisibility(View.VISIBLE);
        string3_f2.setVisibility(View.VISIBLE);
        string3_f3.setVisibility(View.VISIBLE);
        string3_f4.setVisibility(View.VISIBLE);
        string3_f5.setVisibility(View.VISIBLE);
        string2_top.setVisibility(View.VISIBLE);
        string2_f1.setVisibility(View.VISIBLE);
        string2_f2.setVisibility(View.VISIBLE);
        string2_f3.setVisibility(View.VISIBLE);
        string2_f4.setVisibility(View.VISIBLE);
        string2_f5.setVisibility(View.VISIBLE);
        string2_top.setVisibility(View.VISIBLE);
        string1_f1.setVisibility(View.VISIBLE);
        string1_f2.setVisibility(View.VISIBLE);
        string1_f3.setVisibility(View.VISIBLE);
        string1_f4.setVisibility(View.VISIBLE);
        string1_f5.setVisibility(View.VISIBLE);
        resetString6Drawables();
        resetString6Values();
        resetString5HDrawables();
        resetString5Values();
        resetString4Drawables();
        resetString4Values();
        resetString3Drawables();
        resetString3Values();
        resetString2Drawables();
        resetString2Values();
        resetString1Drawables();
        resetString1Values();
        resetStringTops();
        resetStringNotes();
        updateChordText();
    }

    private void set4String() {
        string6_top.setVisibility(View.GONE);
        string6_f1.setVisibility(View.GONE);
        string6_f2.setVisibility(View.GONE);
        string6_f3.setVisibility(View.GONE);
        string6_f4.setVisibility(View.GONE);
        string6_f5.setVisibility(View.GONE);
        string5_top.setVisibility(View.GONE);
        string5_f1.setVisibility(View.GONE);
        string5_f2.setVisibility(View.GONE);
        string5_f3.setVisibility(View.GONE);
        string5_f4.setVisibility(View.GONE);
        string5_f5.setVisibility(View.GONE);
        string4_top.setVisibility(View.VISIBLE);
        string4_f1.setVisibility(View.VISIBLE);
        string4_f2.setVisibility(View.VISIBLE);
        string4_f3.setVisibility(View.VISIBLE);
        string4_f4.setVisibility(View.VISIBLE);
        string4_f5.setVisibility(View.VISIBLE);
        string3_top.setVisibility(View.VISIBLE);
        string3_f1.setVisibility(View.VISIBLE);
        string3_f2.setVisibility(View.VISIBLE);
        string3_f3.setVisibility(View.VISIBLE);
        string3_f4.setVisibility(View.VISIBLE);
        string3_f5.setVisibility(View.VISIBLE);
        string2_top.setVisibility(View.VISIBLE);
        string2_f1.setVisibility(View.VISIBLE);
        string2_f2.setVisibility(View.VISIBLE);
        string2_f3.setVisibility(View.VISIBLE);
        string2_f4.setVisibility(View.VISIBLE);
        string2_f5.setVisibility(View.VISIBLE);
        string2_top.setVisibility(View.VISIBLE);
        string1_f1.setVisibility(View.VISIBLE);
        string1_f2.setVisibility(View.VISIBLE);
        string1_f3.setVisibility(View.VISIBLE);
        string1_f4.setVisibility(View.VISIBLE);
        string1_f5.setVisibility(View.VISIBLE);
        resetString6Drawables();
        resetString6Values();
        resetString5Drawables();
        resetString5Values();
        resetString4Drawables();
        resetString4Values();
        resetString3Drawables();
        resetString3Values();
        resetString2Drawables();
        resetString2Values();
        resetString1Drawables();
        resetString1Values();
        resetStringTops();
        resetStringNotes();
        updateChordText();
    }

    private void resetStringTops() {
        string6_O = false;
        string6_X = true;
        string5_O = false;
        string5_X = true;
        string4_O = false;
        string4_X = true;
        string3_O = false;
        string3_X = true;
        string2_O = false;
        string2_X = true;
        string1_O = false;
        string1_X = true;
        string6_top.setImageDrawable(stringtop_X);
        string5_top.setImageDrawable(stringtop_X);
        string4_top.setImageDrawable(stringtop_X);
        string3_top.setImageDrawable(stringtop_X);
        string2_top.setImageDrawable(stringtop_X);
        string1_top.setImageDrawable(stringtop_X);

    }

    private void resetStringNotes() {
        string6_text = "x";
        string5_text = "x";
        string4_text = "x";
        string3_text = "x";
        string2_text = "x";
        string1_text = "x";
        fret_text = "0";
    }

    private void customChordSave() {
        // The user is trying to save a custom chord
        // Find out what instrument we are using and get the appropriate string data
        switch (instrument_text) {
            case "g":
                // Guitar, so need all 6 strings
                chord_text = string6_text + string5_text + string4_text + string3_text + string2_text + string1_text;
                break;
            case "B":
                // 5 String banjo, so strings 1-5
                chord_text = string5_text + string4_text + string3_text + string2_text + string1_text;
                break;
            case "u":
            case "m":
            case "c":
            case "b":
                // Ukulele, mandolin, cavaquinho or 4 string banjo, so strings 1-4
                chord_text = string4_text + string3_text + string2_text + string1_text;
                break;
        }

        // Get the fret text
        if (fret_text.equals("") || fret_text.isEmpty()) {
            fret_text = "0";
        }

        String customNameToSave = customchord_name.getText().toString();

        // No spaces allowed in chordname. Replace them with hyphens
        // Also do this for underscores, as this could play havoc when decoding it!
        customNameToSave = customNameToSave.replace(" ","-");
        customNameToSave = customNameToSave.replace("_","-");
        String customChordToSave = chord_text + "_" + fret_text + "_" + instrument_text + "_" + customNameToSave;

        // Update customchord_code
        customchord_code.setText(customChordToSave);

        // Check for instrument
        if (!customChordToSave.contains("_g") &&
                !customChordToSave.contains("_u") &&
                !customChordToSave.contains("_m") &&
                !customChordToSave.contains("_b") &&
                !customChordToSave.contains("_B") &&
                !customChordToSave.contains("_c")) {
            //No instrument set
            StaticVariables.myToastMessage = getResources().getString(R.string.customchords_noinstrument);
            ShowToast.showToast(getActivity());
        } else if (customNameToSave.equals("") || customNameToSave.isEmpty()) {
            //No chordname set
            StaticVariables.myToastMessage = getResources().getString(R.string.customchords_nochordname);
            ShowToast.showToast(getActivity());
        } else {
            StaticVariables.myToastMessage = getResources().getString(R.string.customchords_save);
            ShowToast.showToast(getActivity());
            StaticVariables.mCustomChords = StaticVariables.mCustomChords + " " + customChordToSave;
            StaticVariables.mCustomChords = StaticVariables.mCustomChords.trim();

            // Prepare the song for saving
            PopUpEditSongFragment.prepareSongXML();

            // Now write the modified song
            doSave();

            // Refresh the custom chord buttons
            prepareCustomChords();
        }
    }

    public void doSave() {
        // Add the custom chord code to the xml
        Uri uri = storageAccess.getUriForItem(getActivity(), preferences, "Songs", StaticVariables.whichSongFolder,
                StaticVariables.songfilename);

        // Check the uri exists for the outputstream to be valid
        storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences, uri, null,
                "Songs", StaticVariables.whichSongFolder, StaticVariables.songfilename);

        OutputStream outputStream = storageAccess.getOutputStream(getActivity(),uri);
        storageAccess.writeFileFromString(FullscreenActivity.mynewXML,outputStream);
    }

    private class OnDelete implements View.OnClickListener {
        Button b;
        OnDelete(Button deleteChord) {
            b = deleteChord;
        }

        @Override
        public void onClick(View view) {
            String buttonText = b.getText().toString();
            buttonText = buttonText.replace(getResources().getString(R.string.options_song_delete),"");
            buttonText = buttonText.replace("\n","");
            StaticVariables.mCustomChords = StaticVariables.mCustomChords.replace(buttonText,"");
            // Save the song
            PopUpEditSongFragment.prepareSongXML();
            // Makes sure all & are replaced with &amp;
            FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&amp;","&");
            FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&","&amp;");

            // Now write the modified song
            doSave();

            // Refresh the custom chord buttons
            prepareCustomChords();
        }
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if (mListener!=null) {
            mListener.pageButtonAlpha("chords");
            FullscreenActivity.whattodo = "page_chords";
            mListener.openFragment();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
        if (prepare_custom!=null) {
            prepare_custom.cancel(true);
        }
    }

}