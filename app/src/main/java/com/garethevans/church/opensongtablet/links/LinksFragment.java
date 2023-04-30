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
import com.garethevans.church.opensongtablet.customviews.MyMaterialTextView;
import com.garethevans.church.opensongtablet.databinding.SettingsLinksBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class LinksFragment extends Fragment {

    private SettingsLinksBinding myView;
    private MainActivityInterface mainActivityInterface;
    private LinksBottomSheet linksBottomSheet;
    private String link_string="", website_link_string="", link_choose_string="";
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(link_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsLinksBinding.inflate(inflater, container, false);

        prepareStrings();

        webAddress = website_link_string;

        // Set views
        setupViews();

        // Set listeners
        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            link_string = getString(R.string.link);
            website_link_string = getString(R.string.website_link);
            link_choose_string = getString(R.string.link_choose);
        }
    }
    // Make public as can also be called as an update from the MainActivity
    public void setupViews() {
        linksBottomSheet = new LinksBottomSheet("linksFragment",this);
        setCurrentValue(myView.linkYouTube, mainActivityInterface.getSong().getLinkyoutube());
        setCurrentValue(myView.linkOnline, mainActivityInterface.getSong().getLinkweb());
        setCurrentValue(myView.linkAudio, mainActivityInterface.getSong().getLinkaudio());
        setCurrentValue(myView.linkOther, mainActivityInterface.getSong().getLinkother());

    }

    private void setCurrentValue(MyMaterialTextView myMaterialTextView, String songValue) {
        if (songValue==null || songValue.isEmpty()) {
            myMaterialTextView.setHint(link_choose_string);
        } else {
            myMaterialTextView.setHint(songValue);
        }
    }

    private void setListeners() {
        myView.linkYouTube.setOnClickListener(view -> openBottomSheet("linkYouTube"));
        myView.linkOnline.setOnClickListener(view -> openBottomSheet("linkOnline"));
        myView.linkAudio.setOnClickListener(view -> openBottomSheet("linkAudio"));
        myView.linkOther.setOnClickListener(view -> openBottomSheet("linkOther"));
    }

    private void openBottomSheet(String what) {
        if (getActivity() != null) {
            mainActivityInterface.setWhattodo(what);
            linksBottomSheet.show(getActivity().getSupportFragmentManager(), "linksBottomSheet");
        }
    }

}
