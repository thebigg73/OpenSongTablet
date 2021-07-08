package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;

public class MaterialTextView extends LinearLayout {

    private final TextView main;
    private final TextView sub;
    private final ImageView myicon;

    public MaterialTextView(Context context, @Nullable AttributeSet attrs) {
        super(context,attrs);
        inflate(context, R.layout.view_linkbutton,this);

        int[] set = new int[]{android.R.attr.text, android.R.attr.hint, android.R.attr.icon};
        TypedArray a = context.obtainStyledAttributes(attrs, set);
        CharSequence text = a.getText(0);
        CharSequence hint = a.getText(1);
        Drawable icon = a.getDrawable(2);

        main = findViewById(R.id.mainText);
        sub = findViewById(R.id.subText);
        myicon = findViewById(R.id.myicon);

        if (text!=null) {
            main.setText(text);
        }
        if (hint!=null) {
            sub.setText(hint);
        }
        if (icon!=null) {
            myicon.setImageDrawable(icon);
        }

        a.recycle();
    }

    public void setText(String text) {
        Log.d("TextView","text="+text);
        main.setText(text);
    }

    public void setHint(String hint) {
        sub.setText(hint);
    }

    public void setImageDrawable(Drawable drawable) {
        myicon.setImageDrawable(drawable);
    }
}