package com.garethevans.church.opensongtablet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class ChordFormat extends AppCompatActivity {
	
	//Variables
	static RadioGroup radioGroup;
	static RadioGroup radioGroup2;
	static String numeral;
	static String numeral2;

    Switch switchAb;
    Switch switchBb;
    Switch switchDb;
    Switch switchEb;
    Switch switchGb;
    Switch switchAbm;
    Switch switchBbm;
    Switch switchDbm;
    Switch switchEbm;
    Switch switchGbm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the user preferences
        Preferences.loadPreferences();

        numeral = FullscreenActivity.chordFormat;
        numeral2 = FullscreenActivity.alwaysPreferredChordFormat;

        // Set the screen and title
        setContentView(R.layout.choose_chordformat);

        // Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        TextView title = (TextView) findViewById(R.id.songandauthor);
        if (ab != null && title != null) {
            ab.setTitle("");
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setDisplayShowTitleEnabled(false);
            title.setText(getResources().getString(R.string.choosechordformat));
        }

        // Set up the preferred chord buttons
        switchAb = (Switch) findViewById(R.id.switchAb);
        switchBb = (Switch) findViewById(R.id.switchBb);
        switchDb = (Switch) findViewById(R.id.switchDb);
        switchEb = (Switch) findViewById(R.id.switchEb);
        switchGb = (Switch) findViewById(R.id.switchGb);
        switchAbm = (Switch) findViewById(R.id.switchAbm);
        switchBbm = (Switch) findViewById(R.id.switchBbm);
        switchDbm = (Switch) findViewById(R.id.switchDbm);
        switchEbm = (Switch) findViewById(R.id.switchEbm);
        switchGbm = (Switch) findViewById(R.id.switchGbm);

        setSwitches(FullscreenActivity.prefChord_Aflat_Gsharp, switchAb);
        setSwitches(FullscreenActivity.prefChord_Bflat_Asharp, switchBb);
        setSwitches(FullscreenActivity.prefChord_Dflat_Csharp, switchDb);
        setSwitches(FullscreenActivity.prefChord_Eflat_Dsharp, switchEb);
        setSwitches(FullscreenActivity.prefChord_Gflat_Fsharp, switchGb);
        setSwitches(FullscreenActivity.prefChord_Aflatm_Gsharpm, switchAbm);
        setSwitches(FullscreenActivity.prefChord_Bflatm_Asharpm, switchBbm);
        setSwitches(FullscreenActivity.prefChord_Dflatm_Csharpm, switchDbm);
        setSwitches(FullscreenActivity.prefChord_Eflatm_Dsharpm, switchEbm);
        setSwitches(FullscreenActivity.prefChord_Gflatm_Fsharpm, switchGbm);

        radioGroup = (RadioGroup) findViewById(R.id.chordFormat);
        radioGroup2 = (RadioGroup) findViewById(R.id.chordFormat_decideaction);

        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.chordFormat1:
                        numeral = "1";
                        break;
                    case R.id.chordFormat2:
                        numeral = "2";
                        break;
                    case R.id.chordFormat3:
                        numeral = "3";
                        break;
                    case R.id.chordFormat4:
                        numeral = "4";
                        break;
                    case R.id.chordFormat5:
                        numeral = "5";
                        break;
                }
            }
        });

        radioGroup2.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.chordformat_check:
                        numeral2 = "N";
                        break;
                    case R.id.chordformat_default:
                        numeral2 = "Y";
                        break;
                }
            }
        });

        RadioButton radioButton1 = (RadioButton) findViewById(R.id.chordFormat1);
        RadioButton radioButton2 = (RadioButton) findViewById(R.id.chordFormat2);
        RadioButton radioButton3 = (RadioButton) findViewById(R.id.chordFormat3);
        RadioButton radioButton4 = (RadioButton) findViewById(R.id.chordFormat4);
        RadioButton radioButton5 = (RadioButton) findViewById(R.id.chordFormat5);
        RadioButton radioButton6 = (RadioButton) findViewById(R.id.chordformat_check);
        RadioButton radioButton7 = (RadioButton) findViewById(R.id.chordformat_default);

        // Set the appropriate radiobutton
        switch (FullscreenActivity.chordFormat) {
            case "1":
                radioButton1.setChecked(true);
                break;
            case "2":
                radioButton2.setChecked(true);
                break;
            case "3":
                radioButton3.setChecked(true);
                break;
            case "4":
                radioButton4.setChecked(true);
                break;
            case "5":
                radioButton5.setChecked(true);
                break;
        }

        if (FullscreenActivity.alwaysPreferredChordFormat.equals("N")) {
            radioButton6.setChecked(true);
        } else {
            radioButton7.setChecked(true);
        }
    }

    @Override
	public void onBackPressed() {
		Intent viewsong = new Intent(ChordFormat.this, FullscreenActivity.class);
		startActivity(viewsong);
		finish();
    }

	public void exitChordFormat(View view) {
        if (switchAb.isChecked()) {
            FullscreenActivity.prefChord_Aflat_Gsharp = "#";
        } else {
            FullscreenActivity.prefChord_Aflat_Gsharp = "b";
        }
        if (switchBb.isChecked()) {
            FullscreenActivity.prefChord_Bflat_Asharp = "#";
        } else {
            FullscreenActivity.prefChord_Bflat_Asharp = "b";
        }
        if (switchDb.isChecked()) {
            FullscreenActivity.prefChord_Dflat_Csharp = "#";
        } else {
            FullscreenActivity.prefChord_Dflat_Csharp = "b";
        }
        if (switchEb.isChecked()) {
            FullscreenActivity.prefChord_Eflat_Dsharp = "#";
        } else {
            FullscreenActivity.prefChord_Eflat_Dsharp = "b";
        }
        if (switchGb.isChecked()) {
            FullscreenActivity.prefChord_Gflat_Fsharp = "#";
        } else {
            FullscreenActivity.prefChord_Gflat_Fsharp = "b";
        }
        if (switchAbm.isChecked()) {
            FullscreenActivity.prefChord_Aflatm_Gsharpm = "#";
        } else {
            FullscreenActivity.prefChord_Aflatm_Gsharpm = "b";
        }
        if (switchBbm.isChecked()) {
            FullscreenActivity.prefChord_Bflatm_Asharpm = "#";
        } else {
            FullscreenActivity.prefChord_Bflatm_Asharpm = "b";
        }
        if (switchDbm.isChecked()) {
            FullscreenActivity.prefChord_Dflatm_Csharpm = "#";
        } else {
            FullscreenActivity.prefChord_Dflatm_Csharpm = "b";
        }
        if (switchEbm.isChecked()) {
            FullscreenActivity.prefChord_Eflatm_Dsharpm = "#";
        } else {
            FullscreenActivity.prefChord_Eflatm_Dsharpm = "b";
        }
        if (switchGbm.isChecked()) {
            FullscreenActivity.prefChord_Gflatm_Fsharpm = "#";
        } else {
            FullscreenActivity.prefChord_Gflatm_Fsharpm = "b";
        }

        FullscreenActivity.chordFormat = numeral;
        FullscreenActivity.alwaysPreferredChordFormat = numeral2;
		Preferences.savePreferences();
        Intent main = new Intent();
		main.setClass(ChordFormat.this, FullscreenActivity.class);
		startActivity(main);
		finish();
	}

    public void setSwitches(String what, Switch myswitch) {
        if (what.equals("b")) {
            myswitch.setChecked(false);
        } else {
            myswitch.setChecked(true);
        }
    }
}