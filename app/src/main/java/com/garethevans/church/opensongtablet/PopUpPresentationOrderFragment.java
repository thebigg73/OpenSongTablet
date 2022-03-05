package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    private TextView m_mPresentation;

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }

        View V = inflater.inflate(R.layout.popup_presentation_order, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.edit_song_presentation));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getContext());
            closeMe.setEnabled(false);
            dismiss();
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(saveMe,getContext());
            saveMe.setEnabled(false);
            doSave();
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
        String newText;
        if (StaticVariables.mPresentation.equals("")) {
            newText = StaticVariables.mTitle + "\n\n" + getString(R.string.edit_song_presentation);
        } else {
            newText = StaticVariables.mTitle + "\n\n" + StaticVariables.mPresentation + "\n\n\n" +
                    getString(R.string.edit_song_presentation);
        }
        popuppresorder_presorder_title.setText(newText);

        m_mPresentation.setText(StaticVariables.mPresentation.trim());

        // Set the buttons up
        int numbuttons = FullscreenActivity.foundSongSections_heading.size();
        for (int r=0;r<numbuttons;r++) {
            Button but = new Button(getContext());
            but.setId(r);
            but.setText(FullscreenActivity.foundSongSections_heading.get(r));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                but.setTextAppearance(getContext(), android.R.style.TextAppearance_Small);
            } else {
                but.setTextAppearance(android.R.style.TextAppearance_Small);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                but.setBackground(ContextCompat.getDrawable(requireContext(),R.drawable.green_button));
            } else {
                but.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.green_button));
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(12, 12, 12, 12);
            but.setLayoutParams(params);
            but.setMinHeight(0);
            but.setMinimumHeight(0);
            but.setPadding(5, 5, 5, 5);
            but.setOnClickListener(v -> {
                int whichview = v.getId();
                String currpres = m_mPresentation.getText().toString();
                String addthis = currpres.trim() + " " + FullscreenActivity.foundSongSections_heading.get(whichview);
                m_mPresentation.setText(addthis);
            });
            root_buttonshere.addView(but);
        }

        deletePresOrder.setOnClickListener(v -> m_mPresentation.setText(""));
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void doSave() {
        StaticVariables.mPresentation = m_mPresentation.getText().toString().trim();
        PopUpEditSongFragment.prepareSongXML();

        if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
            NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(getContext());
            NonOpenSongSQLite nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(getContext(),storageAccess,preferences,nonOpenSongSQLiteHelper.getSongId());
            nonOpenSongSQLiteHelper.updateSong(getContext(),storageAccess,preferences,nonOpenSongSQLite);
        } else {
            PopUpEditSongFragment.justSaveSongXML(getContext(), preferences);
        }

        try {
            LoadXML.loadXML(getContext(), preferences, storageAccess, processSong);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mListener!=null) {
            mListener.updatePresentationOrder();
        }
        dismiss();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

}