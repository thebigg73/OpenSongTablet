package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

public class ColorChooserView extends View {
        private Paint paint;
        private ComposeShader shader;
        private final float[] color = { 1.f, 1.f, 1.f };

        public ColorChooserView(Context context){
                super(context);
        }

        public ColorChooserView(Context context, AttributeSet attrs) {
                super(context, attrs);
        }

        public ColorChooserView(Context context, AttributeSet attrs, int defStyle) {
                super(context, attrs, defStyle);
        }

        private void initialise() {
                int rgb = Color.HSVToColor(color);
                Shader luar = new LinearGradient(0.f, 0.f, 0.f, this.getMeasuredHeight(), 0xffffffff, 0xff000000, TileMode.CLAMP);
                Shader dalam = new LinearGradient(0.f, 0.f, this.getMeasuredWidth(), 0.f, 0xffffffff, rgb, TileMode.CLAMP);
                shader = new ComposeShader(luar, dalam, PorterDuff.Mode.MULTIPLY);
        }

        @Override
        protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                if (paint == null) {
                        paint = new Paint();
                }
                if (shader == null) {
                        initialise();
                }
                paint.setShader(shader);
                canvas.drawRect(0.f, 0.f, this.getMeasuredWidth(), this.getMeasuredHeight(), paint);
        }

        @Override
        public boolean performClick() {
                super.performClick();
                return false;
        }

        public void setHue(float hue) {
                color[0] = hue;
                invalidate();
        }
}
