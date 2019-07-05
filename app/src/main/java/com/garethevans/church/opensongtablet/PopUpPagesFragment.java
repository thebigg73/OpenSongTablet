package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Objects;

public class PopUpPagesFragment extends DialogFragment {

    static PopUpPagesFragment newInstance() {
        PopUpPagesFragment frag;
        frag = new PopUpPagesFragment();
        return frag;
    }

    public interface MyInterface {
        void pageButtonAlpha(String s);
        void changePDFPage(int page, String direction);
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
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    private SeekBar pageseekbar;
    private TextView pagetextView;
    private int temppos = 1;

    Preferences preferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View V = inflater.inflate(R.layout.popup_pages, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.pdf_selectpage));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        preferences = new Preferences();

        // Initialise the views
        TextView pages_notavailable = V.findViewById(R.id.pages_notavailable);
        LinearLayout pages_available = V.findViewById(R.id.pages_available);
        pageseekbar = V.findViewById(R.id.pageseekbar);
        FloatingActionButton previouspage = V.findViewById(R.id.previouspage);
        FloatingActionButton nextpage = V.findViewById(R.id.nextpage);
        pagetextView = V.findViewById(R.id.pagetextView);

        // If this is an OpenSong song, then this isn't for you!
        if (FullscreenActivity.isPDF) {
            pages_notavailable.setVisibility(View.GONE);
            pages_available.setVisibility(View.VISIBLE);

            // Get the pdf page info
            pagetextView.setText(pageInfo(FullscreenActivity.pdfPageCurrent+1));
            pageseekbar.setMax(FullscreenActivity.pdfPageCount - 1);
            pageseekbar.setProgress(FullscreenActivity.pdfPageCurrent);

            // Set the listeners
            nextpage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int newpos = pageseekbar.getProgress()+1;
                    if (newpos<FullscreenActivity.pdfPageCount) {
                        FullscreenActivity.whichDirection = "R2L";
                        pageseekbar.setProgress(newpos);
                        moveToSelectedPage(newpos);
                    }
                }
            });
            previouspage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int newpos = pageseekbar.getProgress()-1;
                    if (newpos>-1) {
                        FullscreenActivity.whichDirection = "L2R";
                        pageseekbar.setProgress(newpos);
                        moveToSelectedPage(newpos);
                    }
                }
            });
            pageseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    temppos = i+1;
                    pagetextView.setText(pageInfo(temppos));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    moveToSelectedPage(seekBar.getProgress());
                }
            });


        } else {
            pages_notavailable.setVisibility(View.VISIBLE);
            pages_available.setVisibility(View.GONE);
        }
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void moveToSelectedPage(int newpos) {
        String dir = "R2L";
        if (newpos < FullscreenActivity.pdfPageCurrent) {
            dir = "L2R";
        }
        FullscreenActivity.pdfPageCurrent = newpos;
        if (mListener!=null) {
            mListener.changePDFPage(FullscreenActivity.pdfPageCurrent, dir);
        }
    }

    private String pageInfo(int current) {
        return current + " / " + FullscreenActivity.pdfPageCount;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if (mListener!=null) {
            mListener.pageButtonAlpha("");
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
