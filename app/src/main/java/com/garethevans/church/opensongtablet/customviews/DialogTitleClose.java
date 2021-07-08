package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DialogTitleClose extends LinearLayout {

    private final TextView title;
    private final FloatingActionButton close;

    public DialogTitleClose(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_dialogtitle_close,this);

        int[] set = new int[]{android.R.attr.text, android.R.attr.hint, android.R.attr.icon};
        TypedArray a = context.obtainStyledAttributes(attrs, set);
        CharSequence text = a.getText(0);

        title = findViewById(R.id.title);
        close = findViewById(R.id.close);

        if (text!=null) {
            title.setText(text);
        }

        a.recycle();
    }

    public void setText(String titleText) {
        title.setText(titleText);
    }

    public FloatingActionButton getClose() {
        return close;
    }

    public void closeAction(BottomSheetDialogFragment closeThis) {
        closeThis.dismiss();
    }
}
