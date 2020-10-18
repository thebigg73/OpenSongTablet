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
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.InputStream;
import java.util.ArrayList;

public class DrawNotes extends View {

    private Context c;
    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private Bitmap canvasBitmap;
    private boolean imageloaded = false;
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
    //private boolean touchisup = false;
    private final Preferences preferences;
    public DrawNotes(Context c, @Nullable AttributeSet attrs) {
        super(FullscreenActivity.mContext, attrs);
        if (c==null) {
            c = FullscreenActivity.mContext;
        }
        preferences = new Preferences();
        setupDrawing(c);
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
        /*if (FullscreenActivity.bmScreen!=null) {
            imagewidth = FullscreenActivity.bmScreen.getWidth();
            imageheight = FullscreenActivity.bmScreen.getHeight();
        }*/

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
        super.onDraw(canvas);
        if (c==null) {
            c = FullscreenActivity.mContext;
        }
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
            setErase(toolList.get(p).equals("eraser"));
            canvas.drawPath(path, drawPaint);
        }

        drawPaint.setColor(getSavedPaintColor(c));
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(getSavedPaintSize(c));
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        setErase(preferences.getMyPreferenceString(c, "drawingTool", "pen").equals("eraser"));
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
        //touchisup = true;
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
        //touchisup = true;
        invalidate();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (c==null) {
            c = FullscreenActivity.mContext;
        }
        float touchX = event.getX();
        float touchY = event.getY();

        if (event.getToolType(0)==MotionEvent.TOOL_TYPE_ERASER) {
            setErase(true);
        } else if (event.getToolType(0)==MotionEvent.TOOL_TYPE_STYLUS) {
            setErase(false);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(c,touchX, touchY);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(c,touchX, touchY);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp(c);
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

    private void touchStart(Context c, float x, float y) {
        if (c==null) {
            c = FullscreenActivity.mContext;
        }
        undonePaths.clear();
        undonecolourList.clear();
        undonesizeList.clear();
        undonetoolList.clear();
        drawPath.reset();
        //touchisup = false;
        setCurrentPaint(c);
        //drawingapath = false;
        FullscreenActivity.saveHighlight = true;
        drawPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(Context c, float x, float y) {
        if (c==null) {
            c = FullscreenActivity.mContext;
        }
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        int TOUCH_TOLERANCE = 4;
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            setCurrentPaint(c);
            drawPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            //drawingapath = true;
            mX = x;
            mY = y;
        }
    }

    private void touchCancel() {
        drawPath = new Path();
        //drawingapath = false;
    }

    private void touchUp(Context c) {
        if (c==null) {
            c = FullscreenActivity.mContext;
        }
        //touchisup = true;
        drawPath.lineTo(mX, mY);
        paths.add(drawPath);
        sizeList.add(getSavedPaintSize(c));
        toolList.add(preferences.getMyPreferenceString(c,"drawingTool","pen"));
        colourList.add(getSavedPaintColor(c));
        drawPath = new Path();
        //drawingapath = false;
    }

    public void setDrawingSize() {
        //private boolean drawingapath = false;
    }

    private int getSavedPaintSize(Context c) {
        if (c==null) {
            c = FullscreenActivity.mContext;
        }
        int size = 20;
        switch (preferences.getMyPreferenceString(c,"drawingTool","pen")) {
            case "pen":
                size = preferences.getMyPreferenceInt(c,"drawingPenSize",20);
                break;
            case "highlighter":
                size = preferences.getMyPreferenceInt(c,"drawingHighlighterSize",20);
                break;
            case "eraser":
                size = preferences.getMyPreferenceInt(c,"drawingEraserSize",20);
                break;
        }
        return size;
    }

    private int getSavedPaintColor(Context c) {
        if (c==null) {
            c = FullscreenActivity.mContext;
        }
        if (preferences.getMyPreferenceString(c,"drawingTool","pen").equals("pen")) {
            return preferences.getMyPreferenceInt(c,"drawingPenColor",StaticVariables.black);

        } else {
            return preferences.getMyPreferenceInt(c, "drawingHighlighterColor", StaticVariables.highlighteryellow);
        }
    }

    private void setupDrawing(Context c) {
        if (c==null) {
            c = FullscreenActivity.mContext;
        }
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(getSavedPaintColor(c));
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(getSavedPaintSize(c));
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        setErase(preferences.getMyPreferenceString(c, "drawingTool", "pen").equals("eraser"));
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        setLayerType(View.LAYER_TYPE_SOFTWARE, canvasPaint);
    }

    public void loadImage(Context c, Uri uri) {
        if (c==null) {
            c = FullscreenActivity.mContext;
        }
        //touchisup = false;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            StorageAccess storageAccess = new StorageAccess();
            InputStream inputStream = storageAccess.getInputStream(c, uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream,null,options);
            if (bitmap != null) {
                canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                bitmap.recycle();
            }
            //drawCanvas = new Canvas(canvasBitmap);
            imageloaded = true;

        } catch (Exception e) {
            Log.d("DrawNotes", "Loading the image-error");
            canvasBitmap = null;
        } catch (OutOfMemoryError oom) {
            Log.d("DrawNotes", "Loading the image-out of memory");
            canvasBitmap = null;
        }
        FullscreenActivity.saveHighlight = false;
        invalidate();
    }

    public void startNew(Context c, Uri uri) {
        if (c==null) {
            c = FullscreenActivity.mContext;
        }
        StorageAccess storageAccess = new StorageAccess();
        //touchisup = true;
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
            if (uri==null || !storageAccess.deleteFile(c,uri)) {
                Log.d("DrawNotes", "Unable to delete old highlighter note");
            }
        } catch (Exception e) {
            Log.d("DrawNotes", "Error trying to delete old highlighter note");
        }
        canvasBitmap = null;
        invalidate();
    }

    private void setCurrentPaint(Context c) {
        if (c==null) {
            c = FullscreenActivity.mContext;
        }
        drawPaint.setColor(getSavedPaintColor(c));
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(getSavedPaintSize(c));
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        setErase(preferences.getMyPreferenceString(c, "drawingTool", "pen").equals("eraser"));


    }
}
