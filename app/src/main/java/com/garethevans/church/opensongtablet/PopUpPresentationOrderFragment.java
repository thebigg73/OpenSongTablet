/*
 * Copyright (c) 2015.
 * The code is provided free of charge.  You can use, modify, contribute and improve it as long as this source is referenced.
 * Commercial use should seek permission.
 */

package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class PopUpPresentationOrderFragment extends DialogFragment {

    static PopUpPresentationOrderFragment newInstance() {
        PopUpPresentationOrderFragment frag;
        frag = new PopUpPresentationOrderFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(FullscreenActivity.songfilename);
        View V = inflater.inflate(R.layout.popup_presentation_order, container, false);
        LinearLayout root_buttonshere = (LinearLayout) V.findViewById(R.id.songsectionstoadd);
        final TextView m_mPresentation = (TextView) V.findViewById(R.id.popuppres_mPresentation);
        // Try to add buttons for each section
        // How many buttons?
        int numbuttons = FullscreenActivity.foundSongSections_heading.size();
        for (int r=0;r<numbuttons;r++) {
            Button but = new Button(getActivity());
            but.setId(r);
            but.setText(FullscreenActivity.foundSongSections_heading.get(r));
            but.setTextAppearance(getActivity(), android.R.style.TextAppearance_Small);
            but.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.green_button));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(12, 12, 12, 12);
            but.setLayoutParams(params);
            but.setMinHeight(0);
            but.setMinimumHeight(0);
            but.setPadding(5, 5, 5, 5);
            but.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int whichview = v.getId();
                    String currpres = m_mPresentation.getText().toString();
                    String addthis = " " + FullscreenActivity.foundSongSections_heading.get(whichview);
                    m_mPresentation.setText(currpres.trim() + addthis);
                }
            });

            root_buttonshere.addView(but);

        }

        m_mPresentation.setText(FullscreenActivity.mPresentation);

        Button cancelPresentationOrder = (Button) V.findViewById(R.id.cancelPresentationOrder);
        cancelPresentationOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button savePresentationOrder = (Button) V.findViewById(R.id.savePresentationOrder);
        savePresentationOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.mPresentation = m_mPresentation.getText().toString().trim();
                CheckBox presorder = (CheckBox) getActivity().findViewById(R.id.presenter_order_text);
                presorder.setText(m_mPresentation.getText().toString().trim());
                presorder.setChecked(false);
                presorder.setChecked(true);
                PopUpEditSongFragment.prepareSongXML();
                try {
                    PopUpEditSongFragment.justSaveSongXML();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    LoadXML.loadXML();
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
                dismiss();
            }
        });
        Button deletePresOrder = (Button) V.findViewById(R.id.deletePresOrder);
        deletePresOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_mPresentation.setText("");
            }
        });

        return V;
    }
}