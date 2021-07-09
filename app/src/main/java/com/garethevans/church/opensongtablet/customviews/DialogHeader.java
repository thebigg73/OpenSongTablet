package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DialogHeader extends LinearLayout implements View.OnClickListener {

    private final TextView textView;
    private BottomSheetDialogFragment bottomSheetDialogFragment;

    public DialogHeader(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_dialog_header, this);

        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);
        textView = findViewById(R.id.textView);

        floatingActionButton.setClickable(true);
        floatingActionButton.setOnClickListener(this);

        int[] set = new int[]{android.R.attr.text};
        TypedArray a = context.obtainStyledAttributes(attrs, set);

        CharSequence text = a.getText(0);
        if (text!=null) {
            textView.setText(text);
        }

        a.recycle();
    }

    public void setText(String titleText) {
        textView.setText(titleText);
    }

    public void setClose(BottomSheetDialogFragment thisFragment) {
        bottomSheetDialogFragment = thisFragment;
    }

    @Override
    public void onClick(View v) {
        if (bottomSheetDialogFragment != null) {
            bottomSheetDialogFragment.dismiss();
        }

    }
}
