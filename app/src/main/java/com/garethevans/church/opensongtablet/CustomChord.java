package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by gareth on 29/04/15.
 */
public class CustomChord extends Activity implements View.OnClickListener {

    String string6_text = "x";
    String string5_text = "x";
    String string4_text = "x";
    String string3_text = "x";
    String string2_text = "x";
    String string1_text = "x";
    String fret_text = "0";
    String instrument_text = "g";
    String chord_text = "xxxxxx";
    String chord_name = "";

    boolean string6_O = false;
    boolean string5_O = false;
    boolean string4_O = false;
    boolean string3_O = false;
    boolean string2_O = false;
    boolean string1_O = false;
    boolean string6_X = true;
    boolean string5_X = true;
    boolean string4_X = true;
    boolean string3_X = true;
    boolean string2_X = true;
    boolean string1_X = true;
    boolean string6_f1_on = false;
    boolean string6_f2_on = false;
    boolean string6_f3_on = false;
    boolean string6_f4_on = false;
    boolean string6_f5_on = false;
    boolean string5_f1_on = false;
    boolean string5_f2_on = false;
    boolean string5_f3_on = false;
    boolean string5_f4_on = false;
    boolean string5_f5_on = false;
    boolean string4_f1_on = false;
    boolean string4_f2_on = false;
    boolean string4_f3_on = false;
    boolean string4_f4_on = false;
    boolean string4_f5_on = false;
    boolean string3_f1_on = false;
    boolean string3_f2_on = false;
    boolean string3_f3_on = false;
    boolean string3_f4_on = false;
    boolean string3_f5_on = false;
    boolean string2_f1_on = false;
    boolean string2_f2_on = false;
    boolean string2_f3_on = false;
    boolean string2_f4_on = false;
    boolean string2_f5_on = false;
    boolean string1_f1_on = false;
    boolean string1_f2_on = false;
    boolean string1_f3_on = false;
    boolean string1_f4_on = false;
    boolean string1_f5_on = false;
    Drawable stringtop;
    Drawable stringtop_X;
    Drawable stringtop_O;
    Drawable string6;
    Drawable string5;
    Drawable string4;
    Drawable string3;
    Drawable string2;
    Drawable string1;
    Drawable string6_on;
    Drawable string5_on;
    Drawable string4_on;
    Drawable string3_on;
    Drawable string2_on;
    Drawable string1_on;
    ImageView string6_top;
    ImageView string5_top;
    ImageView string4_top;
    ImageView string3_top;
    ImageView string2_top;
    ImageView string1_top;
    ImageView string6_f1;
    ImageView string6_f2;
    ImageView string6_f3;
    ImageView string6_f4;
    ImageView string6_f5;
    ImageView string5_f1;
    ImageView string5_f2;
    ImageView string5_f3;
    ImageView string5_f4;
    ImageView string5_f5;
    ImageView string4_f1;
    ImageView string4_f2;
    ImageView string4_f3;
    ImageView string4_f4;
    ImageView string4_f5;
    ImageView string3_f1;
    ImageView string3_f2;
    ImageView string3_f3;
    ImageView string3_f4;
    ImageView string3_f5;
    ImageView string2_f1;
    ImageView string2_f2;
    ImageView string2_f3;
    ImageView string2_f4;
    ImageView string2_f5;
    ImageView string1_f1;
    ImageView string1_f2;
    ImageView string1_f3;
    ImageView string1_f4;
    ImageView string1_f5;

    Spinner CustomChord_Instrument;
    Spinner CustomChord_Fret;
    EditText customchord_name;
    TextView customchord_code;

    LinearLayout CustomChord_Buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the screen and title
        setContentView(R.layout.custom_chord_creator);
        try {
            getActionBar().setTitle(getResources().getString(R.string.customchords)+ " - "+ FullscreenActivity.mTitle);
        } catch (Exception e) {
            // Struggled to name the title bar;
        }


        stringtop = getResources().getDrawable(R.drawable.string_top);
        stringtop_X = getResources().getDrawable(R.drawable.string_top_x);
        stringtop_O = getResources().getDrawable(R.drawable.string_top_o);

        string6 = getResources().getDrawable(R.drawable.string_6);
        string6_on = getResources().getDrawable(R.drawable.string_6_on);
        string5 = getResources().getDrawable(R.drawable.string_5);
        string5_on = getResources().getDrawable(R.drawable.string_5_on);
        string4 = getResources().getDrawable(R.drawable.string_4);
        string4_on = getResources().getDrawable(R.drawable.string_4_on);
        string3 = getResources().getDrawable(R.drawable.string_3);
        string3_on = getResources().getDrawable(R.drawable.string_3_on);
        string2 = getResources().getDrawable(R.drawable.string_2);
        string2_on = getResources().getDrawable(R.drawable.string_2_on);
        string1 = getResources().getDrawable(R.drawable.string_1);
        string1_on = getResources().getDrawable(R.drawable.string_1_on);

        string6_top = (ImageView) findViewById(R.id.string6_top);
        string5_top = (ImageView) findViewById(R.id.string5_top);
        string4_top = (ImageView) findViewById(R.id.string4_top);
        string3_top = (ImageView) findViewById(R.id.string3_top);
        string2_top = (ImageView) findViewById(R.id.string2_top);
        string1_top = (ImageView) findViewById(R.id.string1_top);
        string6_f1 = (ImageView) findViewById(R.id.string6_f1);
        string6_f2 = (ImageView) findViewById(R.id.string6_f2);
        string6_f3 = (ImageView) findViewById(R.id.string6_f3);
        string6_f4 = (ImageView) findViewById(R.id.string6_f4);
        string6_f5 = (ImageView) findViewById(R.id.string6_f5);
        string5_f1 = (ImageView) findViewById(R.id.string5_f1);
        string5_f2 = (ImageView) findViewById(R.id.string5_f2);
        string5_f3 = (ImageView) findViewById(R.id.string5_f3);
        string5_f4 = (ImageView) findViewById(R.id.string5_f4);
        string5_f5 = (ImageView) findViewById(R.id.string5_f5);
        string4_f1 = (ImageView) findViewById(R.id.string4_f1);
        string4_f2 = (ImageView) findViewById(R.id.string4_f2);
        string4_f3 = (ImageView) findViewById(R.id.string4_f3);
        string4_f4 = (ImageView) findViewById(R.id.string4_f4);
        string4_f5 = (ImageView) findViewById(R.id.string4_f5);
        string3_f1 = (ImageView) findViewById(R.id.string3_f1);
        string3_f2 = (ImageView) findViewById(R.id.string3_f2);
        string3_f3 = (ImageView) findViewById(R.id.string3_f3);
        string3_f4 = (ImageView) findViewById(R.id.string3_f4);
        string3_f5 = (ImageView) findViewById(R.id.string3_f5);
        string2_f1 = (ImageView) findViewById(R.id.string2_f1);
        string2_f2 = (ImageView) findViewById(R.id.string2_f2);
        string2_f3 = (ImageView) findViewById(R.id.string2_f3);
        string2_f4 = (ImageView) findViewById(R.id.string2_f4);
        string2_f5 = (ImageView) findViewById(R.id.string2_f5);
        string1_f1 = (ImageView) findViewById(R.id.string1_f1);
        string1_f2 = (ImageView) findViewById(R.id.string1_f2);
        string1_f3 = (ImageView) findViewById(R.id.string1_f3);
        string1_f4 = (ImageView) findViewById(R.id.string1_f4);
        string1_f5 = (ImageView) findViewById(R.id.string1_f5);
        customchord_name = (EditText) findViewById(R.id.customchord_name);
        customchord_code = (TextView) findViewById(R.id.customchord_code);
        CustomChord_Instrument = (Spinner) findViewById(R.id.customchords_instrument);
        String[] instrument_choice = new String[3];
        instrument_choice[0] = getResources().getString(R.string.guitar);
        instrument_choice[1] = getResources().getString(R.string.ukulele);
        instrument_choice[2] = getResources().getString(R.string.mandolin);
        ArrayAdapter<String> adapter_instrument = new ArrayAdapter<String>(this, R.layout.my_spinner, instrument_choice);
        adapter_instrument.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        CustomChord_Instrument.setAdapter(adapter_instrument);
        CustomChord_Instrument.setOnItemSelectedListener(new InstrumentListener());

        CustomChord_Buttons = (LinearLayout) findViewById(R.id.savedcustomchords);

        CustomChord_Fret = (Spinner) findViewById(R.id.customchords_fret);
        String[] fret_choice = {"","1","2","3","4","5","6","7","8","9"};
        ArrayAdapter<String> adapter_fret = new ArrayAdapter<String>(this, R.layout.my_spinner, fret_choice);
        adapter_fret.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        CustomChord_Fret.setAdapter(adapter_fret);
        CustomChord_Fret.setOnItemSelectedListener(new FretListener());
        redrawRemoveButtons();
    }

    @Override
    public void onBackPressed() {
        Intent viewsong = new Intent();
        viewsong.setClass(CustomChord.this, FullscreenActivity.class);
        startActivity(viewsong);
        this.finish();
    }

    public void gotosongs(View view) {
        Intent viewsong = new Intent();
        viewsong.setClass(CustomChord.this, FullscreenActivity.class);
        startActivity(viewsong);
        this.finish();
    }

    public void customChordSave(View view) {
        // The user is trying to save a custom chord
        // Find out what instrument we are using and get the appropriate string data
        if (instrument_text.equals("g")) {
            // Guitar, so need all 6 strings
            chord_text = string6_text + string5_text + string4_text + string3_text + string2_text + string1_text;
        } else if (instrument_text.equals("u") || instrument_text.equals("m")) {
            // Ukulele or mandolin, so strings 1-4
            chord_text = string4_text + string3_text + string2_text + string1_text;
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
        if (!customChordToSave.contains("_g") && !customChordToSave.contains("_u") && !customChordToSave.contains("_m")) {
            //No instrument set
            FullscreenActivity.myToastMessage = getResources().getString(R.string.customchords_noinstrument);
            ShowToast.showToast(CustomChord.this);
        } else if (customNameToSave==null || customNameToSave.equals("") || customNameToSave.isEmpty()) {
            //No instrument set
            FullscreenActivity.myToastMessage = getResources().getString(R.string.customchords_nochordname);
            ShowToast.showToast(CustomChord.this);
        } else {
            FullscreenActivity.myToastMessage = getResources().getString(R.string.customchords_save);
            ShowToast.showToast(CustomChord.this);
            FullscreenActivity.mCustomChords = FullscreenActivity.mCustomChords + " " + customChordToSave;
            FullscreenActivity.mCustomChords = FullscreenActivity.mCustomChords.trim();

            // Prepare the song for saving
            EditSong.prepareSongXML();

            // Now write the modified song
            doSave();

            // Refresh the custom chord buttons
            redrawRemoveButtons();

            redrawRemoveButtons();
        }
    }

    private class InstrumentListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            if (position==0) {
                instrument_text = "g";
                set6String();
            } else if (position==1) {
                instrument_text = "u";
                set4String();
            } else if (position==2) {
                instrument_text = "m";
                set4String();
            }

            resetStringNotes();
            updateChordText();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.e("popupChord", "Nothing selected");
        }
    }

    private class FretListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            fret_text = CustomChord_Fret.getItemAtPosition(position).toString();

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

    public void redrawRemoveButtons() {
        // Try to decipher any custom chords that have been saved.
        // Split the mCustomChords into an array separated by a space
        CustomChord_Buttons.removeAllViews();
        String[] tempCustomChords = null;

        //Get rid of excessive spaces
        FullscreenActivity.mCustomChords = FullscreenActivity.mCustomChords.trim();
        while (FullscreenActivity.mCustomChords.contains("  ")) {
            FullscreenActivity.mCustomChords = FullscreenActivity.mCustomChords.replace("  "," ");
        }

        tempCustomChords = FullscreenActivity.mCustomChords.split(" ");
        int numcustomchords;
        if (tempCustomChords.length>0) {
            numcustomchords = tempCustomChords.length;
            for (int q=0;q<numcustomchords;q++) {
                String workingChord = tempCustomChords[q];
                TextView chordvalue = new TextView(this);
                Button deleteChord = new Button(this);
                String chorddetails = "";
                if (tempCustomChords[q].contains("_u")) {
                    chorddetails = getResources().getString(R.string.ukulele) + "\n";
                    workingChord = workingChord.replace("_u", "");
                } else if (tempCustomChords[q].contains("_m")) {
                    chorddetails = getResources().getString(R.string.mandolin) + "\n";
                    workingChord = workingChord.replace("_m", "");
                } else {
                    chorddetails = getResources().getString(R.string.guitar) + "\n";
                    workingChord = workingChord.replace("_g", "");
                }
                if (tempCustomChords[q].contains("_0")) {
                    chorddetails = chorddetails + getResources().getString(R.string.customchords_fret) + " = 0\n";
                    workingChord = workingChord.replace("_0", "");
                } else if (tempCustomChords[q].contains("_1")) {
                    chorddetails = chorddetails + getResources().getString(R.string.customchords_fret) + " = 1\n";
                    workingChord = workingChord.replace("_0", "");
                } else if (tempCustomChords[q].contains("_1")) {
                    chorddetails = chorddetails + getResources().getString(R.string.customchords_fret) + " = 1\n";
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
                chordvalue.setText("\n\n" + chorddetails);
                deleteChord.setTransformationMethod(null);
                deleteChord.setText(getResources().getString(R.string.options_song_delete)+"\n"+tempCustomChords[q]);
                deleteChord.setBackgroundDrawable(getResources().getDrawable(R.drawable.red_button));
                deleteChord.setOnClickListener(this);
                CustomChord_Buttons.addView(chordvalue);
                CustomChord_Buttons.addView(deleteChord);
            }
        }

    }

    //This is the code to handle the user clicking on the strings/frets
    public void string6_top (View view) {
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
    public void string6_f1 (View view) {
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
    public void string6_f2 (View view) {
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
    public void string6_f3 (View view) {
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
    public void string6_f4 (View view) {
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
    public void string6_f5 (View view) {
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
    public void string5_top (View view) {
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
    public void string5_f1 (View view) {
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
    public void string5_f2 (View view) {
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
    public void string5_f3 (View view) {
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
    public void string5_f4 (View view) {
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
    public void string5_f5 (View view) {
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
    public void string4_top (View view) {
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
    public void string4_f1 (View view) {
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
    public void string4_f2 (View view) {
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
    public void string4_f3 (View view) {
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
    public void string4_f4 (View view) {
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
    public void string4_f5 (View view) {
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
    public void string3_top (View view) {
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
    public void string3_f1 (View view) {
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
    public void string3_f2 (View view) {
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
    public void string3_f3 (View view) {
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
    public void string3_f4 (View view) {
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
    public void string3_f5 (View view) {
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
    public void string2_top (View view) {
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
    public void string2_f1 (View view) {
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
    public void string2_f2 (View view) {
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
    public void string2_f3 (View view) {
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
    public void string2_f4 (View view) {
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
    public void string2_f5 (View view) {
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
    public void string1_top (View view) {
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
    public void string1_f1 (View view) {
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
    public void string1_f2 (View view) {
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
    public void string1_f3 (View view) {
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
    public void string1_f4 (View view) {
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
    public void string1_f5 (View view) {
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

    public void resetString6Drawables() {
        string6_top.setImageDrawable(stringtop);
        string6_f1.setImageDrawable(string6);
        string6_f2.setImageDrawable(string6);
        string6_f3.setImageDrawable(string6);
        string6_f4.setImageDrawable(string6);
        string6_f5.setImageDrawable(string6);
    }
    public void resetString6Values() {
        string6_f1_on = false;
        string6_f2_on = false;
        string6_f3_on = false;
        string6_f4_on = false;
        string6_f5_on = false;
    }
    public void resetString5Drawables() {
        string5_top.setImageDrawable(stringtop);
        string5_f1.setImageDrawable(string5);
        string5_f2.setImageDrawable(string5);
        string5_f3.setImageDrawable(string5);
        string5_f4.setImageDrawable(string5);
        string5_f5.setImageDrawable(string5);
    }
    public void resetString5Values() {
        string5_f1_on = false;
        string5_f2_on = false;
        string5_f3_on = false;
        string5_f4_on = false;
        string5_f5_on = false;
    }
    public void resetString4Drawables() {
        string4_top.setImageDrawable(stringtop);
        string4_f1.setImageDrawable(string4);
        string4_f2.setImageDrawable(string4);
        string4_f3.setImageDrawable(string4);
        string4_f4.setImageDrawable(string4);
        string4_f5.setImageDrawable(string4);
    }
    public void resetString4Values() {
        string4_f1_on = false;
        string4_f2_on = false;
        string4_f3_on = false;
        string4_f4_on = false;
        string4_f5_on = false;
    }
    public void resetString3Drawables() {
        string3_top.setImageDrawable(stringtop);
        string3_f1.setImageDrawable(string3);
        string3_f2.setImageDrawable(string3);
        string3_f3.setImageDrawable(string3);
        string3_f4.setImageDrawable(string3);
        string3_f5.setImageDrawable(string3);
    }
    public void resetString3Values() {
        string3_f1_on = false;
        string3_f2_on = false;
        string3_f3_on = false;
        string3_f4_on = false;
        string3_f5_on = false;
    }
    public void resetString2Drawables() {
        string2_top.setImageDrawable(stringtop);
        string2_f1.setImageDrawable(string2);
        string2_f2.setImageDrawable(string2);
        string2_f3.setImageDrawable(string2);
        string2_f4.setImageDrawable(string2);
        string2_f5.setImageDrawable(string2);
    }
    public void resetString2Values() {
        string2_f1_on = false;
        string2_f2_on = false;
        string2_f3_on = false;
        string2_f4_on = false;
        string2_f5_on = false;
    }
    public void resetString1Drawables() {
        string1_top.setImageDrawable(stringtop);
        string1_f1.setImageDrawable(string1);
        string1_f2.setImageDrawable(string1);
        string1_f3.setImageDrawable(string1);
        string1_f4.setImageDrawable(string1);
        string1_f5.setImageDrawable(string1);
    }
    public void resetString1Values() {
        string1_f1_on = false;
        string1_f2_on = false;
        string1_f3_on = false;
        string1_f4_on = false;
        string1_f5_on = false;
    }

    public void updateChordText() {
        chord_name = customchord_name.getText().toString();
        if (instrument_text.equals("u") || instrument_text.equals("m")) {
            chord_text = string4_text+string3_text+string2_text+string1_text;
        } else if (instrument_text.equals("g") || instrument_text.equals("u")) {
            chord_text = string6_text+string5_text+string4_text+string3_text+string2_text+string1_text;
        } else {
            chord_text = "xxxxxx";
        }
        customchord_code.setText(chord_text + "_" + fret_text + "_" + instrument_text + "_" + chord_name);
    }

    public void set6String() {
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

    public void set4String() {
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

    public void resetStringTops() {
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

    public void resetStringNotes() {
        string6_text = "x";
        string5_text = "x";
        string4_text = "x";
        string3_text = "x";
        string2_text = "x";
        string1_text = "x";
        fret_text = "0";
    }

    public void onClick(View v) {
        Button b = (Button)v;
        String buttonText = b.getText().toString();
        buttonText = buttonText.replace(getResources().getString(R.string.options_song_delete),"");
        buttonText = buttonText.replace("\n","");
        FullscreenActivity.mCustomChords = FullscreenActivity.mCustomChords.replace(buttonText,"");
        // Save the song
        EditSong.prepareSongXML();
        // Makes sure all & are replaced with &amp;
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&amp;","&");
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&","&amp;");

        // Now write the modified song
        doSave();

        // Refresh the custom chord buttons
        redrawRemoveButtons();
    }

    public void doSave() {
        FileOutputStream overWrite = null;
        try {
            overWrite = new FileOutputStream(FullscreenActivity.dir + "/" + FullscreenActivity.songfilename,false);
            overWrite.write(FullscreenActivity.mynewXML.getBytes());
            overWrite.flush();
            overWrite.close();
            FullscreenActivity.myToastMessage = getResources().getString(R.string.ok);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            FullscreenActivity.myToastMessage = getResources().getString(R.string.no);
        } catch (IOException e) {
            e.printStackTrace();
            FullscreenActivity.myToastMessage = getResources().getString(R.string.no);
        }
        ShowToast.showToast(CustomChord.this);

    }
}
