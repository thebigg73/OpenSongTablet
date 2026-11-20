package com.garethevans.church.opensongtablet.links;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.BottomSheetCommon;
import com.garethevans.church.opensongtablet.databinding.BottomSheetLinksViewBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class LinksViewBottomSheet extends BottomSheetCommon {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private static final String TAG = "LinksViewBottomSheet";
    private BottomSheetLinksViewBinding myView;
    private MainActivityInterface mainActivityInterface;
    private String not_set_string="";
    private String linkAudio, linkYouTube, linkWeb, linkOther, linkMultiTrack;
    private boolean linkAudioValid, linkYouTubeValid, linkWebValid, linkOtherValid, linkMultiTrackValid;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetLinksViewBinding.inflate(inflater, container, false);

        myView.dialogHeading.setClose(this);

        prepareStrings();

        setupViews();

        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            not_set_string = getString(R.string.is_not_set) + " - " +
                    getString(R.string.long_press_to_edit);
        }
    }

    private void setupViews() {
        if (mainActivityInterface!=null && mainActivityInterface.getSong()!=null) {
            linkAudio = mainActivityInterface.getSong().getLinkaudio();
            linkYouTube = mainActivityInterface.getSong().getLinkyoutube();
            linkWeb = mainActivityInterface.getSong().getLinkweb();
            linkOther = mainActivityInterface.getSong().getLinkother();
            linkMultiTrack = mainActivityInterface.getSong().getTitle();
            if (mainActivityInterface.getStorageAccess().isIMGorPDF(linkMultiTrack)) {
                try {
                    linkMultiTrack = linkMultiTrack.substring(0, linkMultiTrack.lastIndexOf(".")).replace(".", "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            linkAudioValid = linkAudio!=null && !linkAudio.isEmpty();
            linkYouTubeValid = linkYouTube!=null && !linkYouTube.isEmpty();
            linkWebValid = linkWeb!=null && !linkWeb.isEmpty();
            linkOtherValid = linkOther!=null && !linkOther.isEmpty();
            Uri linkMultiTrackUri = mainActivityInterface.getStorageAccess().getUriForItem("Multitrack","",linkMultiTrack);
            linkMultiTrackValid = linkMultiTrack!=null && mainActivityInterface.getStorageAccess().uriExists(linkMultiTrackUri);

            mainActivityInterface.getMyThemeColors().tintPopup(myView.linkAudioLayout,mainActivityInterface.getPalette().secondary);
            mainActivityInterface.getMyThemeColors().tintPopup(myView.linkYouTubeLayout,mainActivityInterface.getPalette().secondary);
            mainActivityInterface.getMyThemeColors().tintPopup(myView.linkOnlineLayout,mainActivityInterface.getPalette().secondary);
            mainActivityInterface.getMyThemeColors().tintPopup(myView.linkOtherLayout,mainActivityInterface.getPalette().secondary);
            mainActivityInterface.getMyThemeColors().tintPopup(myView.linkMultiTrackLayout,mainActivityInterface.getPalette().secondary);

            myView.linkAudioLayout.setPaddingRelative(16,16,16,16);
            myView.linkYouTubeLayout.setPaddingRelative(16,16,16,16);
            myView.linkOnlineLayout.setPaddingRelative(16,16,16,16);
            myView.linkOtherLayout.setPaddingRelative(16,16,16,16);
            myView.linkMultiTrackLayout.setPaddingRelative(16,16,16,16);

            myView.linkAudioText.setTextColor(mainActivityInterface.getPalette().hintColor);
            myView.linkYouTubeText.setTextColor(mainActivityInterface.getPalette().hintColor);
            myView.linkOnlineText.setTextColor(mainActivityInterface.getPalette().hintColor);
            myView.linkOtherText.setTextColor(mainActivityInterface.getPalette().hintColor);
            myView.linkMultiTrackText.setTextColor(mainActivityInterface.getPalette().hintColor);

            if (linkAudioValid) {
                myView.linkAudioText.setText(linkAudio);
            } else {
                myView.linkAudioText.setText(not_set_string);
            }

            if (linkYouTubeValid) {
                myView.linkYouTubeText.setText(linkYouTube);
            } else {
                myView.linkYouTubeText.setText(not_set_string);
            }

            if (linkWebValid) {
                myView.linkOnlineText.setText(linkWeb);
            } else {
                myView.linkOnlineText.setText(not_set_string);
            }

            if (linkOtherValid) {
                myView.linkOtherText.setText(linkOther);
            } else {
                myView.linkOtherText.setText(not_set_string);
            }

            myView.linkMultiTrackLayout.setVisibility(linkMultiTrackValid ? View.VISIBLE:View.GONE);
            if (linkMultiTrackValid) {
                String location = "Multitrack/"+linkMultiTrack;
                myView.linkMultiTrackText.setText(location);
            }
        }
    }

    private void setupListeners() {
        myView.linkAudioLayout.setOnClickListener(button -> {
            if (linkAudioValid) {
                mainActivityInterface.popupAudioPlayer(linkAudio);
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mainActivityInterface.setWhattodo("linkAudio");
                showLinkPicker();
            }
        });

        myView.linkAudioLayout.setOnLongClickListener(view -> {
            mainActivityInterface.setWhattodo("linkAudio");
            showLinkPicker();
            return true;
        });

        myView.linkYouTubeLayout.setOnClickListener(button -> {
            if (linkYouTubeValid) {
                mainActivityInterface.openDocument(linkYouTube);
            } else {
                mainActivityInterface.setWhattodo("linkYouTube");
                showLinkPicker();
            }
        });

        myView.linkYouTubeLayout.setOnLongClickListener(view -> {
            mainActivityInterface.setWhattodo("linkYouTube");
            showLinkPicker();
            return true;
        });

        myView.linkOnlineLayout.setOnClickListener(button -> {
            if (linkWebValid) {
                mainActivityInterface.openDocument(linkWeb);
            } else {
                mainActivityInterface.setWhattodo("linkOnline");
                showLinkPicker();
            }
        });

        myView.linkOnlineLayout.setOnLongClickListener(view -> {
            mainActivityInterface.setWhattodo("linkOnline");
            showLinkPicker();
            return true;
        });

        myView.linkOtherLayout.setOnClickListener(button -> {
            if (linkOtherValid) {
                mainActivityInterface.openDocument(linkOther);
            } else {
                mainActivityInterface.setWhattodo("linkOther");
                showLinkPicker();
            }
        });

        myView.linkOtherLayout.setOnLongClickListener(view -> {
            mainActivityInterface.setWhattodo("linkOther");
            showLinkPicker();
            return true;
        });

        myView.linkMultiTrackLayout.setOnClickListener(button -> {
            mainActivityInterface.displayMultiTrack();
            try {
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void showLinkPicker() {
        LinksBottomSheet linksBottomSheet = new LinksBottomSheet(null,null);
        linksBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"LinksBottomSheet");
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
        mainActivityInterface.setWhattodo("");
    }
}
