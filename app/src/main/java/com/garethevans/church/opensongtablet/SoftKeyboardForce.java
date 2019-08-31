package com.garethevans.church.opensongtablet;

import android.inputmethodservice.InputMethodService;

public class SoftKeyboardForce extends InputMethodService {

	public boolean onEvaluateInputViewShown() {
		super.onEvaluateInputViewShown();
	    return true;
	}
}
