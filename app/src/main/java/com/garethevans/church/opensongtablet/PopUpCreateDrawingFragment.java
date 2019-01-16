package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.OutputStream;

public class PopUpCreateDrawingFragment extends DialogFragment {

    static PopUpCreateDrawingFragment newInstance() {
        PopUpCreateDrawingFragment frag;
        frag = new PopUpCreateDrawingFragment();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
    }

    private MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

    private DrawNotes drawView;
    private ImageView screenShot;
    RelativeLayout drawingArea;
    HorizontalScrollView myTools;
    HorizontalScrollView myColors;
    RelativeLayout mySizes;
    FloatingActionButton currentTool;
    FloatingActionButton pencil_FAB;
    FloatingActionButton highlighter_FAB;
    FloatingActionButton eraser_FAB;
    FloatingActionButton undo_FAB;
    FloatingActionButton redo_FAB;
    FloatingActionButton delete_FAB;
    FloatingActionButton color_black;
    FloatingActionButton color_white;
    FloatingActionButton color_yellow;
    FloatingActionButton color_red;
    FloatingActionButton color_green;
    FloatingActionButton color_blue;
    SeekBar size_SeekBar;
    TextView size_TextView;
    int screenshot_width;
    int screenshot_height;
    float scale;
    float off_alpha = 0.4f;
    int isvis = View.VISIBLE;
    int isgone = View.GONE;

    StorageAccess storageAccess;
    Preferences preferences;
    Uri uri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);
        View V = inflater.inflate(R.layout.popup_createdrawing, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText("");
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setEnabled(true);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSave();
            }
        });

        storageAccess = new StorageAccess();
        preferences = new Preferences();

        // Initialise the views
        drawView = V.findViewById(R.id.drawView);
        screenShot = V.findViewById(R.id.screenshot);
        drawingArea = V.findViewById(R.id.drawingArea);
        drawingArea.measure(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        myTools = V.findViewById(R.id.myTools);
        myColors = V.findViewById(R.id.myColors);
        mySizes = V.findViewById(R.id.mySizes);
        currentTool = V.findViewById(R.id.currentTool);
        pencil_FAB = V.findViewById(R.id.pencil_FAB);
        highlighter_FAB = V.findViewById(R.id.highlighter_FAB);
        eraser_FAB = V.findViewById(R.id.eraser_FAB);
        delete_FAB = V.findViewById(R.id.delete_FAB);
        color_black = V.findViewById(R.id.color_black);
        color_white = V.findViewById(R.id.color_white);
        color_yellow = V.findViewById(R.id.color_yellow);
        color_red = V.findViewById(R.id.color_red);
        color_green = V.findViewById(R.id.color_green);
        color_blue = V.findViewById(R.id.color_blue);
        size_SeekBar = V.findViewById(R.id.size_SeekBar);
        size_TextView = V.findViewById(R.id.size_TextView);
        undo_FAB = V.findViewById(R.id.undo_FAB);
        redo_FAB = V.findViewById(R.id.redo_FAB);

        // Get the screenshot size and ajust the drawing to match
        getSizes();
        screenShot.setImageBitmap(FullscreenActivity.bmScreen);
        //screenShot.setScaleType(ImageView.ScaleType.FIT_XY);
        //screenShot.setPadding(2,2,2,2);

        // Set the current tool image
        setCurrentTool();

        // Set the current color
        setCurrentColor();

        // Set the current size
        setCurrentSize();

        // Listen for clicks
        currentTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myTools.getVisibility() == View.GONE) {
                    showorhideToolOptions(isvis);
                } else {
                    showorhideToolOptions(isgone);
                }
            }
        });

        pencil_FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTool("pen");
            }
        });
        highlighter_FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTool("highlighter");
            }
        });
        eraser_FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTool("eraser");
            }
        });
        color_black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateColor("black");
            }
        });
        color_white.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateColor("white");
            }
        });
        color_yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateColor("yellow");
            }
        });
        color_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateColor("red");
            }
        });
        color_green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateColor("green");
            }
        });
        color_blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateColor("blue");
            }
        });
        size_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                getCurrentSize();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
            }
        });
        delete_FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawView.startNew(getActivity(),uri);
            }
        });
        // Hide the undo/redo buttons for now
        //undo_FAB.setVisibility(View.GONE);
        undo_FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawView.undo();
            }
        });
        //redo_FAB.setVisibility(View.GONE);
        redo_FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawView.redo();
            }
        });
        showorhideToolOptions(isgone);

        // Set the appropriate highlighter file
        setHighlighterFile();

        drawView.setDrawingSize(screenshot_width,screenshot_height);

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    public void setHighlighterFile() {
        // If this file already exists, load it up!
        String hname = ProcessSong.getHighlighterName(getActivity());
        uri = storageAccess.getUriForItem(getActivity(), preferences, "Highlighter", "", hname);
        if (storageAccess.uriExists(getActivity(),uri)) {
            drawView.loadImage(getActivity(),uri);
        }
    }

    public void updateTool(String what) {
        FullscreenActivity.drawingTool = what;
        if (what.equals("eraser")) {
            drawView.setErase(true);
        } else {
            drawView.setErase(false);
        }
        Preferences.savePreferences();
        setCurrentTool();
        setCurrentColor();
        setCurrentSize();
        showorhideToolOptions(isvis);
    }

    public void updateColor(String what) {
        switch (FullscreenActivity.drawingTool) {
            case "pen":
                FullscreenActivity.drawingPenColor = what;
                break;
            case "highlighter":
                FullscreenActivity.drawingHighlightColor = what;
                break;
        }
        Preferences.savePreferences();
        setCurrentTool();
        setCurrentColor();
        setCurrentSize();
        showorhideToolOptions(isvis);
    }

    public void showorhideToolOptions(int vis) {
        if (vis == isgone) {
            myTools.setVisibility(isgone);
            myColors.setVisibility(isgone);
            mySizes.setVisibility(isgone);
        } else {
            switch (FullscreenActivity.drawingTool) {
                case "eraser":
                    myTools.setVisibility(vis);
                    myColors.setVisibility(isgone);
                    mySizes.setVisibility(vis);
                    break;
                default:
                    myTools.setVisibility((vis));
                    myColors.setVisibility(vis);
                    mySizes.setVisibility(vis);
                    break;
            }
        }
    }

    public void setCurrentTool() {
        switch (FullscreenActivity.drawingTool) {
            case "pen":
                currentTool.setImageResource(R.drawable.ic_pencil_white_36dp);
                setToolAlpha(1.0f,off_alpha,off_alpha);
                break;
            case "highlighter":
                currentTool.setImageResource(R.drawable.ic_highlighter_white_36dp);
                setToolAlpha(off_alpha,1.0f,off_alpha);break;
            case "eraser":
                currentTool.setImageResource(R.drawable.ic_eraser_white_36dp);
                setToolAlpha(off_alpha,off_alpha,1.0f);
                break;
        }
    }

    public void setCurrentColor() {
        String color;

        switch (FullscreenActivity.drawingTool) {
            case "pen":
            default:
                color = FullscreenActivity.drawingPenColor;
                break;

            case "highlighter":
                color = FullscreenActivity.drawingHighlightColor;
                break;

            case "eraser":
                color = "null";
                break;
        }

        int i = R.drawable.ic_check_white_36dp;
        int o = android.R.color.transparent;

        switch (color) {
            case "black":
                setColorAlpha(1.0f, off_alpha, off_alpha, off_alpha, off_alpha, off_alpha);
                setColorTick(i, o, o, o, o, o);
                break;
            case "white":
                setColorAlpha(off_alpha, 1.0f, off_alpha, off_alpha, off_alpha, off_alpha);
                setColorTick(o, i, o, o, o, o);
                break;
            case "yellow":
                setColorAlpha(off_alpha, off_alpha, 1.0f, off_alpha, off_alpha, off_alpha);
                setColorTick(o, o, i, o, o, o);
                break;
            case "red":
                setColorAlpha(off_alpha, off_alpha, off_alpha, 1.0f, off_alpha, off_alpha);
                setColorTick(o, o, o, i, o, o);
                break;
            case "green":
                setColorAlpha(off_alpha, off_alpha, off_alpha, off_alpha, 1.0f, off_alpha);
                setColorTick(o, o, o, o, i, o);
                break;
            case "blue":
                setColorAlpha(off_alpha, off_alpha, off_alpha, off_alpha, off_alpha, 1.0f);
                setColorTick(o, o, o, o, o, i);
                break;
            case "null":
                setColorAlpha(off_alpha, off_alpha, off_alpha, off_alpha, off_alpha, off_alpha);
                setColorTick(o, o, o, o, o, o);
                break;
        }
    }

    public void setCurrentSize() {
        int size = 0;
        switch (FullscreenActivity.drawingTool) {
            case "pen":
                size = (FullscreenActivity.drawingPenSize / 5) - 1;
                break;
            case "highlighter":
                size = (FullscreenActivity.drawingHighlightSize / 5) - 1;
                break;
            case "eraser":
                size = (FullscreenActivity.drawingEraserSize / 5) - 1;
        }
        size_SeekBar.setProgress(size);
        String t = ((size+1)*5) + " px";
        size_TextView.setText(t);
    }

    public void getCurrentSize() {
        int size = (size_SeekBar.getProgress() + 1) * 5;
        String t = size + " px";
        switch (FullscreenActivity.drawingTool) {
            case "pen":
                FullscreenActivity.drawingPenSize = size;
                break;
            case "highlighter":
                FullscreenActivity.drawingHighlightSize = size;
                break;
            case "eraser":
                FullscreenActivity.drawingEraserSize = size;
                break;
        }
        size_TextView.setText(t);
    }

    public void setToolAlpha(float float_pen, float float_highlighter, float float_eraser) {
        pencil_FAB.setAlpha(float_pen);
        highlighter_FAB.setAlpha(float_highlighter);
        eraser_FAB.setAlpha(float_eraser);
    }

    public void setColorTick(int b_black, int b_white, int b_yellow,
                             int b_red, int b_green, int b_blue) {
        if (FullscreenActivity.drawingTool.equals("eraser")) {
            myColors.setVisibility(View.GONE);
        } else {
            myColors.setVisibility(View.VISIBLE);
        }
        color_black.setImageResource(b_black);
        color_white.setImageResource(b_white);
        color_yellow.setImageResource(b_yellow);
        color_red.setImageResource(b_red);
        color_green.setImageResource(b_green);
        color_blue.setImageResource(b_blue);
    }

    public void setColorAlpha(float c_black, float c_white, float c_yellow,
                              float c_red, float c_green, float c_blue) {
        color_black.setAlpha(c_black);
        color_white.setAlpha(c_white);
        color_yellow.setAlpha(c_yellow);
        color_red.setAlpha(c_red);
        color_green.setAlpha(c_green);
        color_blue.setAlpha(c_blue);
    }

    public void getSizes() {
        int w = FullscreenActivity.bmScreen.getWidth();
        int h = FullscreenActivity.bmScreen.getHeight();

        int wa = drawingArea.getMeasuredWidth();
        int ha = drawingArea.getMeasuredHeight();

        float max_w_scale = (float)wa/(float)w;
        float max_h_scale = (float)ha/(float)h;

        if (max_h_scale>max_w_scale) {
            scale = max_w_scale;
        } else {
            scale = max_h_scale;
        }

        screenshot_width = (int) (w*scale);
        screenshot_height = (int) (h*scale);

        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(screenshot_width,screenshot_height);
        RelativeLayout.LayoutParams rlp2 = new RelativeLayout.LayoutParams(screenshot_width,screenshot_height);
        screenShot.setLayoutParams(rlp);
        drawView.setLayoutParams(rlp2);
    }

    public void doSave() {
        AsyncTask<Object,Void,Void> do_save = new DoSave();
        try {
            do_save.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d","Error saving");
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class DoSave extends AsyncTask<Object, Void, Void> {

        Uri newUri;

        Bitmap bmp;

        @Override
        protected  void onPreExecute() {
            if (FullscreenActivity.saveHighlight) {
                FullscreenActivity.highlightOn = true;
                drawView.setDrawingCacheEnabled(true);
                String hname = ProcessSong.getHighlighterName(getActivity());
                newUri = storageAccess.getUriForItem(getActivity(), preferences,
                        "Highlighter", "", hname);

                // Check the uri exists for the outputstream to be valid
                storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences, newUri, null,
                        "Highlighter", "", hname);

                try {
                    bmp = drawView.getDrawingCache();
                } catch (Exception e) {
                    Log.d("d","Error extracting the drawing");
                } catch (OutOfMemoryError e) {
                    Log.d("d","Out of memory trying to get the drawing");
                }
            }
        }

        @Override
        protected Void doInBackground(Object... objects) {
            if (newUri!=null && bmp!=null) {
                Log.d("d","newUri="+newUri);
                Log.d("d","bmp="+bmp);
                OutputStream outputStream = storageAccess.getOutputStream(getActivity(),newUri);
                Log.d("d","outputStream="+outputStream);
                storageAccess.writeImage(outputStream, bmp, Bitmap.CompressFormat.PNG);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (mListener!=null) {
                mListener.refreshAll();
            }
            try {
                dismiss();
            } catch (Exception e) {
                Log.d("d","Error dismissing dialog");
            }
        }
    }
}
