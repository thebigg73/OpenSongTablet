package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

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
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
        if (getDialog().getWindow()!=null) {
            getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.popup_dialogtitle);
            TextView title = (TextView) getDialog().getWindow().findViewById(R.id.dialogtitle);
            title.setText(getActivity().getResources().getString(R.string.pdf_selectpage));
            getDialog().getWindow().findViewById(R.id.saveMe).setVisibility(View.GONE);
            FloatingActionButton closeMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.closeMe);
            closeMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
        } else {
            getDialog().setTitle(getActivity().getResources().getString(R.string.pdf_selectpage));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    TextView pages_notavailable;
    LinearLayout pages_available;
    SeekBar pageseekbar;
    FloatingActionButton previouspage;
    FloatingActionButton nextpage;
    TextView pagetextView;
    int temppos = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

        View V = inflater.inflate(R.layout.popup_pages, container, false);

        // Initialise the views
        pages_notavailable = (TextView) V.findViewById(R.id.pages_notavailable);
        pages_available = (LinearLayout) V.findViewById(R.id.pages_available);
        pageseekbar = (SeekBar) V.findViewById(R.id.pageseekbar);
        previouspage = (FloatingActionButton) V.findViewById(R.id.previouspage);
        nextpage = (FloatingActionButton) V.findViewById(R.id.nextpage);
        pagetextView = (TextView) V.findViewById(R.id.pagetextView);

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


        } else if (FullscreenActivity.isPDF) {
            pages_notavailable.setVisibility(View.VISIBLE);
            pages_available.setVisibility(View.GONE);
        }



        return V;
    }

    public void moveToSelectedPage(int newpos) {
        String dir = "R2L";
        if (newpos < FullscreenActivity.pdfPageCurrent) {
            dir = "L2R";
        }
        FullscreenActivity.pdfPageCurrent = newpos;
        if (mListener!=null) {
            mListener.changePDFPage(FullscreenActivity.pdfPageCurrent, dir);
        }
    }

    public String pageInfo(int current) {
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
