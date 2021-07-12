package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;

public class MaterialTextView extends LinearLayout {

    private final TextView textView;
    private final TextView hintView;
    private final ImageView imageView;
    private final ImageView checkMark;

    public MaterialTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_material_textview, this);

        textView = findViewById(R.id.textView);
        hintView = findViewById(R.id.hintView);
        imageView = findViewById(R.id.imageView);
        checkMark = findViewById(R.id.checkMark);

        int[] set = new int[] {android.R.attr.text, android.R.attr.hint};
        TypedArray typedArray = context.obtainStyledAttributes(attrs,set);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaterialTextView);
        Drawable drawable = a.getDrawable(R.styleable.MaterialTextView_mydrawable);
        boolean isChecked = a.getBoolean(R.styleable.MaterialTextView_showCheckMark,false);

        String mainText = typedArray.getString(0);
        if (mainText!=null) {
            textView.setText(mainText);
        }

        String hintText = typedArray.getString(1);
        if (hintText!=null) {
            hintView.setText(hintText);
        }

        if (drawable!=null) {
            imageView.setImageDrawable(drawable);
            imageView.setVisibility(View.VISIBLE);
        }

        showCheckMark(isChecked);

        typedArray.recycle();
        a.recycle();
    }

    public void setHintText(String hintText) {
        hintView.setText(hintText);
    }

    public void setMainText(String mainText) {
        textView.setText(mainText);
    }

    public void showCheckMark(boolean isChecked) {
        if (isChecked) {
            checkMark.setVisibility(View.VISIBLE);
        } else {
            checkMark.setVisibility(View.GONE);
        }
    }
}

//
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.graphics.drawable.Drawable;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.Nullable;
//
//import com.garethevans.church.opensongtablet.R;
//
//public class MaterialTextView extends LinearLayout {
//
//    private TextView main;
//    private TextView sub;
//    private ImageView imageView;
//
//    public MaterialTextView(Context context, @Nullable AttributeSet attrs) {
//        super(context,attrs);
//        inflate(context, R.layout.view_material_textview,this);
//
//        int[] set = new int[]{android.R.attr.text, android.R.attr.hint, android.R.attr.src};
//        TypedArray a = context.obtainStyledAttributes(attrs, set);
//        CharSequence text = a.getText(0);
//        CharSequence hint = a.getText(1);
//        //int drawableId = a.getResourceId(2,R.drawable.ic_bullseye_white_36dp);
//
//        main = findViewById(R.id.mainText);
//        sub = findViewById(R.id.subText);
//        imageView = findViewById(R.id.imageView);
//
//        Log.d("MaterialTextView","imageView="+imageView);
//        //Log.d("MaterialTextView","drawableId="+drawableId);
//
//        if (text!=null) {
//            main.setText(text);
//        }
//        if (hint!=null) {
//            sub.setText(hint);
//        }
//        /*if (drawableId!=0) {
//            Drawable drawable = AppCompatResources.getDrawable(imageView.getContext(),drawableId);
//            imageView.setImageDrawable(drawable);
//            imageView.setVisibility(View.VISIBLE);
//            Log.d("MaterialTextView","should work...");
//
//        }*/
//
//        a.recycle();
//    }
//
//
//
//
//
//    public void setImageDrawable(Drawable drawable) {
//        imageView.setImageDrawable(drawable);
//    }
//}