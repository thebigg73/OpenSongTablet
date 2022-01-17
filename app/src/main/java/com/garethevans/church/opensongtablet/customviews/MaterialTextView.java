package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.R;

public class MaterialTextView extends LinearLayout {

    private final TextView textView;
    private final TextView hintView;
    private final ImageView checkMark, imageView;
    private final CheckBox checkBox;
    private final FrameLayout checkBoxHolder;
    private final String TAG = "MaterialTextView";

    public MaterialTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_material_textview, this);

        textView = findViewById(R.id.textView);
        hintView = findViewById(R.id.hintView);
        imageView = findViewById(R.id.imageView);
        checkMark = findViewById(R.id.checkMark);
        checkBox = findViewById(R.id.checkBox);
        checkBoxHolder = findViewById(R.id.checkBoxHolder);

        textView.setId(View.generateViewId());
        hintView.setId(View.generateViewId());
        imageView.setId(View.generateViewId());
        checkMark.setId(View.generateViewId());
        checkBox.setId(View.generateViewId());
        checkBoxHolder.setId(View.generateViewId());

        int[] set = new int[] {android.R.attr.text, android.R.attr.hint};
        TypedArray typedArray = context.obtainStyledAttributes(attrs,set);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaterialTextView);
        Drawable drawable = a.getDrawable(R.styleable.MaterialTextView_mydrawable);
        boolean isChecked = a.getBoolean(R.styleable.MaterialTextView_showCheckMark,false);
        boolean isCheckBox = a.getBoolean(R.styleable.MaterialTextView_showCheckBox, false);
        String mainText = typedArray.getString(0);
        textView.setText(mainText);

        String hintText = typedArray.getString(1);
        hintView.setText(hintText);

        // Default colour is white, but it can be overriden programmatically
        setImageView(drawable, context.getResources().getColor(R.color.white));

        showCheckMark(isChecked);
        showCheckBox(isCheckBox);

        typedArray.recycle();
        a.recycle();
    }

    public void setHint(String hintText) {
        if (hintText==null || hintText.isEmpty()) {
            hintView.setVisibility(View.GONE);
        } else {
            hintView.setVisibility(View.VISIBLE);
        }
        hintView.setText(hintText);
    }

    public void setHintColor(int color) {
        hintView.setTextColor(color);
    }

    public void setText(String mainText) {
        if (mainText==null || mainText.isEmpty()) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
        }
        textView.setText(mainText);
    }

    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    public void showCheckMark(boolean isChecked) {
        if (isChecked) {
            checkMark.setVisibility(View.VISIBLE);
        } else {
            checkMark.setVisibility(View.GONE);
        }
    }

    public void showCheckBox(boolean isCheckBox) {
        if (isCheckBox) {
            checkBoxHolder.setVisibility(View.VISIBLE);
        } else {
            checkBoxHolder.setVisibility(View.GONE);
        }
    }

    public void setCheckBox(boolean checked) {
        checkBox.setChecked(checked);
    }

    public boolean isChecked() {
        return checkBox.isChecked();
    }

    public CharSequence getText() {
        return textView.getText();
    }

    public CharSequence getHint() {
        return hintView.getText();
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(Drawable drawable, int tintColor) {
        RequestOptions requestOptions = new RequestOptions().override(64, 64).centerInside();
        Log.d(TAG,"drawable="+drawable);
        if (drawable!=null) {
            // Clone the drawable
            Drawable cloneDrawable = drawable.mutate();
            // Setup color filter for tinting
            ColorFilter colorFilter = new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
            if (cloneDrawable != null) {
                cloneDrawable.setColorFilter(colorFilter);
                GlideApp.with(this).load(cloneDrawable).apply(requestOptions).into(imageView);
            }
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }


    public void setMyGravity(int gravity) {
        textView.setGravity(gravity);
        hintView.setGravity(gravity);
    }
}
