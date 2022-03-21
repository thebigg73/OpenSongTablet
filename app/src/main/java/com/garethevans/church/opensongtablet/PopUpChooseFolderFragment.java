package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

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
    private SQLiteHelper sqLiteHelper;
    private ArrayList<String> songfolders;

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

    private ListView lv;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }

        final View V = inflater.inflate(R.layout.popup_choosefolder, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.songfolder));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe, PopUpChooseFolderFragment.this.getContext());
            closeMe.setEnabled(false);
            PopUpChooseFolderFragment.this.dismiss();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        lv = V.findViewById(R.id.songfolders_ListView);

        Preferences preferences = new Preferences();
        sqLiteHelper = new SQLiteHelper(getContext());

        // Update the song folders

        UpDateList upDateList = new UpDateList(getActivity());
        upDateList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    @SuppressLint("StaticFieldLeak")
    class UpDateList extends AsyncTask<Object, String, String> {

        final Context c;

        UpDateList(Context ctx) {
            c = ctx;
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                songfolders = sqLiteHelper.getFolders(c);
                if (!songfolders.contains(getString(R.string.mainfoldername))) {
                    songfolders.add(c.getResources().getString(R.string.mainfoldername));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Preferences preferences = new Preferences();

            // Sort the list
            if (StaticVariables.locale==null) {
                FixLocale.fixLocale(c,preferences);
            }
            Collator collator = Collator.getInstance(StaticVariables.locale);
            collator.setStrength(Collator.SECONDARY);
            Collections.sort(songfolders, collator);

            // Add the main folder to the top if it isn't already there
            int pos = songfolders.indexOf(c.getString(R.string.mainfoldername));
            if (pos<0) {
                // It isn't there, so add it
                songfolders.add(0,c.getString(R.string.mainfoldername));

            } else if (pos>0) {
                // It's there, but not at the top - remove it
                songfolders.remove(pos);
                // Add it to the top position
                songfolders.add(0,c.getString(R.string.mainfoldername));
            }

            try {
                ArrayAdapter<String> lva = new ArrayAdapter<>(c, R.layout.songlistitem, songfolders);
                lv.setAdapter(lva);
                lv.setOnItemClickListener((adapterView, view, i, l) -> {
                    StaticVariables.whichSongFolder = songfolders.get(i);
                    if (mListener != null) {
                        mListener.prepareSongMenu();
                    }
                    try {
                        PopUpChooseFolderFragment.this.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

}