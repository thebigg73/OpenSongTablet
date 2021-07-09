package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DialogTitleClose extends LinearLayout implements View.OnClickListener{

    private TextView titleView;
    private FloatingActionButton closeView;
    private BottomSheetDialogFragment bottomSheetDialogFragment;

    public DialogTitleClose(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        super.setOnClickListener(this);

        inflate(context, R.layout.view_dialogtitle_close, this);

        int[] set = new int[]{android.R.attr.text};
        TypedArray a = context.obtainStyledAttributes(attrs, set);
        CharSequence text = a.getText(0);

        identifyViews();

        if (text != null) {
            titleView.setText(text);
        }

        a.recycle();
    }

    private void identifyViews() {
        titleView = findViewById(R.id.titleView);
        closeView = findViewById(R.id.closeView);
    }

    public void setText(String titleText) {
        titleView.setText(titleText);
    }

    public FloatingActionButton getClose() {
        if (bottomSheetDialogFragment!=null) {

        }
        return new FloatingActionButton(titleView.getContext());
    }


    OnClickListener consumerListener = null;
    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        consumerListener = l;
        // DO NOT CALL SUPER HERE
    }

    @Override
    public void onClick(View v) {
        Log.i("dev","perform my custom functions, and then ...");
        if (consumerListener != null) { consumerListener.onClick(v); }
    }
}
