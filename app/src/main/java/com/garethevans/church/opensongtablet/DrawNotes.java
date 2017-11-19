package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
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
import java.util.ArrayList;

public class DrawNotes extends View {

    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private Bitmap canvasBitmap;
    public boolean imageloaded = false, drawingapath = false;
    int imagewidth = 0, imageheight = 0;
    private ArrayList<Path> paths = new ArrayList<>();
    private ArrayList<Path> undonePaths = new ArrayList<>();
    private ArrayList<Integer> colourList = new ArrayList<>();
    private ArrayList<Integer> undonecolourList = new ArrayList<>();
    private ArrayList<Integer> sizeList = new ArrayList<>();
    private ArrayList<Integer> undonesizeList = new ArrayList<>();
    private ArrayList<String> toolList = new ArrayList<>();
    private ArrayList<String> undonetoolList = new ArrayList<>();
    private float mX;
    private float mY;
    boolean touchisup = false;

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
            try {
                canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                //drawCanvas = new Canvas(canvasBitmap);
                imageloaded = true;
            } catch (OutOfMemoryError e) {
                canvasBitmap = null;
                e.printStackTrace();
            }
        }
        invalidate();
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
        if (canvasBitmap!=null) {
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        }
        for (int p = 0; p < paths.size(); p++) {
            Path path = paths.get(p);
            drawPaint.setColor(colourList.get(p));
            drawPaint.setAntiAlias(true);
            drawPaint.setStrokeWidth(sizeList.get(p));
            drawPaint.setStyle(Paint.Style.STROKE);
            drawPaint.setStrokeJoin(Paint.Join.ROUND);
            drawPaint.setStrokeCap(Paint.Cap.ROUND);
            if (toolList.get(p).equals("eraser")) {
                setErase(true);
            } else {
                setErase(false);
            }
            canvas.drawPath(path, drawPaint);
        }

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
        canvas.drawPath(drawPath, drawPaint);
    }

    public void undo() {
        if (paths.size()>0) {
            undonePaths.add(paths.remove(paths.size()-1));
        }
        if (colourList.size()>0) {
            undonecolourList.add(colourList.remove(colourList.size()-1));
        }
        if (sizeList.size()>0) {
            undonesizeList.add(sizeList.remove(sizeList.size()-1));
        }
        if (toolList.size()>0) {
            undonetoolList.add(toolList.remove(toolList.size()-1));
        }
        touchisup = true;
        invalidate();
    }

    public void redo() {
        if (undonePaths.size()>0) {
            paths.add(undonePaths.remove(undonePaths.size()-1));
        }
        if (undonecolourList.size()>0) {
            colourList.add(undonecolourList.remove(undonecolourList.size()-1));
        }
        if (undonesizeList.size()>0) {
            sizeList.add(undonesizeList.remove(undonesizeList.size()-1));
        }
        if (undonetoolList.size()>0) {
            toolList.add(undonetoolList.remove(undonetoolList.size()-1));
        }
        touchisup = true;
        invalidate();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        if (event.getToolType(0)==MotionEvent.TOOL_TYPE_ERASER) {
            setErase(true);
        } else if (event.getToolType(0)==MotionEvent.TOOL_TYPE_STYLUS) {
            setErase(false);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(touchX, touchY);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(touchX, touchY);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                touchCancel();
                invalidate();
                break;
            default:
                return false;
        }
        return true;
    }

    private void touchStart(float x, float y) {
        undonePaths.clear();
        undonecolourList.clear();
        undonesizeList.clear();
        undonetoolList.clear();
        drawPath.reset();
        touchisup = false;
        setCurrentPaint();
        drawingapath = false;
        FullscreenActivity.saveHighlight = true;
        drawPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        int TOUCH_TOLERANCE = 4;
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            setCurrentPaint();
            drawPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            drawingapath = true;
            mX = x;
            mY = y;
        }
    }

    private void touchCancel() {
        drawPath = new Path();
        drawingapath = false;
    }

    private void touchUp() {
        touchisup = true;
        drawPath.lineTo(mX, mY);
        paths.add(drawPath);
        sizeList.add(getSavedPaintSize());
        toolList.add(FullscreenActivity.drawingTool);
        colourList.add(getSavedPaintColor());
        drawPath = new Path();
        drawingapath = false;
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
        setLayerType(View.LAYER_TYPE_SOFTWARE, canvasPaint);
    }

    public void loadImage(File file) {
        touchisup = false;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(file.toString(), options);
            canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            bitmap.recycle();
            //drawCanvas = new Canvas(canvasBitmap);
            imageloaded = true;
            Log.d("d","Loading the image-success");

        } catch (Exception e) {
            e.printStackTrace();
            //drawCanvas = new Canvas();
            Log.d("d","Loading the image-error");
            canvasBitmap = null;
        } catch (OutOfMemoryError oom) {
            oom.printStackTrace();
            Log.d("d","Loading the image-out of memory");
            canvasBitmap = null;
        }
        FullscreenActivity.saveHighlight = false;
        invalidate();
    }

    public void startNew(File file){
        touchisup = true;
        paths = new ArrayList<>();
        undonePaths = new ArrayList<>();
        colourList = new ArrayList<>();
        undonecolourList = new ArrayList<>();
        sizeList = new ArrayList<>();
        undonesizeList = new ArrayList<>();
        toolList = new ArrayList<>();
        undonetoolList = new ArrayList<>();

        imageloaded = false;
        FullscreenActivity.saveHighlight = false;
        try {
            if (file==null || !file.delete()) {
                Log.d("d","Unable to delete old highlighter note ("+file+")");
            }
        } catch (Exception e) {
            Log.d("d","Error trying to delete old highlighter note ("+file+")");
        }
        canvasBitmap = null;
        invalidate();
    }

    public void setCurrentPaint() {
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


    }
}
