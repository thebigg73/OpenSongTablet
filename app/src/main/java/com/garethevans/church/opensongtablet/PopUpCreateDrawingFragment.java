package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.OutputStream;
import java.util.Objects;

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
    public void onAttach(@NonNull Context context) {
        mListener = (MyInterface) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

    private DrawNotes drawView;
    private ImageView screenShot;
    private RelativeLayout drawingArea, mySizes;
    private HorizontalScrollView myTools, myColors;
    private FloatingActionButton currentTool;
    private FloatingActionButton pencil_FAB;
    private FloatingActionButton highlighter_FAB;
    private FloatingActionButton eraser_FAB;
    private FloatingActionButton color_black;
    private FloatingActionButton color_white;
    private FloatingActionButton color_yellow;
    private FloatingActionButton color_red;
    private FloatingActionButton color_green;
    private FloatingActionButton color_blue;
    private SeekBar size_SeekBar;
    private TextView size_TextView;
    private final float off_alpha = 0.4f;
    private final int isvis = View.VISIBLE;
    private final int isgone = View.GONE;

    private StorageAccess storageAccess;
    private Preferences preferences;
    private ProcessSong processSong;
    private Uri uri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(false);
        }

        View V = inflater.inflate(R.layout.popup_createdrawing, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText("");
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe, getActivity());
            closeMe.setEnabled(false);
            dismiss();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setEnabled(true);
        saveMe.setOnClickListener(view -> doSave());

        storageAccess = new StorageAccess();
        preferences = new Preferences();
        processSong = new ProcessSong();

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
        FloatingActionButton delete_FAB = V.findViewById(R.id.delete_FAB);
        color_black = V.findViewById(R.id.color_black);
        color_white = V.findViewById(R.id.color_white);
        color_yellow = V.findViewById(R.id.color_yellow);
        color_red = V.findViewById(R.id.color_red);
        color_green = V.findViewById(R.id.color_green);
        color_blue = V.findViewById(R.id.color_blue);
        size_SeekBar = V.findViewById(R.id.size_SeekBar);
        size_TextView = V.findViewById(R.id.size_TextView);
        FloatingActionButton undo_FAB = V.findViewById(R.id.undo_FAB);
        FloatingActionButton redo_FAB = V.findViewById(R.id.redo_FAB);

        // Get the screenshot size and ajust the drawing to match
        getSizes();
        screenShot.setImageBitmap(FullscreenActivity.bmScreen);

        // Set the current tool image
        setCurrentTool();

        // Set the current color
        setCurrentColor();

        // Set the current size
        setCurrentSize();

        // Listen for clicks
        currentTool.setOnClickListener(view -> {
            if (myTools.getVisibility() == View.GONE) {
                showorhideToolOptions(isvis);
            } else {
                showorhideToolOptions(isgone);
            }
        });

        pencil_FAB.setOnClickListener(view -> updateTool("pen"));
        highlighter_FAB.setOnClickListener(view -> updateTool("highlighter"));
        eraser_FAB.setOnClickListener(view -> updateTool("eraser"));
        color_black.setOnClickListener(view -> updateColor(StaticVariables.black));
        color_white.setOnClickListener(view -> updateColor(StaticVariables.white));
        color_yellow.setOnClickListener(view -> updateColor(StaticVariables.yellow));
        color_red.setOnClickListener(view -> updateColor(StaticVariables.red));
        color_green.setOnClickListener(view -> updateColor(StaticVariables.green));
        color_blue.setOnClickListener(view -> updateColor(StaticVariables.blue));
        size_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                getCurrentSize();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        delete_FAB.setOnClickListener(view -> drawView.startNew(getActivity(),uri));
        // Hide the undo/redo buttons for now
        //undo_FAB.setVisibility(View.GONE);
        undo_FAB.setOnClickListener(view -> drawView.undo());
        //redo_FAB.setVisibility(View.GONE);
        redo_FAB.setOnClickListener(view -> drawView.redo());
        showorhideToolOptions(isgone);

        // Set the appropriate highlighter file
        setHighlighterFile();

        drawView.setDrawingSize();

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void setHighlighterFile() {
        // If this file already exists, load it up!
        String hname = processSong.getHighlighterName(Objects.requireNonNull(getActivity()));
        uri = storageAccess.getUriForItem(getActivity(), preferences, "Highlighter", "", hname);
        if (storageAccess.uriExists(getActivity(),uri)) {
            drawView.loadImage(getActivity(),uri);
        }
    }

    private void updateTool(String what) {
        preferences.setMyPreferenceString(getActivity(),"drawingTool",what);
        drawView.setErase(what.equals("eraser"));
        setCurrentTool();
        setCurrentColor();
        setCurrentSize();
        if (what.equals("highlighter")) {

        }
        showorhideToolOptions(isvis);
    }

    private void updateColor(int what) {
        switch (preferences.getMyPreferenceString(getActivity(),"drawingTool","pen")) {
            case "pen":
                preferences.setMyPreferenceInt(getActivity(),"drawingPenColor",what);
                break;
            case "highlighter":
                int val = StaticVariables.highlighteryellow;
                switch (what) {
                    case StaticVariables.black:
                        val = StaticVariables.highlighterblack;
                        break;
                    case StaticVariables.white:
                        val = StaticVariables.highlighterwhite;
                        break;
                    case StaticVariables.yellow:
                        val = StaticVariables.highlighteryellow;
                        break;
                    case StaticVariables.red:
                        val = StaticVariables.highlighterred;
                        break;
                    case StaticVariables.green:
                        val = StaticVariables.highlightergreen;
                        break;
                    case StaticVariables.blue:
                        val = StaticVariables.highlighterblue;
                        break;
                }
                preferences.setMyPreferenceInt(getActivity(),"drawingHighlighterColor",val);
                break;
        }
        setCurrentTool();
        setCurrentColor();
        setCurrentSize();
        showorhideToolOptions(isvis);
    }

    private void showorhideToolOptions(int vis) {
        if (vis == isgone) {
            myTools.setVisibility(isgone);
            myColors.setVisibility(isgone);
            mySizes.setVisibility(isgone);
        } else {
            if ("eraser".equals(preferences.getMyPreferenceString(getActivity(), "drawingTool", "pen"))) {
                myTools.setVisibility(vis);
                myColors.setVisibility(isgone);
                mySizes.setVisibility(vis);
            } else {
                myTools.setVisibility((vis));
                myColors.setVisibility(vis);
                mySizes.setVisibility(vis);
            }
        }
    }

    private void setCurrentTool() {
        switch (preferences.getMyPreferenceString(getActivity(),"drawingTool","pen")) {
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

    private int getPenColor() {
        return preferences.getMyPreferenceInt(getActivity(),"drawingPenColor",StaticVariables.black);
    }
    private int getHighlighterColor() {
        return preferences.getMyPreferenceInt(getActivity(),"drawingHighlighterColor",StaticVariables.highlighteryellow);
    }
    private String getDrawingTool() {
        return preferences.getMyPreferenceString(getActivity(),"drawingTool","pen");
    }

    private void setCurrentColor() {
        String color;
        int pencolor = getPenColor();
        int highlightercolor = getHighlighterColor();
        String tool = getDrawingTool();

        if (tool.equals("pen")) {
            switch (pencolor) {
                case StaticVariables.white:
                default:
                    color = "white";
                    break;
                case StaticVariables.black:
                    color = "black";
                    break;
                case StaticVariables.blue:
                    color = "blue";
                    break;
                case StaticVariables.red:
                    color = "red";
                    break;
                case StaticVariables.green:
                    color = "green";
                    break;
                case StaticVariables.yellow:
                    color = "yellow";
                    break;
            }
        } else if (tool.equals("highlighter")) {
            switch (highlightercolor) {
                case StaticVariables.highlighterwhite:
                    color = "white";
                    break;
                case StaticVariables.highlighterblack:
                    color = "black";
                    break;
                case StaticVariables.highlighterblue:
                    color = "blue";
                    break;
                case StaticVariables.highlighterred:
                    color = "red";
                    break;
                case StaticVariables.highlightergreen:
                    color = "green";
                    break;
                case StaticVariables.highlighteryellow:
                default:
                    color = "yellow";
                    break;
            }
        } else {
            color = "null";
        }

        int i = R.drawable.ic_check_white_36dp;
        int j = R.drawable.ic_check_black_36dp;
        int o = android.R.color.transparent;

        switch (color) {
            case "black":
                setColorAlpha(1.0f, off_alpha, off_alpha, off_alpha, off_alpha, off_alpha);
                setColorTick(i, o, o, o, o, o);
                break;
            case "white":
                setColorAlpha(off_alpha, 1.0f, off_alpha, off_alpha, off_alpha, off_alpha);
                setColorTick(o, j, o, o, o, o);
                break;
            case "yellow":
                setColorAlpha(off_alpha, off_alpha, 1.0f, off_alpha, off_alpha, off_alpha);
                setColorTick(o, o, j, o, o, o);
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

    private void setCurrentSize() {
        int size = 0;
        switch (preferences.getMyPreferenceString(getActivity(),"drawingTool","highlighter")) {
            case "pen":
                size = (preferences.getMyPreferenceInt(getActivity(),"drawingPenSize",20) / 5) - 1;
                break;
            case "highlighter":
                size = (preferences.getMyPreferenceInt(getActivity(),"drawingHighlighterSize",20) / 5) - 1;
                break;
            case "eraser":
                size = (preferences.getMyPreferenceInt(getActivity(),"drawingEraserSize",20) / 5) - 1;
        }
        size_SeekBar.setProgress(size);
        String t = ((size+1)*5) + " px";
        size_TextView.setText(t);
    }

    private void getCurrentSize() {
        int size = (size_SeekBar.getProgress() + 1) * 5;
        String t = size + " px";
        switch (preferences.getMyPreferenceString(getActivity(),"drawingTool","pen")) {
            case "pen":
                preferences.setMyPreferenceInt(getActivity(),"drawingPenSize",size);
                break;
            case "highlighter":
                preferences.setMyPreferenceInt(getActivity(),"drawingHighlighterSize",size);
                break;
            case "eraser":
                preferences.setMyPreferenceInt(getActivity(),"drawingEraserSize",size);
                break;
        }
        size_TextView.setText(t);
    }

    private void setToolAlpha(float float_pen, float float_highlighter, float float_eraser) {
        pencil_FAB.setAlpha(float_pen);
        highlighter_FAB.setAlpha(float_highlighter);
        eraser_FAB.setAlpha(float_eraser);
    }

    private void setColorTick(int b_black, int b_white, int b_yellow,
                              int b_red, int b_green, int b_blue) {
        if (preferences.getMyPreferenceString(getActivity(),"drawingTool","pen").equals("eraser")) {
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

    private void setColorAlpha(float c_black, float c_white, float c_yellow,
                               float c_red, float c_green, float c_blue) {

        color_black.setAlpha(c_black);
        color_white.setAlpha(c_white);
        color_yellow.setAlpha(c_yellow);
        color_red.setAlpha(c_red);
        color_green.setAlpha(c_green);
        color_blue.setAlpha(c_blue);
    }

    private void getSizes() {
        int w = FullscreenActivity.bmScreen.getWidth();
        int h = FullscreenActivity.bmScreen.getHeight();

        int wa = drawingArea.getMeasuredWidth();
        int ha = drawingArea.getMeasuredHeight();

        float max_w_scale = (float)wa/(float)w;
        float max_h_scale = (float)ha/(float)h;

        float scale = Math.min(max_h_scale, max_w_scale);

        int screenshot_width = (int) (w * scale);
        int screenshot_height = (int) (h * scale);

        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(screenshot_width, screenshot_height);
        RelativeLayout.LayoutParams rlp2 = new RelativeLayout.LayoutParams(screenshot_width, screenshot_height);
        screenShot.setLayoutParams(rlp);
        drawView.setLayoutParams(rlp2);
    }

    private void doSave() {
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
                String hname = processSong.getHighlighterName(Objects.requireNonNull(getActivity()));
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
                storageAccess.writeImage(outputStream, bmp);
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
