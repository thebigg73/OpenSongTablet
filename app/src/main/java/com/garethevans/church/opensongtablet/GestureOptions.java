package com.garethevans.church.opensongtablet;

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

		getActionBar().setTitle(getResources().getString(R.string.customgestures));
		radioGroup = (RadioGroup) findViewById(R.id.doubleTap);
		radioGroup2 = (RadioGroup) findViewById(R.id.longPress);
		
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
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
	            }
			}
		});

		radioGroup2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
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
	            }
			}
		});

		RadioButton radioButton1 = (RadioButton) findViewById(R.id.doubleTap1);
		RadioButton radioButton2 = (RadioButton) findViewById(R.id.doubleTap2);
		RadioButton radioButton3 = (RadioButton) findViewById(R.id.doubleTap3);
		RadioButton radioButton4 = (RadioButton) findViewById(R.id.doubleTap4);
		RadioButton radioButton5 = (RadioButton) findViewById(R.id.doubleTap5);
		RadioButton radioButton6 = (RadioButton) findViewById(R.id.longPress1);
		RadioButton radioButton7 = (RadioButton) findViewById(R.id.longPress2);
		RadioButton radioButton8 = (RadioButton) findViewById(R.id.longPress3);
		RadioButton radioButton9 = (RadioButton) findViewById(R.id.longPress4);
		RadioButton radioButton10 = (RadioButton) findViewById(R.id.longPress5);
		
		// Set the appropriate radiobutton
		if (FullscreenActivity.gesture_doubletap.equals("1")) {
			radioButton1.setChecked(true);
		} else if (FullscreenActivity.gesture_doubletap.equals("2")) {
			radioButton2.setChecked(true);
		} else if (FullscreenActivity.gesture_doubletap.equals("3")) {
			radioButton3.setChecked(true);
		} else if (FullscreenActivity.gesture_doubletap.equals("4")) {
			radioButton4.setChecked(true);
		} else {
			radioButton5.setChecked(true);
		}
		
		if (FullscreenActivity.gesture_longpress.equals("1")) {
			radioButton6.setChecked(true);
		} else if (FullscreenActivity.gesture_longpress.equals("2")) {
			radioButton7.setChecked(true);
		} else if (FullscreenActivity.gesture_longpress.equals("3")) {
			radioButton8.setChecked(true);
		} else if (FullscreenActivity.gesture_longpress.equals("4")) {
			radioButton9.setChecked(true);
		} else {
			radioButton10.setChecked(true);
		}
}
	
	@Override
	public void onBackPressed() {
		Intent viewsong = new Intent(GestureOptions.this, FullscreenActivity.class);
		startActivity(viewsong);
		finish();
	    return;
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