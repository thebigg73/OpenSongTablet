/*
package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

import br.com.sapereaude.maskedEditText.MaskedEditText;
class AmbilWarnaDialog {
	interface OnAmbilWarnaListener {
		void onCancel(AmbilWarnaDialog dialog);
		void onOk(AmbilWarnaDialog dialog, int color);
		}

	private final AlertDialog dialog;
	private final boolean supportsAlpha;
	private final OnAmbilWarnaListener listener;
	private final View viewHue, viewNewColor, viewAlphaOverlay;
	private final AmbilWarnaSquare viewSatVal;
	private final ImageView viewCursor, viewAlphaCursor, viewTarget, viewAlphaCheckered;
	private final ViewGroup viewContainer;
	private final MaskedEditText colorhex;
    private static String hexColor;
	private final float[] currentColorHsv = new float[3], newColorHsv = new float[3];
	private int alpha, mColor;
    //Context context;

	AmbilWarnaDialog(final Context context, int color, OnAmbilWarnaListener listener) {
		this(context, color, false, listener);
		}

	@SuppressLint("ClickableViewAccessibility")
	private AmbilWarnaDialog(final Context context, int color, boolean supportsAlpha, OnAmbilWarnaListener listener) {
		this.supportsAlpha = supportsAlpha;
		this.listener = listener;
        mColor = color;

		if (!supportsAlpha) { // remove alpha if not supported
			color = color | 0xff000000;
			}

		Color.colorToHSV(color, currentColorHsv);
		alpha = Color.alpha(color);
        hexColor = String.format("%06X",(0xFFFFFF & color));


        @SuppressLint("InflateParams") final View view = LayoutInflater.from(context).inflate(R.layout.ambilwarna_dialog, null);
		viewHue = view.findViewById(R.id.ambilwarna_viewHue);
		viewSatVal = view.findViewById(R.id.ambilwarna_viewSatBri);
		viewCursor = view.findViewById(R.id.ambilwarna_cursor);
		View viewOldColor = view.findViewById(R.id.ambilwarna_oldColor);
		viewNewColor = view.findViewById(R.id.ambilwarna_newColor);
		viewTarget = view.findViewById(R.id.ambilwarna_target);
		viewContainer = view.findViewById(R.id.ambilwarna_viewContainer);
		viewAlphaOverlay = view.findViewById(R.id.ambilwarna_overlay);
		viewAlphaCursor = view.findViewById(R.id.ambilwarna_alphaCursor);
		viewAlphaCheckered = view.findViewById(R.id.ambilwarna_alphaCheckered);
        colorhex = view.findViewById(R.id.colorhex);

        colorhex.setText(hexColor);
        colorhex.setKeepHint(true);
        colorhex.setHint(hexColor);

        colorhex.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                // Checks the textView matches the appropriate format
                String gettext = textView.getText().toString().toUpperCase(Locale.ROOT);
                int mycolor;
                try {
                    mycolor = Color.parseColor("#FF"+gettext);
                } catch (Exception e) {
                    mycolor = mColor;
                }
                hexColor = String.format("%06X",(0xFFFFFF & mycolor));
                mColor = mycolor;
                textView.setText(hexColor);
                Color.colorToHSV(mycolor, newColorHsv);

                // update view
                viewSatVal.setHue(getNewHue());
                moveTargetNew();
                moveCursorNew();
                viewNewColor.setBackgroundColor(mycolor);
                updateAlphaView();
                currentColorHsv[0] = newColorHsv[0];
                currentColorHsv[1] = newColorHsv[1];
                currentColorHsv[2] = newColorHsv[2];
                return true;
            }
        });

		{ // hide/show alpha
			viewAlphaOverlay.setVisibility(supportsAlpha? View.VISIBLE: View.GONE);
			viewAlphaCursor.setVisibility(supportsAlpha? View.VISIBLE: View.GONE);
			viewAlphaCheckered.setVisibility(supportsAlpha? View.VISIBLE: View.GONE);
			}

		viewSatVal.setHue(getHue());
		viewOldColor.setBackgroundColor(color);
		viewNewColor.setBackgroundColor(color);

		viewHue.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE
						|| event.getAction() == MotionEvent.ACTION_DOWN
						|| event.getAction() == MotionEvent.ACTION_UP) {

					float y = event.getY();
					if (y < 0.f) y = 0.f;
					if (y > viewHue.getMeasuredHeight()) {
						y = viewHue.getMeasuredHeight() - 0.001f; // to avoid jumping the cursor from bottom to top.
						}
					float hue = 360.f - 360.f / viewHue.getMeasuredHeight() * y;
					if (hue == 360.f) hue = 0.f;
					setHue(hue);

					// update view
					viewSatVal.setHue(getHue());
					moveCursor();
					viewNewColor.setBackgroundColor(getColor());
					updateAlphaView();

					return true;
					}
				return false;
				}
			});

		if (supportsAlpha) viewAlphaCheckered.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_MOVE)
						|| (event.getAction() == MotionEvent.ACTION_DOWN)
						|| (event.getAction() == MotionEvent.ACTION_UP)) {

					float y = event.getY();
					if (y < 0.f) {
						y = 0.f;
						}
					if (y > viewAlphaCheckered.getMeasuredHeight()) {
						y = viewAlphaCheckered.getMeasuredHeight() - 0.001f; // to avoid jumping the cursor from bottom to top.
						}
					final int a = Math.round(255.f - ((255.f / viewAlphaCheckered.getMeasuredHeight()) * y));
					AmbilWarnaDialog.this.setAlpha(a);

					// update view
					moveAlphaCursor();
					int col = AmbilWarnaDialog.this.getColor();
					int c = a << 24 | col & 0x00ffffff;
					viewNewColor.setBackgroundColor(c);
					return true;
					}
				return false;
				}
			});
		viewSatVal.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE
						|| event.getAction() == MotionEvent.ACTION_DOWN
						|| event.getAction() == MotionEvent.ACTION_UP) {

					float x = event.getX(); // touch event are in dp units.
					float y = event.getY();

					if (x < 0.f) x = 0.f;
					if (x > viewSatVal.getMeasuredWidth()) x = viewSatVal.getMeasuredWidth();
					if (y < 0.f) y = 0.f;
					if (y > viewSatVal.getMeasuredHeight()) y = viewSatVal.getMeasuredHeight();

					setSat(1.f / viewSatVal.getMeasuredWidth() * x);
					setVal(1.f - (1.f / viewSatVal.getMeasuredHeight() * y));

					// update view
					moveTarget();
					viewNewColor.setBackgroundColor(getColor());

					return true;
					}
				return false;
				}
			});

		dialog = new AlertDialog.Builder(context)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				if (AmbilWarnaDialog.this.listener != null) {
					AmbilWarnaDialog.this.listener.onOk(AmbilWarnaDialog.this, getColor());
					}
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (AmbilWarnaDialog.this.listener != null) {
						AmbilWarnaDialog.this.listener.onCancel(AmbilWarnaDialog.this);
						}
					}
				})
				.setOnCancelListener(new OnCancelListener() {
					// if back button is used, call back our listener.
					@Override
					public void onCancel(DialogInterface paramDialogInterface) {
						if (AmbilWarnaDialog.this.listener != null) {
							AmbilWarnaDialog.this.listener.onCancel(AmbilWarnaDialog.this);
							}

					}
					})
					.create();
		// kill all padding from the dialog window
		dialog.setView(view, 0, 0, 0, 0);

		// move cursor & target on first draw
		ViewTreeObserver vto = view.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				moveCursor();
				if (AmbilWarnaDialog.this.supportsAlpha) moveAlphaCursor();
				moveTarget();
				if (AmbilWarnaDialog.this.supportsAlpha) updateAlphaView();
				view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			});
		}

	private void moveCursor() {
		float y = viewHue.getMeasuredHeight() - (getHue() * viewHue.getMeasuredHeight() / 360.f);
		if (y == viewHue.getMeasuredHeight()) y = 0.f;
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewCursor.getLayoutParams();
		layoutParams.leftMargin = (int) (viewHue.getLeft() - Math.floor(viewCursor.getMeasuredWidth() / 2.0f) - viewContainer.getPaddingLeft());
		layoutParams.topMargin = (int) (viewHue.getTop() + y - Math.floor(viewCursor.getMeasuredHeight() / 2.0f) - viewContainer.getPaddingTop());
		viewCursor.setLayoutParams(layoutParams);
		}

    private void moveCursorNew() {
        float y = viewHue.getMeasuredHeight() - (getNewHue() * viewHue.getMeasuredHeight() / 360.f);
        if (y == viewHue.getMeasuredHeight()) y = 0.f;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewCursor.getLayoutParams();
        layoutParams.leftMargin = (int) (viewHue.getLeft() - Math.floor(viewCursor.getMeasuredWidth() / 2.0f) - viewContainer.getPaddingLeft());
        layoutParams.topMargin = (int) (viewHue.getTop() + y - Math.floor(viewCursor.getMeasuredHeight() / 2.0f) - viewContainer.getPaddingTop());
        viewCursor.setLayoutParams(layoutParams);
    }

    private void moveTarget() {
		float x = getSat() * viewSatVal.getMeasuredWidth();
		float y = (1.f - getVal()) * viewSatVal.getMeasuredHeight();
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewTarget.getLayoutParams();
		layoutParams.leftMargin = (int) (viewSatVal.getLeft() + x - Math.floor(viewTarget.getMeasuredWidth() / 2.0f) - viewContainer.getPaddingLeft());
		layoutParams.topMargin = (int) (viewSatVal.getTop() + y - Math.floor(viewTarget.getMeasuredHeight() / 2.0f) - viewContainer.getPaddingTop());
		viewTarget.setLayoutParams(layoutParams);
		}

    private void moveTargetNew() {
        float x = getNewSat() * viewSatVal.getMeasuredWidth();
        float y = (1.f - getNewVal()) * viewSatVal.getMeasuredHeight();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewTarget.getLayoutParams();
        layoutParams.leftMargin = (int) (viewSatVal.getLeft() + x - Math.floor(viewTarget.getMeasuredWidth() / 2.0f) - viewContainer.getPaddingLeft());
        layoutParams.topMargin = (int) (viewSatVal.getTop() + y - Math.floor(viewTarget.getMeasuredHeight() / 2.0f) - viewContainer.getPaddingTop());
        viewTarget.setLayoutParams(layoutParams);
    }

    private void moveAlphaCursor() {
		final int measuredHeight = this.viewAlphaCheckered.getMeasuredHeight();
		float y = measuredHeight - ((this.getAlpha() * measuredHeight) / 255.f);
		final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.viewAlphaCursor.getLayoutParams();
		layoutParams.leftMargin = (int) (this.viewAlphaCheckered.getLeft() - Math.floor(this.viewAlphaCursor.getMeasuredWidth() / 2.0f) - this.viewContainer.getPaddingLeft());
		layoutParams.topMargin = (int) ((this.viewAlphaCheckered.getTop() + y) - Math.floor(this.viewAlphaCursor.getMeasuredHeight() / 2.0f) - this.viewContainer.getPaddingTop());

		this.viewAlphaCursor.setLayoutParams(layoutParams);
		}

	private int getColor() {
		final int argb = Color.HSVToColor(currentColorHsv);
        hexColor = String.format("%06X", (0xFFFFFF & argb));
        colorhex.setText(hexColor);
		return alpha << 24 | (argb & 0x00ffffff);
		}

	private float getHue() {
		return currentColorHsv[0];
    }

    private float getNewHue() {
        return newColorHsv[0];
    }

	private float getAlpha() {
		return this.alpha;
    }

	private float getSat() {
		return currentColorHsv[1];
    }

    private float getNewSat() {
        return newColorHsv[1];
    }

    private float getVal() {
		return currentColorHsv[2];
		}

    private float getNewVal() {
        return newColorHsv[2];
    }

    private void setHue(float hue) {
		currentColorHsv[0] = hue;
		}

	private void setSat(float sat) {
		currentColorHsv[1] = sat;
		}

	private void setAlpha(int alpha) {
		this.alpha = alpha;
		}

	private void setVal(float val) {
		currentColorHsv[2] = val;
		}

	public void show() {
		dialog.show();
		}

	*/
/*public AlertDialog getDialog() {
		return dialog;
		}*//*


	//@SuppressWarnings("deprecation")
	private void updateAlphaView() {
		final GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {
				Color.HSVToColor(currentColorHsv), 0x0
				});
		//viewAlphaOverlay.setBackgroundDrawable(gd);
		viewAlphaOverlay.setBackground(gd);
		}
}*/
