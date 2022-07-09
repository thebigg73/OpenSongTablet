package com.garethevans.church.opensongtablet.appdata;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class SoftKeyboard {

    private final String TAG = "SoftKeyboard";
    public void showSoftKeyboard(Context c, View view){
        if (view.requestFocus()){
            InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view,InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hideSoftKeyboard(Context c, View view){
        InputMethodManager imm =(InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public interface SoftKeyBoardStatusListener {
        void onKeyBoardShow(View rootView, int totalScreenHeight);
        void onKeyBoardHide(View rootView, int totalScreenHeight);
    }

    private View mChildOfContent;
    private int usableHeightPrevious;
    private int totalScreenHeight;
    private FrameLayout.LayoutParams frameLayoutParams;
    private int currentlyScrolled = 0;

    public SoftKeyboard() {}
    public SoftKeyboard assistActivity (Activity activity, SoftKeyBoardStatusListener listener) {
        return new SoftKeyboard(activity, listener);
    }

    private SoftKeyboard(Activity activity, final SoftKeyBoardStatusListener listener) {
        try {
            FrameLayout content = activity.findViewById(android.R.id.content);
            mChildOfContent = content.getChildAt(0);
            mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(() -> possiblyResizeChildOfContent((MainActivityInterface) activity,listener));
            frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void possiblyResizeChildOfContent(MainActivityInterface mainActivityInterface, SoftKeyBoardStatusListener listener) {
        int usableHeightNow = computeUsableHeight();

        if (usableHeightNow != usableHeightPrevious) {
            totalScreenHeight = mChildOfContent.getRootView().getHeight();

            int heightDifference = usableHeightPrevious - usableHeightNow;

            if (heightDifference > (totalScreenHeight/4)) {
                // keyboard probably just became visible
                Log.d(TAG,"Keyboard visible");
                frameLayoutParams.height = totalScreenHeight - heightDifference;
                listener.onKeyBoardShow(mChildOfContent, totalScreenHeight);
                mainActivityInterface.setWindowFlags(false);
                mChildOfContent.requestLayout();

            } else if (heightDifference < (totalScreenHeight/4) * -1) {
                // keyboard probably just became hidden
                Log.d(TAG,"Keyboard hidden");
                handleShiftDown();
                frameLayoutParams.height = totalScreenHeight;
                mainActivityInterface.setWindowFlags(true);
                mChildOfContent.requestLayout();
                listener.onKeyBoardHide(mChildOfContent, totalScreenHeight);
            }
            usableHeightPrevious = usableHeightNow;
        }

    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return (r.bottom - r.top);
    }

    public void handleShiftDown() {

        currentlyScrolled = 0;
        mChildOfContent.scrollTo(0,0);
    }

    public void handleShiftUp(View focusedView) {

        if (focusedView == null){
            Log.d(TAG,"focusedView is null");
            return;
        }

        int[] location = new int[2];
        focusedView.getLocationInWindow(location);
        int absY = location[1];

        int oneFourth = totalScreenHeight/4;

        if (absY > oneFourth){
            int distanceToScroll = absY - oneFourth + currentlyScrolled;
            currentlyScrolled = distanceToScroll;
            mChildOfContent.scrollTo(0,distanceToScroll);
            Log.d(TAG,"Shift up: "+distanceToScroll);
        }
    }
}
