package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ChordFormat extends Activity {
	
	//Variables
	static RadioGroup radioGroup;
	static RadioGroup radioGroup2;
	static String numeral;
	static String numeral2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Load the user preferences
		Preferences.loadPreferences();
		
		numeral = FullscreenActivity.chordFormat;
		numeral2 = FullscreenActivity.alwaysPreferredChordFormat;
		
		// Set the screen and title
		setContentView(R.layout.choose_chordformat);

		getActionBar().setTitle(getResources().getString(R.string.choosechordformat));
		radioGroup = (RadioGroup) findViewById(R.id.chordFormat);
		radioGroup2 = (RadioGroup) findViewById(R.id.chordFormat_decideaction);
		
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
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
	            }
			}
		});

		radioGroup2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
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
		RadioButton radioButton5 = (RadioButton) findViewById(R.id.chordformat_check);
		RadioButton radioButton6 = (RadioButton) findViewById(R.id.chordformat_default);
		
		// Set the appropriate radiobutton
		if (FullscreenActivity.chordFormat.equals("1")) {
			radioButton1.setChecked(true);
		} else if (FullscreenActivity.chordFormat.equals("2")) {
			radioButton2.setChecked(true);
		} else if (FullscreenActivity.chordFormat.equals("3")) {
			radioButton3.setChecked(true);
		} else if (FullscreenActivity.chordFormat.equals("4")) {
			radioButton4.setChecked(true);
		}
		
		if (FullscreenActivity.alwaysPreferredChordFormat.equals("N")) {
			radioButton5.setChecked(true);
		} else {
			radioButton6.setChecked(true);
		}
}
	
	@Override
	public void onBackPressed() {
		Intent viewsong = new Intent(ChordFormat.this, FullscreenActivity.class);
		startActivity(viewsong);
		finish();
	    return;
	}

	
	public void exitChordFormat(View view) {
        FullscreenActivity.chordFormat = numeral;
        FullscreenActivity.alwaysPreferredChordFormat = numeral2;
		Preferences.savePreferences();
        Intent main = new Intent();
		main.setClass(ChordFormat.this, FullscreenActivity.class);
		startActivity(main);
		finish();
		
	}
/*    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            String numeral = null;
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
            }
            FullscreenActivity.chordFormat = numeral;
            Preferences.savePreferences();
            Intent main = new Intent();
			main.setClass(ChordFormat.this, FullscreenActivity.class);
			startActivity(main);
			finish();
    }*/
}