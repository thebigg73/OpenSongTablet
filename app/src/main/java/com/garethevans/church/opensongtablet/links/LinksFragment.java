package com.garethevans.church.opensongtablet.links;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MaterialTextView;
import com.garethevans.church.opensongtablet.databinding.SettingsLinksBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class LinksFragment extends Fragment {

    private SettingsLinksBinding myView;
    private MainActivityInterface mainActivityInterface;
    private LinksBottomSheet linksBottomSheet;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsLinksBinding.inflate(inflater, container, false);
        mainActivityInterface.updateToolbar(getString(R.string.link));
        mainActivityInterface.updateToolbarHelp(getString(R.string.website_link));

        // Set views
        setupViews();

        // Set listeners
        setListeners();

        return myView.getRoot();
    }

    // Make public as can also be called as an update from the MainActivity
    public void setupViews() {
        linksBottomSheet = new LinksBottomSheet("linksFragment",this);
        setCurrentValue(myView.linkYouTube, mainActivityInterface.getSong().getLinkyoutube());
        setCurrentValue(myView.linkOnline, mainActivityInterface.getSong().getLinkweb());
        setCurrentValue(myView.linkAudio, mainActivityInterface.getSong().getLinkaudio());
        setCurrentValue(myView.linkOther, mainActivityInterface.getSong().getLinkother());

    }

    private void setCurrentValue(MaterialTextView materialTextView, String songValue) {
        if (songValue==null || songValue.isEmpty()) {
            materialTextView.setHint(getString(R.string.link_choose));
        } else {
            materialTextView.setHint(songValue);
        }
    }

    private void setListeners() {
        myView.linkYouTube.setOnClickListener(view -> openBottomSheet("linkYouTube"));
        myView.linkOnline.setOnClickListener(view -> openBottomSheet("linkOnline"));
        myView.linkAudio.setOnClickListener(view -> openBottomSheet("linkAudio"));
        myView.linkOther.setOnClickListener(view -> openBottomSheet("linkOther"));
    }

    private void openBottomSheet(String what) {
        mainActivityInterface.setWhattodo(what);
        linksBottomSheet.show(requireActivity().getSupportFragmentManager(),"linksBottomSheet");
    }

}
