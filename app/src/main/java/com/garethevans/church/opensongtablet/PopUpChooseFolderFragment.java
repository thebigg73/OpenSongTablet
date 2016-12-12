package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PopUpChooseFolderFragment extends DialogFragment {

    static PopUpChooseFolderFragment newInstance() {
        PopUpChooseFolderFragment frag;
        frag = new PopUpChooseFolderFragment();
        return frag;
    }

    public interface MyInterface {
        void prepareSongMenu();
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

    ListView lv;

    @Override
    public void onStart() {
        super.onStart();

        // safety check
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View V = inflater.inflate(R.layout.popup_choosefolder, container, false);

        getDialog().setTitle(getActivity().getResources().getString(R.string.songfolder));
        getDialog().setCanceledOnTouchOutside(true);

        lv = (ListView) V.findViewById(R.id.songfolders_ListView);

        if (FullscreenActivity.mSongFolderNames!=null) {
            ArrayAdapter<String> lva = new ArrayAdapter<>(getActivity(),
                    R.layout.songlistitem, FullscreenActivity.mSongFolderNames);
            lv.setAdapter(lva);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    FullscreenActivity.whichSongFolder = FullscreenActivity.mSongFolderNames[i];
                    Preferences.savePreferences();
                    if (mListener!=null) {
                        mListener.prepareSongMenu();
                    }
                    dismiss();
                }
            });
        }

        return V;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}