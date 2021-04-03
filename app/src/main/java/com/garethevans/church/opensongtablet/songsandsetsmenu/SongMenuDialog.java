package com.garethevans.church.opensongtablet.songsandsetsmenu;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.MenuSongsDialogBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SongMenuDialog extends DialogFragment {

    private MenuSongsDialogBinding myView;
    private MainActivityInterface mainActivityInterface;
    private final FloatingActionButton fab;

    SongMenuDialog(FloatingActionButton fab) {
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
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().setGravity(Gravity.BOTTOM|Gravity.END);

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
        myView.songActions.setOnClickListener(v -> navigateTo("opensongapp://songs/actions"));
        myView.newSongs.setOnClickListener(v -> navigateTo("opensongapp://songs/import"));
    }

    private void navigateTo(String deepLink) {
        int popId;
        if (mainActivityInterface.getMode().equals("Presentation")) {
            popId = R.id.presentationFragment;
        } else {
            popId = R.id.performanceFragment;
        }
        mainActivityInterface.closeDrawer(true);
        NavOptions navOptions = new NavOptions.Builder().setPopUpTo(popId, false).build();
        NavHostFragment.findNavController(this).navigate(Uri.parse(deepLink),navOptions);
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
}