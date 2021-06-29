package com.garethevans.church.opensongtablet.songsandsetsmenu;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.MenuSongsDialogBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SongMenuBottomSheet extends BottomSheetDialogFragment {

    private MenuSongsDialogBinding myView;
    private MainActivityInterface mainActivityInterface;
    private final FloatingActionButton fab;

    SongMenuBottomSheet(FloatingActionButton fab) {
        this.fab = fab;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        dismiss();
        mainActivityInterface.songMenuActionButtonShow(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = MenuSongsDialogBinding.inflate(inflater,container,false);
        if (getDialog()!=null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.scrim)));
            getDialog().setCanceledOnTouchOutside(true);
        }

        // Set up the views
        setListeners();

        return myView.getRoot();
    }

    private void setListeners() {
        // Set up the dialog title
        ((TextView)myView.closeView.findViewById(R.id.title)).setText(getString(R.string.song));

        // Set up the song title
        String songTitle = mainActivityInterface.getSong().getTitle();
        if (songTitle==null || songTitle.isEmpty() || songTitle.equals("Welcome to OpenSongApp")) {
            myView.songActions.setVisibility(View.GONE);
        } else {
            myView.songActions.setVisibility(View.VISIBLE);
            ((TextView)myView.songActions.findViewById(R.id.mainText)).setText(songTitle);
        }

        // Listener for buttons
        myView.closeView.findViewById(R.id.close).setOnClickListener(v -> dismiss());
        myView.songActions.setOnClickListener(v -> navigateTo("opensongapp://settings/actions"));
        myView.newSongs.setOnClickListener(v -> navigateTo("opensongapp://settings/import"));
    }

    private void navigateTo(String deepLink) {
        mainActivityInterface.closeDrawer(true);
        mainActivityInterface.navigateToFragment(deepLink,0);
        dismiss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (fab!=null) {
            try {
                fab.show();
            } catch (Exception e) {
                Log.d("SongMenuDialog","Can't show menu button");
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}