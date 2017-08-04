package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;

public class DrawNotes extends View {

    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    public boolean imageloaded = false, drawingapath = false;
    int imagewidth = 0, imageheight = 0;

    public DrawNotes(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w==0) {
            w = 1;
        }
        if (h==0) {
            h = 1;
        }
        if (FullscreenActivity.bmScreen!=null) {
            imagewidth = FullscreenActivity.bmScreen.getWidth();
            imageheight = FullscreenActivity.bmScreen.getHeight();
        }

        if (!imageloaded) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            drawCanvas = new Canvas(canvasBitmap);
            imageloaded = true;
        }
    }

    public void setErase(boolean isErase){
        if (isErase) {
            drawPaint.setColor(Color.WHITE);
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else {
            drawPaint.setXfermode(null);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPaint.setColor(getSavedPaintColor());
                drawPaint.setStrokeWidth(getSavedPaintSize());
                drawPath.moveTo(touchX, touchY);
                FullscreenActivity.saveHighlight = true;
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                drawingapath = true;
                break;
            case MotionEvent.ACTION_UP:
                if (!drawingapath) {
                    // To show taps if no path is drawn
                    drawCanvas.drawPoint(touchX, touchY, drawPaint);
                }
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                drawingapath = false;
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    public void setDrawingSize(int w, int h) {
        imagewidth = w;
        imageheight = h;
    }

    public int getSavedPaintSize() {
        int size = 20;
        switch (FullscreenActivity.drawingTool) {
            case "pen":
                size = FullscreenActivity.drawingPenSize;
                break;
            case "highlighter":
                size = FullscreenActivity.drawingHighlightSize;
                break;
            case "eraser":
                size = FullscreenActivity.drawingEraserSize;
                break;
        }
        return size;
    }

    public int getSavedPaintColor() {
        int c = 0xFFFFFF00;
        if (FullscreenActivity.drawingTool.equals("pen")) {
            switch (FullscreenActivity.drawingPenColor) {
                case "black":
                    c = 0xFF000000;
                    break;

                case "white":
                    c = 0xFFFFFFFF;
                    break;

                case "yellow":
                    c = 0xFFFFFF00;
                    break;

                case "red":
                    c = 0xFFFF0000;
                    break;

                case "green":
                    c = 0xFF00FF00;
                    break;

                case "blue":
                    c = 0xFF0000FF;
                    break;
            }
        } else {
            switch (FullscreenActivity.drawingHighlightColor) {
                case "black":
                    c = 0x66000000;
                    break;

                case "white":
                    c = 0x66FFFFFF;
                    break;

                case "yellow":
                    c = 0x66FFFF00;
                    break;

                case "red":
                    c = 0x66FF0000;
                    break;

                case "green":
                    c = 0x6600FF00;
                    break;

                case "blue":
                    c = 0x660000FF;
                    break;
            }
        }
        return c;
    }

    public void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(getSavedPaintColor());
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(getSavedPaintSize());
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        if (FullscreenActivity.drawingTool.equals("eraser")) {
            setErase(true);
        } else {
            setErase(false);
        }
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    public void loadImage(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(file.toString(), options);
        canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        bitmap.recycle();
        drawCanvas = new Canvas(canvasBitmap);
        imageloaded = true;
        FullscreenActivity.saveHighlight = false;
    }

    public void startNew(File file){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        imageloaded = false;
        FullscreenActivity.saveHighlight = false;
        try {
            if (file==null || !file.delete()) {
                Log.d("d","Unable to delete old highlighter note ("+file+")");
            }
        } catch (Exception e) {
            Log.d("d","Error trying to delete old highlighter note ("+file+")");
        }
        invalidate();
    }
}
