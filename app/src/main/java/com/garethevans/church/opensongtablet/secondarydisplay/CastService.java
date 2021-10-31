package com.garethevans.church.opensongtablet.secondarydisplay;


import android.util.Log;
import android.view.Display;

import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;

// All of the classes after initialisation are the same for the PresentationService and PresentationServiceHDMI files
// They both call functions in the PresentationCommon file
// Both files are needed as they initialise and communicate with the display differently, but after that the stuff is almost entirely identical

public class CastService extends CastRemoteDisplayLocalService {

    DisplayInterface displayInterface;
    MainActivityInterface mainActivityInterface;
    MyCastDisplay myCastDisplay;

    private final String TAG = "PresentationService";
    private Display display;

    public CastService(){}

    public CastService(MainActivityInterface mainActivityInterface) {
        this.mainActivityInterface = mainActivityInterface;
    }

    public MyCastDisplay getMyCastDisplay() {
        return myCastDisplay;
    }

    public void updateMainActivityInterface(MainActivityInterface mainActivityInterface) {
        this.mainActivityInterface = mainActivityInterface;
        Log.d(TAG,"mainActivityInterface="+mainActivityInterface);
        Log.d(TAG,"diplay=" + display);
    }
    @Override
    public void onCreatePresentation(@NonNull Display display) {
        this.display = display;
        createPresentation(display);
    }

    public void createPresentation(Display display) {
        myCastDisplay = new MyCastDisplay(this, display, mainActivityInterface);
        myCastDisplay.show();
        //this.mainActivityInterface = mainActivityInterface;
        /*dismissPresentation();
        myCastDisplay = new MyCastDisplay(this, display, mainActivityInterface);
        try {
            myCastDisplay.show();
            mainActivityInterface.getPresentationCommon().setIsPresenting(true);

        } catch (WindowManager.InvalidDisplayException ex) {
            ex.printStackTrace();
            dismissPresentation();
            mainActivityInterface.getPresentationCommon().setIsPresenting(false);
        }*/
    }

    @Override
    public void onDismissPresentation() {
        //mainActivityInterface.getPresentationCommon().setIsPresenting(false);
        dismissPresentation();
    }

    @Override
    public void onDestroy() {
        if (myCastDisplay!=null) {
            try {
                myCastDisplay.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /*if (mainActivityInterface!=null) {
            mainActivityInterface.getPresentationCommon().setIsPresenting(false);
        }*/
    }

    private void dismissPresentation() {
        if (myCastDisplay != null) {
            myCastDisplay.dismiss();
            myCastDisplay = null;
        }
        //mainActivityInterface.getPresentationCommon().setIsPresenting(false);
    }


}
