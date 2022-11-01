package com.garethevans.church.opensongtablet.customviews;

// This class deals with a drawing canvas

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SwipeDrawingInterface;

import java.util.ArrayList;

public class DrawNotes extends View {

    // So we can enable/disable the view
    private boolean enabled = true;

    // The inteface to communication with the main activity
    private final SwipeDrawingInterface swipeDrawingInterface;

    // Existing highlighter file
    boolean loadExisting = false;
    Bitmap existingHighlighterFile;
    Paint bitmapPaint;

    // Background screenshot

    // Values for the current drawing
    private boolean currentlyDrawing = false;
    private Path currentPath;
    private Paint currentPaint;
    private boolean isErase = false;

    // For holding the paths and infos
    private ArrayList<Path> allPaths;
    private ArrayList<Paint> allPaints;

    // For the undo memory
    private ArrayList<Path> undoPaths;
    private ArrayList<Paint> undoPaints;

    // For animated swipe gesture (only used in SwipeFragment)
    private Paint currentSwipePaint;
    private ArrayList<Path> allSwipePaths;

    // For onTouch events
    private float startX;
    private float startY;

    // Values for swipe
    private boolean startSwipe = true;
    private float startXSwipe;
    private float startYSwipe;
    private long startTime;
    private boolean animateSwipe = false;  // Used to decide in on draw if we show the user or animate drawing

    // For tweaking how onDraw works
    private int updateFrom = 0;
    public boolean delayClear = false;

    // For the image background (when annotating)
    private int canvasWidth, canvasHeight;


    public DrawNotes(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        swipeDrawingInterface = (SwipeDrawingInterface) context;
        setFocusable(true);
        setFocusableInTouchMode(true);
        initialiseArrays();
        initialiseCurrentObjs();
    }

    private void initialiseArrays() {
        allPaths = new ArrayList<>();
        allPaints = new ArrayList<>();
        undoPaths = new ArrayList<>();
        undoPaints = new ArrayList<>();
        allSwipePaths = new ArrayList<>();
    }
    private void initialiseCurrentObjs() {
        currentPath = new Path();
        currentPaint = new Paint();
        currentPaint.setColor(0xffffffff);
        currentPaint.setStrokeWidth(12);
        currentSwipePaint = new Paint();
        currentSwipePaint.setAlpha(100);
        currentSwipePaint.setColor(0x66ff0000);
        currentSwipePaint.setStrokeWidth(20);
        currentPaint.setAntiAlias(true);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentSwipePaint.setAntiAlias(true);
        currentSwipePaint.setStyle(Paint.Style.STROKE);
        currentSwipePaint.setStrokeJoin(Paint.Join.ROUND);
        currentSwipePaint.setStrokeCap(Paint.Cap.ROUND);
        setLayerType(View.LAYER_TYPE_SOFTWARE, currentPaint);
        setLayerType(View.LAYER_TYPE_SOFTWARE, bitmapPaint);
    }

    public void resetVars() {
        initialiseArrays();
        initialiseCurrentObjs();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasWidth = w;
        canvasHeight = h;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // The getters
    public int getCanvasWidth() {
        return canvasWidth;
    }
    public int getCanvasHeight() {
        return canvasHeight;
    }

    // The setters
    public void setCurrentPaint(int size, int color) {
        currentPaint.setStrokeWidth(size);
        currentPaint.setColor(color);
    }
    public void resetSwipe() {
        allSwipePaths.clear();
        allSwipePaths = new ArrayList<>();
        postInvalidate();
    }
    public void setSwipeAnimate(boolean animate) {
        animateSwipe = animate;
    }
    public void addToSwipePaths(Path path) {
        allSwipePaths.add(path);
        postInvalidate();
    }
    public void setErase(boolean isErase){
        // isErase is determined by preferences.getMyPreferenceString(c,"drawingTool","pen").equals("eraser")
        this.isErase = isErase;
        if (isErase) {
            currentPaint.setColor(0x66666666);
            currentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else {
            currentPaint.setXfermode(null);
        }
    }

    // This bit actually does the drawing to screen called when view is initialised or invalidated
    protected void onDraw(Canvas canvas) {
        if (existingHighlighterFile != null) {
            canvas.drawBitmap(existingHighlighterFile,0,0, bitmapPaint);
        }

        if (animateSwipe) {
            // Animating a simulated swipe
            for (int y = 0; y < allSwipePaths.size(); y++) {
                canvas.drawPath(allSwipePaths.get(y), currentSwipePaint);
            }

        } else {
            if (currentlyDrawing) {
                // Add in the current path
                // We are between action down and up
                canvas.drawPath(currentPath,currentPaint);
            }
            // Using the users drawing input
            for (int x = 0; x < allPaths.size(); x++) {
                Path thisPath = allPaths.get(x);
                Paint thisPaint = allPaints.get(x);
                canvas.drawPath(thisPath, thisPaint);
            }
        }
    }


    // Deal with the touch events
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float touchX = event.getX();
        float touchY = event.getY();
        int tolerance = 4;

        // For stylus pens
        if (event.getToolType(0)==MotionEvent.TOOL_TYPE_ERASER) {
            setErase(true);
        } else if (event.getToolType(0)==MotionEvent.TOOL_TYPE_STYLUS) {
            setErase(false);
        }

        if (enabled) {
            if (action == MotionEvent.ACTION_DOWN) {
                currentlyDrawing = true;
                updateStartXY(touchX, touchY);
                checkSwipeClearStart(touchX, touchY);
                if (currentPath == null) {
                    currentPath = new Path();
                }
                currentPath.moveTo(touchX, touchY);

            } else if (action == MotionEvent.ACTION_UP) {
                // Check for clear swipe
                checkClearSwipe(touchX, touchY);
                currentPath.quadTo(startX,startY,(startX+touchX)/2,(startY+touchY)/2);
                updateStartXY(touchX, touchY);
                currentlyDrawing = false;
                addToArrays();

            } else if (MotionEvent.ACTION_MOVE == event.getAction()) {
                float dx = Math.abs(touchX - startX);
                float dy = Math.abs(touchY - startY);

                if (dx >= tolerance || dy >= tolerance) {
                    // Accept move
                    currentPath.quadTo(startX, startY, (startX + touchX) / 2, (startY + touchY) / 2);
                    updateStartXY(touchX,touchY);
                }

            } else if (MotionEvent.ACTION_CANCEL == event.getAction()) {
                currentPath.moveTo(touchX, touchY);
                updateStartXY(touchX,touchY);
                startSwipe = true;
                // Check for clear swipe
                checkClearSwipe(touchX, touchY);
            }

            // Indicate view should be redrawn
            postInvalidate();
            performClick();
            return true;
        }
        return true;
    }
    private void addToArrays() {
        allPaths.add(currentPath);
        allPaints.add(currentPaint);
        newPaintAndPath();
    }

    private void newPaintAndPath() {
        int color = currentPaint.getColor();
        int size = (int)currentPaint.getStrokeWidth();
        initialiseCurrentObjs();
        setCurrentPaint(size,color);
        setErase(isErase);
    }
    private void updateStartXY(float touchX, float touchY) {
        startX = touchX;
        startY = touchY;
    }
    private void checkSwipeClearStart(float touchX, float touchY) {
        if (startSwipe && delayClear) {
            // This is only triggered for the swipe gesture setting
            startSwipe = false;
            startTime = System.currentTimeMillis();
            startXSwipe = touchX;
            startYSwipe = touchY;
        }
    }
    private void checkClearSwipe(float touchX, float touchY) {
        // If we want to clear after a delay (swipe settings)
        if (delayClear && !startSwipe) {
            Runnable r = () -> {
                // We are using the notes to simulate swipe
                sendSwipeInfo(touchX,touchY);
                startSwipe = true;
                initialiseArrays();
                currentPath.reset();
                allPaths.clear();
                postInvalidate();
            };
            postDelayed(r,300);
        }
    }
    private void sendSwipeInfo(float touchX, float touchY) {
        // Get dX and dY
        int dX = (int)Math.abs(touchX - startXSwipe);
        int dY = (int)Math.abs(touchY - startYSwipe);
        // Work out the time in secs between down and up
        int timetaken = (int) ((System.currentTimeMillis()-startTime));
        if (swipeDrawingInterface!=null) {
            swipeDrawingInterface.getSwipeValues(dX,dY,timetaken);
        }
    }



    // Undo and redo
    public ArrayList<Path> getUndoPaths() {
        return undoPaths;
    }
    public ArrayList<Path> getAllPaths() {
        return allPaths;
    }
    public void undo() {
        int pathpos = allPaths.size()-1;
        int paintpos = allPaints.size()-1;
        if (pathpos>=0) {
            undoPaths.add(allPaths.get(pathpos));
            allPaths.remove(pathpos);
            updateFrom = updateFrom-1;
        }
        if (paintpos>=0) {
            undoPaints.add(allPaints.get(paintpos));
            allPaints.remove(paintpos);
        }
        postInvalidate();
    }
    public void redo() {
        int pathpos = undoPaths.size()-1;
        int paintpos = undoPaints.size()-1;
        if (pathpos>0) {
            allPaths.add(undoPaths.get(pathpos));
            undoPaths.remove(pathpos);
            updateFrom = updateFrom+1;
        }
        if (paintpos>0) {
            allPaints.add(undoPaints.get(paintpos));
            undoPaints.remove(paintpos);
        }
        postInvalidate();
    }

    public void delete() {
        // No undos for this
        //int size = allPaths.size()-1;
        allPaths.clear();
        allPaints.clear();
        undoPaths.clear();
        undoPaints.clear();
        existingHighlighterFile = null;
        postInvalidate();
    }


    // Existing highlighter notes to be loaded
    public void loadExistingHighlighter(MainActivityInterface mainActivityInterface, int w, int h) {
        existingHighlighterFile = mainActivityInterface.getProcessSong().
                getHighlighterFile(w,h);

        if (existingHighlighterFile!=null) {
            bitmapPaint = new Paint(Paint.DITHER_FLAG);
            bitmapPaint.setAntiAlias(true);
            loadExisting = true;
        }
        postInvalidate();
    }

}
