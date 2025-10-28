package com.garethevans.church.opensongtablet.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import androidx.core.widget.TextViewCompat
import com.garethevans.church.opensongtablet.screensetup.Palette
import com.google.android.material.textview.MaterialTextView

@SuppressLint("PrivateResource")
class MyMaterialSimpleTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : MaterialTextView(context, attrs) {

    init {
        val palette = Palette(context)
        val originalColor = palette.textColor
        attrs?.let {
            // Look for android:textColor in the XML explicitly
            val textColorValue = it.getAttributeValue("http://schemas.android.com/apk/res/android", "textColor")
            if (textColorValue != null) {
                // Attribute exists in XML, safe to read
                context.withStyledAttributes(it, intArrayOf(android.R.attr.textColor)) {
                    palette.textColor = getColor(0, originalColor)
                }
            }
        }
        setPalette(palette)
    }

    fun setPalette(palette: Palette) {
        setTextColor(palette.textColor)
        TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(palette.textColor))
    }
}
