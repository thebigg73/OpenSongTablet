package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class FloatingActionButtonBehaviour extends CoordinatorLayout.Behavior<FloatingActionButton> {

    public FloatingActionButtonBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        Log.d("d","Called behaviour");
        return !(dependency instanceof Snackbar.SnackbarLayout);
        //return false;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        //float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        //child.setTranslationY(translationY);
        Log.d("d","Called dependentviewchanged");
        return true;
    }
}
