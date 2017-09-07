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
import java.util.ArrayList;

public class DrawNotes extends View {

    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    public boolean imageloaded = false, drawingapath = false;
    int imagewidth = 0, imageheight = 0;
    private ArrayList<Path> currentMove = null;
    private int currentColor;
    private ArrayList<Path> undoMoves = null;
    private ArrayList<Path> allMoves = null;
    private ArrayList<Integer> colourList = null;
    private ArrayList<Integer> sizeList = null;
    private ArrayList<String> toolList = null;

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
        //
        //canvas.drawPath(drawPath, drawPaint);

        Log.d("d","currentMove="+currentMove);

        if (currentMove!=null && drawingapath) {
            Log.d("d","currentMove.size()="+currentMove.size());
            for (int p = 0; p < currentMove.size(); p++) {
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
                canvas.drawPath(currentMove.get(p), drawPaint);
            }
        }

        if (!drawingapath && allMoves!=null) {
            Log.d("d","allMoves.size()="+allMoves.size());
            for (int p = 0; p < allMoves.size(); p++) {
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
                canvas.drawPath(allMoves.get(p), drawPaint);
            }
        }

    }

    public void undo() {
        if (allMoves!=null && undoMoves!=null && allMoves.size() > 0) {
            undoMoves.add(allMoves.remove(allMoves.size() - 1));
            invalidate();
        }
    }

    public void redo() {
        if (allMoves!=null && undoMoves!=null && undoMoves.size()>0) {
            allMoves.add(undoMoves.remove(undoMoves.size() - 1));
            invalidate();
        }
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
                Log.d("d","ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                drawingapath = true;
                currentMove.add(drawPath);
                allMoves.add(drawPath);
                drawCanvas.drawPath(drawPath, drawPaint);
                colourList.add(getSavedPaintColor());
                sizeList.add(getSavedPaintSize());
                toolList.add(FullscreenActivity.drawingTool);
                Log.d("d","ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.d("d","ACTION_UP");
                Log.d("d","drawCanvas.getWidth="+drawCanvas.getWidth());
                Log.d("d","drawCanvas.getHeight="+drawCanvas.getHeight());
                drawPath.lineTo(touchX, touchY);
                if (!drawingapath) {
                    // To show taps if no path is drawn
                    drawCanvas.drawPoint(touchX, touchY, drawPaint);
                    //currentMove.add(drawPath);
                } else {
                    currentMove.add(drawPath);
                }
                currentMove.add(drawPath);
                drawCanvas.drawPath(drawPath, drawPaint);
                colourList.add(getSavedPaintColor());
                currentMove.clear();
                allMoves.add(drawPath);
                sizeList.add(getSavedPaintSize());
                toolList.add(FullscreenActivity.drawingTool);
                drawPath.reset();
                drawingapath = false;
                drawPath = new Path();
                drawPaint = new Paint();
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
        currentMove = new ArrayList<>();
        undoMoves = new ArrayList<>();
        allMoves = new ArrayList<>();
        colourList = new ArrayList<>();
        toolList = new ArrayList<>();
        sizeList = new ArrayList<>();
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    public void loadImage(File file) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(file.toString(), options);
            canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            bitmap.recycle();
            drawCanvas = new Canvas(canvasBitmap);
            imageloaded = true;
        } catch (Exception e) {
            e.printStackTrace();
            drawCanvas = new Canvas();
        } catch (OutOfMemoryError oom) {
            oom.printStackTrace();
        }
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
