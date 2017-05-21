package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ShowToast {

	public static void showToast(Context view) {
		if (FullscreenActivity.myToastMessage!=null && !FullscreenActivity.myToastMessage.equals("")) {
			Toast toast = Toast.makeText(view, FullscreenActivity.myToastMessage, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			FullscreenActivity.myToastMessage = null;
			FullscreenActivity.myToastMessage = "";
		}
	}
}