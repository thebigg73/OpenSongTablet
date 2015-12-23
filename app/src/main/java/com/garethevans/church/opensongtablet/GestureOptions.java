package com.garethevans.church.opensongtablet;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class GestureOptions extends Activity {

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

		numeral = FullscreenActivity.gesture_doubletap;
		numeral2 = FullscreenActivity.gesture_longpress;

		// Set the screen and title
		setContentView(R.layout.choose_gestures);

		try {
            getActionBar().setTitle(getResources().getString(R.string.customgestures));
        } catch (Exception e) {
            e.printStackTrace();
        }

		radioGroup = (RadioGroup) findViewById(R.id.doubleTap);
		radioGroup2 = (RadioGroup) findViewById(R.id.longPress);

		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.doubleTap1:
						numeral = "1";
						break;
					case R.id.doubleTap2:
						numeral = "2";
						break;
					case R.id.doubleTap3:
						numeral = "3";
						break;
					case R.id.doubleTap4:
						numeral = "4";
						break;
					case R.id.doubleTap5:
						numeral = "5";
						break;
					case R.id.doubleTap6:
						numeral = "6";
						break;
					case R.id.doubleTap7:
						numeral = "7";
						break;
					case R.id.doubleTap8:
						numeral = "0";
						break;
				}
			}
		});

		radioGroup2.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.longPress1:
						numeral2 = "1";
						break;
					case R.id.longPress2:
						numeral2 = "2";
						break;
					case R.id.longPress3:
						numeral2 = "3";
						break;
					case R.id.longPress4:
						numeral2 = "4";
						break;
					case R.id.longPress5:
						numeral2 = "5";
						break;
					case R.id.longPress6:
						numeral2 = "6";
						break;
					case R.id.longPress7:
						numeral2 = "7";
						break;
					case R.id.longPress8:
						numeral2 = "0";
						break;
				}
			}
		});

		RadioButton doubleTap1 = (RadioButton) findViewById(R.id.doubleTap1);
		RadioButton doubleTap2 = (RadioButton) findViewById(R.id.doubleTap2);
		RadioButton doubleTap3 = (RadioButton) findViewById(R.id.doubleTap3);
		RadioButton doubleTap4 = (RadioButton) findViewById(R.id.doubleTap4);
		RadioButton doubleTap5 = (RadioButton) findViewById(R.id.doubleTap5);
        RadioButton doubleTap6 = (RadioButton) findViewById(R.id.doubleTap6);
        RadioButton doubleTap7 = (RadioButton) findViewById(R.id.doubleTap7);
        RadioButton doubleTap8 = (RadioButton) findViewById(R.id.doubleTap8);
		RadioButton longPress1 = (RadioButton) findViewById(R.id.longPress1);
		RadioButton longPress2 = (RadioButton) findViewById(R.id.longPress2);
		RadioButton longPress3 = (RadioButton) findViewById(R.id.longPress3);
		RadioButton longPress4 = (RadioButton) findViewById(R.id.longPress4);
		RadioButton longPress5 = (RadioButton) findViewById(R.id.longPress5);
        RadioButton longPress6 = (RadioButton) findViewById(R.id.longPress6);
        RadioButton longPress7 = (RadioButton) findViewById(R.id.longPress7);
        RadioButton longPress8 = (RadioButton) findViewById(R.id.longPress8);

		// Set the appropriate radiobutton
        // 0 will be equivalent to off, the default
		switch (FullscreenActivity.gesture_doubletap) {
			case "1":
                doubleTap1.setChecked(true);
				break;
			case "2":
                doubleTap2.setChecked(true);
				break;
			case "3":
                doubleTap3.setChecked(true);
				break;
			case "4":
                doubleTap4.setChecked(true);
				break;
            case "5":
                doubleTap5.setChecked(true);
                break;
            case "6":
                doubleTap6.setChecked(true);
                break;
            case "7":
                doubleTap7.setChecked(true);
                break;
			default: // or 0
                doubleTap8.setChecked(true);
				break;
		}

		switch (FullscreenActivity.gesture_longpress) {
			case "1":
				longPress1.setChecked(true);
				break;
			case "2":
                longPress2.setChecked(true);
				break;
			case "3":
                longPress3.setChecked(true);
				break;
			case "4":
                longPress4.setChecked(true);
				break;
            case "5":
                longPress5.setChecked(true);
                break;
            case "6":
                longPress6.setChecked(true);
                break;
            case "7":
                longPress7.setChecked(true);
                break;
			default: // or 0
				longPress8.setChecked(true);
				break;
		}
	}

	@Override
	public void onBackPressed() {
		Intent viewsong = new Intent(GestureOptions.this, FullscreenActivity.class);
		startActivity(viewsong);
		finish();
	}


	public void exitGestures(View view) {
		FullscreenActivity.gesture_doubletap = numeral;
		FullscreenActivity.gesture_longpress = numeral2;
		Preferences.savePreferences();
		Intent main = new Intent();
		main.setClass(GestureOptions.this, FullscreenActivity.class);
		startActivity(main);
		finish();
	}

}