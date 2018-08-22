package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

public class PopUpChordsFragment extends DialogFragment {

    static PopUpChordsFragment newInstance() {
        PopUpChordsFragment frag;
        frag = new PopUpChordsFragment();
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

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    TableLayout chordimageshere;
    ArrayList<String> unique_chords;
    AsyncTask<Object,Void,String> prepare_chords;

    // Identify the chord images
    private Drawable f1, f2, f3, f4, f5, f6, f7, f8, f9, lx, l0, l1, l2, l3, l4, l5,
            mx, m0, m1, m2, m3, m4, m5, rx, r0, r1, r2, r3, r4, r5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (mListener!=null) {
            mListener.pageButtonAlpha("chords");
        }

        View V = inflater.inflate(R.layout.popup_page_chords, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.chords));
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
        Spinner popupchord_instrument = V.findViewById(R.id.popupchord_instrument);
        chordimageshere = V.findViewById(R.id.chordimageshere);
        // Identify the chord images
        f1 = getActivity().getResources().getDrawable(R.drawable.chord_f1);
        f2 = getActivity().getResources().getDrawable(R.drawable.chord_f2);
        f3 = getActivity().getResources().getDrawable(R.drawable.chord_f3);
        f4 = getActivity().getResources().getDrawable(R.drawable.chord_f4);
        f5 = getActivity().getResources().getDrawable(R.drawable.chord_f5);
        f6 = getActivity().getResources().getDrawable(R.drawable.chord_f6);
        f7 = getActivity().getResources().getDrawable(R.drawable.chord_f7);
        f8 = getActivity().getResources().getDrawable(R.drawable.chord_f8);
        f9 = getActivity().getResources().getDrawable(R.drawable.chord_f9);
        lx = getActivity().getResources().getDrawable(R.drawable.chord_l_x);
        l0 = getActivity().getResources().getDrawable(R.drawable.chord_l_0);
        l1 = getActivity().getResources().getDrawable(R.drawable.chord_l_1);
        l2 = getActivity().getResources().getDrawable(R.drawable.chord_l_2);
        l3 = getActivity().getResources().getDrawable(R.drawable.chord_l_3);
        l4 = getActivity().getResources().getDrawable(R.drawable.chord_l_4);
        l5 = getActivity().getResources().getDrawable(R.drawable.chord_l_5);
        mx = getActivity().getResources().getDrawable(R.drawable.chord_m_x);
        m0 = getActivity().getResources().getDrawable(R.drawable.chord_m_0);
        m1 = getActivity().getResources().getDrawable(R.drawable.chord_m_1);
        m2 = getActivity().getResources().getDrawable(R.drawable.chord_m_2);
        m3 = getActivity().getResources().getDrawable(R.drawable.chord_m_3);
        m4 = getActivity().getResources().getDrawable(R.drawable.chord_m_4);
        m5 = getActivity().getResources().getDrawable(R.drawable.chord_m_5);
        rx = getActivity().getResources().getDrawable(R.drawable.chord_r_x);
        r0 = getActivity().getResources().getDrawable(R.drawable.chord_r_0);
        r1 = getActivity().getResources().getDrawable(R.drawable.chord_r_1);
        r2 = getActivity().getResources().getDrawable(R.drawable.chord_r_2);
        r3 = getActivity().getResources().getDrawable(R.drawable.chord_r_3);
        r4 = getActivity().getResources().getDrawable(R.drawable.chord_r_4);
        r5 = getActivity().getResources().getDrawable(R.drawable.chord_r_5);
        Button customchordedit = V.findViewById(R.id.customchordedit);
        customchordedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "customchords";
                mListener.openFragment();
                dismiss();
            }
        });

        // Set the spinner options
        ArrayList<String> instrument_choice = new ArrayList<>();
        instrument_choice.add(getResources().getString(R.string.guitar));
        instrument_choice.add(getResources().getString(R.string.ukulele));
        instrument_choice.add(getResources().getString(R.string.mandolin));
        instrument_choice.add(getResources().getString(R.string.cavaquinho));
        instrument_choice.add(getResources().getString(R.string.banjo4));
        instrument_choice.add(getResources().getString(R.string.banjo5));
        ArrayAdapter<String> adapter_instrument = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, instrument_choice);
        adapter_instrument.setDropDownViewResource(R.layout.my_spinner);
        popupchord_instrument.setAdapter(adapter_instrument);
        switch (FullscreenActivity.chordInstrument) {
            case "g":
                popupchord_instrument.setSelection(0);
                break;
            case "u":
                popupchord_instrument.setSelection(1);
                break;
            case "m":
                popupchord_instrument.setSelection(2);
                break;
            case "c":
                popupchord_instrument.setSelection(3);
                break;
            case "b":
                popupchord_instrument.setSelection(4);
                break;
            case "B":
                popupchord_instrument.setSelection(5);
                break;

        }

        // Set up the listener for the instruments
        popupchord_instrument.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        FullscreenActivity.chordInstrument = "g";
                        break;
                    case 1:
                        FullscreenActivity.chordInstrument = "u";
                        break;
                    case 2:
                        FullscreenActivity.chordInstrument = "m";
                        break;
                    case 3:
                        FullscreenActivity.chordInstrument = "c";
                        break;
                    case 4:
                        FullscreenActivity.chordInstrument = "b";
                        break;
                    case 5:
                        FullscreenActivity.chordInstrument = "B";
                        break;
                }
                Preferences.savePreferences();
                prepareChords();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        prepareChords();

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    public void prepareChords() {
        prepare_chords = new PrepareChords();
        try {
            prepare_chords.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d","Error preparing chords");
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class PrepareChords extends AsyncTask<Object,Void,String> {

        @Override
        protected void onPreExecute() {
            try {
                chordimageshere.removeAllViews();
            } catch (Exception e) {
                // nothing to remove
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            // Read in my custom chords
            if (FullscreenActivity.mCustomChords == null) {
                FullscreenActivity.mCustomChords = "";
            }
            while (FullscreenActivity.mCustomChords.contains("  ")) {
                FullscreenActivity.mCustomChords = FullscreenActivity.mCustomChords.replace("  ", " ");
            }

            // Initialise the chords in the song
            FullscreenActivity.allchords = ProcessSong.getAllChords(FullscreenActivity.mLyrics);
            while (FullscreenActivity.allchords.contains("  ")) {
                FullscreenActivity.allchords = FullscreenActivity.allchords.replace("  "," ");
            }

            String tempallchords = FullscreenActivity.allchords;
            FullscreenActivity.mCustomChords = FullscreenActivity.mCustomChords.trim();
            String[] tempCustomChordsArray = FullscreenActivity.mCustomChords.split(" ");
            StringBuilder tempCustomChordsToAdd = new StringBuilder();
            int numcustomchords;

            if (tempCustomChordsArray.length > 0) {
                numcustomchords = tempCustomChordsArray.length;
                for (int q = 0; q < numcustomchords; q++) {
                    if ((FullscreenActivity.chordInstrument.equals("u") && tempCustomChordsArray[q] != null && tempCustomChordsArray[q].contains("_u_")) ||
                            (FullscreenActivity.chordInstrument.equals("m") && tempCustomChordsArray[q] != null && tempCustomChordsArray[q].contains("_m_")) ||
                            (FullscreenActivity.chordInstrument.equals("c") && tempCustomChordsArray[q] != null && tempCustomChordsArray[q].contains("_c_")) ||
                            (FullscreenActivity.chordInstrument.equals("b") && tempCustomChordsArray[q] != null && tempCustomChordsArray[q].contains("_b_")) ||
                            (FullscreenActivity.chordInstrument.equals("B") && tempCustomChordsArray[q] != null && tempCustomChordsArray[q].contains("_B_")) ||
                            (FullscreenActivity.chordInstrument.equals("g") && tempCustomChordsArray[q] != null && tempCustomChordsArray[q].contains("_g_"))) {
                        tempCustomChordsToAdd.append(" $$$").append(tempCustomChordsArray[q]);
                    }
                }
            }

            // Remove all whitespace between chords
            if (FullscreenActivity.allchords == null) {
                FullscreenActivity.allchords = "";
            }
            while (FullscreenActivity.allchords.contains("  ")) {
                FullscreenActivity.allchords = FullscreenActivity.allchords.replace("  ", " ");
            }

            // Get rid of other bits that shouldn't be there
            FullscreenActivity.allchords = FullscreenActivity.allchords.replace("(", "");
            FullscreenActivity.allchords = FullscreenActivity.allchords.replace(")", "");
            FullscreenActivity.allchords = FullscreenActivity.allchords.replace("*", "");
            FullscreenActivity.allchords = FullscreenActivity.allchords.replace("!", "");
            FullscreenActivity.allchords = FullscreenActivity.allchords.replace(";", "");
            FullscreenActivity.allchords = FullscreenActivity.allchords.replace(":", "");
            FullscreenActivity.allchords = FullscreenActivity.allchords.replace("*", "");

            // Add the identified custom chords (start with $$$) to the allchords
            tempallchords = tempCustomChordsToAdd + " " + tempallchords;

            unique_chords = new ArrayList<>();
            tempallchords = tempallchords.trim();
            String[] allchords_array = tempallchords.split(" ");
            if (allchords_array.length > 0) {
                for (String anAllchords_array : allchords_array) {
                    if (!unique_chords.contains(anAllchords_array)) {
                        unique_chords.add(anAllchords_array);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                // Send the unique chords off to get the string layout
                // This will eventually be if guitar/ukulele/mandolin/piano/other
                // Custom chords don't get sent for retrieval as they are already defined
                for (int l = 0; l < unique_chords.size(); l++) {
                    if (FullscreenActivity.chordInstrument.equals("u") && !unique_chords.get(l).contains("$$$")) {
                        ChordDirectory.ukuleleChords(unique_chords.get(l));
                    } else if (FullscreenActivity.chordInstrument.equals("m") && !unique_chords.get(l).contains("$$$")) {
                        ChordDirectory.mandolinChords(unique_chords.get(l));
                    } else if (FullscreenActivity.chordInstrument.equals("g") && !unique_chords.get(l).contains("$$$")) {
                        ChordDirectory.guitarChords(unique_chords.get(l));
                    } else if (FullscreenActivity.chordInstrument.equals("c") && !unique_chords.get(l).contains("$$$")) {
                        ChordDirectory.cavaquinhoChords(unique_chords.get(l));
                    } else if (FullscreenActivity.chordInstrument.equals("b") && !unique_chords.get(l).contains("$$$")) {
                        ChordDirectory.banjo4stringChords(unique_chords.get(l));
                    } else if (FullscreenActivity.chordInstrument.equals("B") && !unique_chords.get(l).contains("$$$")) {
                        ChordDirectory.banjo5stringChords(unique_chords.get(l));
                    }

                    // If chord is custom, prepare this prefix to the name
                    String iscustom = "";
                    if (unique_chords.get(l).contains("$$$")) {
                        iscustom = "\n" + getResources().getString(R.string.custom) + "";
                        FullscreenActivity.chordnotes = unique_chords.get(l);
                        FullscreenActivity.chordnotes = FullscreenActivity.chordnotes.replace("$$$", "");
                        unique_chords.set(l, unique_chords.get(l).replace("$$$", ""));
                        int startposcname = unique_chords.get(l).lastIndexOf("_");
                        if (startposcname != -1) {
                            unique_chords.set(l, unique_chords.get(l).substring(startposcname + 1));
                        }
                    }

                    // Prepare a new Horizontal Linear Layout for each chord
                    TableRow chordview = new TableRow(getActivity());
                    TableLayout.LayoutParams tableRowParams =
                            new TableLayout.LayoutParams
                                    (TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);

                    int leftMargin = 10;
                    int topMargin = 10;
                    int rightMargin = 10;
                    int bottomMargin = 10;

                    tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);

                    chordview.setLayoutParams(tableRowParams);
                    TextView chordname = new TextView(getActivity());
                    ImageView image1 = new ImageView(getActivity());
                    ImageView image2 = new ImageView(getActivity());
                    ImageView image3 = new ImageView(getActivity());
                    ImageView image4 = new ImageView(getActivity());
                    ImageView image5 = new ImageView(getActivity());
                    ImageView image6 = new ImageView(getActivity());
                    ImageView image0 = new ImageView(getActivity());

                    // Initialise 6 strings and frets
                    String string_6 = "";
                    String string_5 = "";
                    String string_4 = "";
                    String string_3 = "";
                    String string_2 = "";
                    String string_1 = "";
                    String fret = "";

                    switch (FullscreenActivity.chordInstrument) {
                        case "g":

                            if (FullscreenActivity.chordnotes.length() > 0) {
                                string_6 = FullscreenActivity.chordnotes.substring(0, 1);
                            }
                            if (FullscreenActivity.chordnotes.length() > 1) {
                                string_5 = FullscreenActivity.chordnotes.substring(1, 2);
                            }
                            if (FullscreenActivity.chordnotes.length() > 2) {
                                string_4 = FullscreenActivity.chordnotes.substring(2, 3);
                            }
                            if (FullscreenActivity.chordnotes.length() > 3) {
                                string_3 = FullscreenActivity.chordnotes.substring(3, 4);
                            }
                            if (FullscreenActivity.chordnotes.length() > 4) {
                                string_2 = FullscreenActivity.chordnotes.substring(4, 5);
                            }
                            if (FullscreenActivity.chordnotes.length() > 5) {
                                string_1 = FullscreenActivity.chordnotes.substring(5, 6);
                            }
                            if (FullscreenActivity.chordnotes.length() > 7) {
                                fret = FullscreenActivity.chordnotes.substring(7, 8);
                            }

                            // Prepare string_6
                            switch (string_6) {
                                case "0":
                                    image6.setImageDrawable(l0);
                                    break;
                                case "1":
                                    image6.setImageDrawable(l1);
                                    break;
                                case "2":
                                    image6.setImageDrawable(l2);
                                    break;
                                case "3":
                                    image6.setImageDrawable(l3);
                                    break;
                                case "4":
                                    image6.setImageDrawable(l4);
                                    break;
                                case "5":
                                    image6.setImageDrawable(l5);
                                    break;
                                default:
                                    image6.setImageDrawable(lx);
                                    break;
                            }

                            // Prepare string_5
                            switch (string_5) {
                                case "0":
                                    image5.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image5.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image5.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image5.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image5.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image5.setImageDrawable(m5);
                                    break;
                                default:
                                    image5.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_4
                            switch (string_4) {
                                case "0":
                                    image4.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image4.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image4.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image4.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image4.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image4.setImageDrawable(m5);
                                    break;
                                default:
                                    image4.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_3
                            switch (string_3) {
                                case "0":
                                    image3.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image3.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image3.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image3.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image3.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image3.setImageDrawable(m5);
                                    break;
                                default:
                                    image3.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_2
                            switch (string_2) {
                                case "0":
                                    image2.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image2.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image2.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image2.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image2.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image2.setImageDrawable(m5);
                                    break;
                                default:
                                    image2.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_1
                            switch (string_1) {
                                case "0":
                                    image1.setImageDrawable(r0);
                                    break;
                                case "1":
                                    image1.setImageDrawable(r1);
                                    break;
                                case "2":
                                    image1.setImageDrawable(r2);
                                    break;
                                case "3":
                                    image1.setImageDrawable(r3);
                                    break;
                                case "4":
                                    image1.setImageDrawable(r4);
                                    break;
                                case "5":
                                    image1.setImageDrawable(r5);
                                    break;
                                default:
                                    image1.setImageDrawable(rx);
                                    break;
                            }

                            // Prepare fret
                            switch (fret) {
                                case "1":
                                    image0.setImageDrawable(f1);
                                    break;
                                case "2":
                                    image0.setImageDrawable(f2);
                                    break;
                                case "3":
                                    image0.setImageDrawable(f3);
                                    break;
                                case "4":
                                    image0.setImageDrawable(f4);
                                    break;
                                case "5":
                                    image0.setImageDrawable(f5);
                                    break;
                                case "6":
                                    image0.setImageDrawable(f6);
                                    break;
                                case "7":
                                    image0.setImageDrawable(f7);
                                    break;
                                case "8":
                                    image0.setImageDrawable(f8);
                                    break;
                                case "9":
                                    image0.setImageDrawable(f9);
                                    break;
                                default:
                                    image0 = null;
                                    break;
                            }

                            chordname.setPadding(0, 0, 12, 0);
                            chordview.addView(chordname);
                            if (image0 != null) {
                                chordview.addView(image0);
                            }
                            chordview.addView(image6);
                            chordview.addView(image5);
                            chordview.addView(image4);
                            chordview.addView(image3);
                            chordview.addView(image2);
                            chordview.addView(image1);

                            break;
                        case "B":

                            if (FullscreenActivity.chordnotes.length() > 0) {
                                string_5 = FullscreenActivity.chordnotes.substring(0, 1);
                            }
                            if (FullscreenActivity.chordnotes.length() > 1) {
                                string_4 = FullscreenActivity.chordnotes.substring(1, 2);
                            }
                            if (FullscreenActivity.chordnotes.length() > 2) {
                                string_3 = FullscreenActivity.chordnotes.substring(2, 3);
                            }
                            if (FullscreenActivity.chordnotes.length() > 3) {
                                string_2 = FullscreenActivity.chordnotes.substring(3, 4);
                            }
                            if (FullscreenActivity.chordnotes.length() > 4) {
                                string_1 = FullscreenActivity.chordnotes.substring(4, 5);
                            }
                            if (FullscreenActivity.chordnotes.length() > 6) {
                                fret = FullscreenActivity.chordnotes.substring(6, 7);
                            }

                            // Prepare string_5
                            switch (string_5) {
                                case "0":
                                    image5.setImageDrawable(l0);
                                    break;
                                case "1":
                                    image5.setImageDrawable(l1);
                                    break;
                                case "2":
                                    image5.setImageDrawable(l2);
                                    break;
                                case "3":
                                    image5.setImageDrawable(l3);
                                    break;
                                case "4":
                                    image5.setImageDrawable(l4);
                                    break;
                                case "5":
                                    image5.setImageDrawable(l5);
                                    break;
                                default:
                                    image5.setImageDrawable(lx);
                                    break;
                            }

                            // Prepare string_4
                            switch (string_4) {
                                case "0":
                                    image4.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image4.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image4.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image4.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image4.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image4.setImageDrawable(m5);
                                    break;
                                default:
                                    image4.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_3
                            switch (string_3) {
                                case "0":
                                    image3.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image3.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image3.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image3.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image3.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image3.setImageDrawable(m5);
                                    break;
                                default:
                                    image3.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_2
                            switch (string_2) {
                                case "0":
                                    image2.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image2.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image2.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image2.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image2.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image2.setImageDrawable(m5);
                                    break;
                                default:
                                    image2.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_1
                            switch (string_1) {
                                case "0":
                                    image1.setImageDrawable(r0);
                                    break;
                                case "1":
                                    image1.setImageDrawable(r1);
                                    break;
                                case "2":
                                    image1.setImageDrawable(r2);
                                    break;
                                case "3":
                                    image1.setImageDrawable(r3);
                                    break;
                                case "4":
                                    image1.setImageDrawable(r4);
                                    break;
                                case "5":
                                    image1.setImageDrawable(r5);
                                    break;
                                default:
                                    image1.setImageDrawable(rx);
                                    break;
                            }

                            // Prepare fret
                            switch (fret) {
                                case "1":
                                    image0.setImageDrawable(f1);
                                    break;
                                case "2":
                                    image0.setImageDrawable(f2);
                                    break;
                                case "3":
                                    image0.setImageDrawable(f3);
                                    break;
                                case "4":
                                    image0.setImageDrawable(f4);
                                    break;
                                case "5":
                                    image0.setImageDrawable(f5);
                                    break;
                                case "6":
                                    image0.setImageDrawable(f6);
                                    break;
                                case "7":
                                    image0.setImageDrawable(f7);
                                    break;
                                case "8":
                                    image0.setImageDrawable(f8);
                                    break;
                                case "9":
                                    image0.setImageDrawable(f9);
                                    break;
                                default:
                                    image0 = null;
                                    break;
                            }

                            chordview.addView(chordname);
                            if (image0 != null) {
                                chordview.addView(image0);
                            }
                            chordview.addView(image5);
                            chordview.addView(image4);
                            chordview.addView(image3);
                            chordview.addView(image2);
                            chordview.addView(image1);

                            break;
                        case "u":
                        case "m":
                        case "c":
                        case "b":
                            if (FullscreenActivity.chordnotes.length() > 0) {
                                string_4 = FullscreenActivity.chordnotes.substring(0, 1);
                            }
                            if (FullscreenActivity.chordnotes.length() > 1) {
                                string_3 = FullscreenActivity.chordnotes.substring(1, 2);
                            }
                            if (FullscreenActivity.chordnotes.length() > 2) {
                                string_2 = FullscreenActivity.chordnotes.substring(2, 3);
                            }
                            if (FullscreenActivity.chordnotes.length() > 3) {
                                string_1 = FullscreenActivity.chordnotes.substring(3, 4);
                            }
                            if (FullscreenActivity.chordnotes.length() > 5) {
                                fret = FullscreenActivity.chordnotes.substring(5, 6);
                            }

                            // Prepare string_4
                            switch (string_4) {
                                case "0":
                                    image4.setImageDrawable(l0);
                                    break;
                                case "1":
                                    image4.setImageDrawable(l1);
                                    break;
                                case "2":
                                    image4.setImageDrawable(l2);
                                    break;
                                case "3":
                                    image4.setImageDrawable(l3);
                                    break;
                                case "4":
                                    image4.setImageDrawable(l4);
                                    break;
                                default:
                                    image4.setImageDrawable(lx);
                                    break;
                            }

                            // Prepare string_3
                            switch (string_3) {
                                case "0":
                                    image3.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image3.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image3.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image3.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image3.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image3.setImageDrawable(m5);
                                    break;
                                default:
                                    image3.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_2
                            switch (string_2) {
                                case "0":
                                    image2.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image2.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image2.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image2.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image2.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image2.setImageDrawable(m5);
                                    break;
                                default:
                                    image2.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_1
                            switch (string_1) {
                                case "0":
                                    image1.setImageDrawable(r0);
                                    break;
                                case "1":
                                    image1.setImageDrawable(r1);
                                    break;
                                case "2":
                                    image1.setImageDrawable(r2);
                                    break;
                                case "3":
                                    image1.setImageDrawable(r3);
                                    break;
                                case "4":
                                    image1.setImageDrawable(r4);
                                    break;
                                case "5":
                                    image1.setImageDrawable(r5);
                                    break;
                                default:
                                    image1.setImageDrawable(rx);
                                    break;
                            }

                            // Prepare fret
                            switch (fret) {
                                case "1":
                                    image0.setImageDrawable(f1);
                                    break;
                                case "2":
                                    image0.setImageDrawable(f2);
                                    break;
                                case "3":
                                    image0.setImageDrawable(f3);
                                    break;
                                case "4":
                                    image0.setImageDrawable(f4);
                                    break;
                                case "5":
                                    image0.setImageDrawable(f5);
                                    break;
                                case "6":
                                    image0.setImageDrawable(f6);
                                    break;
                                case "7":
                                    image0.setImageDrawable(f7);
                                    break;
                                case "8":
                                    image0.setImageDrawable(f8);
                                    break;
                                case "9":
                                    image0.setImageDrawable(f9);
                                    break;
                                default:
                                    image0 = null;
                                    break;
                            }

                            chordview.addView(chordname);
                            if (image0 != null) {
                                chordview.addView(image0);
                            }
                            chordview.addView(image4);
                            chordview.addView(image3);
                            chordview.addView(image2);
                            chordview.addView(image1);
                            break;
                    }

                    if (FullscreenActivity.chordnotes != null && !FullscreenActivity.chordnotes.contains("xxxx_") && !FullscreenActivity.chordnotes.contains("xxxxxx_")) {
                        chordimageshere.addView(chordview);
                        String text;
                        if (unique_chords.get(l) == null) {
                            text = "" + iscustom;
                        } else {
                            text = unique_chords.get(l) + iscustom;
                        }
                        chordname.setText(text);
                        chordname.setTextColor(0xffffffff);
                        chordname.setTextSize(20);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        if (prepare_chords!=null) {
            prepare_chords.cancel(true);
        }
        this.dismiss();
    }

}
