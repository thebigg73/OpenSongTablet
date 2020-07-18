package com.garethevans.church.opensongtablet.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;

public class PrefTextLinkView extends LinearLayout {
    public PrefTextLinkView(Context context, @Nullable AttributeSet attrs) {
        super(context,attrs);
        inflate(context, R.layout.settings_view_linkbutton,this);

        TextView main = findViewById(R.id.mainText);
        TextView sub = findViewById(R.id.subText);

        TypedArray s = context.obtainStyledAttributes(attrs,R.styleable.PrefTextLinkView);
        main.setText(s.getString(R.styleable.PrefTextLinkView_main));
        sub.setText(s.getString(R.styleable.PrefTextLinkView_sub));
        s.recycle();
    }
}
