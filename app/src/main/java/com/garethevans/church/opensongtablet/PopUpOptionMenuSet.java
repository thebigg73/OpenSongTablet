/*
package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PopUpOptionMenuSet extends DialogFragment {

    static PopUpOptionMenuSet newInstance() {
        PopUpOptionMenuSet frag;
        frag = new PopUpOptionMenuSet();
        return frag;
    }

    public interface MyInterface {
        void openFragment();
        void loadSong();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View V = inflater.inflate(R.layout.popup_option_set, container, false);

        SetActions.updateOptionListSets();
        FullscreenActivity.setSize = FullscreenActivity.mSetList.length;

        // List the views
        Button setLoadButton = (Button) V.findViewById(R.id.setLoadButton);
        Button setSaveButton = (Button) V.findViewById(R.id.setSaveButton);
        Button setNewButton = (Button) V.findViewById(R.id.setNewButton);
        Button setDeleteButton = (Button) V.findViewById(R.id.setDeleteButton);
        Button setExportButton = (Button) V.findViewById(R.id.setExportButton);
        Button setCustomButton = (Button) V.findViewById(R.id.setCustomButton);
        Button setVariationButton = (Button) V.findViewById(R.id.setVariationButton);
        Button setEditButton = (Button) V.findViewById(R.id.setEditButton);
        LinearLayout setLinearLayout = (LinearLayout) V.findViewById(R.id.setLinearLayout);

        // Set the text as to uppercase as per locale
        setLoadButton.setText(getActivity().getString(R.string.options_set_load).toUpperCase(FullscreenActivity.locale));
        setSaveButton.setText(getActivity().getString(R.string.options_set_save).toUpperCase(FullscreenActivity.locale));
        setNewButton.setText(getActivity().getString(R.string.options_set_clear).toUpperCase(FullscreenActivity.locale));
        setDeleteButton.setText(getActivity().getString(R.string.options_set_delete).toUpperCase(FullscreenActivity.locale));
        setExportButton.setText(getActivity().getString(R.string.options_set_export).toUpperCase(FullscreenActivity.locale));
        setCustomButton.setText(getActivity().getString(R.string.add_custom_slide).toUpperCase(FullscreenActivity.locale));
        setVariationButton.setText(getActivity().getString(R.string.customise_set_item).toUpperCase(FullscreenActivity.locale));
        setEditButton.setText(getActivity().getString(R.string.options_set_edit).toUpperCase(FullscreenActivity.locale));

        // Set the button listeners
        setLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "loadset";
                mListener.openFragment();
                dismiss();
            }
        });

        setSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "saveset";
                mListener.openFragment();
                dismiss();
            }
        });

        setNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "clearset";
                mListener.openFragment();
                dismiss();
            }
        });

        setDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "deleteset";
                mListener.openFragment();
                dismiss();
            }
        });

        setExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "exportset";
                mListener.openFragment();
                dismiss();
            }
        });

        setCustomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "customcreate";
                mListener.openFragment();
                dismiss();
            }
        });

        setVariationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "setitemvariation";
                mListener.openFragment();
                dismiss();
            }
        });

        setEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "editset";
                mListener.openFragment();
                dismiss();
            }
        });

        // Add the set list to the menu
        if (FullscreenActivity.mSetList!=null) {
            for (int x = 0; x<FullscreenActivity.mSetList.length; x++) {
                TextView tv = new TextView(getActivity());
                tv.setText(FullscreenActivity.mSetList[x]);
                tv.setTextColor(0xffffffff);
                tv.setTextSize(16.0f);
                tv.setPadding(16,16,16,16);
                LinearLayout.LayoutParams tvp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                tvp.setMargins(40,40,40,40);
                tv.setLayoutParams(tvp);
                final int val = x;
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FullscreenActivity.setView = true;
                        FullscreenActivity.pdfPageCurrent = 0;
                        FullscreenActivity.linkclicked = FullscreenActivity.mSetList[val];
                        FullscreenActivity.indexSongInSet = val;
                        SetActions.songIndexClickInSet();
                        SetActions.getSongFileAndFolder(getActivity());
                        mListener.loadSong();
                        dismiss();
                    }
                });
                setLinearLayout.addView(tv);
            }
        }

        return V;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
*/
