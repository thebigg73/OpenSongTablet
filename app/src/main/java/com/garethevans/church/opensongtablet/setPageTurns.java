package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class setPageTurns extends Activity {

	Button assignPrevious;
	Button assignNext;
	Button assignUp;
	Button assignDown;
	Button assignPad;
	Button assignAutoscroll;
	Button assignMetronome;
	Button toggleScrollBeforeSwipeButton;
	String assignWhich="";
	private boolean PresentMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the user preferences
		Preferences.loadPreferences();
        PresentMode = getIntent().getBooleanExtra("PresentMode", false);
		// Set the screen and title
		setContentView(R.layout.page_turn_buttons);
		getActionBar().setTitle(getResources().getString(R.string.pageturn_title));
		assignPrevious = (Button) findViewById(R.id.prevButton);
		assignNext = (Button) findViewById(R.id.nextButton);
		assignUp = (Button) findViewById(R.id.upButton);
		assignDown = (Button) findViewById(R.id.downButton);
		assignPad = (Button) findViewById(R.id.padButton);
		assignAutoscroll = (Button) findViewById(R.id.autoscrollButton);
		assignMetronome = (Button) findViewById(R.id.metronomeButton);
		toggleScrollBeforeSwipeButton = (Button) findViewById(R.id.toggleScrollBeforeSwipeButton);
		resetButtons();
	}
	
	@Override
	public void onBackPressed() {
		Intent viewsong = new Intent(this, FullscreenActivity.class);
		if (PresentMode) {
			viewsong.setClass(setPageTurns.this, PresentMode.class);			
			startActivity(viewsong);
			this.finish();
		} else {
			viewsong.setClass(setPageTurns.this, FullscreenActivity.class);			
			startActivity(viewsong);
			this.finish();
		}
	    return;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		
		// Reset buttons already using this keycode
		if (FullscreenActivity.pageturner_PREVIOUS == keyCode) {
			FullscreenActivity.pageturner_PREVIOUS = -1;
		} else if (FullscreenActivity.pageturner_NEXT == keyCode) {
			FullscreenActivity.pageturner_NEXT = -1;
		} else if (FullscreenActivity.pageturner_UP == keyCode) {
			FullscreenActivity.pageturner_UP = -1;
		} else if (FullscreenActivity.pageturner_DOWN == keyCode) {
			FullscreenActivity.pageturner_DOWN = -1;
		} else if (FullscreenActivity.pageturner_PAD == keyCode) {
			FullscreenActivity.pageturner_PAD = -1;
		} else if (FullscreenActivity.pageturner_AUTOSCROLL == keyCode) {
			FullscreenActivity.pageturner_AUTOSCROLL = -1;
		} else if (FullscreenActivity.pageturner_METRONOME == keyCode) {
			FullscreenActivity.pageturner_METRONOME = -1;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK && assignWhich.length()>0) {
			//User has pressed the back key - not allowed!!!!
			FullscreenActivity.myToastMessage = getResources().getString(R.string.no);
			ShowToast.showToast(setPageTurns.this);
		} else if (keyCode == KeyEvent.KEYCODE_BACK && assignWhich.length()==0) {
			Intent viewsong = new Intent(this, FullscreenActivity.class);
			startActivity(viewsong);
			finish();
		    return false;
		} else if (assignWhich.equals("prev")) {
			FullscreenActivity.pageturner_PREVIOUS = keyCode;
			Preferences.savePreferences();
		} else if (assignWhich.equals("next")) {
			FullscreenActivity.pageturner_NEXT = keyCode;
			Preferences.savePreferences();
		} else if (assignWhich.equals("up")) {
			FullscreenActivity.pageturner_UP = keyCode;
			Preferences.savePreferences();
		} else if (assignWhich.equals("down")) {
			FullscreenActivity.pageturner_DOWN = keyCode;
			Preferences.savePreferences();
		} else if (assignWhich.equals("pad")) {
			FullscreenActivity.pageturner_PAD = keyCode;
			Preferences.savePreferences();
		} else if (assignWhich.equals("autoscroll")) {
			FullscreenActivity.pageturner_AUTOSCROLL = keyCode;
			Preferences.savePreferences();
		} else if (assignWhich.equals("metronome")) {
			FullscreenActivity.pageturner_METRONOME = keyCode;
			Preferences.savePreferences();
		} 
		assignWhich = "";
		resetButtons();
		return true;
	}


	public void resetButtons() {
		assignPrevious.setEnabled(true);
		assignNext.setEnabled(true);
		assignUp.setEnabled(true);
		assignDown.setEnabled(true);
		assignPad.setEnabled(true);
		assignAutoscroll.setEnabled(true);
		assignMetronome.setEnabled(true);
		if (FullscreenActivity.pageturner_PREVIOUS==-1) {
			assignPrevious.setText(getResources().getString(R.string.pageturn_previous) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset));
		} else {
			assignPrevious.setText(getResources().getString(R.string.pageturn_previous) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + FullscreenActivity.pageturner_PREVIOUS);
		}

		if (FullscreenActivity.pageturner_NEXT==-1) {
			assignNext.setText(getResources().getString(R.string.pageturn_next) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset));
		} else {
			assignNext.setText(getResources().getString(R.string.pageturn_next) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + FullscreenActivity.pageturner_NEXT);
		}

		if (FullscreenActivity.pageturner_UP==-1) {
			assignUp.setText(getResources().getString(R.string.pageturn_up) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset));
		} else {
			assignUp.setText(getResources().getString(R.string.pageturn_up) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + FullscreenActivity.pageturner_UP);
		}

		if (FullscreenActivity.pageturner_DOWN==-1) {
			assignDown.setText(getResources().getString(R.string.pageturn_down) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset));
		} else {
			assignDown.setText(getResources().getString(R.string.pageturn_down) + "\n" + getResources().getString(R.string.currentkeycode) + "=" +  FullscreenActivity.pageturner_DOWN);
		}

		if (FullscreenActivity.pageturner_PAD==-1) {
			assignPad.setText(getResources().getString(R.string.padPedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset));
		} else {
			assignPad.setText(getResources().getString(R.string.padPedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" +  FullscreenActivity.pageturner_PAD);
		}

		if (FullscreenActivity.pageturner_AUTOSCROLL==-1) {
			assignAutoscroll.setText(getResources().getString(R.string.autoscrollPedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset));
		} else {
			assignAutoscroll.setText(getResources().getString(R.string.autoscrollPedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" +  FullscreenActivity.pageturner_AUTOSCROLL);
		}

		if (FullscreenActivity.pageturner_METRONOME==-1) {
			assignMetronome.setText(getResources().getString(R.string.metronomePedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" + getResources().getString(R.string.notset));
		} else {
			assignMetronome.setText(getResources().getString(R.string.metronomePedalText) + "\n" + getResources().getString(R.string.currentkeycode) + "=" +  FullscreenActivity.pageturner_METRONOME);
		}

		if (FullscreenActivity.toggleScrollBeforeSwipe.equals("Y")) {
			toggleScrollBeforeSwipeButton.setText(getResources().getString(R.string.toggleScrollBeforeSwipe) + "\n" + getResources().getString(R.string.currently) + "=" + getResources().getString(R.string.yes));			
		} else {
			toggleScrollBeforeSwipeButton.setText(getResources().getString(R.string.toggleScrollBeforeSwipe) + "\n" + getResources().getString(R.string.currently) + "=" + getResources().getString(R.string.no));			
		}

	}
	
	public void setPrevious (View view) {
		resetButtons();
		assignPrevious.setEnabled(false);
		assignPrevious.setText(getResources().getString(
				R.string.pageturn_waiting));
		assignWhich="prev";
		}
	
	public void setNext (View view) {
		resetButtons();
		assignNext.setEnabled(false);
		assignNext.setText(getResources().getString(
				R.string.pageturn_waiting));
		assignWhich="next";
	}

	public void setUp (View view) {
		resetButtons();
		assignUp.setEnabled(false);
		assignUp.setText(getResources().getString(
				R.string.pageturn_waiting));
		assignWhich="up";
	}

	public void setDown (View view) {
		resetButtons();
		assignDown.setEnabled(false);
		assignDown.setText(getResources().getString(
				R.string.pageturn_waiting));
		assignWhich="down";
	}

	public void setPad (View view) {
		resetButtons();
		assignPad.setEnabled(false);
		assignPad.setText(getResources().getString(
				R.string.pageturn_waiting));
		assignWhich="pad";
	}

	public void setAutoscroll (View view) {
		resetButtons();
		assignAutoscroll.setEnabled(false);
		assignAutoscroll.setText(getResources().getString(
				R.string.pageturn_waiting));
		assignWhich="autoscroll";
	}

	public void setMetronome (View view) {
		resetButtons();
		assignMetronome.setEnabled(false);
		assignMetronome.setText(getResources().getString(
				R.string.pageturn_waiting));
		assignWhich="metronome";
	}

	
	public void toggleScrollBeforeSwipe (View view) {
		if (FullscreenActivity.toggleScrollBeforeSwipe.equals("Y")) {
			FullscreenActivity.toggleScrollBeforeSwipe = "N";
			FullscreenActivity.myToastMessage = getResources().getString(R.string.toggleScrollBeforeSwipeToggle) + " " + getResources().getString(R.string.off);
			ShowToast.showToast(setPageTurns.this);
		} else {
			FullscreenActivity.toggleScrollBeforeSwipe = "Y";
			FullscreenActivity.myToastMessage = getResources().getString(R.string.toggleScrollBeforeSwipeToggle) + " " + getResources().getString(R.string.on);
			ShowToast.showToast(setPageTurns.this);			
		}
		Preferences.savePreferences();
	}

	
	
	public void closePedal (View view) {
		Intent viewsong = new Intent(this, FullscreenActivity.class);
		if (PresentMode) {
			viewsong.setClass(setPageTurns.this, PresentMode.class);			
			startActivity(viewsong);
			this.finish();
		} else {
			viewsong.setClass(setPageTurns.this, FullscreenActivity.class);			
			startActivity(viewsong);
			this.finish();
		}
	}


}
