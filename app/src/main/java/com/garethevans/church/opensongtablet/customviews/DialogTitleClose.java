package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;

public class DialogTitleClose extends LinearLayout {

    public DialogTitleClose(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_dialogtitle_close,this);

        TextView title = findViewById(R.id.title);

        TypedArray s = context.obtainStyledAttributes(attrs,R.styleable.DialogTitle);
        title.setText(s.getString(R.styleable.DialogTitle_title));
        s.recycle();
    }
}
