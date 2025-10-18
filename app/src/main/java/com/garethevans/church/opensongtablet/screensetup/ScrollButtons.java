package com.garethevans.church.opensongtablet.screensetup;

import android.view.View;
import android.widget.ScrollView;

import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ScrollButtons {

    public void showScrollButtons(ScrollView scrollView, MyFloatingActionButton upButton, FloatingActionButton downButton) {
        if (scrollView.getScrollY()==0) {
            upButton.setVisibility(View.GONE);
        } else {
            upButton.setVisibility(View.VISIBLE);
        }
        if (scrollView.getScrollY()>=scrollView.getMaxScrollAmount()) {
            downButton.setVisibility(View.GONE);
        } else {
            downButton.setVisibility(View.VISIBLE);
        }
    }
}
