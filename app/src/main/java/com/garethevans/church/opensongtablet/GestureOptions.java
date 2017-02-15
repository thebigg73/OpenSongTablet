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
import android.widget.TextView;

public class GestureOptions extends AppCompatActivity {

	//Variables
	RadioGroup radioGroup;
    RadioGroup radioGroup2;
    RadioGroup radioGroup3;
    RadioGroup radioGroup4;
    RadioGroup radioGroup5;
    RadioGroup radioGroup6;
	static String numeral;
	static String numeral2;
    static String numeral3;
    static String numeral4;
    static String numeral5;
    static String numeral6;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the user preferences
		Preferences.loadPreferences();

		numeral = FullscreenActivity.gesture_doubletap;
		numeral2 = FullscreenActivity.gesture_longpress;
        numeral3 = FullscreenActivity.longpresspreviouspedalgesture;
        numeral4 = FullscreenActivity.longpressnextpedalgesture;
        numeral5 = FullscreenActivity.longpressuppedalgesture;
        numeral6 = FullscreenActivity.longpressdownpedalgesture;

		// Set the screen and title
		setContentView(R.layout.choose_gestures);

		// Set up the toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar ab = getSupportActionBar();

		TextView title = (TextView) findViewById(R.id.songandauthor);
		if (ab != null && title != null) {
			ab.setTitle("");
			ab.setDisplayHomeAsUpEnabled(false);
			ab.setDisplayShowTitleEnabled(false);
			title.setText(getResources().getString(R.string.customgestures));
		}

		radioGroup = (RadioGroup) findViewById(R.id.doubleTap);
		radioGroup2 = (RadioGroup) findViewById(R.id.longPress);
		radioGroup3 = (RadioGroup) findViewById(R.id.longPressPrevious);
		radioGroup4 = (RadioGroup) findViewById(R.id.longPressNext);
		radioGroup5 = (RadioGroup) findViewById(R.id.longPressUp);
		radioGroup6 = (RadioGroup) findViewById(R.id.longPressDown);

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
		radioGroup3.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.longPressPrevious1:
						numeral3 = "1";
						break;
					case R.id.longPressPrevious2:
						numeral3 = "2";
						break;
					case R.id.longPressPrevious3:
						numeral3 = "3";
						break;
					case R.id.longPressPrevious4:
						numeral3 = "4";
						break;
					case R.id.longPressPrevious5:
						numeral3 = "5";
						break;
					case R.id.longPressPrevious6:
						numeral3 = "6";
						break;
					case R.id.longPressPrevious7:
						numeral3 = "7";
						break;
					case R.id.longPressPrevious8:
						numeral3 = "0";
						break;
				}
			}
		});
		radioGroup4.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.longPressNext1:
						numeral4 = "1";
						break;
					case R.id.longPressNext2:
						numeral4 = "2";
						break;
					case R.id.longPressNext3:
						numeral4 = "3";
						break;
					case R.id.longPressNext4:
						numeral4 = "4";
						break;
					case R.id.longPressNext5:
						numeral4 = "5";
						break;
					case R.id.longPressNext6:
						numeral4 = "6";
						break;
					case R.id.longPressNext7:
						numeral4 = "7";
						break;
					case R.id.longPressNext8:
						numeral4 = "0";
						break;
				}
			}
		});
		radioGroup5.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.longPressUp1:
						numeral5 = "1";
						break;
					case R.id.longPressUp2:
						numeral5 = "2";
						break;
					case R.id.longPressUp3:
						numeral5 = "3";
						break;
					case R.id.longPressUp4:
						numeral5 = "4";
						break;
					case R.id.longPressUp5:
						numeral5 = "5";
						break;
					case R.id.longPressUp6:
						numeral5 = "6";
						break;
					case R.id.longPressUp7:
						numeral5 = "7";
						break;
					case R.id.longPressUp8:
						numeral5 = "0";
						break;
				}
			}
		});
		radioGroup6.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.longPressDown1:
						numeral6 = "1";
						break;
					case R.id.longPressDown2:
						numeral6 = "2";
						break;
					case R.id.longPressDown3:
						numeral6 = "3";
						break;
					case R.id.longPressDown4:
						numeral6 = "4";
						break;
					case R.id.longPressDown5:
						numeral6 = "5";
						break;
					case R.id.longPressDown6:
						numeral6 = "6";
						break;
					case R.id.longPressDown7:
						numeral6 = "7";
						break;
					case R.id.longPressDown8:
						numeral6 = "0";
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
		RadioButton longPressUp1 = (RadioButton) findViewById(R.id.longPressUp1);
		RadioButton longPressUp2 = (RadioButton) findViewById(R.id.longPressUp2);
		RadioButton longPressUp3 = (RadioButton) findViewById(R.id.longPressUp3);
		RadioButton longPressUp4 = (RadioButton) findViewById(R.id.longPressUp4);
		RadioButton longPressUp5 = (RadioButton) findViewById(R.id.longPressUp5);
		RadioButton longPressUp6 = (RadioButton) findViewById(R.id.longPressUp6);
		RadioButton longPressUp7 = (RadioButton) findViewById(R.id.longPressUp7);
		RadioButton longPressUp8 = (RadioButton) findViewById(R.id.longPressUp8);
		RadioButton longPressDown1 = (RadioButton) findViewById(R.id.longPressDown1);
		RadioButton longPressDown2 = (RadioButton) findViewById(R.id.longPressDown2);
		RadioButton longPressDown3 = (RadioButton) findViewById(R.id.longPressDown3);
		RadioButton longPressDown4 = (RadioButton) findViewById(R.id.longPressDown4);
		RadioButton longPressDown5 = (RadioButton) findViewById(R.id.longPressDown5);
		RadioButton longPressDown6 = (RadioButton) findViewById(R.id.longPressDown6);
		RadioButton longPressDown7 = (RadioButton) findViewById(R.id.longPressDown7);
		RadioButton longPressDown8 = (RadioButton) findViewById(R.id.longPressDown8);
		RadioButton longPressPrevious1 = (RadioButton) findViewById(R.id.longPressPrevious1);
		RadioButton longPressPrevious2 = (RadioButton) findViewById(R.id.longPressPrevious2);
		RadioButton longPressPrevious3 = (RadioButton) findViewById(R.id.longPressPrevious3);
		RadioButton longPressPrevious4 = (RadioButton) findViewById(R.id.longPressPrevious4);
		RadioButton longPressPrevious5 = (RadioButton) findViewById(R.id.longPressPrevious5);
		RadioButton longPressPrevious6 = (RadioButton) findViewById(R.id.longPressPrevious6);
		RadioButton longPressPrevious7 = (RadioButton) findViewById(R.id.longPressPrevious7);
		RadioButton longPressPrevious8 = (RadioButton) findViewById(R.id.longPressPrevious8);
		RadioButton longPressNext1 = (RadioButton) findViewById(R.id.longPressNext1);
		RadioButton longPressNext2 = (RadioButton) findViewById(R.id.longPressNext2);
		RadioButton longPressNext3 = (RadioButton) findViewById(R.id.longPressNext3);
		RadioButton longPressNext4 = (RadioButton) findViewById(R.id.longPressNext4);
		RadioButton longPressNext5 = (RadioButton) findViewById(R.id.longPressNext5);
		RadioButton longPressNext6 = (RadioButton) findViewById(R.id.longPressNext6);
		RadioButton longPressNext7 = (RadioButton) findViewById(R.id.longPressNext7);
		RadioButton longPressNext8 = (RadioButton) findViewById(R.id.longPressNext8);

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

		switch (FullscreenActivity.longpresspreviouspedalgesture) {
			case "1":
				longPressPrevious1.setChecked(true);
				break;
			case "2":
				longPressPrevious2.setChecked(true);
				break;
			case "3":
				longPressPrevious3.setChecked(true);
				break;
			case "4":
				longPressPrevious4.setChecked(true);
				break;
			case "5":
				longPressPrevious5.setChecked(true);
				break;
			case "6":
				longPressPrevious6.setChecked(true);
				break;
			case "7":
				longPressPrevious7.setChecked(true);
				break;
			default: // or 0
				longPressPrevious8.setChecked(true);
				break;
		}

		switch (FullscreenActivity.longpressnextpedalgesture) {
			case "1":
				longPressNext1.setChecked(true);
				break;
			case "2":
				longPressNext2.setChecked(true);
				break;
			case "3":
				longPressNext3.setChecked(true);
				break;
			case "4":
				longPressNext4.setChecked(true);
				break;
			case "5":
				longPressNext5.setChecked(true);
				break;
			case "6":
				longPressNext6.setChecked(true);
				break;
			case "7":
				longPressNext7.setChecked(true);
				break;
			default: // or 0
				longPressNext8.setChecked(true);
				break;
		}

		switch (FullscreenActivity.longpressuppedalgesture) {
			case "1":
				longPressUp1.setChecked(true);
				break;
			case "2":
				longPressUp2.setChecked(true);
				break;
			case "3":
				longPressUp3.setChecked(true);
				break;
			case "4":
				longPressUp4.setChecked(true);
				break;
			case "5":
				longPressUp5.setChecked(true);
				break;
			case "6":
				longPressUp6.setChecked(true);
				break;
			case "7":
				longPressUp7.setChecked(true);
				break;
			default: // or 0
				longPressUp8.setChecked(true);
				break;
		}

		switch (FullscreenActivity.longpressdownpedalgesture) {
			case "1":
				longPressDown1.setChecked(true);
				break;
			case "2":
				longPressDown2.setChecked(true);
				break;
			case "3":
				longPressDown3.setChecked(true);
				break;
			case "4":
				longPressDown4.setChecked(true);
				break;
			case "5":
				longPressDown5.setChecked(true);
				break;
			case "6":
				longPressDown6.setChecked(true);
				break;
			case "7":
				longPressDown7.setChecked(true);
				break;
			default: // or 0
				longPressDown8.setChecked(true);
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
        FullscreenActivity.longpresspreviouspedalgesture = numeral3;
        FullscreenActivity.longpressnextpedalgesture = numeral4;
        FullscreenActivity.longpressuppedalgesture = numeral5;
        FullscreenActivity.longpressdownpedalgesture = numeral6;

		Preferences.savePreferences();
		Intent main = new Intent();
		main.setClass(GestureOptions.this, FullscreenActivity.class);
		startActivity(main);
		finish();
	}

}