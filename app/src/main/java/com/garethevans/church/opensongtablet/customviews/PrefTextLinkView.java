package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;

public class PrefTextLinkView extends LinearLayout {
    public PrefTextLinkView(Context context, @Nullable AttributeSet attrs) {
        super(context,attrs);
        inflate(context, R.layout.view_linkbutton,this);

        TextView main = findViewById(R.id.mainText);
        TextView sub = findViewById(R.id.subText);
        ImageView myicon = findViewById(R.id.myicon);

        TypedArray s = context.obtainStyledAttributes(attrs,R.styleable.PrefTextLinkView);
        main.setText(s.getString(R.styleable.PrefTextLinkView_main));
        sub.setText(s.getString(R.styleable.PrefTextLinkView_sub));
        if (s.getDrawable(R.styleable.PrefTextLinkView_myicon)!=null) {
            myicon.setImageDrawable(s.getDrawable(R.styleable.PrefTextLinkView_myicon));
            myicon.setVisibility(View.VISIBLE);
        }
        s.recycle();
    }
}