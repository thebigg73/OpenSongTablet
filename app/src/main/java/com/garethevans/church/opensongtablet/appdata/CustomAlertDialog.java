package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

public class CustomAlertDialog {

    /**
     * Shows a fully styled MaterialAlertDialog with themed title, message, buttons, and background.
     *
     * @param context       Context for the dialog
     * @param title         Dialog title text
     * @param message       Dialog message text
     * @param positiveClick Optional listener for positive button (nullable)
     * @param negativeClick Optional listener for negative button (nullable)
     * @param iconId        The icon to show
     */

    public static void showStyledDialog(
            Context context,
            MainActivityInterface mainActivityInterface,
            String title,
            String message,
            @Nullable DialogInterface.OnClickListener positiveClick,
            @Nullable DialogInterface.OnClickListener negativeClick,
            int iconId) {
        Drawable icon = AppCompatResources.getDrawable(context, iconId);
        if (icon != null) {
            DrawableCompat.setTint(icon, mainActivityInterface.getPalette().textColor);
        }
        // --- Build the dialog ---
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.okay), positiveClick)
                .setNegativeButton(context.getString(R.string.cancel), negativeClick)
                .setIcon(icon)
                .create();

        // --- Show first so buttons and layout exist ---
        dialog.show();

        // --- Apply full custom styling ---
        applyMaterialStyle(dialog, mainActivityInterface, title);

    }

    /**
     * Applies theme-based Material styling to an existing AlertDialog.
     */
    private static void applyMaterialStyle(AlertDialog dialog, MainActivityInterface mainActivityInterface, String title) {
        // --- Background with rounded corners ---
        if (dialog.getWindow() != null) {
            MaterialShapeDrawable background = new MaterialShapeDrawable();
            background.setFillColor(ColorStateList.valueOf(mainActivityInterface.getPalette().secondary));
            background.setElevation(24f);
            background.setShapeAppearanceModel(
                    ShapeAppearanceModel.builder()
                            .setAllCornerSizes(28f)
                            .build()
            );
            dialog.getWindow().setBackgroundDrawable(background);
        }

        // --- Title color (Spannable) ---
        SpannableString titleSpan = new SpannableString(title);
        titleSpan.setSpan(
                new ForegroundColorSpan(mainActivityInterface.getPalette().textColor),
                0, titleSpan.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        dialog.setTitle(titleSpan);

        // --- Message text color ---
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setTextColor(mainActivityInterface.getPalette().textColor);
        }

        // --- Button text colors ---
        int[] buttonIds = {
                AlertDialog.BUTTON_POSITIVE,
                AlertDialog.BUTTON_NEGATIVE
        };

        for (int id : buttonIds) {
            Button button = dialog.getButton(id);
            if (button != null) {
                button.setTextColor(mainActivityInterface.getPalette().textColor);
                if (button instanceof MaterialButton) {
                    ((MaterialButton) button).setRippleColor(ColorStateList.valueOf(mainActivityInterface.getPalette().secondaryVariant));
                }
            }
        }
    }
}
