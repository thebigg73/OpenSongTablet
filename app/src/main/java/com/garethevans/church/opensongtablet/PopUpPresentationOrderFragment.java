package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Objects;

public class PopUpPresentationOrderFragment extends DialogFragment {

    static PopUpPresentationOrderFragment newInstance() {
        PopUpPresentationOrderFragment frag;
        frag = new PopUpPresentationOrderFragment();
        return frag;
    }

    public interface MyInterface {
        void updatePresentationOrder();
    }

    private MyInterface mListener;
    private StorageAccess storageAccess;
    private Preferences preferences;
    private ProcessSong processSong;

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

    private TextView m_mPresentation;

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View V = inflater.inflate(R.layout.popup_presentation_order, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.edit_song_presentation));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe,getActivity());
                saveMe.setEnabled(false);
                doSave();
            }
        });

        storageAccess = new StorageAccess();
        preferences = new Preferences();
        processSong = new ProcessSong();

        // Define the views
        LinearLayout root_buttonshere = V.findViewById(R.id.songsectionstoadd);
        m_mPresentation = V.findViewById(R.id.popuppres_mPresentation);
        TextView popuppresorder_presorder_title = V.findViewById(R.id.popuppresorder_presorder_title);
        Button deletePresOrder = V.findViewById(R.id.deletePresOrder);

        // Set the values
        popuppresorder_presorder_title.setText(StaticVariables.mTitle);
        m_mPresentation.setText(StaticVariables.mPresentation);

        // Set the buttons up
        int numbuttons = FullscreenActivity.foundSongSections_heading.size();
        for (int r=0;r<numbuttons;r++) {
            Button but = new Button(getActivity());
            but.setId(r);
            but.setText(FullscreenActivity.foundSongSections_heading.get(r));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                but.setTextAppearance(getActivity(), android.R.style.TextAppearance_Small);
            } else {
                but.setTextAppearance(android.R.style.TextAppearance_Small);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                but.setBackground(getActivity().getDrawable(R.drawable.green_button));
            } else {
                but.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.green_button));
            }
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
                    String addthis = currpres.trim() + " " + FullscreenActivity.foundSongSections_heading.get(whichview);
                    m_mPresentation.setText(addthis);
                }
            });
            root_buttonshere.addView(but);
        }

        deletePresOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_mPresentation.setText("");
            }
        });
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void doSave() {
        StaticVariables.mPresentation = m_mPresentation.getText().toString().trim();
        PopUpEditSongFragment.prepareSongXML();

        if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
            NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(getActivity());
            NonOpenSongSQLite nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(getActivity(),storageAccess,preferences,nonOpenSongSQLiteHelper.getSongId());
            nonOpenSongSQLiteHelper.updateSong(getActivity(),storageAccess,preferences,nonOpenSongSQLite);
        } else {
            PopUpEditSongFragment.justSaveSongXML(getActivity(), preferences);
        }

        try {
            LoadXML.loadXML(getActivity(), preferences, storageAccess, processSong);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mListener!=null) {
            mListener.updatePresentationOrder();
        }
        dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}