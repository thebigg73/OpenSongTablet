package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ShowToast  extends Activity {

	public static void showToast(Context view) {
		if (FullscreenActivity.myToastMessage!=null && !FullscreenActivity.myToastMessage.equals("")) {
			try {
                CoordinatorLayout rootview = ((Activity) view).getWindow().getDecorView().findViewById(R.id.coordinator_layout);
				//FrameLayout rootview = ((Activity) view).getWindow().getDecorView().findViewById(R.id.coordinator_layout);
				DrawerLayout dl = ((Activity) view).getWindow().getDecorView().findViewById(R.id.drawer_layout);
                LinearLayout sm = ((Activity) view).getWindow().getDecorView().findViewById(R.id.songmenu);
                LinearLayout om = ((Activity) view).getWindow().getDecorView().findViewById(R.id.optionmenu);

                DrawerTweaks.closeMyDrawers(dl,sm,om,"both");

			    //CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) rootview.getChildAt(0).getLayoutParams();
			    //params.setMargins(params.leftMargin,params.topMargin,params.rightMargin,params.bottomMargin-((Activity) view).getWindow().getDecorView().getNa);

                // Set the length.  Default is short, unless it meets certain criteria
                int length = Snackbar.LENGTH_SHORT;
                if (FullscreenActivity.myToastMessage.contains(view.getResources().getString(R.string.edit_song_capo) + " " + FullscreenActivity.mCapo)) {
                    length = Snackbar.LENGTH_LONG;
                }
                Snackbar mySnackbar = Snackbar.make(rootview,FullscreenActivity.myToastMessage,length);
                mySnackbar.show();

				//Toast toast = Toast.makeText(view, FullscreenActivity.myToastMessage, Toast.LENGTH_LONG);
				//toast.setGravity(Gravity.CENTER, 0, 0);
				//toast.show();

				FullscreenActivity.myToastMessage = null;
				FullscreenActivity.myToastMessage = "";
			} catch (Exception e) {
				Log.d("d","Error showing toast message");
				e.printStackTrace();
			}
		}
	}
}